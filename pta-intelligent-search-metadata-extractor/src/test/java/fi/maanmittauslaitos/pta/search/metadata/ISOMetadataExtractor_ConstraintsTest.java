package fi.maanmittauslaitos.pta.search.metadata;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ISOMetadataExtractor_ConstraintsTest extends BaseMetadataExtractorTest {

    @Test
    public void testMaastotietokanta_UseLimitation() throws Exception {
        Document document = createMaastotietokantaDocument();

        String value = document.getValue(ResultMetadataFields.CONSTRAINT_USE_LIMITATION, String.class);
        String str = "Tuote on avointa aineistoa. Avoimen tietoaineiston lisenssi http://www.maanmittauslaitos.fi/maanmittauslaitoksen-avoimen-tietoaineiston-cc-40-lisenssi . \n" +
                "Lisätietoa Avoimien aineistojen hankinta -sivustolta http://www.maanmittauslaitos.fi/avoindata/hankinta.\n" +
                "\n" +
                "Maastotietokannan voi hankkia koko maan kattavana tai alueeltaan rajatuissa osissa. Alueellinen rajaus tapahtuu karttalehtien tai hallinnollisten rajojen perusteella tai asiakkaan omalla rajauksella erikseen tilattaessa. Tiestö osoitteilla on erillinen osaelementti. Maastotietokannan ja Tiestö osoitteilla osaelementin voi ladata Tiedostopalvelusta tai tilata maksullisena toimituksena asiakaspalvelusta. VAROITUS: Ei navigointikäyttöön. Liikennevirasto ei ole tarkistanut tämän tuotteen tietoja, eikä se ota vastuuta tietojen oikeellisuudesta tai valmistuksen jälkeisistä muutoksista.";
        assertEquals(str, value);
    }
    @Test
    public void testMaastotietokanta_Access() throws Exception {
        Document document = createMaastotietokantaDocument();

        String value = document.getValue(ResultMetadataFields.CONSTRAINT_ACCESS, String.class);
        assertEquals("copyright", value);
    }
    @Test
    public void testMaastotietokanta_Other() throws Exception {
        Document document = createMaastotietokantaDocument();

        String value = document.getValue(ResultMetadataFields.CONSTRAINT_OTHER, String.class);
        String str = "Maanmittauslaitoksella on tekijänoikeus ja muut immateriaalioikeudet Maastotietokantaan. Tuotteen ylläpidossa on käytetty kuntien aineistoja. Suomen ympäristökeskuksella ja Liikennevirastolla on tekijänoikeus Maastotietokannan laadinnassa käytettyihin väylätietoihin. Lisäksi Suomen ympäristökeskuksella on tekijänoikeus Maastotietokannan laadinnassa käytettyihin syvyystietoihin. Tietoaineisto ei ole salassa pidettävä. Julkista saatavuutta ei ole rajoitettu INSPIRE-direktiivin nojalla.";
        assertEquals(str, value);
    }
}
