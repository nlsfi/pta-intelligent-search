package fi.maanmittauslaitos.pta.search.api;

import com.google.common.collect.ImmutableMap;
import fi.maanmittauslaitos.pta.search.api.hints.FacetHintProviderImpl;
import fi.maanmittauslaitos.pta.search.api.hints.HintProvider;
import fi.maanmittauslaitos.pta.search.api.language.LanguageDetector;
import fi.maanmittauslaitos.pta.search.api.language.LuceneAnalyzerStemmer;
import fi.maanmittauslaitos.pta.search.api.language.StemmingOntologyLanguageDetectorImpl;
import fi.maanmittauslaitos.pta.search.api.region.RegionNameContainer;
import fi.maanmittauslaitos.pta.search.api.region.RegionNameContainerImpl;
import fi.maanmittauslaitos.pta.search.api.search.ElasticsearchQueryAPI;
import fi.maanmittauslaitos.pta.search.api.search.FacetedElasticsearchHakuKoneImpl;
import fi.maanmittauslaitos.pta.search.api.search.HakuKone;
import fi.maanmittauslaitos.pta.search.api.search.OntologyElasticsearchQueryProviderImpl;
import fi.maanmittauslaitos.pta.search.text.*;
import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;
import fi.maanmittauslaitos.pta.search.text.stemmer.StemmerFactory;
import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.sv.SwedishAnalyzer;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.*;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

@Configuration
public class ApplicationConfiguration {
	private static Logger logger = Logger.getLogger(ApplicationConfiguration.class);

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
			@Qualifier("FI") StopWordsProcessor stopwordsFI,
			@Qualifier("FI") WordCombinationProcessor combinatorFI,
			@Qualifier("FI") RDFTerminologyMatcherProcessor terminologyFI,
			@Qualifier("SV") StopWordsProcessor stopwordsSV,
			@Qualifier("SV") WordCombinationProcessor combinatorSV,
			@Qualifier("SV") RDFTerminologyMatcherProcessor terminologySV,
			@Qualifier("EN") StopWordsProcessor stopwordsEN,
			@Qualifier("EN") WordCombinationProcessor combinatorEN,
			@Qualifier("EN") RDFTerminologyMatcherProcessor terminologyEN) {

		Map<Language, TextProcessor> ret = new HashMap<>();

		TextProcessingChain chain_FI = new TextProcessingChain();
		chain_FI.getChain().add(stopwordsFI);
		chain_FI.getChain().add(combinatorFI);
		chain_FI.getChain().add(terminologyFI);
		ret.put(Language.FI, chain_FI);

		TextProcessingChain chain_SV = new TextProcessingChain();
		chain_SV.getChain().add(stopwordsSV);
		chain_SV.getChain().add(combinatorSV);
		chain_SV.getChain().add(terminologySV);
		ret.put(Language.SV, chain_SV);

		TextProcessingChain chain_EN = new TextProcessingChain();
		chain_EN.getChain().add(stopwordsEN);
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
											 @Qualifier("FI") StopWordsProcessor stopwordsFI,
											 @Qualifier("SV") StopWordsProcessor stopwordsSV,
											 @Qualifier("EN") StopWordsProcessor stopwordsEN,
											 List<IRI> terminologyLabels,
											 @Qualifier("PreferredLanguages") List<Language> languagesInPreferenceOrder) {
		StemmingOntologyLanguageDetectorImpl ret = new StemmingOntologyLanguageDetectorImpl();
		ret.setTerminologyLabels(terminologyLabels);
		ret.setSupportedLanguages(languagesInPreferenceOrder);

		ret.setModel(terminologyModel);

		Map<Language, Stemmer> stemmers = new HashMap<>();
		stemmers.put(Language.FI, stemmerFI);
		stemmers.put(Language.SV, stemmerSV);
		stemmers.put(Language.EN, stemmerEN);
		ret.setStemmers(stemmers);

		Map<Language, StopWordsProcessor> stopwords = new HashMap<>();
		stopwords.put(Language.FI, stopwordsFI);
		stopwords.put(Language.SV, stopwordsSV);
		stopwords.put(Language.EN, stopwordsEN);
		ret.setStopWordsProcessors(stopwords);

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
		return new LuceneAnalyzerStemmer(new SwedishAnalyzer());
	}

	@Bean
	@Qualifier("EN")
	public Stemmer stemmer_EN() {
		return new LuceneAnalyzerStemmer(new EnglishAnalyzer());
	}

	@Bean
	@Qualifier("StemmersPerLanguage")
	public Map<Language, Stemmer> stemmersPerLanguage(
			@Qualifier("FI") Stemmer stemmer_FI,
			@Qualifier("SV") Stemmer stemmer_SV,
			@Qualifier("EN") Stemmer stemmer_EN) {
		return ImmutableMap.of(Language.FI, stemmer_FI, Language.SV, stemmer_SV, Language.EN, stemmer_EN);
	}

	@Bean
	@Qualifier("FI")
	public StopWordsProcessor stopwords_FI() throws IOException {
		StopWordsProcessor stopWords = new StopWordsProcessor();
		stopWords.loadWords(
				ApplicationConfiguration.class.getResourceAsStream(
						"/nls.fi/pta-intelligent-search/stopwords-fi.txt"));
		return stopWords;
	}

	@Bean
	@Qualifier("SV")
	public StopWordsProcessor stopwords_SV() throws IOException {
		StopWordsProcessor stopWords = new StopWordsProcessor();
		stopWords.loadWords(
				ApplicationConfiguration.class.getResourceAsStream(
						"/nls.fi/pta-intelligent-search/stopwords-sv.txt"));
		return stopWords;
	}

	@Bean
	@Qualifier("EN")
	public StopWordsProcessor stopwords_EN() throws IOException {
		StopWordsProcessor stopWords = new StopWordsProcessor();
		stopWords.loadWords(
				ApplicationConfiguration.class.getResourceAsStream(
						"/nls.fi/pta-intelligent-search/stopwords-en.txt"));
		return stopWords;
	}


	@Bean
	@Profile("default")
	public ElasticsearchQueryAPI elasticsearchClient() throws UnknownHostException {
		return new ElasticsearchQueryAPIImpl();
	}

	public static class ElasticsearchQueryAPIImpl implements ElasticsearchQueryAPI, DisposableBean {
		private RestHighLevelClient client;

		public ElasticsearchQueryAPIImpl() {
			client = new RestHighLevelClient(
					RestClient.builder(new HttpHost("localhost", 9200, "http")));
		}

		@Override
		public void destroy() throws Exception {
			client.close();
		}

		@Override
		public SearchResponse search(SearchRequest request) throws IOException {
			return client.search(request);
		}
	}

	@Bean
	@Profile("createIntegrationTestQueries")
	public MockElasticsearchQueryAPI elasticsearchQueryStoreClient() throws IOException {
		return new MockElasticsearchQueryAPI();
	}

	public static class MockElasticsearchQueryAPI implements ElasticsearchQueryAPI {
		private String lastQueryAsJSON;

		@Override
		public SearchResponse search(SearchRequest request) throws IOException {
			lastQueryAsJSON = request.source().toString();
			return null;
		}

		public String getLastQueryAsJSON() {
			return lastQueryAsJSON;
		}
	}

	@Bean
	public Model terminologyModel() throws IOException {
		return loadModels("/pto-skos.ttl.gz");
	}

	public static Model loadModels(String... files) throws IOException {
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
	@Qualifier("exactMatchWords")
	public Set<String> exactMatchWords() throws IOException {
		InputStream is = null;

		try {
			String filename = System.getProperty("EXACT_MATCH_FILE");
			if (filename != null && filename.length() > 0) {
				logger.info("Loading exact word match file from '" + filename + "' (configured via system property EXACT_MATCH_FILE)");
				is = new FileInputStream(filename);
			}

			if (is == null) {
				filename = "./exact_match_words.txt";
				File f = new File(filename);
				if (f.exists()) {
					logger.info("Loading exact word match file 'exact_match_words.txt' from process CWD");
					is = new FileInputStream(f);
				}
			}

			if (is == null) {
				logger.info("Loading exact word match file 'exact_match_words.txt' from classpath");
				is = ApplicationConfiguration.class.getResourceAsStream("/exact_match_words.txt");
			}

			Set<String> ret = new HashSet<>();

			BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

			String line;
			while ((line = br.readLine()) != null) {
				int idx = line.indexOf('#');
				if (idx != -1) {
					line = line.substring(0, idx);
				}
				line = line.trim().toLowerCase();
				if (line.length() > 0) {
					ret.add(line);
				}
			}

			return ret;

		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	@Bean
	public RegionNameContainer regionNameContainer(@Qualifier("StemmersPerLanguage") Map<Language, Stemmer> stemmers) {
		String countryResource = "data/well_known_location_bboxes_countries.json";
		String regionResource = "data/well_known_location_bboxes_regions.json";
		String subRegionResource = "data/well_known_location_bboxes_subregions.json";
		String municipalityResource = "data/well_known_location_bboxes_municipalities.json";
		RegionNameContainerImpl regionNameContainer = new RegionNameContainerImpl(countryResource, regionResource, subRegionResource, municipalityResource, stemmers);
		regionNameContainer.init();
		return regionNameContainer;
	}

	@Bean
	public HakuKone hakuKone(Map<Language, TextProcessor> queryTextProcessors, ElasticsearchQueryAPI elasticsearchAPI,
							 Model model, HintProvider hintProvider, @Qualifier("exactMatchWords") Set<String> exactMatchWords,
							 @Qualifier("StemmersPerLanguage") Map<Language, Stemmer> stemmers, RegionNameContainer regionNameContainer) throws IOException {
		FacetedElasticsearchHakuKoneImpl ret = new FacetedElasticsearchHakuKoneImpl();
		ret.setDistributionFormatsFacetTermMaxSize(100);
		ret.setInspireKeywordsFacetTermMaxSize(100);
		ret.setOrganisationsFacetTermMaxSize(500);
		ret.setTopicCategoriesFacetTermMaxSize(100);
		ret.setClient(elasticsearchAPI);

		OntologyElasticsearchQueryProviderImpl queryProvider = new OntologyElasticsearchQueryProviderImpl();
		queryProvider.addRelationPredicate(SKOS.NARROWER);
		queryProvider.setTextProcessors(queryTextProcessors);
		queryProvider.setModel(model);
		queryProvider.setOntologyLevels(2);
		queryProvider.setWeightFactor(0.5);
		queryProvider.setBasicWordMatchWeight(0.5);
		queryProvider.setStemmers(stemmers);
		queryProvider.setRegionNameContainer(regionNameContainer);

		queryProvider.setRequireExactWordMatch(exactMatchWords);

		ret.setQueryProvider(queryProvider);
		ret.setHintProvider(hintProvider);

		return ret;
	}
}
