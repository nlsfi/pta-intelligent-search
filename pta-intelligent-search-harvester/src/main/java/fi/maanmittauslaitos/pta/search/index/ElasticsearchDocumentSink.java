package fi.maanmittauslaitos.pta.search.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ConcurrentHashMultiset;
import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.index.ElasticsearchSearchIdsResponse.Hit;
import fi.maanmittauslaitos.pta.search.utils.HarvesterTracker;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * This sink indexes an entire source and finally reomves all documents from ES that were not part of the inde
 * @author v2
 *
 */
public class ElasticsearchDocumentSink implements DocumentSink {
	private static Logger logger = Logger.getLogger(ElasticsearchDocumentSink.class);
	
	private String protocol = "http";
	private String hostname = "localhost";
	private int port = 9200;
	
	private String index;
	private String type;
	private String idField;
	
	private String defaultResponseCharset = "UTF-8";
	
	// Set in startIndexing()
	private Set<String> idsInESBeforeIndexing;
	private ConcurrentHashMultiset<String> idsIndexed;
	private HarvesterTracker tracker;


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
	
	private URL getSearchIdURL(int size, Integer from) throws MalformedURLException {
		// http://localhost:9200/pta/metadata/_search?stored_fields=&pretty=true
		// http://localhost:9200/pta/metadata/_search?stored_fields=&sort=_id&size=100
		// http://localhost:9200/pta/metadata/_search?stored_fields=&sort=_id&size=100&from=100
		
		StringBuffer buf = new StringBuffer();
		buf.append(getProtocol());
		buf.append("://");
		buf.append(getHostname());
		buf.append(":"+getPort());
		buf.append("/"+getIndex()+"/"+getType()+"/_search?stored_fields=&sort=_id&size="+size);
		if (from != null) {
			buf.append("&from="+from );
		}
		
		return new URL(buf.toString());
	}
	
	@Override
	public void startIndexing() throws SinkProcessingException {
		// Retrieve list of all IDs in ES
		
		final int pageSize = 100;
		final ObjectMapper objectMapper = new ObjectMapper();

		final Set<String> existingIds = new HashSet<>();
		logger.info("Identifying metadata in catalogue");
		try {
			Integer from = null;
			boolean done = false;
			
			while (!done) {
				logger.debug("Requesting list of IDs with pageSize "+pageSize+(from == null ? "" : " (starting from entry "+from+")"));
				
				URL url = getSearchIdURL(pageSize, from);
				HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();

				int code = httpCon.getResponseCode();
				if (code != 200) {
					throw new SinkProcessingException(new Exception("ES returned HTTP "+code));
				}
				
				try (InputStream is = httpCon.getInputStream()) {
					ElasticsearchSearchIdsResponse foo = objectMapper.readValue(is, ElasticsearchSearchIdsResponse.class);
					
					for (Hit hit : foo.getHits().getHits()) {
						existingIds.add(hit.getId());
					}
					
					if (foo.getHits().getHits().size() < pageSize) {
						done = true;
					} else {
						if (from == null) {
							from = pageSize;
						} else {
							from += pageSize;
						}
					}
					
				}
				
			}
		} catch(IOException ie) {
			throw new SinkProcessingException(ie);
		}

		logger.info("Previous catalogue has " + existingIds.size() + " records");


		idsInESBeforeIndexing = existingIds;

		idsIndexed = ConcurrentHashMultiset.create();
	}
	
	@Override
	public int stopIndexing() throws SinkProcessingException {
		// Remove all IDs that were not indexed during the indexing process
		Set<String> idsToRemove = new HashSet<>(idsInESBeforeIndexing);
		Set<String> idsProcessedDuringLastSession = tracker.getIdentifiers();
		idsToRemove.removeAll(idsIndexed);
		idsToRemove.removeAll(idsProcessedDuringLastSession);
		
		logger.info("Removing "+idsToRemove.size()+" vanished entries from catalgoue");
		
		int ret = 0;
		try {
			for (String id : idsToRemove) {
				if (deleteRecord(id)) {
					ret++;
				}
			}
		} catch(IOException ie) {
			throw new SinkProcessingException(ie);
		}
		return ret;
	}

	public boolean deleteRecord(String id) throws MalformedURLException, IOException, ProtocolException {
		boolean ret = true;
		URL deleteURL = getURL(id);
		
		HttpURLConnection httpCon = (HttpURLConnection) deleteURL.openConnection();
		
		httpCon.setRequestMethod("DELETE");
		int code = httpCon.getResponseCode();
		if (code != 200) {
			logger.warn("Problem deleting removed id "+id);
			ret = false;
		}
		httpCon.disconnect();
		return ret;
	}
	
	@Override
	public IndexResult indexDocument(Document doc) throws SinkProcessingException {
		try {
			String id = doc.getValue(getIdField(), String.class);
			
			URL url = getURL(id);
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

				Boolean created = foo.isCreated();

				ret = Boolean.TRUE.equals(foo.isCreated()) || "created".equals(foo.getResult()) ? IndexResult.INSERTED : IndexResult.UPDATED;
				
				if (ret == IndexResult.INSERTED || ret == IndexResult.UPDATED) {
					idsIndexed.add(id);
				}
				
			}
			return ret;
		} catch(IOException e) {
			throw new SinkProcessingException(e);
		}
	}

	public void setTracker(HarvesterTracker tracker) {
		this.tracker = tracker;
	}
}
