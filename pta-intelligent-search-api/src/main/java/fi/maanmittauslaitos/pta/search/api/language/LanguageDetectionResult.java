package fi.maanmittauslaitos.pta.search.api.language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import fi.maanmittauslaitos.pta.search.api.Language;

public class LanguageDetectionResult {
	private List<Language> potentialLanguages;
	private Map<Language, Integer> scorePerLanguage;
	
	public void setPotentialLanguages(List<Language> potentialLanguages) {
		this.potentialLanguages = potentialLanguages;
	}
	
	public void setScorePerLanguage(Map<Language, Integer> scorePerLanguage) {
		this.scorePerLanguage = scorePerLanguage;
	}
	
	/**
	 * Returns a list of potential matching languages in order of preference
	 * 
	 * @return
	 */
	public List<Language> getPotentialLanguages() {
		return Collections.unmodifiableList(potentialLanguages);
	}
	
	public int getScoreForLanguage(Language language) {
		Integer ret = scorePerLanguage.get(language);
		if (ret == null) {
			ret = 0;
		}
		return ret;
	}
	
	public List<Language> getTopLanguages() {
		List<Language> ret = new ArrayList<>();
		if (potentialLanguages.size() > 0) {
			Integer topScore = getScoreForLanguage(potentialLanguages.get(0));
			for (int i = 0; i < potentialLanguages.size(); i++) {
				if (getScoreForLanguage(potentialLanguages.get(i)) == topScore) {
					ret.add(potentialLanguages.get(i));
				} else {
					break;
				}
			}
		}
		
		return ret;
	}
}