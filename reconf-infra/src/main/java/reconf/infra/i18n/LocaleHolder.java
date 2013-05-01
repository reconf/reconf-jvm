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
package reconf.infra.i18n;

import java.util.*;
import org.apache.commons.lang.*;
import reconf.infra.log.*;


public final class LocaleHolder {

    private static String providedLocale;

    public static void set(String value) {
        if (providedLocale == null) {
            providedLocale = value;
        }
    }

    public static Locale value() {
        if (StringUtils.isNotBlank(providedLocale)) {
            try {
                return LocaleUtils.toLocale(providedLocale);
            } catch (IllegalArgumentException e) {
                LoggerHolder.getLog().error(String.format("invalid locale [%s]. assuming default [%s]", providedLocale, Locale.getDefault()));
            }
        }
        return Locale.getDefault();
    }
}
