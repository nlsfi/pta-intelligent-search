package fi.maanmittauslaitos.pta.search.api;

import java.io.IOException;
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

		
		SearchRequest request = new SearchRequest("catalog");
		request.types("doc");
		request.source(sourceBuilder);
		
		SearchResponse response = client.search(request);
		
		response.getHits().forEach(new Consumer<SearchHit>() {
			@Override
			public void accept(SearchHit t) {
				Osuma osuma = new Osuma();
				osuma.setUrl(t.getId());
				osuma.setRelevanssi((double)t.getScore());
				tulos.getOsumat().add(osuma);
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
