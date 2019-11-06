package fi.maanmittauslaitos.pta.search.metadata;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;

public class ISOMetadataExtractorTest {

	private DocumentProcessor processor;
	
	@Before
	public void setUp() throws Exception {
		processor = new ISOMetadataExtractorConfigurationFactory().createMetadataDocumentProcessor();
	}

	private Document createMaastotietokantaDocument()
			throws DocumentProcessingException, IOException, FileNotFoundException {
		Document document;
		try (FileInputStream fis = new FileInputStream("src/test/resources/ddad3347-05ca-401a-b746-d883d4110180.xml")) {
			document = processor.processDocument(fis);
		}
		return document;
	}


	@Test
	public void testTitle() throws Exception {
		Document document = createMaastotietokantaDocument();
		
		String titleValue = document.getValue(ResultMetadataFields.TITLE, String.class);
		assertEquals("Maastotietokanta", titleValue);
		
	}

}
