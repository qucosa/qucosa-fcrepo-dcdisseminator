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
