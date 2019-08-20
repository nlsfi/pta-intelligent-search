package fi.maanmittauslaitos.pta.search.text;

import com.entopix.maui.filters.MauiFilter;
import com.entopix.maui.filters.MauiFilter.MauiFilterException;
import com.entopix.maui.stemmers.Stemmer;
import com.entopix.maui.stopwords.Stopwords;
import com.entopix.maui.util.Topic;
import com.entopix.maui.vocab.Vocabulary;
import org.apache.log4j.Logger;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

public class MauiTextProcessor implements TextProcessor {
	private static Logger logger = Logger.getLogger(MauiTextProcessor.class);
	
	// Injected by spring
	private String modelResource;
	private String vocabularyName;
	private String vocabularyFormat;
	private String language;
	
	private Stopwords mauiStopWords;
	private Stemmer mauiStemmer;
	
	private int topicsPerDocument = 10;
	private double cutOffTopicProbability = 0.0; // Default from MauiTopicExtractor
	
	// Set up in init()
	private MauiFilter model;
	
	public void setTopicsPerDocument(int topicsPerDocument) {
		this.topicsPerDocument = topicsPerDocument;
	}
	
	public int getTopicsPerDocument() {
		return topicsPerDocument;
	}
	
	public void setCutOffTopicProbability(double cutOffTopicProbability) {
		this.cutOffTopicProbability = cutOffTopicProbability;
	}
	
	public double getCutOffTopicProbability() {
		return cutOffTopicProbability;
	}
	
	public void setModelResource(String modelResource) {
		this.modelResource = modelResource;
	}
	
	public String getModelResource() {
		return modelResource;
	}
	
	public void setMauiStemmer(Stemmer mauiStemmer) {
		this.mauiStemmer = mauiStemmer;
	}
	
	public Stemmer getMauiStemmer() {
		return mauiStemmer;
	}
	
	public void setVocabularyName(String vocabularyName) {
		this.vocabularyName = vocabularyName;
	}
	
	public String getVocabularyName() {
		return vocabularyName;
	}
	
	public void setVocabularyFormat(String vocabularyFormat) {
		this.vocabularyFormat = vocabularyFormat;
	}
	
	public String getVocabularyFormat() {
		return vocabularyFormat;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public void setMauiStopWords(Stopwords mauiStopWords) {
		this.mauiStopWords = mauiStopWords;
	}
	
	public Stopwords getMauiStopWords() {
		return mauiStopWords;
	}
	
	public void init() {
		logger.info("Initializing");
		
		InputStream is = MauiTextProcessor.class.getResourceAsStream(getModelResource());
		try (ObjectInputStream in = new ObjectInputStream(is)) {
			this.model = (MauiFilter) in.readObject();
			
		} catch (IOException e) {
			logger.error("Error while loading extraction model from classpath " + getModelResource() + "!\n", e);
			throw new RuntimeException();
		} catch (ClassNotFoundException e) {
			logger.error("Mismatch of the class in " + getModelResource() + "!\n", e);
			throw new RuntimeException();
		}
		
		this.model.setVocabularyName(getVocabularyName());
		this.model.setVocabularyFormat(getVocabularyFormat());
		this.model.setDocumentLanguage(getLanguage());
		this.model.setStemmer(getMauiStemmer());
		
		
		Vocabulary vocabulary = new Vocabulary();
		vocabulary.setStemmer(getMauiStemmer());
		if (!getVocabularyName().equals("lcsh")) {
			vocabulary.setStopwords(getMauiStopWords());
		}
		vocabulary.setLanguage(getLanguage());
		vocabulary.setSerialize(false); // Not sure what this really is
		vocabulary.initializeVocabulary(getVocabularyName(), getVocabularyFormat());

		this.model.setVocabulary(vocabulary);
		
	}
	
	@Override
	public synchronized List<String> process(List<String> str) {
		List<String> ret = new ArrayList<>();
		
		for (String tmp : str) {
			ret.addAll(process(tmp));
		}
		
		return ret;
	}
	
	public List<String> process(String str) {
		if (logger.isTraceEnabled()) {
			logger.trace("Process input: "+str);
		}
		
		
		FastVector atts = new FastVector(3);
		atts.addElement(new Attribute("filename", (FastVector) null));
		atts.addElement(new Attribute("doc", (FastVector) null));
		atts.addElement(new Attribute("keyphrases", (FastVector) null));
		Instances data = new Instances("keyphrase_training_data", atts, 0);

		


		double[] newInst = new double[3];

		newInst[0] = Instance.missingValue();
		//newInst[0] = data.attribute(0).addStringValue(document.getFileName());

		// Adding the text of the document to the instance
		newInst[1] = data.attribute(1).addStringValue(str);
				
		newInst[2] = Instance.missingValue();

		data.add(new Instance(1.0, newInst));

		try {
			this.model.input(data.instance(0));
		} catch(MauiFilterException mfe) {
			throw new IllegalArgumentException("Unable to process data", mfe);
		}

		data = data.stringFreeStructure();
		//logger.info("-- Processing document: " + document.getFileName());


		//Instance[] topRankedInstances = new Instance[topicsPerDocument];
		
		//MauiTopics documentTopics = new MauiTopics(document.getFilePath());

		//documentTopics.setPossibleCorrect(document.getTopicsString().split("\n").length);

		Instance inst;
		int index = 0;
		double probability;
		Topic topic;
		String title, id;

		logger.trace("-- Keyphrases and feature values:");

		List<String> ret = new ArrayList<>();
		
		// Iterating over all extracted topic instances
		while ((inst = this.model.output()) != null) {
			probability = inst.value(this.model.getProbabilityIndex());
			if (index < getTopicsPerDocument()) {
				if (probability > getCutOffTopicProbability()) {
					//topRankedInstances[index] = inst;
					title = inst.stringValue(this.model.getOutputFormIndex());
					id = "1"; // topRankedInstances[index].
					//stringValue(mauiFilter.getOutputFormIndex() + 1); // TODO: Check
					topic = new Topic(title,  id,  probability);

					if ((int)inst.value(inst.numAttributes() - 1) == 1) {
						topic.setCorrectness(true);
					} else {
						topic.setCorrectness(false);
					}
					
					if (logger.isTraceEnabled()) {
						logger.trace("Topic " + title + " " + id + " " + probability + " > " + topic.isCorrect());
					}
					ret.add(inst.stringValue(0));
					index++;
				}
			}
		}
		
		return ret;
	}

}
