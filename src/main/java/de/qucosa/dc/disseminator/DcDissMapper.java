package de.qucosa.dc.disseminator;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.HashMap;

public class DcDissMapper {
    private Document metsDoc = null;

    private StreamSource xslSource = null;

    public DcDissMapper(Document metsDoc, StreamSource xslSource) {
        this.metsDoc = metsDoc;
        this.xslSource = xslSource;
    }

    public Document transformDcDiss() throws TransformerFactoryConfigurationError, Exception, XPathExpressionException {
        Transformer transformer = null;
        StringWriter stringWriter = new StringWriter();
        StreamResult streamResult = new StreamResult(stringWriter);
        Document xmetadiss = null;
        transformer = TransformerFactory.newInstance().newTransformer(xslSource);
        transformer.transform(new DOMSource(metsDoc), streamResult);

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
        xmetadiss = documentBuilder.parse(new ByteArrayInputStream(stringWriter.toString().getBytes("UTF-8")));

        return xmetadiss;
    }

    public String pid() throws XPathExpressionException {
        return extractPid();
    }

    public String lastModeDate() throws XPathExpressionException {
        return extractLastModDate();
    }

    private String extractPid() throws XPathExpressionException {
        String pid = null;
        XPath xPath = xpath();
        pid = (String) xPath.compile("//mets:mets/@OBJID").evaluate(metsDoc, XPathConstants.STRING);
        return pid;
    }

    private String extractLastModDate() throws XPathExpressionException {
        String date = null;
        XPath xPath = xpath();
        date = (String) xPath.compile("//mets:mets/mets:metsHdr/@LASTMODDATE").evaluate(metsDoc, XPathConstants.STRING);
        return date;
    }

    @SuppressWarnings("serial")
    private XPath xpath() {
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new SimpleNamespaceContext(new HashMap<String, String>() {
            {
                put("mets", "http://www.loc.gov/METS/");
            }
        }));
        return xPath;
    }
}
