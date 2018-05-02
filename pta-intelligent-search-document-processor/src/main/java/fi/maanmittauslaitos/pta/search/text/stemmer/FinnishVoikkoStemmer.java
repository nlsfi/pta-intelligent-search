package fi.maanmittauslaitos.pta.search.text.stemmer;

import java.util.List;

import org.puimula.libvoikko.Analysis;
import org.puimula.libvoikko.Voikko;

public class FinnishVoikkoStemmer implements Stemmer {
	private Voikko voikko;
	
	public FinnishVoikkoStemmer() {
		voikko = new Voikko("fi");
	}
	
	@Override
	public String stem(String str) {
		List<Analysis> analysis = voikko.analyze(str);
		
		String stemmed = null;
		for (Analysis a : analysis) {
			String baseform = a.get("BASEFORM");
			if (baseform != null) {
				stemmed = baseform;
				break;
			}
		}
		
		if (stemmed == null) {
			stemmed = str;
		}
		
		return stemmed;
	}

}
