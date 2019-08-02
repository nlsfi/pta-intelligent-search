package fi.maanmittauslaitos.pta.search.api.search;

import java.io.IOException;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;

/**
 * This interface is required so that we can intercept search requests when producing
 * queries for the QA test module. Unfortunately this wrapper interface is required
 * because mockito is unable to successfully mock RestHighLevelClient.
 * 
 * @author v2
 */
public interface ElasticsearchQueryAPI {
	public SearchResponse search(SearchRequest request) throws IOException;
}
