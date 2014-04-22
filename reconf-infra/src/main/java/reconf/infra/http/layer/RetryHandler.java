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

import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.protocol.*;

public class RetryHandler implements HttpRequestRetryHandler {

    private int maxRetry = 3;

    public RetryHandler(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {

        if (exception instanceof SocketException && executionCount <= 1) {
            return true;
        }
        if (executionCount >= maxRetry) {
            // Do not retry if over max retry count
            return false;
        }
        if (exception instanceof NoHttpResponseException) {
            // Retry if the server dropped connection on us
            return true;
        }
        if (exception instanceof SSLHandshakeException) {
            // Do not retry on SSL handshake exception
            return false;
        }
        HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
        boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
        if (idempotent) {
            // Retry if the request is considered idempotent
            return true;
        }
        return false;
    }

}
