package fi.maanmittauslaitos.pta.search.metadata;

import static org.junit.Assert.*;

import org.junit.Test;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;

public class ISOMetadataExtractor_IdTest extends BaseMetadataExtractorTest {

	@Test
	public void testMaastotietokantaId() throws Exception {
		Document document = createMaastotietokantaDocument();
		
		String value = document.getValue(ISOMetadataFields.ID, String.class);
		assertEquals("ddad3347-05ca-401a-b746-d883d4110180", value);
	}


	@Test
	public void testStatFiWFSId() throws Exception {
		Document document = createStatFiWFS();
		
		String value = document.getValue(ISOMetadataFields.ID, String.class);
		assertEquals("c3c05280-b1cd-4ae6-9c1a-26a8d9f7201d", value);
	}
	
	@Test
	public void testLukeAineistosarjaID() throws Exception {
		Document document = createLukeTietoaineistosarja();
		
		String value = document.getValue(ISOMetadataFields.ID, String.class);
		assertEquals("2e5565ff-f17f-42a5-9435-d6353f2db46f", value);
	}
	
	@Test
	public void testLukeAineistosarjaID_fromCSW() throws Exception {
		Document document = createLukeTietoaineistosarja_fromCSW();
		
		String value = document.getValue(ISOMetadataFields.ID, String.class);
		assertEquals("2e5565ff-f17f-42a5-9435-d6353f2db46f", value);
	}

}
