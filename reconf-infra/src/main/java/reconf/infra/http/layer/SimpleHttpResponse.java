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
import org.apache.http.util.*;
import reconf.infra.system.*;

public class SimpleHttpResponse {

    public static final String CHARSET_DEFAULT = "UTF-8";

    private final HttpResponse response;
    private final HttpClient   httpClient;
    private String body;

    SimpleHttpResponse(HttpClient httpClient, HttpUriRequest request) throws ClientProtocolException, IOException {
        this.body = null;
        this.httpClient = httpClient;
        this.response = httpClient.execute(request);
    }

    /**
     * @return this HTTP status code
     */
    public int getStatusCode() {
        return response.getStatusLine().getStatusCode();
    }

    /**
     * @return this HTTP entity content as String.
     * @throws IOException if an error occurs while reading the input stream
     */
    public String getBodyAsString() throws IOException {
        if (body == null) {
            final HttpEntity entity = response.getEntity();
            if (entity == null) {
                return body = StringUtils.EMPTY;
            }

            final String encoding = getContentEncoding(entity);
            final String charset = getContentCharSet(entity);

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
            } finally {
                EntityUtils.consume(entity);
                httpClient.getConnectionManager().shutdown();
            }
        }

        return body;
    }

    /**
     * @param entity The HTTP entity to get the charset
     * @return the charset of a given HTTP entity
     */
    public static String getContentCharSet(HttpEntity entity) {
        if (entity != null) {
            Header type = entity.getContentType();
            if (type != null) {
                for (HeaderElement headerElement : type.getElements()) {
                    if ("charset".equalsIgnoreCase(headerElement.getName())) {
                        return headerElement.getValue();
                    }
                }
            }
        }

        return CHARSET_DEFAULT;
    }

    /**
     * @param entity The HTTP entity to get the encoding
     * @return the encoding of a given HTTP entity or a empty String if unknow
     */
    public static String getContentEncoding(HttpEntity entity) {
        if (entity != null) {
            Header encoding = entity.getContentEncoding();
            if (encoding != null) {
                return encoding.getValue();
            }
        }

        return StringUtils.EMPTY;
    }

    @Override
    public String toString() {
        if (response == null) {
            return "null";
        }

        return response.toString();
    }
}
