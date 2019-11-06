package fi.maanmittauslaitos.pta.search.metadata;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ISOMetadataExtractor_IsServiceTest extends BaseMetadataExtractorTest {

	@Test
	public void testStatFiWFSIsService() throws Exception {
		Document document = createStatFiWFS();
		
		Boolean isService = document.getValue(ResultMetadataFields.IS_SERVICE, Boolean.class);
		assertEquals(Boolean.TRUE, isService);
	}

	@Test
	public void testStatFiWFSModifiedIsService() throws Exception {
		Document document = createStatFiWFS_modified();

		Boolean isService = document.getValue(ResultMetadataFields.IS_SERVICE, Boolean.class);
		assertEquals(Boolean.TRUE, isService);
	}


	@Test
	public void testMaastotietokantaIsNotService() throws Exception {
		Document document = createMaastotietokantaDocument();
		
		Boolean isService = document.getValue(ResultMetadataFields.IS_SERVICE, Boolean.class);
		assertEquals(Boolean.FALSE, isService);
	}
	
	@Test
	public void testMaastotietokantaIsNotService_modified() throws Exception {
		Document document = createMaastotietokantaDocument_modified();
		
		Boolean isService = document.getValue(ResultMetadataFields.IS_SERVICE, Boolean.class);
		assertEquals(Boolean.FALSE, isService);
	}
	
	@Test
	public void testLukeAineistosarjaIsNotService() throws Exception {
		Document document = createLukeTietoaineistosarja();
		
		Boolean isService = document.getValue(ResultMetadataFields.IS_SERVICE, Boolean.class);
		assertEquals(Boolean.FALSE, isService);
	}
	
	@Test
	public void testLukeAineistosarjaIsNotService_fromCSW() throws Exception {
		Document document = createLukeTietoaineistosarja_fromCSW();
		
		Boolean isService = document.getValue(ResultMetadataFields.IS_SERVICE, Boolean.class);
		assertEquals(Boolean.FALSE, isService);
	}

}
