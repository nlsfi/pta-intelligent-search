package fi.maanmittauslaitos.pta.search.api;

import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import fi.maanmittauslaitos.pta.search.api.ApplicationConfiguration;
import fi.maanmittauslaitos.pta.search.api.ApplicationConfiguration.MockElasticsearchQueryAPI;
import fi.maanmittauslaitos.pta.search.api.Language;
import fi.maanmittauslaitos.pta.search.api.model.SearchQuery;
import fi.maanmittauslaitos.pta.search.api.search.HakuKone;

@SpringBootApplication
public class CreateIntegrationTestQueries implements ApplicationRunner {
	private static Logger logger = Logger.getLogger(CreateIntegrationTestQueries.class);
	
	@SuppressWarnings("unused")
	@Autowired
	private ApplicationConfiguration configuration;
	
	@Autowired
	private MockElasticsearchQueryAPI esQueryAPI;
	
	@Autowired
	HakuKone hakukone;
	
	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "createIntegrationTestQueries");
		try {
			ConfigurableApplicationContext ctx = SpringApplication.run(CreateIntegrationTestQueries.class, args);
			SpringApplication.exit(ctx);
		} catch(Exception e) {
			logger.error("Configuration error", e);
		}
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		SearchQuery pyynto = new SearchQuery();
		pyynto.setQuery(Arrays.asList("suomi"));
		
		boolean focusOnRegionalHits = false;
		String json = convertQueryIntoJSON(pyynto, Language.FI, focusOnRegionalHits);
		
		System.out.println("Query as JSON: "+json);
	}

	private String convertQueryIntoJSON(SearchQuery pyynto, Language lang, boolean focusOnRegionalHits) throws IOException
	{
		String json;
		
		try {
			hakukone.haku(pyynto, lang, focusOnRegionalHits);
		} catch(NullPointerException npe) {
			/* NOP, we expect this to happen as the ES mock returns null */
		}

		json = esQueryAPI.getLastQueryAsJSON();
		return json;
	}
}
