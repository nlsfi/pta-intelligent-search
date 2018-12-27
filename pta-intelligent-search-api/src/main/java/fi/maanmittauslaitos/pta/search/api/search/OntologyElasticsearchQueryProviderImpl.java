package fi.maanmittauslaitos.pta.search.api.search;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;

import fi.maanmittauslaitos.pta.search.api.Language;
import fi.maanmittauslaitos.pta.search.api.model.SearchQuery;
import fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants;
import fi.maanmittauslaitos.pta.search.text.TextProcessor;


public class OntologyElasticsearchQueryProviderImpl implements ElasticsearchQueryProvider {
	private static Logger logger = Logger.getLogger(OntologyElasticsearchQueryProviderImpl.class);
	
	private Set<IRI> relationPredicates = new HashSet<>();
	
	private Set<String> requireExactWordMatch = new HashSet<>();
	private Map<Language, TextProcessor> textProcessors;
	private Model model;
	
	private int ontologyLevels = 1;
	private double weightFactor = 0.5; // weightForLevel(1) = 1.0, weightForLevel(x) = weightForLevel(x-1) * weightFactor  
	private double basicWordMatchWeight = 1.0;
	private double titleWordMatchWeight = 1.5;
	private double organisationNameMatchWeight = 1.5;
	
	private final ValueFactory vf = SimpleValueFactory.getInstance();
	

	public void setModel(Model model) {
		this.model = model;
	}
	
	public Model getModel() {
		return model;
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
	
	@Override
	public BoolQueryBuilder buildSearchSource(SearchQuery pyynto, Language lang, boolean focusOnRegionalHits) {
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

		List<String> pyyntoTerms = getPyyntoTerms(pyynto, lang);
		if (logger.isInfoEnabled()) {
			logger.info("Hakusanat: "+pyynto.getQuery()+", kieli: "+lang+", tunnistetut termit: "+pyyntoTerms);
		}
		
		addOntologicalTermQueries(pyyntoTerms, boolQuery);
		addFreetextQueries(pyynto.getQuery(), boolQuery);

		QueryBuilder regionalityQuery = createRegionalityQuery(focusOnRegionalHits);
		
		BoolQueryBuilder fullQuery = QueryBuilders.boolQuery();
		fullQuery.must(boolQuery);
		fullQuery.should(regionalityQuery);
		
		return fullQuery;
	}

	private QueryBuilder createRegionalityQuery(boolean focusOnRegionalHits) {
		double finlandAreaWGS84 = 112.15985284328191068268;
		
		float regionalityBoost = 10.0f;

		RangeQueryBuilder q =  QueryBuilders
				.rangeQuery("geographicBoundingBoxArea")
				.boost(regionalityBoost);
		
		if (focusOnRegionalHits) {
			return q.lte(finlandAreaWGS84/2.0);
		} else {
			return q.gt(finlandAreaWGS84/2.0);
		}
	}

	private void addFreetextQueries(Collection<String> terms, BoolQueryBuilder boolQuery) {
		for (String sana : terms) {
			QueryBuilder tmp = freetextQuery("abstract", sana, basicWordMatchWeight);
			boolQuery.should().add(tmp);
		}
		
		for (String sana : terms) {
			QueryBuilder tmp = freetextQuery("title", sana, titleWordMatchWeight);
			boolQuery.should().add(tmp);
		}
		
		for (String sana : terms) {
			QueryBuilder tmp = freetextQuery("organisationName_text", sana, organisationNameMatchWeight);
			boolQuery.should().add(tmp);
		}
	}

	private QueryBuilder freetextQuery(String field, String word, double weight) {
		QueryBuilder tmp;
		
		if (getRequireExactWordMatch().contains(word)) {
			tmp = QueryBuilders.termQuery(field, word);
		} else {
			tmp = QueryBuilders.fuzzyQuery(field, word);
		}
		tmp.boost((float)weight);
		return tmp;
	}

	private void addOntologicalTermQueries(Collection<String> termit, BoolQueryBuilder boolQuery) {
		for (String term : termit) {
			QueryBuilder tmp;

			tmp = QueryBuilders.termQuery(PTAElasticSearchMetadataConstants.FIELD_KEYWORDS_URI, term);
			tmp.boost(1.25f);
			boolQuery.should().add(tmp);

			tmp = QueryBuilders.termQuery(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_MAUI_URI, term);
			tmp.boost(1.25f);
			boolQuery.should().add(tmp);

			
			tmp = QueryBuilders.termQuery(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_URI, term);
			tmp.boost(1.0f);
			boolQuery.should().add(tmp);

			
			tmp = QueryBuilders.termQuery(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_MAUI_URI_PARENTS, term);
			tmp.boost(0.75f);
			boolQuery.should().add(tmp);

			tmp = QueryBuilders.termQuery(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_URI_PARENTS, term);
			tmp.boost(0.5f);
			boolQuery.should().add(tmp);
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
