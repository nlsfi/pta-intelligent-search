package fi.maanmittauslaitos.pta.search.documentprocessor;

import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfigurationImpl.FieldExtractorType;
import fi.maanmittauslaitos.pta.search.text.ExistsInSetProcessor;
import fi.maanmittauslaitos.pta.search.text.RegexProcessor;
import fi.maanmittauslaitos.pta.search.text.TextProcessingChain;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExampleMetadataTest {
	private DocumentProcessor processor;
	
	@Before
	public void setup() throws Exception
	{
		DocumentProcessingConfiguration configuration = new DocumentProcessingConfiguration();
		configuration.getNamespaces().put("gmd", "http://www.isotc211.org/2005/gmd");
		configuration.getNamespaces().put("gco", "http://www.isotc211.org/2005/gco");
		configuration.getNamespaces().put("srv", "http://www.isotc211.org/2005/srv");
		configuration.getNamespaces().put("gmx", "http://www.isotc211.org/2005/gmx");
		
		configuration.getNamespaces().put("xlink", "http://www.w3.org/1999/xlink");


		FieldExtractorConfigurationImpl idExtractor = new FieldExtractorConfigurationImpl();
		idExtractor.setField("@id");
		idExtractor.setType(FieldExtractorType.FIRST_MATCHING_VALUE);
		idExtractor.setQuery("//gmd:fileIdentifier/*/text()");
		
		configuration.getFieldExtractors().add(idExtractor);

		FieldExtractorConfigurationImpl keywordExtractor = new FieldExtractorConfigurationImpl();
		keywordExtractor.setField("avainsanat");
		keywordExtractor.setType(FieldExtractorType.ALL_MATCHING_VALUES);
		keywordExtractor.setQuery("//gmd:MD_Keywords/gmd:keyword/*/text()");
		keywordExtractor.setTextProcessorName("noWhitespace");
		
		configuration.getFieldExtractors().add(keywordExtractor);

		FieldExtractorConfigurationImpl onlineResourceExtractor = new FieldExtractorConfigurationImpl();
		onlineResourceExtractor.setField("onlineResource");
		onlineResourceExtractor.setType(FieldExtractorType.ALL_MATCHING_VALUES);
		onlineResourceExtractor.setQuery("//gmd:distributionInfo/*/gmd:transferOptions/*/gmd:onLine/*[contains(translate(gmd:protocol/*/text(),\"WFS\",\"wfs\"),\"wfs\")]/gmd:linkage/gmd:URL/text()");
		
		configuration.getFieldExtractors().add(onlineResourceExtractor);

		FieldExtractorConfigurationImpl isServiceExtractor = new FieldExtractorConfigurationImpl();
		isServiceExtractor.setField("isService");
		isServiceExtractor.setType(FieldExtractorType.TRUE_IF_MATCHES_OTHERWISE_FALSE);
		isServiceExtractor.setQuery("//gmd:identificationInfo/srv:SV_ServiceIdentification");
		
		configuration.getFieldExtractors().add(isServiceExtractor);

		FieldExtractorConfigurationImpl isDatasetExtractor = new FieldExtractorConfigurationImpl();
		isDatasetExtractor.setField("isDataset");
		isDatasetExtractor.setType(FieldExtractorType.TRUE_IF_MATCHES_OTHERWISE_FALSE);
		isDatasetExtractor.setQuery("//gmd:identificationInfo/gmd:MD_DataIdentification");
		
		configuration.getFieldExtractors().add(isDatasetExtractor);

		FieldExtractorConfigurationImpl annotatedKeywordExtractor = new FieldExtractorConfigurationImpl();
		annotatedKeywordExtractor.setField("annotated_keywords_uri");
		annotatedKeywordExtractor.setType(FieldExtractorType.ALL_MATCHING_VALUES);
		annotatedKeywordExtractor.setQuery("//gmd:descriptiveKeywords/*/gmd:keyword/gmx:Anchor/@xlink:href");
		
		annotatedKeywordExtractor.setTextProcessorName("isInOntologyFilterProcessor");
		
		configuration.getFieldExtractors().add(annotatedKeywordExtractor);
		
		
		TextProcessingChain noWhitespaceProcessingChain = new TextProcessingChain();
		RegexProcessor whitespaceRemoval = new RegexProcessor();
		whitespaceRemoval.setPattern(Pattern.compile("^\\s*$"));
		whitespaceRemoval.setIncludeMatches(false);
		
		noWhitespaceProcessingChain.getChain().add(whitespaceRemoval);
		
		
		configuration.getTextProcessingChains().put("noWhitespace", noWhitespaceProcessingChain);
		
		TextProcessingChain isInOntologyFilterProcessor = new TextProcessingChain();
		
		ExistsInSetProcessor allowInOntology = new ExistsInSetProcessor();
		allowInOntology.setAcceptedStrings(new HashSet<>(Arrays.asList("http://paikkatiedot.fi/def/1001002/p877", "http://paikkatiedot.fi/def/1001001/p296")));
		isInOntologyFilterProcessor.getChain().add(allowInOntology);
		
		configuration.getTextProcessingChains().put("isInOntologyFilterProcessor", isInOntologyFilterProcessor);


		processor = DocumentProcessorFactory.getInstance().createXmlProcessor(configuration);
		
	}
	
	@Test
	public void test_1719dcdd() throws Exception
	{
		Document document;
		try (FileInputStream fis = new FileInputStream("src/test/resources/metadata/1719dcdd-0f24-4406-a347-354532c97bde.xml")) {
			document = processor.processDocument(fis);
		}
		
		assertEquals("1719dcdd-0f24-4406-a347-354532c97bde", document.getValue("@id", String.class));
		
		assertArrayEquals(new String[] { "Kasvillisuus", "Suo", "Biomaantieteelliset alueet", "Kasvillisuus", "Suot" } , document.getListValue("avainsanat", String.class).toArray());
		
		assertArrayEquals(new String[] { "http://paikkatiedot.fi/def/1001002/p877", "http://paikkatiedot.fi/def/1001001/p296" }, document.getListValue("annotated_keywords_uri", String.class).toArray()); 
		
		assertFalse(document.getValue("isService", Boolean.class));
		assertTrue(document.getValue("isDataset", Boolean.class));
		
		
	}
	
	@Test
	public void test_d47ac165() throws Exception
	{
		Document document;
		try (FileInputStream fis = new FileInputStream("src/test/resources/metadata/d47ac165-6abd-4357-a4f9-a6f17e2b0c58.xml")) {
			document = processor.processDocument(fis);
		}
		
		assertEquals("d47ac165-6abd-4357-a4f9-a6f17e2b0c58", document.getValue("@id", String.class));
		
		assertArrayEquals(new String[] { "Johtoverkot", "Korkeus", "Tieliikenneverkko", "Rataverkko", "Hallinnolliset rajat", "Taajamat", "Rakennukset", "Paikannimet", "Kiintopisteet", "Vesistöt", "Ympäristö", "Vesirakentaminen", "Suojelukohteet", "Maanpeite", "Väylät", "Maankäyttö", "Ei-Inspire", "avoindata.fi" } , document.getListValue("avainsanat", String.class).toArray());
		
		assertFalse(document.getValue("isService", Boolean.class));
		assertTrue(document.getValue("isDataset", Boolean.class));

	}
	
	@Test
	public void test_a901d40a() throws Exception
	{
		Document document;
		try (FileInputStream fis = new FileInputStream("src/test/resources/metadata/a901d40a-8a6b-4678-814c-79d2e2ab130c.xml")) {
			document = processor.processDocument(fis);
		}
		
		assertEquals("a901d40a-8a6b-4678-814c-79d2e2ab130c", document.getValue("@id", String.class));
		
		List<String> tmp = document.getListValue("onlineResource", String.class);
		System.out.println(tmp);
		
		assertEquals(1, tmp.size());
		assertEquals("http://geo.stat.fi/geoserver/vaestoruutu/wfs?service=WFS&request=GetCapabilities&version=1.0.0", tmp.get(0));
		
		assertFalse(document.getValue("isService", Boolean.class));
		assertTrue(document.getValue("isDataset", Boolean.class));
	}
	
	@Test
	public void test_1895c9c3() throws Exception
	{
		Document document;
		try (FileInputStream fis = new FileInputStream("src/test/resources/metadata/1895c9c3-ca82-4fbe-b6fd-e71297700afe.xml")) {
			document = processor.processDocument(fis);
		}
		
		assertEquals("1895c9c3-ca82-4fbe-b6fd-e71297700afe", document.getValue("@id", String.class));
		
		assertTrue(document.getValue("isService", Boolean.class));
		assertFalse(document.getValue("isDataset", Boolean.class));

	}

}
