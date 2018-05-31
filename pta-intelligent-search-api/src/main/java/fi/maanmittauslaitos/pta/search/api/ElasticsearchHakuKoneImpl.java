package fi.maanmittauslaitos.pta.search.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import fi.maanmittauslaitos.pta.search.api.HakuTulos.Hit;
import fi.maanmittauslaitos.pta.search.api.HakuTulos.HitText;
import fi.maanmittauslaitos.pta.search.api.hints.HintProvider;
import fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants;

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
	public HakuTulos haku(HakuPyynto pyynto) throws IOException {
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
		
		final AtomicInteger hitCount = new AtomicInteger(0);
		tulos.setTotalHits(hits.getTotalHits());
		hits.forEach(new Consumer<SearchHit>() {
			@Override
			public void accept(SearchHit t) {
				if (hitCount.getAndIncrement() == 0 && logger.isTraceEnabled()) {
					logger.trace("Explanation for why first hit matched:");
					logger.trace(t.getExplanation());
				}
				Hit osuma = new Hit();
				
				HitText fi = HitText.create(
						"FI",
						extractStringValue(t.getSourceAsMap().get("title")),
						extractStringValue(t.getSourceAsMap().get("abstract")),
						"TODO"); // TODO: <- organisation name
				
				osuma.getText().add(fi);
				
				osuma.setAbstractUris(extractListValue(t.getSourceAsMap().get(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_URI)));
				osuma.setAbstractTopicUris(extractListValue(t.getSourceAsMap().get(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_MAUI_URI)));
				osuma.setUrl("http://www.paikkatietohakemisto.fi/geonetwork/srv/eng/catalog.search#/metadata/" + t.getId());
				osuma.setScore((double)t.getScore());
				tulos.getHits().add(osuma);
			}

			private List<String> extractListValue(Object obj) {
				List<String> ret = new ArrayList<>();
				if (obj != null) {
					if (obj instanceof Collection<?>) {
						Collection<?> tmp = (Collection<?>)obj;
						
						for (Object o : tmp) {
							ret.add(o.toString());
						}
					} else {
						ret.add(obj.toString());
					}
				}
				return ret;
			}

			private String extractStringValue(Object obj) {
				String title;
				if (obj != null) {
					if (obj instanceof Collection<?>) {
						Collection<?> tmp = (Collection<?>)obj;
						if (tmp.size() > 0) {
							StringBuffer buf = new StringBuffer();
							int i = 0;
							for (Object o : tmp) {
								if (i > 0) {
									buf.append('\n');
								}
								buf.append(o.toString());
								i++;
							}
							title = buf.toString();
						} else {
							title = null;
						}
					} else {
						title = obj.toString();
					}
				} else {
					title = null;
				}
				return title;
			}
		});
		
		List<String> terms = getQueryProvider().getPyyntoTerms(pyynto);
		
		tulos.setHints(getHintProvider().getHints(terms, tulos.getHits()));
		
		return tulos;
	}

}
