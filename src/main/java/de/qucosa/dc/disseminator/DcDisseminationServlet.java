package de.qucosa.dc.disseminator;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;

public class DcDisseminationServlet extends HttpServlet {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final long serialVersionUID = 1L;
    private static final String REQUEST_PARAM_METS_URL = "metsurl";

    private ObjectPool<CloseableHttpClient> httpClientPool;
    private ObjectPool<Transformer> transformerPool;

    @Override
    public void init() {
        warnIfDefaultEncodingIsNotUTF8();

        final HttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

        httpClientPool = new GenericObjectPool<>(new BasePooledObjectFactory<CloseableHttpClient>() {
            @Override
            public CloseableHttpClient create() {
                return HttpClientBuilder.create().setConnectionManager(connectionManager).build();
            }

            @Override
            public PooledObject<CloseableHttpClient> wrap(CloseableHttpClient closeableHttpClient) {
                return new DefaultPooledObject<>(closeableHttpClient);
            }

            @Override
            public void destroyObject(PooledObject<CloseableHttpClient> p) throws Exception {
                p.getObject().close();
                super.destroyObject(p);
            }
        });

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
            httpClientPool.clear();
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

            CloseableHttpClient httpClient = httpClientPool.borrowObject();

            try (CloseableHttpResponse resp = httpClient.execute(new HttpGet(metsDocumentUri))) {

                if (SC_OK == resp.getStatusLine().getStatusCode()) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                    Transformer transformer = transformerPool.borrowObject();
                    transformer.transform(
                            new StreamSource(resp.getEntity().getContent()),
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

            httpClientPool.returnObject(httpClient);

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
