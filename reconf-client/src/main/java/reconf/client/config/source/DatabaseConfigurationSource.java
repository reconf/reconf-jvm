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
package reconf.client.config.source;

import java.lang.reflect.*;
import org.apache.commons.lang.*;
import reconf.client.adapters.*;
import reconf.client.locator.*;
import reconf.client.setup.*;
import reconf.infra.i18n.*;
import reconf.infra.log.*;


public class DatabaseConfigurationSource implements ConfigurationSource {

    private static final MessagesBundle msg = MessagesBundle.getBundle(DatabaseConfigurationSource.class);
    private final String fullProperty;
    private final ConfigurationAdapter adapter;
    private final Method method;
    private final ServiceLocator locator;

    public DatabaseConfigurationSource(String fullProperty, Method method, ConfigurationAdapter adapter, ServiceLocator locator) {

        if (StringUtils.isBlank(fullProperty)) {
            throw new NullPointerException(msg.get("error.stub"));
        }

        if (null == adapter) {
            adapter = ConfigurationAdapter.noConfigurationAdapter;
        }

        if (null == method) {
            throw new NullPointerException(msg.get("error.method"));
        }

        this.fullProperty = fullProperty;
        this.adapter = adapter;
        this.method = method;
        this.locator = locator;
    }

    public String get() {
        try {
            DatabaseManager proxy = locator.databaseManagerLocator().find();
            return proxy.get(fullProperty, method);

        } catch (Throwable t) {
            LoggerHolder.getLog().error(msg.format("error.read", getClass().getName()), t);
        }
        return null;
    }

    public boolean isNew(String value) {
        try {
            DatabaseManager manager = locator.databaseManagerLocator().find();
            return manager.isNew(fullProperty, method, value);

        } catch (Throwable t) {
            LoggerHolder.getLog().error(msg.format("error.read", getClass().getName()), t);
        }
        return false;
    }

    public void temporaryUpdate(String value) {
        try {
            DatabaseManager manager = locator.databaseManagerLocator().find();
            manager.temporaryUpsert(fullProperty, method, value);

        } catch (Throwable t) {
            LoggerHolder.getLog().error(msg.get("error.save"), t);
        }
    }

    public ConfigurationAdapter getAdapter() {
        return adapter;
    }
}
