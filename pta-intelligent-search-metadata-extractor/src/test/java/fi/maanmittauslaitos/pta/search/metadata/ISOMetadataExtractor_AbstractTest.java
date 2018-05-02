package fi.maanmittauslaitos.pta.search.metadata;

import static org.junit.Assert.*;

import org.junit.Test;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;

public class ISOMetadataExtractor_AbstractTest extends BaseMetadataExtractorTest {

	@Test
	public void testMaastotietokantaAbstract() throws Exception {
		Document document = createMaastotietokantaDocument();

		assertEquals(1, document.getListValue(ISOMetadataFields.ABSTRACT, String.class).size());

		String titleValue = document.getValue(ISOMetadataFields.ABSTRACT, String.class);
		assertEquals(
				"Maanmittauslaitoksen Maastotietokanta on koko Suomen kattava maastoa kuvaava aineisto. Sen tärkeimpiä kohderyhmiä ovat liikenneverkko, rakennukset ja rakenteet, hallintorajat, nimistö, maankäyttö, vedet ja korkeussuhteet.\n"
						+ "\n"
						+ "Maastotietokannan ajantasaistuksessa käytetään hyväksi ilmakuvia, keilausaineistoja ja muiden tiedon-tuottajien tuottamia aineistoja. Ajantasaistuksessa tehdään tiivistä yhteistyötä kuntien kanssa. Jonkin verran joudutaan turvautumaan myös maastotarkistuksiin lähinnä kohteiden luokituksen osalta.\n"
						+ "\n"
						+ "Maastotietokantaa käytetään muiden karttatuotteiden valmistukseen sekä erilaisissa optimoinneissa.\n"
						+ "\n"
						+ "Tuote on avointa aineistoa. Lisätietoa Avoimien aineistojen hankinta -sivustolta http://www.maanmittauslaitos.fi/kartat-ja-paikkatieto/asiantuntevalle-kayttajalle/maastotiedot-ja-niiden-hankinta",
				titleValue);
	}

	@Test
	public void testMaastotietokantaAbstractSV() throws Exception {
		Document document = createMaastotietokantaDocument();

		assertEquals(1, document.getListValue(ISOMetadataFields.ABSTRACT_SV, String.class).size());

		String titleValue = document.getValue(ISOMetadataFields.ABSTRACT_SV, String.class);
		assertEquals(
				"Lantmäteriverkets Terrängdatabas är ett material som täcker och beskriver terrängen i hela Finland. De viktigaste objekten är trafikledsnätet, byggnader och konstruktioner, de administrativa gränserna, namnbeståndet, markanvändning, vattendrag och höjdförhållanden.\n"
						+ "\n"
						+ "Vid uppdateringen av Terrängdatabasen utnyttjas flygbilder, skanningsmaterial och material som andra dataproducenter producerat. Uppdateringen sker i tätt samarbete med kommunerna. I någon mån behöver man även ta hjälp av kontroller i terrängen främst vad gäller klassificeringen av objekt.\n"
						+ "\n"
						+ "Terrängdatabasen används för produktion av andra kartprodukter samt vid olika typer av optimering.\n"
						+ "\n"
						+ "Produkten ingår i Lantmäteriverkets öppna data. Mera information: Terrängdata och anskaffning av demhttp://www.maanmittauslaitos.fi/sv/kartor-och-geodata/expertanvandare/terrangdata-och-anskaffning-av-dem.",
				titleValue);
	}

	@Test
	public void testMaastotietokantaAbstractEN() throws Exception {
		Document document = createMaastotietokantaDocument();

		assertEquals(1, document.getListValue(ISOMetadataFields.ABSTRACT_EN, String.class).size());

		String titleValue = document.getValue(ISOMetadataFields.ABSTRACT_EN, String.class);
		assertEquals(
				"The Topographic database is a dataset depicting the terrain of all of Finland. The key objects in the Topographic database are the road network, buildings and constructions, administrative borders, geographic names, land use, waterways and elevation.\n"
						+ "\n"
						+ "Aerial photographs, scanning data and data provided by other data providers are utilised in updating the Topographic database. The updating is done in close cooperation with the municipalities. Field checks in the terrain are also needed to some extent, mostly as regards the classification of features.\n"
						+ "\n"
						+ "The topographic database is used in the production of other map products and in various optimisation tasks.\n"
						+ "\n"
						+ "The product belongs to the open data of the National Land Survey of Finland. More information: Topographic data and how to acquire it http://www.maanmittauslaitos.fi/en/maps-and-spatial-data/expert-users/topographic-data-and-how-acquire-it.",
				titleValue);
	}

}
