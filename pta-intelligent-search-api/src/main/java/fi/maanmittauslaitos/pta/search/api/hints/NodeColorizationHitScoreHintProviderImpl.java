package fi.maanmittauslaitos.pta.search.api.hints;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.SKOS;

import fi.maanmittauslaitos.pta.search.api.HakuPyynto;
import fi.maanmittauslaitos.pta.search.api.HakuTulos.Hit;
import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;

public class NodeColorizationHitScoreHintProviderImpl implements HintProvider {
	private ValueFactory vf = SimpleValueFactory.getInstance();
	
	private Model model;
	private List<Entry<IRI, Double>> relationsAndWeights;
	private int maxColorizationDepth = 3;
	private int maxHints = 5;
	private Stemmer stemmer;
	private String language;
	
	public void setModel(Model model) {
		this.model = model;
	}
	
	public Model getModel() {
		return model;
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
	
	/**
	 * This HintProvider selects hints based on terms found in the returned hits. It searches recursively for
	 * related terms in the given ontology model. Terms are scored in the following manner:
	 *  
	 * 1. Each term found in each hit is given the score of that particular hit
	 * 2. These terms + scores form the first group to be processed
	 * 3. Create the next empty batch of terms + scores
	 * 4. For each term and score (t, s) in the group:
	 *    I) For each configured pair of relation and weight (r and w)
	 *      a) Define relation score rs = s * w
	 *      b) For each term T in the model model that have the relation r between t and T
	 *        1) Increment the score for T by rs
	 *        2) Add (T, rs) to the next batch to be processed
	 *    II) If there are recursion levels to go through, go to step 3 using the collected batch of terms + scores
	 *    III) Otherwise, go to step 5
	 * 5. Order terms based on their score (highest score first)
	 * 6. Remove terms that were already used as search terms
	 * 7. Return labels for these terms (in the selected language) as search hints
	 *
	 * In English, the algorithm follows the configured relations starting from terms found in the current hits.
	 * Each related term is scored based on the weight of the relation and the score applied to the original term
	 * during that stage. The scores in the first stage are the scores of the hits the term were mentioned in. 
	 * This process is done recursively until the configured level of recursion has been reached.   
	 */
	@Override
	public List<String> getHints(HakuPyynto pyynto, List<Hit> hits) {
		
		Set<Entry<IRI, Double>> iris = new HashSet<>();
		for (Hit hit : hits) {
			for (String uri : hit.getAbstractUris()) {
				iris.add(new AbstractMap.SimpleEntry<IRI, Double>(vf.createIRI(uri), hit.getScore()));
			}
		}
		
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
		Set<String> stemmedQueryTerms = new HashSet<>();
		for (String str : pyynto.getQuery()) {
			stemmedQueryTerms.add(getStemmer().stem(str));
		}
		
		List<String> ret = findLabelsForHints(entries, stemmedQueryTerms, getMaxHints());
		
		return ret;
	}

	private List<String> findLabelsForHints(List<Entry<IRI, Double>> entries, Set<String> notTheseLabels, int maxSize) {
		List<String> ret = new ArrayList<>();
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
				
				if (!notTheseLabels.contains(getStemmer().stem(label))) {
					ret.add(label);
	
					break;
				}
			}
			
			if (ret.size() >= maxSize) {
				break;
			}
		}
		return ret;
	}
	
	// Public for testing
	public Map<IRI, Double> colorize(Set<Entry<IRI, Double>> iris) {
		Map<IRI, Double> ret = new HashMap<>();
		
		for (Entry<IRI, Double> entry : iris) {
			increment(entry.getKey(), entry.getValue(), ret);
			colorizeResource(entry.getKey(), 1, entry.getValue(), ret);
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
