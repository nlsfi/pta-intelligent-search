package fi.maanmittauslaitos.pta.search.csw;

public class CSWProcessingException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public CSWProcessingException(Throwable cause) {
		super(cause);
	}
	
	public CSWProcessingException(String message, Throwable cause) {
		super(message, cause);
	}
}
