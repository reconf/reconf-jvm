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
import javax.validation.*;
import javax.validation.constraints.*;
import org.apache.commons.lang.builder.*;
import org.hibernate.validator.constraints.*;
import reconf.client.setup.*;


public class ConfigurationRepositoryElement {

    private ConnectionSettings connectionSettings;
    private String product;
    private String component;
    private DoNotUpdatePolicyElement doNotReloadPolicy;
    private UpdatePolicyElement configurationReloadPolicy;
    private Class<?> interfaceClass;
    private List<ConfigurationItemElement> configurationItems = new ArrayList<ConfigurationItemElement>();

    @NotNull @Valid
    public ConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }
    public void setConnectionSettings(ConnectionSettings connectionSettings) {
        this.connectionSettings = connectionSettings;
    }

    @NotNull @NotEmpty
    public String getComponent() {
        return component;
    }
    public void setComponent(String component) {
        this.component = component;
    }

    @NotNull @NotEmpty
    public String getProduct() {
        return product;
    }
    public void setProduct(String product) {
        this.product = product;
    }

    public DoNotUpdatePolicyElement getDoNotReloadPolicy() {
        return doNotReloadPolicy;
    }

    public void setDoNotReloadPolicy(DoNotUpdatePolicyElement doNotReloadPolicy) {
        this.doNotReloadPolicy = doNotReloadPolicy;
    }

    @Valid
    public UpdatePolicyElement getConfigurationReloadPolicy() {
        return configurationReloadPolicy;
    }
    public void setConfigurationReloadPolicy(UpdatePolicyElement configurationReloadPolicy) {
        this.configurationReloadPolicy = configurationReloadPolicy;
    }

    @NotNull
    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }
    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    @Valid
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
        .append("do-not-reload-policy", null == doNotReloadPolicy ? "false" : "true");
        if (null == getConfigurationReloadPolicy()) {
            result.append("configuration-reload-policy", "n/a");
        } else {
            result.append("configuration-reload-policy", getConfigurationReloadPolicy());
        }
        result.append("configuration-items", getConfigurationItems());
        return result.toString();
    }
}
