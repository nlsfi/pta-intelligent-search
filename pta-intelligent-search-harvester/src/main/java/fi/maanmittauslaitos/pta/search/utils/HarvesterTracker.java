package fi.maanmittauslaitos.pta.search.utils;

import java.util.Set;

public interface HarvesterTracker {

	int RETRY_FOR_HARVESTING_EXCEPTION = 2;
	int RETRY_BEFORE_PERMANENTLY_SKIPPING = 2;

	void addToSkippedDueHarvestingException(String identifier);

	boolean isYetToBeProcessed(String identifier);

	void addIdToInserted(String identifier);

	void addIdToUpdated(String identifier);

	void addToSkippedDueProcessingException(String identifier);

	enum IdentifierType {
		INSERTED,
		UPDATED,
		PERMANENTLY_SKIPPED,
		SKIPPED_DUE_PROCESSING_EXCEPTION,
		SKIPPED_DUE_HARVESTING_EXCEPTION
	}

	Set<String> getIdentifiersByType(IdentifierType identifierType);

	Set<String> getIdentifiers();

	void harvestingFinished();

	void harvestingInterrupted();
}
