package fi.maanmittauslaitos.pta.search.api.region;


import com.google.common.collect.ImmutableMap;
import fi.maanmittauslaitos.pta.search.api.Language;
import fi.maanmittauslaitos.pta.search.api.language.LuceneAnalyzerStemmer;
import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;
import fi.maanmittauslaitos.pta.search.text.stemmer.StemmerFactory;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.sv.SwedishAnalyzer;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class RegionNameContainerTest {
    private static String RESOURCE_ROOT = "fi/maanmittauslaitos/pta/search/api/region/";
    private static String RESOURCE_COUNTRY = RESOURCE_ROOT + "test_countries.json";
    private static String RESOURCE_REGIONS = RESOURCE_ROOT + "test_regions.json";
    private static String RESOURCE_SUBREGIONS = RESOURCE_ROOT + "test_subregions.json";
    private static String RESOURCE_MUNICIPALITIES = RESOURCE_ROOT + "test_municipalities.json";

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();


    private RegionNameContainerImpl regionNameContainer;

    private Map<Language, Stemmer> stemmers = ImmutableMap.of(Language.FI, StemmerFactory.createFinnishStemmer(),
            Language.SV, new LuceneAnalyzerStemmer(new SwedishAnalyzer()),
            Language.EN, new LuceneAnalyzerStemmer(new EnglishAnalyzer()));


    @Before
    public void setUp() {
        regionNameContainer = new RegionNameContainerImpl(RESOURCE_COUNTRY, RESOURCE_REGIONS, RESOURCE_SUBREGIONS, RESOURCE_MUNICIPALITIES, stemmers);
        regionNameContainer.init();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void region_name_container_has_values() {
        softly.assertThat(regionNameContainer.getStemmedRegionNames()).containsKeys(Language.values());
        softly.assertThat(regionNameContainer.getStemmedRegionNames().get(Language.FI))
                .contains(entry("suomia", "Suomi"), entry("Uusimaa", "Uusimaa"), entry("Helsinki", "Helsinki"), entry("stadi", "Helsinki"), entry("kansallinen", "Suomi"));

        softly.assertThat(regionNameContainer.getStemmedRegionNames().get(Language.EN))
                .contains(entry("citi", "Helsinki"), entry("nationwid", "Suomi"),
                        entry("stadi", "Helsinki"), entry("helsingfor", "Helsinki"));

        softly.assertThat(regionNameContainer.getStemmedRegionNames().get(Language.SV))
                .contains(entry("stad", "Helsinki"), entry("helsingfor", "Helsinki"));

        softly.assertThat(regionNameContainer.getRegionNamesByRegionType()).containsKeys(RegionNameContainer.RegionType.values());

        softly.assertThat(regionNameContainer.getRegionNamesByRegionType().get(RegionNameContainer.RegionType.MUNICIPALITY))
                .containsExactlyInAnyOrder("Espoo", "Helsinki", "Vantaa", "Lahti");
    }

    @Test
    public void search_result_finds_existing_region() {
        RegionNameSearchResult regionNameSearchResult = RegionNameSearchResult.executeSearch("Kansallinen", "kansallinen", regionNameContainer, Language.FI);

        assertThat(regionNameSearchResult.hasRegionName()).isTrue();
        assertThat(regionNameSearchResult.getParsedRegion()).isEqualTo("Suomi");
    }

    @Test
    public void search_result_with_invalid_region() {
        RegionNameSearchResult regionNameSearchResult = RegionNameSearchResult.executeSearch("else", "else", regionNameContainer, Language.EN);

        softly.assertThat(regionNameSearchResult.hasRegionName()).isFalse();
        softly.assertThat(regionNameSearchResult.getParsedRegion()).isEmpty();
    }

}