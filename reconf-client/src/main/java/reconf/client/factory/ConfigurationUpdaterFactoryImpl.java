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
package reconf.client.factory;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import reconf.client.callback.*;
import reconf.client.config.update.*;
import reconf.client.proxy.*;


public class ConfigurationUpdaterFactoryImpl implements ConfigurationUpdaterFactory {

    @Override
    public ConfigurationUpdater independent(Map<Method, UpdateResult> toUpdate, MethodConfiguration target, int reloadInterval, TimeUnit reloadTimeUnit, Collection<UpdateListener> listeners) {
        return new IndependentConfigurationUpdater(toUpdate, target, false, reloadInterval, reloadTimeUnit, listeners);
    }

    @Override
    public ConfigurationUpdater syncIndependent(Map<Method, UpdateResult> toUpdate, MethodConfiguration target, int reloadInterval, TimeUnit reloadTimeUnit, Collection<UpdateListener> listeners) {
        return new IndependentConfigurationUpdater(toUpdate, target, true, reloadInterval, reloadTimeUnit, listeners);
    }

    @Override
    public ConfigurationUpdater standard(Map<Method, UpdateResult> toUpdate, MethodConfiguration target, CountDownLatch latch) {
        return new ConfigurationUpdater(toUpdate, target, false, latch);
    }

    @Override
    public ConfigurationUpdater syncStandard(Map<Method, UpdateResult> toUpdate, MethodConfiguration target, CountDownLatch latch) {
        return new ConfigurationUpdater(toUpdate, target, true, latch);
    }


    @Override
    public ConfigurationUpdater remote(Map<Method, UpdateResult> toUpdate, MethodConfiguration target) {
        return new RemoteConfigurationUpdater(toUpdate, target, false);
    }

    @Override
    public ConfigurationUpdater syncRemote(Map<Method, UpdateResult> toUpdate, MethodConfiguration target) {
        return new RemoteConfigurationUpdater(toUpdate, target, true);
    }

    @Override
    public ConfigurationUpdater remote(Map<Method, UpdateResult> toUpdate, MethodConfiguration target, CountDownLatch latch) {
        return new RemoteConfigurationUpdater(toUpdate, target, false, latch);
    }

    @Override
    public ConfigurationUpdater syncRemote(Map<Method, UpdateResult> toUpdate, MethodConfiguration target, CountDownLatch latch) {
        return new RemoteConfigurationUpdater(toUpdate, target, true, latch);
    }

    @Override
    public ConfigurationUpdater local(Map<Method, UpdateResult> toUpdate, MethodConfiguration target) {
        return new LocalConfigurationUpdater(toUpdate, target, false);
    }

    @Override
    public ConfigurationUpdater syncLocal(Map<Method, UpdateResult> toUpdate, MethodConfiguration target) {
        return new LocalConfigurationUpdater(toUpdate, target, true);
    }

    @Override
    public ConfigurationUpdater local(Map<Method, UpdateResult> toUpdate, MethodConfiguration target, CountDownLatch latch) {
        return new LocalConfigurationUpdater(toUpdate, target, false, latch);
    }

    @Override
    public ConfigurationUpdater syncLocal(Map<Method, UpdateResult> toUpdate, MethodConfiguration target, CountDownLatch latch) {
        return new LocalConfigurationUpdater(toUpdate, target, true, latch);
    }
}
