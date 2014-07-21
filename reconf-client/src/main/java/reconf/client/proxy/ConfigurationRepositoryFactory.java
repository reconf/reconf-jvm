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
package reconf.client.proxy;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import org.apache.commons.collections.*;
import reconf.client.annotations.*;
import reconf.client.config.update.*;
import reconf.client.elements.*;
import reconf.client.factory.*;
import reconf.client.locator.*;
import reconf.client.notification.*;
import reconf.client.setup.*;
import reconf.infra.i18n.*;
import reconf.infra.log.*;
import reconf.infra.system.*;

public class ConfigurationRepositoryFactory implements InvocationHandler {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ConfigurationRepositoryFactory.class);
    private ConfigurationRepositoryUpdater updater;
    private static ConfigurationRepositoryElementFactory factory;
    private static final ReentrantLock lock = new ReentrantLock();
    private static ConcurrentMap<String, Object> cache = new ConcurrentHashMap<String, Object>();
    private static ConcurrentMap<String, Collection<? extends ConfigurationItemListener>> listenerCache = new ConcurrentHashMap<String, Collection<? extends ConfigurationItemListener>>();

    public static synchronized <T> T get(Class<T> arg) {
        return get(arg, null, null);
    }

    public static synchronized <T> T get(Class<T> arg, Customization customization) {
        return get(arg, customization, null);
    }

    public static synchronized <T> T get(Class<T> arg, Collection<? extends ConfigurationItemListener> configurationItemListeners) {
        return get(arg, null, configurationItemListeners);
    }

    public static synchronized <T> T get(Class<T> arg, Customization customization, Collection<? extends ConfigurationItemListener> configurationItemListeners) {
        setUpIfNeeded();

        if (customization == null) {
            customization = new Customization();
        }
        if (configurationItemListeners == null) {
            configurationItemListeners = Collections.EMPTY_LIST;
        }

        String key = arg.getName() + customization;
        if (cache.containsKey(key)) {
            if (CollectionUtils.isEqualCollection(configurationItemListeners, listenerCache.get(key))) {
                LoggerHolder.getLog().info(msg.format("cached.instance", arg.getName()));
                return (T) cache.get(key);
            }

            throw new IllegalArgumentException(msg.format("error.customization", arg.getName()));
        }

        ConfigurationRepositoryElement repo = Environment.getFactory().create(arg);
        repo.setCustomization(customization);
        repo.setComponent(customization.getCustomComponent(repo.getComponent()));
        repo.setProduct(customization.getCustomProduct(repo.getProduct()));
        if (configurationItemListeners != null) {
            for (ConfigurationItemListener listener : configurationItemListeners) {
                repo.addConfigurationItemListener(listener);
            }
        }

        for (ConfigurationItemElement item : repo.getConfigurationItems()) {
            item.setProduct(repo.getProduct());
            item.setComponent(customization.getCustomComponent(item.getComponent()));
            item.setValue(customization.getCustomItem(item.getValue()));
        }

        LoggerHolder.getLog().info(msg.format("new.instance", LineSeparator.value(), repo.toString()));

        Object result = newInstance(arg, repo);
        cache.put(key, result);
        listenerCache.put(key, configurationItemListeners);
        return (T) result;
    }

    private static synchronized void setUpIfNeeded() {
        if (factory != null) {
            return;
        }
        boolean locked = lock.tryLock();
        if (!locked) {
            return;
        }
        try {
            Environment.setUp();
            factory = Environment.getFactory();
        } finally {
            lock.unlock();
        }
    }

    private static synchronized <T> T newInstance(Class<T> arg, ConfigurationRepositoryElement repo) {
        ConfigurationRepositoryFactory factory = new ConfigurationRepositoryFactory();
        ConfigurationRepositoryUpdater thread = new ConfigurationRepositoryUpdater(repo, ServiceLocator.defaultImplementation, factory);
        Environment.addThreadToCheck(thread);
        thread.start();
        return (T) Proxy.newProxyInstance(arg.getClassLoader(), new Class<?>[] {arg}, factory);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        boolean updateAnnotationPresent = method.isAnnotationPresent(UpdateConfigurationRepository.class);
        boolean configurationAnnotationPresent = method.isAnnotationPresent(ConfigurationItem.class);

        if (!configurationAnnotationPresent && !updateAnnotationPresent) {
            return method.invoke(proxy, args);
        }

        if (updateAnnotationPresent) {
            updater.syncNow(method.getAnnotation(UpdateConfigurationRepository.class).onErrorThrow());
        }

        Object configValue = null;

        if(configurationAnnotationPresent) {
            configValue = updater.getValueOf(method);
        }

        return configValue;
    }

    public void setUpdater(ConfigurationRepositoryUpdater updater) {
        this.updater = updater;
    }
}