package fi.maanmittauslaitos.pta.search.text.stemmer;

public class StemmerFactory {
	public static Stemmer createFinnishStemmer() {
		return new FinnishVoikkoStemmer();
	}
}
