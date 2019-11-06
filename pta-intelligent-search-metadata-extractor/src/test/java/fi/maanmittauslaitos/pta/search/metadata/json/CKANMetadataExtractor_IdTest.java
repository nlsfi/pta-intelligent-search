package fi.maanmittauslaitos.pta.search.metadata.json;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.metadata.ResultMetadataFields;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;

public class CKANMetadataExtractor_IdTest {

	@Rule
	public final JUnitSoftAssertions softly = new JUnitSoftAssertions();
	protected DocumentProcessor processor;

	@Before
	public void setUp() throws Exception {
		processor = new CKANMetadataExtractorConfigurationFactory().createMetadataDocumentProcessor();
	}

	private Document createDocument(String path) throws DocumentProcessingException, IOException {
		Document document;
		try (FileInputStream fis = new FileInputStream(path)) {
			document = processor.processDocument(fis);
		}
		return document;
	}

	@Test
	public void testService1() throws DocumentProcessingException, IOException {
		Document document = createDocument("src/test/resources/test_ckan_service_1.json");
		softly.assertThat(document.getValue(ResultMetadataFields.ID, String.class)).isEqualTo("service-1-id");
		softly.assertThat(document.getValue(ResultMetadataFields.IS_SERVICE, Boolean.class)).isTrue();
		softly.assertThat(document.getValue(ResultMetadataFields.IS_DATASET, Boolean.class)).isFalse();
	}

	@Test
	public void testService2() throws DocumentProcessingException, IOException {
		Document document = createDocument("src/test/resources/test_ckan_dataset_1.json");
		softly.assertThat(document.getValue(ResultMetadataFields.ID, String.class)).isEqualTo("dataset-1-id");
		softly.assertThat(document.getValue(ResultMetadataFields.IS_DATASET, Boolean.class)).isTrue();
		softly.assertThat(document.getValue(ResultMetadataFields.IS_SERVICE, Boolean.class)).isFalse();
	}
}