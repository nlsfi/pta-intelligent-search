package fi.maanmittauslaitos.pta.search.metadata;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfigurationImpl;
import fi.maanmittauslaitos.pta.search.metadata.model.ResponsibleParty;
import fi.maanmittauslaitos.pta.search.metadata.model.TextRewriter;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ISOMetadataExtractor_OrganisationTest extends BaseMetadataExtractorTest {

	@Test
	public void testMaastotietokantaOrganisations() throws Exception {
		Document document = createMaastotietokantaDocument();
		
		List<ResponsibleParty> organisations = document.getListValue(ResultMetadataFields.ORGANISATIONS, ResponsibleParty.class);

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
		
		List<ResponsibleParty> organisations = document.getListValue(ResultMetadataFields.ORGANISATIONS, ResponsibleParty.class);

		assertEquals(2, organisations.size());
		ResponsibleParty party1 = organisations.get(0);
		
		assertEquals("Tilastokeskus", party1.getOrganisationName());
		assertEquals("originator", party1.getIsoRole());
		
		assertEquals(0, party1.getLocalisedOrganisationName().size());
		
		ResponsibleParty party2 = organisations.get(1);
		
		assertEquals("Tilastokeskus", party2.getOrganisationName());
		assertEquals("publisher", party2.getIsoRole());
		
		assertEquals(0, party2.getLocalisedOrganisationName().size());
	}
	

	@Test
	public void testStatFiWFSModifiedOrganisations() throws Exception {
		Document document = createStatFiWFS_modified();
		
		List<ResponsibleParty> organisations = document.getListValue(ResultMetadataFields.ORGANISATIONS, ResponsibleParty.class);

		assertEquals(2, organisations.size());
		ResponsibleParty party1 = organisations.get(0);
		
		assertEquals("Tilastokeskus", party1.getOrganisationName());
		assertEquals("originator", party1.getIsoRole());
		
		assertEquals(0, party1.getLocalisedOrganisationName().size());
		
		ResponsibleParty party2 = organisations.get(1);
		
		assertEquals("Tilastokeskus", party2.getOrganisationName());
		assertEquals("publisher", party2.getIsoRole());
		
		assertEquals(0, party2.getLocalisedOrganisationName().size());
	}

	@Test
	public void testLukeTietoaineistosarjaOrganisations() throws Exception {
		Document document = createLukeTietoaineistosarja();
		
		List<ResponsibleParty> organisations = document.getListValue(ResultMetadataFields.ORGANISATIONS, ResponsibleParty.class);

		assertEquals(1, organisations.size());
		ResponsibleParty party = organisations.get(0);
		
		assertEquals("Luonnonvarakeskus (Luke)", party.getOrganisationName());
		assertEquals("owner", party.getIsoRole());
		
		assertEquals(1, party.getLocalisedOrganisationName().size());
		assertEquals("Natural Resources Institute Finland (Luke)", party.getLocalisedOrganisationName().get("EN"));
	}
	

	@Test
	public void testLukeTietoaineistosarjaOrganisations_fromCSW() throws Exception {
		Document document = createLukeTietoaineistosarja_fromCSW();
		
		List<ResponsibleParty> organisations = document.getListValue(ResultMetadataFields.ORGANISATIONS, ResponsibleParty.class);

		assertEquals(1, organisations.size());
		ResponsibleParty party = organisations.get(0);
		
		assertEquals("Luonnonvarakeskus (Luke)", party.getOrganisationName());
		assertEquals("owner", party.getIsoRole());

		assertEquals(1, party.getLocalisedOrganisationName().size());
		assertEquals("Natural Resources Institute Finland (Luke)", party.getLocalisedOrganisationName().get("EN"));
	}
	
	@Test
	public void testOrgNameRewrite() throws Exception {
		FieldExtractorConfiguration fec = processor.getDocumentProcessingConfiguration().getFieldExtractor(ResultMetadataFields.ORGANISATIONS);
		FieldExtractorConfigurationImpl xfec = (FieldExtractorConfigurationImpl) fec;
		ResponsiblePartyCustomExtractor rpxpce = (ResponsiblePartyCustomExtractor) xfec.getCustomExtractor();
		
		rpxpce.setOrganisationNameRewriter(new TextRewriter() {
			
			@Override
			public String rewrite(String name, String language) {
				return "canon-"+language;
			}
			
			@Override
			public String rewrite(String name) {
				return "canon";
			}
		});
		
		Document document = createMaastotietokantaDocument();
		
		List<ResponsibleParty> organisations = document.getListValue(ResultMetadataFields.ORGANISATIONS, ResponsibleParty.class);

		assertEquals(1, organisations.size());
		ResponsibleParty party = organisations.get(0);
		
		assertEquals("canon", party.getOrganisationName());
		assertEquals("owner", party.getIsoRole());
		
		assertEquals(2, party.getLocalisedOrganisationName().size());
		assertEquals("canon-EN", party.getLocalisedOrganisationName().get("EN"));
		assertEquals("canon-SV", party.getLocalisedOrganisationName().get("SV"));
	}
	
	@Test
	public void testChooseCorrectOrganisationForSatakunnanRakennusinvestointienWFS() throws Exception {
		Document document = createSatakunnanRakennusinvestointienWFS();
		
		List<ResponsibleParty> organisations = document.getListValue(ResultMetadataFields.ORGANISATIONS, ResponsibleParty.class);

		assertEquals(1, organisations.size());
		ResponsibleParty party = organisations.get(0);
		
		assertEquals("Satakunnan museo", party.getOrganisationName());
		assertEquals("pointOfContact", party.getIsoRole());
		
		assertEquals(0, party.getLocalisedOrganisationName().size());
	}
}
