package fi.maanmittauslaitos.pta.search.index;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;

public interface DocumentSink {
	public IndexResult indexDocument(Document doc) throws SinkProcessingException;
	
	public enum IndexResult {
		INSERTED,
		UPDATED
	};
}
