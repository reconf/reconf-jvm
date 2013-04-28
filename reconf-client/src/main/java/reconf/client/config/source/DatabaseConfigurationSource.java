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
package reconf.client.config.source;

import java.lang.reflect.*;
import org.apache.commons.lang.*;
import reconf.client.adapters.*;
import reconf.client.proxy.*;
import reconf.infra.i18n.*;
import reconf.infra.log.*;


public class DatabaseConfigurationSource implements ConfigurationSource {

    private static final MessagesBundle msg = MessagesBundle.getBundle(DatabaseConfigurationSource.class);
    private final String product;
    private final String component;
    private final String key;
    private final ConfigurationAdapter adapter;
    private final Method method;

    public DatabaseConfigurationSource(String product, String component, Method method, String key, ConfigurationAdapter adapter) {

        if (StringUtils.isBlank(key)) {
            throw new NullPointerException(msg.get("error.stub"));
        }

        if (StringUtils.isBlank(product)) {
            throw new NullPointerException(msg.get("error.product"));
        }

        if (StringUtils.isBlank(component)) {
            throw new NullPointerException(msg.get("error.component"));
        }

        if (null == adapter) {
            adapter = ConfigurationAdapter.noConfigurationAdapter;
        }

        if (null == method) {
            throw new NullPointerException(msg.get("error.method"));
        }

        this.key = key;
        this.product = product;
        this.component = component;
        this.adapter = adapter;
        this.method = method;
    }

    public String get() {
        try {
            DatabaseManager proxy = XmlConfigurationHolder.getManager();
            return proxy.get(product, component, method, key);

        } catch (Throwable t) {
            LoggerHolder.getLog().error(msg.format("error.load", getClass().getName()), t);
        }
        return null;
    }

    public void update(String value) {
        try {
            DatabaseManager manager = XmlConfigurationHolder.getManager();
            manager.upsert(product, component, method, key, value);

        } catch (Throwable t) {
            LoggerHolder.getLog().error(msg.get("error.save"), t);
        }
    }

    public void temporaryUpdate(String value) {
        try {
            DatabaseManager manager = XmlConfigurationHolder.getManager();
            manager.temporaryUpsert(product, component, method, key, value);

        } catch (Throwable t) {
            LoggerHolder.getLog().error(msg.get("error.save"), t);
        }
    }

    public ConfigurationAdapter getAdapter() {
        return adapter;
    }
}
