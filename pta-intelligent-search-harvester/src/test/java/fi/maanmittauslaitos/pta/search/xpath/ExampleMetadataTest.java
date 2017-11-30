package fi.maanmittauslaitos.pta.search.xpath;

import static org.junit.Assert.*;

import java.io.FileInputStream;
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
	}

}
