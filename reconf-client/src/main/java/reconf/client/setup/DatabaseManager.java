/*
 *    Copyright 1996-2013 UOL Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package reconf.client.setup;

import java.io.*;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import org.apache.commons.codec.binary.*;
import org.apache.commons.dbcp.*;
import org.apache.commons.io.*;
import org.apache.commons.lang.StringUtils;
import reconf.infra.i18n.*;
import reconf.infra.log.*;
import reconf.infra.shutdown.*;
import reconf.infra.throwables.*;


public class DatabaseManager implements ShutdownBean {

    private static final MessagesBundle msg = MessagesBundle.getBundle(DatabaseManager.class);
    private final File directory;
    private final BasicDataSource dataSource;
    private static final String driverClassName = "org.hsqldb.jdbc.JDBCDriver";

    private final String DEFINITIVE_INSERT = "INSERT INTO PUBLIC.PRD_COMP_CONFIG_V1 (NAM_CLASS, NAM_METHOD, PROD, COMP, PROP, VALUE, UPDATED) VALUES (?,?,?,?,?,?,?)";
    private final String TEMPORARY_INSERT = "INSERT INTO PUBLIC.PRD_COMP_CONFIG_V1 (NAM_CLASS, NAM_METHOD, PROD, COMP, PROP, NEW_VALUE, UPDATED) VALUES (?,?,?,?,?,?,?)";
    private final String DEFINITIVE_UPDATE = "UPDATE PUBLIC.PRD_COMP_CONFIG_V1 SET VALUE = ?, UPDATED = ? WHERE PROD = ? AND COMP = ? AND PROP = ? AND NAM_CLASS = ? AND NAM_METHOD = ?";
    private final String TEMPORARY_UPDATE = "UPDATE PUBLIC.PRD_COMP_CONFIG_V1 SET NEW_VALUE = ?, UPDATED = ? WHERE PROD = ? AND COMP = ? AND PROP = ? AND NAM_CLASS = ? AND NAM_METHOD = ?";
    private final String COMMIT_TEMP_CHANGES = "UPDATE PUBLIC.PRD_COMP_CONFIG_V1 SET VALUE = NEW_VALUE, NEW_VALUE = NULL, UPDATED = ? WHERE PROD IN (%s) AND COMP IN (%s) AND NAM_CLASS = ? AND NEW_VALUE IS NOT NULL";

    private static final String cryptKey;
    static {
        try {
            SecretKeySpec key = new SecretKeySpec("abcdefghijklmnop".getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cryptKey = new String(Hex.encodeHex(cipher.doFinal("remoteconfigdb".getBytes())));
        } catch (Exception e) {
            throw new Error(msg.get("error.crypt.key"));
        }
    }

    public DatabaseManager(LocalCacheSettings config) {
        new ShutdownInterceptor(this).register();
        try {
            if (null == config) {
                throw new ReConfInitializationError(msg.get("error.dir.not.provided"));
            }

            this.directory = config.getBackupLocation();
            provisionBackupDirectory();

            firstConnection();
            dataSource = createDataSource();
            useCompressed(config.isCompressed());
            if (config.getMaxLogFileSize() > 0) {
                logFileSize(config.getMaxLogFileSize());
            }
            if (!tableExists()) {
                createTable();
            }

        } catch (Throwable t) {
            throw new ReConfInitializationError(t);
        }
    }

    private void provisionBackupDirectory() {
        LoggerHolder.getLog().info(msg.format("setup.local.dir", directory));
        if (directory.isFile()) {
            throw new ReConfInitializationError(msg.format("error.local.dir.file", directory));
        }
        if (!directory.exists()) {
            LoggerHolder.getLog().info(msg.format("local.dir.not.found", directory));
            try {
                FileUtils.forceMkdir(directory);
            } catch (Exception e) {
                throw new ReConfInitializationError(e);
            }
            LoggerHolder.getLog().info(msg.format("local.dir.new", directory));
        }
        if (!directory.canRead()) {
            throw new ReConfInitializationError(msg.format("error.local.dir.read", directory));
        }
        if (!directory.canWrite()) {
            throw new ReConfInitializationError(msg.format("error.local.dir.write", directory));
        }
        File parent = directory.getParentFile();
        if (!parent.canRead()) {
            throw new ReConfInitializationError(msg.format("error.local.dir.read", parent));
        }
        if (!parent.canWrite()) {
            throw new ReConfInitializationError(msg.format("error.local.dir.write", parent));
        }
    }

    private BasicDataSource createDataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(driverClassName);
        ds.setUrl("jdbc:hsqldb:file:" + directory.getPath() + ";hsqldb.lock_file=false;hsqldb.crypt_key="+cryptKey+";hsqldb.crypt_type=AES;hsqldb.crypt_lobs=true;hsqldb.ifexists=true;hsqldb.shutdown=true");
        ds.setUsername("reconfdb");
        ds.setPassword("local");
        return ds;
    }

    private boolean tableExists() throws Exception {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            stmt.execute("SELECT 1                               " +
                         "FROM   INFORMATION_SCHEMA.TABLES       " +
                         "WHERE  TABLE_CATALOG = 'PUBLIC'        " +
                         "AND    TABLE_SCHEMA = 'PUBLIC'         " +
                         "AND    TABLE_NAME='PRD_COMP_CONFIG_V1' ");
            return stmt.getResultSet().next();

        } finally {
            close(stmt);
            close(conn);
        }
    }

    public String get(String product, String component, Method method, String key) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement("SELECT VALUE                      " +
                                         "FROM   PUBLIC.PRD_COMP_CONFIG_V1  " +
                                         "WHERE  PROD = ?                   " +
                                         "AND    COMP = ?                   " +
                                         "AND    PROP = ?                   " +
                                         "AND    NAM_CLASS = ?              " +
                                         "AND    NAM_METHOD = ?             ");

            stmt.setString(1, StringUtils.upperCase(product));
            stmt.setString(2, StringUtils.upperCase(component));
            stmt.setString(3, StringUtils.upperCase(key));
            stmt.setString(4, method.getDeclaringClass().getName());
            stmt.setString(5, method.getName());
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getClob(1) == null ? null : rs.getClob(1).getCharacterStream() == null ? null : IOUtils.toString(rs.getClob(1).getCharacterStream());
            }

            return null;

        } catch (Exception e) {
            LoggerHolder.getLog().warn(msg.format("error.db", "get"), e);
            return null;

        } finally {
            close(rs);
            close(stmt);
            close(conn);
        }
    }

    public Map<String, String> getProductComponentPropertyValue() {
        Map<String, String> result = new HashMap<String, String>();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement("SELECT PROD, COMP, PROP, VALUE FROM PUBLIC.PRD_COMP_CONFIG_V1");
            rs = stmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("PROD") + "." + rs.getString("COMP") + "." + rs.getString("PROP"), rs.getString("VALUE"));
            }

        } catch (Exception e) {
            LoggerHolder.getLog().warn(msg.format("error.db", "getProductComponentPropertyValue"), e);
            return Collections.EMPTY_MAP;

        } finally {
            close(rs);
            close(stmt);
            close(conn);
        }
        return result;
    }

    public void upsert(String product, String component, Method method, String key, String value) {
        innerUpsert(product, component, method, key, value, true);
    }

    public void temporaryUpsert(String product, String component, Method method, String key, String value) {
        innerUpsert(product, component, method, key, value, false);
    }

    private void innerUpsert(String product, String component, Method method, String key, String value, boolean definitive) {
        synchronized (dataSource) {
            if (dataSource.isClosed()) {
                return;
            }
        }
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();

            if (needToInsert(product, component, method, key)) {
                stmt = conn.prepareStatement(definitive ? DEFINITIVE_INSERT : TEMPORARY_INSERT);
                stmt.setString(1, method.getDeclaringClass().getName());
                stmt.setString(2, method.getName());
                stmt.setString(3, StringUtils.upperCase(product));
                stmt.setString(4, StringUtils.upperCase(component));
                stmt.setString(5, StringUtils.upperCase(key));
                stmt.setString(6, value);
                stmt.setTimestamp(7, new Timestamp(System.currentTimeMillis()));

            } else {
                stmt = conn.prepareStatement(definitive ? DEFINITIVE_UPDATE : TEMPORARY_UPDATE);
                stmt.setString(1, value);
                stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                stmt.setString(3, StringUtils.upperCase(product));
                stmt.setString(4, StringUtils.upperCase(component));
                stmt.setString(5, StringUtils.upperCase(key));
                stmt.setString(6, method.getDeclaringClass().getName());
                stmt.setString(7, method.getName());
            }

            if (0 == stmt.executeUpdate()) {
                throw new IllegalStateException(msg.get("error.db.update.zero"));
            }

        } catch (Exception e) {
            LoggerHolder.getLog().warn(msg.format("error.db", (definitive ? "upsert" : "temporaryUpsert")), e);
            throw new RuntimeException(e);

        } finally {
            close(stmt);
            close(conn);
        }
    }

    private boolean needToInsert(String product, String component, Method method, String key) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement("SELECT 1                          " +
                                         "FROM   PUBLIC.PRD_COMP_CONFIG_V1  " +
                                         "WHERE  PROD = ?                   " +
                                         "AND    COMP = ?                   " +
                                         "AND    PROP = ?                   " +
                                         "AND    NAM_CLASS = ?              " +
                                         "AND    NAM_METHOD = ?             ");

            stmt.setString(1, StringUtils.upperCase(product));
            stmt.setString(2, StringUtils.upperCase(component));
            stmt.setString(3, StringUtils.upperCase(key));
            stmt.setString(4, method.getDeclaringClass().getName());
            stmt.setString(5, method.getName());
            rs = stmt.executeQuery();

            if (rs.next()) {
                return false;
            }

            return true;

        } catch (Exception e) {
            LoggerHolder.getLog().warn(msg.format("error.db", "needToInsert"), e);
            throw new RuntimeException(e);

        } finally {
            close(rs);
            close(stmt);
            close(conn);
        }
    }


    public void commitTemporaryUpdate(Collection<String> product, Collection<String> component, Class<?> declaringClass) {
        synchronized (dataSource) {
            if (dataSource.isClosed()) {
                return;
            }
        }
        Connection conn = null;
        PreparedStatement stmt = null;
        String qry = String.format(COMMIT_TEMP_CHANGES, StringUtils.join(toUpper(product), ","), StringUtils.join(toUpper(component), ","));

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(qry);
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setString(2, declaringClass.getName());
            int changed = stmt.executeUpdate();
            LoggerHolder.getLog().debug(msg.format("db.update.number", changed));

        } catch (Exception e) {
            LoggerHolder.getLog().warn(msg.format("error.db", "commitTemporaryUpdate"), e);
            throw new RuntimeException(e);

        } finally {
            close(stmt);
            close(conn);
        }
    }

    private Collection<String> toUpper(Collection<String> arg) {
        Set<String> result = new LinkedHashSet<String>();
        for (String str : arg) {
            result.add("'" + StringUtils.upperCase(str) + "'");
        }
        return result;
    }

    private void createTable() throws Exception {
            execute("CREATE TABLE PUBLIC.PRD_COMP_CONFIG_V1                  " +
                    "(NAM_CLASS VARCHAR(255) NOT NULL,                       " +
                    " NAM_METHOD VARCHAR(255) NOT NULL,                      " +
                    " PROD VARCHAR(50) NOT NULL,                             " +
                    " COMP VARCHAR(50) NOT NULL,                             " +
                    " PROP VARCHAR(255) NOT NULL,                            " +
                    " VALUE LONGVARCHAR,                                     " +
                    " NEW_VALUE LONGVARCHAR,                                 " +
                    " UPDATED TIMESTAMP,                                     " +
                    " PRIMARY KEY (NAM_CLASS, NAM_METHOD, PROD, COMP, PROP)) ");
    }

    private synchronized void useCompressed(boolean compressed) throws Exception {
        execute("SET FILES SCRIPT FORMAT " + (compressed ? "COMPRESSED" : "TEXT"));
    }

    private synchronized void logFileSize(int arg) throws Exception {
        execute("SET FILES LOG SIZE " + (arg));
    }

    private synchronized void execute(String cmd) throws Exception {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            stmt.execute(cmd);

        } finally {
            close(stmt);
            close(conn);
        }
    }

    public static void close(Statement arg) {
        if (null == arg) return;
        try {
            arg.close();
        } catch (Exception e) {
        }
    }

    public static void close(ResultSet arg) {
        if (null == arg) return;
        try {
            arg.close();
        } catch (Exception e) {
        }
    }

    public static void close(Connection arg) {
        if (null == arg) return;
        try {
            arg.close();
        } catch (Exception e) {
        }
    }

    private synchronized Connection getConnection() throws SQLException {
        Connection conn = dataSource.getConnection();
        conn.setAutoCommit(true);
        return conn;
    }

    private synchronized void firstConnection() throws SQLException {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(driverClassName);
        ds.setUrl("jdbc:hsqldb:file:" + directory.getPath() + ";hsqldb.lock_file=false;hsqldb.crypt_key="+cryptKey+";hsqldb.crypt_type=AES;hsqldb.crypt_lobs=true;hsqldb.shutdown=true");
        ds.setUsername("reconfdb");
        ds.setPassword("local");
        ds.getConnection().close();
    }

    public synchronized void shutdown() {
        try {
            LoggerHolder.getLog().info(msg.get("db.stopping"));
            execute("SHUTDOWN");
            if (dataSource != null) {
                dataSource.close();
            }
            LoggerHolder.getLog().info(msg.get("db.stopped"));
        } catch (Exception ignored) {
            LoggerHolder.getLog().warn(msg.get("error.db.stopping"), ignored);
        }
    }
}
