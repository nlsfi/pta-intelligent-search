package fi.maanmittauslaitos.pta.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import com.entopix.maui.stemmers.FinnishStemmer;
import com.entopix.maui.stopwords.StopwordsFinnish;

import fi.maanmittauslaitos.pta.search.csw.CSWHarvesterSource;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.XPathFieldExtractorConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.XPathFieldExtractorConfiguration.FieldExtractorType;
import fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants;
import fi.maanmittauslaitos.pta.search.index.DocumentSink;
import fi.maanmittauslaitos.pta.search.index.ElasticsearchDocumentSink;
import fi.maanmittauslaitos.pta.search.metadata.ISOMetadataExtractorConfigurationFactory;
import fi.maanmittauslaitos.pta.search.metadata.ISOMetadataFields;
import fi.maanmittauslaitos.pta.search.text.ExistsInSetProcessor;
import fi.maanmittauslaitos.pta.search.text.MauiTextProcessor;
import fi.maanmittauslaitos.pta.search.text.RDFTerminologyMatcherProcessor;
import fi.maanmittauslaitos.pta.search.text.RegexProcessor;
import fi.maanmittauslaitos.pta.search.text.StopWordsProcessor;
import fi.maanmittauslaitos.pta.search.text.TerminologyExpansionProcessor;
import fi.maanmittauslaitos.pta.search.text.TextProcessingChain;
import fi.maanmittauslaitos.pta.search.text.TextSplitterProcessor;
import fi.maanmittauslaitos.pta.search.text.WordCombinationProcessor;
import fi.maanmittauslaitos.pta.search.text.stemmer.StemmerFactor;

public class HarvesterConfig {
	public HarvesterSource getCSWSource() {
		HarvesterSource source = new CSWHarvesterSource();
		source.setBatchSize(10);
		source.setOnlineResource("http://paikkatietohakemisto.fi/geonetwork/srv/en/csw");
		//source.setOnlineResource("http://demo.paikkatietohakemisto.fi/geonetwork/srv/en/csw");
		
		return source;
	}
	
	
	public DocumentProcessor getCSWRecordProcessor() throws ParserConfigurationException, IOException  {
		ISOMetadataExtractorConfigurationFactory factory = new ISOMetadataExtractorConfigurationFactory();
		
		// Basic configuration
		DocumentProcessingConfiguration configuration = factory.createMetadataDocumentProcessingConfiguration();
		
		
		// Ontology models and and text processors
		Model model = getTerminologyModel();
		RDFTerminologyMatcherProcessor terminologyProcessor = createTerminologyMatcher(model);
		WordCombinationProcessor wordCombinationProcessor = createWordCombinationProcessor(model);
		
		
		// Set up abstract processor (abstract => abstract_uri)
		TextProcessingChain abstractChain = createAbstractProcessingChain(terminologyProcessor, wordCombinationProcessor);
		configuration.getTextProcessingChains().put("abstractProcessor", abstractChain);
		
		FieldExtractorConfiguration abstractUri = configuration.getFieldExtractor(ISOMetadataFields.ABSTRACT).copy();
		abstractUri.setField(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_URI);
		abstractUri.setTextProcessorName("abstractProcessor");
		
		configuration.getFieldExtractors().add(abstractUri);
		
		// Abstract processor that determines the parents of
		TextProcessingChain abstractParentsChain = createAbstractParentProcessingChain(terminologyProcessor, wordCombinationProcessor, model);
		configuration.getTextProcessingChains().put("abstractParentProcessor", abstractParentsChain);
		
		FieldExtractorConfiguration abstract2Uri = configuration.getFieldExtractor(ISOMetadataFields.ABSTRACT).copy();
		abstract2Uri.setField(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_URI_PARENTS);
		abstract2Uri.setTextProcessorName("abstractParentProcessor");
		
		configuration.getFieldExtractors().add(abstract2Uri);
		// tätä ei vielä ajeta, jokin bugaa
		
		
		// Set up maui chain for abstract (abstract => abstract_maui_uri)
		MauiTextProcessor mauiTextProcessor = createMauiProcessingChain(); 
		TextProcessingChain mauiChain = new TextProcessingChain();
		mauiChain.getChain().add(mauiTextProcessor);
		
		configuration.getTextProcessingChains().put("mauiProcessor", mauiChain);
		
		FieldExtractorConfiguration abstractMauiUri = configuration.getFieldExtractor(ISOMetadataFields.ABSTRACT).copy();
		abstractMauiUri.setField(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_MAUI_URI);
		abstractMauiUri.setTextProcessorName("mauiProcessor");
		
		configuration.getFieldExtractors().add(abstractMauiUri);
		
		// Set up maui chain for abstract (abstract => abstract_maui_uri_parents)
		TextProcessingChain mauiParentsChain = createMauiParentProcessingChain(mauiTextProcessor, model);
		configuration.getTextProcessingChains().put("mauiParentsProcessor", mauiParentsChain);
		
		FieldExtractorConfiguration abstractMauiParentsUri = configuration.getFieldExtractor(ISOMetadataFields.ABSTRACT).copy();
		abstractMauiParentsUri.setField(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_MAUI_URI_PARENTS);
		abstractMauiParentsUri.setTextProcessorName("mauiParentsProcessor");
		
		configuration.getFieldExtractors().add(abstractMauiParentsUri);
		
		
		// Keyword to uri detection (keywords => keywords_uri) 
		TextProcessingChain keywordChain = createKeywordProcessingChain(terminologyProcessor, wordCombinationProcessor);
		configuration.getTextProcessingChains().put("keywordProcessor", keywordChain);
		
		FieldExtractorConfiguration keywordsUri = configuration.getFieldExtractor(ISOMetadataFields.KEYWORDS_ALL).copy();
		keywordsUri.setField(PTAElasticSearchMetadataConstants.FIELD_KEYWORDS_URI);
		keywordsUri.setTextProcessorName("keywordProcessor");
		
		configuration.getFieldExtractors().add(keywordsUri);
		
		// Extra matchers that are used to match things not matched by pta-intelligent-search-metadata-extractor
		
		// Annotated keywords
		TextProcessingChain isInOntologyFilterProcessor = createIsInOntologyProcessor(terminologyProcessor);
		
		configuration.getTextProcessingChains().put("isInOntologyFilterProcessor", isInOntologyFilterProcessor);
		
		
		XPathFieldExtractorConfiguration annotatedKeywordExtractor = new XPathFieldExtractorConfiguration();
		annotatedKeywordExtractor.setField("annotated_keywords_uri");
		annotatedKeywordExtractor.setType(FieldExtractorType.ALL_MATCHING_VALUES);
		annotatedKeywordExtractor.setXpath("//gmd:descriptiveKeywords/*/gmd:keyword/gmx:Anchor/@xlink:href");
		
		annotatedKeywordExtractor.setTextProcessorName("isInOntologyFilterProcessor");
		
		configuration.getFieldExtractors().add(annotatedKeywordExtractor);
		
		// Copy the title to titleSort (which is a keyword field to allow sorting)
		FieldExtractorConfiguration titleFiSort = configuration.getFieldExtractor(ISOMetadataFields.TITLE).copy();
		titleFiSort.setField("titleFiSort");
		configuration.getFieldExtractors().add(titleFiSort);
		
		FieldExtractorConfiguration titleSvSort = configuration.getFieldExtractor(ISOMetadataFields.TITLE_SV).copy();
		titleSvSort.setField("titleSvSort");
		configuration.getFieldExtractors().add(titleSvSort);
		
		FieldExtractorConfiguration titleEnSort = configuration.getFieldExtractor(ISOMetadataFields.TITLE_EN).copy();
		titleEnSort.setField("titleEnSort");
		configuration.getFieldExtractors().add(titleEnSort);
		
		
		return factory.getDocumentProcessorFactory().createProcessor(configuration);
		
	}


	private TextProcessingChain createMauiParentProcessingChain(MauiTextProcessor mauiTextProcessor, Model model) {
		TextProcessingChain ret = new TextProcessingChain();
		
		ret.getChain().add(mauiTextProcessor);

		TerminologyExpansionProcessor expansionProcessor = new TerminologyExpansionProcessor();
		expansionProcessor.setModel(model);
		expansionProcessor.setPredicates(Arrays.asList(SKOS.BROADER));
		
		ret.getChain().add(expansionProcessor);
		
		return ret;
	}


	private TextProcessingChain createIsInOntologyProcessor(RDFTerminologyMatcherProcessor terminologyProcessor) {
		TextProcessingChain isInOntologyFilterProcessor = new TextProcessingChain();
		
		ExistsInSetProcessor allowInOntology = new ExistsInSetProcessor();
		allowInOntology.setAcceptedStrings(terminologyProcessor.getAllKnownTerms());
		isInOntologyFilterProcessor.getChain().add(allowInOntology);
		return isInOntologyFilterProcessor;
	}


	private TextProcessingChain createKeywordProcessingChain(RDFTerminologyMatcherProcessor terminologyProcessor,
			WordCombinationProcessor wordCombinationProcessor) {
		TextProcessingChain keywordChain = new TextProcessingChain();
		RegexProcessor whitespaceRemoval = new RegexProcessor();
		whitespaceRemoval.setPattern(Pattern.compile("^\\s*$"));
		whitespaceRemoval.setIncludeMatches(false);
		
		keywordChain.getChain().add(new TextSplitterProcessor());
		keywordChain.getChain().add(wordCombinationProcessor);
		keywordChain.getChain().add(whitespaceRemoval);
		keywordChain.getChain().add(terminologyProcessor);
		return keywordChain;
	}


	private TextProcessingChain createAbstractProcessingChain(RDFTerminologyMatcherProcessor terminologyProcessor,
			WordCombinationProcessor wordCombinationProcessor) throws IOException {
		TextProcessingChain ret = new TextProcessingChain();
		ret.getChain().add(new TextSplitterProcessor());
		ret.getChain().add(wordCombinationProcessor);
		
		StopWordsProcessor stopWordsProcessor = new StopWordsProcessor();
		try (InputStreamReader isr = new InputStreamReader(HarvesterConfig.class.getResourceAsStream("/stopwords-fi.txt"))) {
			List<String> stopWords = new ArrayList<>();
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				String tmp = line.toLowerCase().trim();
				if (tmp.length() > 0) {
					stopWords.add(tmp);
				}
			}
			stopWordsProcessor.setStopwords(stopWords);
		}
		ret.getChain().add(stopWordsProcessor);
		ret.getChain().add(terminologyProcessor);
		return ret;
	}
	
	private TextProcessingChain createAbstractParentProcessingChain(RDFTerminologyMatcherProcessor terminologyProcessor,
			WordCombinationProcessor wordCombinationProcessor, Model model) throws IOException {
		TextProcessingChain ret = createAbstractProcessingChain(terminologyProcessor, wordCombinationProcessor);

		TerminologyExpansionProcessor expansionProcessor = new TerminologyExpansionProcessor();
		expansionProcessor.setModel(model);
		expansionProcessor.setPredicates(Arrays.asList(SKOS.BROADER));
		
		ret.getChain().add(expansionProcessor);
		
		return ret;
	}
	

	private MauiTextProcessor createMauiProcessingChain() {
		MauiTextProcessor mauiTextProcessor = new MauiTextProcessor();
		mauiTextProcessor.setMauiStemmer(new FinnishStemmer());
		mauiTextProcessor.setMauiStopWords(new StopwordsFinnish());
		
		mauiTextProcessor.setModelResource("/paikkatietohakemisto-pto.model");
		mauiTextProcessor.setVocabularyName("pto-skos.rdf.gz");
		mauiTextProcessor.setVocabularyFormat("skos");
		mauiTextProcessor.setLanguage("fi");
		
		mauiTextProcessor.init();
		return mauiTextProcessor;
	}


	private RDFTerminologyMatcherProcessor createTerminologyMatcher(Model model) throws IOException {
		RDFTerminologyMatcherProcessor ret = new RDFTerminologyMatcherProcessor();
		ret.setModel(model);
		ret.setTerminologyLabels(Arrays.asList(SKOS.PREF_LABEL, SKOS.ALT_LABEL));
		ret.setStemmer(StemmerFactor.createStemmer());
		ret.setLanguage("fi");
		return ret;
	}

	private WordCombinationProcessor createWordCombinationProcessor(Model model) throws IOException {
		WordCombinationProcessor ret = new WordCombinationProcessor();
		ret.setModel(model);
		ret.setTerminologyLabels(Arrays.asList(SKOS.PREF_LABEL, SKOS.ALT_LABEL));
		ret.setStemmer(StemmerFactor.createStemmer());
		ret.setLanguage("fi");
		return ret;
	}
	
	public DocumentSink getDocumentSink() {
		ElasticsearchDocumentSink ret = new ElasticsearchDocumentSink();
		ret.setHostname("localhost");
		ret.setPort(9200);
		ret.setProtocol("http");
		
		ret.setIndex(PTAElasticSearchMetadataConstants.INDEX);
		ret.setType(PTAElasticSearchMetadataConstants.TYPE);
		
		ret.setIdField("@id");
		
		return ret;
	}
	

	Model getTerminologyModel() throws IOException {
		return loadModels(getTerminologyModelResourceName());
	}

	private String getTerminologyModelResourceName() {
		return "/pto-skos.ttl.gz";
	}
	
	private static Model loadModels(String...files) throws IOException {
		Model ret = null;
		
		for (String file : files) {
			try (Reader reader = new InputStreamReader(new GZIPInputStream(HarvesterConfig.class.getResourceAsStream(file)))) {
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
	
}
