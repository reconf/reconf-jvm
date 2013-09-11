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
import reconf.client.proxy.*;
import reconf.infra.log.*;


public class RemoteConfigurationUpdater extends ConfigurationUpdater {

    public RemoteConfigurationUpdater(Map<Method, Object> toUpdate, MethodConfiguration target) {
        super(toUpdate, target);
    }

    public RemoteConfigurationUpdater(Map<Method, Object> toUpdate, MethodConfiguration target, CountDownLatch latch) {
        super(toUpdate, target, latch);
    }

    protected String getUpdaterType() {
        return "remote";
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
                newValue = holder.getDb().temporaryUpdate(value);
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
}
