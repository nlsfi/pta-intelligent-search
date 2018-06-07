package fi.maanmittauslaitos.pta.search.metadata;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.metadata.model.ResponsibleParty;

public class ISOMetadataExtractor_OrganisationTest extends BaseMetadataExtractorTest {

	@Test
	public void testMaastotietokantaOrganisations() throws Exception {
		Document document = createMaastotietokantaDocument();
		
		List<ResponsibleParty> organisations = document.getListValue(ISOMetadataFields.ORGANISATIONS, ResponsibleParty.class);

		assertEquals(1, organisations.size());
		ResponsibleParty party = organisations.get(0);
		
		assertEquals("Maanmittauslaitos", party.getOrganisationName());
		assertEquals("owner", party.getIsoRole());
		
		assertEquals(2, party.getLocalisedOrganisationName().size());
		assertEquals("National Land Survey of Finland", party.getLocalisedOrganisationName().get("EN"));
		assertEquals("Lantmäteriverket", party.getLocalisedOrganisationName().get("SV"));
	}


	@Test
	public void testStatFiWFSOrganisations() throws Exception {
		Document document = createStatFiWFS();
		
		List<ResponsibleParty> organisations = document.getListValue(ISOMetadataFields.ORGANISATIONS, ResponsibleParty.class);

		assertEquals(1, organisations.size());
		ResponsibleParty party = organisations.get(0);
		
		assertEquals("Tilastokeskus", party.getOrganisationName());
		assertEquals("pointOfContact", party.getIsoRole());
		
		assertEquals(1, party.getLocalisedOrganisationName().size());
		assertEquals("Statistics Finland", party.getLocalisedOrganisationName().get("EN"));
	}
	

	@Test
	public void testStatFiWFSModifiedOrganisations() throws Exception {
		Document document = createStatFiWFS_modified();
		
		List<ResponsibleParty> organisations = document.getListValue(ISOMetadataFields.ORGANISATIONS, ResponsibleParty.class);

		assertEquals(2, organisations.size());
		ResponsibleParty party1 = organisations.get(0);
		
		assertEquals("Tilastokeskus", party1.getOrganisationName());
		assertEquals("pointOfContact", party1.getIsoRole());

		assertEquals(1, party1.getLocalisedOrganisationName().size());
		assertEquals("Statistics Finland", party1.getLocalisedOrganisationName().get("EN"));
		
		ResponsibleParty party2 = organisations.get(1);
		
		assertEquals("X-Tilastokeskus", party2.getOrganisationName());
		assertEquals("owner", party2.getIsoRole());

		assertEquals(1, party2.getLocalisedOrganisationName().size());
		assertEquals("Statistikcentralen", party2.getLocalisedOrganisationName().get("SV"));
	}

	@Test
	public void testLukeTietoaineistosarjaOrganisations() throws Exception {
		Document document = createLukeTietoaineistosarja();
		
		List<ResponsibleParty> organisations = document.getListValue(ISOMetadataFields.ORGANISATIONS, ResponsibleParty.class);

		assertEquals(1, organisations.size());
		ResponsibleParty party = organisations.get(0);
		
		assertEquals("Luonnonvarakeskus (Luke)", party.getOrganisationName());
		assertEquals("pointOfContact", party.getIsoRole());
		
		assertEquals(1, party.getLocalisedOrganisationName().size());
		assertEquals("Natural Resources Institute Finland (Luke)", party.getLocalisedOrganisationName().get("EN"));
	}
	

	@Test
	public void testLukeTietoaineistosarjaOrganisations_fromCSW() throws Exception {
		Document document = createLukeTietoaineistosarja_fromCSW();
		
		List<ResponsibleParty> organisations = document.getListValue(ISOMetadataFields.ORGANISATIONS, ResponsibleParty.class);

		assertEquals(1, organisations.size());
		ResponsibleParty party = organisations.get(0);
		
		assertEquals("Luonnonvarakeskus (Luke)", party.getOrganisationName());
		assertEquals("pointOfContact", party.getIsoRole());

		assertEquals(1, party.getLocalisedOrganisationName().size());
		assertEquals("Natural Resources Institute Finland (Luke)", party.getLocalisedOrganisationName().get("EN"));
	}
}
