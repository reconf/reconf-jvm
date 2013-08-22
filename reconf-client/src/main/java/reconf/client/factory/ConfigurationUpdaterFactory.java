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
import reconf.client.config.update.*;
import reconf.client.proxy.*;


public interface ConfigurationUpdaterFactory {

    ConfigurationUpdaterFactory defaultImplementation = new ConfigurationUpdaterFactoryImpl();

    ConfigurationUpdater independent(Map<Method, Object> toUpdate, MethodConfiguration target, int reloadInterval, TimeUnit reloadTimeUnit);
    ConfigurationUpdater standard(Map<Method, Object> toUpdate, MethodConfiguration target, CountDownLatch latch);

    ConfigurationUpdater remote(Map<Method, Object> toUpdate, MethodConfiguration target);
    ConfigurationUpdater remote(Map<Method, Object> toUpdate, MethodConfiguration target, CountDownLatch latch);

    ConfigurationUpdater local(Map<Method, Object> toUpdate, MethodConfiguration target);
    ConfigurationUpdater local(Map<Method, Object> toUpdate, MethodConfiguration target, CountDownLatch latch);
}
