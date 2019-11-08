package fi.maanmittauslaitos.pta.search.metadata.json;

import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfigurationImpl;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfigurationImpl.FieldExtractorType;
import fi.maanmittauslaitos.pta.search.metadata.MetadataExtractorConfigurationFactory;
import fi.maanmittauslaitos.pta.search.metadata.ResultMetadataFields;

import javax.xml.parsers.ParserConfigurationException;
import java.util.List;

public class CKANMetadataExtractorConfigurationFactory extends MetadataExtractorConfigurationFactory {

	@Override
	public DocumentProcessingConfiguration createMetadataDocumentProcessingConfiguration() throws ParserConfigurationException {
		DocumentProcessingConfiguration configuration = new DocumentProcessingConfiguration();
		List<FieldExtractorConfiguration> extractors = configuration.getFieldExtractors();

		// Id extractor
		extractors.add(createJsonPathExtractor(
				ResultMetadataFields.ID,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"$.id"));

		//TODO: title ? name vs title

		// isService
		extractors.add(createJsonPathExtractor(
				ResultMetadataFields.IS_SERVICE,
				FieldExtractorType.TRUE_IF_MATCHES_OTHERWISE_FALSE,
				"$.num_resources"));

		// isDataset
		extractors.add(createJsonPathExtractor(
				ResultMetadataFields.IS_DATASET,
				FieldExtractorType.TRUE_IF_MATCHES_OTHERWISE_FALSE,
				"$.package_id"));


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
}
