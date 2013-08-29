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

public class ConfigurationItemElementValidator {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ConfigurationItemElement.class);

    public static Set<String> validate(int pos, ConfigurationItemElement arg) {
        if (arg == null) {
            return Collections.EMPTY_SET;
        }

        Set<String> errors = new LinkedHashSet<String>();
        String prefix = getPrefix(pos);
        checkMethodName(prefix, arg, errors);
        checkValue(prefix, arg, errors);
        checkAdapter(prefix, arg, errors);
        checkMethod(prefix, arg, errors);
        checkUpdateFrequency(prefix, arg, errors);
        return errors;
    }

    private static void checkMethodName(String prefix, ConfigurationItemElement arg, Collection<String> errors) {
        if (arg.getMethodName() == null) {
            errors.add(prefix + msg.get("method.name.null"));
        }
        if (arg.getMethodName() != null && StringUtils.isEmpty(arg.getMethodName())) {
            errors.add(prefix + msg.get("method.name.empty"));
        }
    }

    private static void checkValue(String prefix, ConfigurationItemElement arg, Collection<String> errors) {
        if (arg.getValue() == null) {
            errors.add(prefix + msg.get("error.value"));
        }
        if (arg.getValue() != null && StringUtils.isEmpty(arg.getValue())) {
            errors.add(prefix + msg.get("error.value"));
        }
    }

    private static void checkAdapter(String prefix, ConfigurationItemElement arg, Collection<String> errors) {
        if (arg.getAdapter() == null) {
            errors.add(prefix + msg.get("adapter.null"));
        }
    }

    private static void checkMethod(String prefix, ConfigurationItemElement arg, Collection<String> errors) {
        if (arg.getMethod() == null) {
            errors.add(prefix + msg.get("method.null"));
        }
    }

    private static void checkUpdateFrequency(String prefix, ConfigurationItemElement arg, Collection<String> errors) {
        if (arg.getUpdateFrequency() != null) {
            Collection<String> updateFreqErrors = UpdateFrequencyElementValidator.validate(arg.getUpdateFrequency());
            for (String error : updateFreqErrors) {
                errors.add(prefix + error);
            }
        }
    }

    private static String getPrefix(int pos) {
        return ConfigurationItemElement.class.getSimpleName() + "[" + pos + "] ";
    }
}
