package fi.maanmittauslaitos.pta.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import fi.maanmittauslaitos.pta.search.csw.CSWHarvesterSource;
import fi.maanmittauslaitos.pta.search.index.DocumentSink;
import fi.maanmittauslaitos.pta.search.index.ElasticsearchDocumentSink;
import fi.maanmittauslaitos.pta.search.text.RDFTerminologyMatcherProcessor;
import fi.maanmittauslaitos.pta.search.text.RegexProcessor;
import fi.maanmittauslaitos.pta.search.text.StopWordsProcessor;
import fi.maanmittauslaitos.pta.search.text.TextProcessingChain;
import fi.maanmittauslaitos.pta.search.text.TextSplitterProcessor;
import fi.maanmittauslaitos.pta.search.text.stemmer.StemmerFactor;
import fi.maanmittauslaitos.pta.search.xpath.FieldExtractorConfiguration;
import fi.maanmittauslaitos.pta.search.xpath.XPathExtractionConfiguration;
import fi.maanmittauslaitos.pta.search.xpath.XPathProcessor;
import fi.maanmittauslaitos.pta.search.xpath.XPathProcessorFactory;
import fi.maanmittauslaitos.pta.search.xpath.FieldExtractorConfiguration.FieldExtractorType;

public class HarvesterConfig {
	public HarvesterSource getCSWSource() {
		HarvesterSource source = new CSWHarvesterSource();
		source.setBatchSize(10);
		source.setOnlineResource("http://paikkatietohakemisto.fi/geonetwork/srv/en/csw");
		//source.setOnlineResource("http://demo.paikkatietohakemisto.fi/geonetwork/srv/en/csw");
		
		return source;
	}
	
	
	public XPathProcessor getCSWRecordProcessor() throws ParserConfigurationException, IOException {
		XPathExtractionConfiguration configuration = new XPathExtractionConfiguration();
		configuration.getNamespaces().put("gmd", "http://www.isotc211.org/2005/gmd");
		configuration.getNamespaces().put("gco", "http://www.isotc211.org/2005/gco");
		configuration.getNamespaces().put("srv", "http://www.isotc211.org/2005/srv");
		
		TextProcessingChain abstractChain = new TextProcessingChain();
		abstractChain.getChain().add(new TextSplitterProcessor());
		
		StopWordsProcessor stopWordsProcessor = new StopWordsProcessor();
		try (InputStreamReader isr = new InputStreamReader(HarvesterConfig.class.getResourceAsStream("/stopwords-fi.txt"))) {
			List<String> stopWords = new ArrayList<>();
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				String tmp = line.toLowerCase().trim();
				if (tmp.length() > 0) {
					stopWords.add(tmp);
				}
			}
			stopWordsProcessor.setStopwords(stopWords);
		}
		abstractChain.getChain().add(stopWordsProcessor);
		
		RDFTerminologyMatcherProcessor terminologyProcessor = createTerminologyMatcher();
		
		abstractChain.getChain().add(terminologyProcessor);
		
		configuration.getTextProcessingChains().put("abstractProcessor", abstractChain);
		
		

		TextProcessingChain keywordChain = new TextProcessingChain();
		RegexProcessor whitespaceRemoval = new RegexProcessor();
		whitespaceRemoval.setPattern(Pattern.compile("^\\s*$"));
		whitespaceRemoval.setIncludeMatches(false);
		
		keywordChain.getChain().add(new TextSplitterProcessor());
		keywordChain.getChain().add(whitespaceRemoval);
		keywordChain.getChain().add(terminologyProcessor);
		
		
		configuration.getTextProcessingChains().put("keywordProcessor", keywordChain);
		
		{
			FieldExtractorConfiguration idExtractor = new FieldExtractorConfiguration();
			idExtractor.setField("@id");
			idExtractor.setType(FieldExtractorType.FIRST_MATCHING_VALUE);
			idExtractor.setXpath("//gmd:fileIdentifier/*/text()");
			
			configuration.getFieldExtractors().add(idExtractor);
		}
		
		{
			FieldExtractorConfiguration titleExtractor = new FieldExtractorConfiguration();
			titleExtractor.setField("title");
			titleExtractor.setType(FieldExtractorType.FIRST_MATCHING_VALUE);
			titleExtractor.setXpath("//gmd:identificationInfo/*/gmd:citation/*/gmd:title/*/text()");
			
			configuration.getFieldExtractors().add(titleExtractor);
		}
		
		{
			FieldExtractorConfiguration keywordExtractor = new FieldExtractorConfiguration();
			keywordExtractor.setField("avainsanat_uri");
			keywordExtractor.setType(FieldExtractorType.ALL_MATCHING_VALUES);
			keywordExtractor.setXpath("//gmd:MD_Keywords/gmd:keyword/*/text()");
			
			keywordExtractor.setTextProcessorName("keywordProcessor");
			
			configuration.getFieldExtractors().add(keywordExtractor);
		}
		
		{
			FieldExtractorConfiguration abstractExtractor = new FieldExtractorConfiguration();
			abstractExtractor.setField("abstract_uri");
			abstractExtractor.setType(FieldExtractorType.ALL_MATCHING_VALUES);
			abstractExtractor.setXpath("//gmd:abstract/*/text()");
			
			abstractExtractor.setTextProcessorName("abstractProcessor");
			
			configuration.getFieldExtractors().add(abstractExtractor);
		}
		
		{
			FieldExtractorConfiguration abstractAsTextExtractor = new FieldExtractorConfiguration();
			abstractAsTextExtractor.setField("abstract");
			abstractAsTextExtractor.setType(FieldExtractorType.ALL_MATCHING_VALUES);
			abstractAsTextExtractor.setXpath("//gmd:abstract/*/text()");
			
			configuration.getFieldExtractors().add(abstractAsTextExtractor);
		}
		
		{
			FieldExtractorConfiguration onlineResourceExtractor = new FieldExtractorConfiguration();
			onlineResourceExtractor.setField("onlineResource");
			onlineResourceExtractor.setType(FieldExtractorType.ALL_MATCHING_VALUES);
			// Select linkage URL in onLine transferoptions where protocol contains "wfs"
			onlineResourceExtractor.setXpath("//gmd:distributionInfo/*/gmd:transferOptions/*/gmd:onLine/*[contains(translate(gmd:protocol/*/text(),\"WFS\",\"wfs\"),\"wfs\")]/gmd:linkage/gmd:URL/text()");
			
			configuration.getFieldExtractors().add(onlineResourceExtractor);
		}
		
		{
			FieldExtractorConfiguration isServiceExtractor = new FieldExtractorConfiguration();
			isServiceExtractor.setField("isService");
			isServiceExtractor.setType(FieldExtractorType.TRUE_IF_MATCHES_OTHERWISE_FALSE);
			isServiceExtractor.setXpath("//gmd:identificationInfo/srv:SV_ServiceIdentification");
			
			configuration.getFieldExtractors().add(isServiceExtractor);
		}
		
		{
			FieldExtractorConfiguration isDatasetExtractor = new FieldExtractorConfiguration();
			isDatasetExtractor.setField("isDataset");
			isDatasetExtractor.setType(FieldExtractorType.TRUE_IF_MATCHES_OTHERWISE_FALSE);
			isDatasetExtractor.setXpath("//gmd:identificationInfo/gmd:MD_DataIdentification");
			
			configuration.getFieldExtractors().add(isDatasetExtractor);
			
		}
		
		return new XPathProcessorFactory().createProcessor(configuration);
	}


	private RDFTerminologyMatcherProcessor createTerminologyMatcher() throws IOException {
		RDFTerminologyMatcherProcessor terminologyProcessor = new RDFTerminologyMatcherProcessor();
		terminologyProcessor.setModel(getTerminologyModel());
		terminologyProcessor.setTerminologyLabels(Arrays.asList(SKOS.PREF_LABEL, SKOS.ALT_LABEL));
		terminologyProcessor.setStemmer(StemmerFactor.createStemmer());
		terminologyProcessor.setLanguage("fi");
		return terminologyProcessor;
	}

	public DocumentSink getDocumentSink() {
		ElasticsearchDocumentSink ret = new ElasticsearchDocumentSink();
		ret.setHostname("localhost");
		ret.setPort(9200);
		ret.setProtocol("http");
		ret.setIndex("catalog");
		ret.setType("doc");
		
		ret.setIdField("@id");
		
		return ret;
	}
	

	private Model getTerminologyModel() throws IOException {
		return loadModels("/koko-skos.ttl.gz");
	}

	private static Model loadModels(String...files) throws IOException {
		Model ret = null;
		
		for (String file : files) {
			try (Reader reader = new InputStreamReader(new GZIPInputStream(HarvesterConfig.class.getResourceAsStream(file)))) {
				Model model = Rio.parse(reader, "", RDFFormat.TURTLE);
				
				if (ret == null) {
					ret = model;
				} else {
					ret.addAll(model);
				}
			}
		}
		
		return ret;
	}
	
}
