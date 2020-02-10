package fi.maanmittauslaitos.pta.search.metadata.model;

import org.apache.logging.log4j.Logger;

public abstract class NonThrowingCustomExtractor {

    private boolean isThrowException;

    public NonThrowingCustomExtractor() {
        this.isThrowException = true;
    }
    public NonThrowingCustomExtractor(boolean isThrowException) {
        this.isThrowException = isThrowException;
    }

    public boolean isThrowException() {
        return isThrowException;
    }

    public void setThrowException(boolean throwException) {
        isThrowException = throwException;
    }

    protected abstract Logger getLogger();

    protected void handleExtractorException(Throwable t, String logMessage) throws RuntimeException{
        if(isThrowException) {
            throw new RuntimeException(t);
        }
        if (logMessage == null) {
            logMessage = t.getMessage();
        }
        getLogger().debug(logMessage);
    }
}
