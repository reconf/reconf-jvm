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
package reconf.client.proxy;



public class EqualsTest {

    public static void main(String[] args) throws Exception {
        EqualsTestInterface iface1 = ConfigurationRepositoryFactory.get(EqualsTestInterface.class);
        EqualsTestInterface iface2 = ConfigurationRepositoryFactory.get(EqualsTestInterface.class);

        Customization cust = new Customization();
        cust.setComponentPrefix("cp-");
        cust.setComponentSuffix("-cs");
        cust.setComponentItemPrefix("kp-");
        cust.setComponentItemSuffix("-ks");

        EqualsTestInterface customIface1 = ConfigurationRepositoryFactory.get(EqualsTestInterface.class, cust);
        EqualsTestInterface customIface2 = ConfigurationRepositoryFactory.get(EqualsTestInterface.class, cust);

        System.out.println(iface1 == iface2);
        System.out.println(iface1 == customIface1);
        System.out.println(iface2 == customIface1);
        System.out.println(customIface1 == customIface2);
        System.out.println(iface1 == customIface2);
        System.out.println(iface2 == customIface2);
    }
}
