package fi.maanmittauslaitos.pta.search.codelist;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class OrganisationNormaliserTest {
	OrganisationNormaliser organisationNormaliser;
	
	@Before
	public void setUp() throws Exception {
		ODFOrganisationNameNormaliserImpl tmp = new ODFOrganisationNameNormaliserImpl();
		tmp.loadWorkbook(OrganisationNormaliserTest.class.getResourceAsStream("/canonical_organisations.ods"));
		organisationNormaliser = tmp;
	}

	@Test
	public void testHameenlinnaFirstRow() {
		String alternate = "Hämeenlinnan kaupunki/yhdyskunta- ja ympäristöpalvelut/tietopalvelut";
		String canonical = organisationNormaliser.getCanonicalOrganisationName(alternate, "fi");
		
		assertEquals("Hämeenlinnan kaupunki", canonical);
	}

	@Test
	public void testKuopioLastRow() {
		String alternate = "Kuopion Vesi, suunnittelu; Kuopion kaupungin kaupunkiympäristön palvelualueen kunnallistekniikan suunnittelu";
		String canonical = organisationNormaliser.getCanonicalOrganisationName(alternate, "fi");
		
		assertEquals("Kuopion kaupunki", canonical);
	}

	@Test
	public void testCanonicalIsCanonical() {
		String alternate = "CGI Suomi Oy";
		String canonical = organisationNormaliser.getCanonicalOrganisationName(alternate, "fi");
		
		assertEquals("CGI Suomi Oy", canonical);
	}
	
}
