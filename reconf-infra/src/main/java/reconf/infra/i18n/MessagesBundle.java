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


public class MessagesBundle {

    private ResourceBundle bundle;
    private String className;

    private MessagesBundle(Class<?> cls, String arg) {
        Locale locale = Locale.getDefault();

        String providedLocale = System.getProperty("reconf.locale");

        if (StringUtils.isNotBlank(providedLocale)) {
            locale = new Locale(StringUtils.substringBefore(providedLocale, "_"), StringUtils.substringAfter(providedLocale, "_"));
        }

        LoggerHolder.getLog().debug("MessagesBundle locale [{}]", locale);
        bundle = ResourceBundle.getBundle(arg, locale);
        className = cls.getSimpleName();
    }

    public static MessagesBundle getBundle(Class<?> cls) {
        return new MessagesBundle(cls, StringUtils.replaceChars(cls.getPackage().getName(), '.', '_'));
    }

    public String get(String key) {
        return bundle.getString(className + "." + key);
    }

    public String format(String key, Object...args) {
        return String.format(bundle.getString(className + "." + key), args);
    }
}
