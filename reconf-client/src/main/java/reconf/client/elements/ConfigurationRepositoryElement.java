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
package reconf.client.elements;

import java.util.*;
import org.apache.commons.lang.*;
import org.apache.commons.lang.builder.*;
import reconf.client.setup.*;
import reconf.infra.system.*;


public class ConfigurationRepositoryElement {

    private ConnectionSettings connectionSettings;
    private String product;
    private String component;
    private DoNotUpdateElement doNotUpdate;
    private UpdateFrequencyElement updateFrequency;
    private Class<?> interfaceClass;
    private List<ConfigurationItemElement> configurationItems = new ArrayList<ConfigurationItemElement>();

    public ConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }
    public void setConnectionSettings(ConnectionSettings connectionSettings) {
        this.connectionSettings = connectionSettings;
    }

    public String getComponent() {
        return component;
    }
    public void setComponent(String component) {
        this.component = component;
    }

    public Collection<String> getFullProperties() {
        Set<String> result = new LinkedHashSet<String>();
        for (ConfigurationItemElement elem : configurationItems) {
            String productName = null;
            if (StringUtils.isEmpty(elem.getProduct())) {
                productName = getProduct();
            } else {
                productName = elem.getProduct();
            }

            String componentName = null;
            if (StringUtils.isEmpty(elem.getComponent())) {
                componentName = getComponent();
            } else {
                componentName = elem.getComponent();
            }
            result.add(FullPropertyElement.from(productName, componentName, elem.getValue()));
        }
        return result;
    }

    public String getProduct() {
        return product;
    }
    public void setProduct(String product) {
        this.product = product;
    }

    public DoNotUpdateElement getDoNotUpdate() {
        return doNotUpdate;
    }

    public void setDoNotUpdate(DoNotUpdateElement doNotUpdate) {
        this.doNotUpdate = doNotUpdate;
    }

    public UpdateFrequencyElement getUpdateFrequency() {
        return updateFrequency;
    }
    public void setUpdateFrequency(UpdateFrequencyElement updateFrequency) {
        this.updateFrequency = updateFrequency;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }
    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public List<ConfigurationItemElement> getConfigurationItems() {
        return configurationItems;
    }
    public void setConfigurationItems(List<ConfigurationItemElement> configurationItems) {
        this.configurationItems = configurationItems;
    }

    @Override
    public String toString() {
        ToStringBuilder result = new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
        .append("class", getInterfaceClass())
        .append("product", getProduct())
        .append("component", getComponent())
        .append("@DoNotUpdate", null == doNotUpdate ? "not found" : "found");
        if (getUpdateFrequency() == null) {
            result.append("@UpdateFrequency", "not found");
        } else {
            result.append("@UpdateFrequency", getUpdateFrequency());
        }
        result.append("@ConfigurationItems", LineSeparator.value() + getConfigurationItems());
        return result.toString();
    }
}
