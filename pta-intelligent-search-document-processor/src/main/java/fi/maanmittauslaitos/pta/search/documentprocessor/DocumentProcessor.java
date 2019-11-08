package fi.maanmittauslaitos.pta.search.documentprocessor;

import fi.maanmittauslaitos.pta.search.documentprocessor.query.DocumentQuery;
import fi.maanmittauslaitos.pta.search.text.TextProcessingChain;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public abstract class DocumentProcessor {

	protected abstract DocumentQuery getDocumentQuerier();

	public abstract DocumentProcessingConfiguration getDocumentProcessingConfiguration();

	public abstract Document processDocument(InputStream is) throws DocumentProcessingException;

	protected Document processDocument(Map<String, TextProcessingChain> textProcessingChains, Document document) throws DocumentProcessingException {
		for (FieldExtractorConfiguration fec : getDocumentProcessingConfiguration().getFieldExtractors()) {
			Object value = fec.process(document, getDocumentQuerier());

			if (fec.getTextProcessorName() != null) {
				TextProcessingChain chain = textProcessingChains.get(fec.getTextProcessorName());

				if (chain == null) {
					throw new IllegalArgumentException("Text processor chain '" + fec.getTextProcessorName() + "' not declared");
				}

				@SuppressWarnings("unchecked")
				List<String> asList = (List<String>) value;

				value = chain.process(asList);
			}

			document.getFields().put(fec.getField(), value);
		}
		return document;
	}


}
