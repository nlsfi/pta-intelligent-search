package fi.maanmittauslaitos.pta.search.metadata;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessorFactory;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfigurationImpl;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfigurationImpl.FieldExtractorType;
import fi.maanmittauslaitos.pta.search.text.RegexProcessor;
import fi.maanmittauslaitos.pta.search.text.TextProcessingChain;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

public class OrganisationNamesForFullTextTest {

	private DocumentProcessor processor;
	
	@Before
	public void setup() throws Exception {
		DocumentProcessingConfiguration configuration = new DocumentProcessingConfiguration();

		configuration.getNamespaces().put("gmd", "http://www.isotc211.org/2005/gmd");
		

		TextProcessingChain removeEmptyEntriesChain = new TextProcessingChain();
		RegexProcessor whitespaceRemoval = new RegexProcessor();
		whitespaceRemoval.setPattern(Pattern.compile("^\\s*$"));
		whitespaceRemoval.setIncludeMatches(false);
		
		removeEmptyEntriesChain.getChain().add(whitespaceRemoval);
		
		configuration.getTextProcessingChains().put("removeEmptyEntries", removeEmptyEntriesChain);

		FieldExtractorConfigurationImpl organisationForSearch = new FieldExtractorConfigurationImpl();
		organisationForSearch.setField("organisationName_text");
		organisationForSearch.setType(FieldExtractorType.ALL_MATCHING_VALUES);
		organisationForSearch.setQuery("//gmd:contact//gmd:organisationName//text()");
		
		organisationForSearch.setTextProcessorName("removeEmptyEntries");
		
		configuration.getFieldExtractors().add(organisationForSearch);

		processor = DocumentProcessorFactory.getInstance().createXmlProcessor(configuration);
	}
	
	protected Document createStatFiWFS()
			throws DocumentProcessingException, IOException, FileNotFoundException {
		Document document;
		try (FileInputStream fis = new FileInputStream("src/test/resources/c3c05280-b1cd-4ae6-9c1a-26a8d9f7201d.xml")) {
			document = processor.processDocument(fis);
		}
		return document;
	}

	
	@Test
	public void test() throws Exception {
		Document doc = createStatFiWFS();
		
		List<String> values = doc.getListValue("organisationName_text", String.class);
		
		assertEquals(2, values.size());
		
		assertEquals("Tilastokeskus", values.get(0));
		assertEquals("Statistics Finland", values.get(1));
		//assertEquals("Tilastokeskus", values.get(2));
		//assertEquals("Tilastokeskus", values.get(3));
		
	}

}
