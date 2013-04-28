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

import java.net.*;
import java.util.regex.*;
import org.apache.commons.lang.*;


public class RegExp {

    public static final String URI_WITHOUT_PARAMETERS = "([A-Za-z]+://([^?&]*)).*";

    public static String withoutSchemeAndParameters(URI uri) {
        Pattern pattern = Pattern.compile(URI_WITHOUT_PARAMETERS);
        Matcher match = pattern.matcher(uri.toString());
        if (!match.matches()) {
            return uri.toString();
        }
        String target = match.group(2);
        if (target.endsWith("/")) {
            return StringUtils.substringBeforeLast(match.group(2), "/");
        }
        return target;
    }
}
