package fi.maanmittauslaitos.pta.search.metadata;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.metadata.model.CodeListValue;
import fi.maanmittauslaitos.pta.search.metadata.model.MetadataDate;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.Assert.assertEquals;

public class ISOMetadataExtractor_DatesTest extends BaseMetadataExtractorTest {

    @Test
    public void testMaastotietokantaDatestamp_String() throws Exception {
        Document document = createMaastotietokantaDocument();

        String value = document.getValue(ResultMetadataFields.DATESTAMP, String.class);
        assertEquals("2018-04-03T11:03:13", value);
    }

    @Test
    public void testMaastotietokantaDatestamp_LocalDateTime() throws Exception {
        Document document = createMaastotietokantaDocument();

        LocalDateTime expected = LocalDateTime.of(2018, 4, 3, 11, 3, 13);

        LocalDateTime value = document.getValue(ResultMetadataFields.DATESTAMP, LocalDateTime.class);
        assertEquals(expected, value);
    }


    @Test
    public void testStatFiWFSDatestamp_String() throws Exception {
        Document document = createStatFiWFS();

        String value = document.getValue(ResultMetadataFields.DATESTAMP, String.class);
        assertEquals("2018-03-16T10:20:21", value);
    }


    @Test
    public void testStatFiWFSDatestamp_Datestamp() throws Exception {
        Document document = createStatFiWFS();

        LocalDateTime expected = LocalDateTime.of(2018, 3, 16, 10, 20, 21);

        LocalDateTime value = document.getValue(ResultMetadataFields.DATESTAMP, LocalDateTime.class);
        assertEquals(expected, value);
    }

    @Test
    public void testStatFiWFSModifiedDatestamp_String() throws Exception {
        Document document = createStatFiWFS_modified();

        String value = document.getValue(ResultMetadataFields.DATESTAMP, String.class);
        assertEquals("2018-03-16T10:20:21-03:00", value);
    }

    @Test
    public void testStatFiWFSModifiedDatestamp_OffsetDatestamp() throws Exception {
        Document document = createStatFiWFS_modified();

        ZoneOffset offset = ZoneOffset.ofHours(-3);

        OffsetDateTime expected = OffsetDateTime.of(2018, 3, 16, 10, 20, 21, 0, offset);
        OffsetDateTime value = document.getValue(ResultMetadataFields.DATESTAMP, OffsetDateTime.class);
        assertEquals(expected, value);
    }


    @Test
    public void testLukeAineistosarjaDatestamp() throws Exception {
        Document document = createLukeTietoaineistosarja();

        LocalDateTime expected = LocalDateTime.of(2018, 1, 23, 14, 52, 17, 0);
        LocalDateTime value = document.getValue(ResultMetadataFields.DATESTAMP, LocalDateTime.class);
        assertEquals(expected, value);
    }


    @Test
    public void testLukeAineistosarjaDatestamp_fromCSW() throws Exception {
        Document document = createLukeTietoaineistosarja_fromCSW();

        LocalDateTime expected = LocalDateTime.of(2018, 1, 23, 14, 52, 17, 0);
        LocalDateTime value = document.getValue(ResultMetadataFields.DATESTAMP, LocalDateTime.class);
        assertEquals(expected, value);
    }

    @Test
    public void testCreateMaastotietokanta_DatePublished() throws Exception {
        Document document = createMaastotietokantaDocument();

        String value = document.getValue(ResultMetadataFields.DATE_PUBLISHED, String.class);
        assertEquals("2017-03-08", value);
    }

    @Test
    public void testCreateMaastotietokanta_DateIdentificationInfo() throws Exception {
        Document document = createMaastotietokantaDocument();

        MetadataDate value = document.getValue(ResultMetadataFields.DATE_IDENTIFICATION_INFO, MetadataDate.class);
        CodeListValue codeListValue = value.getType();
        assertEquals("revision", codeListValue.getValue());
        assertEquals("http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode", codeListValue.getList());
        assertEquals("gmd:CI_DateTypeCode", codeListValue.getType());
        assertEquals("2017-03-08", value.getDate());
    }


    @Test
    public void testCreateMaastotietokanta_MaintenanceFrequency() throws Exception {
        Document document = createMaastotietokantaDocument();

        CodeListValue value = document.getValue(ResultMetadataFields.MAINTENANCE_FREQUENCY, CodeListValue.class);
        assertEquals("continual", value.getValue());
        assertEquals("http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_MaintenanceFrequencyCode", value.getList());
        assertEquals("gmd:MD_MaintenanceFrequencyCode", value.getType());
    }
}
