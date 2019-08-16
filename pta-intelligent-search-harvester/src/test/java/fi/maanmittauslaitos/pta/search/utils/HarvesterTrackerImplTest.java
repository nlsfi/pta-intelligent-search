package fi.maanmittauslaitos.pta.search.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.maanmittauslaitos.pta.search.utils.HarvesterTracker.IdentifierType;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static fi.maanmittauslaitos.pta.search.utils.HarvesterTracker.RETRY_FOR_PROCESSING_EXCEPTION;
import static fi.maanmittauslaitos.pta.search.utils.HarvesterTrackerImpl.TRACKER_FILE_TYPE_REFERENCE;
import static org.assertj.core.api.Assertions.assertThat;

public class HarvesterTrackerImplTest {

	private static final String EXISTING_HARVESTER_TRACKER_FILENAME = "test_harvester_tracker.json";
	private static final String TRACKER_FILENAME = "test_temp_harvester_tracker.json";
	private static final File TRACKER_FILE = Paths.get(TRACKER_FILENAME).toFile();
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Rule
	public final JUnitSoftAssertions softly = new JUnitSoftAssertions();
	private HarvesterTrackerImpl harvesterTracker;

	@Before
	public void setUp() throws Exception {
		deleteTempTrackerFile();
		assertThat(TRACKER_FILE).doesNotExist();
		harvesterTracker = new HarvesterTrackerImpl(TRACKER_FILE, OBJECT_MAPPER);
		assertThat(TRACKER_FILE).exists();
	}

	@After
	public void tearDown() throws Exception {
		deleteTempTrackerFile();
	}

	private void deleteTempTrackerFile() {
		if (TRACKER_FILE.exists()) {
			TRACKER_FILE.delete();
		}
	}

	@Test
	public void testRegularHarvestingWithExistingFile() throws IOException, URISyntaxException, XPathExpressionException, ParserConfigurationException {
		URL resource = getClass().getClassLoader().getResource(EXISTING_HARVESTER_TRACKER_FILENAME);
		assertThat(resource).isNotNull();

		harvesterTracker = new HarvesterTrackerImpl(Paths.get(resource.toURI()).toFile(), OBJECT_MAPPER);

		softly.assertThat(harvesterTracker.getTrackerMap())
				.containsKeys(IdentifierType.values());

		softly.assertThat(harvesterTracker.getTrackerMap().get(IdentifierType.SKIPPED_DUE_PROCESSING_EXCEPTION))
				.isEmpty();

		softly.assertThat(harvesterTracker.getTrackerMap().get(IdentifierType.INSERTED))
				.containsExactlyInAnyOrder("96da7ff7-f36a-490a-946f-f1aa3e287147", "198de061-3772-492d-bced-25789ac818dd");
	}


	@Test
	public void testRegularHarvestingFlow() throws IOException, XPathExpressionException, ParserConfigurationException {
		// First harvesting
		harvesterTracker.addIdToInserted("test-id");

		harvesterTracker.harvestingInterrupted();

		Map<HarvesterTracker.IdentifierType, List<String>> trackerFileContent = OBJECT_MAPPER.readValue(
				TRACKER_FILE, TRACKER_FILE_TYPE_REFERENCE);

		softly.assertThat(trackerFileContent.get(IdentifierType.INSERTED))
				.containsExactly("test-id");

		harvesterTracker.harvestingFinished();

		softly.assertThat(harvesterTracker.getIdentifiers()).isEmpty();
		softly.assertThat(TRACKER_FILE).doesNotExist();
	}

	@Test
	public void testWithSkipped() throws IOException, XPathExpressionException, ParserConfigurationException {
		// First harvesting
		harvesterTracker.addIdToInserted("test-id");
		harvesterTracker.addToSkippedDueProcessingException("test-id2");

		harvesterTracker.harvestingInterrupted();

		Map<HarvesterTracker.IdentifierType, List<String>> trackerFileContent = OBJECT_MAPPER.readValue(
				TRACKER_FILE, TRACKER_FILE_TYPE_REFERENCE);

		softly.assertThat(trackerFileContent.get(IdentifierType.INSERTED))
				.containsExactly("test-id");

		harvesterTracker.harvestingFinished();

		softly.assertThat(TRACKER_FILE).exists();

		// Second harvesting
		harvesterTracker.addIdToInserted("test-id2");
		harvesterTracker.harvestingFinished();

		softly.assertThat(harvesterTracker.getIdentifiers()).isEmpty();
		softly.assertThat(TRACKER_FILE).doesNotExist();
	}

	@Test
	public void testWithSkipped2() throws IOException, XPathExpressionException, ParserConfigurationException {
		String invalid = "invalid";

		harvesterTracker.addIdToInserted("test-id");

		IntStream.range(0, RETRY_FOR_PROCESSING_EXCEPTION)
				.forEach(i -> harvesterTracker.addToSkippedDueProcessingException(invalid));

		softly.assertThat(harvesterTracker.getIdentifiersByType(IdentifierType.PERMANENTLY_SKIPPED))
				.isEmpty();

		harvesterTracker.addToSkippedDueProcessingException(invalid);

		softly.assertThat(harvesterTracker.getIdentifiersByType(IdentifierType.PERMANENTLY_SKIPPED))
				.contains(invalid);
	}

	@Test
	public void testPermanentlySkipping() throws IOException, XPathExpressionException, ParserConfigurationException {
		String valid = "test-id";
		String invalid = "invalid";

		// First harvesting
		harvesterTracker.addIdToInserted(valid);

		IntStream.range(0, RETRY_FOR_PROCESSING_EXCEPTION)
				.forEach(i -> harvesterTracker.addToSkippedDueProcessingException(invalid));

		softly.assertThat(harvesterTracker.getIdentifiersByType(IdentifierType.PERMANENTLY_SKIPPED))
				.isEmpty();

		harvesterTracker.harvestingFinished();

		softly.assertThat(harvesterTracker.getIdentifiersByType(IdentifierType.SKIPPED_DUE_PROCESSING_EXCEPTION))
				.contains(invalid);
		softly.assertThat(harvesterTracker.getIdentifiersByType(IdentifierType.INSERTED))
				.contains(valid);

		// Second harvesting
		harvesterTracker.addToSkippedDueProcessingException(invalid);
		harvesterTracker.harvestingFinished();

		softly.assertThat(harvesterTracker.getIdentifiersByType(IdentifierType.PERMANENTLY_SKIPPED))
				.contains(invalid);
		softly.assertThat(harvesterTracker.getIdentifiersByType(IdentifierType.INSERTED))
				.isEmpty();
		softly.assertThat(TRACKER_FILE).exists();
	}


	@Test
	public void testThreadSafety() throws IOException, XPathExpressionException, ParserConfigurationException, ExecutionException, InterruptedException {
		harvesterTracker = new HarvesterTrackerImpl(TRACKER_FILE, OBJECT_MAPPER);

		int threads = 1000;
		List<String> ids = IntStream.range(0, threads).mapToObj(i -> "test_id_" + i).collect(Collectors.toList());
		CountDownLatch latch = new CountDownLatch(1);
		AtomicBoolean running = new AtomicBoolean();
		AtomicInteger overlaps = new AtomicInteger();

		ExecutorService service =
				Executors.newFixedThreadPool(threads);

		Collection<Future<String>> futures =
				new ArrayList<>(threads);
		for (int t = 0; t < threads; ++t) {
			String id = ids.get(t);
			futures.add(
					service.submit(
							() -> {
								latch.await();
								if (running.get()) {
									overlaps.incrementAndGet();
								}
								running.set(true);
								harvesterTracker.addIdToInserted(id);
								running.set(false);
								return id;
							}
					)
			);
		}
		latch.countDown();
		Set<String> ids2 = new HashSet<>();
		for (Future<String> f : futures) {
			ids2.add(f.get());
		}

		softly.assertThat(harvesterTracker.getIdentifiers()).containsExactlyInAnyOrderElementsOf(ids);
		softly.assertThat(ids2).containsExactlyInAnyOrderElementsOf(ids);
	}
}