package fi.maanmittauslaitos.pta.search.api.region;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;

public class StemmingTextFileBackedRegionNameDetectorImpl implements RegionNameDetector {

	// Injected
	private Stemmer stemmer;
	private String resourceName;
	
	// Initialized lazily
	private Set<String> stemmedRegionNames;
	
	public void setStemmer(Stemmer stemmer) {
		this.stemmer = stemmer;
	}
	
	public Stemmer getStemmer() {
		return stemmer;
	}
	
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	
	public String getResourceName() {
		return resourceName;
	}
	
	public void init() throws IOException {
		InputStream is = StemmingTextFileBackedRegionNameDetectorImpl.class.getResourceAsStream(getResourceName());
		if (is == null) {
			throw new IOException("No such resource: "+getResourceName());
		}
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			
			Set<String> tmp = new HashSet<>();
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim().toLowerCase();
				line = getStemmer().stem(line);
				
				tmp.add(line);
			}
			
			this.stemmedRegionNames = tmp;
			
		} finally {
			is.close();
		}
	}
	
	@Override
	public boolean containsRegionalName(List<String> query) {
		for (String s : query) {
			s = s.toLowerCase().trim();
			s = getStemmer().stem(s);
			
			if (stemmedRegionNames.contains(s)) {
				return true;
			}
		}

		return false;
	}

}
