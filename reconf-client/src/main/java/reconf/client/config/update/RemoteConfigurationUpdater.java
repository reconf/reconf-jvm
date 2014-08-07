/*
 *    Copyright 2013-2014 ReConf Team
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
import reconf.client.config.update.ConfigurationItemUpdateResult.Source;
import reconf.client.proxy.*;
import reconf.infra.log.*;


public class RemoteConfigurationUpdater extends ConfigurationUpdater {

    public RemoteConfigurationUpdater(Map<Method, ConfigurationItemUpdateResult> toUpdate, MethodConfiguration target, boolean sync) {
        super(toUpdate, target, sync);
    }

    public RemoteConfigurationUpdater(Map<Method, ConfigurationItemUpdateResult> toUpdate, MethodConfiguration target, boolean sync, CountDownLatch latch) {
        super(toUpdate, target, sync, latch);
    }

    protected String getUpdaterType() {
        return "server";
    }

    protected void update() {

        String value = null;
        ConfigurationSource obtained = null;
        boolean newValue = false;
        ConfigurationSourceHolder holder = null;

        try {
            if (Thread.currentThread().isInterrupted()) {
                releaseLatch();
                logInterruptedThread();
            }

            LoggerHolder.getLog().debug(msg.format("method.reload", getName(), methodCfg.getMethod().getName()));
            holder = methodCfg.getConfigurationSourceHolder();
            value = holder.getRemote().get();

        } catch (Throwable t) {
            LoggerHolder.getLog().error(msg.format("error.load", getName()), t);
            updateLastResultWithError(Source.server, methodCfg.getConfigurationItemElement(), null, t);
            releaseLatch();
            return;
        }

        try {
            if (null != value) {
                obtained = holder.getRemote();
                newValue = holder.getDb().isNew(value);
            }

            if (value != null && obtained != null) {
                boolean ok = updateMap(value, newValue, obtained, ConfigurationItemUpdateResult.Source.server);
                if (ok) {
                    holder.getDb().temporaryUpdate(value);
                    LoggerHolder.getLog().debug(msg.format("method.done", getName(), methodCfg.getMethod().getName()));
                } else {
                    LoggerHolder.getLog().error(msg.format("error.load", getName()), lastResult.getError());
                }
            }

        } catch (Throwable t) {
            LoggerHolder.getLog().error(msg.format("error.load", getName()), t);

        } finally {
            releaseLatch();
        }
    }
}
