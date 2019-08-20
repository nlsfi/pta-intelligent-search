package fi.maanmittauslaitos.pta.search.csw;

import fi.maanmittauslaitos.pta.search.HarvesterSource;
import fi.maanmittauslaitos.pta.search.HarvestingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.*;
import fi.maanmittauslaitos.pta.search.documentprocessor.XPathFieldExtractorConfiguration.FieldExtractorType;
import org.apache.log4j.Logger;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CSWHarvesterSource extends HarvesterSource {
	private static Logger logger = Logger.getLogger(CSWHarvesterSource.class);

	@Override
	public Iterator<Harvestable> iterator() {
		return new CSWIterator();
	}

	@Override
	public HarvesterInputStream getInputStream(Harvestable harvestable) {
		return readRecord(harvestable.getIdentifier());
	}


	private HarvesterInputStream readRecord(String id) {
		logger.debug("Requesting record with id" + id);

		StringBuilder reqUrl = new StringBuilder(getOnlineResource());
		if (reqUrl.indexOf("?") == -1) {
			reqUrl.append("?");
		} else if (reqUrl.charAt(reqUrl.length() - 1) != '&') {
			reqUrl.append("&");
		}

		reqUrl.append("SERVICE=CSW&REQUEST=GetRecordById&VERSION=2.0.2&outputSchema=http://www.isotc211.org/2005/gmd&elementSetName=full");

		try {
			reqUrl.append("&id=" + URLEncoder.encode(id, "UTF-8"));

			logger.trace("CSW GetRecordById URL: " + reqUrl);

			URL url = new URL(reqUrl.toString());

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new HarvestingException();
			}

			return HarvesterInputStream.wrap(url.openStream());

		} catch (IOException e) {
			throw new HarvestingException(e);
		}
	}

	private class CSWIterator implements Iterator<Harvestable> {
		private int numberOfRecordsProcessed = 0;
		private Integer numberOfRecordsInService;
		private LinkedList<String> idsInBatch;

		CSWIterator() {
			idsInBatch = new LinkedList<>();
			getNextBatch();
		}


		@Override
		public boolean hasNext() {
			return numberOfRecordsProcessed < numberOfRecordsInService;
		}

		@Override
		public Harvestable next() {
			if (idsInBatch.size() == 0) {
				getNextBatch();
				// BUG: sometimes the next batch of brief records contain no id's and therefore idsInBatch.size() == 0 even after getNextBatch()
				// Note that if we skip the records with no identifier, we must take that into account in the numberOfRecordsProcessed (or numberOfRecordsInService)
			}

			if (idsInBatch.size() == 0) {
				return null;
			}

			String id = idsInBatch.removeFirst();
			numberOfRecordsProcessed++;

			return CSWHarvestable.create(id);
		}


		private void getNextBatch() {
			int startPosition = 1 + numberOfRecordsProcessed;
			int maxRecords = getBatchSize();
			logger.debug("Requesting records startPosition = " + startPosition + ",maxRecords = " + maxRecords);

			try {

				StringBuilder reqUrl = new StringBuilder(getOnlineResource());
				if (reqUrl.indexOf("?") == -1) {
					reqUrl.append("?");
				} else if (reqUrl.charAt(reqUrl.length() - 1) != '&') {
					reqUrl.append("&");
				}

				reqUrl.append("SERVICE=CSW&REQUEST=GetRecords&VERSION=2.0.2&typeNames=gmd%3AMD_Metadata&resultType=results&elementSetName=brief");

				reqUrl.append("&startPosition=" + startPosition + "&maxRecords=" + maxRecords);

				logger.trace("CSW GetRecords URL: " + reqUrl);

				URL url = new URL(reqUrl.toString());

				try (InputStream is = url.openStream()) {
					DocumentProcessingConfiguration configuration = new DocumentProcessingConfiguration();
					configuration.getNamespaces().put("dc", "http://purl.org/dc/elements/1.1/");
					configuration.getNamespaces().put("csw", "http://www.opengis.net/cat/csw/2.0.2");

					XPathFieldExtractorConfiguration numberOfRecordsMatched = new XPathFieldExtractorConfiguration();
					numberOfRecordsMatched.setField("numberOfRecordsMatched");
					numberOfRecordsMatched.setType(FieldExtractorType.FIRST_MATCHING_VALUE);
					numberOfRecordsMatched.setXpath("//csw:SearchResults/@numberOfRecordsMatched");
					configuration.getFieldExtractors().add(numberOfRecordsMatched);

					XPathFieldExtractorConfiguration ids = new XPathFieldExtractorConfiguration();
					ids.setField("ids");
					ids.setType(FieldExtractorType.ALL_MATCHING_VALUES);
					ids.setXpath("//dc:identifier/text()");
					configuration.getFieldExtractors().add(ids);

					DocumentProcessorFactory xppf = new DocumentProcessorFactory();
					DocumentProcessor processor = xppf.createProcessor(configuration);
					Document doc = processor.processDocument(is);

					logger.debug("\tReceived ids: " + doc.getFields().get("ids"));
					logger.debug("\tnumberOfRecordsMatched = " + doc.getFields().get("numberOfRecordsMatched"));

					idsInBatch.addAll(doc.getListValue("ids", String.class));

					List<String> nRecords = doc.getListValue("numberOfRecordsMatched", String.class);
					if (nRecords.size() > 0) {
						numberOfRecordsInService = Integer.parseInt(doc.getValue("numberOfRecordsMatched", String.class));
					}

					if (numberOfRecordsInService == null) {
						throw new IOException("Unable to determine how many records in CSW service");
					}
				}


			} catch (IOException | ParserConfigurationException | DocumentProcessingException e) {
				throw new HarvestingException(e);
			}
		}
	}
}
