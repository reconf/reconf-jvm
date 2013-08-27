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
package reconf.infra.http.layer;

import java.net.*;
import java.security.*;
import java.security.cert.*;
import java.util.concurrent.*;
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
    private static ExecutorService requestExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    });

    /**
     * Creates a new HTTP GET Request.
     * @param pathBase The base URI path of the request
     * @param pathParam Optional path parameters (without slashes) to be appended to the base path
     * @return a new HTTP GET Request
     * @throws URISyntaxException if the pathBase appended with the pathParams cannot be recognized as a URI
     */
    public static SimpleHttpRequest newGetRequest(String pathBase, String... pathParam) throws URISyntaxException  {
        return new SimpleHttpRequest("GET", pathBase, pathParam);
    }

    /**
     * Executes a given HTTP/HTTPS request respecting a given timeout and avoiding any SSL errors.
     * @param request The HTTP request to be executed
     * @param timeout The given timeout of the request
     * @param timeunit The unit of the timeout
     * @return the HTTP response of the request execution
     * @throws ExecutionException if the execution thread fails
     * @throws TimeoutException if the timeout occurs
     * @throws InterruptedException if the exection thread is interrupted
     * @throws GeneralSecurityException if the SSL avoiding fails
     */
    public static SimpleHttpResponse executeAvoidingSSL(SimpleHttpRequest request, long timeout, TimeUnit timeunit, int retries) throws Exception {
        return execute(newHttpClientAvoidSSL(timeout, timeunit, retries), request, timeout, timeunit);
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