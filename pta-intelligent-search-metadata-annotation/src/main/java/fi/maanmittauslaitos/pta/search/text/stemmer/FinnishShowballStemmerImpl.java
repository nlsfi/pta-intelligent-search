package fi.maanmittauslaitos.pta.search.text.stemmer;

import org.tartarus.snowball.ext.finnishStemmer;

public class FinnishShowballStemmerImpl implements Stemmer {
	private finnishStemmer stemmer = new finnishStemmer();
	
	@Override
	public String stem(String str) {
		stemmer.setCurrent(str);
		stemmer.stem();
		return stemmer.getCurrent();
	}

}
