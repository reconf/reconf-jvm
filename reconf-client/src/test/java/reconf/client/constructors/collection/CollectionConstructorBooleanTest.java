/*
 *    Copyright 2013-2015 ReConf Team
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
package reconf.client.constructors.collection;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import reconf.client.constructors.CollectionConstructor;
import reconf.client.constructors.MethodData;


public class CollectionConstructorBooleanTest {

    private MethodData data;
    private Method methodList;

    @Before
    public void prepare() throws Exception {
        methodList = CollectionConstructorBooleanTestTarget.class.getMethod("list", new Class<?>[]{});
    }

    @Test
    public void test_null_boolean_list() throws Throwable {
        data = new MethodData(methodList, methodList.getGenericReturnType(), null);
        Object o = new CollectionConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(ArrayList.class));
        Assert.assertTrue(((Collection<?>) o).isEmpty());
    }

    @Test
    public void test_empty_boolean_list() throws Throwable {
        data = new MethodData(methodList, methodList.getGenericReturnType(), "");
        Object o = new CollectionConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(ArrayList.class));
        Assert.assertTrue(((Collection<?>) o).isEmpty());
    }

    @Test
    public void test_one_elem_boolean_list() throws Throwable {
        data = new MethodData(methodList, methodList.getGenericReturnType(), "['TrUe',]");
        Object o = new CollectionConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(ArrayList.class));
        Assert.assertTrue(((Collection<?>) o).size() == 1);
        Assert.assertTrue(((ArrayList<Boolean>) o).get(0).equals(Boolean.TRUE));
    }

    @Test
    public void test_two_elem_boolean_list() throws Throwable {
        data = new MethodData(methodList, methodList.getGenericReturnType(), "['TrUe', 'false'] ");
        Object o = new CollectionConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(ArrayList.class));
        Assert.assertTrue(((Collection<?>) o).size() == 2);
        Assert.assertTrue(((ArrayList<Boolean>) o).get(0).equals(Boolean.TRUE));
        Assert.assertTrue(((ArrayList<Boolean>) o).get(1).equals(Boolean.FALSE));
    }
}

interface CollectionConstructorBooleanTestTarget {
    ArrayList<Boolean> list();
}