package fi.maanmittauslaitos.pta.search.metadata;

import static org.junit.Assert.*;

import org.junit.Test;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;

public class ISOMetadataExtractor_IdTest extends BaseMetadataExtractorTest {

	@Test
	public void testMaastotietokantaId() throws Exception {
		Document document = createMaastotietokantaDocument();
		
		String titleValue = document.getValue(ISOMetadataFields.ID, String.class);
		assertEquals("ddad3347-05ca-401a-b746-d883d4110180", titleValue);
	}


	@Test
	public void testStatFiWFSId() throws Exception {
		Document document = createStatFiWFS();
		
		String titleValue = document.getValue(ISOMetadataFields.ID, String.class);
		assertEquals("c3c05280-b1cd-4ae6-9c1a-26a8d9f7201d", titleValue);
	}

}
