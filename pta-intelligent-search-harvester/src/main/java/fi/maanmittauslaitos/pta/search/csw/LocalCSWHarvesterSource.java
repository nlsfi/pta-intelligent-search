package fi.maanmittauslaitos.pta.search.csw;

import fi.maanmittauslaitos.pta.search.HarvesterSource;
import fi.maanmittauslaitos.pta.search.HarvestingException;
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


	public URL getResourceRootURL() {
		return resourceRootURL;
	}

	public void setResourceRootURL(URL resourceRootURL) {
		this.resourceRootURL = resourceRootURL;
	}

	@Override
	public Iterator<Harvestable> iterator() {
		return new CSWIterator();
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


	private class CSWIterator implements Iterator<Harvestable> {
		private int numberOfRecordsProcessed = 0;
		private Integer numberOfRecordsInService;
		private LinkedList<File> localCSWs = null;

		CSWIterator() {
			localCSWs = new LinkedList<>();
			getLocalCSWs();
		}


		@Override
		public boolean hasNext() {
			return numberOfRecordsProcessed < numberOfRecordsInService;
		}

		@Override
		public Harvestable next() {
			if (localCSWs.size() == 0) {
				getLocalCSWs();
			}

			if (localCSWs.size() == 0) {
				return null;
			}

			File cswFile = localCSWs.removeFirst();
			numberOfRecordsProcessed++;

			return LocalHarvestable.create(cswFile, cswFile.getName().replace(".xml", ""));
		}


		private void getLocalCSWs() {
			try {
				if (resourceRootURL != null) {
					File[] resources = new File(resourceRootURL.getPath()).listFiles();
					if (resources != null) {
						numberOfRecordsInService = resources.length;
						localCSWs.addAll(Arrays.asList(resources));
					}

				}

				if (numberOfRecordsInService == null) {
					throw new IOException("Unable to determine how many records in CSW service");
				}

			} catch (IOException e) {
				throw new HarvestingException(e);
			}
		}
	}
}
