package fi.maanmittauslaitos.pta.search.metadata.json;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.metadata.model.ResponsibleParty;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static fi.maanmittauslaitos.pta.search.metadata.ResultMetadataFields.ABSTRACT;
import static fi.maanmittauslaitos.pta.search.metadata.ResultMetadataFields.DATESTAMP;
import static fi.maanmittauslaitos.pta.search.metadata.ResultMetadataFields.DISTRIBUTION_FORMATS;
import static fi.maanmittauslaitos.pta.search.metadata.ResultMetadataFields.GEOGRAPHIC_BOUNDING_BOX;
import static fi.maanmittauslaitos.pta.search.metadata.ResultMetadataFields.ID;
import static fi.maanmittauslaitos.pta.search.metadata.ResultMetadataFields.IS_AVOINDATA;
import static fi.maanmittauslaitos.pta.search.metadata.ResultMetadataFields.IS_DATASET;
import static fi.maanmittauslaitos.pta.search.metadata.ResultMetadataFields.IS_SERVICE;
import static fi.maanmittauslaitos.pta.search.metadata.ResultMetadataFields.KEYWORDS_ALL;
import static fi.maanmittauslaitos.pta.search.metadata.ResultMetadataFields.KEYWORDS_INSPIRE;
import static fi.maanmittauslaitos.pta.search.metadata.ResultMetadataFields.ORGANISATIONS;
import static fi.maanmittauslaitos.pta.search.metadata.ResultMetadataFields.TITLE;
import static fi.maanmittauslaitos.pta.search.metadata.ResultMetadataFields.TITLE_EN;
import static fi.maanmittauslaitos.pta.search.metadata.ResultMetadataFields.TITLE_SV;
import static fi.maanmittauslaitos.pta.search.metadata.json.CKANMetadataExtractorConfigurationFactory.DEFAULT_BOUNDING_BOX_FOR_CKAN_METADATA;

@SuppressWarnings("unchecked")
public class CKANMetadataExtractorsTest {

	@Rule
	public final JUnitSoftAssertions softly = new JUnitSoftAssertions();
	private DocumentProcessor processor;

	@Before
	public void setUp() throws Exception {
		processor = new CKANMetadataExtractorConfigurationFactory().createMetadataDocumentProcessor();
	}

	private Document createDocument(String path) throws DocumentProcessingException {
		return processor.processDocument(this.getClass().getClassLoader().getResourceAsStream(path));
	}

	@Test
	public void testService1() throws DocumentProcessingException {
		Document doc = createDocument("test_ckan_service_1.json");

		softly.assertThat(doc.getValue(ID, String.class)).isEqualTo("service-1-id");
		softly.assertThat(doc.getValue(IS_SERVICE, Boolean.class)).as(IS_SERVICE).isTrue();
		softly.assertThat(doc.getValue(IS_DATASET, Boolean.class)).as(IS_DATASET).isFalse();
		softly.assertThat(doc.getValue(TITLE, String.class)).as(TITLE).isEqualTo("service-1 title");
		softly.assertThat(doc.getValue(TITLE_SV, String.class)).as(TITLE_SV).as(TITLE_SV).isNull();
		softly.assertThat(doc.getValue(TITLE_EN, String.class)).as(TITLE_EN).as(TITLE_EN).isNull();
		softly.assertThat(doc.getValue(ABSTRACT, String.class)).as(ABSTRACT).isEqualTo("service-1 notes");
		softly.assertThat(doc.getValue(DATESTAMP, String.class)).as(DATESTAMP).isEqualTo("2019-10-11T07:50:15");
		softly.assertThat(doc.getValue(DISTRIBUTION_FORMATS, String.class)).as(DISTRIBUTION_FORMATS).isNull();
		softly.assertThat(doc.getListValue(KEYWORDS_ALL, String.class)).as(KEYWORDS_ALL).contains("keyword");
		softly.assertThat(doc.getListValue(KEYWORDS_INSPIRE, String.class)).as(KEYWORDS_INSPIRE).isEmpty();
		softly.assertThat(doc.getValue(IS_AVOINDATA, Boolean.class)).as(IS_AVOINDATA).isFalse();

		List<ResponsibleParty> organisations = doc.getListValue(ORGANISATIONS, ResponsibleParty.class);
		softly.assertThat(organisations).as(ORGANISATIONS)
				.isNotEmpty()
				.first()
				.extracting(ResponsibleParty::getOrganisationName)
				.isEqualTo("ORGANIZATION");
		softly.assertThat(organisations).as(ORGANISATIONS)
				.extracting(ResponsibleParty::getOrganisationName)
				.contains("ORGANIZATION", "ELY", "VTT");


		//softly.assertThat(doc.getListValue(TOPIC_CATEGORIES, String.class)).as(TOPIC_CATEGORIES).isEmpty();
		List<Double> bbox = doc.getValue(GEOGRAPHIC_BOUNDING_BOX, List.class);
		softly.assertThat(bbox).as(GEOGRAPHIC_BOUNDING_BOX)
				.containsExactlyElementsOf(DEFAULT_BOUNDING_BOX_FOR_CKAN_METADATA);

	}

	@Test
	public void testDataset1() throws DocumentProcessingException {
		Document doc = createDocument("test_ckan_dataset_1.json");

		softly.assertThat(doc.getValue(ID, String.class)).isEqualTo("dataset-1-id");
		softly.assertThat(doc.getValue(IS_DATASET, Boolean.class)).as(IS_DATASET).isTrue();
		softly.assertThat(doc.getValue(IS_SERVICE, Boolean.class)).as(IS_SERVICE).isFalse();
		softly.assertThat(doc.getValue(TITLE, String.class)).as(TITLE).isEqualTo("dataset-1 name");
		softly.assertThat(doc.getValue(TITLE_SV, String.class)).as(TITLE_SV).isNull();
		softly.assertThat(doc.getValue(TITLE_EN, String.class)).as(TITLE_EN).isNull();
		softly.assertThat(doc.getValue(ABSTRACT, String.class)).as(ABSTRACT).isEqualTo("Description of dataset 1");
		softly.assertThat(doc.getValue(DATESTAMP, String.class)).as(DATESTAMP).isEqualTo("2019-10-01T07:25:06");
		softly.assertThat(doc.getValue(DISTRIBUTION_FORMATS, String.class)).as(DISTRIBUTION_FORMATS).isEqualTo("XLSX");
		softly.assertThat(doc.getListValue(KEYWORDS_ALL, String.class)).as(KEYWORDS_ALL).contains("keyword");
		softly.assertThat(doc.getListValue(KEYWORDS_INSPIRE, String.class)).as(KEYWORDS_INSPIRE).isEmpty();
		softly.assertThat(doc.getValue(IS_AVOINDATA, Boolean.class)).as(IS_AVOINDATA).isFalse();

		List<ResponsibleParty> organisations = doc.getListValue(ORGANISATIONS, ResponsibleParty.class);
		softly.assertThat(organisations).as(ORGANISATIONS)
				.isNotEmpty()
				.first()
				.extracting(ResponsibleParty::getOrganisationName)
				.isEqualTo("ORGANIZATION");
		softly.assertThat(organisations).as(ORGANISATIONS)
				.extracting(ResponsibleParty::getOrganisationName)
				.contains("ORGANIZATION", "ELY", "VTT");

		List<Double> bbox = doc.getValue(GEOGRAPHIC_BOUNDING_BOX, List.class);
		softly.assertThat(bbox).as(GEOGRAPHIC_BOUNDING_BOX)
				.containsExactlyElementsOf(DEFAULT_BOUNDING_BOX_FOR_CKAN_METADATA);
	}


	@Test
	public void testDataset2() throws DocumentProcessingException {
		Document doc = createDocument("test_ckan_dataset_2.json");

		softly.assertThat(doc.getValue(ID, String.class)).isEqualTo("dataset-2-id");
		softly.assertThat(doc.getValue(IS_DATASET, Boolean.class)).as(IS_DATASET).isTrue();
		softly.assertThat(doc.getValue(IS_SERVICE, Boolean.class)).as(IS_SERVICE).isFalse();
		softly.assertThat(doc.getValue(TITLE, String.class)).as(TITLE).isEqualTo("test pdf");
		softly.assertThat(doc.getValue(TITLE_SV, String.class)).as(TITLE_SV).isNull();
		softly.assertThat(doc.getValue(TITLE_EN, String.class)).as(TITLE_EN).isNull();
		softly.assertThat(doc.getValue(ABSTRACT, String.class)).as(ABSTRACT).isEqualTo("");
		softly.assertThat(doc.getValue(DATESTAMP, String.class)).as(DATESTAMP).isEqualTo("2019-10-11T07:20:51");
		softly.assertThat(doc.getValue(DISTRIBUTION_FORMATS, String.class)).as(DISTRIBUTION_FORMATS).isEqualTo("application/pdf");

		List<Double> bbox = doc.getValue(GEOGRAPHIC_BOUNDING_BOX, List.class);
		softly.assertThat(bbox).as(GEOGRAPHIC_BOUNDING_BOX)
				.containsExactlyElementsOf(DEFAULT_BOUNDING_BOX_FOR_CKAN_METADATA);
	}

	@Test
	public void testService2() throws DocumentProcessingException {
		Document doc = createDocument("test_ckan_service_2.json");

		softly.assertThat(doc.getListValue(ORGANISATIONS, ResponsibleParty.class)).as(ORGANISATIONS).isEmpty();

		List<Double> bbox = doc.getValue(GEOGRAPHIC_BOUNDING_BOX, List.class);
		softly.assertThat(bbox).as(GEOGRAPHIC_BOUNDING_BOX).containsExactly(24.831, 60.130, 25.271, 60.298);

		softly.assertThat(doc.getListValue(KEYWORDS_INSPIRE, String.class)).as(KEYWORDS_INSPIRE).contains(
				"Bio-geographical regions",
				"Administraive units",
				"Hydrography"
		);

	}
}