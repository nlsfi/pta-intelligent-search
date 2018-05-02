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

}
