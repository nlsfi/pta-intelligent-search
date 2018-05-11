package fi.maanmittauslaitos.pta.search.metadata;

import static org.junit.Assert.*;

import org.junit.Test;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;

public class ISOMetadataExtractor_IsAvoindataTest extends BaseMetadataExtractorTest {

	@Test
	public void testStatFiWFSIsAvoindata() throws Exception {
		Document document = createStatFiWFS();
		
		Boolean isAvoindata = document.getValue(ISOMetadataFields.IS_AVOINDATA, Boolean.class);
		assertEquals(Boolean.TRUE, isAvoindata);
	}
	

	@Test
	public void testStatFiWFS_modifiedIsNotAvoindata() throws Exception {
		Document document = createStatFiWFS_modified();
		
		Boolean isAvoindata = document.getValue(ISOMetadataFields.IS_AVOINDATA, Boolean.class);
		assertEquals(Boolean.FALSE, isAvoindata);
	}
	
	
	@Test
	public void testMaastotietokantaIsAvoindata() throws Exception {
		Document document = createMaastotietokantaDocument();
		
		Boolean isAvoindata = document.getValue(ISOMetadataFields.IS_AVOINDATA, Boolean.class);
		assertEquals(Boolean.TRUE, isAvoindata);
	}

}
