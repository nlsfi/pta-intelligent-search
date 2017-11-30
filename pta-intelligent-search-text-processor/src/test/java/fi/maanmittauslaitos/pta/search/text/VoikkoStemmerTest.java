package fi.maanmittauslaitos.pta.search.text;

import static org.junit.Assert.*;

import org.junit.Test;

import fi.maanmittauslaitos.pta.search.text.stemmer.FinnishVoikkoStemmer;

public class VoikkoStemmerTest {


	@Test
	public void test() {
		FinnishVoikkoStemmer stemmer = new FinnishVoikkoStemmer();
		
		assertEquals("kissa", stemmer.stem("kissa"));
		assertEquals("kissa", stemmer.stem("kissoja"));
		assertEquals("kissa", stemmer.stem("kissoista"));
		assertEquals("kissa", stemmer.stem("kissoihin"));
	}

}
