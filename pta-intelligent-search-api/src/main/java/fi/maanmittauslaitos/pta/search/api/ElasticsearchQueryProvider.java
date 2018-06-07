package fi.maanmittauslaitos.pta.search.api;

import java.util.List;

import org.elasticsearch.index.query.BoolQueryBuilder;

import fi.maanmittauslaitos.pta.search.api.model.SearchQuery;

public interface ElasticsearchQueryProvider {
	public List<String> getPyyntoTerms(SearchQuery pyynto);
	public BoolQueryBuilder buildSearchSource(SearchQuery pyynto);
	
}
