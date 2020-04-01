package fi.maanmittauslaitos.pta.search.metadata;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ISOMetadataExtractor_ClassificationTest extends BaseMetadataExtractorTest {


    @Test
    public void testMaastotietokanta_classification() throws Exception {
        Document document = createMaastotietokantaDocument();

        String value = document.getValue(ResultMetadataFields.CLASSIFICATION, String.class);
        assertEquals("unclassified", value);

    }
}
