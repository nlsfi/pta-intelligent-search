package fi.maanmittauslaitos.pta.search.api.hints;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import fi.maanmittauslaitos.pta.search.api.model.SearchResult.Hit;
import fi.maanmittauslaitos.pta.search.text.RDFTerminologyMatcherProcessor;

/**
 * This HintProvider is based on the research paper
 * "Query expansion via conceptual distance in thesaurus indexed collections"
 * By Douglas Tudhope, Ceri Binding, Dorothee Blocks, Daniel Cunliffe (2006)
 *
 * @author v2
 *
 */
public class TudhopeBindingBlocksCunliffeHintProvider extends AbstractHintProvider {
	private ValueFactory vf = SimpleValueFactory.getInstance();
	
	private List<Entry<IRI, Double>> relationsAndTravelCosts;
	private RDFTerminologyMatcherProcessor terminologyProcessor;
	
	public void setRelationsAndTravelCosts(List<Entry<IRI, Double>> relationsAndTravelCosts) {
		this.relationsAndTravelCosts = relationsAndTravelCosts;
	}
	
	public List<Entry<IRI, Double>> getRelationsAndTravelCosts() {
		return relationsAndTravelCosts;
	}
	
	public void setTerminologyProcessor(RDFTerminologyMatcherProcessor terminologyProcessor) {
		this.terminologyProcessor = terminologyProcessor;
	}
	
	public RDFTerminologyMatcherProcessor getTerminologyProcessor() {
		return terminologyProcessor;
	}
	
	/**
	 * 
	 */
	@Override
	public HintExtractor registerHintProvider(List<String> pyyntoTerms, SearchSourceBuilder builder) {
		return new HintExtractor() {
			
			@Override
			public List<String> getHints(SearchResponse response, List<Hit> hits) {
				Set<IRI> iris = new HashSet<>();
				for (String iri : pyyntoTerms) {
					iris.add(vf.createIRI(iri));
				}
				
				Map<IRI, Double> colorized = colorize(iris);
				
				return produceAndOrderHints(pyyntoTerms, colorized);
			}
		};
	}
	
	
	public Map<IRI, Double> colorize(Set<IRI> iris) {
		
		double startingCloseness = 1.0;
		final double cutoffThreshold = 0.0;
		
		Map<IRI, Double> scTerms = new HashMap<>();
		
		LinkedHashMap<IRI, Double> toBeExpanded = new LinkedHashMap<>(); // IRI + closeness pairs
		for (IRI iri : iris) {
			toBeExpanded.put(iri, startingCloseness);
			scTerms.put(iri, startingCloseness);
		}
		
		while (toBeExpanded.size() > 0) {
			// Remove one entry
			Iterator<Entry<IRI, Double>> i = toBeExpanded.entrySet().iterator();
			Entry<IRI, Double> e = i.next();
			i.remove();
			
			
			IRI sourceTerm = e.getKey();
			Double sourceCloseness = e.getValue();
			
			for (Entry<IRI, Double> tmp : getRelationsAndTravelCosts()) {
				IRI relation = tmp.getKey();
				Double travelCost = tmp.getValue();
				
				double newValue = sourceCloseness - travelCost;
				if (newValue <= cutoffThreshold) {
					continue;
				}
				
				for (Value v : getModel().filter(sourceTerm, relation, null).objects()) {
					IRI targetTerm = vf.createIRI(v.stringValue());
					Double targetCloseness = scTerms.get(targetTerm);
					
					if (targetCloseness != null) {
						if (targetCloseness < newValue) {
							// Update closeness and expand again, because now something else might be closer
							scTerms.put(targetTerm, newValue);
							toBeExpanded.put(targetTerm, newValue);
						} else {
							// We found a longer route to this term, no need to make updates
						}
					} else {
						// Add as new and expand
						scTerms.put(targetTerm, newValue);
						toBeExpanded.put(targetTerm, newValue);
					}
				}
			}
		}
		
		return scTerms;
	}

}
