package fi.maanmittauslaitos.pta.search.api;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class OntologyModel {
	private Model model;
	
	private final ValueFactory vf = SimpleValueFactory.getInstance();
	
	public OntologyModel()
	{
	}
	
	public OntologyModel(Model model) {
		setModel(model);
	}
	
	public void setModel(Model model) {
		this.model = model;
	}
	
	public Model getModel() {
		return model;
	}

	public Set<String> getByPredicate(String resourceName, IRI predicate) {
		final IRI iri = vf.createIRI(resourceName);
		
		Set<String> ret = new HashSet<>();
		for (Value v : model.filter(iri, predicate, null).objects()) {
			ret.add(v.stringValue());
		}
		return ret;
	}
	
	
}
