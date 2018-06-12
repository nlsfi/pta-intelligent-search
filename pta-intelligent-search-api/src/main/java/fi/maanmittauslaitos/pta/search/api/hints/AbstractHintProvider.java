package fi.maanmittauslaitos.pta.search.api.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.SKOS;

import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;

public abstract class AbstractHintProvider implements HintProvider {
	protected ValueFactory vf = SimpleValueFactory.getInstance();
	
	private Model model;
	private int maxHints = 5;
	private Stemmer stemmer;
	private String language;
	
	public int getMaxHints() {
		return maxHints;
	}
	
	public void setMaxHints(int maxHints) {
		this.maxHints = maxHints;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public void setStemmer(Stemmer stemmer) {
		this.stemmer = stemmer;
	}
	
	public Stemmer getStemmer() {
		return stemmer;
	}
	

	public void setModel(Model model) {
		this.model = model;
	}
	
	public Model getModel() {
		return model;
	}
	
	/**
	 * This method orders the given map of scored terms and picks out the winners removing
	 * the search terms used in the original request. The terms are returned in the configured
	 * language.
	 * 
	 * @param pyynto
	 * @param colorized
	 * @return Maximum of maxHits results. Used search terms are never returned as hints.
	 */
	protected List<String> produceAndOrderHints(List<String> pyyntoTerms, Map<IRI, Double> colorized) {
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
		List<String> ret = determineLabelsForHintsKeepResultsWithinMaxSize(entries, pyyntoTerms);
		
		return ret;
	}

	protected List<String> determineLabelsForHintsKeepResultsWithinMaxSize(List<Entry<IRI, Double>> entries, List<String> pyyntoTerms) {
		Set<String> labels = new HashSet<>();
		for (Entry<IRI, Double> entry : entries) {
			IRI resource = entry.getKey();
			Set<Literal> values = Models.getPropertyLiterals(getModel(), resource, SKOS.PREF_LABEL);
			
			for (Literal value : values) {
				// If language is set, only care about labels for that particular language
				if (getLanguage() != null && value.getLanguage().isPresent()) {
					if (!getLanguage().equals(value.getLanguage().get())) {
						continue;
					}
				}
				String label = value.stringValue();
				
				if (!pyyntoTerms.contains(resource.toString())) {
					labels.add(label);
	
					break;
				}
			}
			
			if (labels.size() >= getMaxHints()) {
				break;
			}
		}
		List<String> ret = new ArrayList<>(labels);
		Collections.sort(ret);
		return ret;
	}
	

}
