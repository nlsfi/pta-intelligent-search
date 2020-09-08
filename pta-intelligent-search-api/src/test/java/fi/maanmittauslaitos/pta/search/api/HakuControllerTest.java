package fi.maanmittauslaitos.pta.search.api;

import fi.maanmittauslaitos.pta.search.api.language.LanguageDetectionResult;
import fi.maanmittauslaitos.pta.search.api.language.LanguageDetector;
import fi.maanmittauslaitos.pta.search.api.model.SearchQuery;
import org.elasticsearch.common.collect.Tuple;
import org.junit.Assert;
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
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class}, loader = AnnotationConfigContextLoader.class)
public class HakuControllerTest {

    @Autowired
    LanguageDetector languageDetector;

    @Autowired
    HakuController hakuController;

    @Test
    public void EvenScoreLanguageHintNotOneOfLanguages() throws Exception {
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQuery(Arrays.asList("barn"));

        Tuple<Language, LanguageDetectionResult> result = hakuController.deduceLanguage(searchQuery, Language.FI);

        LanguageDetectionResult detectionResult = result.v2();

        Assert.assertTrue(detectionResult.getPotentialLanguages().contains(Language.EN));
        Assert.assertTrue(detectionResult.getPotentialLanguages().contains(Language.SV));

        for(Language language : detectionResult.getTopLanguages()) {
            Assert.assertEquals(1, detectionResult.getScoreForLanguage(language));
        }
        //The score is equal, but application language preference order is Finnish, Swedish, English, so Swedish is selected
        Assert.assertEquals("sv", result.v1().getLowercaseLanguageCode());
    }

    @Test
    public void EvenScoreNoLanguageHint() throws Exception {
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQuery(Arrays.asList("barn"));

        Tuple<Language, LanguageDetectionResult> result = hakuController.deduceLanguage(searchQuery, null);

        LanguageDetectionResult detectionResult = result.v2();

        Assert.assertTrue(detectionResult.getPotentialLanguages().contains(Language.EN));
        Assert.assertTrue(detectionResult.getPotentialLanguages().contains(Language.SV));

        for(Language language : detectionResult.getTopLanguages()) {
            Assert.assertEquals(1, detectionResult.getScoreForLanguage(language));
        }
        //The score is equal, but the language hint is null and application language preference order is Finnish, Swedish, English, so Swedish is selected
        Assert.assertEquals("sv", result.v1().getLowercaseLanguageCode());
    }

    @Test
    public void EvenScoreLanguageHintEnglish() throws Exception {
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQuery(Arrays.asList("barn"));

        Tuple<Language, LanguageDetectionResult> result = hakuController.deduceLanguage(searchQuery, Language.EN);

        LanguageDetectionResult detectionResult = result.v2();

        Assert.assertTrue(detectionResult.getPotentialLanguages().contains(Language.EN));
        Assert.assertTrue(detectionResult.getPotentialLanguages().contains(Language.SV));

        for(Language language : detectionResult.getTopLanguages()) {
            Assert.assertEquals(1, detectionResult.getScoreForLanguage(language));
        }
        //The score is equal, but languagehint is English, so English is selected
        Assert.assertEquals("en", result.v1().getLowercaseLanguageCode());
    }

    @Test
    public void HigherEnglishScoreSwedidhHintEnglishLanguage() throws Exception {
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setQuery(Arrays.asList("local", "barn"));

        Tuple<Language, LanguageDetectionResult> result = hakuController.deduceLanguage(searchQuery, Language.SV);

        LanguageDetectionResult detectionResult = result.v2();

        Assert.assertEquals(Arrays.asList(Language.EN, Language.SV), detectionResult.getPotentialLanguages());
        Assert.assertEquals(1, detectionResult.getScoreForLanguage(Language.SV));
        Assert.assertEquals(2, detectionResult.getScoreForLanguage(Language.EN));

        Assert.assertEquals("en", result.v1().getLowercaseLanguageCode());
    }
}




