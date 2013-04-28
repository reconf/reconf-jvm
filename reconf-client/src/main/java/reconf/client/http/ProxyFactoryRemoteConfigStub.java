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
package reconf.client.http;

import java.util.concurrent.*;
import reconf.client.*;
import reconf.infra.i18n.*;
import reconf.infra.http.*;
import reconf.infra.network.*;

public class ProxyFactoryRemoteConfigStub {

    private static final MessagesBundle msg = MessagesBundle.getBundle(ProxyFactoryRemoteConfigStub.class);
    private String serviceUri = Constants.HTTP_SERVICE_URL;
    private String product;
    private String component;
    private String instance;
    private long timeout;
    private TimeUnit timeunit;

    public ProxyFactoryRemoteConfigStub(long timeout, TimeUnit timeunit) {
        this.timeout = timeout;
        this.timeunit = timeunit;
        this.instance = LocalHostname.getName();
    }

    public String get(String property) throws Exception {
        final SimpleHttpRequest httpGet = SimpleHttpClient.newGetRequest(serviceUri, product, component, property)
                .addQueryParam("instance", instance)
                .addHeaderField("Accept-Encoding", "gzip,deflate");

        int status = 0;
        try {
            SimpleHttpResponse result = SimpleHttpClient.executeAvoidingSSL(httpGet, timeout, timeunit);
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
        throw new IllegalStateException(msg.format("error.generic", httpGet.getURI()));
    }

    public String getServiceUri() {
        return serviceUri;
    }

    public void setServiceUri(String serviceUri) {
        this.serviceUri = serviceUri;
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
