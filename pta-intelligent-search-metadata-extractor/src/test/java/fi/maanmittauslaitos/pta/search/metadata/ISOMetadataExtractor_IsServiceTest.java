package fi.maanmittauslaitos.pta.search.metadata;

import static org.junit.Assert.*;

import org.junit.Test;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;

public class ISOMetadataExtractor_IsServiceTest extends BaseMetadataExtractorTest {

	@Test
	public void testStatFiWFSIsService() throws Exception {
		Document document = createStatFiWFS();
		
		Boolean isService = document.getValue(ISOMetadataFields.IS_SERVICE, Boolean.class);
		assertEquals(Boolean.TRUE, isService);
	}
	
	@Test
	public void testMaastotietokantaIsNotService() throws Exception {
		Document document = createMaastotietokantaDocument();
		
		Boolean isService = document.getValue(ISOMetadataFields.IS_SERVICE, Boolean.class);
		assertEquals(Boolean.FALSE, isService);
	}
	
	@Test
	public void testLukeAineistosarjaIsNotService() throws Exception {
		Document document = createLukeTietoaineistosarja();
		
		Boolean isService = document.getValue(ISOMetadataFields.IS_SERVICE, Boolean.class);
		assertEquals(Boolean.FALSE, isService);
	}
	
	@Test
	public void testLukeAineistosarjaIsNotService_fromCSW() throws Exception {
		Document document = createLukeTietoaineistosarja_fromCSW();
		
		Boolean isService = document.getValue(ISOMetadataFields.IS_SERVICE, Boolean.class);
		assertEquals(Boolean.FALSE, isService);
	}

}
