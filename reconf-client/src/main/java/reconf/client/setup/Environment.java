/*
 *    Copyright 1996-2014 UOL Inc
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
import java.util.*;
import org.apache.commons.collections.*;
import org.apache.commons.lang.*;
import reconf.client.check.*;
import reconf.client.config.update.*;
import reconf.client.factory.*;
import reconf.client.validation.*;
import reconf.infra.http.*;
import reconf.infra.i18n.*;
import reconf.infra.io.*;
import reconf.infra.io.InputStreamReader;
import reconf.infra.log.*;
import reconf.infra.system.*;
import reconf.infra.throwables.*;


public class Environment {

    private static final String RECONF_DEFAULT_FILE = "reconf.xml";
    private static final String SYSTEM_PROPERTY = "reconf.client.xml.location";
    private static final XmlConfiguration config;
    private static final ConfigurationRepositoryElementFactory factory;
    private static DatabaseManager mgr;
    private static MessagesBundle msg;
    private static ObserverThread checker;

    static {
        try {
            String raw = null;

            String prop = System.getProperty(SYSTEM_PROPERTY);
            if (StringUtils.isNotBlank(prop)) {
                LoggerHolder.getLog().info(String.format("system property [%] found. trying to read file [%s]", SYSTEM_PROPERTY, prop));
                raw = InputStreamReader.read(new FileInputStream(new File(prop)));

            } else {
                LoggerHolder.getLog().info(String.format("trying to read file [%s] from classpath", RECONF_DEFAULT_FILE));
                raw = ClasspathReader.read(RECONF_DEFAULT_FILE);
            }
            if (StringUtils.isBlank(raw)) {
                throw new ReConfInitializationError("configuration file is either empty or could not be found");
            }

            XmlConfigurationParser parser = XmlConfigurationParser.from(raw);
            LocaleHolder.set(parser.getLocale());

            config = new XmlConfiguration();
            config.setAnnotationOverride(parser.getAnnotationOverride());
            config.setConnectionSettings(parser.getConnectionSettings());
            config.setLocalCacheSettings(parser.getLocalCacheSettings());
            config.setDebug(parser.isDebugEnabled());
            config.setExperimentalFeatures(parser.isExperimentalFeatures());

            msg = MessagesBundle.getBundle(Environment.class);
            LoggerHolder.getLog().info(msg.get("file.load"));

            validate(config);
            LoggerHolder.getLog().info(msg.format("configured", LineSeparator.value(), config.toString()));

            factory = new ConfigurationRepositoryElementFactory(config);
            LoggerHolder.getLog().info(msg.get("db.setup"));
            mgr = new DatabaseManager(config.getLocalCacheSettings());

            LoggerHolder.getLog().info(msg.format("instance.name", LocalHostname.getName()));
            checker = new ObserverThread();
            checker.start();

            if (config.isExperimentalFeatures()) {
            }

        } catch (ReConfInitializationError e) {
            if (mgr != null) {
                mgr.shutdown();
            }
            throw e;

        } catch (Throwable t) {
            if (mgr != null) {
                mgr.shutdown();
            }
            throw new ReConfInitializationError(t);
        }
    }

    private static void validate(XmlConfiguration xmlConfig) {
        if (xmlConfig == null) {
            throw new ReConfInitializationError(msg.get("error.internal"));
        }
        Set<String> violations = XmlConfigurationValidator.validate(xmlConfig);
        if (CollectionUtils.isEmpty(violations)) {
            return;
        }
        List<String> errors = new ArrayList<String>();
        int i = 1;
        for (String violation : violations) {
            errors.add(i++ + " - " + violation);
        }

        if (xmlConfig.isDebug()) {
            LoggerHolder.getLog().error(msg.format("error.xml", LineSeparator.value(), StringUtils.join(errors, LineSeparator.value())));
        } else {
            throw new ReConfInitializationError(msg.format("error.xml", LineSeparator.value(), StringUtils.join(errors, LineSeparator.value())));
        }

    }

    public static void setUp() {
        LoggerHolder.getLog().info(msg.get("start"));
    }

    public static DatabaseManager getManager() {
        return mgr;
    }

    public static ConfigurationRepositoryElementFactory getFactory() {
        return factory;
    }

    public static void addThreadToCheck(ObservableThread thread) {
        if (checker != null) {
            checker.add(thread);
        }
    }

    public static List<SyncResult> syncActiveConfigurationRepositoryUpdaters() {
        List<ObservableThread> toSync = checker.getActiveThreads();
        List<SyncResult> result = new ArrayList<SyncResult>();

        for (ObservableThread thread : toSync) {
            if (!(thread instanceof ConfigurationRepositoryUpdater)) {
                continue;
            }
            ConfigurationRepositoryUpdater updater = (ConfigurationRepositoryUpdater) thread;

            try {
                updater.syncNow(RuntimeException.class);
                result.add(new SyncResult(updater.getName()));

            } catch (Exception e) {
                result.add(new SyncResult(updater.getName(), e));
            }
        }
        return result;
    }
}
