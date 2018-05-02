package fi.maanmittauslaitos.pta.documentprocessor;

import java.io.InputStream;

public interface DocumentProcessor {
	public Document processDocument(InputStream is) throws DocumentProcessingException;
}
