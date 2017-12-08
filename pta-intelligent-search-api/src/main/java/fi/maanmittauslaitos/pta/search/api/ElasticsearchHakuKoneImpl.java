package fi.maanmittauslaitos.pta.search.api;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import fi.maanmittauslaitos.pta.search.api.ElasticsearchQueryProvider.SearchTerm;
import fi.maanmittauslaitos.pta.search.api.HakuTulos.Hit;

public class ElasticsearchHakuKoneImpl implements HakuKone {
	private RestHighLevelClient client;
	
	private ElasticsearchQueryProvider queryProvider;
	
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
	
	@Override
	public HakuTulos haku(HakuPyynto pyynto) throws IOException {
		HakuTulos tulos = new HakuTulos();
		
		if (pyynto.getQuery().size() == 0) {
			return new HakuTulos();
		}
		
		SearchSourceBuilder sourceBuilder = getQueryProvider().buildSearchSource(pyynto);
		
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

		SearchRequest request = new SearchRequest("catalog");
		request.types("doc");
		request.source(sourceBuilder);
		
		SearchResponse response = client.search(request);
		
		SearchHits hits = response.getHits();
		
		tulos.setTotalHits(hits.getTotalHits());
		hits.forEach(new Consumer<SearchHit>() {
			@Override
			public void accept(SearchHit t) {
				Hit osuma = new Hit();
				
				
				osuma.setTitle(extractStringValue(t.getSourceAsMap().get("title")));
				osuma.setAbstractText(extractStringValue(t.getSourceAsMap().get("abstract")));
				osuma.setUrl("http://www.paikkatietohakemisto.fi/geonetwork/srv/eng/catalog.search#/metadata/" + t.getId());
				osuma.setScore((double)t.getScore());
				tulos.getHits().add(osuma);
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
		
		// TODO: vinkit
		
		return tulos;
	}

}
