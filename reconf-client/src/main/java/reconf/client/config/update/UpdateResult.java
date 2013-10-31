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
package reconf.client.config.update;

import java.lang.reflect.*;

public class UpdateResult {

    private final boolean success;
    private final boolean change;
    private final Object object;
    private final String product;
    private final String component;
    private final String item;
    private final Method method;
    private final Class<?> cast;

    public UpdateResult(Object object, Class<?> cast, boolean success, boolean change, String product, String component, String item, Method method) {
        this.object = object;
        this.success = success;
        this.change = change;
        this.product = product;
        this.component = component;
        this.item = item;
        this.method = method;
        this.cast = cast;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isChange() {
        return change;
    }

    public Object getObject() {
        return object;
    }

    public String getProduct() {
        return product;
    }

    public String getComponent() {
        return component;
    }

    public String getItem() {
        return item;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getCast() {
        return cast;
    }
}
