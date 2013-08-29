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
package reconf.client.factory;

import java.util.*;
import java.util.Map.Entry;
import org.apache.commons.collections.*;
import org.apache.commons.lang.*;
import reconf.client.annotations.*;
import reconf.client.elements.*;
import reconf.client.setup.*;
import reconf.client.validation.*;
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

        if(!arg.isInterface()) {
            throw new ReConfInitializationError(msg.format("error.is.not.interface", arg.getCanonicalName()));
        }

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
        if (!arg.isAnnotationPresent(UpdateFrequency.class) && !arg.isAnnotationPresent(DoNotUpdate.class)) {
            LoggerHolder.getLog().warn(msg.format("reload.policy.missing", arg));
            return;
        }

        if (arg.isAnnotationPresent(UpdateFrequency.class) && arg.isAnnotationPresent(DoNotUpdate.class)) {
            LoggerHolder.getLog().warn(msg.format("error.conflict.reload.policy", arg));
        }

        if (arg.isAnnotationPresent(UpdateFrequency.class)) {
            if (configuration.getAnnotationOverride() != null) {
                UpdateFrequencyElement overrideFrequency = new UpdateFrequencyElement();
                overrideFrequency.setInterval(configuration.getAnnotationOverride().getInterval());
                overrideFrequency.setTimeUnit(configuration.getAnnotationOverride().getTimeUnit());
                result.setUpdateFrequency(overrideFrequency);
                LoggerHolder.getLog().info(msg.format("global.reload.policy.override", arg));
                return;
            }

            UpdateFrequency reloadAnn = arg.getAnnotation(UpdateFrequency.class);
            UpdateFrequencyElement reloadPolicy = new UpdateFrequencyElement();
            reloadPolicy.setInterval(reloadAnn.interval());
            reloadPolicy.setTimeUnit(reloadAnn.timeUnit());
            result.setUpdateFrequency(reloadPolicy);
            return;
        }

        if (arg.isAnnotationPresent(DoNotUpdate.class)) {
            result.setDoNotUpdate(new DoNotUpdateElement());
            LoggerHolder.getLog().warn(msg.format("do.not.reload", arg));
            return;
        }
    }

    private void validate(ConfigurationRepositoryElement arg) {
        if (arg == null) {
            throw new ReConfInitializationError(msg.get("error.internal"));
        }

        Map<String, String> violations = ConfigurationRepositoryElementValidator.validate(arg);
        if (MapUtils.isEmpty(violations)) {
            return;
        }

        List<String> errors = new ArrayList<String>();
        int i = 1;
        for (Entry<String, String> violation : violations.entrySet()) {
            errors.add(i++ + " - " + violation.getValue() + " @ " + StringUtils.replace(arg.getInterfaceClass().toString(), "interface ", "") + "." + violation.getKey());
        }

        throw new ReConfInitializationError(msg.format("error.factory", LineSeparator.value(), StringUtils.join(errors, LineSeparator.value()))+LineSeparator.value());
    }
}
