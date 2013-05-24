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
import java.util.Map.Entry;
import java.util.concurrent.*;
import org.apache.commons.lang.*;
import reconf.client.factory.*;
import reconf.infra.i18n.*;

public class MapConstructor implements ObjectConstructor {

    private static final MessagesBundle msg = MessagesBundle.getBundle(MapConstructor.class);

    public Object construct(MethodData data) throws Throwable {

        if (data.hasAdapter()) {
            return data.getAdapter().adapt(data.getValue());
        }

        Class<?> returnClass = null;
        Type keyType = null;
        Type valueType = null;

        if (data.getReturnType() instanceof ParameterizedType){
            ParameterizedType parameterized = (ParameterizedType) data.getReturnType();
            returnClass = (Class<?>) parameterized.getRawType();
            keyType = parameterized.getActualTypeArguments()[0];
            valueType = parameterized.getActualTypeArguments()[1];

        } else if (data.getReturnType() instanceof Class) {
            returnClass = (Class<?>) data.getReturnType();
            keyType = Object.class;
            valueType = Object.class;

        } else {
            throw new IllegalArgumentException(msg.get("error.return"));
        }

        if (returnClass.isInterface()) {
            returnClass = getDefaultImplementation(returnClass);
        }

        Constructor<?> constructor = returnClass.getConstructor(ArrayUtils.EMPTY_CLASS_ARRAY);
        Map<Object, Object> mapInstance = (Map<Object,Object>) constructor.newInstance(ArrayUtils.EMPTY_OBJECT_ARRAY);

        if (null == data.getValue() || StringUtils.isEmpty(data.getValue())) {
            return mapInstance;
        }

        if ((!(keyType instanceof Class)) || (!StringUtils.startsWith(data.getValue(), "[") || !StringUtils.endsWith(data.getValue(), "]"))) {
            throw new IllegalArgumentException(msg.format("error.build", data.getValue()));
        }

        StringParser parser = new StringParser(data.getValue());
        for (Entry<String, String> each : parser.getTokensAsMap().entrySet()) {

            Object value;
            if (valueType instanceof Class) {
                value = ObjectConstructorFactory.get(valueType).construct(new MethodData(data.getMethod(), valueType, each.getValue()));

            } else {
                value = ObjectConstructorFactory.get(valueType).construct(new MethodData(data.getMethod(), valueType, each.getValue()));
            }
            mapInstance.put(ObjectConstructorFactory.get(keyType).construct(new MethodData(data.getMethod(), keyType, each.getKey())), value);

        }

        return mapInstance;
    }

    private Class<?> getDefaultImplementation(Class<?> returnClass) {
        if (Map.class.equals(returnClass)) {
            return HashMap.class;
        }
        if (ConcurrentMap.class.equals(returnClass)) {
            return ConcurrentHashMap.class;
        }
        if (ConcurrentNavigableMap.class.equals(returnClass)) {
            return ConcurrentSkipListMap.class;
        }
        if (NavigableMap.class.equals(returnClass) || SortedMap.class.equals(returnClass)) {
            return TreeMap.class;
        }
        throw new UnsupportedOperationException(msg.format("error.implementation", returnClass));
    }
}
