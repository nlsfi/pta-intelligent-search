package fi.maanmittauslaitos.pta.search.documentprocessor;

public class DocumentProcessingException extends Exception {
	private static final long serialVersionUID = 1L;

	public DocumentProcessingException(Throwable cause) {
		super(cause);
	}

	public DocumentProcessingException(String message) {
		super(message);
	}
	
	public DocumentProcessingException(String message, Throwable cause) {
		super(message, cause);
	}
}
