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
package reconf.infra.http;

import java.util.concurrent.TimeUnit;
import reconf.infra.http.layer.SimpleHttpRequest;
import reconf.infra.http.layer.SimpleHttpResponse;
import reconf.infra.i18n.MessagesBundle;

public class ServerStub {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ServerStub.class);
    private static final String PROTOCOL = "reconf.client-v1+text/plain";
    private SimpleHttpDelegatorFactory factory;
    private final String serviceUri;
    private final long timeout;
    private final TimeUnit timeunit;
    private final int maxRetry;
    private String product;
    private String component;
    private String instance;

    public ServerStub(String serviceUri, long timeout, TimeUnit timeUnit, int maxRetry) {
        this(serviceUri, timeout, timeUnit, maxRetry, SimpleHttpDelegatorFactory.defaultImplementation);
    }

    public ServerStub(String serviceUri, long timeout, TimeUnit timeUnit, int maxRetry, SimpleHttpDelegatorFactory factory) {
        this.serviceUri = serviceUri;
        this.timeout = timeout;
        this.timeunit = timeUnit;
        this.instance = LocalHostname.getName();
        this.maxRetry = maxRetry;
        this.factory = factory;
    }

    public String get(String property) throws Exception {
        final SimpleHttpRequest httpGet = factory.newGetRequest(serviceUri, product, component, property)
                .addQueryParam("instance", instance)
                .addHeaderField("Accept-Encoding", "gzip,deflate")
                .addHeaderField("X-ReConf-Protocol", PROTOCOL);

        int status = 0;
        SimpleHttpResponse result = null;
        try {
            result = factory.execute(httpGet, timeout, timeunit, maxRetry);
            status = result.getStatusCode();
            if (status == 200) {
                return result.getBodyAsString();
            }
        } catch (Exception e) {
            if (status == 0) {
                throw new IllegalStateException(msg.format("error.generic", httpGet.getURI()), e);
            }
            throw new IllegalStateException(msg.format("error.http", status, httpGet.getURI()), e);
        }

        throw new IllegalStateException(msg.format("error.http", result.getStatusCode(), httpGet.getURI()));
    }

    public String getComponent() {
        return component;
    }
    public void setComponent(String component) {
        this.component = component;
    }

    public String getProduct() {
        return product;
    }
    public void setProduct(String product) {
        this.product = product;
    }
}
