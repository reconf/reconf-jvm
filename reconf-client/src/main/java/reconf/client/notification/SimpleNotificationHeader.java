/*
 *    Copyright 2013-2015 ReConf Team
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
package reconf.client.notification;

import reconf.client.config.update.ConfigurationItemUpdateResult;

public class SimpleNotificationHeader implements NotificationHeader {

    private final String product;
    private final String component;
    private final String item;
    private final String qualifier;

    public SimpleNotificationHeader(ConfigurationItemUpdateResult result) {
        this.product = result.getProduct();
        this.component = result.getComponent();
        this.item = result.getItem();
        this.qualifier = result.getQualifier();
    }

    @Override
    public String getProduct() {
        return product;
    }

    @Override
    public String getComponent() {
        return component;
    }

    @Override
    public String getItem() {
        return item;
    }

    @Override
    public String getQualifier() {
        return qualifier;
    }

}
