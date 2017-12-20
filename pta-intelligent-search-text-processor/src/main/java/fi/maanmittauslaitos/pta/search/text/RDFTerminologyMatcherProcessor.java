package fi.maanmittauslaitos.pta.search.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;

import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;

/**
 * This is not thread safe due to how the snowball stemmer has been done
 */
public class RDFTerminologyMatcherProcessor implements TextProcessor {
	private static Logger logger = Logger.getLogger(RDFTerminologyMatcherProcessor.class);
	
	// Setters, getters
	private Model model;
	private List<IRI> terminologyLabels;
	private Stemmer stemmer;
	
	// Private lazily populated field, access only through getDict()
	private Map<String, List<String>> dict;
	
	public void setModel(Model model) {
		this.model = model;
		this.dict = null;
	}
	
	public Model getModel() {
		return model;
	}
	
	public void setTerminologyLabels(List<IRI> terminologyLabels) {
		this.terminologyLabels = terminologyLabels;
		this.dict = null;
	}
	
	public List<IRI> getTerminologyLabels() {
		return terminologyLabels;
	}
	
	public void setStemmer(Stemmer stemmer) {
		this.stemmer = stemmer;
	}
	
	public Stemmer getStemmer() {
		return stemmer;
	}
	
	private Map<String, List<String>> getDict() {
		if (dict != null) {
			return dict;
		}
		
		dict = createDict();
		
		return dict;
	}
	
	
	private Map<String, List<String>> createDict() {
		logger.info("Building dict from RDF model");
		Map<String, List<String>> ret = new HashMap<>();
		
		for (Resource r : getModel().subjects()) {

			String resourceName = r.stringValue();
			
			// Osumat tallennetaan ensiksi settiin, jotta samaan resurssiin ei tule tuplaosumia
			Set<String> values = null;
			
			for (IRI resource : getTerminologyLabels()) {
				for (Statement statement : model.filter(r, resource, null)) {
					
					String value = statement.getObject().stringValue();
					
					value = stem(value);
					
					if (values == null) {
						values = new HashSet<>();
					}
					
					values.add(value);
				}
			}
			
			if (values != null) {
				for (String value : values) {
					List<String> tmp = ret.get(value);
					if (tmp == null) {
						tmp = new ArrayList<>();
						ret.put(value, tmp);
					}
					
					tmp.add(resourceName);
				}
			}
		}
		
		return ret;
	}

	private String stem(String str) {
		str = str.toLowerCase();
		if (getStemmer() != null) {
			str = getStemmer().stem(str);
		}
		return str;
	}
	
	@Override
	public List<String> process(String str) {
		String stemmed = stem(str);
		List<String> ret = getDict().get(stemmed);
		
		if (ret == null) {
			return Collections.emptyList();
		} else {
			return new ArrayList<>(ret);
		}
	}

}
