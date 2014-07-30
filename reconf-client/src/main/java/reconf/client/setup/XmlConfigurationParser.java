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
package reconf.client.setup;

import java.io.*;
import java.util.concurrent.*;
import javax.xml.parsers.*;
import org.apache.commons.io.*;
import org.apache.commons.lang.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import reconf.infra.log.*;
import reconf.infra.system.*;
import reconf.infra.throwables.*;


public class XmlConfigurationParser extends DefaultHandler {

    public static XmlConfigurationParser from(String xmlContent) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XmlConfigurationParser reader = new XmlConfigurationParser();
            parser.parse(IOUtils.toInputStream(xmlContent), reader);

            if (!reader.begin) {
                LoggerHolder.getLog().error("error parsing the configuration file. check if the file is valid");
            }

            return reader;

        } catch (Exception e) {
            throw new ReConfInitializationError("error parsing the configuration file with content" + LineSeparator.value() + xmlContent, e);
        }
    }

    private String temp;
    private String tag;

    private String locale;
    private boolean experimentalFeatures;
    private LocalCacheSettings localCacheSettings;
    private boolean openLocalCacheSettings;
    private boolean begin = false;

    private ConnectionSettings connectionSettings;
    private boolean openConnectionSettings;

    private GlobalPollingFrequencySettings annotationOverride;
    private boolean openAnnotationOverride;
    private boolean debugEnabled;

    public void characters(char[] buffer, int start, int length) {
        if (!begin) {
            return;
        }
        temp = new String(buffer, start, length);
        if (openLocalCacheSettings) {
            buildLocalCacheSettings();
        }
        if (openConnectionSettings) {
            buildConnectionSettings();
        }
        if (openAnnotationOverride) {
            buildAnnotationOverride();
        }
    }

    private void buildLocalCacheSettings() {
        if (StringUtils.equalsIgnoreCase("location", tag)) {
            File file = null;
            try {
                file = new File(temp);
            } catch (Exception ignored) {}
            localCacheSettings.setBackupLocation(file);
        }
        if (StringUtils.equalsIgnoreCase("max-log-file-size-mb", tag)) {
            localCacheSettings.setMaxLogFileSize(tempAsInteger());
        }
        if (StringUtils.equalsIgnoreCase("compressed", tag)) {
            try {
                localCacheSettings.setCompressed(Boolean.valueOf(temp));
            } catch (Exception ignored) {
                localCacheSettings.setCompressed(false);
            }
        }
    }

    private void buildConnectionSettings() {
        if (StringUtils.equalsIgnoreCase("url", tag)) {
            connectionSettings.setUrl(temp);
        }
        if (StringUtils.equalsIgnoreCase("timeout", tag)) {
            connectionSettings.setTimeout(tempAsInteger());
        }
        if (StringUtils.equalsIgnoreCase("time-unit", tag)) {
            connectionSettings.setTimeUnit(tempAsTimeUnit());
        }
        if (StringUtils.equalsIgnoreCase("max-retry", tag)) {
            connectionSettings.setMaxRetry(tempAsInteger());
        }
    }

    private void buildAnnotationOverride() {
        if (StringUtils.equalsIgnoreCase("interval", tag)) {
            annotationOverride.setInterval(tempAsInteger());
        }
        if (StringUtils.equalsIgnoreCase("time-unit", tag)) {
            annotationOverride.setTimeUnit(tempAsTimeUnit());
        }
    }

    private Integer tempAsInteger() {
        Integer integer = null;
        try {
            integer = Integer.valueOf(temp);
        } catch (Exception ignored) {}
        return integer;
    }

    private TimeUnit tempAsTimeUnit() {
        TimeUnit timeUnit = null;
        try {
            timeUnit = TimeUnit.valueOf(temp);
        } catch (Exception ignored) {}
        return timeUnit;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tag = qName;
        if (!begin && StringUtils.equalsIgnoreCase("configuration", qName)) {
            begin = true;
        }
        if (StringUtils.equalsIgnoreCase("local-cache", qName)) {
            localCacheSettings = new LocalCacheSettings();
            openLocalCacheSettings = true;
        }
        if (StringUtils.equalsIgnoreCase("server", qName)) {
            connectionSettings = new ConnectionSettings();
            openConnectionSettings = true;
        }
        if (StringUtils.equalsIgnoreCase("global-polling-frequency", qName)) {
            annotationOverride = new GlobalPollingFrequencySettings();
            openAnnotationOverride = true;
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (!begin) {
            return;
        }
        tag = StringUtils.EMPTY;
        if (StringUtils.equalsIgnoreCase("locale", qName)) {
            locale = temp;
        }
        if (StringUtils.equalsIgnoreCase("enable-debug", qName)) {
            debugEnabled = true;
        }
        if (StringUtils.equalsIgnoreCase("enable-experimental-features", qName)) {
            experimentalFeatures = true;
        }
        if (StringUtils.equalsIgnoreCase("local-cache", qName)) {
            openLocalCacheSettings = false;
        }
        if (StringUtils.equalsIgnoreCase("server", qName)) {
            openConnectionSettings = false;
        }
        if (StringUtils.equalsIgnoreCase("global-polling-frequency", qName)) {
            openAnnotationOverride = false;
        }
    }

    public LocalCacheSettings getLocalCacheSettings() {
        return localCacheSettings;
    }

    public ConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }

    public GlobalPollingFrequencySettings getAnnotationOverride() {
        return annotationOverride;
    }

    public String getLocale() {
        return locale;
    }

    public boolean isExperimentalFeatures() {
        return experimentalFeatures;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }
}
