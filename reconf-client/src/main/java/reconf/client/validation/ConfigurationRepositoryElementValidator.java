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
package reconf.client.validation;

import java.util.*;
import org.apache.commons.lang.*;
import reconf.client.elements.*;
import reconf.infra.i18n.*;

public class ConfigurationRepositoryElementValidator {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ConfigurationRepositoryElement.class);

    public static Set<String> validate(ConfigurationRepositoryElement arg) {
        if (arg == null) {
            return Collections.EMPTY_SET;
        }

        Set<String> errors = new LinkedHashSet<String>();

        checkConnectionSettings(arg, errors);
        checkComponent(arg, errors);
        checkProduct(arg, errors);
        checkUpdateFrequency(arg, errors);
        checkInterfaceClass(arg, errors);
        checkConfigurationItemElements(arg, errors);
        return errors;
    }

    private static void checkConnectionSettings(ConfigurationRepositoryElement arg, Set<String> errors) {
        errors.addAll(ConnectionSettingsValidator.validate(arg.getConnectionSettings()));
    }

    private static void checkComponent(ConfigurationRepositoryElement arg, Collection<String> errors) {
        if (StringUtils.isEmpty(arg.getComponent())) {
            errors.add(msg.get("error.component"));
        }
    }

    private static void checkProduct(ConfigurationRepositoryElement arg, Collection<String> errors) {
        if (StringUtils.isEmpty(arg.getProduct())) {
            errors.add(msg.get("error.product"));
        }
    }

    private static void checkUpdateFrequency(ConfigurationRepositoryElement arg, Collection<String> errors) {
        if (arg.getUpdateFrequency() != null) {
            errors.addAll(UpdateFrequencyElementValidator.validate(arg.getUpdateFrequency()));
        }
    }

    private static void checkInterfaceClass(ConfigurationRepositoryElement arg, Collection<String> errors) {
        if (arg.getInterfaceClass() == null) {
            errors.add("[internal error] interfaceClass is null");
        }
    }

    private static void checkConfigurationItemElements(ConfigurationRepositoryElement arg, Collection<String> errors) {
        if (arg.getConfigurationItems() == null) {
            return;
        }
        for (int i = 0; i < arg.getConfigurationItems().size(); i++) {
            errors.addAll(ConfigurationItemElementValidator.validate(i, arg.getConfigurationItems().get(i)));
        }
    }
}
