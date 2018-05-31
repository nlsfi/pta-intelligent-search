package fi.maanmittauslaitos.pta.search.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.metrics.sum.ParsedSum;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import fi.maanmittauslaitos.pta.search.api.HakuTulos.Facet;
import fi.maanmittauslaitos.pta.search.api.HakuTulos.Hit;
import fi.maanmittauslaitos.pta.search.api.HakuTulos.HitText;
import fi.maanmittauslaitos.pta.search.api.hints.HintProvider;
import fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants;

public class FacetedElasticsearchHakuKoneImpl implements HakuKone {
	private static final String FACETS_INSPIRE_KEYWORDS     = "keywordsInspire";
	private static final String FACETS_DISTRIBUTION_FORMATS = "distributionFormats";
	private static final String FACETS_TOPIC_CATEGORIES     = "topicCategories";
	private static final String FACETS_ORGANISATIONS        = "organisations";
	
	private static final String FACETS_TYPES                = "types";
	
	// Internal query only
	private static final String FACETS_TYPE_ISSERVICE       = "isService";
	private static final String FACETS_TYPE_ISDATASET       = "isDataset";
	private static final String FACETS_TYPE_ISAVOINDATA     = "isAvoindata";
	private static final String FACETS_TYPE_ISPTAAINEISTO   = "isPtaAineisto";

	private static final List<String> FACETS_TYPE_ALL = Collections.unmodifiableList(
			Arrays.asList(FACETS_TYPE_ISSERVICE, FACETS_TYPE_ISDATASET, 
					FACETS_TYPE_ISAVOINDATA, FACETS_TYPE_ISPTAAINEISTO));
			
	
	private static Logger logger = Logger.getLogger(FacetedElasticsearchHakuKoneImpl.class);
	
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
		
		// TODO: sort
		// TODO: facet filters
		
		sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		sourceBuilder.fetchSource("*", null);
		
		
		// The aggregation queries
		sourceBuilder.aggregation(AggregationBuilders.terms(FACETS_INSPIRE_KEYWORDS).field("keywordsInspire"));
		sourceBuilder.aggregation(AggregationBuilders.terms(FACETS_TOPIC_CATEGORIES).field("topicCategories"));
		sourceBuilder.aggregation(AggregationBuilders.terms(FACETS_DISTRIBUTION_FORMATS).field("distributionFormats"));
		sourceBuilder.aggregation(AggregationBuilders.terms(FACETS_ORGANISATIONS).field("organisations.organisationName"));
		
		// The "type" in the facet response is built out of these four separate queries
		sourceBuilder.aggregation(AggregationBuilders.sum(FACETS_TYPE_ISSERVICE).field("isService"));
		sourceBuilder.aggregation(AggregationBuilders.sum(FACETS_TYPE_ISDATASET).field("isDataset"));
		sourceBuilder.aggregation(AggregationBuilders.sum(FACETS_TYPE_ISAVOINDATA).field("isAvoindata"));
		sourceBuilder.aggregation(AggregationBuilders.sum(FACETS_TYPE_ISPTAAINEISTO).field("isPtaAineisto"));
		
		// Only request explanations if trace level logging is enabled
		if (logger.isTraceEnabled()) {
			sourceBuilder.explain(true);
		}
		
		SearchRequest request = new SearchRequest(PTAElasticSearchMetadataConstants.INDEX);
		request.types(PTAElasticSearchMetadataConstants.TYPE);
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

				// TODO: organisations are a mess at the moment
				osuma.getText().add(HitText.create(
						"FI",
						extractStringValue(t.getSourceAsMap().get("title")),
						extractStringValue(t.getSourceAsMap().get("abstract")),
						"TODO")); // TODO: <- organisation name
				
				osuma.getText().add(HitText.create(
						"SV",
						extractStringValue(t.getSourceAsMap().get("title_sv")),
						extractStringValue(t.getSourceAsMap().get("abstract_sv")),
						"TODO")); // TODO: <- organisation name

				osuma.getText().add(HitText.create(
						"EN",
						extractStringValue(t.getSourceAsMap().get("title_en")),
						extractStringValue(t.getSourceAsMap().get("abstract_en")),
						"TODO")); // TODO: <- organisation name

				
				osuma.setAbstractUris(extractListValue(t.getSourceAsMap().get(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_URI)));
				osuma.setAbstractTopicUris(extractListValue(t.getSourceAsMap().get(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_MAUI_URI)));
				osuma.setUrl("http://www.paikkatietohakemisto.fi/geonetwork/srv/eng/catalog.search#/metadata/" + t.getId());
				osuma.setScore((double)t.getScore());
				osuma.setDateStamp(extractStringValue(t.getSourceAsMap().get("datestamp")));
				osuma.setDistributionFormats(extractListValue(t.getSourceAsMap().get("distributionFormats")));
				osuma.setKeywordsInspire(extractListValue(t.getSourceAsMap().get("keywordsInspire")));
				osuma.setTopicCategories(extractListValue(t.getSourceAsMap().get("topicCategories")));
				
				
				
				
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
		
		// Do the facets
		Aggregations aggregations = response.getAggregations();
		
		// Basic facets
		tulos.getFacets().put(FACETS_INSPIRE_KEYWORDS,     readFacetValues(aggregations, FACETS_INSPIRE_KEYWORDS));
		tulos.getFacets().put(FACETS_ORGANISATIONS,        readFacetValues(aggregations, FACETS_ORGANISATIONS));
		tulos.getFacets().put(FACETS_TOPIC_CATEGORIES,     readFacetValues(aggregations, FACETS_TOPIC_CATEGORIES));
		tulos.getFacets().put(FACETS_DISTRIBUTION_FORMATS, readFacetValues(aggregations, FACETS_DISTRIBUTION_FORMATS));
		
		// Type facet
		tulos.getFacets().put(FACETS_TYPES, combineParsedSumFacets(aggregations, FACETS_TYPE_ALL));
		
		
		// Do the hints
		List<String> terms = getQueryProvider().getPyyntoTerms(pyynto);
		
		tulos.setHints(getHintProvider().getHints(terms, tulos.getHits()));
		
		return tulos;
	}

	private List<Facet> combineParsedSumFacets(Aggregations aggregations, List<String> facets) {
		List<Facet> typeFacetValues = new ArrayList<>();
		for (String str : facets) {
			ParsedSum sum = aggregations.get(str);
			typeFacetValues.add(Facet.create(str, (long)sum.getValue()));
		}
		return typeFacetValues;
	}

	private List<Facet> readFacetValues(Aggregations aggregations, String facetName) {
		List<Facet> facetValues = new ArrayList<>();
		ParsedStringTerms terms = aggregations.get(facetName);
		for (Bucket bucket : terms.getBuckets()) {
			facetValues.add(Facet.create(bucket.getKey().toString(), bucket.getDocCount()));
		}
		return facetValues;
	}

}
