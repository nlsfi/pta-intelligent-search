package fi.maanmittauslaitos.pta.search;

import org.tartarus.snowball.ext.finnishStemmer;

public class StemTest {
	public static void main(String[] args) {
		finnishStemmer stemmer = new finnishStemmer();
		
		stemmer.setCurrent("kissa");
		boolean b = stemmer.stem();
		System.out.println(b);
		
		String stemmed = stemmer.getCurrent();
		System.out.println(stemmed);
	}
}
