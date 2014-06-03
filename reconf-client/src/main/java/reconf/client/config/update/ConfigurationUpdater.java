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
package reconf.client.config.update;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import org.apache.commons.lang.*;
import reconf.client.check.*;
import reconf.client.config.source.*;
import reconf.client.constructors.*;
import reconf.client.elements.*;
import reconf.client.factory.*;
import reconf.client.proxy.*;
import reconf.infra.i18n.*;
import reconf.infra.log.*;


public abstract class ConfigurationUpdater extends ObservableThread {

    protected final static MessagesBundle msg = MessagesBundle.getBundle(ConfigurationUpdater.class);
    protected final Map<Method, ConfigurationItemUpdateResult> methodValue;
    protected final MethodConfiguration methodCfg;
    protected final CountDownLatch latch;
    protected ConfigurationItemUpdateResult lastResult = null;
    protected boolean isSync = false;

    public ConfigurationUpdater(Map<Method, ConfigurationItemUpdateResult> toUpdate, MethodConfiguration target, boolean sync) {
        this(toUpdate, target, sync, new CountDownLatch(0));
    }

    public ConfigurationUpdater(Map<Method, ConfigurationItemUpdateResult> toUpdate, MethodConfiguration target, boolean sync, CountDownLatch latch) {
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

    protected abstract String getUpdaterType();

    public void run() {
        clearLastResult();
        update();
    }

    protected void clearLastResult() {
        this.lastResult = null;
    }

    public ConfigurationItemUpdateResult getLastResult() {
        return this.lastResult;
    }

    protected abstract void update();

    protected boolean updateMap(String value, boolean newValue, ConfigurationSource obtained, ConfigurationItemUpdateResult.Source source) throws Throwable {
        Class<?> clazz = methodCfg.getMethod().getReturnType();
        MethodData data = null;
        if (clazz.isArray()) {
            data = new MethodData(methodCfg.getMethod(), clazz.getComponentType(), value, obtained.getAdapter());

        } else if (Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)) {
            data = new MethodData(methodCfg.getMethod(), methodCfg.getMethod().getGenericReturnType(), value, obtained.getAdapter());

        } else {
            data = new MethodData(methodCfg.getMethod(), clazz, value, obtained.getAdapter());
        }

        ConfigurationItemElement elem = methodCfg.getConfigurationItemElement();
        ConfigurationItemUpdateResult.Builder builder = null;

        try {
            if (newValue || isSync) {
                builder = ConfigurationItemUpdateResult.Builder.update(ObjectConstructorFactory.get(clazz).construct(data));

            } else {
                builder = ConfigurationItemUpdateResult.Builder.noChange();
            }

            builder.valueRead(data.getValue())
            .product(elem.getProduct())
            .component(elem.getComponent())
            .item(elem.getValue())
            .qualifier(elem.getQualifier())
            .method(methodCfg.getMethod())
            .cast(methodCfg.getMethod().getReturnType())
            .from(source);

        } catch (Throwable t) {
            updateLastResultWithError(source, elem, value, t);
            return false;
        }

        lastResult = builder.build();
        methodValue.put(methodCfg.getMethod(), lastResult);
        return true;
    }

    protected void updateLastResultWithError(ConfigurationItemUpdateResult.Source source, ConfigurationItemElement elem, String value, Throwable t) {
        ConfigurationItemUpdateResult.Builder builder = ConfigurationItemUpdateResult.Builder.error(t);
        builder.valueRead(value)
        .product(elem.getProduct())
        .component(elem.getComponent())
        .item(elem.getValue())
        .qualifier(elem.getQualifier())
        .method(methodCfg.getMethod())
        .cast(methodCfg.getMethod().getReturnType())
        .from(source);

        lastResult = builder.build();
        methodValue.put(methodCfg.getMethod(), lastResult);
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
    public int getReloadRate() {
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
