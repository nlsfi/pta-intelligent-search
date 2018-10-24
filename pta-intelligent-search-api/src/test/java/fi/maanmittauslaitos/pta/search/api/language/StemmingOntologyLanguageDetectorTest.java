package fi.maanmittauslaitos.pta.search.api.language;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fi.maanmittauslaitos.pta.search.api.ApplicationConfiguration;
import fi.maanmittauslaitos.pta.search.api.Language;
import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;

public class StemmingOntologyLanguageDetectorTest {

	private LanguageDetector languageDetector;
	
	private static Model model;
	
	@BeforeClass
	public static void setUpModel() throws Exception {
		model = ApplicationConfiguration.loadModels("/pto-skos.ttl.gz");
	}
	
	@Before
	public void setUp() throws Exception {
		StemmingOntologyLanguageDetectorImpl tmp = new StemmingOntologyLanguageDetectorImpl();
		tmp.setTerminologyLabels(Arrays.asList(SKOS.PREF_LABEL, SKOS.ALT_LABEL));
		tmp.setSupportedLanguages(Arrays.asList(Language.FI, Language.EN, Language.SV));
		
		tmp.setModel(model);
		
		ApplicationConfiguration config = new ApplicationConfiguration();
		Map<Language, Stemmer> stemmers = new HashMap<>();
		stemmers.put(Language.FI, config.stemmer_FI());
		stemmers.put(Language.EN, config.stemmer_EN());
		stemmers.put(Language.SV, config.stemmer_SV());
		tmp.setStemmers(stemmers);
		
		languageDetector = tmp;
	}

	@Test
	public void testBasicFinnish() {
		LanguageDetectionResult result = languageDetector.detectLanguage(Arrays.asList("suomella", "on", "oma", "kieli"));
		assertNotNull(result);
		assertTrue(result.getPotentialLanguages().size() > 0);
		assertEquals(Language.FI, result.getPotentialLanguages().get(0));
		assertEquals(2, result.getScoreForLanguage(Language.FI));
	}


	@Test
	public void testBasicSwedish() {
		LanguageDetectionResult result = languageDetector.detectLanguage(Arrays.asList("Vi", "skyddar", "din", "data", "och", "använder"));
		assertNotNull(result);
		assertTrue(result.getPotentialLanguages().size() > 0);
		assertEquals(Language.SV, result.getPotentialLanguages().get(0));
		assertEquals(2, result.getScoreForLanguage(Language.SV));
	}


	@Test
	public void testBasicEnglish() {
		LanguageDetectionResult result = languageDetector.detectLanguage(Arrays.asList("registered", "service", "class"));
		assertNotNull(result);
		assertTrue(result.getPotentialLanguages().size() > 0);
		assertEquals(Language.EN, result.getPotentialLanguages().get(0));
		assertEquals(2, result.getScoreForLanguage(Language.EN));
	}


	@Test
	public void testDrawBetweenEnglishAndSwedish() {
		LanguageDetectionResult result = languageDetector.detectLanguage(Arrays.asList("salt"));
		assertNotNull(result);
		assertEquals(2, result.getPotentialLanguages().size());
		assertNotEquals(Language.FI, result.getPotentialLanguages().get(0));
		assertNotEquals(Language.FI, result.getPotentialLanguages().get(1));
		
		assertEquals(1, result.getScoreForLanguage(Language.SV));
		assertEquals(1, result.getScoreForLanguage(Language.EN));
		
	}


	

	@Test
	public void testNotFinnishAtAll() {
		LanguageDetectionResult result = languageDetector.detectLanguage(Arrays.asList("change", "your", "perception"));
		assertNotNull(result);
		assertEquals(0, result.getScoreForLanguage(Language.FI));
	}

}
