package fi.maanmittauslaitos.pta.search.api;

import fi.maanmittauslaitos.pta.search.api.ApplicationConfiguration.MockElasticsearchQueryAPI;
import fi.maanmittauslaitos.pta.search.api.model.SearchQuery;
import fi.maanmittauslaitos.pta.search.api.search.HakuKone;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@SpringBootApplication
public class CreateIntegrationTestQueries implements ApplicationRunner {
	private static Logger logger = Logger.getLogger(CreateIntegrationTestQueries.class);

	@SuppressWarnings("unused")
	@Autowired
	private ApplicationConfiguration configuration;

	@Autowired
	private MockElasticsearchQueryAPI esQueryAPI;

	@Autowired
	private HakuKone hakukone;

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "createIntegrationTestQueries");
		try {
			ConfigurableApplicationContext ctx = SpringApplication.run(CreateIntegrationTestQueries.class, args);
			SpringApplication.exit(ctx);
		} catch (Exception e) {
			logger.error("Configuration error", e);
		}
	}

	@Override
	public void run(ApplicationArguments args) {
		String outputDir = args.getSourceArgs().length > 0 ? args.getSourceArgs()[0] : "jsontest";
		File directory = new File(outputDir);
		if (!directory.exists()) {
			directory.mkdirs();
		}


		Arrays.asList(
				new ConversionHelper("testcase_empty", Collections.emptyList()),
				new ConversionHelper(Collections.singletonList("suomi")),
				new ConversionHelper(Collections.singletonList("kansallinen")),
				new ConversionHelper(Arrays.asList("jyväskylä", "tiet")),
				new ConversionHelper(Collections.singletonList("hsl")),
				new ConversionHelper(Collections.singletonList("uusimaa")),
				new ConversionHelper(Collections.singletonList("keski-suomi"))
		).forEach(helper -> generateTestCaseQuery(helper.getQueryList(), Paths.get(outputDir, helper.getTestCaseName() + ".json")));
	}

	private void generateTestCaseQuery(List<String> queryList, Path outFile) {
		SearchQuery pyynto = new SearchQuery();
		pyynto.setQuery(queryList);

		String json = null;
		try {
			JSONObject jsonObject = new JSONObject(convertQueryIntoJSON(pyynto, Language.FI));
			json = jsonObject.getJSONObject("query").toString(2);

			Files.write(outFile, json.getBytes());
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}


		System.out.println("Query as JSON: " + json);
	}

	private String convertQueryIntoJSON(SearchQuery pyynto, Language lang) throws IOException {
		String json;

		try {
			hakukone.haku(pyynto, lang);
		} catch (NullPointerException npe) {
			/* NOP, we expect this to happen as the ES mock returns null */
		}

		json = esQueryAPI.getLastQueryAsJSON();
		return json;
	}

	static class ConversionHelper {
		private static final String TESTCASE = "testcase_";
		String testCaseName;
		List<String> queryList;

		ConversionHelper(List<String> queryList) {
			this.testCaseName = TESTCASE + String.join("_", queryList);
			this.queryList = queryList;
		}

		ConversionHelper(String testcase, List<String> queryList) {
			this.testCaseName = testcase;
			this.queryList = queryList;
		}

		String getTestCaseName() {
			return testCaseName;
		}

		public void setTestCaseName(String testCaseName) {
			this.testCaseName = testCaseName;
		}

		List<String> getQueryList() {
			return queryList;
		}

		public void setQueryList(List<String> queryList) {
			this.queryList = queryList;
		}

	}
}
