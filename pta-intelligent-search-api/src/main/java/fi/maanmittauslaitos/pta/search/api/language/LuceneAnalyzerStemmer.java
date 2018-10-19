package fi.maanmittauslaitos.pta.search.api.language;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.QueryParser;

import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;

public class LuceneAnalyzerStemmer implements Stemmer {

	private QueryParser parser;
	
	public LuceneAnalyzerStemmer(Analyzer analyzer) {
		parser = new QueryParser("", analyzer);
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
