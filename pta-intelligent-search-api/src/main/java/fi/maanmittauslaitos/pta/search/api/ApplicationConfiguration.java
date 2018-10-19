package fi.maanmittauslaitos.pta.search.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpHost;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.sv.SwedishAnalyzer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fi.maanmittauslaitos.pta.search.api.hints.FacetHintProviderImpl;
import fi.maanmittauslaitos.pta.search.api.hints.HintProvider;
import fi.maanmittauslaitos.pta.search.api.language.LanguageDetector;
import fi.maanmittauslaitos.pta.search.api.language.LuceneAnalyzerStemmer;
import fi.maanmittauslaitos.pta.search.api.language.StemmingOntologyLanguageDetectorImpl;
import fi.maanmittauslaitos.pta.search.api.search.FacetedElasticsearchHakuKoneImpl;
import fi.maanmittauslaitos.pta.search.api.search.HakuKone;
import fi.maanmittauslaitos.pta.search.api.search.OntologyElasticsearchQueryProviderImpl;
import fi.maanmittauslaitos.pta.search.text.RDFTerminologyMatcherProcessor;
import fi.maanmittauslaitos.pta.search.text.TextProcessingChain;
import fi.maanmittauslaitos.pta.search.text.TextProcessor;
import fi.maanmittauslaitos.pta.search.text.WordCombinationProcessor;
import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;
import fi.maanmittauslaitos.pta.search.text.stemmer.StemmerFactory;

@Configuration
public class ApplicationConfiguration {

	@Bean
	@Qualifier("FI")
	public RDFTerminologyMatcherProcessor terminologyMatcher_FI(Model terminologyModel, @Qualifier("FI") Stemmer stemmer, List<IRI> terminologyLabels) throws IOException {
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
	@Qualifier("FI")
	public WordCombinationProcessor wordCombinationProcessor_FI(Model terminologyModel, @Qualifier("FI") Stemmer stemmer, List<IRI> terminologyLabels) throws IOException {
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
	@Qualifier("SV")
	public RDFTerminologyMatcherProcessor terminologyMatcher_SV(Model terminologyModel, @Qualifier("SV") Stemmer stemmer, List<IRI> terminologyLabels) throws IOException {
		RDFTerminologyMatcherProcessor ret = new RDFTerminologyMatcherProcessor();
		ret.setModel(terminologyModel);
		ret.setTerminologyLabels(terminologyLabels);
		ret.setStemmer(stemmer);
		ret.setLanguage("sv");
		
		// Initialize
		ret.getDict();
		
		return ret;
	}
	
	@Bean
	@Qualifier("SV")
	public WordCombinationProcessor wordCombinationProcessor_SV(Model terminologyModel, @Qualifier("SV") Stemmer stemmer, List<IRI> terminologyLabels) throws IOException {
		WordCombinationProcessor ret = new WordCombinationProcessor();
		ret.setModel(terminologyModel);
		ret.setTerminologyLabels(terminologyLabels);
		ret.setStemmer(stemmer);
		ret.setLanguage("sv");

		// Initialize
		ret.getDict();

		return ret;
	}
	
	@Bean
	@Qualifier("EN")
	public RDFTerminologyMatcherProcessor terminologyMatcher_EN(Model terminologyModel, @Qualifier("EN") Stemmer stemmer, List<IRI> terminologyLabels) throws IOException {
		RDFTerminologyMatcherProcessor ret = new RDFTerminologyMatcherProcessor();
		ret.setModel(terminologyModel);
		ret.setTerminologyLabels(terminologyLabels);
		ret.setStemmer(stemmer);
		ret.setLanguage("en");
		
		// Initialize
		ret.getDict();
		
		return ret;
	}
	
	@Bean
	@Qualifier("EN")
	public WordCombinationProcessor wordCombinationProcessor_EN(Model terminologyModel, @Qualifier("EN") Stemmer stemmer, List<IRI> terminologyLabels) throws IOException {
		WordCombinationProcessor ret = new WordCombinationProcessor();
		ret.setModel(terminologyModel);
		ret.setTerminologyLabels(terminologyLabels);
		ret.setStemmer(stemmer);
		ret.setLanguage("en");

		// Initialize
		ret.getDict();

		return ret;
	}
	
	@Bean
	public Map<Language, TextProcessor> queryTextProcessors(
			@Qualifier("FI") WordCombinationProcessor combinatorFI, 
			@Qualifier("FI") RDFTerminologyMatcherProcessor terminologyFI,
			@Qualifier("SV") WordCombinationProcessor combinatorSV, 
			@Qualifier("SV") RDFTerminologyMatcherProcessor terminologySV,
			@Qualifier("EN") WordCombinationProcessor combinatorEN, 
			@Qualifier("EN") RDFTerminologyMatcherProcessor terminologyEN) {
		
		Map<Language, TextProcessor> ret = new HashMap<>();
		
		TextProcessingChain chain_FI = new TextProcessingChain();
		chain_FI.getChain().add(combinatorFI);
		chain_FI.getChain().add(terminologyFI);
		ret.put(Language.FI, chain_FI);
		
		TextProcessingChain chain_SV = new TextProcessingChain();
		chain_SV.getChain().add(combinatorSV);
		chain_SV.getChain().add(terminologySV);
		ret.put(Language.SV, chain_SV);
		
		TextProcessingChain chain_EN = new TextProcessingChain();
		chain_EN.getChain().add(combinatorEN);
		chain_EN.getChain().add(terminologyEN);
		ret.put(Language.EN, chain_EN);
		
		return ret;
	}
	
	@Bean
	@Qualifier("PreferredLanguages")
	public List<Language> preferredLanguages() {
		return Arrays.asList(Language.FI, Language.SV, Language.EN);
	}
	
	@Bean
	public LanguageDetector languageDetector(Model terminologyModel,
			@Qualifier("FI") Stemmer stemmerFI,
			@Qualifier("SV") Stemmer stemmerSV,
			@Qualifier("EN") Stemmer stemmerEN,
			List<IRI> terminologyLabels,
			@Qualifier("PreferredLanguages") List<Language> languagesInPreferenceOrder)
	{
		StemmingOntologyLanguageDetectorImpl ret = new StemmingOntologyLanguageDetectorImpl();
		ret.setTerminologyLabels(terminologyLabels);
		ret.setSupportedLanguages(languagesInPreferenceOrder);
		
		ret.setModel(terminologyModel);
		
		Map<Language, Stemmer> stemmers = new HashMap<>();
		stemmers.put(Language.FI, stemmerFI);
		stemmers.put(Language.SV, stemmerSV);
		stemmers.put(Language.EN, stemmerEN);
		ret.setStemmers(stemmers);
		
		// Initialize
		for (RDFTerminologyMatcherProcessor processor : ret.ensureLanguageSupport().values()) {
			processor.getDict();
		}
		
		
		return ret;
	}
	
	@Bean
	public List<IRI> terminologyLabels() {
		return Arrays.asList(SKOS.PREF_LABEL, SKOS.ALT_LABEL);
	}
	
	@Bean
	@Qualifier("FI")
	public Stemmer stemmer_FI() {
		return StemmerFactory.createFinnishStemmer();
	}


	@Bean
	@Qualifier("SV")
	public Stemmer stemmer_SV() {
		// TODO: Ensure stopwords!!
		return new LuceneAnalyzerStemmer(new SwedishAnalyzer());
	}

	@Bean
	@Qualifier("EN")
	public Stemmer stemmer_EN() {
		// TODO: Ensure stopwords!!
		return new LuceneAnalyzerStemmer(new EnglishAnalyzer());
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
	public HintProvider hintProvider(Model terminologyModel) {
		FacetHintProviderImpl ret = new FacetHintProviderImpl();
		ret.setModel(terminologyModel);
		
		return ret;
	}
	
	
	/*
	@Bean
	public HintProvider hintProvider(Model terminologyModel, Stemmer stemmer, RDFTerminologyMatcherProcessor terminologyProcessor) {
		ElasticSuggestionHintProviderImpl ret = new ElasticSuggestionHintProviderImpl();
		ret.setStemmer(stemmer);
		ret.setModel(terminologyModel);
		ret.setLanguage("fi");
		
		return ret;
	}
	*/
	
	@Bean
	public HakuKone hakuKone(Map<Language, TextProcessor> queryTextProcessors, RestHighLevelClient elasticsearchClient, Model model, HintProvider hintProvider) throws IOException {
		FacetedElasticsearchHakuKoneImpl ret = new FacetedElasticsearchHakuKoneImpl();
		ret.setDistributionFormatsFacetTermMaxSize(100);
		ret.setInspireKeywordsFacetTermMaxSize(100);
		ret.setOrganisationsFacetTermMaxSize(500);
		ret.setTopicCategoriesFacetTermMaxSize(100);
		ret.setClient(elasticsearchClient);
		
		OntologyElasticsearchQueryProviderImpl queryProvider = new OntologyElasticsearchQueryProviderImpl();
		queryProvider.addRelationPredicate(SKOS.NARROWER);
		queryProvider.setTextProcessors(queryTextProcessors);
		queryProvider.setModel(model);
		queryProvider.setOntologyLevels(2);
		queryProvider.setWeightFactor(0.5);
		queryProvider.setBasicWordMatchWeight(0.5);
		
		ret.setQueryProvider(queryProvider);
		ret.setHintProvider(hintProvider);
		
		return ret;
	}
}
