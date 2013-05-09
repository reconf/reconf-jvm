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
package reconf.client.i18n;

import java.util.*;
import java.util.Map.Entry;
import org.apache.commons.lang.*;
import org.junit.*;

@SuppressWarnings("serial")
public class MessagedBundleTest {

    Map<String, List<String>> basePackageBundles = new LinkedHashMap<String, List<String>>() {
        {
            put("reconf.client.", Arrays.asList("/messages_client_pt_BR.properties", "/messages_client.properties"));
            put("reconf.infra.", Arrays.asList("/messages_infra_pt_BR.properties", "/messages_infra.properties"));
        }

    };

    @Test
    public void test() throws Exception {

        for (Entry<String, List<String>> each : basePackageBundles.entrySet()) {
            for (String path : each.getValue()) {
                Properties props = new Properties();
                props.load(this.getClass().getResourceAsStream(path));

                for (Object key : props.keySet()) {
                    List<String> parts = Arrays.asList(StringUtils.split(each.getKey() + key.toString(), "."));
                    boolean found = false;
                    for (int i = 0; i < parts.size(); i++) {
                        String className = StringUtils.join(parts.subList(0, i), ".");
                        try {
                            Class.forName(className);
                            found = true;
                            break;
                        } catch (ClassNotFoundException e) { }
                    }
                    if (!found) {
                        throw new ClassNotFoundException(each.getKey() + key.toString() + " at " + path);
                    }
                }
            }
        }
    }

}
