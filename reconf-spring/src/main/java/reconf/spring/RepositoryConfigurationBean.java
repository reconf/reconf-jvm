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

import java.util.*;
import org.springframework.beans.factory.*;
import reconf.client.callback.*;
import reconf.client.proxy.*;

@SuppressWarnings("rawtypes")
public class RepositoryConfigurationBean implements FactoryBean {

    private Class<?> configInterface;
    private Customization customization = new Customization();

    public Object getObject() throws Exception {
        return ConfigurationRepositoryFactory.get(getConfigInterface(), getCustomization());
    }

    public Class<?> getObjectType() {
        return configInterface;
    }

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

    public void setProductPrefix(String productPrefix) {
        this.customization.setProductPrefix(productPrefix);
    }

    public void setProductSuffix(String productSuffix) {
        this.customization.setProductSuffix(productSuffix);
    }

    public void setUpdateListeners(Collection<UpdateListener> updateListeners) {
        this.customization.setUpdateListeners(updateListeners);
    }

    public void setErrorListeners(Collection<ErrorListener> errorListeners) {
        this.customization.setErrorListeners(errorListeners);
    }
}
