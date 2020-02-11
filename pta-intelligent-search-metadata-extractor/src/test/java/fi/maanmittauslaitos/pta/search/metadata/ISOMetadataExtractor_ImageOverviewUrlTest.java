package fi.maanmittauslaitos.pta.search.metadata;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ISOMetadataExtractor_ImageOverviewUrlTest extends BaseMetadataExtractorTest  {

    @Test
    public void testCreateStatFiWFS() throws Exception {
        Document document = createStatFiWFS();

        String value = document.getValue(ResultMetadataFields.IMAGE_OVERVIEW_URL, String.class);
        assertEquals("http://www.paikkatietohakemisto.fi/geonetwork/srv/api/records/c3c05280-b1cd-4ae6-9c1a-26a8d9f7201d/attachments/TK_logo_s.png", value);
    }

}
