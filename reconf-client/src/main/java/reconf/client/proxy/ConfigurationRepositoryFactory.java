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
import java.util.concurrent.locks.*;
import reconf.client.annotations.*;
import reconf.client.config.update.*;
import reconf.client.elements.*;
import reconf.client.factory.*;
import reconf.client.locator.*;
import reconf.client.setup.*;
import reconf.infra.i18n.*;

public class ConfigurationRepositoryFactory implements InvocationHandler {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ConfigurationRepositoryFactory.class);
    private ConfigurationRepositoryUpdater updater;
    private static ConfigurationRepositoryElementFactory factory;
    private static final ReentrantLock lock = new ReentrantLock();
    private static ConcurrentMap<String, Object> cache = new ConcurrentHashMap<String, Object>();

    public static synchronized <T> T get(Class<T> arg) {
        setUpIfNeeded();

        String key = arg.getName();
        if (cache.containsKey(key)) {
            return (T) cache.get(key);
        }

        Object result = newInstance(arg, Environment.getFactory().create(arg));
        cache.putIfAbsent(key, result);
        return (T) result;
    }

    public static synchronized <T> T get(Class<T> arg, Customization customization) {
        setUpIfNeeded();

        String key = arg.getName() + (customization == null ? "" : customization);
        if (cache.containsKey(key)) {
            return (T) cache.get(key);
        }

        ConfigurationRepositoryElement repo = Environment.getFactory().create(arg);
        if (customization == null) {
            customization = Customization.EMPTY;
        }

        repo.setListeners(customization.getCallbackListeners());
        repo.setComponent(customization.getCustomComponent(repo.getComponent()));
        repo.setProduct(customization.getCustomProduct(repo.getProduct()));

        for (ConfigurationItemElement item : repo.getConfigurationItems()) {
            item.setProduct(repo.getProduct());
            item.setComponent(customization.getCustomComponent(item.getComponent()));
            item.setValue(customization.getCustomItem(item.getValue()));
        }

        Object result = newInstance(arg, repo);
        cache.putIfAbsent(key, result);
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

        if (thread.shouldReload()) {
            thread.start();
        }

        return (T) Proxy.newProxyInstance(arg.getClassLoader(), new Class<?>[] {arg}, factory);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        boolean updateAnnotationPresent = method.isAnnotationPresent(UpdateConfigurationRepository.class);
        boolean configurationAnnotationPresent = method.isAnnotationPresent(ConfigurationItem.class);

        if (!configurationAnnotationPresent && !updateAnnotationPresent) {
            throw new IllegalArgumentException(msg.format("error.method", method));
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