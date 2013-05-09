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
package reconf.debug;

import reconf.client.examples.*;
import reconf.client.proxy.*;


public class ProxyFactoryTest {

    public static void main(String[] args) throws Exception {
        WelcomeConfiguration welcome = ConfigurationRepositoryFactory.create(WelcomeConfiguration.class);
        System.out.println(welcome.getText());
        System.out.println(welcome.getMap());
        System.out.println(welcome.getRawMap());

        System.out.println("going to sleep...");
        Thread.sleep(30000);

        welcome.updateIt();

        System.out.println(welcome.getText());
        System.out.println(welcome.getMap());
        System.out.println(welcome.getRawMap());

        System.exit(0);
    }
}
