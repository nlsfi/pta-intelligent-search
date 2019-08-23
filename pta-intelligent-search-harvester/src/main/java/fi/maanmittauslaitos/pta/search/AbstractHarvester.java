package fi.maanmittauslaitos.pta.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.pivovarit.collectors.ParallelCollectors;
import fi.maanmittauslaitos.pta.search.csw.Harvestable;
import fi.maanmittauslaitos.pta.search.csw.HarvesterInputStream;
import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.index.DocumentSink;
import fi.maanmittauslaitos.pta.search.index.SinkProcessingException;
import fi.maanmittauslaitos.pta.search.metadata.ISOMetadataFields;
import fi.maanmittauslaitos.pta.search.utils.HarvesterTracker;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.log4j.Logger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.DefaultApplicationArguments;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static fi.maanmittauslaitos.pta.search.utils.HarvesterTracker.IdentifierType;
import static fi.maanmittauslaitos.pta.search.utils.HarvesterTracker.RETRY_FOR_HARVESTING_EXCEPTION;

public abstract class AbstractHarvester implements CommandLineRunner {

	protected static final Logger logger = Logger.getLogger(AbstractHarvester.class);
	private static final String NUMBER_OF_THREADS_ARGUMENT = "threads";
	private static final int DEFAULT_NUMBER_OF_THREADS = 4;

	abstract protected DocumentSink getDocumentSink(HarvesterConfig config, HarvesterTracker harvesterTracker, ApplicationArguments args);

	abstract protected DocumentProcessor getDocumentProcessor(HarvesterConfig config) throws ParserConfigurationException, IOException;

	abstract protected HarvesterSource getHarvesterSource(HarvesterConfig config) throws XPathExpressionException, ParserConfigurationException;

	protected Optional<String> parseArgument(String optionName, ApplicationArguments args) {
		return Optional.ofNullable(args.getOptionValues(optionName))
				.filter(values -> !values.isEmpty())
				.map(values -> values.get(0));
	}


	HarvesterConfig getConfig() {
		return new HarvesterConfig();
	}


	@Override
	public void run(String... args) throws Exception {
		ApplicationArguments arguments = new DefaultApplicationArguments(args);
		logger.info("Arguments are " + arguments.getOptionNames());
		int nThreads = parseArgument(NUMBER_OF_THREADS_ARGUMENT, arguments)
				.map(Integer::parseInt)
				.orElse(DEFAULT_NUMBER_OF_THREADS);

		HarvesterConfig config = getConfig();
		HarvesterTracker tracker = config.getHarvesterTracker();
		HarvesterSource source = getHarvesterSource(config);
		DocumentProcessor processor = getDocumentProcessor(config);
		DocumentSink sink = getDocumentSink(config, tracker, arguments);

		boolean store = arguments.containsOption("store");

		if (!tracker.getIdentifiers().isEmpty()) {
			logger.warn("Identifiers of the documents, that are permanently skipped from harvesting process:\n" +
					String.join(",\n", tracker.getIdentifiersByType(IdentifierType.PERMANENTLY_SKIPPED)));
			logger.warn("Identifiers of the documents, that caused HarvestingExceptions during last session(s):\n" +
					String.join(",\n", tracker.getIdentifiersByType(IdentifierType.SKIPPED_DUE_HARVESTING_EXCEPTION)));
			logger.warn("Identifiers of the documents, that caused ProcessingExceptions during last session(s):\n" +
					String.join(",\n", tracker.getIdentifiersByType(IdentifierType.SKIPPED_DUE_PROCESSING_EXCEPTION)));
		}

		sink.startIndexing();



		logger.info("Starting harvesting with " + nThreads + " threads");

		harvestAsynchronously(tracker, source, processor, sink, store, nThreads);

		int deleted = sink.stopIndexing();

		logger.info("Inserted " + tracker.getIdentifiersByType(IdentifierType.INSERTED).size() + " documents");
		logger.info("Updated " + tracker.getIdentifiersByType(IdentifierType.UPDATED).size() + " documents");
		logger.info("Skipped due processing errors " + tracker.getIdentifiersByType(IdentifierType.SKIPPED_DUE_PROCESSING_EXCEPTION).size() + " documents");
		logger.info("Skipped permanently " + tracker.getIdentifiersByType(IdentifierType.PERMANENTLY_SKIPPED).size() + " documents");
		logger.info("Deleted " + deleted + " documents");

		tracker.harvestingFinished();
	}

	private void harvestAsynchronously(HarvesterTracker tracker, HarvesterSource source, DocumentProcessor processor, DocumentSink sink, boolean store, int nThreads) {
		ThreadFactory namedThreadFactory =
				new ThreadFactoryBuilder()
						.setNameFormat("harvest-thrd-%d").build();


		ExecutorService executor = Executors.newFixedThreadPool(nThreads, namedThreadFactory);

		Runnable shutdownExecutor = () -> {
			executor.shutdownNow();
			try {
				executor.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.error("Executor was shut down without waiting the timeout");
			}
		};

		CompletableFuture<List<String>> harvested = Streams.stream(source.iterator()).parallel()
				.collect(ParallelCollectors.parallelToList(harvestable -> {
					String id = harvestable.getIdentifier();
					if (tracker.isYetToBeProcessed(id)) {
						logger.info("Processing: " + id);
						boolean continueToNext = false;
						boolean shouldShutDownExecutor = false;
						int tries = 0;
						while (!continueToNext && tries < RETRY_FOR_HARVESTING_EXCEPTION) {
							try {
								tries++;
								continueToNext = processDocument(tracker, source, processor, sink, store, harvestable, id);

							} catch (HarvestingException e) {
								logger.error("Harvesting error for the id " + id +
										", retries: " + tries + "/" + RETRY_FOR_HARVESTING_EXCEPTION, e);
							} catch (IOException e) {
								logger.error("Exception occurred while closing the input stream");
								shouldShutDownExecutor = true;
								continueToNext = true;
								break;
							}
						}

						if (!continueToNext) {
							logger.error("Stopping harvesting due too many HarvestingExceptions");
							tracker.addToSkippedDueHarvestingException(id);
							shouldShutDownExecutor = true;
						}

						if (shouldShutDownExecutor) {
							shutdownExecutor.run();
						}
					}


					return id;
				}, executor));

		try {
			harvested.join();
		} catch (Exception e) {
			tracker.harvestingInterrupted();
			throw e;
		} finally {
			if (!executor.isShutdown()) {
				shutdownExecutor.run();
			}
		}

	}

	private boolean processDocument(HarvesterTracker tracker, HarvesterSource source, DocumentProcessor processor, DocumentSink sink, boolean store, Harvestable harvestable, String id) throws IOException {
		HarvesterInputStream is = source.getInputStream(harvestable);
		if (is == null) {
			logger.warn("Source is null, skipping, or stopping");
			throw new HarvestingException();
		}

		boolean continueToNext;
		try {
			Document doc = processor.processDocument(is);
			if (store && (tracker.getIdentifiersByType(IdentifierType.INSERTED).size() +
					tracker.getIdentifiersByType(IdentifierType.UPDATED).size()) < 100) {
				try {
					writeDocumentToFile(doc);
				} catch (IOException e) {
					logger.error("Failed to write document to a file: " + id, e);
				}
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


		} catch (DocumentProcessingException | SinkProcessingException | IllegalArgumentException e) {
			logger.error("Processing error for the id " + id, e);
			tracker.addToSkippedDueProcessingException(id);
			continueToNext = true;
		} finally {
			is.close();
		}
		return continueToNext;
	}

	private void writeDocumentToFile(Document doc) throws IOException {
		String id = doc.getValue(ISOMetadataFields.ID, String.class);
		ObjectMapper objectMapper = new ObjectMapper();
		try (FileOutputStream out = new FileOutputStream("indexed-documents/" + id + ".json")) {
			objectMapper.writeValue(out, doc.getFields());
		}
	}

}
