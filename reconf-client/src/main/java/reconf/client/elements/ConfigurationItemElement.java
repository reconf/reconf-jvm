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

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import javax.validation.*;
import javax.validation.constraints.*;
import org.apache.commons.lang.*;
import org.apache.commons.lang.builder.*;
import org.hibernate.validator.constraints.*;
import reconf.client.adapters.*;
import reconf.client.annotations.*;
import reconf.infra.i18n.*;
import reconf.infra.log.*;


public class ConfigurationItemElement {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ConfigurationItemElement.class);
    private String methodName;
    private Method method;
    private String key;
    private String server;
    private Integer timeout;
    private TimeUnit timeUnit;
    private String component;
    private String product;
    private DoNotReloadPolicyElement doNotReloadPolicy;
    private ReloadPolicyElement configurationReloadPolicy;
    private Class<? extends ConfigurationAdapter> adapter;

    public static List<ConfigurationItemElement> from(ConfigurationRepositoryElement repository) {
        List<ConfigurationItemElement> result = new ArrayList<ConfigurationItemElement>();
        for (Method method : repository.getInterfaceClass().getMethods()) {

            ConfigurationItem ann = method.getAnnotation(ConfigurationItem.class);
            if (ann == null) {
                continue;
            }

            ConfigurationItemElement resultItem = null;

            for (ConfigurationItemElement item : repository.getConfigurationItems()) {
                if (StringUtils.equals(item.getMethodName(), method.getName())) {
                    resultItem = item;
                }
            }

            if (resultItem == null) {
                resultItem = new ConfigurationItemElement();
                resultItem.setMethod(method.getName());
                resultItem.setAdapter(ann.adapter());
            }
            resultItem.setMethod(method);
            defineRequestParameters(resultItem, ann);
            defineReloadStrategy(repository, resultItem, ann);
            defineItemProductComponenetOverride(resultItem, ann);
            result.add(resultItem);
        }
        return result;
    }

    private static void defineReloadStrategy(ConfigurationRepositoryElement repository, ConfigurationItemElement resultItem, ConfigurationItem annItem) {
        if (StringUtils.isBlank(resultItem.getKey())) {
            resultItem.setKey(annItem.name());
        }

        if (resultItem.getMethod().isAnnotationPresent(ConfigurationReloadPolicy.class)) {
            ConfigurationReloadPolicy reloadAnn = resultItem.getMethod().getAnnotation(ConfigurationReloadPolicy.class);
            ReloadPolicyElement reloadPolicy = new ReloadPolicyElement();
            reloadPolicy.setInterval(reloadAnn.interval());
            reloadPolicy.setTimeUnit(reloadAnn.timeUnit());
            resultItem.setConfigurationReloadPolicy(reloadPolicy);
        }

        if (resultItem.getMethod().isAnnotationPresent(DoNotReloadPolicy.class) && resultItem.getMethod().isAnnotationPresent(ConfigurationReloadPolicy.class)) {
            LoggerHolder.getLog().warn(msg.format("error.conflict.reload.policy" , resultItem.getMethod(), repository.getClass()));
            return;
        }

        if (resultItem.getMethod().isAnnotationPresent(DoNotReloadPolicy.class)) {
            resultItem.setDoNotReloadPolicy(new DoNotReloadPolicyElement());
        }
    }

    private static void defineRequestParameters(ConfigurationItemElement resultItem, ConfigurationItem annItem) {
        if (StringUtils.isNotBlank(annItem.server())) {
            resultItem.setServer(annItem.server());
        }
        if (annItem.timeout() > 0) {
            resultItem.setTimeout(annItem.timeout());
        }
        if (annItem.timeUnit() != null) {
            resultItem.setTimeUnit(annItem.timeUnit());
        }
    }

    private static void defineItemProductComponenetOverride(ConfigurationItemElement resultItem, ConfigurationItem annItem) {
        if (StringUtils.isBlank(resultItem.getProduct()) && StringUtils.isNotBlank(annItem.product())) {
            resultItem.setProduct(annItem.product());
        }

        if (StringUtils.isBlank(resultItem.getComponent()) && StringUtils.isNotBlank(annItem.component())) {
            resultItem.setComponent(annItem.component());
        }
    }

    @NotNull @NotEmpty
    public String getMethodName() {
        return methodName;
    }
    public void setMethod(String methodName) {
        this.methodName = methodName;
    }

    @NotNull @NotEmpty
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    @NotNull
    public Class<? extends ConfigurationAdapter> getAdapter() {
        return adapter;
    }
    public void setAdapter(Class<? extends ConfigurationAdapter> adapter) {
        this.adapter = adapter;
    }

    @NotNull
    public Method getMethod() {
        return method;
    }
    public void setMethod(Method method) {
        this.method = method;
    }

    public String getServer() {
        return server;
    }
    public void setServer(String server) {
        this.server = server;
    }

    public Integer getTimeout() {
        return timeout;
    }
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public String getComponent() {
        return component;
    }
    public void setComponent(String component) {
        this.component = component;
    }

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

    @Override
    public String toString() {
        ToStringBuilder result = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("method", getMethod());
        addToString(result, "product", getProduct());
        addToString(result, "component", getComponent());
        result.append("key", getKey());
        addToString(result, "server", getServer());

        if (null != getTimeout()) {
            result.append("timeout", getTimeout());
        }
        if (null != getTimeUnit()) {
            result.append("timeUnit", getTimeUnit());
        }
        result.append("do-not-reload-policy", null == doNotReloadPolicy ? "false" : "true");
        if (null == getConfigurationReloadPolicy()) {
            result.append("configuration-reload-policy", "n/a");
        } else {
            result.append("configuration-reload-policy", getConfigurationReloadPolicy());
        }
        return result.toString();
    }

    private void addToString(ToStringBuilder arg, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            arg.append(key, value);
        }
    }
}
