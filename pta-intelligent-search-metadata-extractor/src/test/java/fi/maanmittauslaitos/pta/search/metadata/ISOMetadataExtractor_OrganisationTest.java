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
	}


	@Test
	public void testStatFiWFSOrganisations() throws Exception {
		Document document = createStatFiWFS();
		
		List<ResponsibleParty> organisations = document.getListValue(ISOMetadataFields.ORGANISATIONS, ResponsibleParty.class);

		assertEquals(1, organisations.size());
		ResponsibleParty party = organisations.get(0);
		
		assertEquals("Tilastokeskus", party.getOrganisationName());
		assertEquals("pointOfContact", party.getIsoRole());
	}
	

	@Test
	public void testStatFiWFSModifiedOrganisations() throws Exception {
		Document document = createStatFiWFS_modified();
		
		List<ResponsibleParty> organisations = document.getListValue(ISOMetadataFields.ORGANISATIONS, ResponsibleParty.class);

		assertEquals(2, organisations.size());
		ResponsibleParty party1 = organisations.get(0);
		
		assertEquals("Tilastokeskus", party1.getOrganisationName());
		assertEquals("pointOfContact", party1.getIsoRole());

		ResponsibleParty party2 = organisations.get(1);
		
		assertEquals("X-Tilastokeskus", party2.getOrganisationName());
		assertEquals("owner", party2.getIsoRole());
	}

	@Test
	public void testLukeTietoaineistosarjaOrganisations() throws Exception {
		Document document = createLukeTietoaineistosarja();
		
		List<ResponsibleParty> organisations = document.getListValue(ISOMetadataFields.ORGANISATIONS, ResponsibleParty.class);

		assertEquals(1, organisations.size());
		ResponsibleParty party = organisations.get(0);
		
		assertEquals("Luonnonvarakeskus (Luke)", party.getOrganisationName());
		assertEquals("pointOfContact", party.getIsoRole());
	}
	

	@Test
	public void testLukeTietoaineistosarjaOrganisations_fromCSW() throws Exception {
		Document document = createLukeTietoaineistosarja_fromCSW();
		
		List<ResponsibleParty> organisations = document.getListValue(ISOMetadataFields.ORGANISATIONS, ResponsibleParty.class);

		assertEquals(1, organisations.size());
		ResponsibleParty party = organisations.get(0);
		
		assertEquals("Luonnonvarakeskus (Luke)", party.getOrganisationName());
		assertEquals("pointOfContact", party.getIsoRole());
	}
}
