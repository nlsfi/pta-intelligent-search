package fi.maanmittauslaitos.pta.search.utils;

import java.util.Set;

public interface HarvesterTracker {

	int RETRY_FOR_HARVESTING_EXCEPTION = 2;
	int RETRY_FOR_PROCESSING_EXCEPTION = 2;

	boolean isYetToBeProcessed(String identifier);

	void addToSkippedDueHarvestingException(String identifier);

	void addToSkippedDueProcessingException(String identifier);

	void addIdToInserted(String identifier);

	void addIdToUpdated(String identifier);

	Set<String> getIdentifiersByType(IdentifierType identifierType);

	Set<String> getIdentifiers();

	void harvestingFinished();

	void harvestingInterrupted();

	enum IdentifierType {
		INSERTED,
		UPDATED,
		PERMANENTLY_SKIPPED,
		SKIPPED_DUE_PROCESSING_EXCEPTION,
		SKIPPED_DUE_HARVESTING_EXCEPTION
	}
}
