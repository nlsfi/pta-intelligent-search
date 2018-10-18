package fi.maanmittauslaitos.pta.search.api.language;

import java.util.List;

public interface LanguageDetector {

	public LanguageDetectionResult detectLanguage(List<String> queryTerms);
}
