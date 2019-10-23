package fi.maanmittauslaitos.pta.search;

import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.index.DocumentSink;
import fi.maanmittauslaitos.pta.search.source.HarvesterSource;
import fi.maanmittauslaitos.pta.search.utils.HarvesterTracker;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

@SpringBootApplication
public class Harvester extends AbstractHarvester {

	public static void main(String[] args) throws Exception
	{
		SpringApplication.run(Harvester.class, args);
	}

	protected DocumentSink getDocumentSink(HarvesterConfig config, HarvesterTracker harvesterTracker, ApplicationArguments args) {
		return config.getDocumentSink(harvesterTracker);
	}

	protected DocumentProcessor getDocumentProcessor(HarvesterConfig config) throws ParserConfigurationException, IOException {
		return config.getCSWRecordProcessor();
	}

	protected HarvesterSource getHarvesterSource(HarvesterConfig config) {
		return config.getCSWSource();
	}

}
