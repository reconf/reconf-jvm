/*
 *    Copyright 2013-2014 ReConf Team
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

public class LocalCacheSettingsValidator {

    private static final MessagesBundle msg = MessagesBundle.getBundle(LocalCacheSettings.class);

    public static Set<String> validate(LocalCacheSettings arg) {
        Set<String> errors = new LinkedHashSet<String>();
        if (arg == null) {
            errors.add(msg.get("null"));
            return errors;
        }

        checkMaxLogFileSize(arg, errors);
        checkBackupLocation(arg, errors);

        return errors;
    }

    private static void checkMaxLogFileSize(LocalCacheSettings arg, Collection<String> errors) {
        if (arg.getMaxLogFileSize() < 1 || arg.getMaxLogFileSize() > 50) {
            errors.add(msg.get("backup.max.log.error"));
        }
    }

    private static void checkBackupLocation(LocalCacheSettings arg, Collection<String> errors) {
        if (arg.getBackupLocation() == null) {
            errors.add(msg.get("backup.location.error.null"));
        }
    }
}
