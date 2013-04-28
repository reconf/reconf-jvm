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
package reconf.client.constructor.simple;

import java.lang.reflect.*;
import java.util.concurrent.*;
import org.junit.*;
import reconf.client.constructors.*;


public class SimpleConstructorTimeUnitTest {

    private MethodData data;
    private Method method;

    @Before
    public void prepare() throws Exception {
        method = SimpleConstructorTimeUnitTestTarget.class.getMethod("getTimeUnit", new Class<?>[]{});
    }

    @Test
    public void test_class() throws Throwable {
        data = new MethodData(method, method.getReturnType(), "'SECONDS'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertTrue(TimeUnit.class.isAssignableFrom(o.getClass()));
    }

    @Test
    public void test_normal_string() throws Throwable {
        data = new MethodData(method, method.getReturnType(), "'SECONDS'");
        Object o = new SimpleConstructor().construct(data);
        Assert.assertEquals(TimeUnit.SECONDS, o);
    }
}

interface SimpleConstructorTimeUnitTestTarget {
    TimeUnit getTimeUnit();
}