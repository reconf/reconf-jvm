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

import java.util.*;
import org.apache.commons.lang.*;
import org.apache.commons.lang.builder.*;
import reconf.client.callback.*;


public class Customization {

    public static final Customization EMPTY = new Customization();

    private String productPrefix;
    private String productSuffix;
    private String componentPrefix;
    private String componentSuffix;
    private String namePrefix;
    private String nameSuffix;
    private List<CallbackListener> listeners = new ArrayList<CallbackListener>();

    public String getProductPrefix() {
        return productPrefix;
    }
    public void setProductPrefix(String productPrefix) {
        this.productPrefix = productPrefix;
    }

    public String getProductSuffix() {
        return productSuffix;
    }
    public void setProductSuffix(String productSuffix) {
        this.productSuffix = productSuffix;
    }

    public String getComponentPrefix() {
        return componentPrefix;
    }
    public void setComponentPrefix(String componentPrefix) {
        this.componentPrefix = componentPrefix;
    }

    public String getComponentSuffix() {
        return componentSuffix;
    }
    public void setComponentSuffix(String componentSuffix) {
        this.componentSuffix = componentSuffix;
    }

    public String getComponentItemPrefix() {
        return namePrefix;
    }
    public void setComponentItemPrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public String getComponentItemSuffix() {
        return nameSuffix;
    }
    public void setComponentItemSuffix(String nameSuffix) {
        this.nameSuffix = nameSuffix;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(productPrefix) ||
            StringUtils.isNotBlank(productSuffix) ||
            StringUtils.isNotBlank(componentPrefix) ||
            StringUtils.isNotBlank(componentSuffix) ||
            StringUtils.isNotBlank(namePrefix) ||
            StringUtils.isNotBlank(nameSuffix);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("productPrefix[").append(StringUtils.defaultString(productPrefix)).append("] ")
            .append("productSuffix[").append(StringUtils.defaultString(productSuffix)).append("] ")
            .append("componentPrefix [").append(StringUtils.defaultString(componentPrefix)).append("] ")
            .append("componentSuffix [").append(StringUtils.defaultString(componentSuffix)).append("] ")
            .append("keyPrefix [").append(StringUtils.defaultString(namePrefix)).append("] ")
            .append("keySuffix [").append(StringUtils.defaultString(nameSuffix)).append("]")
            .toString();
    }

    public String getCustomProduct(String originalProduct) {
        if (StringUtils.isBlank(originalProduct) || (StringUtils.isBlank(getProductPrefix()) && StringUtils.isBlank(getProductSuffix()))) {
            return originalProduct;
        }

        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(getProductPrefix())) {
            builder.append(getProductPrefix());
        }
        builder.append(originalProduct);
        if (StringUtils.isNotBlank(getProductSuffix())) {
            builder.append(getProductSuffix());
        }
        return builder.toString();

    }

    public String getCustomComponent(String originalComponent) {
        if (StringUtils.isBlank(originalComponent) || (StringUtils.isBlank(getComponentPrefix()) && StringUtils.isBlank(getComponentSuffix()))) {
            return originalComponent;
        }

        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(getComponentPrefix())) {
            builder.append(getComponentPrefix());
        }
        builder.append(originalComponent);
        if (StringUtils.isNotBlank(getComponentSuffix())) {
            builder.append(getComponentSuffix());
        }
        return builder.toString();

    }

    public String getCustomItem(String originalKey) {
        if (StringUtils.isBlank(originalKey) || (StringUtils.isBlank(getComponentItemPrefix()) && StringUtils.isBlank(getComponentItemSuffix()))) {
            return originalKey;
        }

        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(getComponentItemPrefix())) {
            builder.append(getComponentItemPrefix());
        }
        builder.append(originalKey);
        if (StringUtils.isNotBlank(getComponentItemSuffix())) {
            builder.append(getComponentItemSuffix());
        }
        return builder.toString();
    }

    public List<CallbackListener> getListeners() {
        return listeners;
    }

    public void setListeners(List<CallbackListener> listeners) {
        if (listeners != null) {
            this.listeners = listeners;
        }
    }

    public void addListener(CallbackListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }
}
