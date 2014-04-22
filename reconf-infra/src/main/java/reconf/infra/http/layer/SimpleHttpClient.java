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
package reconf.infra.http.layer;

import java.net.*;
import java.security.*;
import java.security.cert.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javax.net.ssl.*;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.conn.*;
import org.apache.http.conn.scheme.*;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.*;
import org.apache.http.params.*;
import reconf.infra.i18n.*;

public class SimpleHttpClient {

    private static final MessagesBundle msg = MessagesBundle.getBundle(SimpleHttpClient.class);
    private static ExecutorService requestExecutor = Executors.newFixedThreadPool(10, new ThreadFactory() {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "reconf-http-client-" + counter.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    });

    public static SimpleHttpRequest newGetRequest(String pathBase, String... pathParam) throws URISyntaxException  {
        return new SimpleHttpRequest("GET", pathBase, pathParam);
    }

    public static SimpleHttpResponse executeAvoidingSSL(SimpleHttpRequest request, long timeout, TimeUnit timeunit, int retries) throws Exception {
        return execute(newHttpClientAvoidSSL(timeout, timeunit, retries), request, timeout, timeunit);
    }

    public static SimpleHttpResponse defaultExecute(SimpleHttpRequest request, long timeout, TimeUnit timeunit, int retries) throws Exception {
        return execute(newHttpClient(timeout, timeunit, retries), request, timeout, timeunit);
    }

    private static SimpleHttpResponse execute(HttpClient httpClient, SimpleHttpRequest request, long timeout, TimeUnit timeunit) throws Exception {

        RequestTask task = new RequestTask(httpClient, request);
        Future<SimpleHttpResponse> futureResponse = null;

        try {
            futureResponse = requestExecutor.submit(task);
            return futureResponse.get(timeout, timeunit);

        } catch (TimeoutException e) {
            httpClient.getConnectionManager().shutdown();
            RequestLine line = request.getRequestLine();
            String method = request.getMethod();

            if (line != null && method != null) {
                throw new TimeoutException(msg.format("error.complete", method.toUpperCase(), line.getUri(), timeout, timeunit.toString().toLowerCase()));

            } else {
                throw new TimeoutException(msg.format("error", timeout, timeunit.toString().toLowerCase()));
            }

        }  catch (ExecutionException e) {
            httpClient.getConnectionManager().shutdown();
            throw e;

        } catch (InterruptedException e) {
            httpClient.getConnectionManager().shutdown();
            throw e;

        } finally {
            if (futureResponse != null) {
                futureResponse.cancel(true);
            }
        }
    }

    private static DefaultHttpClient newHttpClientAvoidSSL(long timeout, TimeUnit timeUnit, int retries) throws GeneralSecurityException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            }
        }, null);
        SSLSocketFactory ssf = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        ClientConnectionManager connectionManager = new DefaultHttpClient().getConnectionManager();
        connectionManager.getSchemeRegistry().register(new Scheme("https", 443, ssf));

        DefaultHttpClient httpClient = new DefaultHttpClient(connectionManager, createBasicHttpParams(timeout, timeUnit));
        httpClient.setHttpRequestRetryHandler(new RetryHandler(retries));

        return httpClient;
    }

    private static DefaultHttpClient newHttpClient(long timeout, TimeUnit timeUnit, int retries) throws GeneralSecurityException {
        ClientConnectionManager connectionManager = new DefaultHttpClient().getConnectionManager();
        DefaultHttpClient httpClient = new DefaultHttpClient(connectionManager, createBasicHttpParams(timeout, timeUnit));
        httpClient.setHttpRequestRetryHandler(new RetryHandler(retries));

        return httpClient;
    }

    private static HttpParams createBasicHttpParams(long timeout, TimeUnit timeunit) {
        int timemillis = (int) TimeUnit.MILLISECONDS.convert(timeout, timeunit);
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, timemillis);
        HttpConnectionParams.setSoTimeout(params, timemillis);
        HttpConnectionParams.setStaleCheckingEnabled(params, false);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

        return params;
    }
}