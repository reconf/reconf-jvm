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
package reconf.infra.io;

import java.io.*;
import java.net.*;
import reconf.infra.i18n.*;
import reconf.infra.log.*;


public class ClasspathReader {

    private static final MessagesBundle msg = MessagesBundle.getBundle(InputStreamReader.class);

    public static String read(URI uri) {
        if (null == uri) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        InputStream is = null;

        try {
            String name = RegExp.withoutSchemeAndParameters(uri);
            is = classLoader(name);
            if (null == is) {
                is = currentThread(name);
                if (null == is) {
                    is = newClass(name);
                }
                if (null == is) {
                    is = classLoader("/" + name);
                }
                if (null == is) {
                    is = currentThread("/" + name);
                }
                if (null == is) {
                    is = newClass("/" + name);
                }
                if (null == is) {
                    return "";
                }
            }

            sb.append(InputStreamReader.read(is));

        } catch (Exception e) {
            LoggerHolder.getLog().error(msg.get("error"), e);

        } finally {
            Closeables.closeQuietly(is);
        }
        return sb.toString();
    }

    private static InputStream classLoader(String arg) {
        return ClassLoader.getSystemResourceAsStream(arg);
    }

    private static InputStream currentThread(String arg) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(arg);
    }

    private static InputStream newClass(String arg) {
        return Class.class.getResourceAsStream(arg);
    }

    public static String read(String uri) {
        return read(URI.create(uri));
    }
}
