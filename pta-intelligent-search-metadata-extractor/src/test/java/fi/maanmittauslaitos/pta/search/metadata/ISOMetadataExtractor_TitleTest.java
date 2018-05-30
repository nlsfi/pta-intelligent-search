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
	
	
	@Test
	public void testLukeAineistosarjatTitle() throws Exception {
		Document document = createLukeTietoaineistosarja();
		
		String titleValue = document.getValue(ISOMetadataFields.TITLE, String.class);
		assertEquals("Monilähteisen valtakunnan metsien inventoinnin (MVMI) kartta-aineisto 2009", titleValue);
	}
	
	@Test
	public void testLukeAineistosarjatTitleSV() throws Exception {
		Document document = createLukeTietoaineistosarja();
		
		String titleValue = document.getValue(ISOMetadataFields.TITLE_SV, String.class);
		assertNull(titleValue);
	}
	
	@Test
	public void testLukeAineistosarjatTitleEN() throws Exception {
		Document document = createLukeTietoaineistosarja();
		
		String titleValue = document.getValue(ISOMetadataFields.TITLE_EN, String.class);
		assertEquals("Multi-source national forest inventory (MS-NFI) raster maps of 2009", titleValue);
	}
	
	@Test
	public void testLukeAineistosarjatTitle_fromCSW() throws Exception {
		Document document = createLukeTietoaineistosarja_fromCSW();
		
		String titleValue = document.getValue(ISOMetadataFields.TITLE, String.class);
		assertEquals("Monilähteisen valtakunnan metsien inventoinnin (MVMI) kartta-aineisto 2009", titleValue);
	}
	
	@Test
	public void testLukeAineistosarjatTitleSV_fromCSW() throws Exception {
		Document document = createLukeTietoaineistosarja_fromCSW();
		
		String titleValue = document.getValue(ISOMetadataFields.TITLE_SV, String.class);
		assertNull(titleValue);
	}
	
	@Test
	public void testLukeAineistosarjatTitleEN_fromCSW() throws Exception {
		Document document = createLukeTietoaineistosarja_fromCSW();
		
		String titleValue = document.getValue(ISOMetadataFields.TITLE_EN, String.class);
		assertEquals("Multi-source national forest inventory (MS-NFI) raster maps of 2009", titleValue);
	}
}
