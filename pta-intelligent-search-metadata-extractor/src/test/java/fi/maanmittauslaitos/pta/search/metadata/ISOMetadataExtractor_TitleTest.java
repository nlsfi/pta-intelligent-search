package fi.maanmittauslaitos.pta.search.metadata;

import static org.junit.Assert.*;

import org.junit.Test;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;

public class ISOMetadataExtractor_TitleTest extends BaseMetadataExtractorTest {
	
	@Test
	public void testMaastotietokantaTitle() throws Exception {
		Document document = createMaastotietokantaDocument();
		
		String titleValue = document.getValue(ISOMetadataFields.TITLE, String.class);
		assertEquals("Maastotietokanta", titleValue);
	}


	@Test
	public void testMaastotietokantaTitleSV() throws Exception {
		Document document = createMaastotietokantaDocument();
		
		String titleValue = document.getValue(ISOMetadataFields.TITLE_SV, String.class);
		assertEquals("Terrängdatabas", titleValue);
	}


	@Test
	public void testMaastotietokantaTitleEN() throws Exception {
		Document document = createMaastotietokantaDocument();
		
		String titleValue = document.getValue(ISOMetadataFields.TITLE_EN, String.class);
		assertEquals("Topographic Database", titleValue);
	}

}
