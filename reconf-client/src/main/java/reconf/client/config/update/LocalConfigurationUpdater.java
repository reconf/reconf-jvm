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
import reconf.client.config.source.*;
import reconf.client.proxy.*;
import reconf.infra.log.*;


public class LocalConfigurationUpdater extends ConfigurationUpdater {

    public LocalConfigurationUpdater(Map<Method, ConfigurationItemUpdateResult> toUpdate, MethodConfiguration target, boolean sync) {
        super(toUpdate, target, sync);
    }

    public LocalConfigurationUpdater(Map<Method, ConfigurationItemUpdateResult> toUpdate, MethodConfiguration target, boolean sync, CountDownLatch latch) {
        super(toUpdate, target, sync, latch);
    }

    protected String getUpdaterType() {
        return "local";
    }

    protected void update() {

        String value = null;
        ConfigurationSource obtained = null;

        try {
            if (Thread.currentThread().isInterrupted()) {
                releaseLatch();
                logInterruptedThread();
                return;
            }

            LoggerHolder.getLog().debug(msg.format("method.reload", getName(), methodCfg.getMethod().getName()));
            ConfigurationSourceHolder holder = methodCfg.getConfigurationSourceHolder();
            value = holder.getDb().get();
            if (value != null) {
                obtained = holder.getDb();
            }

            if (value != null && obtained != null) {
                updateMap(value, false, obtained, ConfigurationItemUpdateResult.Source.localCache);
                LoggerHolder.getLog().debug(msg.format("method.done", getName(), methodCfg.getMethod().getName()));
            }

        } catch (Throwable t) {
            LoggerHolder.getLog().error(msg.format("error.load", getName()), t);

        } finally {
            releaseLatch();
        }
    }
}
