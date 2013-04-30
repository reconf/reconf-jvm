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

import java.util.concurrent.*;
import javax.validation.*;
import javax.xml.bind.annotation.*;
import org.apache.commons.lang.*;
import reconf.client.*;
import reconf.client.annotations.*;
import reconf.infra.i18n.*;
import reconf.infra.log.*;
import reconf.infra.system.*;


@XmlRootElement(name="configuration")
public class ConfigurationElement {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ConfigurationElement.class);
    private DatabaseConfigurationElement backupLocation;
    private String locale;
    private String server;
    private ReloadPolicyElement overrideReloadPolicy;

    public ConfigurationRepositoryElement getConfigurationRepository(Class<?> arg) {
        ConfigurationRepositoryElement result = createNewRepositoryFor(arg);
        if (null != result) {
            result.validate();
            return result;
        }

        return null;
    }

    private ConfigurationRepositoryElement createNewRepositoryFor(Class<?> arg) {
        if (!arg.isAnnotationPresent(ConfigurationRepository.class)) {
            return null;
        }
        ConfigurationRepositoryElement result = new ConfigurationRepositoryElement();
        defineReloadStrategy(arg, result);

        ConfigurationRepository ann = arg.getAnnotation(ConfigurationRepository.class);
        if (null == ann) {
            result.setTimeout(20);
            result.setTimeUnit(TimeUnit.SECONDS);
            return result;
        }

        result.setProduct(ann.product());
        result.setComponent(ann.component());

        if (StringUtils.isBlank(ann.server())) {
            if (StringUtils.isNotBlank(server)) {
                result.setServer(server);
            } else {
                result.setServer(Constants.HTTP_SERVICE_URL);
            }
        } else {
            result.setServer(ann.server());
        }

        result.setInterfaceClass(arg);
        if (null == result.getTimeout()) {
            result.setTimeout(ann.timeout());
        }
        if (null == result.getTimeUnit()) {
            result.setTimeUnit(ann.timeUnit());
        }
        result.setConfigurationItems(ConfigurationItemElement.from(result));
        LoggerHolder.getLog().info(msg.format("new", LineSeparator.value(), result.toString()));

        return result;
    }

    private void defineReloadStrategy(Class<?> arg, ConfigurationRepositoryElement result) {
        if (!arg.isAnnotationPresent(ConfigurationReloadPolicy.class) && !arg.isAnnotationPresent(DoNotReloadPolicy.class)) {
            LoggerHolder.getLog().warn(msg.format("reload.policy.missing", arg));
            return;
        }

        if (arg.isAnnotationPresent(ConfigurationReloadPolicy.class) && arg.isAnnotationPresent(DoNotReloadPolicy.class)) {
            LoggerHolder.getLog().warn(msg.format("error.conflict.reload.policy", arg));
        }

        if (arg.isAnnotationPresent(ConfigurationReloadPolicy.class)) {
            if (overrideReloadPolicy != null) {
                result.setConfigurationReloadPolicy(overrideReloadPolicy);
                LoggerHolder.getLog().info(msg.format("global.reload.policy.override", arg));
                return;
            }

            ConfigurationReloadPolicy reloadAnn = arg.getAnnotation(ConfigurationReloadPolicy.class);
            ReloadPolicyElement reloadPolicy = new ReloadPolicyElement();
            reloadPolicy.setInterval(reloadAnn.interval());
            reloadPolicy.setTimeUnit(reloadAnn.timeUnit());
            result.setConfigurationReloadPolicy(reloadPolicy);
            return;
        }

        if (arg.isAnnotationPresent(DoNotReloadPolicy.class)) {
            result.setDoNotReloadPolicy(new DoNotReloadPolicyElement());
            LoggerHolder.getLog().warn(msg.format("do.not.reload", arg));
            return;
        }

    }

    @XmlElement(name="cache-location")
    public DatabaseConfigurationElement getBackupLocation() {
        return backupLocation;
    }
    public void setBackupLocation(DatabaseConfigurationElement backupLocation) {
        this.backupLocation = backupLocation;
    }

    @XmlElement(name="locale")
    public String getLocale() {
        return locale;
    }
    public void setLocale(String locale) {
        this.locale = locale;
    }

    @XmlElement(name="server")
    public String getServer() {
        return server;
    }
    public void setServer(String server) {
        this.server = server;
    }

    @XmlElement(name="reload-policy") @Valid
    public ReloadPolicyElement getOverrideReloadPolicy() {
        return overrideReloadPolicy;
    }
    public void setOverrideReloadPolicy(ReloadPolicyElement overrideReloadPolicy) {
        this.overrideReloadPolicy = overrideReloadPolicy;
    }
}
