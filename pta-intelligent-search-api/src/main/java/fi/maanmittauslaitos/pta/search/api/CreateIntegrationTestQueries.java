package fi.maanmittauslaitos.pta.search.api;

import com.google.common.collect.ImmutableMap;
import fi.maanmittauslaitos.pta.search.api.ApplicationConfiguration.MockElasticsearchQueryAPI;
import fi.maanmittauslaitos.pta.search.api.model.SearchQuery;
import fi.maanmittauslaitos.pta.search.api.search.HakuKone;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CreateIntegrationTestQueries implements ApplicationRunner {

	private final MockElasticsearchQueryAPI esQueryAPI;

	private final HakuKone hakukone;

	public CreateIntegrationTestQueries(MockElasticsearchQueryAPI esQueryAPI, HakuKone hakukone) {
		this.esQueryAPI = esQueryAPI;
		this.hakukone = hakukone;
	}

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "createIntegrationTestQueries");
		ApplicationContext ctx = new AnnotationConfigApplicationContext(ApplicationConfiguration.class);
		Objects.requireNonNull(ctx);

		CreateIntegrationTestQueries runner = new CreateIntegrationTestQueries(ctx.getBean(MockElasticsearchQueryAPI.class), ctx.getBean(HakuKone.class));
		runner.run(new DefaultApplicationArguments(args));
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
				ConversionHelper.create(Arrays.asList("suomen", "tiet")),
				ConversionHelper.create(Arrays.asList("suomen", "liikenneverkot")),
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
				ConversionHelper.create(Collections.singletonList("tammerfors"), Language.SV),
				ConversionHelper.create(Collections.singletonList("oulu")),
				ConversionHelper.create(Collections.singletonList("rakennukset")),
				ConversionHelper.create("testcase_rakennukset_biota", Collections.singletonList("rakennukset"),
						ImmutableMap.of("topicCategories", Collections.singletonList("biota")))
		).forEach(helper -> generateTestCaseQuery(helper, outputDir));
	}

	private void generateTestCaseQuery(ConversionHelper helper, String outputDir) {
		SearchQuery pyynto = new SearchQuery();
		pyynto.setQuery(helper.getQueryList());
		pyynto.setFacets(helper.getFacets());

		String json = null;
		try {
			JSONObject jsonObject = new JSONObject(convertQueryIntoJSON(pyynto, helper.getLanguage()));
			json = jsonObject.getJSONObject("query").toString(2);

			Files.write(Paths.get(outputDir, helper.getTestCaseName()), json.getBytes());
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
		private final String testCaseName;
		private final List<String> queryList;
		private final Language lang;
		private final Map<String, List<String>> facets;

		private ConversionHelper(String testcase, List<String> queryList, Language lang) {
			this.testCaseName = testcase + POSTFIX;
			this.queryList = queryList;
			this.lang = lang;
			this.facets = Collections.emptyMap();
		}

		public ConversionHelper(String testcase, List<String> queryList, Language lang, Map<String, List<String>> facets) {
			this.testCaseName = testcase + POSTFIX;
			this.queryList = queryList;
			this.lang = lang;
			this.facets = facets;
		}

		static ConversionHelper create(List<String> queryList) {
			String testcase = TESTCASE + String.join("_", queryList);
			return new ConversionHelper(testcase, queryList, Language.FI);
		}

		static ConversionHelper create(String testcase, List<String> queryList, Map<String, List<String>> facets) {
			return new ConversionHelper(testcase, queryList, Language.FI, facets);
		}

		static ConversionHelper create(List<String> queryList, Language lang) {
			String testcase = TESTCASE + String.join("_", queryList);
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

		Language getLanguage() {
			return lang;
		}

		Map<String, List<String>> getFacets() {
			return facets;
		}
	}

}
