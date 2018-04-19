package de.qucosa.dc.disseminator;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.servlet.ServletConfig;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;

public class DcDisseminationServlet extends HttpServlet {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final long serialVersionUID = 1L;
    private static final String REQUEST_PARAM_METS_URL = "metsurl";
    private static final String PARAM_FRONTPAGE_URL_PATTERN = "frontpage.url.pattern";
    private static final String PARAM_TRANSFER_URL_PATTERN = "transfer.url.pattern";
    private static final String PARAM_TRANSFER_URL_PIDENCODE = "transfer.url.pidencode";
    private static final String PARAM_AGENT_NAME_SUBSTITUTIONS = "agent.substitutions";

    private CloseableHttpClient httpClient;
    private ObjectPool<Transformer> transformerPool;

    private String transferUrlPattern;
    private Map<String, String> agentNameSubstitutions;
    private boolean transferUrlPidencode = false;

    private XPathExpression XPATH_AGENT;
    private String frontpageUrlPattern;

    @Override
    public void init() {
        warnIfDefaultEncodingIsNotUTF8();

        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new SimpleNamespaceContext(new HashMap<String, String>() {{
            put("mets", "http://www.loc.gov/METS/");
        }}));

        try {
            XPATH_AGENT = xPath.compile("//mets:agent[@ROLE='EDITOR' and @TYPE='ORGANIZATION']/mets:name[1]");
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }

        httpClient = HttpClientBuilder.create()
                .setConnectionManager(new PoolingHttpClientConnectionManager())
                .build();

        ServletConfig servletConfig = getServletConfig();

        frontpageUrlPattern = getParameterValue(servletConfig, PARAM_FRONTPAGE_URL_PATTERN,
                System.getProperty(PARAM_FRONTPAGE_URL_PATTERN, ""));

        transferUrlPattern = getParameterValue(servletConfig, PARAM_TRANSFER_URL_PATTERN,
                System.getProperty(PARAM_TRANSFER_URL_PATTERN, ""));

        transferUrlPidencode = Boolean.valueOf(
                getParameterValue(servletConfig, PARAM_TRANSFER_URL_PIDENCODE,
                        System.getProperty(PARAM_TRANSFER_URL_PIDENCODE, "false")));

        agentNameSubstitutions = decodeSubstitutions(
                getParameterValue(servletConfig, PARAM_AGENT_NAME_SUBSTITUTIONS,
                        System.getProperty(PARAM_AGENT_NAME_SUBSTITUTIONS, "")));

        transformerPool = new GenericObjectPool<>(new BasePooledObjectFactory<Transformer>() {
            @Override
            public Transformer create() throws Exception {
                StreamSource source = new StreamSource(getClass().getResourceAsStream("/mets2dcdata.xsl"));
                return TransformerFactory.newInstance().newTransformer(source);
            }

            @Override
            public PooledObject<Transformer> wrap(Transformer transformer) {
                return new DefaultPooledObject<>(transformer);
            }
        });
    }

    private void warnIfDefaultEncodingIsNotUTF8() {
        Charset defaultCharset = Charset.defaultCharset();
        try {
            Charset expectedCharset = Charset.forName("UTF-8");
            if (!defaultCharset.equals(expectedCharset)) {
                log.warn(String.format("'%s' is not default encoding. Used encoding is '%s'",
                        expectedCharset.name(),
                        defaultCharset.name()));
            }
        } catch (UnsupportedCharsetException e) {
            log.warn(String.format("'%s' encoding is not available. Used encoding is '%s'",
                    e.getCharsetName(),
                    defaultCharset.name()));
        }
    }

    @Override
    public void destroy() {
        try {
            httpClient.close();
        } catch (Exception e) {
            log.warn("Problem clearing HTTP client pool: " + e.getMessage());
        }
        try {
            transformerPool.clear();
        } catch (Exception e) {
            log.warn("Problem clearing XML transformer pool: " + e.getMessage());
        }
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            URI metsDocumentUri = URI.create(getRequiredRequestParameterValue(request, REQUEST_PARAM_METS_URL));

            try (CloseableHttpResponse resp = httpClient.execute(new HttpGet(metsDocumentUri))) {
                if (SC_OK == resp.getStatusLine().getStatusCode()) {

                    InputStream metsDocumentInputStream = resp.getEntity().getContent();

                    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    documentBuilderFactory.setNamespaceAware(true);
                    Document metsDocument = documentBuilderFactory.newDocumentBuilder().parse(metsDocumentInputStream);

                    String agentName = extractAgentName(metsDocument);
                    String pid = extractObjectPID(metsDocument, transferUrlPidencode);

                    Transformer transformer = transformerPool.borrowObject();

                    transformer.setParameter("frontpage_url_pattern", frontpageUrlPattern);
                    transformer.setParameter("transfer_url_pattern", transferUrlPattern);
                    transformer.setParameter("agent", agentName);
                    transformer.setParameter("qpid", pid);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    transformer.transform(
                            new DOMSource(metsDocument),
                            new StreamResult(byteArrayOutputStream)
                    );
                    transformerPool.returnObject(transformer);

                    response.setStatus(SC_OK);
                    response.setContentType("application/xml");
                    byteArrayOutputStream.writeTo(response.getOutputStream());
                }

            } catch (TransformerException e) {
                log.error("Error transforming METS/MODS: " + e.getMessage());
                sendError(response, SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }

        } catch (Throwable anythingElse) {
            log.warn("Internal server error", anythingElse);
            sendError(response, SC_INTERNAL_SERVER_ERROR, anythingElse.getMessage());
        }

    }

    private void sendError(HttpServletResponse resp, int status, String msg) throws IOException {
        resp.setStatus(status);
        resp.setContentType("text/plain");
        resp.setContentLength(msg.getBytes().length);
        resp.getWriter().print(msg);
    }

    private String getRequiredRequestParameterValue(ServletRequest request, String param)
            throws MissingRequiredParameter {
        final String value = request.getParameter(param);

        if (value == null || value.isEmpty()) {
            throw new MissingRequiredParameter("Missing parameter '" + REQUEST_PARAM_METS_URL + "'");
        }

        return value;
    }

    private String extractAgentName(Document metsDocument) throws XPathExpressionException {
        String agentNameElement = XPATH_AGENT.evaluate(metsDocument);
        if (agentNameElement != null) {
            String agentName = agentNameElement.trim();
            if (agentNameSubstitutions.containsKey(agentName)) {
                return agentNameSubstitutions.get(agentName);
            } else {
                return agentName;
            }
        }
        return null;
    }

    private String extractObjectPID(Document metsDocument, boolean encodePid) {
        String pid = metsDocument.getDocumentElement().getAttribute("OBJID");
        if (pid != null && !pid.isEmpty() && encodePid) {
            try {
                pid = URLEncoder.encode(pid, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // UTF-8 is always supported, unless the JVM runtime changes this
                throw new RuntimeException(e);
            }
        }
        return pid;
    }

    private Map<String, String> decodeSubstitutions(String parameterValue) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        if (parameterValue != null && !parameterValue.isEmpty()) {
            for (String substitution : parameterValue.split(";")) {
                String[] s = substitution.split("=");
                result.put(s[0].trim(), s[1].trim());
            }
        }
        return result;
    }

    private String getParameterValue(ServletConfig config, String name, String defaultValue) {
        String v = config.getServletContext().getInitParameter(name);
        return v == null ? defaultValue : v;
    }
}
