package fi.maanmittauslaitos.pta.search.api.hints;

import static org.junit.Assert.*;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fi.maanmittauslaitos.pta.search.api.ApplicationConfiguration;
import fi.maanmittauslaitos.pta.search.api.HakuPyynto;
import fi.maanmittauslaitos.pta.search.api.HakuTulos.Hit;
import fi.maanmittauslaitos.pta.search.api.hints.NodeColorizationHintProviderImpl;
import fi.maanmittauslaitos.pta.search.text.stemmer.StemmerFactor;

public class NodeColorizationHitScoreHintProviderImplTest {
	private ValueFactory vf = SimpleValueFactory.getInstance();
	private NodeColorizationHitScoreHintProviderImpl hintProvider;
	private static Model model;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		model = ApplicationConfiguration.loadModels("/ysa-skos.ttl.gz");
	}
	
	@Before
	public void setUp() throws Exception { 
		hintProvider = new NodeColorizationHitScoreHintProviderImpl();
		hintProvider.setMaxColorizationDepth(3);
		hintProvider.setStemmer(StemmerFactor.createStemmer());
		hintProvider.setModel(model);
		hintProvider.setLanguage("fi");
		
		List<Entry<IRI, Double>> weights = new ArrayList<>();
		
		weights.add(new AbstractMap.SimpleEntry<>(SKOS.BROADER, 0.25));
		weights.add(new AbstractMap.SimpleEntry<>(SKOS.RELATED, 0.5));
		
		hintProvider.setRelationsAndWeights(weights);
		
	}

	@Test
	public void testDepth1SingleTermScore2() {
		List<String> uris = Arrays.asList("http://www.yso.fi/onto/ysa/Y108634"); // abiturientit
		hintProvider.setMaxColorizationDepth(1);
		
		Map<IRI, Double> colors = hintProvider.colorize(toResources(uris, Arrays.asList(2.0)));
		
		// Not related (directly)
		Double weightForAmmattikoululaiset = colors.get(vf.createIRI("http://www.yso.fi/onto/ysa/Y151201"));
		assertNull(weightForAmmattikoululaiset);
		
		// Broader
		Double weightForKoululaiset = colors.get(vf.createIRI("http://www.yso.fi/onto/ysa/Y96403"));
		assertNotNull(weightForKoululaiset);
		assertEquals(0.5, weightForKoululaiset.doubleValue(), 0.00001);
		
		
		// Associative
		Double weightForPenkinpainajaiset = colors.get(vf.createIRI("http://www.yso.fi/onto/ysa/Y108579"));
		assertNotNull(weightForPenkinpainajaiset);
		assertEquals(1.0, weightForPenkinpainajaiset.doubleValue(), 0.00001);
		
		Double weightForYlioppilaat = colors.get(vf.createIRI("http://www.yso.fi/onto/ysa/Y104928"));
		assertNotNull(weightForYlioppilaat);
		assertEquals(1.0, weightForYlioppilaat.doubleValue(), 0.00001);
	}
	
	@Test
	public void testColorizesTheTermItself() {
		List<String> uris = Arrays.asList("http://www.yso.fi/onto/ysa/Y108634"); // abiturientit
		hintProvider.setMaxColorizationDepth(1);
		
		Map<IRI, Double> colors = hintProvider.colorize(toResources(uris, Arrays.asList(1.0)));
	
		// The term itself
		Double weightForAbiturientit = colors.get(vf.createIRI("http://www.yso.fi/onto/ysa/Y108634"));
		assertNotNull(weightForAbiturientit);
		assertEquals(1.0, weightForAbiturientit.doubleValue(), 0.00001);
	}
	
	@Test
	public void testColorizesTheTermItselfScoreMatters() {
		List<String> uris = Arrays.asList("http://www.yso.fi/onto/ysa/Y108634"); // abiturientit
		hintProvider.setMaxColorizationDepth(1);
		
		// Score of 3.3 
		Map<IRI, Double> colors = hintProvider.colorize(toResources(uris, Arrays.asList(3.3)));
	
		// The term itself
		Double weightForAbiturientit = colors.get(vf.createIRI("http://www.yso.fi/onto/ysa/Y108634"));
		assertNotNull(weightForAbiturientit);
		assertEquals(3.3, weightForAbiturientit.doubleValue(), 0.00001);
	}
	

	@Test
	public void testDepth1TwoTerms() {
		List<String> uris = Arrays.asList(
				"http://www.yso.fi/onto/ysa/Y108579", // penkinpainajaiset
				"http://www.yso.fi/onto/ysa/Y181670" // vanhojenpäivä
				); 
		hintProvider.setMaxColorizationDepth(1);
		List<Entry<IRI, Double>> weights = new ArrayList<>();
		
		weights.add(new AbstractMap.SimpleEntry<>(SKOS.BROADER, 0.2));
	
		hintProvider.setRelationsAndWeights(weights);
		

		Map<IRI, Double> colors = hintProvider.colorize(toResources(uris, Arrays.asList(1.0, 1.0)));
		
		// Common "broader"
		Double weightForKoulujuhlat = colors.get(vf.createIRI("http://www.yso.fi/onto/ysa/Y102734"));
		assertNotNull(weightForKoulujuhlat);
		assertEquals(0.4, weightForKoulujuhlat.doubleValue(), 0.00001);
	}
	
	@Test
	public void testDepth2TwoTerms() {
		List<String> uris = Arrays.asList(
				"http://www.yso.fi/onto/ysa/Y104253", // uhkasakko
				"http://www.yso.fi/onto/ysa/Y108381" // karkotus
				);
		
		//                  Rangaistukset
		//              0.25 /       | 0.5
		//            Sakko     Karkotus
		//              | 0.5
		//          Uhkasakko
		hintProvider.setMaxColorizationDepth(2);
		List<Entry<IRI, Double>> weights = new ArrayList<>();
		
		weights.add(new AbstractMap.SimpleEntry<>(SKOS.BROADER, 0.5));
		hintProvider.setRelationsAndWeights(weights);
		

		Map<IRI, Double> colors = hintProvider.colorize(toResources(uris, Arrays.asList(1.0, 1.0)));
		
		// Broarder only for uhkasakko
		Double weightForSakko = colors.get(vf.createIRI("http://www.yso.fi/onto/ysa/Y98711")); // sakko
		assertNotNull(weightForSakko);
		assertEquals(0.5, weightForSakko.doubleValue(), 0.00001);
		
		// Common "broader"
		Double weightForRangaistukset = colors.get(vf.createIRI("http://www.yso.fi/onto/ysa/Y98495")); // rangaistukset
		assertNotNull(weightForRangaistukset);
		assertEquals(0.75, weightForRangaistukset.doubleValue(), 0.00001);
	}
	
	
	@Test
	public void testDepth2OneTermAssosiativeTermHasSameBroadTerm() {
		List<String> uris = Arrays.asList(
				"http://www.yso.fi/onto/ysa/Y98711" // sakko
				);
		
		//                  Rangaistukset
		//              0.5 /       | 0.5 * 0.1
		//            Sakko   ---  Sakon muuntorangaistus
		//                     0.1

		hintProvider.setMaxColorizationDepth(2);
		List<Entry<IRI, Double>> weights = new ArrayList<>();
		
		weights.add(new AbstractMap.SimpleEntry<>(SKOS.BROADER, 0.5));
		weights.add(new AbstractMap.SimpleEntry<>(SKOS.RELATED, 0.1));
		hintProvider.setRelationsAndWeights(weights);
		

		Map<IRI, Double> colors = hintProvider.colorize(toResources(uris, Arrays.asList(1.0)));
		
		// Associative
		Double weightForSakko = colors.get(vf.createIRI("http://www.yso.fi/onto/ysa/Y165908")); // sakon muuntorangaistus
		assertNotNull(weightForSakko);
		assertEquals(0.1, weightForSakko.doubleValue(), 0.00001);
		
		// Rangaistukset is broader and the broader of a related term 
		Double weightForRangaistukset = colors.get(vf.createIRI("http://www.yso.fi/onto/ysa/Y98495")); // rangaistukset
		assertNotNull(weightForRangaistukset);
		assertEquals(0.5 + 0.5*0.1, weightForRangaistukset.doubleValue(), 0.00001);
		
	}
	
	@Test
	public void testSearchDepth2OneTermCorrectOrder() {
		//                  Rangaistukset
		//              0.5 /       | 0.5 * 0.1
		//            Sakko   ---  Sakon muuntorangaistus
		//                     0.1

		hintProvider.setMaxColorizationDepth(2);
		hintProvider.setMaxHints(2);
		List<Entry<IRI, Double>> weights = new ArrayList<>();
		
		weights.add(new AbstractMap.SimpleEntry<>(SKOS.BROADER, 0.5));
		weights.add(new AbstractMap.SimpleEntry<>(SKOS.RELATED, 0.1));
		hintProvider.setRelationsAndWeights(weights);
		
		Hit fakeHit = new Hit();
		fakeHit.setAbstractUris(Arrays.asList("http://www.yso.fi/onto/ysa/Y98711")); // Sakko
		fakeHit.setScore(1.0);

		HakuPyynto pyynto = new HakuPyynto();
		pyynto.setQuery(Arrays.asList("sakko"));
		
		List<String> hints = hintProvider.getHints(pyynto, Arrays.asList(fakeHit));
		
		assertEquals(2, hints.size());
		assertEquals("rangaistukset", hints.get(0));
		assertEquals("sakon muuntorangaistus", hints.get(1));
		
	}

	
	@Test
	public void testSearchOriginalQueryTermsNotReturned() {
		//                  Rangaistukset
		//              0.5 /       | 0.5 * 0.1
		//            Sakko   ---  Sakon muuntorangaistus
		//                     0.1

		hintProvider.setMaxColorizationDepth(2);
		hintProvider.setMaxHints(2);
		List<Entry<IRI, Double>> weights = new ArrayList<>();
		
		weights.add(new AbstractMap.SimpleEntry<>(SKOS.BROADER, 0.5));
		weights.add(new AbstractMap.SimpleEntry<>(SKOS.RELATED, 0.1));
		hintProvider.setRelationsAndWeights(weights);
		
		Hit fakeHit1 = new Hit();
		fakeHit1.setAbstractUris(Arrays.asList("http://www.yso.fi/onto/ysa/Y98711")); // Sakko
		fakeHit1.setScore(1.0);
		
		Hit fakeHit2 = new Hit();
		fakeHit2.setAbstractUris(Arrays.asList("http://www.yso.fi/onto/ysa/Y165908")); // Sakon muuntorangaistus
		fakeHit2.setScore(1.0);
		
		HakuPyynto pyynto = new HakuPyynto();
		pyynto.setQuery(Arrays.asList("sakko", "sakon muuntorangaistus"));
		
		List<String> hints = hintProvider.getHints(pyynto, Arrays.asList(fakeHit1, fakeHit2));
		assertEquals(2, hints.size());
		assertEquals("rangaistukset", hints.get(0));
		assertEquals("tuomiot", hints.get(1));
	}

	private Set<Map.Entry<IRI, Double>> toResources(List<String> uris, List<Double> scores) {
		Set<Map.Entry<IRI, Double>> ret = new HashSet<>();
		if (uris.size() != scores.size()) {
			throw new IllegalArgumentException("Size of uris and scores need to be equal");
		}
		for (int i = 0; i < uris.size(); i++) {
			ret.add(new AbstractMap.SimpleEntry<>(vf.createIRI(uris.get(i)), scores.get(i)));
		}
		return ret;
	}

}
