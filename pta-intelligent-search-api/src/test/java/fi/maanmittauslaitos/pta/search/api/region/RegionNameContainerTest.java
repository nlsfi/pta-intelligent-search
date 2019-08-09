package fi.maanmittauslaitos.pta.search.api.region;


import com.google.common.collect.ImmutableMap;
import fi.maanmittauslaitos.pta.search.api.Language;
import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

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

    @Mock
    private Stemmer stemmerMock;

    private Map<Language, Stemmer> stemmersMock;
    private RegionNameContainer regionNameContainer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(stemmerMock.stem(Mockito.anyString())).thenAnswer(region -> region.getArguments()[0].toString().toLowerCase());
        stemmersMock = ImmutableMap.of(Language.FI, stemmerMock, Language.SV, stemmerMock, Language.EN, stemmerMock);
        regionNameContainer = new RegionNameContainer(RESOURCE_COUNTRY, RESOURCE_REGIONS, RESOURCE_SUBREGIONS, RESOURCE_MUNICIPALITIES, stemmersMock);
        regionNameContainer.init();
    }

    @Test
    public void region_name_container_has_values() {
        softly.assertThat(regionNameContainer.getStemmedRegionNames()).containsKeys(Language.values());
        softly.assertThat(regionNameContainer.getStemmedRegionNames().get(Language.FI))
                .contains(entry("suomi", "Suomi"), entry("uusimaa", "Uusimaa"), entry("helsinki", "Helsinki"), entry("stadi", "Helsinki"), entry("kansallinen", "Suomi"));

        softly.assertThat(regionNameContainer.getStemmedRegionNames().get(Language.EN))
                .contains(entry("city", "Helsinki"), entry("nationwide", "Suomi"));

        softly.assertThat(regionNameContainer.getStemmedRegionNames().get(Language.SV))
                .contains(entry("stad", "Helsinki"));

        softly.assertThat(regionNameContainer.getRegionNamesByRegionType()).containsKeys(RegionNameContainer.RegionType.values());

        softly.assertThat(regionNameContainer.getRegionNamesByRegionType().get(RegionNameContainer.RegionType.MUNICIPALITY))
                .containsExactlyInAnyOrder("Espoo", "Helsinki", "Vantaa");
    }

    @Test
    public void search_result_finds_existing_region() {
        RegionNameSearchResult regionNameSearchResult = RegionNameSearchResult.executeSearch("suomi", "suomi", regionNameContainer, Language.FI);

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