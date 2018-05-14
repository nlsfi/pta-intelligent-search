package fi.maanmittauslaitos.pta.search.metadata;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;

public class ISOMetadataExtractor_KeywordsTest extends BaseMetadataExtractorTest {

	@Test
	public void testMaastotietokantaTopicCategories() throws Exception {
		Document document = createMaastotietokantaDocument();
		
		List<String> categories = document.getListValue(ISOMetadataFields.KEYWORDS_ALL,  String.class);
		assertArrayEquals(new String[] {
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
				//"avoindata.fi",
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
			}, categories.toArray());
	}

	@Test
	public void testStatFiWFSTopicCategories() throws Exception {
		Document document = createStatFiWFS();
		
		List<String> categories = document.getListValue(ISOMetadataFields.KEYWORDS_ALL,  String.class);
		System.out.println("categories: "+categories);
		assertArrayEquals(new String[] {
				"Tietokohdepalvelu",
				"infoFeatureAccessService"
			}, categories.toArray());
	}

}
