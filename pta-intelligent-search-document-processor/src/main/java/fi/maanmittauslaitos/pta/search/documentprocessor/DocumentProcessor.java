package fi.maanmittauslaitos.pta.search.documentprocessor;

import java.io.InputStream;

public interface DocumentProcessor {
	public DocumentProcessingConfiguration getDocumentProcessingConfiguration();
	public Document processDocument(InputStream is) throws DocumentProcessingException;
}
