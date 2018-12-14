package fi.maanmittauslaitos.pta.search.metadata;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;

public abstract class BaseMetadataExtractorTest {

	protected DocumentProcessor processor;
	
	@Before
	public void setUp() throws Exception {
		processor = new ISOMetadataExtractorConfigurationFactory().createMetadataDocumentProcessor();
	}

	protected Document createMaastotietokantaDocument()
			throws DocumentProcessingException, IOException, FileNotFoundException {
		Document document;
		try (FileInputStream fis = new FileInputStream("src/test/resources/ddad3347-05ca-401a-b746-d883d4110180.xml")) {
			document = processor.processDocument(fis);
		}
		return document;
	}
	
	protected Document createMaastotietokantaDocument_modified()
			throws DocumentProcessingException, IOException, FileNotFoundException {
		Document document;
		try (FileInputStream fis = new FileInputStream("src/test/resources/ddad3347-05ca-401a-b746-d883d4110180_modified.xml")) {
			document = processor.processDocument(fis);
		}
		return document;
	}
	

	protected Document createMaastotietokantaDocument_doubleText()
			throws DocumentProcessingException, IOException, FileNotFoundException {
		Document document;
		try (FileInputStream fis = new FileInputStream("src/test/resources/ddad3347-05ca-401a-b746-d883d4110180_with_double_text.xml")) {
			document = processor.processDocument(fis);
		}
		return document;
	}
	

	protected Document createStatFiWFS()
			throws DocumentProcessingException, IOException, FileNotFoundException {
		Document document;
		try (FileInputStream fis = new FileInputStream("src/test/resources/c3c05280-b1cd-4ae6-9c1a-26a8d9f7201d.xml")) {
			document = processor.processDocument(fis);
		}
		return document;
	}
	

	protected Document createStatFiWFS_modified()
			throws DocumentProcessingException, IOException, FileNotFoundException {
		Document document;
		try (FileInputStream fis = new FileInputStream("src/test/resources/c3c05280-b1cd-4ae6-9c1a-26a8d9f7201d_modified.xml")) {
			document = processor.processDocument(fis);
		}
		return document;
	}


	protected Document createLukeTietoaineistosarja()
			throws DocumentProcessingException, IOException, FileNotFoundException {
		Document document;
		try (FileInputStream fis = new FileInputStream("src/test/resources/2e5565ff-f17f-42a5-9435-d6353f2db46f.xml")) {
			document = processor.processDocument(fis);
		}
		return document;
	}


	protected Document createLukeTietoaineistosarja_fromCSW()
			throws DocumentProcessingException, IOException, FileNotFoundException {
		Document document;
		try (FileInputStream fis = new FileInputStream("src/test/resources/2e5565ff-f17f-42a5-9435-d6353f2db46f_fromcsw.xml")) {
			document = processor.processDocument(fis);
		}
		return document;
	}
	
	protected Document createYlojarviAineisto()
			throws DocumentProcessingException, IOException, FileNotFoundException {
		Document document;
		try (FileInputStream fis = new FileInputStream("src/test/resources/b845181a-7db0-41d6-bf58-7d1c3c3c5c5a.xml")) {
			document = processor.processDocument(fis);
		}
		return document;
	}
	
	protected Document createLiikennevirastoAvoinWFS()
			throws DocumentProcessingException, IOException, FileNotFoundException {
		Document document;
		try (FileInputStream fis = new FileInputStream("src/test/resources/60d5fd06-44fe-4e41-a39a-60df747ac7ee.xml")) {
			document = processor.processDocument(fis);
		}
		return document;
	}
	
}
