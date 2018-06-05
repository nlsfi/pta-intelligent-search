package fi.maanmittauslaitos.pta.search.api;

import java.util.List;

import org.elasticsearch.index.query.BoolQueryBuilder;

public interface ElasticsearchQueryProvider {
	public List<String> getPyyntoTerms(HakuPyynto pyynto);
	public BoolQueryBuilder buildSearchSource(HakuPyynto pyynto);
	
}
