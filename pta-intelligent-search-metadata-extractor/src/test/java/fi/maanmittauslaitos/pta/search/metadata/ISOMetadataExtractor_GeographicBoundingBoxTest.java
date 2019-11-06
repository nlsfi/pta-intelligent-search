package fi.maanmittauslaitos.pta.search.metadata;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;

public class ISOMetadataExtractor_GeographicBoundingBoxTest extends BaseMetadataExtractorTest {

	@Test
	public void testLukeAineistosarjaBbox() throws Exception {
		Document document = createLukeTietoaineistosarja();
		
		List<Double> bbox = document.getValue(ResultMetadataFields.GEOGRAPHIC_BOUNDING_BOX, List.class);
		
		assertEquals(4, bbox.size());
		assertEquals(19.47, bbox.get(0), 0.001);
		assertEquals(59.8,  bbox.get(1), 0.001);
		assertEquals(31.59, bbox.get(2), 0.001);
		assertEquals(70.09, bbox.get(3), 0.001);
	}

	@Test
	public void testYlojarviBbox() throws Exception {
		Document document = createYlojarviAineisto();
		
		List<List> bboxes = document.getListValue(ResultMetadataFields.GEOGRAPHIC_BOUNDING_BOX, List.class);
		
		assertEquals(1, bboxes.size());
		
		List<Double> bbox = bboxes.get(0);
		
		assertEquals(4, bbox.size());
		assertEquals(23.2399, bbox.get(0), 0.001);
		assertEquals(61.5395,  bbox.get(1), 0.001);
		assertEquals(23.7101, bbox.get(2), 0.001);
		assertEquals(61.9007, bbox.get(3), 0.001);
	}

	@Test
	public void testLiikennevirastoAvoinWFS() throws Exception {
		Document document = createLiikennevirastoAvoinWFS();
		
		List<Double> bbox = document.getValue(ResultMetadataFields.GEOGRAPHIC_BOUNDING_BOX, List.class);
		
		assertEquals(4, bbox.size());
		assertEquals(19,   bbox.get(0), 0.01);
		assertEquals(59,   bbox.get(1), 0.01);
		assertEquals(34,   bbox.get(2), 0.01);
		assertEquals(70.5, bbox.get(3), 0.01);
	}

}
