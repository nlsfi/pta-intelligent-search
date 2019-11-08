package fi.maanmittauslaitos.pta.search.documentprocessor;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.DocumentQuery;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.JsonDocumentQueryImpl;
import fi.maanmittauslaitos.pta.search.text.TextProcessingChain;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JsonDocumentProcessorImpl extends DocumentProcessor {
	private final DocumentProcessingConfiguration configuration;
	private final JsonDocumentQueryImpl documentQuerier;

	JsonDocumentProcessorImpl(DocumentProcessingConfiguration configuration) {
		this.configuration = configuration;
		this.documentQuerier = JsonDocumentQueryImpl.create(createJsonPathConfiguration());
	}

	private static Configuration createJsonPathConfiguration() {
		JsonProvider jsonProvider = new JacksonJsonProvider();
		MappingProvider mappingProvider = new JacksonMappingProvider();

		return Configuration.builder()
				.jsonProvider(jsonProvider)
				.mappingProvider(mappingProvider)
				.options(Option.ALWAYS_RETURN_LIST, Option.DEFAULT_PATH_LEAF_TO_NULL, Option.SUPPRESS_EXCEPTIONS)
				.build();
	}

	@Override
	protected DocumentQuery getDocumentQuerier() {
		return documentQuerier;
	}

	@Override
	public DocumentProcessingConfiguration getDocumentProcessingConfiguration() {
		return configuration;
	}

	public DocumentProcessingConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public Document processDocument(InputStream is) throws DocumentProcessingException {
		Map<String, TextProcessingChain> textProcessingChains = getConfiguration().getTextProcessingChains();
		String content;

		try {
			content = IOUtils.toString(is, StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			throw new DocumentProcessingException(e);
		}
		JsonDocument ret = new JsonDocument();
		ret.setDocumentContext(documentQuerier.parseJsonString(content));

		return processDocument(textProcessingChains, ret);
	}

}
