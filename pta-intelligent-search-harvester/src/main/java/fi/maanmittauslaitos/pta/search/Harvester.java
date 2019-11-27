package fi.maanmittauslaitos.pta.search;

import fi.maanmittauslaitos.pta.search.index.DocumentSink;
import fi.maanmittauslaitos.pta.search.utils.HarvesterTracker;
import fi.maanmittauslaitos.pta.search.utils.HarvesterWrapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Collection;

@SpringBootApplication
public class Harvester extends AbstractHarvester {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Harvester.class, args);
	}

	@Override
	protected Collection<HarvesterWrapper> getHarvesterWrappers(HarvesterConfig config) throws ParserConfigurationException, IOException {
		return config.getHarvesterWrappers();
	}

	protected DocumentSink getDocumentSink(HarvesterConfig config, HarvesterTracker harvesterTracker, ApplicationArguments args) {
		return config.getDocumentSink(harvesterTracker);
	}
}
