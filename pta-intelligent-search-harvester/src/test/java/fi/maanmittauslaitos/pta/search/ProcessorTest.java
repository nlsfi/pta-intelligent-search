package fi.maanmittauslaitos.pta.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessorFactory;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfigurationImpl;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfigurationImpl.FieldExtractorType;
import fi.maanmittauslaitos.pta.search.text.RDFTerminologyMatcherProcessor;
import fi.maanmittauslaitos.pta.search.text.StopWordsProcessor;
import fi.maanmittauslaitos.pta.search.text.TextProcessingChain;
import fi.maanmittauslaitos.pta.search.text.TextSplitterProcessor;
import fi.maanmittauslaitos.pta.search.text.stemmer.FinnishVoikkoStemmer;
import fi.maanmittauslaitos.pta.search.text.stemmer.StemmerFactory;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class ProcessorTest {

	private static Model loadModels(String... files) throws IOException {
		Model ret = null;

		for (String file : files) {
			try (FileReader reader = new FileReader(file)) {
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

	public static void main(String[] args) throws Exception {
		DocumentProcessingConfiguration configuration = new DocumentProcessingConfiguration();
		configuration.getNamespaces().put("gmd", "http://www.isotc211.org/2005/gmd");
		configuration.getNamespaces().put("gco", "http://www.isotc211.org/2005/gco");

		TextProcessingChain abstractChain = new TextProcessingChain();
		abstractChain.getChain().add(TextSplitterProcessor.create(StemmerFactory.createFinnishStemmer(),
				Arrays.asList("aineisto", "hankinta", "alue"), true));

		StopWordsProcessor stopWordsProcessor = new StopWordsProcessor();
		stopWordsProcessor.setStopwords(Arrays.asList("ja", "tai", "on", "jonka", "mitä", "koska")); // TODO .. tämä ei riitä, ehei
		abstractChain.getChain().add(stopWordsProcessor);

		RDFTerminologyMatcherProcessor terminologyProcessor = new RDFTerminologyMatcherProcessor();
		terminologyProcessor.setModel(loadModels("src/test/resources/ysa-skos.ttl"));
		terminologyProcessor.setTerminologyLabels(Arrays.asList(SKOS.PREF_LABEL, SKOS.ALT_LABEL));
		terminologyProcessor.setStemmer(new FinnishVoikkoStemmer());

		abstractChain.getChain().add(terminologyProcessor);

		configuration.getTextProcessingChains().put("abstractProcessor", abstractChain);

		{
			FieldExtractorConfigurationImpl idExtractor = new FieldExtractorConfigurationImpl();
			idExtractor.setField("@id");
			idExtractor.setType(FieldExtractorType.FIRST_MATCHING_VALUE);
			idExtractor.setQuery("//gmd:fileIdentifier/*/text()");

			configuration.getFieldExtractors().add(idExtractor);
		}

		{
			FieldExtractorConfigurationImpl keywordExtractor = new FieldExtractorConfigurationImpl();
			keywordExtractor.setField("avainsanat");
			keywordExtractor.setType(FieldExtractorType.ALL_MATCHING_VALUES);
			keywordExtractor.setQuery("//gmd:MD_Keywords/gmd:keyword/*/text()");

			//keywordExtractor.setTextProcessorName("abstractProcessor");

			configuration.getFieldExtractors().add(keywordExtractor);
		}

		{
			FieldExtractorConfigurationImpl abstractExtractor = new FieldExtractorConfigurationImpl();
			abstractExtractor.setField("sisalto");
			abstractExtractor.setType(FieldExtractorType.ALL_MATCHING_VALUES);
			abstractExtractor.setQuery("//gmd:abstract/*/text()");

			abstractExtractor.setTextProcessorName("abstractProcessor");

			configuration.getFieldExtractors().add(abstractExtractor);
		}


		DocumentProcessor processor = DocumentProcessorFactory.getInstance().createXmlProcessor(configuration);

		Document document;
		try (FileInputStream fis = new FileInputStream("src/test/resources/metadata/1719dcdd-0f24-4406-a347-354532c97bde.xml")) {
			document = processor.processDocument(fis);
		}

		System.out.println(document.getFields().get("avainsanat"));
		System.out.println(document.getFields().get("sisalto"));
		ObjectMapper objectMapper = new ObjectMapper();

		objectMapper.writeValue(System.out, document.getFields());
	}

}
