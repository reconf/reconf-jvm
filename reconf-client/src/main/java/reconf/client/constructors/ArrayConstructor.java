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
package reconf.client.constructors;

import java.lang.reflect.*;
import java.util.*;
import reconf.client.factory.*;


public class ArrayConstructor implements ObjectConstructor {

    public Object construct(MethodData data) throws Throwable {
        if (data.hasAdapter()) {
            return data.getAdapter().adapt(data.getValue());
        }

        Class<?> returnClass;

        if (data.getReturnType() instanceof Class<?>) {
            Class<?> cast = (Class<?>) data.getReturnType();
            returnClass = null == cast.getComponentType() ? cast : cast.getComponentType();

        } else if (data.getReturnType() instanceof GenericArrayType) {
            returnClass = (Class<?>) ((GenericArrayType) data.getReturnType()).getGenericComponentType();
        } else {
            throw new UnsupportedOperationException();
        }

        if (null == data.getValue()) {
            return Array.newInstance(returnClass, 0);
        }

        List<String> parsed = new StringParser(data.getValue()).getTokens();
        Object result = Array.newInstance(returnClass, parsed.size());

        for (int i = 0; i < parsed.size(); i++) {
            Array.set(result, i, ObjectConstructorFactory.get(returnClass).construct(new MethodData(data.getMethod(), returnClass, parsed.get(i))));
        }
        return result;
    }
}
