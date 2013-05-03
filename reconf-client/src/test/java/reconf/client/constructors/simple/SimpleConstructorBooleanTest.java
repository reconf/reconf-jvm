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
package reconf.client.constructors.simple;

import java.lang.reflect.*;
import org.junit.*;
import reconf.client.constructors.*;


public class SimpleConstructorBooleanTest {

    private MethodData data;
    private Method method;

    @Before
    public void prepare() throws Exception {
        method = SimpleConstructorBooleanTestTarget.class.getMethod("getBoolean", new Class<?>[]{});
    }

    @Test
    public void test_boolean_class() throws Throwable {
        data = new MethodData(method, method.getReturnType(), "'true'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertTrue(o.getClass().equals(Boolean.class));
    }

    @Test
    public void test_normal_boolean() throws Throwable {
        data = new MethodData(method, method.getReturnType(), "'false'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertEquals(new Boolean("false"), o);
        Assert.assertEquals(false, o);
    }

    @Test
    public void test_case_boolean() throws Throwable {
        data = new MethodData(method, method.getReturnType(), "'fAlSe'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertEquals(new Boolean("false"), o);
        Assert.assertEquals(false, o);
    }

    @Test
    public void test_untrimmed_boolean() throws Throwable {
        data = new MethodData(method, method.getReturnType(), "' true '");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertEquals(new Boolean("true"), o);
        Assert.assertEquals(true, o);
    }

    @Test
    public void test_empty_value() throws Throwable {
        data = new MethodData(method, method.getReturnType(), "''");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertNull(o);
    }
}

interface SimpleConstructorBooleanTestTarget {
    boolean getBoolean();
}