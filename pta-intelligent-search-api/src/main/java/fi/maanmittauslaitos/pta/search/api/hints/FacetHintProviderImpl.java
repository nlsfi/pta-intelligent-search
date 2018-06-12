package fi.maanmittauslaitos.pta.search.api.hints;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.significant.ParsedSignificantStringTerms;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms.Bucket;
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import fi.maanmittauslaitos.pta.search.api.model.SearchResult.Hit;
import fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants;

public class FacetHintProviderImpl extends AbstractHintProvider {
	private static Logger logger = Logger.getLogger(FacetHintProviderImpl.class);
	private static Random random = new Random();
	
	private final String aggregationFieldName;
	
	public FacetHintProviderImpl() {
		aggregationFieldName = "facetHint."+random.nextInt();
	}
	
	@Override
	public HintExtractor registerHintProvider(List<String> pyyntoTerms, SearchSourceBuilder searchSourceBuilder) {
		
		SignificantTermsAggregationBuilder aggregationBuilder = AggregationBuilders.significantTerms(aggregationFieldName);
		aggregationBuilder.field(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_URI);
		
		searchSourceBuilder.aggregation(aggregationBuilder);
		
		
		return new FacetHintProviderHintExtractor(pyyntoTerms);
	}

	public class FacetHintProviderHintExtractor implements HintExtractor {
		private List<String> pyyntoTerms;
		
		public FacetHintProviderHintExtractor(List<String> pyyntoTerms) {
			this.pyyntoTerms = pyyntoTerms;
		}
		
		@Override
		public List<String> getHints(SearchResponse response, List<Hit> hits) {
			List<Entry<IRI, Double>> tmp = new ArrayList<>();
			logger.trace("Determining hints from aggregation response");
			ParsedSignificantStringTerms aggregationResponse = response.getAggregations().get(aggregationFieldName);
			
			for (Bucket bucket : aggregationResponse.getBuckets()) {
				
				if (logger.isTraceEnabled()) {
					logger.trace(bucket.getKey()+" => "+bucket.getDocCount()+" ("+bucket.getSignificanceScore()+")");
				}
				
				String uri = bucket.getKey().toString();
				double score = bucket.getSignificanceScore();
				
				tmp.add(new AbstractMap.SimpleEntry<IRI, Double>(vf.createIRI(uri), score));
			}
			
			return determineLabelsForHintsKeepResultsWithinMaxSize(tmp, pyyntoTerms);
		}
	}
}
