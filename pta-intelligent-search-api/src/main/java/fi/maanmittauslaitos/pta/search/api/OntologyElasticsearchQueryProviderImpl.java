package fi.maanmittauslaitos.pta.search.api;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import fi.maanmittauslaitos.pta.search.text.TextProcessor;


public class OntologyElasticsearchQueryProviderImpl implements ElasticsearchQueryProvider {
	private IRI relationPredicate;
	private TextProcessor textProcessor;
	private Model model;
	
	private int ontologyLevels = 1;
	private double weightFactor = 0.5; // weightForLevel(1) = 1.0, weightForLevel(x) = weightForLevel(x-1) * weightFactor  
	
	
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
	
	public void setRelationPredicate(IRI relationPredicate) {
		this.relationPredicate = relationPredicate;
	}
	
	public void setRelationPredicate(String relationPredicate) {
		this.relationPredicate = vf.createIRI(relationPredicate);
	}
	
	public IRI getRelationPredicate() {
		return relationPredicate;
	}
	
	public void setTextProcessor(TextProcessor textProcessor) {
		this.textProcessor = textProcessor;
	}
	
	public TextProcessor getTextProcessor() {
		return textProcessor;
	}
	
	@Override
	public SearchSourceBuilder buildSearchSource(Set<SearchTerm> termit) {
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
		
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

		// TODO: avainsanat, teksti
		for (SearchTerm term : termit) {
			MatchQueryBuilder tmp = QueryBuilders.matchQuery("abstract_uri", term.resource);
			tmp.operator(Operator.OR);
			tmp.fuzziness(Fuzziness.ZERO);
			tmp.boost((float)term.weight);
			boolQuery.should().add(tmp);
		}

		sourceBuilder.query(boolQuery);
		
		return sourceBuilder;
	}

	@Override
	public Set<SearchTerm> getSearchTerms(HakuPyynto pyynto) {
		Set<SearchTerm> termit = new HashSet<>();
		
		Set<String> prosessoidutYlakasitteet = new HashSet<>(); // Vältetään uudelleenkäsittely
		Set<String> prosessoimattomatYlakasitteet = new HashSet<>();
		
		double weight = 1.0;
		for (String hakusana : pyynto.getHakusanat()) {
			for (String termi : getTextProcessor().process(hakusana)) {
				termit.add(new SearchTerm(termi, weight));
				prosessoimattomatYlakasitteet.add(termi);
			}
		}
		
		for (int level = 0; level < ontologyLevels; level++) {
			prosessoidutYlakasitteet.addAll(prosessoidutYlakasitteet);
			
			Set<String> alakasitteet = haeAlakasitteet(prosessoimattomatYlakasitteet);
			weight *= weightFactor;
			
			prosessoimattomatYlakasitteet = new HashSet<>();
			
			for (String termi : alakasitteet) {
				termit.add(new SearchTerm(termi, weight));
				if (!prosessoidutYlakasitteet.contains(termi)) {
					prosessoimattomatYlakasitteet.add(termi);
				}
			}
		}
		
		return termit;
	}

	public Set<String> haeAlakasitteet(Set<String> ylakasitteet) {
		Set<String> ret = new HashSet<>();
		
		for (String ylakasite : ylakasitteet) {
			IRI resource = vf.createIRI(ylakasite);
			for (Statement statement : getModel().filter(resource, getRelationPredicate(), null)) {
				ret.add(statement.getObject().stringValue());
			}
		}
		
		return ret;
	}
	
}
