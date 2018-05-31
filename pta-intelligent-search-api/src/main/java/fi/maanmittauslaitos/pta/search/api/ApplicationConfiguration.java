package fi.maanmittauslaitos.pta.search.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpHost;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fi.maanmittauslaitos.pta.search.api.hints.HintProvider;
import fi.maanmittauslaitos.pta.search.api.hints.NodeColorizationHitScoreHintProviderImpl;
import fi.maanmittauslaitos.pta.search.text.RDFTerminologyMatcherProcessor;
import fi.maanmittauslaitos.pta.search.text.TextProcessingChain;
import fi.maanmittauslaitos.pta.search.text.TextProcessor;
import fi.maanmittauslaitos.pta.search.text.WordCombinationProcessor;
import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;
import fi.maanmittauslaitos.pta.search.text.stemmer.StemmerFactor;

@Configuration
public class ApplicationConfiguration {

	@Bean
	public RDFTerminologyMatcherProcessor terminologyMatcher(Model terminologyModel, Stemmer stemmer, List<IRI> terminologyLabels) throws IOException {
		RDFTerminologyMatcherProcessor ret = new RDFTerminologyMatcherProcessor();
		ret.setModel(terminologyModel);
		ret.setTerminologyLabels(terminologyLabels);
		ret.setStemmer(stemmer);
		ret.setLanguage("fi");
		
		// Initialize
		ret.getDict();
		
		return ret;
	}
	
	@Bean
	public WordCombinationProcessor wordCombinationProcessor(Model terminologyModel, Stemmer stemmer, List<IRI> terminologyLabels) throws IOException {
		WordCombinationProcessor ret = new WordCombinationProcessor();
		ret.setModel(terminologyModel);
		ret.setTerminologyLabels(terminologyLabels);
		ret.setStemmer(stemmer);
		ret.setLanguage("fi");

		// Initialize
		ret.getDict();

		return ret;
	}
	
	@Bean
	public TextProcessor queryTextProcessor(WordCombinationProcessor combinator, RDFTerminologyMatcherProcessor terminology) {
		TextProcessingChain chain = new TextProcessingChain();
		chain.getChain().add(combinator);
		chain.getChain().add(terminology);
		return chain;
	}
	
	@Bean
	public List<IRI> terminologyLabels() {
		return Arrays.asList(SKOS.PREF_LABEL, SKOS.ALT_LABEL);
	}
	
	@Bean
	public Stemmer stemmer() {
		return StemmerFactor.createStemmer();
	}
	
	@Bean
	public RestHighLevelClient elasticsearchClient() throws UnknownHostException {
		RestHighLevelClient client = new RestHighLevelClient(
		        RestClient.builder(
		                new HttpHost("localhost", 9200, "http")));
		
		return client;
	}
	
	@Bean
	public Model terminologyModel() throws IOException {
		return loadModels("/pto-skos.ttl.gz");
	}

	public static Model loadModels(String...files) throws IOException {
		Model ret = null;
		
		for (String file : files) {
			try (Reader reader = new InputStreamReader(new GZIPInputStream(ApplicationConfiguration.class.getResourceAsStream(file)))) {
				Model model = Rio.parse(reader, "", RDFFormat.TURTLE);
				
				if (ret == null) {
					ret = model;
				} else {
					ret.addAll(model);
				}
			}
		}
		
		return ret;
	}
	
	@Bean
	public HintProvider hintProvider(Model terminologyModel, Stemmer stemmer, RDFTerminologyMatcherProcessor terminologyProcessor) {
		NodeColorizationHitScoreHintProviderImpl ret = new NodeColorizationHitScoreHintProviderImpl();
		ret.setMaxColorizationDepth(2);
		ret.setStemmer(stemmer);
		ret.setModel(terminologyModel);
		ret.setLanguage("fi");
		
		List<Entry<IRI, Double>> weights = new ArrayList<>();
		
		//weights.add(new AbstractMap.SimpleEntry<>(SKOS.BROADER, 0.5));
		//weights.add(new AbstractMap.SimpleEntry<>(SKOS.NARROWER, 0.4));
		weights.add(new AbstractMap.SimpleEntry<>(SKOS.RELATED, 0.5));
		
		ret.setRelationsAndWeights(weights);
		
		return ret;

		/*
		TudhopeBindingBlocksCunliffeHintProvider ret = new TudhopeBindingBlocksCunliffeHintProvider();
		ret.setStemmer(stemmer);
		ret.setModel(terminologyModel);
		ret.setLanguage("fi");
		ret.setTerminologyProcessor(terminologyProcessor);
		
		List<Entry<IRI, Double>> relationsAndTravelCosts = new ArrayList<>();
		
		relationsAndTravelCosts.add(new AbstractMap.SimpleEntry<>(SKOS.BROADER, 0.33));
		relationsAndTravelCosts.add(new AbstractMap.SimpleEntry<>(SKOS.RELATED, 0.5));
		
		ret.setRelationsAndTravelCosts(relationsAndTravelCosts);
		
		return ret;
		*/
	}

	
	@Bean
	public HakuKone hakuKone(TextProcessor queryTextProcessor, RestHighLevelClient elasticsearchClient, Model model, HintProvider hintProvider) throws IOException {
		FacetedElasticsearchHakuKoneImpl ret = new FacetedElasticsearchHakuKoneImpl();
		ret.setClient(elasticsearchClient);
		
		OntologyElasticsearchQueryProviderImpl queryProvider = new OntologyElasticsearchQueryProviderImpl();
		queryProvider.addRelationPredicate(SKOS.NARROWER);
		queryProvider.setTextProcessor(queryTextProcessor);
		queryProvider.setModel(model);
		queryProvider.setOntologyLevels(2);
		queryProvider.setWeightFactor(0.5);
		queryProvider.setBasicWordMatchWeight(0.5);
		
		ret.setQueryProvider(queryProvider);
		ret.setHintProvider(hintProvider);
		
		return ret;
	}
}
