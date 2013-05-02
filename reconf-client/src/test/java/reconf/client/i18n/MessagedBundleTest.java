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
package reconf.client.i18n;

import java.util.*;

import org.junit.*;

import reconf.client.adapters.*;
import reconf.client.config.source.*;
import reconf.client.config.update.*;
import reconf.client.constructors.*;
import reconf.infra.i18n.*;

public class MessagedBundleTest {

    Class<?>[] adapters = new Class<?>[] {NoConfigurationAdapter.class, RawStringConfigurationAdapter.class};
    Class<?>[] configSource = new Class<?>[] {ConfigurationSourceHolder.class, DatabaseConfigurationSource.class, RemoteConfigurationSource.class};
    Class<?>[] configUpdate = new Class<?>[] {ConfigurationUpdater.class, LocalConfigurationUpdater.class, RemoteConfigurationUpdater.class};
    Class<?>[] constructors = new Class<?>[] {ArrayConstructor.class, CollectionConstructor.class, MapConstructor.class, MethodData.class, ObjectConstructors.class, SimpleConstructor.class, StringParser.class};

    Class<?>[][] allPackages = new Class<?>[][] { adapters,
                                                  configSource,
                                                  configUpdate,
                                                  constructors
                                                };

    @Test
    public void test() {

        for (String locale : Arrays.asList("lv_LV", "pt_BR")) {
            LocaleHolder.set(locale);
            for (Class<?>[] pkg : allPackages) {
                for (Class<?> cls : pkg) {
                    MessagesBundle bundle = MessagesBundle.getBundle(cls);
                    Assert.assertEquals(cls.getSimpleName(), bundle.get("probe"));
                }
            }
        }
    }

}
