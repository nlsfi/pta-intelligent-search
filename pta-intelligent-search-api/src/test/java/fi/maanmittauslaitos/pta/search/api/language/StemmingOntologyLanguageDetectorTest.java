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
import fi.maanmittauslaitos.pta.search.text.stemmer.EnglishSnowballStemmer;
import fi.maanmittauslaitos.pta.search.text.stemmer.FinnishVoikkoStemmer;
import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;
import fi.maanmittauslaitos.pta.search.text.stemmer.SwedishSnowballStemmer;

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
		tmp.setSupportedLanguages(Arrays.asList("fi", "en", "sv"));
		
		tmp.setModel(model);
		
		Map<String, Stemmer> stemmers = new HashMap<>();
		stemmers.put("fi", new FinnishVoikkoStemmer());
		stemmers.put("en", new EnglishSnowballStemmer());
		stemmers.put("sv", new SwedishSnowballStemmer());
		tmp.setStemmers(stemmers);
		
		languageDetector = tmp;
	}

	@Test
	public void testBasicFinnish() {
		LanguageDetectionResult result = languageDetector.detectLanguage(Arrays.asList("suomella", "on", "oma", "kieli"));
		assertNotNull(result);
		assertTrue(result.getPotentialLanguages().size() > 0);
		assertEquals("fi", result.getPotentialLanguages().get(0));
		assertEquals(5.0, result.getScoreForLanguage("fi"), 0.001);
	}


	@Test
	public void testBasicSwedish() {
		LanguageDetectionResult result = languageDetector.detectLanguage(Arrays.asList("Vi", "skyddar", "din", "data", "och", "använder"));
		assertNotNull(result);
		assertTrue(result.getPotentialLanguages().size() > 0);
		assertEquals("sv", result.getPotentialLanguages().get(0));
		assertEquals(2.0, result.getScoreForLanguage("sv"), 0.001);
	}


	@Test
	public void testBasicEnglish() {
		LanguageDetectionResult result = languageDetector.detectLanguage(Arrays.asList("registered", "service", "class"));
		assertNotNull(result);
		assertTrue(result.getPotentialLanguages().size() > 0);
		assertEquals("en", result.getPotentialLanguages().get(0));
		assertEquals(8.0, result.getScoreForLanguage("en"), 0.001);
	}


	@Test
	public void testDrawBetweenEnglishAndFinnish() {
		LanguageDetectionResult result = languageDetector.detectLanguage(Arrays.asList("home"));
		assertNotNull(result);
		assertEquals(2, result.getPotentialLanguages().size());
		assertNotEquals("sv", result.getPotentialLanguages().get(0));
		assertNotEquals("sv", result.getPotentialLanguages().get(1));
		
		assertEquals(1.0, result.getScoreForLanguage("fi"), 0.001);
		assertEquals(1.0, result.getScoreForLanguage("en"), 0.001);
		
	}


	

	@Test
	public void testNotFinnishAtAll() {
		LanguageDetectionResult result = languageDetector.detectLanguage(Arrays.asList("change", "your", "perception"));
		assertNotNull(result);
		assertEquals(0.0, result.getScoreForLanguage("fi"), 0.001);
	}

}
