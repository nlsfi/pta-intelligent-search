package fi.maanmittauslaitos.pta.search.api.hints;

import java.util.List;

import org.elasticsearch.search.builder.SearchSourceBuilder;

import fi.maanmittauslaitos.pta.search.api.Language;

public interface HintProvider {
	/**
	 * Modify searchSourceBuilder in a way to register aggregations or other query parameters required
	 * to provide hints. Returns a hintExtractor that can be used to get the results once the query
	 * has been executed.
	 * 
	 * @param pyyntoTerms
	 * @param searchSourceBuilder
	 * @return
	 */
	public HintExtractor registerHintProvider(List<String> pyyntoTerms, SearchSourceBuilder searchSourceBuilder, Language language);
}
