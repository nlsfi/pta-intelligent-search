package fi.maanmittauslaitos.pta.search.api.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import fi.maanmittauslaitos.pta.search.api.Language;
import fi.maanmittauslaitos.pta.search.api.model.SearchResult.Hit;
import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;

@Deprecated
public class NodeColorizationHintProviderImpl implements HintProvider {
	private ValueFactory vf = SimpleValueFactory.getInstance();
	
	private Model model;
	private List<Entry<IRI, Double>> relationsAndWeights;
	private int maxColorizationDepth = 3;
	private int maxHints = 5;
	private Stemmer stemmer;
	
	public void setModel(Model model) {
		this.model = model;
	}
	
	public Model getModel() {
		return model;
	}
	
	public void setStemmer(Stemmer stemmer) {
		this.stemmer = stemmer;
	}
	
	public Stemmer getStemmer() {
		return stemmer;
	}
	
	public void setMaxColorizationDepth(int maxColorizationDepth) {
		this.maxColorizationDepth = maxColorizationDepth;
	}
	
	public int getMaxColorizationDepth() {
		return maxColorizationDepth;
	}
	
	public void setRelationsAndWeights(List<Entry<IRI, Double>> relationsAndWeights) {
		this.relationsAndWeights = relationsAndWeights;
	}
	
	public List<Entry<IRI, Double>> getRelationsAndWeights() {
		return relationsAndWeights;
	}
	
	public void setMaxHints(int maxHints) {
		this.maxHints = maxHints;
	}
	
	public int getMaxHints() {
		return maxHints;
	}
	
	@Override
	public HintExtractor registerHintProvider(List<String> pyyntoTerms, SearchSourceBuilder builder, Language language) {
		
		return new HintExtractor() {
			
			@Override
			public List<String> getHints(SearchResponse response, List<Hit> hits) {
						
				Set<IRI> iris = new HashSet<>();
				
				Map<IRI, Double> colorized = colorize(iris);
				
				List<Entry<IRI, Double>> entries = new ArrayList<>(colorized.entrySet());
				
				// Sort by score
				Collections.sort(entries, new Comparator<Entry<IRI, Double>>() {
					@Override
					public int compare(Entry<IRI, Double> o1, Entry<IRI, Double> o2) {
						if (o1.getValue() < o2.getValue()) {
							return 1;
						} else if (o1.getValue() > o2.getValue()) {
							return -1;
						} else {
							return 0;
						}
					}
				});
				
				// Pick at most maxHints values, skipping terms used in the query
				List<String> ret = new ArrayList<>();
				for (Entry<IRI, Double> entry : entries) {
					IRI resource = entry.getKey();
					Optional<Value> value = Models.getProperty(getModel(), resource, SKOS.PREF_LABEL);
					if (!value.isPresent()) {
						continue;
					}
					
					String label = value.get().stringValue();
					
					if (!pyyntoTerms.contains(resource.toString())) {
					
					//if (!stemmedQueryTerms.contains(getStemmer().stem(label))) {
						ret.add(label);
		
						if (ret.size() >= getMaxHints()) {
							break;
						}
					}
				}
				
				return ret;

			}
		};
	}
	
	// Public for testing
	public Map<IRI, Double> colorize(Set<IRI> resources) {
		Map<IRI, Double> ret = new HashMap<>();
		
		for (IRI resource : resources) {
			increment(resource, 1.0, ret);
			colorizeResource(resource, 1, 1.0, ret);
		}
		
		return ret;
	}

	private void colorizeResource(IRI resource, int depth, double previousWeight, Map<IRI, Double> ret) {
		if (depth > getMaxColorizationDepth()) return;

		for (Entry<IRI, Double> relation : getRelationsAndWeights()) {
			
			IRI predicate = relation.getKey();
			double targetWeight = previousWeight * relation.getValue();
			
			for (Statement s : model.filter(resource, predicate, null)) {
				Value v = s.getObject();
				IRI object = vf.createIRI(v.stringValue());
				
				increment(object, targetWeight, ret);
				colorizeResource(object, depth + 1, targetWeight, ret);
			}
			
		}

	}

	private void increment(IRI object, double weight, Map<IRI, Double> ret) {
		Double tmp = ret.get(object);
		if (tmp == null) {
			tmp = 0.0;
		}
		tmp += weight;
		ret.put(object, tmp);
	}

}
