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
import org.apache.commons.lang.*;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.util.*;
import reconf.infra.system.*;

public class SimpleHttpResponse {

    public static final String CHARSET_DEFAULT = "UTF-8";

    private final CloseableHttpResponse response;
    private final CloseableHttpClient httpClient;
    private String body;
    private int statusCode = 0;
    private String responseAsString;
    private Exception exception;

    SimpleHttpResponse(CloseableHttpClient httpClient, HttpUriRequest request) throws ClientProtocolException, IOException {
        this.httpClient = httpClient;
        this.response = httpClient.execute(request);
        consumeResponse();
        finalizeResponse();
    }

    private void consumeResponse() {
        consumeStatusCode();
        consumeBody();
    }

    private void consumeStatusCode() {
        try {
            this.statusCode = response.getStatusLine().getStatusCode();
        } catch (Exception ignored) { }
    }

    private void consumeBody() {
        if (response.getEntity() == null) {
            body = StringUtils.EMPTY;
        }

        HttpEntity entity = response.getEntity();
        String encoding = getContentEncoding(entity);
        String charset = getContentCharSet(entity);

        try {
            if ("gzip".equalsIgnoreCase(encoding)) {
                GzipDecompressingEntity gzip = new GzipDecompressingEntity(entity);
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                gzip.writeTo(output);
                body = output.toString(charset);

            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), charset));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                    if (reader.ready()) {
                        result.append(LineSeparator.value());
                    }
                }
                body = result.toString();
            }
        } catch (Exception e) {
            exception = e;

        } finally {
            try {
                EntityUtils.consume(entity);
            } catch (Exception e) {
                exception = e;
            }
        }
    }

    private void finalizeResponse() {
        if (response == null) {
            this.responseAsString = StringUtils.EMPTY;
            return;
        }
        try {
            this.responseAsString = response.toString();
        } catch (Exception ignored) {
            this.responseAsString = StringUtils.EMPTY;
        }
        try {
            response.close();
        } catch (Exception ignored) { }

        try {
            httpClient.close();
        } catch (Exception ignored) { }
    }


    public String getBodyAsString() throws Exception {
        if (exception != null) {
            throw exception;
        }
        return body;
    }

    private static String getContentCharSet(HttpEntity entity) {
        if (entity == null) {
            return CHARSET_DEFAULT;
        }

        Header type = entity.getContentType();
        if (type != null) {
            for (HeaderElement headerElement : type.getElements()) {
                if ("charset".equalsIgnoreCase(headerElement.getName())) {
                    return headerElement.getValue();
                }
            }
        }
        return CHARSET_DEFAULT;
    }

    private static String getContentEncoding(HttpEntity entity) {
        if (entity == null) {
            return StringUtils.EMPTY;
        }
        if (entity != null) {
            Header encoding = entity.getContentEncoding();
            if (encoding != null) {
                return encoding.getValue();
            }
        }
        return StringUtils.EMPTY;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        return responseAsString;
    }
}
