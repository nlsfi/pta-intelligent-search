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


	@Test
	public void testStatFiWFSTitle() throws Exception {
		Document document = createStatFiWFS();
		
		String titleValue = document.getValue(ISOMetadataFields.TITLE, String.class);
		assertEquals("Tilastokeskuksen palvelurajapinta (WFS)", titleValue);
	}


	@Test
	public void testStatFiWFSTitleSV() throws Exception {
		Document document = createStatFiWFS();
		
		String titleValue = document.getValue(ISOMetadataFields.TITLE_SV, String.class);
		assertEquals("Statistikcentralens gränssnittservicen (WFS)", titleValue);
	}


	@Test
	public void testStatFiWFSTitleEN() throws Exception {
		Document document = createStatFiWFS();
		
		String titleValue = document.getValue(ISOMetadataFields.TITLE_EN, String.class);
		assertEquals("Statistics Finland's Web Service (WFS)", titleValue);
	}
}
