/*
 *    Copyright 2013-2015 ReConf Team
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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import reconf.client.setup.XmlConfiguration;

public class XmlConfigurationValidator {

    public static Set<String> validate(XmlConfiguration arg) {
        Set<String> errors = new LinkedHashSet<String>();

        checkLocalCacheSettings(arg, errors);
        checkConnectionSettings(arg, errors);
        checkAnnotationOverride(arg, errors);
        return errors;
    }

    private static void checkLocalCacheSettings(XmlConfiguration arg, Collection<String> errors) {
        errors.addAll(LocalCacheSettingsValidator.validate(arg.getLocalCacheSettings()));
    }

    private static void checkConnectionSettings(XmlConfiguration arg, Collection<String> errors) {
        errors.addAll(ConnectionSettingsValidator.validate(arg.getConnectionSettings()));
    }

    private static void checkAnnotationOverride(XmlConfiguration arg, Collection<String> errors) {
        if (arg.getAnnotationOverride() != null) {
            errors.addAll(GlobalPollingFrequencySettingsValidator.validate(arg.getAnnotationOverride()));
        }
    }
}
