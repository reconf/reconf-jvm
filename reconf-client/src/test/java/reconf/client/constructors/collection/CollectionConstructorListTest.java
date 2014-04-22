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
package reconf.client.constructors.collection;

import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import reconf.client.constructors.*;


public class CollectionConstructorListTest {

    private MethodData data;
    private Method method;
    private final Class<?> targetClass = ArrayList.class;

    @Before
    public void prepare() throws Exception {
        method = CollectionConstructorListTarget.class.getMethod("get", new Class<?>[]{});
    }

    @Test
    public void test_null_string_list() throws Throwable {
        data = new MethodData(method, method.getGenericReturnType(), null);
        Object o = new CollectionConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(targetClass));
        Assert.assertTrue(((Collection<Collection<?>>) o).isEmpty());
    }

}

interface CollectionConstructorListTarget {
    List<String> get();
}