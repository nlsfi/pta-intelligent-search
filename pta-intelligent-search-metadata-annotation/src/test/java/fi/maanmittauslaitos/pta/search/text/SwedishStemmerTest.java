package fi.maanmittauslaitos.pta.search.text;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import fi.maanmittauslaitos.pta.search.text.stemmer.SwedishSnowballStemmer;

public class SwedishStemmerTest {

	SwedishSnowballStemmer stemmer;
	
	@Before
	public void setUp() throws Exception {
		stemmer = new SwedishSnowballStemmer();
	}

	@Test
	public void test() {
		assertEquals("bot", stemmer.stem("bot"));
		assertEquals("bot", stemmer.stem("botar"));
		assertEquals("bot", stemmer.stem("botarna"));
	}

}
