package fi.maanmittauslaitos.pta.search.utils;

import fi.maanmittauslaitos.pta.search.AbstractHarvester;
import fi.maanmittauslaitos.pta.search.HarvesterConfig;
import fi.maanmittauslaitos.pta.search.HarvesterSource;
import fi.maanmittauslaitos.pta.search.csw.Harvestable;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.index.DocumentSink;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.ApplicationArguments;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class LocalResourceMetadataGenerator extends AbstractHarvester {
	public static void main(String[] args) throws Exception {
		LocalResourceMetadataGenerator localResourceMetadataGenerator = new LocalResourceMetadataGenerator();
		localResourceMetadataGenerator.run(args);
	}

	@Override
	protected DocumentSink getDocumentSink(HarvesterConfig config, HarvesterTracker harvesterTracker, ApplicationArguments args) {
		List<String> sinkfileList = args.getOptionValues("sinkfile");
		String sinkfile = sinkfileList.size() > 0 ? sinkfileList.get(0) : "generatedResourceMetatada.zip";
		logger.debug("Sinkfile is " + sinkfile);
		return config.getLocalDocumentSink(sinkfile, harvesterTracker);
	}

	@Override
	protected DocumentProcessor getDocumentProcessor(HarvesterConfig config) throws ParserConfigurationException, IOException {
		return config.getCSWRecordProcessor();
	}

	@Override
	protected HarvesterSource getHarvesterSource(HarvesterConfig config) {
		return config.getLocalCSWSource();
	}

	private void downloadXmlFiles() throws IOException {
		HarvesterConfig config = new HarvesterConfig();

		HarvesterSource source = config.getCSWSource();

		int inserted = 0;

		for (Harvestable harvestable : source) {
			InputStream is = source.getInputStream(harvestable);
			if (is == null) {
				System.out.println("Source is null, skipping, or stopping");
				return;
			}
			try {

				File targetFile = new File("csws/" + inserted + ".xml");
				FileUtils.copyInputStreamToFile(is, targetFile);
				inserted++;
				if (inserted == 50) {
					return;
				}

			} finally {
				is.close();
			}
		}

		System.out.println("Inserted " + inserted + " documents");
	}

}
