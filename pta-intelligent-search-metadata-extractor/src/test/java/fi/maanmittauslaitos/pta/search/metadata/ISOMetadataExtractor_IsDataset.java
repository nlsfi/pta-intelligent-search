package fi.maanmittauslaitos.pta.search.metadata;

import static org.junit.Assert.*;

import org.junit.Test;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;

public class ISOMetadataExtractor_IsDataset extends BaseMetadataExtractorTest {

	@Test
	public void testMaastotietokantaIsDataset() throws Exception {
		Document document = createMaastotietokantaDocument();
		
		Boolean isDataset = document.getValue(ISOMetadataFields.IS_DATASET, Boolean.class);
		assertEquals(Boolean.TRUE, isDataset);
	}


	@Test
	public void testStatFiWFSIsNotDataset() throws Exception {
		Document document = createStatFiWFS();
		
		Boolean isDataset = document.getValue(ISOMetadataFields.IS_DATASET, Boolean.class);
		assertEquals(Boolean.FALSE, isDataset);
	}

}
