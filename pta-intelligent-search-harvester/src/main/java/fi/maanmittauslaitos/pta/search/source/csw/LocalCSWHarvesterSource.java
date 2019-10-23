package fi.maanmittauslaitos.pta.search.source.csw;

import fi.maanmittauslaitos.pta.search.HarvestingException;
import fi.maanmittauslaitos.pta.search.source.Harvestable;
import fi.maanmittauslaitos.pta.search.source.HarvesterInputStream;
import fi.maanmittauslaitos.pta.search.source.HarvesterSource;
import org.apache.log4j.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class LocalCSWHarvesterSource extends HarvesterSource {

	private static Logger logger = Logger.getLogger(LocalCSWHarvesterSource.class);
	private URL resourceRootURL;

	public LocalCSWHarvesterSource() {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);

	}

	public void setResourceRootURL(URL resourceRootURL) {
		this.resourceRootURL = resourceRootURL;
	}

	@Override
	public Iterator<Harvestable> iterator() {
		return new LocalItemIterator(".xml", resourceRootURL);
	}

	@Override
	public HarvesterInputStream getInputStream(Harvestable harvestable) {
		logger.debug("Requesting record with id " + harvestable.getIdentifier());

		try {
			return HarvesterInputStream.wrap(new FileInputStream(((LocalHarvestable) harvestable).getFile()));

		} catch (IOException | ClassCastException e) {
			throw new HarvestingException(e);
		}
	}

	public static class LocalItemIterator implements Iterator<Harvestable> {
		private final URL resourceRootURL;
		private int numberOfRecordsProcessed = 0;
		private Integer numberOfRecordsInService;
		private String prefix;
		private LinkedList<File> localItems = null;

		public LocalItemIterator(String prefix, URL resourceRootURL) {
			localItems = new LinkedList<>();
			this.prefix = prefix;
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

			File metadataItem = localItems.removeFirst();
			numberOfRecordsProcessed++;

			return LocalHarvestable.create(metadataItem, metadataItem.getName().replace(prefix, ""));
		}


		private void getLocalItems() {
			try {
				if (resourceRootURL != null) {
					File[] resources = new File(resourceRootURL.getPath()).listFiles();
					if (resources != null) {
						numberOfRecordsInService = resources.length;
						localItems.addAll(Arrays.asList(resources));
					}

				}

				if (numberOfRecordsInService == null) {
					throw new IOException("Unable to determine how many records in the service");
				}

			} catch (IOException e) {
				throw new HarvestingException(e);
			}
		}
	}
}
