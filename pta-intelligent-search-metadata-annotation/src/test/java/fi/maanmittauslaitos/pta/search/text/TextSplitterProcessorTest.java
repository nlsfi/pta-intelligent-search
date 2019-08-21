package fi.maanmittauslaitos.pta.search.text;

import fi.maanmittauslaitos.pta.search.text.stemmer.StemmerFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
public class TextSplitterProcessorTest {

	private TextProcessor processor;
	
	@Before
	public void setUp() {
		processor = TextSplitterProcessor.create(StemmerFactory.createFinnishStemmer(), Stream.of("aineisto", "hankinta", "alue").collect(Collectors.toSet()), true);
	}

	@Test
	public void test() {
		List<String> result = processor.process(Arrays.asList("ABC Kissa kävelee, tikapuita pitkin - taivaaseen!"));
		assertThat(result).containsExactlyInAnyOrder("ABC", "Kissa", "kävelee", "tikapuita", "pitkin", "taivaaseen");
	}

	@Test
	public void testWhitespacePrefix() {
		List<String> result = processor.process(Arrays.asList(" ABC Kissa kävelee, tikapuita pitkin - taivaaseen!"));

		assertThat(result).containsExactlyInAnyOrder("ABC", "Kissa", "kävelee", "tikapuita", "pitkin", "taivaaseen");
	}

	@Test
	public void testWordsWithHyphen() {
		List<String> result = processor.process(Collections.singletonList("liito-orava eläin- ja kasvikunta ELY-keskus kaava-aineisto"));
		assertThat(result)
				.containsExactlyInAnyOrder("liito-orava", "liitoorava",
						"eläin", "ja", "kasvikunta",
						"ELY-keskus", "ELYkeskus",
						"kaava-aineisto", "kaavaaineisto", "kaava", "aineisto");
	}
}
