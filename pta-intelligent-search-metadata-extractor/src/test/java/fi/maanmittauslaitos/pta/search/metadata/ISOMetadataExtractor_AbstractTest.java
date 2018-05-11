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

	@Test
	public void testStatFiWFSAbstract() throws Exception {
		Document document = createStatFiWFS();

		assertEquals(1, document.getListValue(ISOMetadataFields.ABSTRACT, String.class).size());

		String titleValue = document.getValue(ISOMetadataFields.ABSTRACT, String.class);
		assertEquals(
				"Tilastokeskuksen palvelurajapinta on WFS-rajapintapalvelu, jonka kautta on saatavilla seuraavat INSPIRE:n ja paikkatietolain velvoittamat aineistot:\n"
						+ "1) Tilastoyksiköt: Tilastoissa käytetyt aluejaot (kunta, suuralue, maakunta, seutukunta, AVI-alue, ELY-alue)\n"
						+ "2) Yleishyödylliset ja julkiset palvelut: Oppilaitokset (peruskoulut, lukiot ja yhtenäiskoulut)\n"
						+ "3) Tuotanto- ja teollisuuslaitokset: Tuotanto- ja teollisuuslaitokset\n"
						+ "4) Väestöjakauma tilastoissa käytetyillä aluejaoilla. \n" 
						+ "\n"
						+ "Muita rajapinnalla julkaistuja aineistoja\n" + "- Väestöjakauma 5 km x 5 km -ruuduittain\n"
						+ "- Postinumeroalueittainen avoin tieto (Paavo) \n"
						+ "\n"
						+ "Aineistoja hallinnoi Tilastokeskus. Palvelun käyttö on maksutonta eikä vaadi autentikointia eli tunnistautumista käyttäjätunnuksen ja salasanan avulla.\n"
						+ "\n" 
						+ "Tietoja käytettäessä on noudatettava yleisiä käyttöehtoja\n"
						+ "(http://tilastokeskus.fi/org/lainsaadanto/copyright.html).",
				titleValue);
	}

	@Test
	public void testStatFiWFSAbstractSV() throws Exception {
		Document document = createStatFiWFS();

		assertEquals(1, document.getListValue(ISOMetadataFields.ABSTRACT_SV, String.class).size());

		String titleValue = document.getValue(ISOMetadataFields.ABSTRACT_SV, String.class);
		assertEquals(
				"Statistikcentralens servicegränssnitt är gränssnittsservicen WFS. Följande material, som motsvarar förpliktelserna i INSPIRE och i lagen om en infrastruktur för geografisk information, är tillgängligt i servicen:\n"
						+ "\n"
						+ "1) Statistiska enheter: Områdesindelningar i statistiken (kommun, storområde, landskap, ekonomisk region, regionförvaltningsområde, ELY-centralområde samt rutfält 1 km x 1 km\n"
						+ "2) Allmännyttiga och offentliga tjänster: Läroanstalter (grundskolor, gymnasier och enhetsskolor)\n"
						+ "3) Produktions- och industrianläggningar: Produktions- och industrianläggningar\n"
						+ "4) Befolkningsfördelning enligt områdesindelningen i statistiken och per 1 km x 1 km ruta.\n"
						+ "\n" 
						+ "- Befolkningsfördelning per 5 km x 5 km ruta\n"
						+ "- Öppen data efter postnummerområde (Paavo)\n" + "\n"
						+ "Materialet förvaltas av Statistikcentralen. Tjänsten är avgiftsfri och kräver inte autentisering, dvs. identifiering med användaridentifikation och lösenord.\n"
						+ "\n" 
						+ "Då uppgifterna används, ska de allmänna användarvillkoren iakttas\n"
						+ "(http://tilastokeskus.fi/org/lainsaadanto/copyright_sv.html).",
				titleValue);
	}

	@Test
	public void testStatFiWFSAbstractEN() throws Exception {
		Document document = createStatFiWFS();

		assertEquals(1, document.getListValue(ISOMetadataFields.ABSTRACT_EN, String.class).size());

		String titleValue = document.getValue(ISOMetadataFields.ABSTRACT_EN, String.class);
		assertEquals(
				"Statistics Finland's Web Service is a WFS interface service through which the following data required by INSPIRE and national legislation on geographic information are available:\n"
						+ "\n"
						+ "1) Statistical units: Regional divisions (municipality, major region, region, sub-regional unit, Regional State Administrative Agency (AVI), Centre for Economic Development, Transport and the Environment (ELY) and grid 1 km x 1 km\n"
						+ "2) Non-profit and public services: Educational institutions (comprehensive schools, upper secondary general schools)\n"
						+ "3) Production and industrial facilities: Production and industrial facilities\n"
						+ "4) Population distribution by the regional divisions used in statistics and by 1 km x 1 km grids.\n"
						+ "\n" 
						+ "Other data published:\n" + "- Population distribution by 5 km x 5 km grids\n"
						+ "- Open data by postal code area (Paavo)\n"
						+ "\n"
						+ "The data are administered by Statistics Finland. The service is free of charge and does not require authentication or identification with a user ID and password.\n"
						+ "\n" 
						+ "The general Terms of Use must be observed when using the data \n"
						+ "(http://tilastokeskus.fi/org/lainsaadanto/copyright_en.html).",
				titleValue);
	}
}
