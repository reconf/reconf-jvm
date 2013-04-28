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

import org.apache.commons.lang.*;
import reconf.client.adapters.*;
import reconf.client.http.*;
import reconf.infra.i18n.*;
import reconf.infra.log.*;


public class RemoteConfigurationSource implements ConfigurationSource {

    private static final MessagesBundle msg = MessagesBundle.getBundle(RemoteConfigurationSource.class);
    private final String key;
    private final ProxyFactoryRemoteConfigStub stub;
    private final ConfigurationAdapter adapter;

    public RemoteConfigurationSource(String key, ProxyFactoryRemoteConfigStub stub, ConfigurationAdapter adapter) {
        if (null == stub) {
            throw new NullPointerException(msg.get("error.stub"));
        }
        if (StringUtils.isBlank(key)) {
            throw new NullPointerException(msg.get("error.key"));
        }
        if (null == adapter) {
            adapter = ConfigurationAdapter.noConfigurationAdapter;
        }

        this.key = key;
        this.stub = stub;
        this.adapter = adapter;
    }

    public String get() {
        try {
            return stub.get(key);
        } catch (Throwable t) {
            LoggerHolder.getLog().error(msg.format("error.load", getClass().getName()), t);
        }
        return null;
    }

    public ConfigurationAdapter getAdapter() {
        return adapter;
    }

    public void update(String value) {
        throw new UnsupportedOperationException();
    }

    public void temporaryUpdate(String value) {
        throw new UnsupportedOperationException();
    }
}
