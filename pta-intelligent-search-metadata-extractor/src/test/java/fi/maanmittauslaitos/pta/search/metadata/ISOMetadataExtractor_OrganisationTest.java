package fi.maanmittauslaitos.pta.search.metadata;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;

public class ISOMetadataExtractor_OrganisationTest extends BaseMetadataExtractorTest {

	@Test
	public void testMaastotietokantaOrganisations() throws Exception {
		Document document = createMaastotietokantaDocument();
		
		List<String> names = document.getListValue(ISOMetadataFields.ORGANISATION_NAMES, String.class);
		List<String> roles = document.getListValue(ISOMetadataFields.ORGANISATION_ROLES, String.class);
		
		assertEquals(4, names.size());
		assertEquals(4, roles.size());
		
		testNameAndRole("Maanmittauslaitos", "owner", names, roles, 0);
		testNameAndRole("Maanmittauslaitos", "owner", names, roles, 1);
		testNameAndRole("Maanmittauslaitos", "owner", names, roles, 2);
		testNameAndRole("Maanmittauslaitos", "owner", names, roles, 3);
	}


	@Test
	public void testStatFiWFSOrganisations() throws Exception {
		Document document = createStatFiWFS();
		
		List<String> names = document.getListValue(ISOMetadataFields.ORGANISATION_NAMES, String.class);
		List<String> roles = document.getListValue(ISOMetadataFields.ORGANISATION_ROLES, String.class);
		
		assertEquals(3, names.size());
		assertEquals(3, roles.size());
		
		testNameAndRole("Tilastokeskus", "pointOfContact", names, roles, 0);
		testNameAndRole("Tilastokeskus", "originator",     names, roles, 1);
		testNameAndRole("Tilastokeskus", "publisher",      names, roles, 2);
	}

	void testNameAndRole(String expectedName, String expectedRole, List<String> names, List<String> roles, int idx) {
		String name = names.get(idx);
		String role = roles.get(idx);
		
		assertEquals(expectedName, name);
		assertEquals(expectedRole, role);
	}
	
}
