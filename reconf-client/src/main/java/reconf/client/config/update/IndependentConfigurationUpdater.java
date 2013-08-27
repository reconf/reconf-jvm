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
import reconf.client.proxy.*;

public class IndependentConfigurationUpdater extends ConfigurationUpdater {

    private final int reloadInterval;
    private final TimeUnit timeUnit;

    public IndependentConfigurationUpdater(Map<Method, Object> toUpdate, MethodConfiguration target, int reloadInterval, TimeUnit reloadTimeUnit) {
        super(toUpdate, target);
        this.timeUnit = reloadTimeUnit;
        this.reloadInterval = reloadInterval;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                timeUnit.sleep(reloadInterval);
                updateLastExecution();
                update();
            }
        } catch (InterruptedException e) {
            logInterruptedThread();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public int getReloadInterval() {
        return reloadInterval;
    }

    @Override
    public TimeUnit getReloadTimeUnit() {
        return timeUnit;
    }

    @Override
    public Object clone() {
        return new IndependentConfigurationUpdater(methodValue, methodCfg, reloadInterval, timeUnit);
    }
}
