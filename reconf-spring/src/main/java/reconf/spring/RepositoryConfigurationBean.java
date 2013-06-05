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
package reconf.spring;

import java.util.concurrent.*;
import org.springframework.beans.factory.*;
import reconf.client.proxy.*;


public class RepositoryConfigurationBean implements FactoryBean<Object> {

    private Class<?> configInterface;
    private Customization customization = Customization.EMPTY;
    private static ConcurrentMap<String, Object> cache = new ConcurrentHashMap<String, Object>();

    @Override
    public Object getObject() throws Exception {
        String key = configInterface.getName() + " - " + customization;
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        Object result = ConfigurationRepositoryFactory.create(getConfigInterface(), getCustomization());
        cache.putIfAbsent(key, result);
        return result;
    }

    @Override
    public Class<?> getObjectType() {
        return configInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    protected Class<?> getConfigInterface() {
        return configInterface;
    }

    protected Customization getCustomization() {
        return customization;
    }

    public void setConfigInterface(Class<?> configInterface) {
        this.configInterface = configInterface;
    }

    public void setComponentPrefix(String applicationPrefix) {
        this.customization.setComponentPrefix(applicationPrefix);
    }

    public void setComponentSuffix(String applicationSuffix) {
        this.customization.setComponentSuffix(applicationSuffix);
    }

    public void setComponentItemPrefix(String namePrefix) {
        this.customization.setComponentItemPrefix(namePrefix);
    }

    public void setComponentItemSuffix(String nameSuffix) {
        this.customization.setComponentItemSuffix(nameSuffix);
    }
}
