package fi.maanmittauslaitos.pta.search.api.search;

import java.util.List;

import org.elasticsearch.index.query.BoolQueryBuilder;

import fi.maanmittauslaitos.pta.search.api.Language;
import fi.maanmittauslaitos.pta.search.api.model.SearchQuery;

public interface ElasticsearchQueryProvider {
	public List<String> getPyyntoTerms(SearchQuery pyynto, Language lang);
	public BoolQueryBuilder buildSearchSource(SearchQuery pyynto, Language lang, boolean focusOnRegionalHits);
	
}
