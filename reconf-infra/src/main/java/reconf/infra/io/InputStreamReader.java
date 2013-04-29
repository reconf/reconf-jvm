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
import java.util.*;
import org.apache.commons.io.*;
import reconf.infra.log.*;
import reconf.infra.system.*;


public class InputStreamReader {

    public static String read(InputStream is) {
        if (null == is) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        List<String> lines = readLines(is);
        for (String line : lines) {
            sb.append(line).append(LineSeparator.value());
        }
        return sb.toString();
    }

    public static List<String> readLines(InputStream is) {
        if (null == is) {
            return Collections.EMPTY_LIST;
        }

        List<String> lines = new ArrayList<String>();
        try {
            lines.addAll(IOUtils.readLines(is));

        } catch (Exception e) {
            LoggerHolder.getLog().error("error while reading the inputstream", e);

        } finally {
            Closeables.closeQuietly(is);
        }
        return lines;
    }
}
