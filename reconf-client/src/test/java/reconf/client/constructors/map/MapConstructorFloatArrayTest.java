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
package reconf.client.constructors.map;

import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import reconf.client.constructors.*;


public class MapConstructorFloatArrayTest {

    private MethodData data;
    private Method method;
    private Class<?> targetClass = HashMap.class;

    @Before
    public void prepare() throws Exception {
        method = MapConstructorFloatArrayValueTarget.class.getMethod("get", new Class<?>[]{});
    }

    @Test
    public void test_normal_value() throws Throwable {
        data = new MethodData(method, method.getGenericReturnType(), "['k':['1.01','-1.000001']]");
        Object o = new MapConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(targetClass));
        Map<String, Float[]> cast = (Map<String,Float[]>) o;
        Assert.assertTrue(cast.size() == 1);
        Assert.assertTrue(cast.entrySet().iterator().next().getKey().equals("k"));
        Float[] value = cast.entrySet().iterator().next().getValue();
        Assert.assertTrue(Float.compare(value[0], 1.01f) == 0);
        Assert.assertTrue(Float.compare(value[1], -1.000001f) == 0);
    }
}

interface MapConstructorFloatArrayValueTarget {
    HashMap<String, Float[]> get();
}