package fi.maanmittauslaitos.pta.search.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;

import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;

public class WordCombinationProcessor implements TextProcessor {
	private static Logger logger = Logger.getLogger(WordCombinationProcessor.class);
	
	// Setters, getters
	private Model model;
	private List<IRI> terminologyLabels;
	private Stemmer stemmer;
	private String language;
	
	// Lazy initialization
	private Map<String, String> dict;
	private int maxCombinationLengthWords;

	
	public void setModel(Model model) {
		this.model = model;
	}
	
	public Model getModel() {
		return model;
	}
	
	public void setTerminologyLabels(List<IRI> terminologyLabels) {
		this.terminologyLabels = terminologyLabels;
	}
	
	public List<IRI> getTerminologyLabels() {
		return terminologyLabels;
	}
	
	public void setStemmer(Stemmer stemmer) {
		this.stemmer = stemmer;
	}
	
	public Stemmer getStemmer() {
		return stemmer;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public String getLanguage() {
		return language;
	}

	private Map<String, String> getDict() {
		if (dict != null) {
			return dict;
		}
		
		maxCombinationLengthWords = calculateMaximumNumberOfWordsInModel();
		dict = createDict();
		logger.debug("Dict contained "+dict.size()+" word combinations, with a maximum length of "+maxCombinationLengthWords+" words");
		
		return dict;
	}
	
	private int calculateMaximumNumberOfWordsInModel() {
		int max = 1;
		for (Resource r : getModel().subjects()) {
			for (IRI resource : getTerminologyLabels()) {
				for (Statement statement : model.filter(r, resource, null)) {
					boolean shouldAdd = true;
					
					if (getLanguage() != null && statement.getObject() instanceof Literal) {
						Literal literal = (Literal)statement.getObject();
						if (literal.getLanguage().isPresent()) {
							String lang = literal.getLanguage().get();
							if (!getLanguage().equals(lang)) {
								shouldAdd = false;
							}
						}
					}
					
					if (shouldAdd) {
						String value = statement.getObject().stringValue();
						
						String [] tmp = splitAndStem(value);
						
						if (tmp.length > max) {
							max = tmp.length;
						}
					}
				}
			}
		}
		
		return max;
	}

	private String[] splitAndStem(String value) {		
		String [] parts = value.split("\\s+");
		
		if (getStemmer() != null) {
			for (int i = 0; i< parts.length; i++) {
				parts[i] = getStemmer().stem(parts[i]);
			}
		}
		
		return parts;
	}

	private Map<String, String> createDict() {
		Map<String, String> ret = new HashMap<>();
		
		for (Resource r : getModel().subjects()) {			
			for (IRI resource : getTerminologyLabels()) {
				for (Statement statement : model.filter(r, resource, null)) {
					boolean shouldAdd = true;
					
					if (getLanguage() != null && statement.getObject() instanceof Literal) {
						Literal literal = (Literal)statement.getObject();
						if (literal.getLanguage().isPresent()) {
							String lang = literal.getLanguage().get();
							if (!getLanguage().equals(lang)) {
								shouldAdd = false;
							}
						}
					}
					
					if (shouldAdd) {
						String value = statement.getObject().stringValue();
						
						String[] tmp = splitAndStem(value);
						
						if (tmp.length > 1) {
							String key = createKey(tmp);
							ret.put(key, value);
						}
					}
				}
			}
		}
		return ret;
	}

	private String createKey(String...tmp) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < tmp.length; i++) {
			if (i > 0) {
				buf.append("|");
			}
			buf.append(tmp[i]);
		}
		return buf.toString();
	}

	@Override
	public List<String> process(String str) {
		// TODO Auto-generated method stub
		return null;
	}

	// TODO: this must be moved to be part of the interface
	public List<String> process(List<String> str) {
		List<String> ret = new ArrayList<>();
		
		Map<String, String> dict = getDict();
		
		for (int i = 0; i < str.size(); i++) {
			
			int localMaxLength = str.size() - i;
			if (localMaxLength > maxCombinationLengthWords) {
				localMaxLength = maxCombinationLengthWords;
			}
			
			if (localMaxLength > 1) {
				// Start at maximum length since we want to match the longest possible combination
				
				String foundCombination = null;
				int foundCombinationLength = -1;
				
				for (int len = localMaxLength; len > 1 && foundCombination == null; len--) {
					// formulate a possible combination word of length 'len' starting at index 'i'
					// (stemming each word), but store as an array
					
					String[] possibleWordCombination = produceWordCombinationArray(str, i, len);
					
					// Get the key for this word combination
					String key = createKey(possibleWordCombination);
					
					String value = dict.get(key);
					
					if (value != null) {
						foundCombination = value;
						foundCombinationLength = len;
					}
					
				}
				
				if (foundCombination != null) {
					i += foundCombinationLength-1;
					ret.add(foundCombination);
				} else {
					ret.add(str.get(i));
				}
				
			} else {
				ret.add(str.get(i));
			}
		}
		
		return ret;
	}

	private String[] produceWordCombinationArray(List<String> source, int startidx, int len) {
		String[] possibleWordCombination = new String[len];
		for (int z = 0; z < len; z++) {
			String tmp = source.get(startidx+z);
			if (getStemmer() != null) {
				tmp = getStemmer().stem(tmp);
			}
			possibleWordCombination[z] = tmp;
		}
		return possibleWordCombination;
	}

	
}
