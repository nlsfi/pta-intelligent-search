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

@SpringBootApplication
public class CreateIntegrationTestQueries implements ApplicationRunner {
	private static final Logger logger = Logger.getLogger(CreateIntegrationTestQueries.class);

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
			//noinspection ResultOfMethodCallIgnored
			directory.mkdirs();
		}

		Arrays.asList(
				ConversionHelper.create("testcase_empty", Collections.emptyList()),
				ConversionHelper.create(Collections.singletonList("suomi")),
				ConversionHelper.create(Arrays.asList("jyväskylä", "tiet")),
				ConversionHelper.create(Collections.singletonList("hsl")),
				ConversionHelper.create(Collections.singletonList("uusimaa")),
				ConversionHelper.create(Collections.singletonList("keski-suomi")),
				ConversionHelper.create(Collections.singletonList("liito-orava")),
				ConversionHelper.create(Collections.singletonList("orava")),
				ConversionHelper.create(Collections.singletonList("ranta")),
				ConversionHelper.create(Collections.singletonList("rauta")),
				ConversionHelper.create(Collections.singletonList("korpilahti")),
				ConversionHelper.create("testcase_kansallinen", Collections.singletonList("nationwide"), Language.EN),
				ConversionHelper.create(Collections.singletonList("tammerfors"), Language.SV)
		).forEach(helper -> generateTestCaseQuery(helper.getQueryList(), Paths.get(outputDir, helper.getTestCaseName()), helper.getLanguage()));
	}

	private void generateTestCaseQuery(List<String> queryList, Path outFile, Language lang) {
		SearchQuery pyynto = new SearchQuery();
		pyynto.setQuery(queryList);

		String json = null;
		try {
			JSONObject jsonObject = new JSONObject(convertQueryIntoJSON(pyynto, lang));
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
		private static final String POSTFIX = ".json";
		private String testCaseName;
		private List<String> queryList;
		private Language lang;

		private ConversionHelper(String testcase, List<String> queryList, Language lang) {
			this.testCaseName = testcase;
			this.queryList = queryList;
			this.lang = lang;
		}

		static ConversionHelper create(List<String> queryList) {
			String testcase = TESTCASE + String.join("_", queryList) + POSTFIX;
			return new ConversionHelper(testcase, queryList, Language.FI);
		}

		static ConversionHelper create(List<String> queryList, Language lang) {
			String testcase = TESTCASE + String.join("_", queryList) + POSTFIX;
			return new ConversionHelper(testcase, queryList, lang);
		}

		static ConversionHelper create(String testcase, List<String> queryList) {
			return new ConversionHelper(testcase, queryList, Language.FI);
		}

		static ConversionHelper create(String testcase, List<String> queryList, Language lang) {
			return new ConversionHelper(testcase, queryList, lang);
		}

		String getTestCaseName() {
			return testCaseName;
		}

		List<String> getQueryList() {
			return queryList;
		}

		public Language getLanguage() {
			return lang;
		}
	}
}
