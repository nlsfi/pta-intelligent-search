package fi.maanmittauslaitos.pta.search.codelist;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fi.maanmittauslaitos.pta.search.HarvesterConfig;

public class InspireThemesImplTest {
	private static Model model;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		model = loadRDFModels("/inspire-theme.rdf.gz");
	}

	private static Model loadRDFModels(String...files) throws IOException {
		Model ret = null;
		
		for (String file : files) {
			try (Reader reader = new InputStreamReader(new GZIPInputStream(HarvesterConfig.class.getResourceAsStream(file)))) {
				Model model = Rio.parse(reader, "", RDFFormat.RDFXML);
				
				if (ret == null) {
					ret = model;
				} else {
					ret.addAll(model);
				}
			}
		}
		
		return ret;
	}
	
	private InspireThemesImpl inspireThemes;
	
	@Before
	public void setUp() throws Exception {
		inspireThemes = new InspireThemesImpl();
		inspireThemes.setModel(model);
		inspireThemes.setCanonicalLanguage("en");
		inspireThemes.setHeuristicSearchLanguagePriority("fi", "en", "sv");
	}

	@Test
	public void testMaankayttoFiToEn() {
		String canon = inspireThemes.getCanonicalName("Maankäyttö", "fi");
		assertEquals("Land use", canon);
	}


	@Test
	public void testMaankayttoFiToEn_noLanguageContext() {
		String canon = inspireThemes.getCanonicalName("Maankäyttö");
		assertEquals("Land use", canon);
	}

	
	@Test
	public void testMaankayttoEnToFi() {
		inspireThemes.setCanonicalLanguage("fi");
		String canon = inspireThemes.getCanonicalName("Land use", "en");
		assertEquals("Maankäyttö", canon);
	}

}
