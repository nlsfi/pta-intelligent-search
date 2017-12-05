package fi.maanmittauslaitos.pta.search.api;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.rdf4j.model.Model;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import fi.maanmittauslaitos.pta.search.api.HakuTulos.Osuma;
import fi.maanmittauslaitos.pta.search.text.RDFTerminologyMatcherProcessor;

public abstract class AbstractElasticsearchHakuKoneImpl implements HakuKone {
	private RDFTerminologyMatcherProcessor textProcessor;
	private RestHighLevelClient client;
	private Model model;
	
	public void setClient(RestHighLevelClient client) {
		this.client = client;
	}
	
	public RestHighLevelClient getClient() {
		return client;
	}
	
	public void setTextProcessor(RDFTerminologyMatcherProcessor textProcessor) {
		this.textProcessor = textProcessor;
	}
	
	public RDFTerminologyMatcherProcessor getTextProcessor() {
		return textProcessor;
	}
	
	public void setModel(Model model) {
		this.model = model;
	}
	
	public Model getModel() {
		return model;
	}
	
	@Override
	public HakuTulos haku(HakuPyynto pyynto) throws IOException {
		HakuTulos tulos = new HakuTulos();
		
		Set<SearchTerm> termit = getSearchTerms(pyynto);
		if (termit.size() == 0) {
			return new HakuTulos();
		}
		
		SearchSourceBuilder sourceBuilder = buildSearchSource(termit);
		sourceBuilder.from(0);
		sourceBuilder.size(10);
		sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		sourceBuilder.fetchSource("*", null);

		SearchRequest request = new SearchRequest("catalog");
		request.types("doc");
		request.source(sourceBuilder);
		
		SearchResponse response = client.search(request);
		
		response.getHits().forEach(new Consumer<SearchHit>() {
			@Override
			public void accept(SearchHit t) {
				Osuma osuma = new Osuma();
				
				
				osuma.setTitle(extractStringValue(t.getSourceAsMap().get("title")));
				osuma.setAbstractText(extractStringValue(t.getSourceAsMap().get("abstract")));
				osuma.setUrl(t.getId());
				osuma.setRelevanssi((double)t.getScore());
				tulos.getOsumat().add(osuma);
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

	protected abstract SearchSourceBuilder buildSearchSource(Set<SearchTerm> termit);
	

	protected abstract Set<SearchTerm> getSearchTerms(HakuPyynto pyynto);
	
	public class SearchTerm {
		public final String resource;
		public final double weight;
		
		public SearchTerm(String term, double weight) {
			this.resource = term;
			this.weight = weight;
		}
	}
}
