package de.qucosa.dc.disseminator;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertThat;
import static org.xmlunit.matchers.HasXPathMatcher.hasXPath;

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