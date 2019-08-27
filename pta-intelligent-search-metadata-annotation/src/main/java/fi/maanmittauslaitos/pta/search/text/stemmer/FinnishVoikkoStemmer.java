package fi.maanmittauslaitos.pta.search.text.stemmer;

import org.puimula.libvoikko.Voikko;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class FinnishVoikkoStemmer implements Stemmer {
	private Voikko voikko;
	private Map<String, String> preStem;
	private Map<String, String> postStem;

	public FinnishVoikkoStemmer(Map<String, String> preStem, Map<String, String> postStem) {
		voikko = new Voikko("fi");
		this.preStem = preStem;
		this.postStem = postStem;
	}

	public FinnishVoikkoStemmer() {
		voikko = new Voikko("fi");
		preStem = Collections.emptyMap();
		postStem = Collections.emptyMap();
	}
	
	@Override
	public String stem(String str) {

		String word = preStem.getOrDefault(str.toLowerCase(), str);

		String stemmed = voikko.analyze(word).stream()
				.map(a -> a.get("BASEFORM"))
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(word);

		return postStem.getOrDefault(stemmed.toLowerCase(), stemmed);
	}

}
