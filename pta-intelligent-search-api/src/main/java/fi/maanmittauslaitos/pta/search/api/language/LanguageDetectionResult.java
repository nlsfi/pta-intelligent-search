package fi.maanmittauslaitos.pta.search.api.language;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LanguageDetectionResult {
	private List<String> potentialLanguages;
	private Map<String, Double> scorePerLanguage;
	
	public void setPotentialLanguages(List<String> potentialLanguages) {
		this.potentialLanguages = potentialLanguages;
	}
	
	public void setScorePerLanguage(Map<String, Double> scorePerLanguage) {
		this.scorePerLanguage = scorePerLanguage;
	}
	
	/**
	 * Returns a list of potential matching languages in order of preference
	 * 
	 * @return
	 */
	public List<String> getPotentialLanguages() {
		return Collections.unmodifiableList(potentialLanguages);
	}
	
	public double getScoreForLanguage(String language) {
		Double ret = scorePerLanguage.get(language);
		if (ret == null) {
			ret = 0.0;
		}
		return ret;
	}
}