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
package reconf.client.constructors.map;

import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import reconf.client.constructors.*;


public class MapConstructorStringValueTest {

    private MethodData data;
    private Method method;
    private Class<?> targetClass = HashMap.class;

    @Before
    public void prepare() throws Exception {
        method = MapConstructorStringValueTarget.class.getMethod("get", new Class<?>[]{});
    }

    @Test
    public void test_null() throws Throwable {
        data = new MethodData(method, method.getGenericReturnType(), null);
        Object o = new MapConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(targetClass));
        Assert.assertTrue(((Map<?,?>) o).isEmpty());
    }

    @Test
    public void test_empty() throws Throwable {
        data = new MethodData(method, method.getGenericReturnType(), "");
        Object o = new MapConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(targetClass));
        Assert.assertTrue(((Map<?,?>) o).isEmpty());
    }

    @Test(expected=Exception.class)
    public void test_blank() throws Throwable {
        data = new MethodData(method, method.getGenericReturnType(), "   ");
        new MapConstructor().construct(data);
    }

    @Test
    public void test_new_line() throws Throwable {
        data = new MethodData(method, method.getGenericReturnType(), "[ '\n' : '\n' ]");
        Object o = new MapConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(targetClass));
        Map<String, String> cast = (Map<String,String>) o;
        Assert.assertTrue(cast.size() == 1);
        Assert.assertTrue(cast.entrySet().iterator().next().getKey().equals("\n"));
        Assert.assertTrue(cast.entrySet().iterator().next().getValue().equals("\n"));
    }

    @Test
    public void test_separator_value() throws Throwable {
        data = new MethodData(method, method.getGenericReturnType(), "['x':'http://bla']");
        Object o = new MapConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(targetClass));
        Map<String, String> cast = (Map<String,String>) o;
        Assert.assertTrue(cast.size() == 1);
        Assert.assertTrue(cast.entrySet().iterator().next().getKey().equals("x"));
        Assert.assertTrue(cast.entrySet().iterator().next().getValue().equals("http://bla"));
    }

    @Test
    public void test_two_entries() throws Throwable {
        data = new MethodData(method, method.getGenericReturnType(), "['a':'b', 'c':'d']");
        Object o = new MapConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(targetClass));
        Map<String, String> cast = (Map<String,String>) o;
        Assert.assertTrue(cast.size() == 2);
    }

    @Test
    public void empty_map() throws Throwable {
        data = new MethodData(method, method.getGenericReturnType(), "[]");
        Object o = new MapConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(targetClass));
        Map<String, String> cast = (Map<String,String>) o;
        Assert.assertTrue(cast.isEmpty());
    }
}

interface MapConstructorStringValueTarget {
    HashMap<String, String> get();
}