package fi.maanmittauslaitos.pta.search.wfs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fi.maanmittauslaitos.pta.search.Document;
import fi.maanmittauslaitos.pta.search.HarvesterSource;
import fi.maanmittauslaitos.pta.search.HarvestingException;
import fi.maanmittauslaitos.pta.search.xpath.FieldExtractorConfiguration;
import fi.maanmittauslaitos.pta.search.xpath.XPathExtractionConfiguration;
import fi.maanmittauslaitos.pta.search.xpath.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.xpath.XPathProcessorFactory;
import fi.maanmittauslaitos.pta.search.xpath.FieldExtractorConfiguration.FieldExtractorType;

public class WFS200HarvesterSource extends HarvesterSource {
	private static Logger logger = Logger.getLogger(WFS200HarvesterSource.class);
	
	private String featureType;
	private String sortBy = "gml:id";
	
	public void setFeatureType(String featureType) {
		this.featureType = featureType;
	}
	
	public String getFeatureType() {
		return featureType;
	}
	
	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}
	
	public String getSortBy() {
		return sortBy;
	}
	
	
	@Override
	public Iterator<InputStream> iterator() {
		return new FeatureToInputStreamIterator();
	}
	
	private class FeatureToInputStreamIterator implements Iterator<InputStream> {
		private URL nextURL;
		private LinkedList<Node> currentBatch;

		
		public FeatureToInputStreamIterator() {
			try {
			StringBuffer reqUrl = new StringBuffer(getOnlineResource());
			if (reqUrl.indexOf("?") == -1) {
				reqUrl.append("?");
			} else if (reqUrl.charAt(reqUrl.length()-1) != '&') {
				reqUrl.append("&");
			}
			
			reqUrl.append("SERVICE=WFS&REQUEST=GetFeature&VERSION=2.0.0");
			
			reqUrl.append("&TYPENAMES="+URLEncoder.encode(getFeatureType(), "UTF-8"));
			reqUrl.append("&COUNT="+getBatchSize());
			reqUrl.append("&STARTINDEX=1&SORTBY="+URLEncoder.encode(getSortBy(), "UTF-8"));
			
			nextURL = new URL(reqUrl.toString());
			} catch(IOException e) {
				throw new HarvestingException(e);
			}
			
			getNextBatch();
		}

		private void getNextBatch() {
			try {
				logger.trace("WFS GetFeature URL: "+nextURL);
				
				try (InputStream is = nextURL.openStream()) {
					XPathExtractionConfiguration configuration = new XPathExtractionConfiguration();
					configuration.getNamespaces().put("wfs", "http://www.opengis.net/wfs/2.0");

					FieldExtractorConfiguration next = new FieldExtractorConfiguration();
					next.setField("next");
					next.setType(FieldExtractorType.FIRST_MATCHING_VALUE);
					next.setXpath("/wfs:FeatureCollection/@next");
					configuration.getFieldExtractors().add(next);

					XPathProcessorFactory xppf = new XPathProcessorFactory();
					DocumentProcessor processor = xppf.createProcessor(configuration);
					Document doc = processor.processDocument(is);

					
					logger.debug("\nnext = " + doc.getFields().get("next"));

					String urlStr = doc.getValue("next", String.class);
					
					if (urlStr == null) {
						nextURL = null;
					} else {
						nextURL = new URL(urlStr);
					}
					
					NodeList list = doc.getDom().getElementsByTagNameNS("http://www.opengis.net/wfs/2.0", "member");
					
					currentBatch = new LinkedList<>();
					for (int i = 0; i < list.getLength(); i++) {
						currentBatch.add(list.item(i));
					}
				}
				
				
			} catch(IOException | ParserConfigurationException | SAXException | XPathException e) {
				throw new HarvestingException(e);
			}
		}
		
		@Override
		public boolean hasNext() {
			return currentBatch.size() > 0 || nextURL != null;
		}

		@Override
		public InputStream next() {
			if (currentBatch.size() == 0) {
				getNextBatch();
			}

			if (currentBatch.size() == 0) {
				return null;
			}

			Node node = currentBatch.removeFirst();

			return readRecord(node);
		}

		private InputStream readRecord(Node node) {
			try {
				StringWriter writer = new StringWriter();
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.transform(new DOMSource(node), new StreamResult(writer));
				String xml = writer.toString();
				return new ByteArrayInputStream(xml.getBytes("UTF-8"));
			} catch(TransformerException | IOException e) {
				throw new HarvestingException(e);
			}
		}
	
	}

}
