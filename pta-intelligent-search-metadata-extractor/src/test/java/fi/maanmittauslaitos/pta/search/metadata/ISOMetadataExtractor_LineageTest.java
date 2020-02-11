package fi.maanmittauslaitos.pta.search.metadata;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ISOMetadataExtractor_LineageTest extends BaseMetadataExtractorTest  {

    @Test
    public void testCreateMaastotietokanta() throws Exception {
        Document document = createMaastotietokantaDocument();

        String value = document.getValue(ResultMetadataFields.LINEAGE, String.class);
        String lin = "Maastotietokanta on Maanmittauslaitoksen sijainniltaan tarkin valtakunnallinen maastoa kuvaava aineisto. Sen sijaintitietojen tarkkuus vastaa mittakaavaa 1:5 000 - 1:10 000.\n" +
                "\n" +
                "Maastotietojen laatumallissa kerrotaan, mistä tekijöistä numeeristen maastotietojen laatu muodostuu ja miten eri laatutekijöitä mitataan. Lisäksi laatumallissa annetaan laatuvaatimukset mm. maastotietojen sijaintitarkkuudelle, ajantasaisuudelle, kuvailevalle ominaisuustiedolle ja kattavuudelle. Laatuvaatimuksia ei aseteta geometria- ja topologiatiedoille. \n" +
                "\n" +
                "Maastotietokannan eri kohteilla on erilaiset sijaintitarkkuusvaatimukset, useimmilla rakennetuilla kohteilla tarkkuusvaatimus on 3 m. Maastotietokannan kohteiden sijaintitarkkuus on 95 %:n todennäköisyydellä laatumallissa määritellyn minimitarkkuuden mukainen.\n" +
                "\n" +
                "Maastotietokannassa ajantasaisuus vaihtelee kohteittain (ks. ylläpitotiedot). \n" +
                "\n" +
                "Kuvailevan tiedon (ominaisuustietojen) tarkkuus vaihtelee kohteittain. Kts. tarkemmat tiedot teemoittaisista kuvauksista.\n" +
                "\n" +
                "Kattavuuden suuruudella tarkoitetaan sellaisten kohteiden osuutta, jotka puuttuvat tietokannasta tai jotka ovat tietokannassa mutta niitä ei ole maastossa. Eri kohteilla on erilaiset kattavuusvaatimukset.\n" +
                "\n" +
                "Laatumalli on osoitteessa:\n" +
                "http://www.maanmittauslaitos.fi/sites/default/files/Maastotietojen_laatumalli.pdf";
        assertEquals(lin, value);
    }
}
