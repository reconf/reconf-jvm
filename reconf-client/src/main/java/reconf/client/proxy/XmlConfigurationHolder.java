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
package reconf.client.proxy;

import java.io.*;
import javax.xml.parsers.*;
import org.apache.commons.io.*;
import org.apache.commons.lang.*;
import org.w3c.dom.*;
import reconf.client.elements.*;
import reconf.infra.i18n.*;
import reconf.infra.io.*;
import reconf.infra.io.InputStreamReader;
import reconf.infra.log.*;
import reconf.infra.throwables.*;
import reconf.infra.xml.*;


public class XmlConfigurationHolder {

    private static final String RECONF_DEFAULT_FILE = "reconf.xml";
    private static final String SYSTEM_PROPERTY = "reconf.client.xml.location";
    private static final ConfigurationElement config;
    private static final DatabaseManager mgr;
    private static MessagesBundle msg;

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

            findLocale(raw);
            msg = MessagesBundle.getBundle(XmlConfigurationHolder.class);

            LoggerHolder.getLog().info("configuration file read successfully. setting up execution environment");
            config = Serializer.fromXml(raw, ConfigurationElement.class);

            System.setProperty("reconf.locale", config.getLocale());


            LoggerHolder.getLog().info(msg.get("db.setup"));
            mgr = new DatabaseManager(config.getBackupLocation());

        } catch (Throwable t) {
            throw new ReConfInitializationError(t);
        }
    }


    private static void findLocale(String raw) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(IOUtils.toInputStream(raw));
        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getElementsByTagName("locale");
        if (nodeList != null && nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            if (node != null && node.getFirstChild() != null) {
                MessagesBundle.setLocale(node.getFirstChild().getTextContent());
            }
        }
    }


    public static void init() {
        LoggerHolder.getLog().info(msg.get("start"));
    }

    public static DatabaseManager getManager() {
        return mgr;
    }

    public static ConfigurationElement getOverrideConfiguration() {
        return config;
    }
}
