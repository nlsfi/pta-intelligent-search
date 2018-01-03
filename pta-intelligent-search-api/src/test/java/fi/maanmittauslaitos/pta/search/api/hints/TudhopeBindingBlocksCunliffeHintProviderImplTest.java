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
import fi.maanmittauslaitos.pta.search.text.stemmer.StemmerFactor;

public class TudhopeBindingBlocksCunliffeHintProviderImplTest {
	private ValueFactory vf = SimpleValueFactory.getInstance();
	private TudhopeBindingBlocksCunliffeHintProvider hintProvider;
	private static Model model;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		model = ApplicationConfiguration.loadModels("/ysa-skos.ttl.gz");
	}
	
	@Before
	public void setUp() throws Exception { 
		hintProvider = new TudhopeBindingBlocksCunliffeHintProvider();
		hintProvider.setStemmer(StemmerFactor.createStemmer());
		hintProvider.setModel(model);
		hintProvider.setLanguage("fi");
		
		List<Entry<IRI, Double>> relationsAndTravelCosts = new ArrayList<>();
		
		relationsAndTravelCosts.add(new AbstractMap.SimpleEntry<>(SKOS.BROADER, 0.33));
		relationsAndTravelCosts.add(new AbstractMap.SimpleEntry<>(SKOS.RELATED, 0.5));
		
		hintProvider.setRelationsAndTravelCosts(relationsAndTravelCosts);
	}

	@Test
	public void testStartingpointTermIs1() {
		List<String> uris = Arrays.asList("http://www.yso.fi/onto/ysa/Y108634"); // abiturientit
		
		Map<IRI, Double> colors = hintProvider.colorize(toResources(uris));
		
		// Abiturientit should be 1.0
		Double scoreForAbiturientit = colors.get(vf.createIRI("http://www.yso.fi/onto/ysa/Y108634"));
		assertNotNull(scoreForAbiturientit);
		assertEquals(1.0, scoreForAbiturientit, 0.00001);

	}
	
	@Test
	public void testSingleStartingpointTerm() {
		List<String> uris = Arrays.asList("http://www.yso.fi/onto/ysa/Y108634"); // abiturientit
		
		Map<IRI, Double> colors = hintProvider.colorize(toResources(uris));	
		
		Double scoreForKoululaiset = colors.get(vf.createIRI("http://www.yso.fi/onto/ysa/Y96403"));
		assertNotNull(scoreForKoululaiset);
		assertEquals(0.67, scoreForKoululaiset, 0.00001);
		
		Double scoreForLukiolaiset = colors.get(vf.createIRI("http://www.yso.fi/onto/ysa/Y113175"));
		assertNotNull(scoreForLukiolaiset);
		assertEquals(0.67, scoreForLukiolaiset, 0.00001);
		
		
		// Abiturientit --BROADER--> Lukiolaiset --RELATED--> Opiskelijat
		Double scoreForOpiskelijat = colors.get(vf.createIRI("http://www.yso.fi/onto/ysa/Y97831"));
		assertNotNull(scoreForOpiskelijat);
		assertEquals(0.17, scoreForOpiskelijat, 0.00001);
		
		// Abiturientit --BROADER--> Lukiolaiset --RELATED--> Opiskelijat --RELATED--> Ammattikoululaiset
		// Abiturientit --BROADER--> Koululaiset --RELATED--> Ammattikoululaiset
		Double scoreForAmmattikoululaiset = colors.get(vf.createIRI("http://www.yso.fi/onto/ysa/Y151201"));
		assertNotNull(scoreForAmmattikoululaiset);
		assertEquals(0.17, scoreForAmmattikoululaiset, 0.00001);
		
		
		// Abiturientit --RELATED--> Penkinpainajaiset --BROADER--> Koulujuhlat
		Double scoreForKoulujuhlat = colors.get(vf.createIRI("http://www.yso.fi/onto/ysa/Y102734"));
		assertNotNull(scoreForKoulujuhlat);
		assertEquals(0.17, scoreForKoulujuhlat, 0.00001);
		
		// Abiturientit --RELATED--> Penkinpainajaiset --BROADER--> Koulujuhlat --BROADER--> Juhlat
		Double scoreForJuhlat = colors.get(vf.createIRI("http://www.yso.fi/onto/ysa/Y95650"));
		assertNull(scoreForJuhlat);
		
	}

	

	@Test
	public void testTwoStartingpointTerm() {
		List<String> uris = Arrays.asList(
				"http://www.yso.fi/onto/ysa/Y108634", // abiturientit
				"http://www.yso.fi/onto/ysa/Y181670"  // vanhojenpäivä
				);
		
		Map<IRI, Double> colors = hintProvider.colorize(toResources(uris));	
		
		// Abiturientit --RELATED--> Penkinpainajaiset --BROADER--> Koulujuhlat
		// Vanhojenpäivä --BROADER--> Koulujuhlat
		Double scoreForKoulujuhlat = colors.get(vf.createIRI("http://www.yso.fi/onto/ysa/Y102734"));
		assertNotNull(scoreForKoulujuhlat);
		assertEquals(0.67, scoreForKoulujuhlat, 0.00001);
	}
	
	
	private Set<IRI> toResources(List<String> uris) {
		Set<IRI> ret = new HashSet<>();
		for (String uri : uris) {
			ret.add(vf.createIRI(uri));
		}
		return ret;
	}

}
