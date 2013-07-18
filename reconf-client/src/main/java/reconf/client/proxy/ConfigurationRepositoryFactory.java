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
import java.util.*;
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

    private static final Map<Class<?>, Map<Customization, Object>> cachedProxies = new ConcurrentHashMap<Class<?>, Map<Customization, Object>>();
    
    public static synchronized <T> T create(Class<T> arg) {
        return create(arg, null);
    }

    @SuppressWarnings("unchecked")
    public static synchronized <T> T create(Class<T> arg, Customization customization) {
        Map<Customization, Object> classProxies = cachedProxies.get(arg);
        T proxy = null;
        
        if (customization == null) {
            customization = Customization.EMPTY;
        }
        
        if(null == classProxies) {
            classProxies = new ConcurrentHashMap<Customization, Object>();
            cachedProxies.put(arg, classProxies);
        }

        proxy = (T) classProxies.get(customization);            
        
        if(null == proxy) {            
            proxy = createNonCachedProxy(arg, customization);        
            classProxies.put(customization, proxy);
        }
                
        return proxy;
    }

    /**
     * @param arg
     * @param customization
     */
    private static <T> T createNonCachedProxy(Class<T> arg, Customization customization) {
        setUpIfNeeded();
        
        ConfigurationRepositoryElement repo = Environment.getFactory().create(arg);
        
        repo.setComponent(customization.getCustomComponent(repo.getComponent()));
        for (ConfigurationItemElement item : repo.getConfigurationItems()) {
            item.setProduct(repo.getProduct());
            item.setComponent(customization.getCustomComponent(item.getComponent()));
            item.setValue(customization.getCustomItem(item.getValue()));
        }
        return newInstance(arg, repo);
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
        factory.updater = new ConfigurationRepositoryUpdater(repo, ServiceLocator.defaultImplementation);
        ScheduledExecutorService service = null;
        if (factory.updater.shouldReload()) {
             service = Executors.newScheduledThreadPool(1);
             service.scheduleAtFixedRate(factory.updater, factory.updater.getReloadInterval(), factory.updater.getReloadInterval(), factory.updater.getReloadTimeUnit());
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
}