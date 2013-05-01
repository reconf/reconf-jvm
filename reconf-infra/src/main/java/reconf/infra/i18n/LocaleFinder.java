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
package reconf.infra.i18n;

import javax.xml.parsers.*;
import org.apache.commons.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import reconf.infra.throwables.*;


public class LocaleFinder extends DefaultHandler {

    public static String find(String xmlContent) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            LocaleFinder finder = new LocaleFinder();
            parser.parse(IOUtils.toInputStream(xmlContent), finder);

            return finder.getLocale();
        } catch (Exception e) {
            throw new ReConfInitializationError(e);
        }
    }

    private String temp;
    private String locale;

    public void characters(char[] buffer, int start, int length) {
        temp = new String(buffer, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("locale")) {
            locale = temp;
        }
    }

    public String getLocale() {
        return locale;
    }
}
