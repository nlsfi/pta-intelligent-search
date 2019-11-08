package fi.maanmittauslaitos.pta.search.source.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.maanmittauslaitos.pta.search.HarvestingException;
import fi.maanmittauslaitos.pta.search.source.Harvestable;
import fi.maanmittauslaitos.pta.search.source.HarvesterInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class LocalCKANHarvesterSource extends CKANHarvesterSource {
	private static Logger logger = Logger.getLogger(LocalCKANHarvesterSource.class);
	private final ObjectMapper objectMapper;
	private URL resourceRootURL;

	public LocalCKANHarvesterSource(ObjectMapper objectMapper) {
		super(objectMapper);
		this.objectMapper = objectMapper;
	}

	public void setResourceRootURL(URL resourceRootURL) {
		this.resourceRootURL = resourceRootURL;
	}

	@Override
	public Iterator<Harvestable> iterator() {
		return new LocalCKANIterator(resourceRootURL);
	}

	@Override
	public HarvesterInputStream getInputStream(Harvestable harvestable) {
		JSONHarvestable jsonHarvestable = (JSONHarvestable) harvestable;
		return HarvesterInputStream.wrap(IOUtils.toInputStream(jsonHarvestable.getContent().toString()));
	}

	private JsonNode readJson(File file) {
		try {
			return objectMapper.readTree(file);
		} catch (IOException e) {
			throw new HarvestingException(e);
		}
	}


	private class LocalCKANIterator implements Iterator<Harvestable> {
		private final URL resourceRootURL;
		private int numberOfRecordsProcessed = 0;
		private Integer numberOfRecordsInService;
		private LinkedList<JSONHarvestable> localItems;

		LocalCKANIterator(URL resourceRootURL) {
			localItems = new LinkedList<>();
			this.resourceRootURL = resourceRootURL;
			getLocalItems();
		}

		@Override
		public boolean hasNext() {
			return numberOfRecordsProcessed < numberOfRecordsInService;
		}


		@Override
		public Harvestable next() {
			if (localItems.size() == 0) {
				getLocalItems();
			}

			if (localItems.size() == 0) {
				return null;
			}

			numberOfRecordsProcessed++;
			return localItems.removeFirst();
		}

		private void getLocalItems() {
			if (resourceRootURL != null) {
				File[] resources = new File(resourceRootURL.getPath()).listFiles();
				if (resources != null) {
					localItems = Arrays.stream(resources)
							.map(file -> readJsonToNodes(readJson(file)))
							.flatMap(Collection::stream)
							.map(node -> JSONHarvestable.create(getIdentifierFromJsonNode(node), node))
							.collect(Collectors.toCollection(LinkedList::new));
				}
				numberOfRecordsInService = localItems.size();
			}
		}
	}


}


