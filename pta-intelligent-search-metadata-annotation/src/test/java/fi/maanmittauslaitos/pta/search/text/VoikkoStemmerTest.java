package fi.maanmittauslaitos.pta.search.text;

import com.google.common.collect.ImmutableMap;
import fi.maanmittauslaitos.pta.search.text.stemmer.FinnishVoikkoStemmer;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;

public class VoikkoStemmerTest {

	@Rule
	public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

	@Test
	public void test() {
		FinnishVoikkoStemmer stemmer = new FinnishVoikkoStemmer();

		softly.assertThat(stemmer.stem("kissa")).isEqualTo("kissa");
		softly.assertThat(stemmer.stem("kissoja")).isEqualTo("kissa");
		softly.assertThat(stemmer.stem("kissoista")).isEqualTo("kissa");
		softly.assertThat(stemmer.stem("kissoihin")).isEqualTo("kissa");
		softly.assertThat(stemmer.stem("kissoista")).isEqualTo("kissa");
	}

	@Test
	public void testWithPreStem() {
		ImmutableMap<String, String> preStem = ImmutableMap.of("suomi", "Suomen");
		FinnishVoikkoStemmer stemmer = new FinnishVoikkoStemmer(preStem, Collections.emptyMap());

		softly.assertThat(stemmer.stem("suomi")).isEqualTo("Suomi");
		softly.assertThat(stemmer.stem("Suomi")).isEqualTo("Suomi");
	}

	@Test
	public void testWithPostStem() {
		ImmutableMap<String, String> postStem = ImmutableMap.of("suomia", "Suomi", "suoma", "Suomi");
		FinnishVoikkoStemmer stemmer = new FinnishVoikkoStemmer(Collections.emptyMap(), postStem);

		softly.assertThat(stemmer.stem("suomi")).isEqualTo("Suomi");
		softly.assertThat(stemmer.stem("Suomi")).isEqualTo("Suomi");
	}

}
