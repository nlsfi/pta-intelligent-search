package fi.maanmittauslaitos.pta.search.metadata;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ISOMetadataExtractor_SpatialDataTest extends BaseMetadataExtractorTest {

    //
    // CRS
    //

    @Test
    public void testMaastotietokanta_CrsCode() throws Exception {
        Document document = createMaastotietokantaDocument();

        String value = document.getValue(ResultMetadataFields.CRS_CODE, String.class);
        assertEquals("ETRS89 / TM35FIN(E,N) (EPSG:3067)", value);
    }

    @Test
    public void testMaastotietokanta_CrsCodeSpace() throws Exception {
        Document document = createMaastotietokantaDocument();

        String value = document.getValue(ResultMetadataFields.CRS_CODE_SPACE, String.class);
        assertEquals("EPSG", value);
    }

    @Test
    public void testMaastotietokanta_CrsVersion() throws Exception {
        Document document = createMaastotietokantaDocument();

        String value = document.getValue(ResultMetadataFields.CRS_VERSION, String.class);
        assertEquals("8.6", value);
    }

    @Test
    public void testMaastotietokanta_CrsScaleData() throws Exception {
        Document document = createMaastotietokantaDocument();

        String value = document.getValue(ResultMetadataFields.SCALE_DENOMINATOR, String.class);
        assertEquals("10000", value);
    }

    //
    // Geo bbox + extent
    // These are duplicates in a sense, mb fix/merge these?

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

    @Test
    public void testMaastotietokanta_ExtentNorth() throws Exception {
        Document document = createMaastotietokantaDocument();

        String value = document.getValue(ResultMetadataFields.EXTENT_NORTH_BOUND, String.class);
        assertEquals("70.09229553", value);
    }
    @Test
    public void testMaastotietokanta_ExtentSouth() throws Exception {
        Document document = createMaastotietokantaDocument();

        String value = document.getValue(ResultMetadataFields.EXTENT_SOUTH_BOUND, String.class);
        assertEquals("59.45414258", value);
    }
    @Test
    public void testMaastotietokanta_ExtentEast() throws Exception {
        Document document = createMaastotietokantaDocument();

        String value = document.getValue(ResultMetadataFields.EXTENT_EAST_BOUND, String.class);
        assertEquals("31.58672881", value);
    }
    @Test
    public void testMaastotietokanta_ExtentWest() throws Exception {
        Document document = createMaastotietokantaDocument();

        String value = document.getValue(ResultMetadataFields.EXTENT_WEST_BOUND, String.class);
        assertEquals("19.08317359", value);
    }


}
