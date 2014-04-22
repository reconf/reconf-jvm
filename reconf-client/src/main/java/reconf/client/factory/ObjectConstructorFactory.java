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
package reconf.client.factory;

import java.lang.reflect.*;
import java.util.*;
import reconf.client.constructors.*;


public final class ObjectConstructorFactory {

    private static final ArrayConstructor array = new ArrayConstructor();
    private static final MapConstructor map = new MapConstructor();
    private static final SimpleConstructor simple = new SimpleConstructor();
    private static final CollectionConstructor collection = new CollectionConstructor();

    private ObjectConstructorFactory() {
        throw new UnsupportedOperationException();
    }

    public static ObjectConstructor get(Type type) {

        Class<?> clazz = null;

        if (type instanceof ParameterizedType){
            ParameterizedType parameterized = (ParameterizedType) type;
            clazz = (Class<?>) parameterized.getRawType();

        } else if (type instanceof Class) {
            clazz = (Class<?>) type;

        } else if (type instanceof GenericArrayType) {
            return array;

        } else {
            return null;
        }

        if (clazz.isArray()) {
            return array;

        } else if (Collection.class.isAssignableFrom(clazz)) {
            return collection;

        } else if (Map.class.isAssignableFrom(clazz)) {
            return map;
        }

        return simple;
    }

    public static boolean isSimple(Type type) {
        Class<?> clazz = null;

        if (type instanceof ParameterizedType){
            ParameterizedType parameterized = (ParameterizedType) type;
            clazz = (Class<?>) parameterized.getRawType();

        } else if (type instanceof Class) {
            clazz = (Class<?>) type;

        } else if (type instanceof GenericArrayType) {
            return false;

        } else {
            return false;
        }

        if (clazz.isArray()) {
            return false;

        } else if (Collection.class.isAssignableFrom(clazz)) {
            return false;

        } else if (Map.class.isAssignableFrom(clazz)) {
            return false;
        }

        return true;
    }
}
