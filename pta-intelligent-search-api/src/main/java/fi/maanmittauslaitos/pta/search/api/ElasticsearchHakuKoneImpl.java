package fi.maanmittauslaitos.pta.search.api;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import fi.maanmittauslaitos.pta.search.api.hints.HintProvider;

public class ElasticsearchHakuKoneImpl implements HakuKone {
	private static Logger logger = Logger.getLogger(ElasticsearchHakuKoneImpl.class);
	
	private RestHighLevelClient client;
	
	private ElasticsearchQueryProvider queryProvider;
	private HintProvider hintProvider;
	
	public void setQueryProvider(ElasticsearchQueryProvider queryProvider) {
		this.queryProvider = queryProvider;
	}
	
	public ElasticsearchQueryProvider getQueryProvider() {
		return queryProvider;
	}
	
	public void setClient(RestHighLevelClient client) {
		this.client = client;
	}
	
	public RestHighLevelClient getClient() {
		return client;
	}
	
	public void setHintProvider(HintProvider hintProvider) {
		this.hintProvider = hintProvider;
	}
	
	public HintProvider getHintProvider() {
		return hintProvider;
	}
	
	@Override
	public HakuTulos haku(HakuPyynto pyynto, Language lang) throws IOException {
		HakuTulos tulos = new HakuTulos();
		
		if (pyynto.getQuery().size() == 0) {
			return new HakuTulos();
		}
		
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.query(getQueryProvider().buildSearchSource(pyynto));
		
		if (pyynto.getSkip() != null) {
			tulos.setStartIndex(pyynto.getSkip());
			sourceBuilder.from(pyynto.getSkip().intValue());
		} else {
			tulos.setStartIndex(0l);
			sourceBuilder.from(0);
		}
		
		if (pyynto.getPageSize() != null) {
			sourceBuilder.size(pyynto.getPageSize().intValue());
		} else {
			sourceBuilder.size(10);
		}
		
		sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		sourceBuilder.fetchSource("*", null);
		
		// Only request explanations if trace level logging is enabled
		if (logger.isTraceEnabled()) {
			sourceBuilder.explain(true);
		}
		
		SearchRequest request = new SearchRequest("catalog");
		request.types("doc");
		request.source(sourceBuilder);
		
		SearchResponse response = client.search(request);
		
		SearchHits hits = response.getHits();
		
		tulos.setTotalHits(hits.getTotalHits());
		hits.forEach(new SearchHitToAPIHit(tulos));
		
		List<String> terms = getQueryProvider().getPyyntoTerms(pyynto);
		
		tulos.setHints(getHintProvider().getHints(terms, tulos.getHits()));
		
		return tulos;
	}

}
