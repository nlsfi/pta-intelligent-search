package fi.maanmittauslaitos.pta.search.api.search;

import fi.maanmittauslaitos.pta.search.api.Language;
import fi.maanmittauslaitos.pta.search.api.model.SearchQuery;
import fi.maanmittauslaitos.pta.search.api.region.RegionNameContainer;
import fi.maanmittauslaitos.pta.search.api.region.RegionNameSearchResult;
import fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants;
import fi.maanmittauslaitos.pta.search.text.TextProcessor;
import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants.FIELD_BEST_MATCHING_REGIONS;
import static fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants.FIELD_BEST_MATCHING_REGIONS_NAME;
import static fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants.FIELD_BEST_MATCHING_REGIONS_SCORE;


public class OntologyElasticsearchQueryProviderImpl implements ElasticsearchQueryProvider {
	private static Logger logger = Logger.getLogger(OntologyElasticsearchQueryProviderImpl.class);

	private Set<IRI> relationPredicates = new HashSet<>();

	private Set<String> requireExactWordMatch = new HashSet<>();
	private Map<Language, TextProcessor> textProcessors;
	private Model model;

	private int ontologyLevels = 1;
	private double weightFactor = 0.5; // weightForLevel(1) = 1.0, weightForLevel(x) = weightForLevel(x-1) * weightFactor
	private double basicWordMatchWeight = 1.0;
	private double basicWordMatchFuzzyWeight = 0.4;
	private double titleWordMatchWeight = 1.5;
	private double titleWordMatchFuzzyWeight = 1.1;
	private double organisationNameMatchWeight = 1.5;
	private double organisationNameMatchFuzzyWeight = 1.1;
	private double spatialWeight = 1.0;

	private final ValueFactory vf = SimpleValueFactory.getInstance();
	private Map<Language, Stemmer> stemmers;
	private RegionNameContainer regionNameContainer;



	public void setModel(Model model) {
		this.model = model;
	}

	public Model getModel() {
		return model;
	}

	public void setStemmers(Map<Language, Stemmer> stemmers) {
		this.stemmers = stemmers;
	}

	public void setRegionNameContainer(RegionNameContainer regionNameContainer) {
		this.regionNameContainer = regionNameContainer;
	}

	public void setRequireExactWordMatch(Set<String> requireExactWordMatch) {
		this.requireExactWordMatch = requireExactWordMatch;
	}

	public Set<String> getRequireExactWordMatch() {
		return requireExactWordMatch;
	}

	public void setOntologyLevels(int ontologyLevels) {
		this.ontologyLevels = ontologyLevels;
	}

	public int getOntologyLevels() {
		return ontologyLevels;
	}

	public void setWeightFactor(double weightFactor) {
		this.weightFactor = weightFactor;
	}

	public double getWeightFactor() {
		return weightFactor;
	}

	public void setRelationPredicates(Set<IRI> relationPredicates) {
		this.relationPredicates = relationPredicates;
	}

	public Set<IRI> getRelationPredicates() {
		return relationPredicates;
	}

	public void addRelationPredicate(IRI relationPredicate) {
		this.relationPredicates.add(relationPredicate);
	}

	public void addRelationPredicate(String relationPredicate) {
		addRelationPredicate(vf.createIRI(relationPredicate));
	}

	public void setTextProcessors(Map<Language, TextProcessor> textProcessors) {
		this.textProcessors = textProcessors;
	}

	public Map<Language, TextProcessor> getTextProcessors() {
		return textProcessors;
	}

	public double getBasicWordMatchWeight() {
		return basicWordMatchWeight;
	}

	public void setBasicWordMatchWeight(double basicWordMatchWeight) {
		this.basicWordMatchWeight = basicWordMatchWeight;
	}

	public double getTitleWordMatchWeight() {
		return titleWordMatchWeight;
	}

	public void setTitleWordMatchWeight(double titleWordMatchWeight) {
		this.titleWordMatchWeight = titleWordMatchWeight;
	}

	public double getOrganisationNameMatchWeight() {
		return organisationNameMatchWeight;
	}

	public void setOrganisationNameMatchWeight(double organisationNameMatchWeight) {
		this.organisationNameMatchWeight = organisationNameMatchWeight;
	}

	public void setBasicWordMatchFuzzyWeight(double basicWordMatchFuzzyWeight) {
		this.basicWordMatchFuzzyWeight = basicWordMatchFuzzyWeight;
	}

	public void setTitleWordMatchFuzzyWeight(double titleWordMatchFuzzyWeight) {
		this.titleWordMatchFuzzyWeight = titleWordMatchFuzzyWeight;
	}

	public void setOrganisationNameMatchFuzzyWeight(double organisationNameMatchFuzzyWeight) {
		this.organisationNameMatchFuzzyWeight = organisationNameMatchFuzzyWeight;
	}

	public void setSpatialWeight(double spatialWeight) {
		this.spatialWeight = spatialWeight;
	}

	@Override
	public BoolQueryBuilder buildSearchSource(SearchQuery pyynto, Language lang) {
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

		List<String> pyyntoTerms = getPyyntoTerms(pyynto, lang);
		if (logger.isInfoEnabled()) {
			logger.info("Hakusanat: " + pyynto.getQuery() + ", kieli: " + lang + ", tunnistetut termit: " + pyyntoTerms);
		}


		addOntologicalTermQueries(pyyntoTerms, boolQuery);
		addFreetextQueries(pyynto.getQuery(), boolQuery);


		BoolQueryBuilder fullQuery = QueryBuilders.boolQuery();
		fullQuery.should(boolQuery);

		RegionNameSearchResult regionNameSearchResult = searchQueryForRegionNames(pyynto, regionNameContainer, lang);
		if (regionNameSearchResult.hasRegionName()) {
			QueryBuilder spatialQuery = createSpatialQuery(regionNameSearchResult, regionNameContainer, lang);
			fullQuery.should().add(spatialQuery);
		}

		return fullQuery;
	}

	private QueryBuilder createSpatialQuery(RegionNameSearchResult regionNameSearchResult, RegionNameContainer regionNameContainer, Language lang) {
		String fieldName = FIELD_BEST_MATCHING_REGIONS + ".%s." + FIELD_BEST_MATCHING_REGIONS_NAME;
		String scoreName = FIELD_BEST_MATCHING_REGIONS + ".%s." + FIELD_BEST_MATCHING_REGIONS_SCORE;

		DisMaxQueryBuilder spatialDisMax = QueryBuilders.disMaxQuery();

		regionNameContainer.getRegionNamesByRegionType().forEach((regionType, regionNames) -> {
			if (regionNames.contains(regionNameSearchResult.getParsedRegion())) {
				BoolQueryBuilder query = QueryBuilders.boolQuery();

				FunctionScoreQueryBuilder.FilterFunctionBuilder[] functions = {
						new FunctionScoreQueryBuilder.FilterFunctionBuilder(
								ScoreFunctionBuilders.fieldValueFactorFunction(String.format(scoreName, regionType.getType()))
										.factor((float) spatialWeight))
				};
				query.filter().add(QueryBuilders.termQuery(String.format(fieldName, regionType.getType()), regionNameSearchResult.getParsedRegion()));
				query.should().add(QueryBuilders.functionScoreQuery(functions));
				spatialDisMax.add(query);
			}
		});

		return spatialDisMax;
	}


	private RegionNameSearchResult searchQueryForRegionNames(SearchQuery pyynto, RegionNameContainer regionNameContainer, Language lang) {
		return pyynto.getQuery().stream()//
				.map(queryTerm -> RegionNameSearchResult.executeSearch(queryTerm, stemmers.get(lang).stem(queryTerm.toLowerCase()), regionNameContainer, lang))
				.filter(RegionNameSearchResult::hasRegionName)
				.findFirst()
				.orElse(RegionNameSearchResult.NO_REGION_FOUND);
	}

	private void addFreetextQueries(Collection<String> words, BoolQueryBuilder boolQuery) {
		for (String sana : words) {
			DisMaxQueryBuilder disMax = QueryBuilders.disMaxQuery();

			disMax.add(freetextQuery("abstract", sana, basicWordMatchWeight, basicWordMatchFuzzyWeight));
			disMax.add(freetextQuery("title", sana, titleWordMatchWeight, titleWordMatchFuzzyWeight));
			disMax.add(freetextQuery("organisationName_text", sana, organisationNameMatchWeight, organisationNameMatchFuzzyWeight));

			boolQuery.should().add(disMax);
		}
	}

	private QueryBuilder freetextQuery(String field, String word, double weight, double fuzzyWeight) {
		DisMaxQueryBuilder query = QueryBuilders.disMaxQuery();
		query.add(QueryBuilders.termQuery(field, word)
				.boost((float) weight));

		if (!getRequireExactWordMatch().contains(word)) {
			query.add(
					QueryBuilders.fuzzyQuery(field, word)
							.boost((float) fuzzyWeight)
			);
		}
		return query;
	}

	private void addOntologicalTermQueries(Collection<String> terms, BoolQueryBuilder boolQuery) {
		for (String term : terms) {
			DisMaxQueryBuilder disMax = QueryBuilders.disMaxQuery();

			disMax.add(QueryBuilders
					.termQuery(PTAElasticSearchMetadataConstants.FIELD_KEYWORDS_URI, term)
					.boost(1.25f));

			disMax.add(QueryBuilders
					.termQuery(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_MAUI_URI, term)
					.boost(1.25f));

			disMax.add(QueryBuilders
					.termQuery(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_URI, term)
					.boost(1.0f));

			disMax.add(QueryBuilders
					.termQuery(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_MAUI_URI_PARENTS, term)
					.boost(0.75f));

			disMax.add(QueryBuilders
					.termQuery(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_URI_PARENTS, term)
					.boost(0.5f));

			boolQuery.should().add(disMax);
		}
	}

	@Override
	public List<String> getPyyntoTerms(SearchQuery pyynto, Language lang) {
		return getTextProcessors().get(lang).process(pyynto.getQuery());
	}

	public Set<String> haeAlakasitteet(Set<String> ylakasitteet) {
		Set<String> ret = new HashSet<>();

		for (String ylakasite : ylakasitteet) {
			IRI resource = vf.createIRI(ylakasite);
			for (IRI predicate : getRelationPredicates()) {
				for (Statement statement : getModel().filter(resource, predicate, null)) {
					ret.add(statement.getObject().stringValue());
				}
			}
		}

		return ret;
	}


}
