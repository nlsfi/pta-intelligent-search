package fi.maanmittauslaitos.pta.search.api.hints;

import java.util.List;

import org.elasticsearch.action.search.SearchResponse;

import fi.maanmittauslaitos.pta.search.api.model.SearchResult.Hit;

public interface HintExtractor {
	/**
	 * Returns hits from a elastic search SearchReponse + parsed hits
	 * 
	 * @param response
	 * @param hits
	 * @return
	 */
	public List<String> getHints(SearchResponse response, List<Hit> hits);
}
