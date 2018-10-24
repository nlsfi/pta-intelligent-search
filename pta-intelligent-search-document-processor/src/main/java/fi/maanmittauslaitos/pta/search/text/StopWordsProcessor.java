package fi.maanmittauslaitos.pta.search.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StopWordsProcessor implements TextProcessor {

	// Poistettavat sanat lowercasena
	private Set<String> stopwords = new HashSet<>();
	
	public void setStopwords(Collection<String> stopwords) {
		
		this.stopwords = new HashSet<>();
		for (String word : stopwords) {
			this.stopwords.add(word.toLowerCase());
		}
	}
	
	public Set<String> getStopwords() {
		return stopwords;
	}
	
	@Override
	public List<String> process(List<String> input) {
		List<String> ret = new ArrayList<>();
		for (String str : input) {
			if (!stopwords.contains(str.toLowerCase())) {
				ret.add(str);
			}
		}
		return ret;
	}

	public void loadWords(InputStream is) throws IOException
	{
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
			List<String> stopWords = new ArrayList<>();
			String line;
			while ((line = br.readLine()) != null) {
				String tmp = line.toLowerCase().trim();
				if (tmp.length() > 0) {
					stopWords.add(tmp);
				}
			}
			this.setStopwords(stopWords);
		} finally {
			is.close();
		}
	}

}
