package fi.maanmittauslaitos.pta.search.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants;
import fi.maanmittauslaitos.pta.search.text.TextProcessor;


public class OntologyElasticsearchQueryProviderImpl implements ElasticsearchQueryProvider {
	private static Logger logger = Logger.getLogger(OntologyElasticsearchQueryProviderImpl.class);
	
	private Set<IRI> relationPredicates = new HashSet<>();
	
	private TextProcessor textProcessor;
	private Model model;
	
	private int ontologyLevels = 1;
	private double weightFactor = 0.5; // weightForLevel(1) = 1.0, weightForLevel(x) = weightForLevel(x-1) * weightFactor  
	private double basicWordMatchWeight = 1.0;
	
	private int maxQueryTermsToElasticsearch = 500;
	
	private final ValueFactory vf = SimpleValueFactory.getInstance();
	

	public void setModel(Model model) {
		this.model = model;
	}
	
	public Model getModel() {
		return model;
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
	
	public void setTextProcessor(TextProcessor textProcessor) {
		this.textProcessor = textProcessor;
	}
	
	public TextProcessor getTextProcessor() {
		return textProcessor;
	}
	
	public double getBasicWordMatchWeight() {
		return basicWordMatchWeight;
	}
	
	public void setBasicWordMatchWeight(double basicWordMatchWeight) {
		this.basicWordMatchWeight = basicWordMatchWeight;
	}
	
	public int getMaxQueryTermsToElasticsearch() {
		return maxQueryTermsToElasticsearch;
	}
	
	public void setMaxQueryTermsToElasticsearch(int maxQueryTermsToElasticsearch) {
		this.maxQueryTermsToElasticsearch = maxQueryTermsToElasticsearch;
	}
	
	@Override
	public BoolQueryBuilder buildSearchSource(HakuPyynto pyynto) {
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

		List<String> pyyntoTerms = getPyyntoTerms(pyynto);
		if (logger.isInfoEnabled()) {
			logger.info("Hakusanat: "+pyynto.getQuery()+", tunnistetut termit: "+pyyntoTerms);
		}
		
		lisaaOntologisetTermit(pyyntoTerms, boolQuery);
		lisaaVapaaSanahaku(pyyntoTerms, boolQuery);

		if (boolQuery.should().size() > getMaxQueryTermsToElasticsearch()) {
			List<QueryBuilder> qb = boolQuery.should().subList(0, getMaxQueryTermsToElasticsearch());
			logger.warn("Query has more terms ("+boolQuery.should().size()+") than allowed ("+getMaxQueryTermsToElasticsearch()+"), throwing out some terms");
			boolQuery.should().retainAll(qb);
		}
		
		return boolQuery;
	}

	private void lisaaVapaaSanahaku(Collection<String> terms, BoolQueryBuilder boolQuery) {
		for (String sana : terms) {
			MatchQueryBuilder tmp;
			
			tmp = QueryBuilders.matchQuery("abstract", sana);
			tmp.operator(Operator.OR);
			tmp.boost((float)basicWordMatchWeight);
			boolQuery.should().add(tmp);
		}
	}

	private void lisaaOntologisetTermit(Collection<String> termit, BoolQueryBuilder boolQuery) {
		for (String term : termit) {
			QueryBuilder tmp;
			
			tmp = QueryBuilders.termQuery(PTAElasticSearchMetadataConstants.FIELD_KEYWORDS_URI, term);
			tmp.boost(1.0f);
			boolQuery.should().add(tmp);

			tmp = QueryBuilders.termQuery(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_MAUI_URI, term);
			tmp.boost(1.0f);
			boolQuery.should().add(tmp);

			
			tmp = QueryBuilders.termQuery(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_URI, term);
			tmp.boost(0.75f);
			boolQuery.should().add(tmp);

			
			tmp = QueryBuilders.termQuery(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_MAUI_URI_PARENTS, term);
			tmp.boost(1.0f);
			boolQuery.should().add(tmp);

			tmp = QueryBuilders.termQuery(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_URI_PARENTS, term);
			tmp.boost(0.75f);
			boolQuery.should().add(tmp);
			
		}
	}

	@Override
	public List<String> getPyyntoTerms(HakuPyynto pyynto) {
		return getTextProcessor().process(pyynto.getQuery());
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
