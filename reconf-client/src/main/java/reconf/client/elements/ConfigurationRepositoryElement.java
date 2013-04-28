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
import java.util.concurrent.*;
import javax.validation.*;
import javax.validation.constraints.*;
import org.apache.commons.lang.builder.*;
import org.hibernate.validator.constraints.*;
import reconf.infra.throwables.*;


public class ConfigurationRepositoryElement {

    private String server;
    private Integer timeout;
    private TimeUnit timeUnit;
    private String product;
    private String component;
    private DoNotReloadPolicyElement doNotReloadPolicy;
    private ReloadPolicyElement configurationReloadPolicy;
    private Class<?> interfaceClass;
    private List<ConfigurationItemElement> configurationItems = new ArrayList<ConfigurationItemElement>();
    private static final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

    @NotNull @NotEmpty
    public String getServer() {
        return server;
    }
    public void setServer(String server) {
        this.server = server;
    }

    @NotNull @Min(1)
    public Integer getTimeout() {
        return timeout;
    }
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    @NotNull
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
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

    public DoNotReloadPolicyElement getDoNotReloadPolicy() {
        return doNotReloadPolicy;
    }

    public void setDoNotReloadPolicy(DoNotReloadPolicyElement doNotReloadPolicy) {
        this.doNotReloadPolicy = doNotReloadPolicy;
    }

    @Valid
    public ReloadPolicyElement getConfigurationReloadPolicy() {
        return configurationReloadPolicy;
    }
    public void setConfigurationReloadPolicy(ReloadPolicyElement configurationReloadPolicy) {
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

    public void validate() {
        Set<ConstraintViolation<ConfigurationRepositoryElement>> violations = validatorFactory.getValidator().validate(this);
        if (violations.size() > 0) {
            throw new ReConfInitializationError(violations.toString());
        }
    }

    @Override
    public String toString() {
        ToStringBuilder result = new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
        .append("class", getInterfaceClass())
        .append("product", getProduct())
        .append("component", getComponent())
        .append("server", getServer())
        .append("timeout", getTimeout())
        .append("timeUnit", getTimeUnit())
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
