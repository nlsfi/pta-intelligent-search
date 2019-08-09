package fi.maanmittauslaitos.pta.search.utils;

import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;

public class RegionTest {

    @Test
    public void test_region_intersections() {
        RegionImpl finland = new RegionImpl(Arrays.asList(19.450000, 59.780000, 31.610000, 70.120000));
        RegionImpl keskiSuomi = new RegionImpl(Arrays.asList(24.010000, 61.450000, 26.780000, 63.610000));
        RegionImpl jklMunicipality = new RegionImpl(Arrays.asList(25.260000, 61.840000, 26.050000, 62.430000));
        RegionImpl jkl = new RegionImpl(Arrays.asList(25.26078, 61.83952, 26.25, 62.5));
        RegionImpl inari = new RegionImpl(Arrays.asList(24.900000, 68.280000, 29.340000, 69.850000));

        assertThat(finland.intersects(keskiSuomi)).isTrue();
        assertThat(finland.intersects(jklMunicipality)).isTrue();
        assertThat(finland.intersects(jkl)).isTrue();

        assertThat(finland.intersects(inari)).isTrue();
        assertThat(keskiSuomi.intersects(inari)).isFalse();
        assertThat(jklMunicipality.intersects(inari)).isFalse();

        assertThat(jkl.getIntersectionDividedByArea(finland)).isEqualTo(1.0);
        assertThat(jklMunicipality.getIntersectionDividedByArea(jkl))//
                .isCloseTo(1.0, withPercentage(5));
        assertThat(keskiSuomi.getIntersectionDividedByArea(jkl)).isCloseTo(0.11, withPercentage(5));
    }
}