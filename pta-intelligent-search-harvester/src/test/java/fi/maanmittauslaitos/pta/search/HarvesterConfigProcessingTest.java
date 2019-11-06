package fi.maanmittauslaitos.pta.search;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfiguration;
import fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants;
import fi.maanmittauslaitos.pta.search.metadata.ISOMetadataExtractorConfigurationFactory;
import fi.maanmittauslaitos.pta.search.metadata.ResultMetadataFields;
import fi.maanmittauslaitos.pta.search.metadata.MetadataExtractorConfigurationFactory;
import fi.maanmittauslaitos.pta.search.text.RDFTerminologyMatcherProcessor;
import fi.maanmittauslaitos.pta.search.text.WordCombinationProcessor;
import org.assertj.core.api.JUnitSoftAssertions;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class HarvesterConfigProcessingTest {
	private static String CAT_URL = "http://www.yso.fi/onto/ysa/Y96241";
	private static String PREDATOR_URL = "http://www.yso.fi/onto/ysa/Y98100";

	private HarvesterConfig conf;

	@Rule
	public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

	@Before
	public void setUp() throws IOException {
		conf = new HarvesterConfig();
	}

	private Document getProcessedDocument(String modelName, String metadataFile) throws IOException, ParserConfigurationException, DocumentProcessingException {
		InputStream resource = getClass().getResourceAsStream(modelName);
		Model model = Rio.parse(resource, "", RDFFormat.TURTLE);

		MetadataExtractorConfigurationFactory factory = new ISOMetadataExtractorConfigurationFactory();
		DocumentProcessingConfiguration configuration = factory
				.createMetadataDocumentProcessingConfiguration();

		// Abstract uri
		RDFTerminologyMatcherProcessor terminologyProcessor = conf.createTerminologyMatcher(model);
		WordCombinationProcessor wordCombinationProcessor = conf.createWordCombinationProcessor(model);
		configuration.getTextProcessingChains().put("abstractProcessor",
				conf.createAbstractProcessingChain(terminologyProcessor, wordCombinationProcessor));

		FieldExtractorConfiguration abstractUri = configuration.getFieldExtractor(ResultMetadataFields.ABSTRACT).copy();
		abstractUri.setField(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_URI);
		abstractUri.setTextProcessorName("abstractProcessor");
		configuration.getFieldExtractors().add(abstractUri);

		// Keyword uri
		configuration.getTextProcessingChains().put("keywordProcessor",
				conf.createKeywordProcessingChain(terminologyProcessor, wordCombinationProcessor));

		FieldExtractorConfiguration keywordsUri = configuration.getFieldExtractor(ResultMetadataFields.KEYWORDS_ALL).copy();
		keywordsUri.setField(PTAElasticSearchMetadataConstants.FIELD_KEYWORDS_URI);
		keywordsUri.setTextProcessorName("keywordProcessor");
		configuration.getFieldExtractors().add(keywordsUri);


		DocumentProcessor processor = factory.getDocumentProcessorFactory().createXmlProcessor(configuration);

		Document document;
		try (InputStream fis = getClass().getResourceAsStream(metadataFile)) {
			document = processor.processDocument(fis);
		}
		return document;
	}

	@Test
	public void testAgainstSimpleCsw() throws IOException, ParserConfigurationException, DocumentProcessingException {
		Document document = getProcessedDocument("/kissa.ttl", "/testcsws/kissa.xml");

		softly.assertThat(document.getFields())
				.containsKey(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_URI)
				.flatExtracting(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_URI)
				.containsExactlyInAnyOrder(CAT_URL, CAT_URL, CAT_URL, CAT_URL, PREDATOR_URL);

		softly.assertThat(document.getFields())
				.containsKey(PTAElasticSearchMetadataConstants.FIELD_KEYWORDS_URI)
				.flatExtracting(PTAElasticSearchMetadataConstants.FIELD_KEYWORDS_URI)
				.containsExactlyInAnyOrder(CAT_URL, CAT_URL, PREDATOR_URL);
	}
}
