package fi.maanmittauslaitos.pta.search.documentprocessor;

import java.io.InputStream;

public interface DocumentProcessor {
	public Document processDocument(InputStream is) throws DocumentProcessingException;
}
