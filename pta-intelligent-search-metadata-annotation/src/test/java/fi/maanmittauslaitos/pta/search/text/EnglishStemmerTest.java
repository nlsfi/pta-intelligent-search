package fi.maanmittauslaitos.pta.search.text;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import fi.maanmittauslaitos.pta.search.text.stemmer.EnglishSnowballStemmer;

public class EnglishStemmerTest {

	EnglishSnowballStemmer stemmer;
	
	@Before
	public void setUp() throws Exception {
		stemmer = new EnglishSnowballStemmer();
	}

	@Test
	public void test() {
		assertEquals("rowboat", stemmer.stem("rowboat"));
		assertEquals("rowboat", stemmer.stem("rowboats"));
	}

}
