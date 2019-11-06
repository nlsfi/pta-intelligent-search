package fi.maanmittauslaitos.pta.search.metadata;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;

public class ISOMetadataExtractor_DistributionFormatsTest extends BaseMetadataExtractorTest {

	@Test
	public void testMaastotietokantaDistributionFormats() throws Exception {
		Document document = createMaastotietokantaDocument();
		
		List<String> distributionFormats = document.getListValue(ResultMetadataFields.DISTRIBUTION_FORMATS,  String.class);
		assertArrayEquals(new String[] {
				"GML", "Mapinfo MIF/MID", "ESRI Shapefile"
			}, distributionFormats.toArray());
	}

	@Test
	public void testStatFiDistributionFormats() throws Exception {
		Document document = createStatFiWFS();
		
		List<String> distributionFormats = document.getListValue(ResultMetadataFields.DISTRIBUTION_FORMATS,  String.class);
		
		assertArrayEquals(new String[] {
			}, distributionFormats.toArray());
	}

	@Test
	public void testLukeAineistosarjaDistributionFormats() throws Exception {
		Document document = createLukeTietoaineistosarja();
		
		List<String> distributionFormats = document.getListValue(ResultMetadataFields.DISTRIBUTION_FORMATS,  String.class);
		assertArrayEquals(new String[] {
				"Unknown"
			}, distributionFormats.toArray());
	}
	
	@Test
	public void testLukeAineistosarjaDistributionFormats_fromCSW() throws Exception {
		Document document = createLukeTietoaineistosarja_fromCSW();
		
		List<String> distributionFormats = document.getListValue(ResultMetadataFields.DISTRIBUTION_FORMATS,  String.class);
		assertArrayEquals(new String[] {
				"Unknown"
			}, distributionFormats.toArray());
	}
}
