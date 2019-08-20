package fi.maanmittauslaitos.pta.search.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.maanmittauslaitos.pta.search.utils.HarvesterTracker.IdentifierType.*;

public class HarvesterTrackerImpl implements HarvesterTracker {

	static final TypeReference<ConcurrentMap<IdentifierType, CopyOnWriteArrayList<String>>> TRACKER_FILE_TYPE_REFERENCE =
			new TypeReference<ConcurrentMap<IdentifierType, CopyOnWriteArrayList<String>>>() {
			};

	private static final Map<IdentifierType, List<String>> TRACKER_FILE_TEMPLATE =
			Stream.of(values()).collect(Collectors.toMap(type -> type, type -> Collections.emptyList()));

	private static final Logger logger = Logger.getLogger(HarvesterTrackerImpl.class);

	private final ObjectMapper objectMapper;


	private File trackerFile;
	private ConcurrentMap<IdentifierType, CopyOnWriteArrayList<String>> trackerMap;

	private HarvesterTrackerImpl(File trackerFile, ObjectMapper objectMapper) throws IOException {
		this.trackerFile = trackerFile;
		this.objectMapper = objectMapper;
		if (trackerFile.createNewFile()) {
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(trackerFile, TRACKER_FILE_TEMPLATE);
		}
		this.trackerMap = objectMapper.readValue(trackerFile, TRACKER_FILE_TYPE_REFERENCE);

		assert this.trackerMap.keySet().containsAll(Arrays.asList(values()));
	}

	public static HarvesterTrackerImpl create(File trackerFile, ObjectMapper objectMapper) throws IOException {
		return new HarvesterTrackerImpl(trackerFile, objectMapper);
	}

	@Override
	public void addIdToInserted(String identifier) {
		removeFromSkippeds(identifier);
		getByType(INSERTED).add(identifier);
		saveProcess();
	}

	@Override
	public boolean isYetToBeProcessed(String identifier) {
		return !getByType(PERMANENTLY_SKIPPED).contains(identifier) &&
				!getIdentifiersByType(INSERTED).contains(identifier) &&
				!getIdentifiersByType(UPDATED).contains(identifier);
	}

	@Override
	public void addIdToUpdated(String identifier) {
		removeFromSkippeds(identifier);
		getByType(UPDATED).add(identifier);
		saveProcess();
	}

	@Override
	public void addToSkippedDueHarvestingException(String identifier) {
		addToSkipped(identifier, SKIPPED_DUE_HARVESTING_EXCEPTION);
	}

	@Override
	public void addToSkippedDueProcessingException(String identifier) {
		addToSkipped(identifier, SKIPPED_DUE_PROCESSING_EXCEPTION);
	}

	@Override
	public Set<String> getIdentifiersByType(IdentifierType identifierType) {
		return new HashSet<>(getByType(identifierType));
	}

	@Override
	public Set<String> getIdentifiers() {
		return trackerMap.values().stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}

	@Override
	public void harvestingFinished() {
		if (getByType(SKIPPED_DUE_PROCESSING_EXCEPTION).isEmpty() &&
				getByType(SKIPPED_DUE_HARVESTING_EXCEPTION).isEmpty()) {
			getByType(INSERTED).clear();
			getByType(UPDATED).clear();
		}
		saveProcess();

		if (getIdentifiers().isEmpty() && !trackerFile.delete()) {
			logger.error("Failed to delete tracker file: " + trackerFile.toString());
		}
	}

	@Override
	public void harvestingInterrupted() {
		saveProcess();
	}

	@VisibleForTesting
	ConcurrentMap<IdentifierType, CopyOnWriteArrayList<String>> getTrackerMap() {
		return trackerMap;
	}

	private void addToSkipped(String identifier, IdentifierType skippedReason) {
		if (!isSkippedMoreThanThreshold(identifier, skippedReason)) {
			getByType(skippedReason).add(identifier);
		} else {
			getByType(skippedReason).removeIf(s -> s.equals(identifier));
			if (!getByType(PERMANENTLY_SKIPPED).contains(identifier))
				getByType(PERMANENTLY_SKIPPED).add(identifier);
		}
		saveProcess();
	}

	private CopyOnWriteArrayList<String> getByType(IdentifierType skippedDueProcessingException) {
		return trackerMap.get(skippedDueProcessingException);
	}

	private boolean isSkippedMoreThanThreshold(String identifier, IdentifierType type) {
		return Collections.frequency(getByType(type), identifier) >= RETRY_BEFORE_PERMANENTLY_SKIPPING;
	}

	private void removeFromSkippeds(String identifier) {
		getByType(SKIPPED_DUE_PROCESSING_EXCEPTION).removeIf(s -> s.equals(identifier));
		getByType(SKIPPED_DUE_HARVESTING_EXCEPTION).removeIf(s -> s.equals(identifier));
		getByType(PERMANENTLY_SKIPPED).removeIf(s -> s.equals(identifier));
	}


	private synchronized void saveProcess() {
		try {
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(trackerFile, trackerMap);
		} catch (IOException e) {
			logger.error("Failed to save tracker file", e);
		}
	}
}
