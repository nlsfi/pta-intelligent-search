package fi.maanmittauslaitos.pta.search.api.language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;

import fi.maanmittauslaitos.pta.search.api.Language;
import fi.maanmittauslaitos.pta.search.text.RDFTerminologyMatcherProcessor;
import fi.maanmittauslaitos.pta.search.text.StopWordsProcessor;
import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;

public class StemmingOntologyLanguageDetectorImpl implements LanguageDetector {
	private static Logger logger = Logger.getLogger(StemmingOntologyLanguageDetectorImpl.class);
	
	private Map<Language, Stemmer> stemmers = new HashMap<>();
	private Collection<Language> supportedLanguages = Collections.emptyList();
	private List<IRI> terminologyLabels = Collections.emptyList();
	private Model model;

	private Map<Language, StopWordsProcessor> stopWordsProcessors = new HashMap<>();
	
	// Lazily initialized in ensureLanguages()
	private Map<Language, RDFTerminologyMatcherProcessor> terminologyProcessorsByLanguage;
	
	public void setSupportedLanguages(Collection<Language> supportedLanguages) {
		this.supportedLanguages = supportedLanguages;
		this.terminologyProcessorsByLanguage = null;
	}
	
	public Collection<Language> getSupportedLanguages() {
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
	
	public void setStemmers(Map<Language, Stemmer> stemmers) {
		this.stemmers = stemmers;
		this.terminologyProcessorsByLanguage = null;
	}
	
	public Map<Language, Stemmer> getStemmers() {
		return stemmers;
	}
	
	public void setStopWordsProcessors(Map<Language, StopWordsProcessor> stopWordsProcessors) {
		this.stopWordsProcessors = stopWordsProcessors;
	}
	
	public Map<Language, StopWordsProcessor> getStopWordsProcessors() {
		return stopWordsProcessors;
	}
	
	public Map<Language, RDFTerminologyMatcherProcessor> ensureLanguageSupport() {
		Map<Language, RDFTerminologyMatcherProcessor> ret = this.terminologyProcessorsByLanguage;
		if (ret == null) {
			
			ret = new HashMap<>();
			for (Language language : getSupportedLanguages()) {
				RDFTerminologyMatcherProcessor tmp = new RDFTerminologyMatcherProcessor();
				tmp.setLanguage(language.getLowercaseLanguageCode());
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
		Map<Language, RDFTerminologyMatcherProcessor> processors = ensureLanguageSupport();
		
		LanguageDetectionResult ret = new LanguageDetectionResult();
		
		final Map<Language, Integer> scorePerLanguage = new HashMap<>();
		if (logger.isDebugEnabled()) {
			logger.debug("Detecting language for query: "+queryTerms);
		}
		
		for (Language language: processors.keySet()) {
			int score = 0;
			if (logger.isDebugEnabled()) {
				logger.debug("\t"+language+" =>");
			}
			
			List<String> queryTermsForThisLanguage = queryTerms;
			
			StopWordsProcessor stopWords = getStopWordsProcessors().get(language);
			if (stopWords != null) {
				queryTermsForThisLanguage = stopWords.process(queryTermsForThisLanguage);
				if (logger.isDebugEnabled()) {
					logger.debug(" terms after stop words: "+queryTermsForThisLanguage+" => ");
				}
			}
			
			for (String term : queryTermsForThisLanguage) {
				List<String> results = processors.get(language).process(Arrays.asList(term));
				if (logger.isDebugEnabled()) {
					logger.debug("\t\t"+term+" = "+results);
				}
				if (results.size() > 0) {
					score++;
				}
			}
			
			if (score > 0) {
				scorePerLanguage.put(language, score);
			}
		}
		
		List<Language> tmp = new ArrayList<>(scorePerLanguage.keySet());

		tmp.sort(new Comparator<Language>() {
			@Override
			public int compare(Language o1, Language o2) {
				return scorePerLanguage.get(o2).compareTo(scorePerLanguage.get(o1));
			}
		});
		
		ret.setScorePerLanguage(scorePerLanguage);
		ret.setPotentialLanguages(tmp);
		
		if (scorePerLanguage.size() == 0) {
			logger.debug("Could not detect any language");
		} else {
			logger.debug("Language priority order: "+tmp);
		}
		
		return ret;
	}

}
