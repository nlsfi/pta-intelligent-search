package fi.maanmittauslaitos.pta.search.text.stemmer;

import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;

public class SwedishSnowballStemmer implements Stemmer {

	private QueryParser parser;
	
	public SwedishSnowballStemmer() {
		SnowballAnalyzer analyzer = new SnowballAnalyzer(Version.LUCENE_30, "Swedish");
		parser = new QueryParser(Version.LUCENE_30, "", analyzer);
	}
	
	@Override
	public String stem(String str) {
		try {
			return parser.parse(str).toString("");
		} catch(Exception e) {
			return null;
		}
	}

}
