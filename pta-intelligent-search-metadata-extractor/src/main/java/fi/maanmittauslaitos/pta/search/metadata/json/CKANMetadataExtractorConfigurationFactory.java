package fi.maanmittauslaitos.pta.search.metadata.json;

import fi.maanmittauslaitos.pta.search.documentprocessor.CustomExtractor;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfigurationImpl;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfigurationImpl.FieldExtractorType;
import fi.maanmittauslaitos.pta.search.documentprocessor.ListCustomExtractor;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.QueryResult;
import fi.maanmittauslaitos.pta.search.metadata.MetadataExtractorConfigurationFactory;
import fi.maanmittauslaitos.pta.search.metadata.ResultMetadataFields;

import javax.xml.parsers.ParserConfigurationException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CKANMetadataExtractorConfigurationFactory extends MetadataExtractorConfigurationFactory {

	static final List<Double> DEFAULT_BOUNDING_BOX_FOR_CKAN_METADATA = Arrays.asList(19.450000, 59.780000, 31.610000, 70.120000);
	private static final Map<String, String> INSPIRE_THEME_MAP = Stream.of(new String[][]{
			{"ad", "Addresses"},
			{"au", "Administraive units"},
			{"rs", "Coordinate reference systems"},
			{"gg", "Geographical grid systems"},
			{"cp", "Cadastral parcels"},
			{"gn", "Geographical names"},
			{"hy", "Hydrography"},
			{"ps", "Protected sites"},
			{"tn", "Transport networks"},
			{"el", "Elevation"},
			{"ge", "Geology"},
			{"lc", "Land cover"},
			{"oi", "Orthoimagery"},
			{"af", "Agricultural and aquaculture facilities"},
			{"am", "Area management/restriction/regulation zones and reporting units"},
			{"ac", "Atmospheric conditions"},
			{"br", "Bio-geographical regions"},
			{"bu", "Buildings"},
			{"er", "Energy resources"},
			{"ef", "Environmental monitoring facilities"},
			{"hb", "Habitats and biotopes"},
			{"hh", "Human health and safety"},
			{"lu", "Land use"},
			{"mr", "Mineral resources"},
			{"nz", "Natural risk zones"},
			{"of", "Oceanographic geographical features"},
			{"pd", "Population distribution — demography"},
			{"pf", "Production and industrial facilities"},
			{"sr", "Sea regions"},
			{"so", "Soil"},
			{"sd", "Species distribution"},
			{"su", "Statistical units"},
			{"us", "Utility and governmental services"},
			{"mf", "Meteorological geographical features"},
			{"ac-mf", "Atmospheric Conditions and meteorological geographical features"}
	}).collect(Collectors.toMap(data -> data[0], data -> data[1]));


	@Override
	public DocumentProcessingConfiguration createMetadataDocumentProcessingConfiguration() {


		DocumentProcessingConfiguration configuration = new DocumentProcessingConfiguration();
		List<FieldExtractorConfiguration> extractors = configuration.getFieldExtractors();

		// Id dateStampExtractor
		extractors.add(createFirstMatchingJsonPathExtractor(
				ResultMetadataFields.ID,
				"$.resources[0].id", "$.id"));

		// Title extractors
		extractors.add(createFirstMatchingJsonPathExtractor(
				ResultMetadataFields.TITLE,
				"$.resources[0].name", "$.title"));

		extractors.add(createFirstMatchingJsonPathExtractor(
				ResultMetadataFields.TITLE_EN,
				"$.resources[0].name_sv", "$.title_sv"));

		extractors.add(createFirstMatchingJsonPathExtractor(
				ResultMetadataFields.TITLE_SV,
				"$.resources[0].name_sv", "$.title_sv"));


		// Abstract extractors
		extractors.add(createFirstMatchingJsonPathExtractor(
				ResultMetadataFields.ABSTRACT,
				"$.resources[0].description", "$.notes"
		));

		// isService
		extractors.add(createJsonPathExtractor(
				ResultMetadataFields.IS_SERVICE,
				FieldExtractorType.TRUE_IF_MATCHES_OTHERWISE_FALSE,
				"$.[?(@.resources.length()==0)]"));

		// isDataset
		extractors.add(createJsonPathExtractor(
				ResultMetadataFields.IS_DATASET,
				FieldExtractorType.TRUE_IF_MATCHES_OTHERWISE_FALSE,
				"$..package_id"));

		// isAvoindata
		extractors.add(createJsonPathExtractor(
				ResultMetadataFields.IS_AVOINDATA,
				FieldExtractorType.TRUE_IF_MATCHES_OTHERWISE_FALSE,
				"$.tag_keyword[?(@ =~/.*avoindata\\.fi/i)]"));

		// Topic categories
		extractors.add(createJsonPathExtractor(
				ResultMetadataFields.TOPIC_CATEGORIES,
				FieldExtractorType.ALL_MATCHING_VALUES,
				"$.nonexisting.*"
		));

		// Keywords
		extractors.add(createJsonPathExtractor(
				ResultMetadataFields.KEYWORDS_ALL,
				FieldExtractorType.ALL_MATCHING_VALUES,
				"$.tag_keyword"
		));

		// Inspire themes
		extractors.add(createCustomJsonPathExtractor(
				ResultMetadataFields.KEYWORDS_INSPIRE,
				(documentQuery, queryResult) -> INSPIRE_THEME_MAP.getOrDefault(queryResult.getValue(), queryResult.getValue()),
				"$.tag_inspire_theme.*"
		));


		// Distribution Formats
		extractors.add(createCustomJsonPathExtractor(
				ResultMetadataFields.DISTRIBUTION_FORMATS,
				(documentQuery, queryResult) -> documentQuery.process("", queryResult)
						.stream()
						.map(QueryResult::getValue)
						.findFirst()
						.orElse(null),
				"$.resources[0]['format','mimetype']"
		));

		// Datestamp
		FieldExtractorConfigurationImpl dateStampExtractor = (FieldExtractorConfigurationImpl) createFirstMatchingJsonPathExtractor(
				ResultMetadataFields.DATESTAMP,
				"$.resources[0].last_modified", "$.metadata_modified"
		);

		dateStampExtractor.setTrimmer(dStamp -> dStamp != null ? dStamp.trim().split("\\.")[0] : null);
		extractors.add(dateStampExtractor);

		// Organisation names + roles
		extractors.add(createCustomListJsonPathExtractor(
				ResultMetadataFields.ORGANISATIONS,
				new ResponsiblePartyCKANCustomExtractor(),
				"$.['reporting_organization','reporting_organization_others']"
		));

		// Geographic bounding box
		FieldExtractorConfigurationImpl bboxExtractor = (FieldExtractorConfigurationImpl) createCustomJsonPathExtractor(
				ResultMetadataFields.GEOGRAPHIC_BOUNDING_BOX,
				new GeographicBoundingBoxCKANCustomExtractor(),
				"$.spatial.coordinates.*.*"
		);
		bboxExtractor.setDefaultValue(DEFAULT_BOUNDING_BOX_FOR_CKAN_METADATA);
		extractors.add(bboxExtractor);

		return configuration;
	}

	@Override
	public DocumentProcessor createMetadataDocumentProcessor() throws ParserConfigurationException {
		DocumentProcessingConfiguration configuration = createMetadataDocumentProcessingConfiguration();
		return getDocumentProcessorFactory().createJsonProcessor(configuration);
	}

	private FieldExtractorConfiguration createJsonPathExtractor(String field, FieldExtractorType type, String jsonPath) {
		FieldExtractorConfigurationImpl ret = new FieldExtractorConfigurationImpl();
		ret.setField(field);
		ret.setType(type);
		ret.setQuery(jsonPath);
		return ret;
	}

	private FieldExtractorConfiguration createCustomJsonPathExtractor(String field, CustomExtractor extractor, String jsonPath) {
		FieldExtractorConfigurationImpl ret = (FieldExtractorConfigurationImpl) createJsonPathExtractor(field,
				FieldExtractorType.CUSTOM_CLASS, jsonPath);
		ret.setCustomExtractor(extractor);
		return ret;
	}

	private FieldExtractorConfiguration createCustomListJsonPathExtractor(String field, ListCustomExtractor extractor, String jsonPath) {
		FieldExtractorConfigurationImpl ret = (FieldExtractorConfigurationImpl) createJsonPathExtractor(field,
				FieldExtractorType.CUSTOM_CLASS_SINGLE_VALUE, jsonPath);
		ret.setListCustomExtractor(extractor);
		return ret;
	}

	private FieldExtractorConfiguration createFirstMatchingJsonPathExtractor(String field, String... jsonPaths) {
		FieldExtractorConfigurationImpl ret = (FieldExtractorConfigurationImpl) createJsonPathExtractor(field,
				FieldExtractorType.FIRST_MATCHING_FROM_MULTIPLE_QUERIES, jsonPaths[0]);
		ret.setExtraQueries(Arrays.asList(jsonPaths).subList(1, jsonPaths.length));
		return ret;
	}

}




