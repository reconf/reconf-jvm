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
    private String component;
    private String product;
    private DoNotUpdateElement doNotUpdate;
    private UpdateFrequencyElement updateFrequency;
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
            defineUpdateStrategy(repository, resultItem, ann);
            defineItemProductComponenetOverride(resultItem, ann);
            result.add(resultItem);
        }
        return result;
    }

    private static void defineUpdateStrategy(ConfigurationRepositoryElement repository, ConfigurationItemElement resultItem, ConfigurationItem annItem) {
        if (StringUtils.isBlank(resultItem.getKey())) {
            resultItem.setKey(annItem.value());
        }

        if (resultItem.getMethod().isAnnotationPresent(UpdateFrequency.class)) {
            UpdateFrequency frequencyAnn = resultItem.getMethod().getAnnotation(UpdateFrequency.class);
            UpdateFrequencyElement frequencyElement = new UpdateFrequencyElement();
            frequencyElement.setInterval(frequencyAnn.interval());
            frequencyElement.setTimeUnit(frequencyAnn.timeUnit());
            resultItem.setUpdateFrequency(frequencyElement);
        }

        if (resultItem.getMethod().isAnnotationPresent(DoNotUpdate.class) && resultItem.getMethod().isAnnotationPresent(UpdateFrequency.class)) {
            LoggerHolder.getLog().warn(msg.format("error.conflict.reload.policy" , resultItem.getMethod(), repository.getClass()));
            return;
        }

        if (resultItem.getMethod().isAnnotationPresent(DoNotUpdate.class)) {
            resultItem.setDoNotUpdate(new DoNotUpdateElement());
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

    @NotNull(message="method.name.null")
    @NotEmpty(message="method.name.empty")
    public String getMethodName() {
        return methodName;
    }
    public void setMethod(String methodName) {
        this.methodName = methodName;
    }

    @NotNull(message="key.null")
    @NotEmpty(message="key.empty")
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    @NotNull(message="adapter.null")
    public Class<? extends ConfigurationAdapter> getAdapter() {
        return adapter;
    }
    public void setAdapter(Class<? extends ConfigurationAdapter> adapter) {
        this.adapter = adapter;
    }

    @NotNull(message="method.null")
    public Method getMethod() {
        return method;
    }
    public void setMethod(Method method) {
        this.method = method;
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

    public DoNotUpdateElement getDoNotUpdate() {
        return doNotUpdate;
    }
    public void setDoNotUpdate(DoNotUpdateElement doNotUpdate) {
        this.doNotUpdate = doNotUpdate;
    }

    @Valid
    public UpdateFrequencyElement getUpdateFrequency() {
        return updateFrequency;
    }
    public void setUpdateFrequency(UpdateFrequencyElement updateFrequency) {
        this.updateFrequency = updateFrequency;
    }

    @Override
    public String toString() {
        ToStringBuilder result = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("method", getMethod());
        addToString(result, "product", getProduct());
        addToString(result, "component", getComponent());
        result.append("name", getKey());
        result.append("@DoNotUpdate", null == doNotUpdate ? "not found" : "found");
        if (getUpdateFrequency() == null) {
            result.append("specific @UpdateFrequency", "not found");
        } else {
            result.append("specific @UpdateFrequency", getUpdateFrequency());
        }
        return result.toString();
    }

    private void addToString(ToStringBuilder arg, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            arg.append(key, value);
        }
    }
}
