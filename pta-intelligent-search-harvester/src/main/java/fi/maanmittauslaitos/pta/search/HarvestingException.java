package fi.maanmittauslaitos.pta.search;

public class HarvestingException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public HarvestingException(Throwable cause) {
		super(cause);
	}
	
	public HarvestingException(String message, Throwable cause) {
		super(message, cause);
	}
}
