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
    private String headPackageName;
    private String tailPackageName = StringUtils.EMPTY;

    private MessagesBundle(Class<?> cls, String arg) {
        Locale locale = LocaleHolder.value();
        LoggerHolder.getLog().debug("MessagesBundle locale [{}]", locale);
        className = cls.getSimpleName();

        String[] packages = StringUtils.split(cls.getPackage().getName(), '.');
        if (packages.length == 0 || packages.length == 1) {
            throw new IllegalArgumentException("only meant to be used inside reconf");
        }

        headPackageName = packages[1];
        if (packages.length >= 2) {
            tailPackageName = StringUtils.substringAfter(cls.getPackage().getName(), "reconf." + headPackageName + ".");
        }
        bundle = ResourceBundle.getBundle(arg + headPackageName, locale);
    }

    public static MessagesBundle getBundle(Class<?> cls) {
        return new MessagesBundle(cls, "messages_");
    }

    public String get(String key) {
        return bundle.getString(tailPackageName + "." + className + "." + key);
    }

    public String format(String key, Object...args) {
        return String.format(bundle.getString(className + "." + key), args);
    }
}
