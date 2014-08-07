/*
 *    Copyright 2013-2014 ReConf Team
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
package reconf.servlet;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.lang.*;
import reconf.client.setup.*;

public class SyncServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_URI = "/sync";
    private static final String BODY_TEMPLATE = "{ \"page\" : { \"title\" : \"%s\", \"href\" : \"%s\" }, \"result\" : [ %s ] }";
    private static final String RESULT_TEMPLATE = "{ \"repository\" : \"%s\", \"success\" : \"%s\" }";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
        resp.setContentType(AdminServlet.getContentType());
        PrintWriter writer = resp.getWriter();

        try {
            String path = req.getContextPath() + req.getServletPath();
            List<SyncResult> result = Environment.syncActiveConfigurationRepositoryUpdaters();

            List<String> syncResultMessage = new ArrayList<String>();
            for (SyncResult syncResult : result) {
                if (syncResult == null) {
                    continue;
                }
                syncResultMessage.add(String.format(RESULT_TEMPLATE, syncResult.getName(), syncResult.getThrowable() == null));
            }

            writer.println(String.format(BODY_TEMPLATE, "ReConf Sync Result", path + DEFAULT_URI, StringUtils.join(syncResultMessage, ", ")));

        } finally {
            writer.close();
        }
    }

    public static String getDefaultURI() {
        return DEFAULT_URI;
    }
}
