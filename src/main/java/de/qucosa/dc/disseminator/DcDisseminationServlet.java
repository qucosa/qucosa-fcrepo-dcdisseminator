package de.qucosa.dc.disseminator;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.IOException;
import java.net.URI;

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

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DcDisseminationServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final Logger log = LoggerFactory.getLogger(this.getClass());
	
    private CloseableHttpClient httpClient;
    
    private Transformer transformer;
    
    private static final String REQUEST_PARAM_METS_URL = "metsurl";
	
    @Override
    public void init() throws ServletException {
        httpClient = HttpClientBuilder.create()
            .setConnectionManager(new PoolingHttpClientConnectionManager())
            .build();
        
        try {
            transformer = TransformerFactory.newInstance()
                .newTransformer(new StreamSource(getClass()
                .getResourceAsStream("/mets2dcdata.xsl")));
        } catch (TransformerConfigurationException tce) {
            log.error("Could not initialize XSLT transformer", tce);
            throw new ServletException(tce);
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
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            }
        } catch (MissingRequiredParameter  | IllegalArgumentException mrp) {
            sendError(response, SC_BAD_REQUEST, mrp.getMessage());
            mrp.printStackTrace();
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
