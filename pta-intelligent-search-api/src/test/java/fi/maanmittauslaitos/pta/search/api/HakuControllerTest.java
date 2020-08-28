package fi.maanmittauslaitos.pta.search.api;

import fi.maanmittauslaitos.pta.search.api.language.LanguageDetectionResult;
import fi.maanmittauslaitos.pta.search.api.language.LanguageDetector;
import fi.maanmittauslaitos.pta.search.api.model.SearchQuery;
import org.elasticsearch.common.collect.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.lang.reflect.Method;
import java.util.Arrays;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = {ApplicationConfiguration.class, TestConfig.class}, loader = AnnotationConfigContextLoader.class)
public class HakuControllerTest {

    //@Autowired
    LanguageDetector languageDetector;

    //@Autowired
    HakuController hakuController;

    private Method deduceLanguage;
    @Before
    public void setup() throws Exception {
        deduceLanguage = HakuController.class.getDeclaredMethod("deduceLanguage", SearchQuery.class, Language.class);
        deduceLanguage.setAccessible(true);
    }


    //@Test
    public void test() throws Exception {
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQuery(Arrays.asList("god"));

        Tuple<Language, LanguageDetectionResult> result = (Tuple<Language, LanguageDetectionResult>) deduceLanguage.invoke(hakuController, searchQuery, Language.FI);
        System.out.println(result);
    }

}
class TestConfig {
    //@Bean
    public HakuController hakuController() {
        return new HakuController();
    }
}



