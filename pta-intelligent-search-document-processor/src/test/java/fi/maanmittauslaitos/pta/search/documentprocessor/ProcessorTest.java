package fi.maanmittauslaitos.pta.search.documentprocessor;

import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfigurationImpl.FieldExtractorType;
import fi.maanmittauslaitos.pta.search.text.TextProcessingChain;
import fi.maanmittauslaitos.pta.search.text.TextProcessor;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProcessorTest {
	private DocumentProcessingConfiguration configuration;

	@Before
	public void setUp() throws Exception {
		configuration = new DocumentProcessingConfiguration();
		configuration.getNamespaces().put("gmd", "http://www.isotc211.org/2005/gmd");
		configuration.getNamespaces().put("gco", "http://www.isotc211.org/2005/gco");

		FieldExtractorConfigurationImpl idExtractor = new FieldExtractorConfigurationImpl();
		idExtractor.setField("@id");
		idExtractor.setType(FieldExtractorType.FIRST_MATCHING_VALUE);
		idExtractor.setQuery("//gmd:fileIdentifier/*/text()");

		configuration.getFieldExtractors().add(idExtractor);
	}

	@Test
	public void testSingleValueExtraction() throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(
				("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\">\n" +
						"  <gmd:fileIdentifier>\n" +
						"      <gco:CharacterString>this-is-my-id</gco:CharacterString>\n" +
						"  </gmd:fileIdentifier>\n" +
						"</gmd:MD_Metadata>\n").getBytes(StandardCharsets.UTF_8));

		DocumentProcessor processor = DocumentProcessorFactory.getInstance().createXmlProcessor(configuration);

		Document doc = processor.processDocument(bais);

		List<String> ids = doc.getListValue("@id", String.class);
		assertNotNull(ids);
		assertEquals(1, ids.size());
		String id = ids.get(0);
		assertEquals("this-is-my-id", id);
	}

	@Test
	public void testSingleValueExtractionWith1to1Processor() throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(
				("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\">\n" +
						"  <gmd:fileIdentifier>\n" +
						"      <gco:CharacterString>this-is-my-id</gco:CharacterString>\n" +
						"  </gmd:fileIdentifier>\n" +
						"</gmd:MD_Metadata>\n").getBytes(StandardCharsets.UTF_8));

		TextProcessingChain oneToOne = new TextProcessingChain();

		oneToOne.getChain().add(new TextProcessor() {

			@Override
			public List<String> process(List<String> input) {
				List<String> ret = new ArrayList<>();
				for (String str : input) {
					ret.add(str + "-1");
				}
				return ret;
			}
		});

		configuration.getFieldExtractors().get(0).setTextProcessorName("1to1");
		configuration.getTextProcessingChains().put("1to1", oneToOne);

		DocumentProcessor processor = DocumentProcessorFactory.getInstance().createXmlProcessor(configuration);

		Document doc = processor.processDocument(bais);

		List<String> ids = doc.getListValue("@id", String.class);
		assertNotNull(ids);
		assertEquals(1, ids.size());
		String id = ids.get(0);
		assertEquals("this-is-my-id-1", id);
	}


	@Test
	public void testSingleValueExtractionMultipleInDocument() throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(
				("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\">\n" +
						"  <gmd:fileIdentifier>\n" +
						"      <gco:CharacterString>this-is-my-id</gco:CharacterString>\n" +
						"  </gmd:fileIdentifier>\n" +
						"  <gmd:fileIdentifier>\n" +
						"      <gco:CharacterString>this-is-not-the-id</gco:CharacterString>\n" +
						"  </gmd:fileIdentifier>\n" +
						"</gmd:MD_Metadata>\n").getBytes(StandardCharsets.UTF_8));

		DocumentProcessor processor = DocumentProcessorFactory.getInstance().createXmlProcessor(configuration);

		Document doc = processor.processDocument(bais);

		List<String> ids = doc.getListValue("@id", String.class);
		assertNotNull(ids);
		assertEquals(1, ids.size());
		String id = ids.get(0);
		assertEquals("this-is-my-id", id);
	}


	@Test
	public void testMultiValueExtractionMultipleInDocument() throws Exception {

		FieldExtractorConfigurationImpl extraExtractor = new FieldExtractorConfigurationImpl();
		extraExtractor.setField("extra");
		extraExtractor.setType(FieldExtractorType.ALL_MATCHING_VALUES);
		extraExtractor.setQuery("//gmd:fileIdentifier/*/text()");


		configuration.getFieldExtractors().add(extraExtractor);


		ByteArrayInputStream bais = new ByteArrayInputStream(
				("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\">\n" +
						"  <gmd:fileIdentifier>\n" +
						"      <gco:CharacterString>this-is-my-id</gco:CharacterString>\n" +
						"  </gmd:fileIdentifier>\n" +
						"  <gmd:fileIdentifier>\n" +
						"      <gco:CharacterString>another-ONE</gco:CharacterString>\n" +
						"  </gmd:fileIdentifier>\n" +
						"</gmd:MD_Metadata>\n").getBytes(StandardCharsets.UTF_8));

		DocumentProcessor processor = DocumentProcessorFactory.getInstance().createXmlProcessor(configuration);

		Document doc = processor.processDocument(bais);

		List<String> ids = doc.getListValue("extra", String.class);
		assertNotNull(ids);
		assertEquals(2, ids.size());
		String id = ids.get(0);
		assertEquals("this-is-my-id", id);

		String id2 = ids.get(1);
		assertEquals("another-ONE", id2);
	}


	@Test
	public void testCustomExtractor() throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(
				("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\">\n" +
						"  <gmd:fileIdentifier>\n" +
						"      <gco:CharacterString>one</gco:CharacterString>\n" +
						"  </gmd:fileIdentifier>\n" +
						"  <gmd:fileIdentifier>\n" +
						"      <gco:CharacterString>two</gco:CharacterString>\n" +
						"  </gmd:fileIdentifier>\n" +
						"</gmd:MD_Metadata>\n").getBytes(StandardCharsets.UTF_8));


		FieldExtractorConfigurationImpl customExtractor = new FieldExtractorConfigurationImpl();
		customExtractor.setField("foo");
		customExtractor.setType(FieldExtractorType.CUSTOM_CLASS);
		customExtractor.setCustomExtractor((documentQuery, queryResult) ->
				documentQuery.process("./gco:CharacterString/text()", queryResult).get(0).getValue());

		customExtractor.setQuery("/gmd:MD_Metadata/gmd:fileIdentifier");

		configuration.getFieldExtractors().add(customExtractor);

		DocumentProcessor processor = DocumentProcessorFactory.getInstance().createXmlProcessor(configuration);

		Document doc = processor.processDocument(bais);

		List<String> values = doc.getListValue("foo", String.class);

		assertEquals(2, values.size());
		String val1 = values.get(0);
		assertEquals("one", val1);
		String val2 = values.get(1);
		assertEquals("two", val2);

	}

}
