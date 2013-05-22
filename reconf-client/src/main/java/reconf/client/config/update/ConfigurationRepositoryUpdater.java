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
import reconf.client.elements.*;
import reconf.client.proxy.*;
import reconf.client.proxy.MethodConfiguration.ReloadStrategy;
import reconf.client.setup.*;
import reconf.infra.i18n.*;
import reconf.infra.log.*;
import reconf.infra.system.*;
import reconf.infra.throwables.*;


public class ConfigurationRepositoryUpdater implements Runnable {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ConfigurationRepositoryUpdater.class);
    protected final ConfigurationRepositoryElement cfgRepository;
    protected final ConfigurationRepositoryData data;
    protected Map<Method, Object> independentMethodValue = new ConcurrentHashMap<Method, Object>();
    protected Map<Method, Object> atomicMethodValue = new ConcurrentHashMap<Method, Object>();

    public ConfigurationRepositoryUpdater(ConfigurationRepositoryElement arg) {
        cfgRepository = arg;
        data = new ConfigurationRepositoryData(arg);
        load();
        scheduleIndependent();
    }

    public void syncNow(Class<? extends RuntimeException> cls) {
        sync(cls);
    }

    public void run() {
        try {
            update();

        } catch (Throwable t) {
            LoggerHolder.getLog().error(msg.format("error.reloading.all.items", cfgRepository.getInterfaceClass()), t);
        }
    }

    private void load() {
        CountDownLatch latch = new CountDownLatch(data.getAll().size() + data.getAtomicReload().size());
        ExecutorService service = Executors.newFixedThreadPool(data.getAll().size() + data.getAtomicReload().size());

        Map<Method, Object> remote = new ConcurrentHashMap<Method, Object>();
        Map<Method, Object> local = new ConcurrentHashMap<Method, Object>();

        try {
            for (MethodConfiguration config : data.getAll()) {
                if (ReloadStrategy.INDEPENDENT == config.getReloadStrategy() || ReloadStrategy.NONE == config.getReloadStrategy()) {
                    service.execute(new ConfigurationUpdater(independentMethodValue, config, latch));
                } else {
                    service.execute(new RemoteConfigurationUpdater(remote, config, latch));
                    service.execute(new LocalConfigurationUpdater(local, config, latch));
                }
            }
            waitFor(latch);

        } catch (Exception ignored) {
            LoggerHolder.getLog().error(msg.format("error.load", cfgRepository.getInterfaceClass()), ignored);

        } finally {
            service.shutdown();
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
    }

    private void waitFor(CountDownLatch latch) {
        try {
            LoggerHolder.getLog().debug(msg.format("waiting.load", cfgRepository.getInterfaceClass()));
            latch.await();
            LoggerHolder.getLog().info(msg.format("end.load", cfgRepository.getInterfaceClass()));
        } catch (InterruptedException ignored) {
            LoggerHolder.getLog().error(msg.format("error.load", cfgRepository.getInterfaceClass()), ignored);
        }
    }

    private void validateLoadResult() {
        if ((independentMethodValue.size() + atomicMethodValue.size()) != data.getAll().size()) {
            throw new ReConfInitializationError(msg.format("error.missing.item", cfgRepository.getInterfaceClass()));
        }

        for (MethodConfiguration config : data.getAll()) {
            if (null == config.getMethod()) {
                throw new ReConfInitializationError(msg.get("error.internal"));
            }
        }
        commitTemporaryDatabaseChanges();
    }

    private void scheduleIndependent() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(data.getIndependentReload().size());
        for (MethodConfiguration config : data.getIndependentReload()) {
            service.scheduleAtFixedRate(new ConfigurationUpdater(independentMethodValue, config), config.getReloadInterval(), config.getReloadInterval(), config.getReloadTimeUnit());
        }
    }

    private void update() {
        if (!shouldReload()) {
            return;
        }

        ExecutorService service = Executors.newFixedThreadPool(data.getAtomicReload().size());
        CountDownLatch latch = new CountDownLatch(data.getAtomicReload().size());
        Map<Method, Object> updated = new ConcurrentHashMap<Method, Object>();

        try {
            for (MethodConfiguration config : data.getAtomicReload()) {
                service.execute(new RemoteConfigurationUpdater(updated, config, latch));
            }
            waitFor(latch);
            atomicMethodValue = mergeAtomicMethodObjectWith(updated);

        } catch (Exception ignored) {
            LoggerHolder.getLog().error(msg.format("error.load", cfgRepository.getInterfaceClass()));

        } finally {
            service.shutdown();
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
                notFound.add(msg.format("error.retrieving.item", each.getKey()));
            }
        }
        if (notFound.isEmpty()) {
            return true;
        }

        LoggerHolder.getLog().warn(StringUtils.join(notFound, LineSeparator.value()));
        LoggerHolder.getLog().warn(msg.get("error.retrieving.all.items"));
        return false;
    }

    private void sync(Class<? extends RuntimeException> cls) {
        ExecutorService service = Executors.newFixedThreadPool(data.getAll().size());
        CountDownLatch latch = new CountDownLatch(data.getAll().size());
        Map<Method, Object> updateAtomic = new ConcurrentHashMap<Method, Object>();
        Map<Method, Object> updateIndependent = new ConcurrentHashMap<Method, Object>();

        try {
            for (MethodConfiguration config : data.getAll()) {
                if (ReloadStrategy.INDEPENDENT == config.getReloadStrategy() || ReloadStrategy.NONE == config.getReloadStrategy()) {
                    service.submit(new RemoteConfigurationUpdater(updateIndependent, config, latch));
                } else {
                    service.submit(new RemoteConfigurationUpdater(updateAtomic, config, latch));
                }
            }
            waitFor(latch);

        } catch (Exception ignored) {
            LoggerHolder.getLog().error(msg.format("error.load", cfgRepository.getInterfaceClass()));

        } finally {
            service.shutdown();
        }

        if (updateAtomic.size() + updateIndependent.size() != data.getAll().size()) {
            String error = msg.format("error.load", cfgRepository.getInterfaceClass());
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
        Environment.getManager().commitTemporaryUpdate(cfgRepository.getProduct(), cfgRepository.getComponent(), cfgRepository.getInterfaceClass());
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
}
