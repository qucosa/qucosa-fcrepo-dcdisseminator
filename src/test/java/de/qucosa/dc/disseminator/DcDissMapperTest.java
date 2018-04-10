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

import static org.junit.Assert.assertThat;
import static org.xmlunit.matchers.HasXPathMatcher.hasXPath;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class DcDissMapperTest {

    private DocumentBuilder documentBuilder;

    private Map<String, String> namespaceContext = new HashMap<>();

    @Before
    public void setup() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilder = documentBuilderFactory.newDocumentBuilder();

        namespaceContext.put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
    }

    @Test
    public void Resulting_document_contains_oaidc_root_element() throws Exception {
        Document input = parseXmlString("<mets:mets xmlns:mets=\"http://www.loc.gov/METS/\"/>");

        Document result = new DcDissMapper().transformDcDiss(input);

        assertThat(result, hasXPath("/oai_dc:dc").withNamespaceContext(namespaceContext));
    }

    private Document parseXmlString(String xml) throws IOException, SAXException {
        return documentBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
    }
}