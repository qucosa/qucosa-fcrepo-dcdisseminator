package de.qucosa.dc.disseminator;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;

public class DcDisseminationServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String REQUEST_PARAM_METS_URL = "metsurl";
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private CloseableHttpClient httpClient;
    private Transformer transformer;

    @Override
    public void init() throws ServletException {
        httpClient = HttpClientBuilder.create()
                .setConnectionManager(new PoolingHttpClientConnectionManager())
                .build();

        try {
            warnIfDefaultEncodingIsNotUTF8();
            transformer = TransformerFactory.newInstance()
                    .newTransformer(new StreamSource(getClass()
                            .getResourceAsStream("/mets2dcdata.xsl")));
        } catch (TransformerConfigurationException tce) {
            log.error("Could not initialize XSLT transformer", tce);
            throw new ServletException(tce);
        }
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
        } catch (IOException e) {
            log.warn("Problem closing HTTP client: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            URI metsDocumentUri = URI.create(getRequiredRequestParameterValue(request, REQUEST_PARAM_METS_URL));

            try (CloseableHttpResponse resp = httpClient.execute(new HttpGet(metsDocumentUri))) {

                if (SC_OK == resp.getStatusLine().getStatusCode()) {
                    transformer.transform(
                            new StreamSource(resp.getEntity().getContent()),
                            new StreamResult(response.getOutputStream())
                    );
                    response.setStatus(SC_OK);
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
}
