package fi.maanmittauslaitos.pta.search.documentprocessor;

import com.jayway.jsonpath.Configuration;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.DocumentQuerier;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.JsonDocumentQuerierImpl;
import fi.maanmittauslaitos.pta.search.text.TextProcessingChain;
import org.apache.commons.io.IOUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JsonDocumentProcessorImpl extends DocumentProcessor {
	private DocumentProcessingConfiguration configuration;
	private JsonDocumentQuerierImpl documentQuerier;

	JsonDocumentProcessorImpl(DocumentProcessingConfiguration configuration, Configuration jsonPathConfiguration) throws ParserConfigurationException {
		this.configuration = configuration;
		this.documentQuerier = JsonDocumentQuerierImpl.create(jsonPathConfiguration);
	}

	@Override
	protected DocumentQuerier getDocumentQuerier() {
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
		Map<String, TextProcessingChain> textProcessingChains = configuration.getTextProcessingChains();
		String content;

		try {
			content = IOUtils.toString(is, StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			throw new DocumentProcessingException(e);
		}
		JsonDocument ret = new JsonDocument();
		ret.setContent(content);

		return processDocument(textProcessingChains, ret);
	}

}
