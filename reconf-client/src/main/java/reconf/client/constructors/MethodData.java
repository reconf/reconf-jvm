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
import reconf.client.adapters.*;


public class MethodData {

    private final Method method;
    private final Type returnType;
    private final String value;
    private final ClientAdaptersLocator adapter;

    public MethodData(Method method, Type returnType, String value) {
        this.method = method;
        this.returnType = returnType;
        this.value = value;
        this.adapter = null;
    }

    public MethodData(Method method, Type returnType, String value, ClientAdaptersLocator adapter) {
        this.method = method;
        this.returnType = returnType;
        this.value = value;
        this.adapter = adapter;
    }

    public Method getMethod() {
        return method;
    }

    public Type getReturnType() {
        return returnType;
    }

    public String getValue() {
        return value;
    }

    public ClientAdaptersLocator getAdapter() {
        return adapter;
    }

    public boolean hasAdapter() {
        return adapter != null && !(adapter instanceof NoConfigurationAdapter);
    }
}
