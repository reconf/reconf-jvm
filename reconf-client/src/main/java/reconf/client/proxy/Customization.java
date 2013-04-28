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

import org.apache.commons.lang.*;
import org.apache.commons.lang.builder.*;


public class Customization {

    public static final Customization EMPTY = new Customization();

    private String componentPrefix;
    private String componentSuffix;
    private String keyPrefix;
    private String keySuffix;

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

    public String getKeyPrefix() {
        return keyPrefix;
    }
    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getKeySuffix() {
        return keySuffix;
    }
    public void setKeySuffix(String keySuffix) {
        this.keySuffix = keySuffix;
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
        return StringUtils.isNotBlank(componentPrefix) ||
            StringUtils.isNotBlank(componentSuffix) ||
            StringUtils.isNotBlank(keyPrefix) ||
            StringUtils.isNotBlank(keySuffix);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("componentPrefix [").append(StringUtils.defaultString(componentPrefix)).append("] ")
            .append("componentSuffix [").append(StringUtils.defaultString(componentSuffix)).append("] ")
            .append("keyPrefix [").append(StringUtils.defaultString(keyPrefix)).append("] ")
            .append("keySuffix [").append(StringUtils.defaultString(keySuffix)).append("]")
            .toString();
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

    public String getCustomKey(String originalKey) {
        if (StringUtils.isBlank(originalKey) || (StringUtils.isBlank(getKeyPrefix()) && StringUtils.isBlank(getKeySuffix()))) {
            return originalKey;
        }

        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(getKeyPrefix())) {
            builder.append(getKeyPrefix());
        }
        builder.append(originalKey);
        if (StringUtils.isNotBlank(getKeySuffix())) {
            builder.append(getKeySuffix());
        }
        return builder.toString();
    }
}
