package fi.maanmittauslaitos.pta.search.xpath;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import fi.maanmittauslaitos.pta.search.Document;
import fi.maanmittauslaitos.pta.search.text.RegexProcessor;
import fi.maanmittauslaitos.pta.search.text.TextProcessingChain;
import fi.maanmittauslaitos.pta.search.xpath.FieldExtractorConfiguration.FieldExtractorType;

public class ExampleMetadataTest {
	private XPathProcessor processor;
	
	@Before
	public void setup() throws Exception
	{
		XPathExtractionConfiguration configuration = new XPathExtractionConfiguration();
		configuration.getNamespaces().put("gmd", "http://www.isotc211.org/2005/gmd");
		configuration.getNamespaces().put("gco", "http://www.isotc211.org/2005/gco");
		configuration.getNamespaces().put("srv", "http://www.isotc211.org/2005/srv");
		
		FieldExtractorConfiguration idExtractor = new FieldExtractorConfiguration();
		idExtractor.setField("@id");
		idExtractor.setType(FieldExtractorType.FIRST_MATCHING_VALUE);
		idExtractor.setXpath("//gmd:fileIdentifier/*/text()");
		
		configuration.getFieldExtractors().add(idExtractor);

		FieldExtractorConfiguration keywordExtractor = new FieldExtractorConfiguration();
		keywordExtractor.setField("avainsanat");
		keywordExtractor.setType(FieldExtractorType.ALL_MATCHING_VALUES);
		keywordExtractor.setXpath("//gmd:MD_Keywords/gmd:keyword/*/text()");
		keywordExtractor.setTextProcessorName("noWhitespace");
		
		configuration.getFieldExtractors().add(keywordExtractor);
		
		FieldExtractorConfiguration onlineResourceExtractor = new FieldExtractorConfiguration();
		onlineResourceExtractor.setField("onlineResource");
		onlineResourceExtractor.setType(FieldExtractorType.ALL_MATCHING_VALUES);
		onlineResourceExtractor.setXpath("//gmd:distributionInfo/*/gmd:transferOptions/*/gmd:onLine/*[contains(translate(gmd:protocol/*/text(),\"WFS\",\"wfs\"),\"wfs\")]/gmd:linkage/gmd:URL/text()");
		
		configuration.getFieldExtractors().add(onlineResourceExtractor);
		
		FieldExtractorConfiguration isServiceExtractor = new FieldExtractorConfiguration();
		isServiceExtractor.setField("isService");
		isServiceExtractor.setType(FieldExtractorType.TRUE_IF_MATCHES_OTHERWISE_FALSE);
		isServiceExtractor.setXpath("//gmd:identificationInfo/srv:SV_ServiceIdentification");
		
		configuration.getFieldExtractors().add(isServiceExtractor);

		FieldExtractorConfiguration isDatasetExtractor = new FieldExtractorConfiguration();
		isDatasetExtractor.setField("isDataset");
		isDatasetExtractor.setType(FieldExtractorType.TRUE_IF_MATCHES_OTHERWISE_FALSE);
		isDatasetExtractor.setXpath("//gmd:identificationInfo/gmd:MD_DataIdentification");
		
		configuration.getFieldExtractors().add(isDatasetExtractor);
		
		
		TextProcessingChain noWhitespaceProcessingChain = new TextProcessingChain();
		RegexProcessor whitespaceRemoval = new RegexProcessor();
		whitespaceRemoval.setPattern(Pattern.compile("^\\s*$"));
		whitespaceRemoval.setIncludeMatches(false);
		
		noWhitespaceProcessingChain.getChain().add(whitespaceRemoval);
		
		
		configuration.getTextProcessingChains().put("noWhitespace", noWhitespaceProcessingChain);
		
		processor = new XPathProcessorFactory().createProcessor(configuration);
		
	}
	
	@Test
	public void test_1719dcdd() throws Exception
	{
		Document document;
		try (FileInputStream fis = new FileInputStream("src/test/resources/metadata/1719dcdd-0f24-4406-a347-354532c97bde.xml")) {
			document = processor.processDocument(fis);
		}
		
		assertArrayEquals(new String[] { "1719dcdd-0f24-4406-a347-354532c97bde" } , document.getFields().get("@id").toArray());
		
		assertArrayEquals(new String[] { "Eliömaantieteelliset alueet", "Kasvillisuus", "Suo", "Biomaantieteelliset alueet" } , document.getFields().get("avainsanat").toArray());
		
		assertArrayEquals(new String[] { "false" }, document.getFields().get("isService").toArray());
		
		assertArrayEquals(new String[] { "true" }, document.getFields().get("isDataset").toArray());
	}
	
	@Test
	public void test_d47ac165() throws Exception
	{
		Document document;
		try (FileInputStream fis = new FileInputStream("src/test/resources/metadata/d47ac165-6abd-4357-a4f9-a6f17e2b0c58.xml")) {
			document = processor.processDocument(fis);
		}
		
		assertArrayEquals(new String[] { "d47ac165-6abd-4357-a4f9-a6f17e2b0c58" } , document.getFields().get("@id").toArray());
		
		assertArrayEquals(new String[] { "Johtoverkot", "Korkeus", "Tieliikenneverkko", "Rataverkko", "Hallinnolliset rajat", "Taajamat", "Rakennukset", "Paikannimet", "Kiintopisteet", "Vesistöt", "Ympäristö", "Vesirakentaminen", "Suojelukohteet", "Maanpeite", "Väylät", "Maankäyttö", "Ei-Inspire", "avoindata.fi" } , document.getFields().get("avainsanat").toArray());
		
		assertArrayEquals(new String[] { "false" }, document.getFields().get("isService").toArray());
		
		assertArrayEquals(new String[] { "true" }, document.getFields().get("isDataset").toArray());

	}
	
	@Test
	public void test_a901d40a() throws Exception
	{
		Document document;
		try (FileInputStream fis = new FileInputStream("src/test/resources/metadata/a901d40a-8a6b-4678-814c-79d2e2ab130c.xml")) {
			document = processor.processDocument(fis);
		}
		
		assertArrayEquals(new String[] { "a901d40a-8a6b-4678-814c-79d2e2ab130c" } , document.getFields().get("@id").toArray());
		
		List<String> tmp = document.getFields().get("onlineResource");
		System.out.println(tmp);
		
		assertEquals(1, tmp.size());
		assertEquals("http://geo.stat.fi/geoserver/vaestoruutu/wfs?service=WFS&request=GetCapabilities&version=1.0.0", tmp.get(0));
		
		assertArrayEquals(new String[] { "false" }, document.getFields().get("isService").toArray());
		
		assertArrayEquals(new String[] { "true" }, document.getFields().get("isDataset").toArray());
	}
	
	@Test
	public void test_1895c9c3() throws Exception
	{
		Document document;
		try (FileInputStream fis = new FileInputStream("src/test/resources/metadata/1895c9c3-ca82-4fbe-b6fd-e71297700afe.xml")) {
			document = processor.processDocument(fis);
		}
		
		assertArrayEquals(new String[] { "1895c9c3-ca82-4fbe-b6fd-e71297700afe" } , document.getFields().get("@id").toArray());
		
		assertArrayEquals(new String[] { "true" }, document.getFields().get("isService").toArray());
		
		assertArrayEquals(new String[] { "false" }, document.getFields().get("isDataset").toArray());

	}

}
