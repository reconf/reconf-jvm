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

import java.lang.reflect.*;
import java.util.concurrent.*;
import org.apache.commons.lang.*;
import reconf.client.adapters.*;
import reconf.client.config.source.*;
import reconf.client.elements.*;
import reconf.client.factory.*;
import reconf.client.setup.*;
import reconf.infra.http.*;
import reconf.infra.i18n.*;


public class MethodConfiguration {

    private static final MessagesBundle msg = MessagesBundle.getBundle(MethodConfiguration.class);

    public enum ReloadStrategy {
        NONE, ATOMIC, INDEPENDENT;
    }

    private final ConfigurationRepositoryElement cfgRepository;
    private final ConfigurationItemElement remoteItem;
    private final ServerStub stub;
    private final FactoryLocator locator;

    public MethodConfiguration(ConfigurationRepositoryElement cfgRepository, ConfigurationItemElement itemConfiguration, FactoryLocator locator) {
        this.cfgRepository = cfgRepository;
        this.remoteItem = itemConfiguration;
        this.locator = locator;
        this.stub = createStub();
    }

    public ConfigurationSourceHolder getConfigurationSourceHolder() {
        try {
            ConfigurationAdapter adapter = getRemoteAdapter();
            ConfigurationSourceHolder holder = new ConfigurationSourceHolder(new RemoteConfigurationSource(remoteItem.getValue(), stub, adapter),
                    new DatabaseConfigurationSource(stub.getProduct(), stub.getComponent(), getMethod(), remoteItem.getValue(), adapter));
            return holder;

        } catch (Throwable t) {
            throw new IllegalStateException(msg.get("error.source"), t);
        }
    }

    private ConfigurationAdapter getRemoteAdapter() {
        if (null == remoteItem || remoteItem.getAdapter() == null) {
            return null;
        }
        try {
            Constructor<? extends ConfigurationAdapter> constructor = remoteItem.getAdapter().getConstructor(ArrayUtils.EMPTY_CLASS_ARRAY);
            return constructor.newInstance(ArrayUtils.EMPTY_OBJECT_ARRAY);
        } catch (Throwable t) {
            throw new IllegalArgumentException(msg.get("error.adapter"), t);
        }
    }

    private ServerStub createStub() {
        ConnectionSettings settings = cfgRepository.getConnectionSettings();
        ServerStub stub = locator.serverStubFactory().create(settings.getUrl(), settings.getTimeout(), settings.getTimeUnit(), settings.getMaxRetry());
        stub.setComponent(StringUtils.isNotBlank(remoteItem.getComponent()) ? remoteItem.getComponent() : cfgRepository.getComponent());
        stub.setProduct(StringUtils.isNotBlank(remoteItem.getProduct()) ? remoteItem.getProduct() : cfgRepository.getProduct());
        return stub;
    }

    public int getReloadInterval() {
        switch (getReloadStrategy()) {
        case ATOMIC : return cfgRepository.getUpdateFrequency().getInterval();
        case INDEPENDENT : return remoteItem.getUpdateFrequency().getInterval();
        case NONE : return 0;
        default : throw new IllegalStateException(msg.get("error.internal"));
        }
    }

    public TimeUnit getReloadTimeUnit() {
        switch (getReloadStrategy()) {
        case ATOMIC : return cfgRepository.getUpdateFrequency().getTimeUnit();
        case INDEPENDENT : return remoteItem.getUpdateFrequency().getTimeUnit();
        case NONE : return TimeUnit.DAYS;
        default : throw new IllegalStateException(msg.get("error.internal"));
        }
    }

    public ReloadStrategy getReloadStrategy() {
        if (remoteItem.getDoNotUpdate() != null) {
            return ReloadStrategy.NONE;
        }
        if (remoteItem.getUpdateFrequency() != null) {
            return ReloadStrategy.INDEPENDENT;
        }

        if (cfgRepository.getDoNotUpdate() != null) {
            return ReloadStrategy.NONE;
        }
        return (null != cfgRepository.getUpdateFrequency()) ? ReloadStrategy.ATOMIC : ReloadStrategy.NONE;
    }

    public Method getMethod() {
        return remoteItem.getMethod();
    }

    public ServerStub getServerStub() {
        return stub;
    }
}
