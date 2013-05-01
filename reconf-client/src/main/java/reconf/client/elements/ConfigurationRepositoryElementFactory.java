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
import reconf.client.annotations.*;
import reconf.client.setup.*;
import reconf.infra.i18n.*;
import reconf.infra.log.*;
import reconf.infra.system.*;
import reconf.infra.throwables.*;


public class ConfigurationRepositoryElementFactory {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ConfigurationRepositoryElementFactory.class);
    private XmlConfiguration configuration;

    public ConfigurationRepositoryElementFactory(XmlConfiguration arg) {
        this.configuration = arg;
    }

    public ConfigurationRepositoryElement create(Class<?> arg) {
        ConfigurationRepositoryElement result = createNewRepositoryFor(arg);
        validate(result);
        return result;
    }

    private ConfigurationRepositoryElement createNewRepositoryFor(Class<?> arg) {
        if (!arg.isAnnotationPresent(ConfigurationRepository.class)) {
            return null;
        }
        ConfigurationRepositoryElement result = new ConfigurationRepositoryElement();
        defineReloadStrategy(arg, result);

        ConfigurationRepository ann = arg.getAnnotation(ConfigurationRepository.class);
        result.setProduct(ann.product());
        result.setComponent(ann.component());
        result.setConnectionSettings(configuration.getConnectionSettings());
        result.setInterfaceClass(arg);
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
            if (configuration.getAnnotationOverride() != null) {
                result.setConfigurationReloadPolicy(configuration.getAnnotationOverride());
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

    private Set<?> validate(ConfigurationRepositoryElement arg) {
        if (arg == null) {
            throw new ReConfInitializationError(msg.get("error.internal"));
        }
        return ClassValidatorFactory.create(ConfigurationRepositoryElement.class).validate(arg);
    }
}
