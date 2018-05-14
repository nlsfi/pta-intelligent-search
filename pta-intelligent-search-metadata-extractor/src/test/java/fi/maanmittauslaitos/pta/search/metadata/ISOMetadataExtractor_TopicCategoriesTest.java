package fi.maanmittauslaitos.pta.search.metadata;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;

public class ISOMetadataExtractor_TopicCategoriesTest extends BaseMetadataExtractorTest {

	@Test
	public void testMaastotietokantaTopicCategories() throws Exception {
		Document document = createMaastotietokantaDocument();
		
		List<String> categories = document.getListValue(ISOMetadataFields.TOPIC_CATEGORIES,  String.class);
		assertArrayEquals(categories.toArray(), new String[] {
				"imageryBaseMapsEarthCover",
				"location",
				"structure",
				"transportation",
				"elevation",
				"boundaries",
				"farming",
				"inlandWaters",
				"environment"
			});
	}

	@Test
	public void testStatFiWFSTopicCategories() throws Exception {
		Document document = createStatFiWFS();
		
		List<String> categories = document.getListValue(ISOMetadataFields.TOPIC_CATEGORIES,  String.class);
		assertArrayEquals(categories.toArray(), new String[] {});
	}

	@Test
	public void testStatFiWFSTopicCategories_modified() throws Exception {
		Document document = createStatFiWFS_modified();
		
		List<String> categories = document.getListValue(ISOMetadataFields.TOPIC_CATEGORIES,  String.class);
		assertArrayEquals(categories.toArray(), new String[] {"location"});
	}

}
