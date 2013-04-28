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
import org.apache.commons.lang.*;
import reconf.client.elements.*;
import reconf.infra.i18n.*;
import reconf.infra.log.*;
import reconf.infra.throwables.*;
import reconf.infra.io.*;
import reconf.infra.io.InputStreamReader;
import reconf.infra.xml.*;


public class XmlConfigurationHolder {

    private static final String RECONF_DEFAULT_FILE = "reconf.xml";
    private static final String SYSTEM_PROPERTY = "reconf.client.xml.location";
    private static final ConfigurationElement config;
    private static final DatabaseManager mgr;
    private static final MessagesBundle msg = MessagesBundle.getBundle(XmlConfigurationHolder.class);

    static {
        try {
            String raw = null;

            String prop = System.getProperty(SYSTEM_PROPERTY);
            if (StringUtils.isNotBlank(prop)) {
                LoggerHolder.getLog().info(msg.format("system.property.found", SYSTEM_PROPERTY, prop));
                raw = InputStreamReader.read(new FileInputStream(new File(prop)));

            } else {
                LoggerHolder.getLog().info(msg.format("system.property.not.found", RECONF_DEFAULT_FILE));
                raw = ClasspathReader.read(RECONF_DEFAULT_FILE);
            }
            if (StringUtils.isBlank(raw)) {
                throw new ReConfInitializationError(msg.get("file.empty.not.found"));
            }

            LoggerHolder.getLog().info(msg.get("file.load"));
            config = Serializer.fromXml(raw, ConfigurationElement.class);

            LoggerHolder.getLog().info(msg.get("db.setup"));
            mgr = new DatabaseManager(config.getBackupLocation());

        } catch (Throwable t) {
            throw new ReConfInitializationError(t);
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
