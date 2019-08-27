package fi.maanmittauslaitos.pta.search.text.stemmer;

import java.util.Map;

public class StemmerFactory {
	public static Stemmer createFinnishStemmer() {
		return new FinnishVoikkoStemmer();
	}

	public static Stemmer createFinnishStemmer(Map<String, String> preStem, Map<String, String> postStem) {
		return new FinnishVoikkoStemmer(preStem, postStem);
	}
}
