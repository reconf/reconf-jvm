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


public class EqualsTest {

    public static void main(String[] args) throws Exception {
        WelcomeConfiguration welcome1 = ConfigurationRepositoryFactory.create(WelcomeConfiguration.class);
        WelcomeConfiguration welcome2 = ConfigurationRepositoryFactory.create(WelcomeConfiguration.class);

        Customization cust = new Customization();
        cust.setComponentPrefix("cp-");
        cust.setComponentSuffix("-cs");
        cust.setComponentItemPrefix("kp-");
        cust.setComponentItemSuffix("-ks");

        WelcomeConfiguration customWelcome1 = ConfigurationRepositoryFactory.create(WelcomeConfiguration.class, cust);
        WelcomeConfiguration customWelcome2 = ConfigurationRepositoryFactory.create(WelcomeConfiguration.class, cust);

        System.out.println(welcome1 == welcome2);
        System.out.println(welcome1 == customWelcome1);
        System.out.println(welcome2 == customWelcome1);
        System.out.println(customWelcome1 == customWelcome2);
        System.out.println(welcome1 == customWelcome2);
        System.out.println(welcome2 == customWelcome2);
        System.exit(0);
    }
}
