/*
 * Copyright 2018 Saxon State and University Library Dresden (SLUB)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.qucosa.dc.disseminator;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

public class DcDissMapper {

    private final StreamSource xslSource;
    private final TransformerFactory transformerFactory;
    private final DocumentBuilder documentBuilder;

    public DcDissMapper() throws ParserConfigurationException {
        this("/mets2dcdata.xsl");
    }

    public DcDissMapper(String xsltStylesheetResourceName) throws ParserConfigurationException {
        documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        transformerFactory = TransformerFactory.newInstance();
        xslSource = new StreamSource(this.getClass().getResourceAsStream(xsltStylesheetResourceName));
    }

    public Document transformDcDiss(Document metsDocument) throws TransformerFactoryConfigurationError, Exception {
        Document newDocument = documentBuilder.newDocument();

        Transformer transformer = transformerFactory.newTransformer(xslSource);
        transformer.transform(new DOMSource(metsDocument), new DOMResult(newDocument));

        return newDocument;
    }

}
