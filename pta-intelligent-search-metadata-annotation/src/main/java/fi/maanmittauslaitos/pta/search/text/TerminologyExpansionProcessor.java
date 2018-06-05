package fi.maanmittauslaitos.pta.search.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * 
 */
public class TerminologyExpansionProcessor implements TextProcessor {
	// Setters, getters
	private Model model;
	private Collection<IRI> predicates;
	
	// Private lazily populated field, access only through getDict()
	private Map<String, Set<String>> dict = new HashMap<>();

	private ValueFactory vf = SimpleValueFactory.getInstance();

	public void setModel(Model model) {
		this.model = model;
		this.dict = new HashMap<>();
	}
	
	public Model getModel() {
		return model;
	}
	
	/**
	 * Set the predicate to follow when expanding a predicate. The most usual 
	 * predicate to follow is the SKOS.BROADER, the expander will then expand
	 * a word into the broader definitions of the original term. Expansion is
	 * done recursively until no more new relatives are found.
	 * 
	 * @param predicate
	 */
	public void setPredicates(Collection<IRI> predicates) {
		this.predicates = predicates;
	}
	
	public Collection<IRI> getPredicates() {
		return predicates;
	}
	
	Set<String> getParents(String resource) {
		Set<String> ret = dict.get(resource);
		
		if (ret == null) {
			ret = new HashSet<>();
			
			addParentsRecursively(Collections.singleton(resource), ret);
			
			dict.put(resource, ret);
		}
		
		return ret;
	}
	
	private void addParentsRecursively(Set<String> terms, Set<String> addParentsHere) {
		Set<String> directParents = new HashSet<>();
		
		for (String resource : terms) {
			// We could use "dict" here to speed things up, but I'm unsure if it's really necessary
			for (IRI pred : getPredicates()) {
				for (Value v : getModel().filter(vf.createIRI(resource), pred, null).objects()) {
					directParents.add(v.stringValue());
					addParentsHere.add(v.stringValue());
				}
			}
		}
		
		if (directParents.size() > 0) {
			addParentsRecursively(directParents, addParentsHere);
		}
		
	}

	@Override
	public List<String> process(List<String> input) {
		Set<String> ret = new HashSet<>();
		
		for (String str : input) {
			
			Set<String> parents = getParents(str);
			
			ret.addAll(parents);
			
		}
		return new ArrayList<>(ret);
	}

}
