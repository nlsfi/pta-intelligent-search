package fi.maanmittauslaitos.pta.search.csw;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathException;

import org.apache.log4j.Logger;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.client.AbstractClientFactory;
import org.geotoolkit.csw.CSWClientFactory;
import org.geotoolkit.csw.CatalogServicesClient;
import org.geotoolkit.csw.GetRecordByIdRequest;
import org.geotoolkit.csw.GetRecordsRequest;
import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.csw.xml.ResultType;
import org.opengis.parameter.ParameterValueGroup;
import org.xml.sax.SAXException;

import fi.maanmittauslaitos.pta.search.Document;
import fi.maanmittauslaitos.pta.search.xpath.FieldExtractorConfiguration;
import fi.maanmittauslaitos.pta.search.xpath.XPathExtractionConfiguration;
import fi.maanmittauslaitos.pta.search.xpath.XPathProcessor;
import fi.maanmittauslaitos.pta.search.xpath.XPathProcessorFactory;
import fi.maanmittauslaitos.pta.search.xpath.FieldExtractorConfiguration.FieldExtractorType;

public class CSWSource implements Iterable<InputStream> {
	private static Logger logger = Logger.getLogger(CSWSource.class);
	
	private String onlineResource;
	private int batchSize = 1024;
	
	public String getOnlineResource() {
		return onlineResource;
	}
	
	public void setOnlineResource(String onlineResource) {
		this.onlineResource = onlineResource;
	}
	
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	
	public int getBatchSize() {
		return batchSize;
	}
	
	@Override
	public Iterator<InputStream> iterator() {
		return new CSWIterator();
	}
	
	private class CSWIterator implements Iterator<InputStream> {
		private int numberOfRecordsProcessed = 0;
		private int numberOfRecordsInService;
		private LinkedList<String> idsInBatch = null;
		private boolean failed = false;
		
		private CatalogServicesClient catalogServicesClient; 
		
		public CSWIterator() {
			CSWClientFactory cswClientFactory = new CSWClientFactory();
			
			ParameterValueGroup clientFactoryParams = CSWClientFactory.PARAMETERS.createValue();

			try {
				clientFactoryParams.parameter(AbstractClientFactory.URL.getName().getCode()).setValue(new URL(getOnlineResource()));


				catalogServicesClient = (CatalogServicesClient) cswClientFactory.open(clientFactoryParams);
			
			} catch(DataStoreException | MalformedURLException ex) {
				throw new CSWProcessingException(ex);
			}
			
			idsInBatch = new LinkedList<>();
			getNextBatch();
		}
		
		
		
		
		@Override
		public boolean hasNext() {
			if (failed) {
				return false;
			}
			return numberOfRecordsProcessed < numberOfRecordsInService; 
		}
		
		@Override
		public InputStream next() {
			if (idsInBatch.size() == 0) {
				getNextBatch();
			}
			
			
			if (idsInBatch.size() == 0) {
				return null;
			}
			
			
			String id = idsInBatch.removeFirst();
			numberOfRecordsProcessed++;
			
			return readRecord(id);
		}


		private InputStream readRecord(String id) {
			logger.debug("Requesting record with id"+id);
			
			GetRecordByIdRequest req = catalogServicesClient.createGetRecordById();
			
			req.setIds(id);
			req.setElementSetName(ElementSetType.FULL);
			//req.setOutputFormat(arg0);
			req.setOutputSchema("http://www.isotc211.org/2005/gmd");
			
			try {
				return req.getResponseStream();
			} catch(IOException e) {
				throw new CSWProcessingException(e);
			}
		}




		private void getNextBatch() {
			logger.debug("Requesting records starting at position "+(1+numberOfRecordsProcessed)+", batch size is "+getBatchSize());
			
			GetRecordsRequest req = catalogServicesClient.createGetRecords();
			
			req.setMaxRecords(getBatchSize());
			req.setStartPosition(1+numberOfRecordsProcessed);
			req.setTypeNames("gmd:MD_Metadata");
			req.setNamespace("gmd:http://www.isotc211.org/2005/gmd");
			req.setConstraintLanguage("Filter");
			req.setConstraintLanguageVersion("1.1.0");
			req.setResultType(ResultType.RESULTS);
			
			try (InputStream is = req.getResponseStream()) {
				XPathExtractionConfiguration configuration = new XPathExtractionConfiguration();
				configuration.getNamespaces().put("dc", "http://purl.org/dc/elements/1.1/");
				configuration.getNamespaces().put("csw", "http://www.opengis.net/cat/csw/2.0.2");
				
				FieldExtractorConfiguration numberOfRecordsMatched = new FieldExtractorConfiguration();
				numberOfRecordsMatched.setField("numberOfRecordsMatched");
				numberOfRecordsMatched.setType(FieldExtractorType.FIRST_MATCHING_VALUE);
				numberOfRecordsMatched.setXpath("//csw:SearchResults/@numberOfRecordsMatched");
				configuration.getFieldExtractors().add(numberOfRecordsMatched);
				
				FieldExtractorConfiguration ids = new FieldExtractorConfiguration();
				ids.setField("ids");
				ids.setType(FieldExtractorType.ALL_MATCHING_VALUES);
				ids.setXpath("//dc:identifier/text()");
				configuration.getFieldExtractors().add(ids);
				
				XPathProcessorFactory xppf = new XPathProcessorFactory();
				XPathProcessor processor = xppf.createProcessor(configuration);
				Document doc = processor.processDocument(is);
				
				logger.debug("\tReceived ids: "+doc.getFields().get("ids"));
				logger.debug("\tnumberOfRecordsMatched = "+doc.getFields().get("numberOfRecordsMatched"));
				
				idsInBatch.addAll(doc.getFields().get("ids"));
				numberOfRecordsInService = Integer.parseInt(doc.getFields().get("numberOfRecordsMatched").get(0));
			} catch(IOException | ParserConfigurationException | XPathException | SAXException e) {
				failed = true;
				throw new CSWProcessingException(e);
			}
			
		}
	}
}
