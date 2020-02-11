package fi.maanmittauslaitos.pta.search.metadata;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ISOMetadataExtractor_ResourceIdTest extends BaseMetadataExtractorTest  {

    @Test
    public void testCreateMaastotietokanta() throws Exception {
        Document document = createMaastotietokantaDocument();

        String value = document.getValue(ResultMetadataFields.RESOURCE_ID, String.class);
        assertEquals("1000007", value);
    }
}

