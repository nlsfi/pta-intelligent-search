package fi.maanmittauslaitos.pta.search.metadata.json;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfigurationImpl;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfigurationImpl.FieldExtractorType;
import fi.maanmittauslaitos.pta.search.metadata.ISOMetadataFields;
import fi.maanmittauslaitos.pta.search.metadata.MetadataExtractorConfigurationFactory;

import javax.xml.parsers.ParserConfigurationException;
import java.util.List;

public class CKANMetadataExtractorConfigurationFactory extends MetadataExtractorConfigurationFactory {

	@Override
	public DocumentProcessingConfiguration createMetadataDocumentProcessingConfiguration() throws ParserConfigurationException {
		DocumentProcessingConfiguration configuration = new DocumentProcessingConfiguration();
		List<FieldExtractorConfiguration> extractors = configuration.getFieldExtractors();

		// Id extractor
		extractors.add(createJsonPathExtractor(
				ISOMetadataFields.ID,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"$.id"));

		//TODO: title ? name vs title

		// isService
		extractors.add(createJsonPathExtractor(
				ISOMetadataFields.IS_SERVICE,
				FieldExtractorType.TRUE_IF_MATCHES_OTHERWISE_FALSE,
				"$.num_resources"));

		// isDataset
		extractors.add(createJsonPathExtractor(
				ISOMetadataFields.IS_DATASET,
				FieldExtractorType.TRUE_IF_MATCHES_OTHERWISE_FALSE,
				"$.package_id"));


		return configuration;
	}

	private Configuration createJsonPathConfiguration() {
		JsonProvider jsonProvider = new JacksonJsonProvider();
		MappingProvider mappingProvider = new JacksonMappingProvider();

		//TODO: check configuration
		return Configuration.builder()
				.jsonProvider(jsonProvider)
				.mappingProvider(mappingProvider)
				.options(Option.ALWAYS_RETURN_LIST, Option.DEFAULT_PATH_LEAF_TO_NULL)
				.build();
	}

	@Override
	public DocumentProcessor createMetadataDocumentProcessor() throws ParserConfigurationException {
		DocumentProcessingConfiguration configuration = createMetadataDocumentProcessingConfiguration();
		return getDocumentProcessorFactory().createJsonProcessor(configuration, createJsonPathConfiguration());
	}

	private FieldExtractorConfiguration createJsonPathExtractor(String field, FieldExtractorType type, String jsonPath) {
		FieldExtractorConfigurationImpl ret = new FieldExtractorConfigurationImpl();
		ret.setField(field);
		ret.setType(type);
		ret.setQuery(jsonPath);

		return ret;
	}
}
