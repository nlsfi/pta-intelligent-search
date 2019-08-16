package fi.maanmittauslaitos.pta.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.maanmittauslaitos.pta.search.csw.Harvestable;
import fi.maanmittauslaitos.pta.search.csw.HarvesterInputStream;
import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.index.DocumentSink;
import fi.maanmittauslaitos.pta.search.index.SinkProcessingException;
import fi.maanmittauslaitos.pta.search.metadata.ISOMetadataFields;
import fi.maanmittauslaitos.pta.search.utils.HarvesterTracker;
import fi.maanmittauslaitos.pta.search.utils.HarvesterTrackerImpl;
import org.apache.log4j.Logger;
import org.springframework.boot.CommandLineRunner;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.FileOutputStream;
import java.io.IOException;

import static fi.maanmittauslaitos.pta.search.utils.HarvesterTracker.IdentifierType;
import static fi.maanmittauslaitos.pta.search.utils.HarvesterTracker.RETRY_FOR_HARVESTING_EXCEPTION;

public abstract class AbstractHarvester implements CommandLineRunner {

	protected static final Logger logger = Logger.getLogger(HarvesterTrackerImpl.class);

	abstract protected DocumentSink getDocumentSink(HarvesterConfig config, HarvesterTracker harvesterTracker, String[] args);

	abstract protected DocumentProcessor getDocumentProcessor(HarvesterConfig config) throws ParserConfigurationException, IOException;

	abstract protected HarvesterSource getHarvesterSource(HarvesterConfig config) throws XPathExpressionException, ParserConfigurationException;

	protected HarvesterConfig getConfig() {
		return new HarvesterConfig();
	}


	@Override
	public void run(String... args) throws Exception {
		HarvesterConfig config = getConfig();
		HarvesterTracker tracker = config.getHarvesterTracker();
		HarvesterSource source = getHarvesterSource(config);
		DocumentProcessor processor = getDocumentProcessor(config);
		DocumentSink sink = getDocumentSink(config, tracker, args);

		boolean store = args.length > 0 && args[0].equals("store");

		if (!tracker.getIdentifiers().isEmpty()) {
			logger.warn("Identifiers of the documents, that are permanently skipped from harvesting process:\n" +
					String.join(",\n", tracker.getIdentifiersByType(IdentifierType.PERMANENTLY_SKIPPED)));
			logger.warn("Identifiers of the documents, that caused HarvestingExceptions during last session(s):\n" +
					String.join(",\n", tracker.getIdentifiersByType(IdentifierType.SKIPPED_DUE_HARVESTING_EXCEPTION)));
			logger.warn("Identifiers of the documents, that caused ProcessingExceptions during last session(s):\n" +
					String.join(",\n", tracker.getIdentifiersByType(IdentifierType.SKIPPED_DUE_PROCESSING_EXCEPTION)));
		}

		sink.startIndexing();

		try {

			for (Harvestable harvestable : source) {

				String id = harvestable.getIdentifier();
				if (!tracker.isYetToBeProcessed(id)) {
					continue;
				}

				boolean continueToNext = false;
				int tries = 0;
				while (!continueToNext && tries < RETRY_FOR_HARVESTING_EXCEPTION) {
					try {
						tries++;
						HarvesterInputStream is = source.getInputStream(harvestable);
						if (is == null) {
							logger.warn("Source is null, skipping, or stopping");
							throw new HarvestingException();
						}

						try {
							Document doc = processor.processDocument(is);
							if (store && (tracker.getIdentifiersByType(IdentifierType.INSERTED).size() +
									tracker.getIdentifiersByType(IdentifierType.UPDATED).size()) < 100) {
								writeDocumentToFile(doc);
							}
							DocumentSink.IndexResult result = sink.indexDocument(doc);

							switch (result) {
								case UPDATED:
									tracker.addIdToUpdated(id);
									break;
								case INSERTED:
									tracker.addIdToInserted(id);
									break;
							}
							continueToNext = true;


						} catch (DocumentProcessingException e) {
							logger.error("Processing error for the id " + id, e);
							tracker.addToSkippedDueProcessingException(id);
							continueToNext = true;
						} finally {
							is.close();
						}

					} catch (HarvestingException e) {
						logger.error("Harvesting error for the id " + id +
								", retries: " + tries + "/" + RETRY_FOR_HARVESTING_EXCEPTION, e);
					}
				}

				if (!continueToNext) {
					logger.error("Stopping harvesting due too many HarvestingException");
					tracker.addToSkippedDueHarvestingException(id);
					break;
				}

			}

		} catch (IOException | SinkProcessingException e) {
			tracker.harvestingInterrupted();
			throw e;
		}


		int deleted = sink.stopIndexing();

		logger.info("Inserted " + tracker.getIdentifiersByType(IdentifierType.INSERTED).size() + " documents");
		logger.info("Updated " + tracker.getIdentifiersByType(IdentifierType.UPDATED).size() + " documents");
		logger.info("Deleted " + deleted + " documents");

		tracker.harvestingFinished();
	}

	private void writeDocumentToFile(Document doc) throws IOException {
		String id = doc.getValue(ISOMetadataFields.ID, String.class);
		ObjectMapper objectMapper = new ObjectMapper();
		try (FileOutputStream out = new FileOutputStream("indexed-documents/" + id + ".json")) {
			objectMapper.writeValue(out, doc.getFields());
		}
	}

}
