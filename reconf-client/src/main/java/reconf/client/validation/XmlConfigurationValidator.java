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
import reconf.client.setup.*;
import reconf.infra.i18n.*;

public class XmlConfigurationValidator {

    private static final MessagesBundle msg = MessagesBundle.getBundle(XmlConfiguration.class);

    public static Set<String> validate(XmlConfiguration arg) {
        Set<String> errors = new LinkedHashSet<String>();

        return errors;
    }

    private static void checkLocalCacheSettings(XmlConfiguration arg, Collection<String> errors) {
        if (arg.getLocalCacheSettings() == null) {
            errors.add("local-cache is null");
            return;
        }
    }
}
