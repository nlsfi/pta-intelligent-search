package fi.maanmittauslaitos.pta.search.api.language;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;

import fi.maanmittauslaitos.pta.search.text.RDFTerminologyMatcherProcessor;
import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;

public class StemmingOntologyLanguageDetectorImpl implements LanguageDetector {
	
	private Map<String, Stemmer> stemmers;
	private Collection<String> supportedLanguages;
	private List<IRI> terminologyLabels;
	private Model model;

	// Lazily initialized in ensureLanguages()
	private Map<String, RDFTerminologyMatcherProcessor> terminologyProcessorsByLanguage;

	public void setSupportedLanguages(Collection<String> supportedLanguages) {
		this.supportedLanguages = supportedLanguages;
		this.terminologyProcessorsByLanguage = null;
	}
	
	public Collection<String> getSupportedLanguages() {
		return supportedLanguages;
	}
	
	public void setTerminologyLabels(List<IRI> terminologyLabels) {
		this.terminologyLabels = terminologyLabels;
		this.terminologyProcessorsByLanguage = null;
	}
	
	public List<IRI> getTerminologyLabels() {
		return terminologyLabels;
	}
	
	public void setModel(Model model) {
		this.model = model;
		this.terminologyProcessorsByLanguage = null;
	}
	
	public Model getModel() {
		return model;
	}
	
	public void setStemmers(Map<String, Stemmer> stemmers) {
		this.stemmers = stemmers;
		this.terminologyProcessorsByLanguage = null;
	}
	
	public Map<String, Stemmer> getStemmers() {
		return stemmers;
	}
	
	private Map<String, RDFTerminologyMatcherProcessor> ensureLanguageSupport() {
		Map<String, RDFTerminologyMatcherProcessor> ret = this.terminologyProcessorsByLanguage;
		if (ret == null) {
			
			ret = new HashMap<>();
			for (String language : getSupportedLanguages()) {
				RDFTerminologyMatcherProcessor tmp = new RDFTerminologyMatcherProcessor();
				tmp.setLanguage(language);
				tmp.setModel(getModel());
				tmp.setStemmer(getStemmers().get(language));
				tmp.setTerminologyLabels(getTerminologyLabels());
				ret.put(language, tmp);
			}
			
			this.terminologyProcessorsByLanguage = ret;
		}
		
		return ret;
	}
	
	
	@Override
	public LanguageDetectionResult detectLanguage(List<String> queryTerms) {
		Map<String, RDFTerminologyMatcherProcessor> processors = ensureLanguageSupport();
		
		LanguageDetectionResult ret = new LanguageDetectionResult();
		
		final Map<String, Double> scorePerLanguage = new HashMap<>();
		for (String language: processors.keySet()) {
			List<String> results = processors.get(language).process(queryTerms);
			if (results.size() > 0) {
				scorePerLanguage.put(language, new Double(results.size()));
			}
		}
		
		List<String> tmp = new ArrayList<>(scorePerLanguage.keySet());

		tmp.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return scorePerLanguage.get(o2).compareTo(scorePerLanguage.get(o1));
			}
		});
		
		ret.setScorePerLanguage(scorePerLanguage);
		ret.setPotentialLanguages(tmp);
		
		return ret;
	}

}
