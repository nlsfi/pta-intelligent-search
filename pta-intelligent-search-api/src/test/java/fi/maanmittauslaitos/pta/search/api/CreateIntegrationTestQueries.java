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
    HakuKone hakukone;

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



        Stream.of(new ConversionHelper("testcase-1", Collections.singletonList("suomi"), false),
                new ConversionHelper("testcase-2", Arrays.asList("jyväskylä", "tiet"), true),
                new ConversionHelper("testcase-3", Collections.emptyList(), false),
                new ConversionHelper("testcase-4", Collections.singletonList("hsl"), false))
                .forEach(helper -> generateTestCaseQuery(helper.getQueryList(), Paths.get(outputDir, helper.getTestCaseName() + ".json"), helper.isFocusOnRegional()));
    }

    private void generateTestCaseQuery(List<String> queryList, Path outFile, boolean focusOnRegional) {
        SearchQuery pyynto = new SearchQuery();
        pyynto.setQuery(queryList);

        String json = null;
        try {
            JSONObject jsonObject = new JSONObject(convertQueryIntoJSON(pyynto, Language.FI, focusOnRegional));
            json = jsonObject.getJSONObject("query").toString(2);

            Files.write(outFile, json.getBytes());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }


        System.out.println("Query as JSON: " + json);
    }

    private String convertQueryIntoJSON(SearchQuery pyynto, Language lang, boolean focusOnRegionalHits) throws IOException {
        String json;

        try {
            hakukone.haku(pyynto, lang, focusOnRegionalHits);
        } catch (NullPointerException npe) {
            /* NOP, we expect this to happen as the ES mock returns null */
        }

        json = esQueryAPI.getLastQueryAsJSON();
        return json;
    }

    static class ConversionHelper {
        String testCaseName;
        List<String> queryList;
        boolean focusOnRegional;


        ConversionHelper(String testCaseName, List<String> queryList, boolean regionalFlag) {
            this.testCaseName = testCaseName;
            this.queryList = queryList;
            this.focusOnRegional = regionalFlag;
        }

        public String getTestCaseName() {
            return testCaseName;
        }

        public void setTestCaseName(String testCaseName) {
            this.testCaseName = testCaseName;
        }

        public List<String> getQueryList() {
            return queryList;
        }

        public void setQueryList(List<String> queryList) {
            this.queryList = queryList;
        }

        public boolean isFocusOnRegional() {
            return focusOnRegional;
        }

        public void setFocusOnRegional(boolean focusOnRegional) {
            this.focusOnRegional = focusOnRegional;
        }
    }
}
