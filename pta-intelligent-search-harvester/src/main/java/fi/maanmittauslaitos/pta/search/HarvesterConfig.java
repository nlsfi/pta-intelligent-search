package fi.maanmittauslaitos.pta.search;

import com.entopix.maui.stemmers.FinnishStemmer;
import com.entopix.maui.stopwords.StopwordsFinnish;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.annotations.VisibleForTesting;
import fi.maanmittauslaitos.pta.search.codelist.InspireThemesImpl;
import fi.maanmittauslaitos.pta.search.codelist.ODFOrganisationNameNormaliserImpl;
import fi.maanmittauslaitos.pta.search.codelist.OrganisationNormaliser;
import fi.maanmittauslaitos.pta.search.codelist.OrganisationNormaliserTextRewriter;
import fi.maanmittauslaitos.pta.search.documentprocessor.CustomExtractor;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfigurationImpl;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfigurationImpl.FieldExtractorType;
import fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants;
import fi.maanmittauslaitos.pta.search.index.DocumentSink;
import fi.maanmittauslaitos.pta.search.index.ElasticsearchDocumentSink;
import fi.maanmittauslaitos.pta.search.index.LocalArchiveDocumentSink;
import fi.maanmittauslaitos.pta.search.metadata.BestMatchingRegionListCustomExtractor;
import fi.maanmittauslaitos.pta.search.metadata.ISOMetadataExtractorConfigurationFactory;
import fi.maanmittauslaitos.pta.search.metadata.MetadataExtractorConfigurationFactory;
import fi.maanmittauslaitos.pta.search.metadata.ResultMetadataFields;
import fi.maanmittauslaitos.pta.search.metadata.extractor.GeographicBoundingBoxXmlCustomExtractor;
import fi.maanmittauslaitos.pta.search.metadata.extractor.ResponsiblePartyXmlCustomExtractor;
import fi.maanmittauslaitos.pta.search.metadata.json.CKANMetadataExtractorConfigurationFactory;
import fi.maanmittauslaitos.pta.search.metadata.json.extractor.GeographicBoundingBoxCKANCustomExtractor;
import fi.maanmittauslaitos.pta.search.metadata.json.extractor.SimpleResponsiblePartyCKANCustomExtractor;
import fi.maanmittauslaitos.pta.search.source.HarvesterSource;
import fi.maanmittauslaitos.pta.search.source.csw.CSWHarvesterSource;
import fi.maanmittauslaitos.pta.search.source.csw.LocalCSWHarvesterSource;
import fi.maanmittauslaitos.pta.search.source.json.CKANHarvesterSource;
import fi.maanmittauslaitos.pta.search.source.json.LocalCKANHarvesterSource;
import fi.maanmittauslaitos.pta.search.text.ExistsInSetProcessor;
import fi.maanmittauslaitos.pta.search.text.MauiTextProcessor;
import fi.maanmittauslaitos.pta.search.text.RDFTerminologyMatcherProcessor;
import fi.maanmittauslaitos.pta.search.text.RegexProcessor;
import fi.maanmittauslaitos.pta.search.text.StopWordsProcessor;
import fi.maanmittauslaitos.pta.search.text.TerminologyExpansionProcessor;
import fi.maanmittauslaitos.pta.search.text.TextProcessingChain;
import fi.maanmittauslaitos.pta.search.text.TextProcessor;
import fi.maanmittauslaitos.pta.search.text.TextSplitterProcessor;
import fi.maanmittauslaitos.pta.search.text.WordCombinationProcessor;
import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;
import fi.maanmittauslaitos.pta.search.text.stemmer.StemmerFactory;
import fi.maanmittauslaitos.pta.search.utils.HarvesterTracker;
import fi.maanmittauslaitos.pta.search.utils.HarvesterTrackerImpl;
import fi.maanmittauslaitos.pta.search.utils.HarvesterWrapper;
import fi.maanmittauslaitos.pta.search.utils.Region;
import fi.maanmittauslaitos.pta.search.utils.RegionFactory;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import static java.util.Objects.requireNonNull;

public class HarvesterConfig {

	private static final Logger logger = Logger.getLogger(HarvesterConfig.class);

	private static final String ENV_CANONICAL_ORGANISATIONS_FILENAME = "CANONICAL_ORGANISATIONS_FILE";
	private static final String CANONICAL_ORGANISATIONS_DEFAULT_FILENAME = "canonical_organisations.ods";
	private static final String TRACKER_FILENAME = "harvester_tracker.json";
	private static final String LOCAL_CSW_SOURCE_DIR = "csws";
	private static final String LOCAL_CKAN_SOURCE_DIR = "ckans";


	private final ObjectMapper objectMapper;
	private final Stemmer finnishStemmer;
	private final Collection<String> wellKnownPostfixes;
	private Model terminologyModel;

	public HarvesterConfig() throws IOException {
		this.objectMapper = new ObjectMapper();
		this.finnishStemmer = getFinnishStemmer();
		this.wellKnownPostfixes = getWellKnownPostfixes();
	}

	public Collection<HarvesterWrapper> getHarvesterWrappers() throws IOException, ParserConfigurationException {
		return Arrays.asList(
				HarvesterWrapper.create(getCKANSource(), getCKANRecordProcessor(), objectMapper)
				, HarvesterWrapper.create(getCSWSource(), getCSWRecordProcessor(), objectMapper)
		);
	}

	public Collection<HarvesterWrapper> getLocalHarvesterWrappers() throws IOException, ParserConfigurationException {
		return Arrays.asList(
				HarvesterWrapper.create(getLocalCKANSource(), getCKANRecordProcessor(), objectMapper)
				, HarvesterWrapper.create(getLocalCSWSource(), getCSWRecordProcessor(), objectMapper)
		);
	}


	private Collection<String> getWellKnownPostfixes() throws IOException {
		URL resource = HarvesterConfig.class.getClassLoader().getResource("nls.fi/pta-intelligent-search/well-known-postfixes-fi.txt");
		String content = IOUtils.toString(requireNonNull(resource).openStream(), StandardCharsets.UTF_8);
		String[] split = content.split("\n");
		return Arrays.asList(split);
	}

	private Stemmer getFinnishStemmer() throws IOException {
		URL preStemRes = requireNonNull(getClass().getClassLoader()
				.getResource("nls.fi/pta-intelligent-search/pre-stem-fi.json"));
		URL postStemRes = requireNonNull(getClass().getClassLoader()
				.getResource("nls.fi/pta-intelligent-search/post-stem-fi.json"));
		TypeReference<Map<String, String>> valueTypeRef = new TypeReference<Map<String, String>>() {
		};
		Map<String, String> preStem = objectMapper.readValue(preStemRes, valueTypeRef);
		Map<String, String> postStem = objectMapper.readValue(postStemRes, valueTypeRef);

		return StemmerFactory.createFinnishStemmer(preStem, postStem);
	}

	public HarvesterSource getCSWSource() {
		HarvesterSource source = new CSWHarvesterSource();
		source.setMetadataType(HarvesterSource.MetadataType.CSW);
		source.setBatchSize(10);
		source.setOnlineResource("https://paikkatietohakemisto.fi");
		source.setApiPath("geonetwork/srv/en/csw");
		//source.setOnlineResource("http://demo.paikkatietohakemisto.fi/geonetwork/srv/en/csw");
		return source;
	}

	public HarvesterSource getLocalCSWSource() {
		LocalCSWHarvesterSource source = new LocalCSWHarvesterSource();
		source.setMetadataType(HarvesterSource.MetadataType.CSW);
		URL cswRoot = this.getClass().getClassLoader().getResource(LOCAL_CSW_SOURCE_DIR);
		source.setResourceRootURL(cswRoot);
		return source;
	}

	public HarvesterSource getCKANSource() {
		CKANHarvesterSource source = new CKANHarvesterSource(objectMapper);
		source.setMetadataType(HarvesterSource.MetadataType.CKAN);
		source.setBatchSize(10);
		source.setOnlineResource("https://ckan.ymparisto.fi");
		source.setApiPath("api/3/action/package_search");
		source.setQuery("type:envi-reports");
		return source;
	}

	public HarvesterSource getLocalCKANSource() {
		LocalCKANHarvesterSource source = new LocalCKANHarvesterSource(objectMapper);
		source.setMetadataType(HarvesterSource.MetadataType.CKAN);
		source.setResourceRootURL(this.getClass().getClassLoader().getResource(LOCAL_CKAN_SOURCE_DIR));
		return source;
	}

	public DocumentSink getDocumentSink(HarvesterTracker harvesterTracker) {
		ElasticsearchDocumentSink ret = new ElasticsearchDocumentSink();
		ret.setTracker(harvesterTracker);
		ret.setHostname("localhost");
		ret.setPort(9200);
		ret.setProtocol("http");

		ret.setIndex(PTAElasticSearchMetadataConstants.INDEX);
		ret.setType(PTAElasticSearchMetadataConstants.TYPE);

		ret.setIdField("@id");

		return ret;
	}

	public DocumentSink getLocalDocumentSink(String sinkfile, HarvesterTracker harvesterTracker) {
		LocalArchiveDocumentSink localArchiveDocumentSink = new LocalArchiveDocumentSink();
		localArchiveDocumentSink.setTracker(harvesterTracker);
		localArchiveDocumentSink.setOutputFileName(sinkfile);
		return localArchiveDocumentSink;
	}

	public DocumentProcessor getCKANRecordProcessor() throws ParserConfigurationException, IOException {
		MetadataExtractorConfigurationFactory factory = new CKANMetadataExtractorConfigurationFactory();

		// Basic configuration
		DocumentProcessingConfiguration configuration = factory.createMetadataDocumentProcessingConfiguration();

		addCommonProcessorConfiguration(configuration);

		// Modify organisation extractor to canonicalize organisation names
		OrganisationNormaliser organisationNormaliser = loadOrganisationNormaliser();
		OrganisationNormaliserTextRewriter orgRewriter = new OrganisationNormaliserTextRewriter();
		orgRewriter.setOrganisationNormaliser(organisationNormaliser);

		FieldExtractorConfiguration fec = configuration.getFieldExtractor(ResultMetadataFields.ORGANISATIONS);
		FieldExtractorConfigurationImpl x = (FieldExtractorConfigurationImpl) fec;

		SimpleResponsiblePartyCKANCustomExtractor rpxpce = (SimpleResponsiblePartyCKANCustomExtractor) x.getListCustomExtractor();
		rpxpce.setOrganisationNameRewriter(orgRewriter);

		// Extract bounding box area
		FieldExtractorConfigurationImpl bboxFec = (FieldExtractorConfigurationImpl)
				configuration.getFieldExtractor(ResultMetadataFields.GEOGRAPHIC_BOUNDING_BOX);

		FieldExtractorConfigurationImpl bboxAreaFec = (FieldExtractorConfigurationImpl) bboxFec.copy();
		final GeographicBoundingBoxCKANCustomExtractor originalBboxCustomExtractor = (GeographicBoundingBoxCKANCustomExtractor) bboxAreaFec.getCustomExtractor();

		bboxAreaFec.setCustomExtractor((documentQuery, queryResult) -> {
			List<Double> coords;
			if (queryResult != null) {
				Object original = originalBboxCustomExtractor.process(documentQuery, queryResult);
				coords = (List<Double>) original;
			} else {
				coords = (List<Double>) bboxFec.getDefaultValue();
			}

			if (coords == null) {
				return null;
			} else {
				return (coords.get(2) - coords.get(0)) * (coords.get(3) - coords.get(1));
			}
		});
		bboxAreaFec.setField("geographicBoundingBoxArea");
		bboxAreaFec.setDefaultValue(Optional.ofNullable(bboxFec.getDefaultValue())
				.map(coords -> (List<Double>) coords)
				.map(coords -> (coords.get(2) - coords.get(0)) * (coords.get(3) - coords.get(1)))
				.orElse(null)
		);
		configuration.getFieldExtractors().add(bboxAreaFec);

		return factory.getDocumentProcessorFactory().createJsonProcessor(configuration);
	}


	public DocumentProcessor getCSWRecordProcessor() throws ParserConfigurationException, IOException {
		MetadataExtractorConfigurationFactory factory = new ISOMetadataExtractorConfigurationFactory();

		// Basic configuration
		DocumentProcessingConfiguration configuration = factory.createMetadataDocumentProcessingConfiguration();

		addCommonProcessorConfiguration(configuration);

		FieldExtractorConfigurationImpl annotatedKeywordExtractor = new FieldExtractorConfigurationImpl();
		annotatedKeywordExtractor.setField("annotated_keywords_uri");
		annotatedKeywordExtractor.setType(FieldExtractorType.ALL_MATCHING_VALUES);
		annotatedKeywordExtractor.setQuery("//gmd:descriptiveKeywords/*/gmd:keyword/gmx:Anchor/@xlink:href");

		annotatedKeywordExtractor.setTextProcessorName("isInOntologyFilterProcessor");

		configuration.getFieldExtractors().add(annotatedKeywordExtractor);

		// Modify organisation extractor to canonicalize organisation names
		OrganisationNormaliser organisationNormaliser = loadOrganisationNormaliser();
		OrganisationNormaliserTextRewriter orgRewriter = new OrganisationNormaliserTextRewriter();
		orgRewriter.setOrganisationNormaliser(organisationNormaliser);

		FieldExtractorConfiguration fec = configuration.getFieldExtractor(ResultMetadataFields.ORGANISATIONS);
		FieldExtractorConfigurationImpl x = (FieldExtractorConfigurationImpl) fec;
		ResponsiblePartyXmlCustomExtractor rpxpce = (ResponsiblePartyXmlCustomExtractor) x.getCustomExtractor();
		rpxpce.setOrganisationNameRewriter(orgRewriter);

		// Extract bounding box area
		FieldExtractorConfigurationImpl bboxFec = (FieldExtractorConfigurationImpl)
				configuration.getFieldExtractor(ResultMetadataFields.GEOGRAPHIC_BOUNDING_BOX);

		FieldExtractorConfigurationImpl bboxAreaFec = (FieldExtractorConfigurationImpl) bboxFec.copy();
		final GeographicBoundingBoxXmlCustomExtractor originalBboxCustomExtractor = (GeographicBoundingBoxXmlCustomExtractor) bboxAreaFec.getCustomExtractor();
		bboxAreaFec.setCustomExtractor((documentQuery, queryResult) -> {
			Object original = originalBboxCustomExtractor.process(documentQuery, queryResult);
			@SuppressWarnings("unchecked")
			List<Double> coords = (List<Double>) original;
			if (coords == null) {
				return null;
			} else {
				return (coords.get(2) - coords.get(0)) * (coords.get(3) - coords.get(1));
			}
		});
		bboxAreaFec.setField("geographicBoundingBoxArea");
		configuration.getFieldExtractors().add(bboxAreaFec);

		return factory.getDocumentProcessorFactory().createXmlProcessor(configuration);
	}

	private void addCommonProcessorConfiguration(DocumentProcessingConfiguration configuration) throws IOException {
		// Ontology models and and text processors
		if (terminologyModel == null) {
			terminologyModel = getTerminologyModel();
		}
		RDFTerminologyMatcherProcessor terminologyProcessor = createTerminologyMatcher(terminologyModel);
		WordCombinationProcessor wordCombinationProcessor = createWordCombinationProcessor(terminologyModel);

		// Copy the title to titleSort (which is a keyword field to allow sorting)
		FieldExtractorConfiguration titleFiSort = configuration.getFieldExtractor(ResultMetadataFields.TITLE).copy();
		titleFiSort.setField("titleFiSort");
		configuration.getFieldExtractors().add(titleFiSort);

		FieldExtractorConfiguration titleSvSort = configuration.getFieldExtractor(ResultMetadataFields.TITLE_SV).copy();
		titleSvSort.setField("titleSvSort");
		configuration.getFieldExtractors().add(titleSvSort);

		FieldExtractorConfiguration titleEnSort = configuration.getFieldExtractor(ResultMetadataFields.TITLE_EN).copy();
		titleEnSort.setField("titleEnSort");
		configuration.getFieldExtractors().add(titleEnSort);


		// Set up abstract processor (abstract => abstract_uri)
		TextProcessingChain abstractChain = createAbstractProcessingChain(terminologyProcessor, wordCombinationProcessor);
		configuration.getTextProcessingChains().put("abstractProcessor", abstractChain);

		FieldExtractorConfiguration abstractUri = configuration.getFieldExtractor(ResultMetadataFields.ABSTRACT).copy();
		abstractUri.setField(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_URI);
		abstractUri.setTextProcessorName("abstractProcessor");

		configuration.getFieldExtractors().add(abstractUri);

		// Abstract processor that determines the parents of
		TextProcessingChain abstractParentsChain = createAbstractParentProcessingChain(terminologyProcessor, wordCombinationProcessor, terminologyModel);
		configuration.getTextProcessingChains().put("abstractParentProcessor", abstractParentsChain);

		FieldExtractorConfiguration abstract2Uri = configuration.getFieldExtractor(ResultMetadataFields.ABSTRACT).copy();
		abstract2Uri.setField(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_URI_PARENTS);
		abstract2Uri.setTextProcessorName("abstractParentProcessor");

		configuration.getFieldExtractors().add(abstract2Uri);


		// Set up maui chain for abstract (abstract => abstract_maui_uri)
		MauiTextProcessor mauiTextProcessor = createMauiProcessingChain();
		TextProcessingChain mauiChain = new TextProcessingChain();
		mauiChain.getChain().add(mauiTextProcessor);

		configuration.getTextProcessingChains().put("mauiProcessor", mauiChain);

		FieldExtractorConfiguration abstractMauiUri = configuration.getFieldExtractor(ResultMetadataFields.ABSTRACT).copy();
		abstractMauiUri.setField(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_MAUI_URI);
		abstractMauiUri.setTextProcessorName("mauiProcessor");

		configuration.getFieldExtractors().add(abstractMauiUri);

		// Set up maui chain for abstract (abstract => abstract_maui_uri_parents)
		TextProcessingChain mauiParentsChain = createMauiParentProcessingChain(mauiTextProcessor, terminologyModel);
		configuration.getTextProcessingChains().put("mauiParentsProcessor", mauiParentsChain);

		FieldExtractorConfiguration abstractMauiParentsUri = configuration.getFieldExtractor(ResultMetadataFields.ABSTRACT).copy();
		abstractMauiParentsUri.setField(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_MAUI_URI_PARENTS);
		abstractMauiParentsUri.setTextProcessorName("mauiParentsProcessor");

		configuration.getFieldExtractors().add(abstractMauiParentsUri);

		// Extract all organisation names in a text field for full-text search purposes
		TextProcessingChain organisationNameTextProcessor = new TextProcessingChain();
		RegexProcessor whitespaceRemoval = new RegexProcessor();
		whitespaceRemoval.setPattern(Pattern.compile("^\\s*$"));
		whitespaceRemoval.setIncludeMatches(false);

		organisationNameTextProcessor.getChain().add(whitespaceRemoval);

		configuration.getTextProcessingChains().put("organisationNameTextProcessor", organisationNameTextProcessor);

		FieldExtractorConfigurationImpl organisationForSearch = new FieldExtractorConfigurationImpl();
		organisationForSearch.setField("organisationName_text");
		organisationForSearch.setType(FieldExtractorType.ALL_MATCHING_VALUES);
		organisationForSearch.setQuery("//gmd:contact//gmd:organisationName//text()");

		organisationForSearch.setTextProcessorName("organisationNameTextProcessor");

		configuration.getFieldExtractors().add(organisationForSearch);


		// Keyword to uri detection (keywords => keywords_uri)
		TextProcessingChain keywordChain = createKeywordProcessingChain(terminologyProcessor, wordCombinationProcessor);
		configuration.getTextProcessingChains().put("keywordProcessor", keywordChain);

		FieldExtractorConfiguration keywordsUri = configuration.getFieldExtractor(ResultMetadataFields.KEYWORDS_ALL).copy();
		keywordsUri.setField(PTAElasticSearchMetadataConstants.FIELD_KEYWORDS_URI);
		keywordsUri.setTextProcessorName("keywordProcessor");

		configuration.getFieldExtractors().add(keywordsUri);

		// Annotated keywords
		TextProcessingChain isInOntologyFilterProcessor = createIsInOntologyProcessor(terminologyProcessor);

		configuration.getTextProcessingChains().put("isInOntologyFilterProcessor", isInOntologyFilterProcessor);


		// Configure INSPIRE theme extractor to normalize the theme to
		final InspireThemesImpl inspireThemes = new InspireThemesImpl();
		inspireThemes.setCanonicalLanguage("fi"); // Normalize the records to Finnish
		inspireThemes.setModel(loadModels(RDFFormat.RDFXML, "/inspire-theme.rdf.gz"));
		inspireThemes.setHeuristicSearchLanguagePriority("fi", "en", "sv");

		FieldExtractorConfiguration inspireFieldExtractorConfiguration =
				configuration.getFieldExtractor(ResultMetadataFields.KEYWORDS_INSPIRE);

		TextProcessingChain inspireThemeNormalizer = new TextProcessingChain();
		inspireThemeNormalizer.getChain().add(input -> {
			List<String> ret = new ArrayList<>();

			for (String str : input) {
				String value = inspireThemes.getCanonicalName(str);
				if (value == null) {
					value = str;
				}
				ret.add(value);
			}

			return ret;
		});

		configuration.getTextProcessingChains().put("inspireThemeNormalizer", inspireThemeNormalizer);

		inspireFieldExtractorConfiguration.setTextProcessorName("inspireThemeNormalizer");

		// Best matching regions
		FieldExtractorConfigurationImpl bboxFec = (FieldExtractorConfigurationImpl)
				configuration.getFieldExtractor(ResultMetadataFields.GEOGRAPHIC_BOUNDING_BOX);

		configuration.getFieldExtractors().add(getBestMatchingRegions(bboxFec));
	}

	private FieldExtractorConfigurationImpl getBestMatchingRegions(FieldExtractorConfigurationImpl bboxFec) {
		ObjectReader listReader = objectMapper.readerFor(new TypeReference<List<Double>>() {
		});

		Map<String, Region> countries = RegionFactory.readRegionResource(objectMapper, listReader, "data/well_known_location_bboxes_countries.json");
		Map<String, Region> regions = RegionFactory.readRegionResource(objectMapper, listReader, "data/well_known_location_bboxes_regions.json");
		Map<String, Region> subregions = RegionFactory.readRegionResource(objectMapper, listReader, "data/well_known_location_bboxes_subregions.json");
		Map<String, Region> municipalities = RegionFactory.readRegionResource(objectMapper, listReader, "data/well_known_location_bboxes_municipalities.json");

		FieldExtractorConfigurationImpl regionFec = (FieldExtractorConfigurationImpl) bboxFec.copy();
		final CustomExtractor originalBboxCustomExtractor = regionFec.getCustomExtractor();

		List<Double> defaultCoordinates = (List<Double>) Optional.ofNullable(bboxFec.getDefaultValue()).orElse(Collections.emptyList());
		BestMatchingRegionListCustomExtractor customExtractor = BestMatchingRegionListCustomExtractor.create(
				objectMapper, countries, regions, subregions, municipalities, originalBboxCustomExtractor, defaultCoordinates);

		regionFec.setListCustomExtractor(customExtractor);
		regionFec.setField(PTAElasticSearchMetadataConstants.FIELD_BEST_MATCHING_REGIONS);
		regionFec.setType(FieldExtractorType.CUSTOM_CLASS_SINGLE_VALUE);

		return regionFec;
	}


	private OrganisationNormaliser loadOrganisationNormaliser() throws IOException {

		try {
			InputStream is;
			String env = System.getProperty(ENV_CANONICAL_ORGANISATIONS_FILENAME);
			if (env != null) {
				logger.info("Loading canonical organisations from file " + env);
				is = new FileInputStream(env);
			} else {

				File file = new File(CANONICAL_ORGANISATIONS_DEFAULT_FILENAME);
				if (file.exists()) {
					is = new FileInputStream(file);
				} else {
					is = HarvesterConfig.class.getResourceAsStream("/canonical_organisations.ods");
				}
			}

			ODFOrganisationNameNormaliserImpl ret = new ODFOrganisationNameNormaliserImpl();
			ret.loadWorkbook(is);

			return ret;
		} catch (IOException | ParseException e) {
			throw new IOException("Could not load canonical organisations", e);
		}

	}


	private TextProcessingChain createMauiParentProcessingChain(MauiTextProcessor mauiTextProcessor, Model model) {
		TextProcessingChain ret = new TextProcessingChain();

		ret.getChain().add(mauiTextProcessor);

		TerminologyExpansionProcessor expansionProcessor = new TerminologyExpansionProcessor();
		expansionProcessor.setModel(model);
		expansionProcessor.setPredicates(Collections.singletonList(SKOS.BROADER));

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

	HarvesterTracker getHarvesterTracker() throws IOException {
		File trackerFile = Paths.get(TRACKER_FILENAME).toFile();
		return HarvesterTrackerImpl.create(trackerFile, objectMapper);
	}

	private TextProcessor createTextSplitterProcessor(boolean joinHyphened) throws IOException {
		return TextSplitterProcessor.create(finnishStemmer, wellKnownPostfixes, joinHyphened);

	}

	@VisibleForTesting
	TextProcessingChain createKeywordProcessingChain(RDFTerminologyMatcherProcessor terminologyProcessor,
													 WordCombinationProcessor wordCombinationProcessor) throws IOException {
		TextProcessingChain keywordChain = new TextProcessingChain();
		RegexProcessor whitespaceRemoval = new RegexProcessor();
		whitespaceRemoval.setPattern(Pattern.compile("^\\s*$"));
		whitespaceRemoval.setIncludeMatches(false);

		keywordChain.getChain().add(createTextSplitterProcessor(false));
		keywordChain.getChain().add(wordCombinationProcessor);
		keywordChain.getChain().add(whitespaceRemoval);
		keywordChain.getChain().add(terminologyProcessor);
		return keywordChain;
	}


	@VisibleForTesting
	TextProcessingChain createAbstractProcessingChain(RDFTerminologyMatcherProcessor terminologyProcessor,
													  WordCombinationProcessor wordCombinationProcessor) throws IOException {
		TextProcessingChain ret = new TextProcessingChain();
		ret.getChain().add(createTextSplitterProcessor(true));
		ret.getChain().add(wordCombinationProcessor);

		StopWordsProcessor stopWordsProcessor = new StopWordsProcessor();
		stopWordsProcessor.loadWords(HarvesterConfig.class.getResourceAsStream("/nls.fi/pta-intelligent-search/stopwords-fi.txt"));
		ret.getChain().add(stopWordsProcessor);
		ret.getChain().add(terminologyProcessor);
		return ret;
	}

	private TextProcessingChain createAbstractParentProcessingChain(RDFTerminologyMatcherProcessor terminologyProcessor,
																	WordCombinationProcessor wordCombinationProcessor, Model model) throws IOException {
		TextProcessingChain ret = createAbstractProcessingChain(terminologyProcessor, wordCombinationProcessor);

		TerminologyExpansionProcessor expansionProcessor = new TerminologyExpansionProcessor();
		expansionProcessor.setModel(model);
		expansionProcessor.setPredicates(Collections.singletonList(SKOS.BROADER));

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

	@VisibleForTesting
	RDFTerminologyMatcherProcessor createTerminologyMatcher(Model model) {
		RDFTerminologyMatcherProcessor ret = new RDFTerminologyMatcherProcessor();
		ret.setModel(model);
		ret.setTerminologyLabels(Arrays.asList(SKOS.PREF_LABEL, SKOS.ALT_LABEL));
		ret.setStemmer(finnishStemmer);
		ret.setLanguage("fi");
		return ret;
	}

	@VisibleForTesting
	WordCombinationProcessor createWordCombinationProcessor(Model model) {
		WordCombinationProcessor ret = new WordCombinationProcessor();
		ret.setModel(model);
		ret.setTerminologyLabels(Arrays.asList(SKOS.PREF_LABEL, SKOS.ALT_LABEL));
		ret.setStemmer(finnishStemmer);
		ret.setLanguage("fi");
		return ret;
	}


	Model getTerminologyModel() throws IOException {
		return loadModels(RDFFormat.TURTLE, getTerminologyModelResourceName());
	}

	private String getTerminologyModelResourceName() {
		return "/pto-skos.ttl.gz";
	}

	private static Model loadModels(RDFFormat format, String... files) throws IOException {
		Model ret = null;

		for (String file : files) {
			try (Reader reader = new InputStreamReader(new GZIPInputStream(HarvesterConfig.class.getResourceAsStream(file)))) {
				Model model = Rio.parse(reader, "", format);

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
