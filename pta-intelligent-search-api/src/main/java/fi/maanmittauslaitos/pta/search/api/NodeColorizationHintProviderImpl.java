package fi.maanmittauslaitos.pta.search.api;

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
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import fi.maanmittauslaitos.pta.search.api.HakuTulos.Hit;

public class NodeColorizationHintProviderImpl implements HintProvider {
	private ValueFactory vf = SimpleValueFactory.getInstance();
	
	private Model model;
	private List<Entry<IRI, Double>> relationsAndWeights;
	private int maxColorizationDepth = 3;
	private int maxHints = 5;
	
	public void setModel(Model model) {
		this.model = model;
	}
	
	public Model getModel() {
		return model;
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
	public List<String> getHints(HakuPyynto pyynto, List<Hit> hits) {
		
		Set<IRI> iris = new HashSet<>();
		for (Hit hit : hits) {
			for (String uri : hit.getAbstractUris()) {
				iris.add(vf.createIRI(uri));
			}
		}
		
		Map<IRI, Double> colorized = colorize(iris);
		
		List<Entry<IRI, Double>> entries = new ArrayList<>(colorized.entrySet());
		
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
		
		List<String> ret = new ArrayList<>();
		for (int i = 0; i < getMaxHints() && i < entries.size(); i++) {
			ret.add(entries.get(i).getKey().stringValue());
		}
		
		return ret;
	}
	
	// Public for testing
	public Map<IRI, Double> colorize(Set<IRI> resources) {
		Map<IRI, Double> ret = new HashMap<>();
		
		for (IRI resource : resources) {
			
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
