package fi.maanmittauslaitos.pta.search.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.metrics.sum.ParsedSum;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import fi.maanmittauslaitos.pta.search.api.HakuPyynto.Sort;
import fi.maanmittauslaitos.pta.search.api.HakuTulos.Facet;
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
			
	private static final List<String> FACETS_TERMS_ALL = Collections.unmodifiableList(
			Arrays.asList(FACETS_INSPIRE_KEYWORDS, FACETS_DISTRIBUTION_FORMATS,
					FACETS_TOPIC_CATEGORIES, FACETS_ORGANISATIONS
					));
	
	
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
	public HakuTulos haku(HakuPyynto pyynto, Language lang) throws IOException {
		HakuTulos tulos = new HakuTulos();
		
		if (pyynto.getQuery().size() == 0) {
			return new HakuTulos();
		}
		
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		sourceBuilder.fetchSource("*", null);
		
		BoolQueryBuilder query = getQueryProvider().buildSearchSource(pyynto);
		
		sourceBuilder.query(query);
		
		// Paging
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
		
		for (Sort sort : pyynto.getSort()) {
			SortBuilder<?> sortBuilder = null;
			if (sort.getField().equals("title")) {
				switch(lang) {
				case SV:
					sortBuilder = SortBuilders.fieldSort("titleSvSort");
					break;
				case EN:
					sortBuilder = SortBuilders.fieldSort("titleEnSort");
					break;
				default:
					sortBuilder = SortBuilders.fieldSort("titleFiSort");
					
				}
				
			} else if (sort.getField().equals("datestamp")) {
				sortBuilder = SortBuilders.fieldSort("datestamp");
			} else if (sort.getField().equals("datestamp")) {
				sortBuilder = SortBuilders.fieldSort("organisations.organisationName");
			} else if (sort.getField().equals("score")) {
				sortBuilder = SortBuilders.fieldSort("_score");
			}
			
			
			if (sortBuilder == null) {
				throw new IllegalArgumentException("Sort field '"+sort.getField()+"' not recognized");
			}
			
			if ("asc".equals(sort.getOrder())) {
				sortBuilder.order(SortOrder.ASC);
			} else if ("desc".equals(sort.getOrder())) {
				sortBuilder.order(SortOrder.DESC);
			} else {
				throw new IllegalArgumentException("Sort order '"+sort.getOrder()+"' not recognized");
			}
			
			sourceBuilder.sort(sortBuilder);
		}
		
		
		// Facet filter

		// This is really important, otherwise having a facet "filter" will change the behavior
		// of the "should" query
		query.minimumShouldMatch(1);
		
		for (String facetTerm : FACETS_TERMS_ALL) {
			List<String> values = pyynto.getFacets().get(facetTerm);
			if (values != null) {
				
				for (String value : values) {
					TermQueryBuilder term = QueryBuilders.termQuery(facetTerm, value);
					
					
					query.filter().add(term);
				}
			}
		}
		
		List<String> types = pyynto.getFacets().get(FACETS_TYPES);
		if (types != null) {
			for (String type : types) {
				TermQueryBuilder term = QueryBuilders.termQuery(type, true);
				query.filter().add(term);
			}
		}

		
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
		
		tulos.setTotalHits(hits.getTotalHits());
		hits.forEach(new SearchHitToAPIHit(tulos));
		
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
