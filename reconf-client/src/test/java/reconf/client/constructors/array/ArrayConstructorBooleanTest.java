/*
 *    Copyright 2013-2014 ReConf Team
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
package reconf.client.constructors.array;

import java.lang.reflect.*;
import org.junit.*;
import reconf.client.constructors.*;


public class ArrayConstructorBooleanTest {

    private MethodData data;
    private Method method;
    private final Class<?> arrayClass = new Boolean[0].getClass();
    private final Class<?> targetClass = Boolean.class;

    @Before
    public void prepare() throws Exception {
        method = ArrayConstructorBooleanTarget.class.getMethod("get", new Class<?>[]{});
    }

    @Test
    public void test_null() throws Throwable {
        data = new MethodData(method, targetClass, null);
        Object o = new ArrayConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(arrayClass));
        Assert.assertTrue(((Object[]) o).length == 0);
    }

    @Test
    public void test_blank() throws Throwable {
        data = new MethodData(method, targetClass, "");
        Object o = new ArrayConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(arrayClass));
        Assert.assertTrue(((Object[]) o).length == 0);
    }

    @Test
    public void test_empty() throws Throwable {
        data = new MethodData(method, targetClass, "[]");
        Object o = new ArrayConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(arrayClass));
        Assert.assertTrue(((Object[]) o).length == 0);
    }

    @Test
    public void test_two_elem() throws Throwable {
        data = new MethodData(method, targetClass, "[ 'TrUe' , 'faLse' ]");
        Object o = new ArrayConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(arrayClass));
        Assert.assertTrue(((Object[]) o).length == 2);
        Assert.assertTrue(((Object[]) o)[0].equals(true));
        Assert.assertTrue(((Object[]) o)[1].equals(false));
    }
}

interface ArrayConstructorBooleanTarget {
    boolean[] get();
}