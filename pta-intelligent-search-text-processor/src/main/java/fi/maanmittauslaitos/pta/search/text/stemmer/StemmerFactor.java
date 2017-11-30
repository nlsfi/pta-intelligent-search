package fi.maanmittauslaitos.pta.search.text.stemmer;

public class StemmerFactor {
	public static Stemmer createStemmer() {
		return new FinnishVoikkoStemmer();
	}
}
