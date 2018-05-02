package fi.maanmittauslaitos.pta.search.index;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;

public class ElasticsearchDocumentSink implements DocumentSink {
	private static Logger logger = Logger.getLogger(ElasticsearchDocumentSink.class);
	
	private String protocol = "http";
	private String hostname = "localhost";
	private int port = 9200;
	
	private String index;
	private String type;
	private String idField;
	
	private String defaultResponseCharset = "UTF-8";
	
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	
	public String getProtocol() {
		return protocol;
	}
	
	public void setIndex(String index) {
		this.index = index;
	}
	
	public String getIndex() {
		return index;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
	public void setIdField(String idField) {
		this.idField = idField;
	}
	
	public String getIdField() {
		return idField;
	}
	
	public void setDefaultResponseCharset(String defaultResponseCharset) {
		this.defaultResponseCharset = defaultResponseCharset;
	}
	
	public String getDefaultResponseCharset() {
		return defaultResponseCharset;
	}
	
	private URL getURL(String id) throws MalformedURLException {
		StringBuffer buf = new StringBuffer();
		buf.append(getProtocol());
		buf.append("://");
		buf.append(getHostname());
		buf.append(":"+getPort());
		buf.append("/"+getIndex()+"/"+getType()+"/");
		buf.append(id);
		
		return new URL(buf.toString());
	}
	
	@Override
	public IndexResult indexDocument(Document doc) throws SinkProcessingException {
		// TODO: id-kenttä tulee olla jotenkin konfiguroitavissa
		
		try {
			URL url = getURL(doc.getValue(getIdField(), String.class));
			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			
			httpCon.setRequestProperty("Content-Type", "application/json");
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("PUT");
			
			ObjectMapper objectMapper = new ObjectMapper();
			try (OutputStream out = httpCon.getOutputStream()) {
				objectMapper.writeValue(out, doc.getFields());
			}
			
			int code = httpCon.getResponseCode();
			logger.debug("ElasticSearch document indexing HTTP response = status: "+code+", content-type: "+httpCon.getContentType());
			
			IndexResult ret;
			try (InputStream is = httpCon.getInputStream()) {
				ElasticsearchIndexingResponse foo = objectMapper.readValue(is, ElasticsearchIndexingResponse.class);
				
				if (!"updated".equals(foo.getResult()) && !"created".equals(foo.getResult())) {
					logger.warn("Unknown 'result' in response from ElasticSearch: '"+foo.getResult()+"'. created = "+foo.isCreated());
				}
				
				ret = Boolean.TRUE.equals(foo.isCreated()) ? IndexResult.INSERTED : IndexResult.UPDATED;
				
			}
			return ret;
		} catch(IOException e) {
			throw new SinkProcessingException(e);
		}
	}

}
