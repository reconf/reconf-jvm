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
import reconf.client.config.source.*;
import reconf.client.constructors.*;
import reconf.client.proxy.*;
import reconf.infra.i18n.*;
import reconf.infra.log.*;


public class ConfigurationUpdater implements Runnable {

    protected final static MessagesBundle msg = MessagesBundle.getBundle(ConfigurationUpdater.class);
    protected final Map<Method, Object> methodValue;
    protected final MethodConfiguration methodCfg;
    protected final CountDownLatch latch;

    public ConfigurationUpdater(Map<Method, Object> toUpdate, MethodConfiguration target) {
        methodValue = toUpdate;
        methodCfg = target;
        latch = new CountDownLatch(0);
    }

    public ConfigurationUpdater(Map<Method, Object> toUpdate, MethodConfiguration target, CountDownLatch latch) {
        methodValue = toUpdate;
        methodCfg = target;
        this.latch = latch;
    }

    public void run() {
        update();
    }

    protected void update() {

        String value = null;
        ConfigurationSource obtained = null;

        try {
            LoggerHolder.getLog().debug(msg.format("method.reload", getClass().getName(), methodCfg.getMethod().getName()));
            ConfigurationSourceHolder holder = methodCfg.getConfigurationSourceHolder();
            value = holder.getRemote().get();
            if (null != value) {
                obtained = holder.getRemote();
                holder.getDb().update(value);

            } else {
                value = holder.getDb().get();
                if (value != null) {
                    obtained = holder.getDb();
                }
            }
            if (value != null && obtained != null) {
                updateMap(value, obtained);
            }

        } catch (Throwable t) {
            LoggerHolder.getLog().error(msg.format("error.load", getClass().getName()), t);

        } finally {
            releaseLatch();
        }
    }

    protected void updateMap(String value, ConfigurationSource obtained) throws Throwable {
        Class<?> clazz = methodCfg.getMethod().getReturnType();
        MethodData data = null;
        if (clazz.isArray()) {
            data = new MethodData(methodCfg.getMethod(), clazz.getComponentType(), value, obtained.getAdapter());

        } else if (Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)) {
            data = new MethodData(methodCfg.getMethod(), methodCfg.getMethod().getGenericReturnType(), value, obtained.getAdapter());

        } else {
            data = new MethodData(methodCfg.getMethod(), clazz, value, obtained.getAdapter());
        }

        Object result = ObjectConstructors.get(clazz).construct(data);
        methodValue.put(methodCfg.getMethod(), result);
    }

    protected void releaseLatch() {
        if (latch != null) {
            latch.countDown();
        }
    }
}
