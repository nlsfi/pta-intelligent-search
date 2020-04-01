package fi.maanmittauslaitos.pta.search.metadata;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ISOMetadataExtractor_LanguagesTest extends BaseMetadataExtractorTest {

    @Test
    public void testCreateLukeTietoaineistosarja_LanguageMetadata() throws Exception {
        Document document = createLukeTietoaineistosarja_fromCSW();

        String value = document.getValue(ResultMetadataFields.LANGUAGE_METADATA, String.class);
        assertEquals("fin", value);
    }

    @Test
    public void testCreateMaastotietokanta_LanguageResource() throws Exception {
        Document document = createMaastotietokantaDocument();

        List<String> langs = document.getListValue(ResultMetadataFields.LANGUAGE_RESOURCE, String.class);
        assertArrayEquals(new String[]{
            "fin",
            "swe",
            "sme",
            "smn",
            "sms"
        }, langs.toArray());
    }
}
