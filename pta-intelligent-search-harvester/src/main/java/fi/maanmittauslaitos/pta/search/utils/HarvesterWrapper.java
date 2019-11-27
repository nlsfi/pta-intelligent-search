package fi.maanmittauslaitos.pta.search.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfigurationImpl;
import fi.maanmittauslaitos.pta.search.source.HarvesterSource;
import org.apache.log4j.Logger;

import java.io.IOException;

import static fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants.FIELD_CATALOG;
import static fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants.FIELD_CATALOG_TYPE;
import static fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants.FIELD_CATALOG_URL;

public class HarvesterWrapper {

	private static final Logger logger = Logger.getLogger(HarvesterWrapper.class);

	private final HarvesterSource source;
	private final DocumentProcessor processor;
	private static final String CATALOG_FIELD_TEMPLATE = "{\n" +
			"    \"" + FIELD_CATALOG_URL + "\": \"%s\",\n" +
			"    \"" + FIELD_CATALOG_TYPE + "\": \"%s\"\n" +
			"  }";

	protected HarvesterWrapper(HarvesterSource source, DocumentProcessor processor) {
		this.source = source;
		this.processor = processor;
	}

	public static HarvesterWrapper create(HarvesterSource source, DocumentProcessor processor, ObjectMapper objectMapper) {
		// Add Type and source
		FieldExtractorConfigurationImpl catalogDefaultExtractor = new FieldExtractorConfigurationImpl();

		try {
			catalogDefaultExtractor.setDefaultValue(objectMapper.readTree(String.format(CATALOG_FIELD_TEMPLATE,
					source.getOnlineResource(), source.getMetadataType())));
		} catch (IOException e) {
			logger.error("Could not add catalog extractor");
		}
		catalogDefaultExtractor.setType(FieldExtractorConfigurationImpl.FieldExtractorType.DEFAULT_VALUE);
		catalogDefaultExtractor.setField(FIELD_CATALOG);

		processor.getDocumentProcessingConfiguration().getFieldExtractors().add(catalogDefaultExtractor);
		return new HarvesterWrapper(source, processor);
	}

	public HarvesterSource getSource() {
		return source;
	}

	public DocumentProcessor getProcessor() {
		return processor;
	}

}
