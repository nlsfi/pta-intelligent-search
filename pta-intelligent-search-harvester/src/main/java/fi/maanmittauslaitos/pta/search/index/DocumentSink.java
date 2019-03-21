package fi.maanmittauslaitos.pta.search.index;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;

public interface DocumentSink {
	public void startIndexing() throws SinkProcessingException;
	public IndexResult indexDocument(Document doc) throws SinkProcessingException;
	public int stopIndexing() throws SinkProcessingException;
	
	public enum IndexResult {
		INSERTED,
		UPDATED
	};
}
