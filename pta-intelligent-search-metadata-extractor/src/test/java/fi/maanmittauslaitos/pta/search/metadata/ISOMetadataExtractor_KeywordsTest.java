package fi.maanmittauslaitos.pta.search.metadata;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;

public class ISOMetadataExtractor_KeywordsTest extends BaseMetadataExtractorTest {

	@Test
	public void testMaastotietokantaKeywordsAll() throws Exception {
		Document document = createMaastotietokantaDocument();
		
		
		List<String> keywords = document.getListValue(ISOMetadataFields.KEYWORDS_ALL,  String.class);
		assertArrayEquals(new String[] {
				// Note that avoindata.fi is not here since it's read into it's own field
				"Rataverkko",
				"Tieliikenneverkko",
				"Ympäristö",
				"Vesistöt",
				"Taajamat",
				"Rakennukset",
				"Johtoverkot",
				"Paikannimet",
				"Maanpeite",
				"Osoitteet",
				"Hallinnolliset rajat",
				"Korkeus",
				"Suojelukohteet",
				"Väylät",
				"Vesirakentaminen",
				"Maankäyttö",
				
				"Rakennukset",
				"Hydrografia",
				"Korkeus",
				"Hallinnolliset yksiköt",
				"Liikenneverkot",
				"Tuotanto- ja teollisuuslaitokset",
				"Yleishyödylliset ja muut julkiset palvelut",
				"Maankäyttö",
				"Maanpeite",
				"Ortoilmakuvat"
			}, keywords.toArray());
	}

	@Test
	public void testStatFiWFSKeywordsAll() throws Exception {
		Document document = createStatFiWFS();
		
		List<String> keywords = document.getListValue(ISOMetadataFields.KEYWORDS_ALL,  String.class);
		
		assertArrayEquals(new String[] {
				"Tietokohdepalvelu",
				"infoFeatureAccessService"
			}, keywords.toArray());
	}
	
	@Test
	public void testLukeAineistosarjaKeywordsAll() throws Exception {
		Document document = createLukeTietoaineistosarja();
		
		List<String> keywords = document.getListValue(ISOMetadataFields.KEYWORDS_ALL,  String.class);
		
		assertArrayEquals(new String[] {
				"Maanpeite",
				"Maankäyttö",
				"Energiavarat",
				
				"luonnonvarat",
				"metsävarat",
				"metsävarojen arviointi",
				"biomassa",
				"puutavaralaji",
				
				"metsätalous",
				"kasvupaikat",
				"elinympäristöt"
			}, keywords.toArray());
	}

	@Test
	public void testLukeAineistosarjaKeywordsAll_fromCSW() throws Exception {
		Document document = createLukeTietoaineistosarja_fromCSW();
		
		List<String> keywords = document.getListValue(ISOMetadataFields.KEYWORDS_ALL,  String.class);
		
		assertArrayEquals(new String[] {
				"Maanpeite",
				"Maankäyttö",
				"Energiavarat",
				
				"luonnonvarat",
				"metsävarat",
				"metsävarojen arviointi",
				"biomassa",
				"puutavaralaji",
				
				"metsätalous",
				"kasvupaikat",
				"elinympäristöt"
			}, keywords.toArray());
	}

}
