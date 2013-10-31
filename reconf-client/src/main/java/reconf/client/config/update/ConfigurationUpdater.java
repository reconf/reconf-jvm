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
import reconf.client.callback.*;
import reconf.client.check.*;
import reconf.client.config.source.*;
import reconf.client.constructors.*;
import reconf.client.elements.*;
import reconf.client.factory.*;
import reconf.client.proxy.*;
import reconf.infra.i18n.*;
import reconf.infra.log.*;


public class ConfigurationUpdater extends ObservableThread {

    protected final static MessagesBundle msg = MessagesBundle.getBundle(ConfigurationUpdater.class);
    protected final Map<Method, UpdateResult> methodValue;
    protected final MethodConfiguration methodCfg;
    protected final CountDownLatch latch;
    protected UpdateResult lastResult = null;
    protected boolean isSync = false;

    public ConfigurationUpdater(Map<Method, UpdateResult> toUpdate, MethodConfiguration target, boolean sync) {
        this(toUpdate, target, sync, new CountDownLatch(0));
    }

    public ConfigurationUpdater(Map<Method, UpdateResult> toUpdate, MethodConfiguration target, boolean sync, CountDownLatch latch) {
        setDaemon(true);
        methodValue = toUpdate;
        methodCfg = target;
        this.latch = latch;
        this.isSync = sync;
        setUpdaterName();
    }

    private void setUpdaterName() {
        setName(StringUtils.replace(methodCfg.getMethod().toString(), "public abstract ", "") + "_" + getUpdaterType() + "_updater" + StringUtils.replace(new Object().toString(), "java.lang.Object", ""));
    }

    protected String getUpdaterType() {
        return "standard";
    }

    public void run() {
        clearLastResult();
        update();
    }

    protected Notification createNotification() {
        if (lastResult == null || !lastResult.isChange() || !lastResult.isSuccess()) {
            return null;
        }
        return new Notification(lastResult.getProduct(), lastResult.getComponent(), lastResult.getItem(), lastResult.getMethod(), lastResult.getObject(), lastResult.getCast());
    }

    protected void clearLastResult() {
        this.lastResult = null;
    }

    protected void update() {

        String value = null;
        ConfigurationSource obtained = null;
        boolean newValue = false;
        boolean success = false;

        try {
            if (Thread.currentThread().isInterrupted()) {
                releaseLatch();
                logInterruptedThread();
                return;
            }

            LoggerHolder.getLog().debug(msg.format("method.reload", getName(), methodCfg.getMethod().getName()));
            ConfigurationSourceHolder holder = methodCfg.getConfigurationSourceHolder();
            value = holder.getRemote().get();
            if (null != value) {
                obtained = holder.getRemote();
                newValue = holder.getDb().update(value);
                success = true;

            } else {
                value = holder.getDb().get();
                if (value != null) {
                    obtained = holder.getDb();
                }
            }
            if (value != null && obtained != null) {
                lastResult = updateMap(value, newValue, success, obtained);
                LoggerHolder.getLog().debug(msg.format("method.done", getName(), methodCfg.getMethod().getName()));
            }


        } catch (Throwable t) {
            LoggerHolder.getLog().error(msg.format("error.load", getName()), t);

        } finally {
            releaseLatch();
        }
    }

    protected UpdateResult updateMap(String value, boolean newValue, boolean success, ConfigurationSource obtained) throws Throwable {
        Class<?> clazz = methodCfg.getMethod().getReturnType();
        MethodData data = null;
        if (clazz.isArray()) {
            data = new MethodData(methodCfg.getMethod(), clazz.getComponentType(), value, obtained.getAdapter());

        } else if (Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)) {
            data = new MethodData(methodCfg.getMethod(), methodCfg.getMethod().getGenericReturnType(), value, obtained.getAdapter());

        } else {
            data = new MethodData(methodCfg.getMethod(), clazz, value, obtained.getAdapter());
        }

        Object result;
        if (newValue || isSync) {
            result = ObjectConstructorFactory.get(clazz).construct(data);
        } else {
            result = null;
        }

        ConfigurationItemElement elem = methodCfg.getConfigurationItemElement();
        UpdateResult updateResult = new UpdateResult(result, methodCfg.getMethod().getReturnType(), success, newValue, elem.getProduct(), elem.getComponent(), elem.getValue(), methodCfg.getMethod());
        methodValue.put(methodCfg.getMethod(), updateResult);
        return updateResult;
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

    public Notification getNotification() {
        return createNotification();
    }
}
