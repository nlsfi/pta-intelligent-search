package fi.maanmittauslaitos.pta.search.metadata;

import static org.junit.Assert.*;

import org.junit.Test;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;

public class ISOMetadataExtractor_IsService extends BaseMetadataExtractorTest {

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

}
