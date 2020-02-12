package fi.maanmittauslaitos.pta.search.metadata.json;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.metadata.ResultMetadataFields;
import fi.maanmittauslaitos.pta.search.metadata.model.MetadataDownloadLink;
import fi.maanmittauslaitos.pta.search.metadata.model.ResponsibleParty;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class CKANMetadataExtractorsExtendedTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();
    private DocumentProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new CKANMetadataExtractorConfigurationFactory().createMetadataDocumentProcessor();
    }

    private Document createDocument(String path) throws DocumentProcessingException {
        return processor.processDocument(this.getClass().getClassLoader().getResourceAsStream(path));
    }

    @Test
    public void testResourceDownloadLinkParsing() throws DocumentProcessingException {
        Document doc = createDocument("test_ckan_dataset_1.json");
        List<MetadataDownloadLink> links = doc.getListValue(ResultMetadataFields.DOWNLOAD_LINKS, MetadataDownloadLink.class);

        softly.assertThat(links.size()).isEqualTo(1);

        MetadataDownloadLink link = links.get(0);
        softly.assertThat(link.getDesc()).isEqualTo("Description of dataset 1");
        softly.assertThat(link.getProtocol()).isEqualTo("https");
        softly.assertThat(link.getTitle()).isEqualTo("dataset-1 name");
        softly.assertThat(link.getUrl()).isEqualTo("https://ckan.test.fi/fi/dataset/service-1-id/resource/dataset-1-id/download/test.xlsx");

    }

    @Test
    public void testResourceCreationDateParsing() throws Exception {
        Document doc = createDocument("test_ckan_dataset_1.json");

        String date = doc.getValue(ResultMetadataFields.CKAN_CREATION_DATE, String.class);

        softly.assertThat(date).isEqualTo("2019-10-01T07:25:06");

    }

    @Test
    public void testServiceCreationDateParsing() throws Exception {
        Document doc = createDocument("test_ckan_service_1.json");

        String date = doc.getValue(ResultMetadataFields.CKAN_CREATION_DATE, String.class);

        softly.assertThat(date).isEqualTo("2019-10-01T07:21:44");

    }

    @Test
    public void testServiceOrgResourceParsing() throws Exception {
        Document doc = createDocument("test_ckan_service_1.json");

        List<ResponsibleParty> responsibleParties = doc.getListValue(ResultMetadataFields.ORGANISATIONS_RESOURCE, ResponsibleParty.class);
        softly.assertThat(responsibleParties.size()).isEqualTo(1);

        ResponsibleParty party = responsibleParties.get(0);

        softly.assertThat(party.getEmail().get(0)).isEqualTo("test.test@test.fi");
        softly.assertThat(party.getPartyName()).isEqualTo("ORGANIZATION");
        softly.assertThat(party.getIsoRole()).isEqualTo("owner");

        Map<String, String> localizedNames = party.getLocalizedPartyName();
        softly.assertThat(localizedNames.size()).isEqualTo(1);
        softly.assertThat(localizedNames.get("FI")).isEqualTo("ORGANIZATION");
    }

    @Test
    public void testServiceOrgMetadataParsing() throws Exception {
        Document doc = createDocument("test_ckan_service_1.json");

        List<ResponsibleParty> responsibleParties = doc.getListValue(ResultMetadataFields.ORGANISATIONS_METADATA, ResponsibleParty.class);
        softly.assertThat(responsibleParties.size()).isEqualTo(1);

        ResponsibleParty party = responsibleParties.get(0);

        softly.assertThat(party.getEmail().get(0)).isEqualTo("author@test.fi");
        softly.assertThat(party.getPartyName()).isEqualTo("test");
        softly.assertThat(party.getIsoRole()).isEqualTo("owner");

        Map<String, String> localizedNames = party.getLocalizedPartyName();
        softly.assertThat(localizedNames.size()).isEqualTo(1);
        softly.assertThat(localizedNames.get("FI")).isEqualTo("test");
    }

    @Test
    public void testServiceOrgOtherParsing() throws Exception {
        Document doc = createDocument("test_ckan_service_1.json");

        List<ResponsibleParty> responsibleParties = doc.getListValue(ResultMetadataFields.ORGANISATIONS_OTHER, ResponsibleParty.class);

        List<String> orgs = Arrays.asList(
                "ELY",
                "KEHA",
                "EA",
                "LUKE",
                "TK",
                "TUKES",
                "VTT"
        );


        softly.assertThat(responsibleParties.size()).isEqualTo(orgs.size());

        IntStream.range(0, orgs.size()).forEach(i -> {
            ResponsibleParty party = responsibleParties.get(i);

            softly.assertThat(party.getEmail().isEmpty()).isTrue();
            softly.assertThat(party.getPartyName()).isEqualTo(orgs.get(i));
            softly.assertThat(party.getIsoRole()).isNullOrEmpty();
            Map<String, String> localizedNames = party.getLocalizedPartyName();
            softly.assertThat(localizedNames.size()).isEqualTo(1);
            softly.assertThat(localizedNames.get("FI")).isEqualTo(orgs.get(i));
        });



    }
}
