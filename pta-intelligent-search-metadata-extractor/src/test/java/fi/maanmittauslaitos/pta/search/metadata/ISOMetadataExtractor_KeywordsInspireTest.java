package fi.maanmittauslaitos.pta.search.metadata;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;

public class ISOMetadataExtractor_KeywordsInspireTest extends BaseMetadataExtractorTest {

	@Test
	public void testMaastotietokantaTopicCategories() throws Exception {
		Document document = createMaastotietokantaDocument();
		
		List<String> keywords = document.getListValue(ISOMetadataFields.KEYWORDS_INSPIRE,  String.class);
		assertArrayEquals(new String[] {
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
	public void testStatFiWFSTopicCategories() throws Exception {
		Document document = createStatFiWFS();
		
		List<String> keywords = document.getListValue(ISOMetadataFields.KEYWORDS_INSPIRE,  String.class);
		
		assertArrayEquals(new String[] { }, keywords.toArray());
	}

}
