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
package reconf.infra.xml;

import java.io.*;
import javax.xml.bind.*;


public class Serializer {

    public static <T> String toXml(T arg) throws JAXBException {
        Marshaller marshaller = JAXBContext.newInstance(arg.getClass()).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, System.getProperty("file.encoding"));
        StringWriter writer = new StringWriter();
        marshaller.marshal(arg, writer);
        return writer.toString();
    }

    public static <T> String toXml(T arg, String encoding) throws JAXBException {
        Marshaller marshaller = JAXBContext.newInstance(arg.getClass()).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
        StringWriter writer = new StringWriter();
        marshaller.marshal(arg, writer);
        return writer.toString();
    }

    public static <T> T fromXml(String xml, Class<T> clazz) throws JAXBException {
        Unmarshaller unmarshaller = JAXBContext.newInstance(clazz).createUnmarshaller();
        StringReader reader = new StringReader(xml);

        T entity = (T) unmarshaller.unmarshal(reader);
        return entity;
    }

    public static <T> T fromXml(InputStream is, Class<T> clazz) throws JAXBException {
        Unmarshaller unmarshaller = JAXBContext.newInstance(clazz).createUnmarshaller();
        T entity = (T) unmarshaller.unmarshal(is);
        return entity;
    }
}
