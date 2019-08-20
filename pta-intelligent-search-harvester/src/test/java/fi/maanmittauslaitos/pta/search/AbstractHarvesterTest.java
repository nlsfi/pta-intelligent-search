package fi.maanmittauslaitos.pta.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.maanmittauslaitos.pta.search.csw.CSWHarvestable;
import fi.maanmittauslaitos.pta.search.csw.Harvestable;
import fi.maanmittauslaitos.pta.search.csw.HarvesterInputStream;
import fi.maanmittauslaitos.pta.search.csw.LocalHarvestable;
import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.index.DocumentSink;
import fi.maanmittauslaitos.pta.search.index.DocumentSink.IndexResult;
import fi.maanmittauslaitos.pta.search.utils.HarvesterTracker;
import fi.maanmittauslaitos.pta.search.utils.HarvesterTracker.IdentifierType;
import fi.maanmittauslaitos.pta.search.utils.HarvesterTrackerImpl;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.exceptions.base.MockitoException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class AbstractHarvesterTest {
	private static final String TRACKER_FILENAME = "test_temp_harvester_tracker.json";
	private static final File TRACKER_FILE = Paths.get(TRACKER_FILENAME).toFile();
	private static final MockitoException STOPPER_EXCEPTION =
			new MockitoException("Lets the test inspect the tracker content, when orherwise succesfull harvesting");
	@Mock
	private static DocumentSink mockSink;
	@Mock
	private static DocumentProcessor mockProcessor;
	@Mock
	private static HarvesterSource mockSource;
	@Mock
	private static HarvesterConfig mockConfig;
	@Mock
	private static HarvesterInputStream mockIs;
	@Mock
	private static HarvesterInputStream mockIsInvalid;
	@Rule
	public final JUnitSoftAssertions softly = new JUnitSoftAssertions();
	@Mock
	private Iterator<Harvestable> mockIterator;

	@Mock
	private Document mockDocument;

	private AbstractHarvester harvester;

	@Spy
	private HarvesterTrackerImpl harvesterTracker = new HarvesterTrackerImpl(TRACKER_FILE, new ObjectMapper());

	public AbstractHarvesterTest() throws IOException {
	}


	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		deleteTempTrackerFile();

		harvester = new TestHarvester();
		when(mockSource.iterator()).thenReturn(mockIterator);
		when(mockSource.getInputStream(Mockito.any(LocalHarvestable.class))).thenReturn(mockIs);
		when(mockProcessor.processDocument(Mockito.any(InputStream.class))).thenReturn(mockDocument);
		when(mockSink.indexDocument(mockDocument)).thenReturn(IndexResult.INSERTED);
		when(mockConfig.getHarvesterTracker()).thenReturn(harvesterTracker);
	}

	@After
	public void tearDown() {
		deleteTempTrackerFile();
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private void deleteTempTrackerFile() {
		if (TRACKER_FILE.exists()) {
			TRACKER_FILE.delete();
		}
	}

	@Test
	public void testSuccessfulHarvesting() throws Exception {
		List<String> ids = Arrays.asList("successful1", "successful2", "successful3");

		when(mockIterator.hasNext()).thenReturn(true, true, true, false);
		when(mockIterator.next())
				.thenReturn(CSWHarvestable.create(ids.get(0)))
				.thenReturn(CSWHarvestable.create(ids.get(1)))
				.thenReturn(CSWHarvestable.create(ids.get(2)));

		Mockito.doThrow(STOPPER_EXCEPTION).when(harvesterTracker).harvestingFinished();
		try {
			harvester.run();
		} catch (MockitoException ignored) {
			assertThat(harvesterTracker.getIdentifiersByType(IdentifierType.INSERTED))
					.containsExactlyInAnyOrderElementsOf(ids);
		}
	}

	@Test
	public void testContinuingAfterProcessingException() throws Exception {
		Harvestable valid = CSWHarvestable.create("successful1");
		Harvestable invalid = CSWHarvestable.create("causing_processing_exception");
		Harvestable valid2 = CSWHarvestable.create("successful2");

		when(mockIterator.hasNext()).thenReturn(true, true, true, false);
		when(mockIterator.next()).thenReturn(valid, invalid, valid2);


		when(mockSource.getInputStream(invalid)).thenReturn(mockIsInvalid);


		when(mockProcessor.processDocument(mockIs)).thenReturn(mockDocument);
		when(mockProcessor.processDocument(mockIsInvalid))
				.thenThrow(new DocumentProcessingException("caused by invalid document", new Throwable()));

		harvester.run();

		softly.assertThat(harvesterTracker.getIdentifiersByType(IdentifierType.INSERTED))
				.contains(valid.getIdentifier(), valid2.getIdentifier());
		softly.assertThat(harvesterTracker.getIdentifiersByType(IdentifierType.SKIPPED_DUE_PROCESSING_EXCEPTION))
				.contains(invalid.getIdentifier());

	}


	@Test
	public void testRecoveringFromProcessingException() throws Exception {
		Harvestable valid = CSWHarvestable.create("successful1");
		Harvestable previouslyInvalid = CSWHarvestable.create("caused_processing_exception_in_previous_run");
		harvesterTracker.addToSkippedDueProcessingException(previouslyInvalid.getIdentifier());

		when(mockIterator.hasNext()).thenReturn(true, true, false);
		when(mockIterator.next())
				.thenReturn(valid, previouslyInvalid);

		Mockito.doThrow(STOPPER_EXCEPTION).when(harvesterTracker).harvestingFinished();

		try {
			harvester.run();
		} catch (MockitoException ignored) {
			softly.assertThat(harvesterTracker.getIdentifiersByType(IdentifierType.INSERTED))
					.contains(valid.getIdentifier(), previouslyInvalid.getIdentifier());
			softly.assertThat(harvesterTracker.getIdentifiersByType(IdentifierType.SKIPPED_DUE_PROCESSING_EXCEPTION))
					.isEmpty();
		}
	}

	@Test
	public void testHarvestingException() throws Exception {
		Harvestable valid = CSWHarvestable.create("successful");
		Harvestable invalid = CSWHarvestable.create("causing_harvesting_exception");
		Harvestable valid2 = CSWHarvestable.create("successful_but_doesn't_get_harvested_if_synchronous_run");

		when(mockIterator.hasNext()).thenReturn(true, true, true, false);
		when(mockIterator.next()).thenReturn(valid, invalid, valid2);

		when(mockSource.getInputStream(invalid)).thenThrow(new HarvestingException());

		harvester.run();

		softly.assertThat(harvesterTracker.getIdentifiersByType(IdentifierType.INSERTED))
				.contains(valid.getIdentifier());
		softly.assertThat(harvesterTracker.getIdentifiersByType(IdentifierType.SKIPPED_DUE_HARVESTING_EXCEPTION))
				.contains(invalid.getIdentifier());
	}

	@Test
	public void testHarvestingExceptionRecovery() throws Exception {
		Harvestable valid = CSWHarvestable.create("successful");
		Harvestable invalid = CSWHarvestable.create("causing_harvesting_exception_at_first");
		Harvestable valid2 = CSWHarvestable.create("successful2");

		when(mockIterator.hasNext()).thenReturn(true, true, true, false);
		when(mockIterator.next())
				.thenReturn(valid, invalid, valid2);

		when(mockSource.getInputStream(invalid))
				.thenThrow(new HarvestingException())
				.thenReturn(mockIs);

		Mockito.doThrow(STOPPER_EXCEPTION).when(harvesterTracker).harvestingFinished();

		try {
			harvester.run();
		} catch (MockitoException ignored) {
			softly.assertThat(harvesterTracker.getIdentifiersByType(IdentifierType.INSERTED))
					.containsExactlyInAnyOrder(valid.getIdentifier(), valid2.getIdentifier(), invalid.getIdentifier());
			softly.assertThat(harvesterTracker.getIdentifiersByType(IdentifierType.SKIPPED_DUE_HARVESTING_EXCEPTION))
					.isEmpty();
		}
	}

	static class TestHarvester extends AbstractHarvester {

		@Override
		protected HarvesterConfig getConfig() {
			return mockConfig;
		}

		@Override
		protected DocumentSink getDocumentSink(HarvesterConfig config, HarvesterTracker harvesterTracker, String[] args) {
			return mockSink;
		}

		@Override
		protected DocumentProcessor getDocumentProcessor(HarvesterConfig config) {
			return mockProcessor;
		}

		@Override
		protected HarvesterSource getHarvesterSource(HarvesterConfig config) {
			return mockSource;
		}
	}
}