package fi.maanmittauslaitos.pta.search.codelist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.SKOS;

/**
 * Takes as an input a model such as http://www.paikkatietohakemisto.fi/geonetwork/srv/eng/thesaurus.download?ref=external.theme.inspire-theme
 * 
 * @author v2
 *
 */
public class InspireThemesImpl implements InspireThemes {
	private static Logger logger = Logger.getLogger(InspireThemesImpl.class);
	
	private Model model;
	private String canonicalLanguage;
	private List<String> heuristicSearchLanguagePriority = new ArrayList<>();
	
	// Lazily initialized
	private Map<String, TextsInLanguage> perLanguage;
	private List<String> fullHeuristicSearchLanguages;
	
	public void setModel(Model model) {
		this.model = model;
		this.perLanguage = null; // invalidate cache
	}
	
	public Model getModel() {
		return model;
	}
	
	@Override
	public void setCanonicalLanguage(String canonicalLanguage) {
		this.canonicalLanguage = canonicalLanguage;
		this.perLanguage = null; // invalidate cache
	}
	
	@Override
	public String getCanonicalLanguage() {
		return canonicalLanguage;
	}
	
	public void setHeuristicSearchLanguagePriority(String...languages) {
		this.heuristicSearchLanguagePriority = Arrays.asList(languages);
		this.perLanguage = null; // invalidate cache
	}


	@Override
	public String getCanonicalName(String text, String language) {
		TextsInLanguage tmp = ensureDict().get(language);
		
		if (tmp == null) {
			logger.warn("getCanonicalName() called with unknown language '"+language+"', returning null");
			return null;
		}
		
		return tmp.languageStringToCanonical.get(text);
	}
	
	private Map<String, TextsInLanguage> ensureDict() {
		Map<String, TextsInLanguage> ret = this.perLanguage;
		if (ret == null) {
			ret = new HashMap<>();
			
			
			for (Resource r : getModel().subjects()) {
				if (!(r instanceof IRI)) {
					continue;
				}
				Map<String, String> labelPerLanguage = new HashMap<>();
				
				
				for (Statement s : getModel().filter(r, SKOS.PREF_LABEL, null)) {
					Value v = s.getObject();
					if (v instanceof Literal) {
						Literal l = (Literal)v;
						
						if (l.getLanguage().isPresent()) {
							
							labelPerLanguage.put(l.getLanguage().get(), l.getLabel());
						}
					}
				}
				
				
				String canonicalLabel = labelPerLanguage.get(getCanonicalLanguage());
				if (canonicalLabel == null) {
					logger.warn("RDF Model contains no label in canonical language ("+getCanonicalLanguage()+") for resource "+r.stringValue()+", skipping it");
					continue;
				}
				
				for (String language : labelPerLanguage.keySet()) {
					TextsInLanguage til = ret.get(language);
					if (til == null) {
						til = new TextsInLanguage();
						ret.put(language, til);
					}
					
					til.languageStringToCanonical.put(labelPerLanguage.get(language), canonicalLabel);
				}
			}
			
			
			// Produce an ordered list of languages for the heuristic search (=when no language context is available)
			List<String> fullHeuristicSearchLanguages = new ArrayList<>();
			
			fullHeuristicSearchLanguages.addAll(this.heuristicSearchLanguagePriority);
			for (String language : ret.keySet()) {
				if (!fullHeuristicSearchLanguages.contains(language)) {
					fullHeuristicSearchLanguages.add(language);
				}
			}
			
			this.perLanguage = ret;
			this.fullHeuristicSearchLanguages = fullHeuristicSearchLanguages;
		}
		
		
		
		return ret;
	}
	
	@Override
	public String getCanonicalName(String text) {
		Map<String, TextsInLanguage> dict = ensureDict();
		
		for (String lang : this.fullHeuristicSearchLanguages) {
			String ret = dict.get(lang).languageStringToCanonical.get(text);
			if (ret != null) {
				return ret;
			}
		}
		
		return null;
	}
	
	private class TextsInLanguage {
		private Map<String, String> languageStringToCanonical = new HashMap<>();
	}
	
}
