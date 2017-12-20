package fi.maanmittauslaitos.pta.search.api;

import static org.junit.Assert.*;

import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.junit.BeforeClass;
import org.junit.Test;

public class OntologyModelTest {
	private static Model model;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		model = ApplicationConfiguration.loadModels("/ysa-skos.ttl.gz");
	}

	@Test
	public void testNarrowerFound() {
		final String kotikissa = "http://www.yso.fi/onto/ysa/Y96241";
		final String korat = "http://www.yso.fi/onto/ysa/Y172597";
		
		ValueFactory vf = SimpleValueFactory.getInstance();

		final IRI kotikissaIRI = vf.createIRI(kotikissa);
		
		boolean koratFound = false;
		for (Value v : model.filter(kotikissaIRI, SKOS.NARROWER, null).objects()) {
			if (v.stringValue().equals(korat)) {
				koratFound = true;
			}
		}
		
		assertTrue(koratFound);
	}
	

	@Test
	public void ontologyModelClassNarrowerFound() {
		final String kotikissa = "http://www.yso.fi/onto/ysa/Y96241";
		final String korat = "http://www.yso.fi/onto/ysa/Y172597";
		
		Set<String> narrower = new OntologyModel(model).getByPredicate(kotikissa, SKOS.NARROWER);
		
		assertTrue(narrower.contains(korat));
	}

}
