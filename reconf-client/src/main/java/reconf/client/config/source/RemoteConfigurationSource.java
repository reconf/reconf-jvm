/*
 *    Copyright 2013-2014 ReConf Team
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
import reconf.infra.http.*;
import reconf.infra.i18n.*;


public class RemoteConfigurationSource implements ConfigurationSource {

    private static final MessagesBundle msg = MessagesBundle.getBundle(RemoteConfigurationSource.class);
    private final String key;
    private final ServerStub stub;
    private final ConfigurationAdapter adapter;

    public RemoteConfigurationSource(String key, ServerStub stub, ConfigurationAdapter adapter) {
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

    public String get() throws Throwable {
        return stub.get(key);
    }

    public ConfigurationAdapter getAdapter() {
        return adapter;
    }

    public boolean update(String value) {
        throw new UnsupportedOperationException();
    }

    public void temporaryUpdate(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNew(String value) {
        throw new UnsupportedOperationException();
    }
}
