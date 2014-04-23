/*
 *    Copyright 1996-2014 UOL Inc
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
import java.util.concurrent.*;
import reconf.client.setup.*;
import reconf.infra.i18n.*;

public class GlobalPollingFrequencySettingsValidator {

    private static final MessagesBundle msg = MessagesBundle.getBundle(GlobalPollingFrequencySettings.class);

    public static Set<String> validate(GlobalPollingFrequencySettings arg) {
        Set<String> errors = new LinkedHashSet<String>();

        checkInterval(arg, errors);
        checkTimeUnit(arg, errors);
        return errors;
    }

    private static void checkInterval(GlobalPollingFrequencySettings arg, Collection<String> errors) {
        if (arg.getInterval() == null) {
            errors.add(msg.get("interval.error"));
        }
        if (arg.getInterval() != null && arg.getInterval() < 1) {
            errors.add(msg.get("interval.error"));
        }
    }

    private static void checkTimeUnit(GlobalPollingFrequencySettings arg, Collection<String> errors) {
        if (arg.getTimeUnit() == null) {
            errors.add(msg.get("timeUnit.null"));
        }
        if (!EnumSet.of(TimeUnit.MINUTES,TimeUnit.HOURS,TimeUnit.DAYS).contains(arg.getTimeUnit())) {
            errors.add(msg.get("timeUnit.null"));
        }
    }

}
