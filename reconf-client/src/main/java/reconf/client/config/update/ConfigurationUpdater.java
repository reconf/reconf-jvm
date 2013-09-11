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
import java.util.concurrent.*;
import org.apache.commons.lang.*;
import reconf.client.config.source.*;
import reconf.client.constructors.*;
import reconf.client.experimental.*;
import reconf.client.factory.*;
import reconf.client.proxy.*;
import reconf.infra.i18n.*;
import reconf.infra.log.*;


public class ConfigurationUpdater extends ObservableThread {

    protected final static MessagesBundle msg = MessagesBundle.getBundle(ConfigurationUpdater.class);
    protected final Map<Method, Object> methodValue;
    protected final MethodConfiguration methodCfg;
    protected final CountDownLatch latch;
    protected Object lastResult = null;

    public ConfigurationUpdater(Map<Method, Object> toUpdate, MethodConfiguration target) {
        this(toUpdate, target, new CountDownLatch(0));
    }

    public ConfigurationUpdater(Map<Method, Object> toUpdate, MethodConfiguration target, CountDownLatch latch) {
        setDaemon(true);
        methodValue = toUpdate;
        methodCfg = target;
        this.latch = latch;
        setUpdaterName();
    }

    private void setUpdaterName() {
        setName(StringUtils.replace(methodCfg.getMethod().toString(), "public abstract ", "") + "_" + getUpdaterType() + "_updater" + StringUtils.replace(new Object().toString(), "java.lang.Object", ""));
    }

    protected String getUpdaterType() {
        return "standard";
    }

    public void run() {
        lastResult = null;
        update();
    }

    protected boolean update() {

        String value = null;
        ConfigurationSource obtained = null;
        boolean newValue = false;

        try {
            if (Thread.currentThread().isInterrupted()) {
                releaseLatch();
                logInterruptedThread();
                return false;
            }

            LoggerHolder.getLog().debug(msg.format("method.reload", getName(), methodCfg.getMethod().getName()));
            ConfigurationSourceHolder holder = methodCfg.getConfigurationSourceHolder();
            value = holder.getRemote().get();
            if (null != value) {
                obtained = holder.getRemote();
                newValue = holder.getDb().update(value);

            } else {
                value = holder.getDb().get();
                if (value != null) {
                    obtained = holder.getDb();
                }
            }
            if (value != null && obtained != null) {
                Object result = updateMap(value, obtained);
                if (newValue) {
                    lastResult = result;
                }
                LoggerHolder.getLog().debug(msg.format("method.done", getName(), methodCfg.getMethod().getName()));
            }


        } catch (Throwable t) {
            LoggerHolder.getLog().error(msg.format("error.load", getName()), t);

        } finally {
            releaseLatch();
        }
        return newValue;
    }

    protected Object updateMap(String value, ConfigurationSource obtained) throws Throwable {
        Class<?> clazz = methodCfg.getMethod().getReturnType();
        MethodData data = null;
        if (clazz.isArray()) {
            data = new MethodData(methodCfg.getMethod(), clazz.getComponentType(), value, obtained.getAdapter());

        } else if (Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)) {
            data = new MethodData(methodCfg.getMethod(), methodCfg.getMethod().getGenericReturnType(), value, obtained.getAdapter());

        } else {
            data = new MethodData(methodCfg.getMethod(), clazz, value, obtained.getAdapter());
        }

        Object result = ObjectConstructorFactory.get(clazz).construct(data);
        methodValue.put(methodCfg.getMethod(), result);
        return result;
    }

    protected void releaseLatch() {
        if (latch != null) {
            latch.countDown();
        }
    }

    protected void logInterruptedThread() {
        LoggerHolder.getLog().warn(msg.format("interrupted.thread", getName()));
    }

    @Override
    public int getReloadInterval() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TimeUnit getReloadTimeUnit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    @Override
    public List<ObservableThread> getChildren() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void stopIt() {
        try {
            Thread.currentThread().interrupt();
        } catch (Exception ignored) {
        }
    }
}
