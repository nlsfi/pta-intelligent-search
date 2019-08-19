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

public class HarvesterTrackerImpl implements HarvesterTracker {

	static final TypeReference<ConcurrentMap<IdentifierType, CopyOnWriteArrayList<String>>> TRACKER_FILE_TYPE_REFERENCE =
			new TypeReference<ConcurrentMap<IdentifierType, CopyOnWriteArrayList<String>>>() {
			};

	private static final Map<IdentifierType, List<String>> TRACKER_FILE_TEMPLATE =
			Stream.of(IdentifierType.values()).collect(Collectors.toMap(type -> type, type -> Collections.emptyList()));

	private static final Logger logger = Logger.getLogger(HarvesterTrackerImpl.class);

	private final ObjectMapper objectMapper;


	private File trackerFile;
	private ConcurrentMap<IdentifierType, CopyOnWriteArrayList<String>> trackerMap;


	public HarvesterTrackerImpl(File trackerFile, ObjectMapper objectMapper) throws IOException {
		this.trackerFile = trackerFile;
		this.objectMapper = objectMapper;
		if (trackerFile.createNewFile()) {
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(trackerFile, TRACKER_FILE_TEMPLATE);
		}
		this.trackerMap = objectMapper.readValue(trackerFile, TRACKER_FILE_TYPE_REFERENCE);

		assert this.trackerMap.keySet().containsAll(Arrays.asList(IdentifierType.values()));

	}


	@VisibleForTesting
	ConcurrentMap<IdentifierType, CopyOnWriteArrayList<String>> getTrackerMap() {
		return trackerMap;
	}

	@Override
	public boolean isYetToBeProcessed(String identifier) {
		return !trackerMap.get(IdentifierType.PERMANENTLY_SKIPPED).contains(identifier) &&
				!getIdentifiersByType(IdentifierType.INSERTED).contains(identifier);
	}

	@Override
	public void addToSkippedDueHarvestingException(String identifier) {
		if (!trackerMap.get(IdentifierType.SKIPPED_DUE_HARVESTING_EXCEPTION).contains(identifier)) {
			trackerMap.get(IdentifierType.SKIPPED_DUE_HARVESTING_EXCEPTION).add(identifier);
		}
	}

	@Override
	public void addToSkippedDueProcessingException(String identifier) {
		if (!isSkippedMoreThanThreshold(identifier)) {
			trackerMap.get(IdentifierType.SKIPPED_DUE_PROCESSING_EXCEPTION).add(identifier);
		} else {
			trackerMap.get(IdentifierType.SKIPPED_DUE_PROCESSING_EXCEPTION).removeIf(s -> s.equals(identifier));
			if (!trackerMap.get(IdentifierType.PERMANENTLY_SKIPPED).contains(identifier))
				trackerMap.get(IdentifierType.PERMANENTLY_SKIPPED).add(identifier);
		}
		saveProcess();
	}

	private boolean isSkippedMoreThanThreshold(String identifier) {
		return Collections.frequency(trackerMap.get(IdentifierType.SKIPPED_DUE_PROCESSING_EXCEPTION), identifier)
				>= RETRY_FOR_PROCESSING_EXCEPTION;
	}

	@Override
	public void addIdToInserted(String identifier) {
		removeFromSkippeds(identifier);
		trackerMap.get(IdentifierType.INSERTED).add(identifier);
		saveProcess();
	}

	@Override
	public void addIdToUpdated(String identifier) {
		removeFromSkippeds(identifier);
		trackerMap.get(IdentifierType.UPDATED).add(identifier);
		saveProcess();
	}

	private void removeFromSkippeds(String identifier) {
		trackerMap.get(IdentifierType.SKIPPED_DUE_PROCESSING_EXCEPTION).removeIf(s -> s.equals(identifier));
		trackerMap.get(IdentifierType.SKIPPED_DUE_HARVESTING_EXCEPTION).removeIf(s -> s.equals(identifier));
		trackerMap.get(IdentifierType.PERMANENTLY_SKIPPED).removeIf(s -> s.equals(identifier));
	}


	@Override
	public Set<String> getIdentifiersByType(IdentifierType identifierType) {
		return new HashSet<>(trackerMap.get(identifierType));
	}

	@Override
	public Set<String> getIdentifiers() {
		return trackerMap.values().stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}

	@Override
	public void harvestingFinished() {
		if (trackerMap.get(IdentifierType.SKIPPED_DUE_PROCESSING_EXCEPTION).isEmpty() &&
				trackerMap.get(IdentifierType.SKIPPED_DUE_HARVESTING_EXCEPTION).isEmpty()) {
			trackerMap.get(IdentifierType.INSERTED).clear();
			trackerMap.get(IdentifierType.UPDATED).clear();
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

	private synchronized void saveProcess() {
		try {
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(trackerFile, trackerMap);
		} catch (IOException e) {
			logger.error("Failed to save tracker file", e);
		}
	}
}
