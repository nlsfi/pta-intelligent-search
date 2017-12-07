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
	private Set<IRI> relationPredicates = new HashSet<>();
	private TextProcessor textProcessor;
	private Model model;
	
	private int ontologyLevels = 1;
	private double weightFactor = 0.5; // weightForLevel(1) = 1.0, weightForLevel(x) = weightForLevel(x-1) * weightFactor  
	private double basicWordMatchWeight = 1.0;
	
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
	
	@Override
	public SearchSourceBuilder buildSearchSource(HakuPyynto pyynto) {
		
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
		
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

		lisaaOntologisetTermit(pyynto, boolQuery);
		lisaaVapaaSanahaku(pyynto, boolQuery);

		sourceBuilder.query(boolQuery);
		
		return sourceBuilder;
	}

	private void lisaaVapaaSanahaku(HakuPyynto pyynto, BoolQueryBuilder boolQuery) {
		for (String sana : pyynto.getHakusanat()) {
			MatchQueryBuilder tmp = QueryBuilders.matchQuery("abstract", sana);
			tmp.operator(Operator.OR);
			tmp.boost((float)basicWordMatchWeight);
			boolQuery.should().add(tmp);
		}
	}

	private void lisaaOntologisetTermit(HakuPyynto pyynto, BoolQueryBuilder boolQuery) {
		Set<SearchTerm> termit = getSearchTerms(pyynto);

		for (SearchTerm term : termit) {
			MatchQueryBuilder tmp = QueryBuilders.matchQuery("abstract_uri", term.resource);
			tmp.operator(Operator.OR);
			tmp.fuzziness(Fuzziness.ZERO);
			tmp.boost((float)term.weight);
			boolQuery.should().add(tmp);
		}
	}

	private Set<SearchTerm> getSearchTerms(HakuPyynto pyynto) {
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
			for (IRI predicate : getRelationPredicates()) {
				for (Statement statement : getModel().filter(resource, predicate, null)) {
					ret.add(statement.getObject().stringValue());
				}
			}
		}
		
		return ret;
	}
	
}
