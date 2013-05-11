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
package reconf.client.examples;

import java.util.*;
import java.util.concurrent.*;
import reconf.client.adapters.*;
import reconf.client.annotations.*;


@ConfigurationRepository(component="test", product="test")
@UpdateFrequency(interval=10, timeUnit=TimeUnit.SECONDS)
public interface WelcomeConfiguration {

    @ConfigurationItem("texto.de.boas.vindas")
    String getText();

    @ConfigurationItem("hugemap.param")
    Map<Long, String> getMap();

    @ConfigurationItem(value="", adapter=RawStringConfigurationAdapter.class)
    @UpdateFrequency(interval=100, timeUnit=TimeUnit.MINUTES)
    @DoNotUpdate
    String getRawMap();

    @UpdateConfigurationRepository
    void updateIt();
}
