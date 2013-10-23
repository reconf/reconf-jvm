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
package reconf.client.config.update;

import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import org.apache.commons.lang.*;
import reconf.client.callback.*;
import reconf.client.check.*;
import reconf.client.elements.*;
import reconf.client.locator.*;
import reconf.client.proxy.*;
import reconf.client.proxy.MethodConfiguration.ReloadStrategy;
import reconf.client.setup.*;
import reconf.infra.i18n.*;
import reconf.infra.log.*;
import reconf.infra.system.*;
import reconf.infra.throwables.*;


public class ConfigurationRepositoryUpdater extends ObservableThread {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ConfigurationRepositoryUpdater.class);
    private final ConfigurationRepositoryElement cfgRepository;
    private final ConfigurationRepositoryData data;
    private Map<Method, Object> independentMethodValue = new ConcurrentHashMap<Method, Object>();
    private Map<Method, Object> atomicMethodValue = new ConcurrentHashMap<Method, Object>();
    private final ConfigurationRepositoryFactory factory;
    private ServiceLocator locator;
    private List<ObservableThread> independentReload = new ArrayList<ObservableThread>();
    private Collection<CallbackListener> listeners = Collections.EMPTY_LIST;

    public ConfigurationRepositoryUpdater(ConfigurationRepositoryElement elem, ServiceLocator locator, ConfigurationRepositoryFactory factory) {
        setDaemon(true);
        this.locator = locator;
        this.factory = factory;
        cfgRepository = elem;
        setName(elem.getInterfaceClass().getName() + "_updater" + new Object().toString().replace("java.lang.Object", ""));
        data = new ConfigurationRepositoryData(elem, locator);
        listeners = elem.getCustomization().getCallbackListeners();

        load();
        scheduleIndependent();
        updateLastExecution();
        factory.setUpdater(this);
    }

    public void syncNow(Class<? extends RuntimeException> cls) {
        sync(cls);
    }

    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                getReloadTimeUnit().sleep(getReloadInterval());
                updateLastExecution();
                update();
            }
        } catch (InterruptedException e) {
            LoggerHolder.getLog().warn(msg.format("interrupted.thread", getName()));
            Thread.currentThread().interrupt();

        } catch (Throwable t) {
            LoggerHolder.getLog().error(msg.format("error.reloading.all.items", getName()), t);
        }
    }

    private void load() {
        CountDownLatch latch = new CountDownLatch(data.getAll().size() + data.getAtomicReload().size());
        List<ConfigurationUpdater> toExecute = new ArrayList<ConfigurationUpdater>();

        Map<Method, Object> remote = new ConcurrentHashMap<Method, Object>();
        Map<Method, Object> local = new ConcurrentHashMap<Method, Object>();

        try {
            for (MethodConfiguration config : data.getAll()) {
                if (ReloadStrategy.INDEPENDENT == config.getReloadStrategy() || ReloadStrategy.NONE == config.getReloadStrategy()) {
                    toExecute.add(locator.configurationUpdaterFactory().standard(independentMethodValue, config, latch));
                } else {
                    toExecute.add(locator.configurationUpdaterFactory().remote(remote, config, latch));
                    toExecute.add(locator.configurationUpdaterFactory().local(local, config, latch));
                }
            }
            for (ConfigurationUpdater thread : toExecute) {
                thread.start();
            }
            waitFor(latch);

        } catch (Exception ignored) {
            LoggerHolder.getLog().error(msg.format("error.load", getName()), ignored);

        } finally {
            interruptAll(toExecute);
        }

        if (remote.size() < local.size()) {
            for (Entry<Method, Object> each : local.entrySet()) {
                atomicMethodValue.put(each.getKey(), each.getValue());
            }
        } else {
            for (Entry<Method, Object> each : remote.entrySet()) {
                atomicMethodValue.put(each.getKey(), each.getValue());
            }
        }
        validateLoadResult();
        notifyListeners(toExecute);
    }

    private void notifyListeners(List<ConfigurationUpdater> toExecute) {
        for (CallbackListener listener : listeners) {
            for (ConfigurationUpdater updater : toExecute) {
                Notification event = updater.getNotification();
                if (event == null) {
                    continue;
                }
                try {
                    listener.onChange(event);
                } catch (Throwable t) {
                    LoggerHolder.getLog().error(msg.format("error.notify", getName()), t);
                }
            }
        }
    }

    private void waitFor(CountDownLatch latch) {
        try {
            LoggerHolder.getLog().debug(msg.format("waiting.load", getName()));
            latch.await();
            LoggerHolder.getLog().info(msg.format("end.load", getName()));
        } catch (InterruptedException ignored) {
            LoggerHolder.getLog().error(msg.format("error.load", getName()), ignored);
        }
    }

    private void validateLoadResult() {
        if ((independentMethodValue.size() + atomicMethodValue.size()) != data.getAll().size()) {
            throw new ReConfInitializationError(msg.format("error.missing.item", getName()));
        }

        for (MethodConfiguration config : data.getAll()) {
            if (null == config.getMethod()) {
                throw new ReConfInitializationError(msg.format("error.internal", getName()));
            }
        }
        commitTemporaryDatabaseChanges();
    }

    private void scheduleIndependent() {
        for (MethodConfiguration config : data.getIndependentReload()) {
            ObservableThread thread = locator.configurationUpdaterFactory().independent(independentMethodValue, config, config.getReloadInterval(), config.getReloadTimeUnit(), listeners);
            thread.start();
            Environment.addThreadToCheck(thread);
            independentReload.add(thread);
        }
    }

    private void update() {
        if (!shouldReload()) {
            return;
        }

        List<ConfigurationUpdater> toExecute = new ArrayList<ConfigurationUpdater>(data.getAtomicReload().size());
        CountDownLatch latch = new CountDownLatch(data.getAtomicReload().size());
        Map<Method, Object> updated = new ConcurrentHashMap<Method, Object>();

        try {
            for (MethodConfiguration config : data.getAtomicReload()) {
                ConfigurationUpdater t = locator.configurationUpdaterFactory().remote(updated, config, latch);
                toExecute.add(t);
                t.start();
            }
            waitFor(latch);
            atomicMethodValue = mergeAtomicMethodObjectWith(updated);
            notifyListeners(toExecute);

        } catch (Exception ignored) {
            LoggerHolder.getLog().error(msg.format("error.load", getName()));

        } finally {
            interruptAll(toExecute);
        }
    }

    private void interruptAll(List<? extends Thread> arg) {
        for (Thread t : arg) {
            try {
                t.interrupt();
            } catch (Exception ignored) { }
        }
    }

    private Map<Method,Object> mergeAtomicMethodObjectWith(Map<Method, Object> updated) {
        if (!shouldMerge(updated)) {
            return atomicMethodValue;
        }

        Map<Method,Object> result = new ConcurrentHashMap<Method, Object>();
        for (Entry<Method, Object> each : atomicMethodValue.entrySet()) {
            result.put(each.getKey(), (!updated.containsKey(each.getKey()) ? each.getValue() : updated.get(each.getKey())));
        }
        commitTemporaryDatabaseChanges();
        return result;
    }

    private boolean shouldMerge(Map<Method, Object> updated) {
        List<String> notFound = new ArrayList<String>();
        for (Entry<Method, Object> each : atomicMethodValue.entrySet()) {
            if (updated.get(each.getKey()) == null) {
                notFound.add(msg.format("error.retrieving.item", getName(), each.getKey()));
            }
        }
        if (notFound.isEmpty()) {
            return true;
        }

        LoggerHolder.getLog().warn(StringUtils.join(notFound, LineSeparator.value()));
        LoggerHolder.getLog().warn(msg.format("error.retrieving.all.items", getName()));
        return false;
    }

    private void sync(Class<? extends RuntimeException> cls) {
        LoggerHolder.getLog().info(msg.format("sync.start", getName()));
        List<ConfigurationUpdater> toExecute = new ArrayList<ConfigurationUpdater>();
        CountDownLatch latch = new CountDownLatch(data.getAll().size());
        Map<Method, Object> updateAtomic = new ConcurrentHashMap<Method, Object>();
        Map<Method, Object> updateIndependent = new ConcurrentHashMap<Method, Object>();

        try {
            for (MethodConfiguration config : data.getAll()) {
                if (ReloadStrategy.INDEPENDENT == config.getReloadStrategy() || ReloadStrategy.NONE == config.getReloadStrategy()) {
                    toExecute.add(locator.configurationUpdaterFactory().remote(updateIndependent, config, latch));
                } else {
                    toExecute.add(locator.configurationUpdaterFactory().remote(updateAtomic, config, latch));
                }
            }
            for (Thread thread : toExecute) {
                thread.start();
            }
            waitFor(latch);

        } catch (Exception ignored) {
            LoggerHolder.getLog().error(msg.format("sync.error", getName()), ignored);

        } finally {
            interruptAll(toExecute);
        }

        if (updateAtomic.size() + updateIndependent.size() != data.getAll().size()) {
            String error = msg.format("sync.error", getName());
            try {
                Constructor<?> constructor = null;
                constructor = cls.getConstructor(String.class);
                constructor.setAccessible(true);
                throw cls.cast(constructor.newInstance(error));

            } catch (NoSuchMethodException ignored) {
                throw new UpdateConfigurationRepositoryException(error);
            } catch (InvocationTargetException ignored) {
                throw new UpdateConfigurationRepositoryException(error);
            } catch (InstantiationException ignored) {
                throw new UpdateConfigurationRepositoryException(error);
            } catch (IllegalAccessException ignored) {
                throw new UpdateConfigurationRepositoryException(error);
            }
        }
        finishSync(updateAtomic, updateIndependent);
        notifyListeners(toExecute);
        LoggerHolder.getLog().info(msg.format("sync.end", getName()));
    }

    private void finishSync(Map<Method, Object> updateAtomic, Map<Method, Object> updateIndependent) {
        Map<Method,Object> mergedAtomic = new ConcurrentHashMap<Method, Object>();
        for (Entry<Method, Object> each : atomicMethodValue.entrySet()) {
            mergedAtomic.put(each.getKey(), (!updateAtomic.containsKey(each.getKey()) ? each.getValue() : updateAtomic.get(each.getKey())));
        }

        Map<Method,Object> mergedIndependent = new ConcurrentHashMap<Method, Object>();
        for (Entry<Method, Object> each : independentMethodValue.entrySet()) {
            mergedIndependent.put(each.getKey(), (!updateIndependent.containsKey(each.getKey()) ? each.getValue() : updateIndependent.get(each.getKey())));
        }
        this.atomicMethodValue = mergedAtomic;
        this.independentMethodValue = mergedIndependent;

        commitTemporaryDatabaseChanges();
    }

    private void commitTemporaryDatabaseChanges() {
        locator.databaseManagerLocator().find().commitTemporaryUpdate(cfgRepository.getFullProperties(), cfgRepository.getInterfaceClass());
    }

    public int getReloadInterval() {
        if (!shouldReload()) {
            return 0;
        }
        return cfgRepository.getUpdateFrequency().getInterval();
    }

    public TimeUnit getReloadTimeUnit() {
        if (!shouldReload()) {
            return TimeUnit.DAYS;
        }
        return cfgRepository.getUpdateFrequency().getTimeUnit();
    }

    public Object getValueOf(Method m) {
        return atomicMethodValue.containsKey(m) ? atomicMethodValue.get(m) : independentMethodValue.containsKey(m) ? independentMethodValue.get(m) : null;
    }

    public boolean shouldReload() {
        return !data.getAtomicReload().isEmpty();
    }

    @Override
    public void stopIt() {
        interruptAll(independentReload);
        try {
            super.interrupt();
        } catch (Exception ignored) { }
    }

    @Override
    public Object clone() {
        return new ConfigurationRepositoryUpdater(cfgRepository, locator, factory);
    }

    @Override
    public List<ObservableThread> getChildren() {
        return independentReload;
    }
}
