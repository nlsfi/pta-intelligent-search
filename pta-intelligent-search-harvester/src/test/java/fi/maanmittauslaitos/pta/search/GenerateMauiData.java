package fi.maanmittauslaitos.pta.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.SKOS;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessorFactory;
import fi.maanmittauslaitos.pta.search.documentprocessor.XPathFieldExtractorConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.XPathFieldExtractorConfiguration.FieldExtractorType;
import fi.maanmittauslaitos.pta.search.text.RDFTerminologyMatcherProcessor;
import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;

/**
 * This class is used to produce training data for maui. It uses an input directory of metadata xml and uses
 * abstract text and annotated keywords to produce an output directory of maui training data.
 * 
 * @author v2
 *
 */
public class GenerateMauiData {
	public static DocumentProcessor getProcessor() throws Exception {
		
		DocumentProcessingConfiguration configuration = new DocumentProcessingConfiguration();
		configuration.getNamespaces().put("gmd", "http://www.isotc211.org/2005/gmd");
		configuration.getNamespaces().put("gco", "http://www.isotc211.org/2005/gco");
		configuration.getNamespaces().put("srv", "http://www.isotc211.org/2005/srv");
		configuration.getNamespaces().put("gmx", "http://www.isotc211.org/2005/gmx");
		
		configuration.getNamespaces().put("xlink", "http://www.w3.org/1999/xlink");
		
		// Id
		XPathFieldExtractorConfiguration idExtractor = new XPathFieldExtractorConfiguration();
		idExtractor.setField("@id");
		idExtractor.setType(FieldExtractorType.FIRST_MATCHING_VALUE);
		idExtractor.setXpath("//gmd:fileIdentifier/*/text()");
		
		configuration.getFieldExtractors().add(idExtractor);
		
		// Abstract text
		XPathFieldExtractorConfiguration abstractExtractor = new XPathFieldExtractorConfiguration();
		abstractExtractor.setField("abstract");
		abstractExtractor.setType(FieldExtractorType.ALL_MATCHING_VALUES);
		abstractExtractor.setXpath("//gmd:abstract/*/text()");
		
		configuration.getFieldExtractors().add(abstractExtractor);
		
		// Annotated keywords
		XPathFieldExtractorConfiguration annotatedKeywordExtractor = new XPathFieldExtractorConfiguration();
		annotatedKeywordExtractor.setField("annotated_keywords_uri");
		annotatedKeywordExtractor.setType(FieldExtractorType.ALL_MATCHING_VALUES);
		annotatedKeywordExtractor.setXpath("//gmd:descriptiveKeywords/*/gmd:keyword/gmx:Anchor/@xlink:href");
		
		//annotatedKeywordExtractor.setTextProcessorName("isInOntologyFilterProcessor");
		
		configuration.getFieldExtractors().add(annotatedKeywordExtractor);
		
		return new DocumentProcessorFactory().createProcessor(configuration);
		
	}
	
	private static RDFTerminologyMatcherProcessor createTerminologyMatcher(Model model) throws IOException {
		RDFTerminologyMatcherProcessor terminologyProcessor = new RDFTerminologyMatcherProcessor();
		terminologyProcessor.setModel(model);
		terminologyProcessor.setTerminologyLabels(Arrays.asList(SKOS.PREF_LABEL));
		terminologyProcessor.setStemmer(new Stemmer() {
			
			@Override
			public String stem(String str) {
				return str;
			}
		});
		terminologyProcessor.setLanguage("fi");
		return terminologyProcessor;
	}

	
	public static void main(String[] args) throws Exception {
		File inputDir = new File("input/");
		File outputDir = new File("output/");
		List<File> files = Arrays.asList(inputDir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");

			}
		}));
		
		if (outputDir.exists() && !outputDir.isDirectory()) {
			throw new IOException("Output directory "+outputDir+" exists but is not a directory");
		}
		
		if (!outputDir.exists()) {
			if (!outputDir.mkdir()) {
				throw new IOException("Could not create output directory "+outputDir);
			}
		}
		
		DocumentProcessor processor = getProcessor();
		

		HarvesterConfig config = new HarvesterConfig();
		Model model = config.getTerminologyModel();
		
		RDFTerminologyMatcherProcessor proc = createTerminologyMatcher(model);
		Map<String, String> reverseDict = proc.getReverseDict();
		
		for (File f : files) {
		
			try (FileInputStream fis = new FileInputStream(f)) {
				
				Document doc = processor.processDocument(fis);
				
				//System.out.println("--------------------------------------------------------------");
				String id = doc.getValue("@id", String.class);
				//System.out.println(id);
				
				String text = join(doc.getListValue("abstract", String.class), " ");
				
				File outputTxt  = new File(outputDir, id+".txt");
				File outputSubj = new File(outputDir, id+".subj");
				File outputKey  = new File(outputDir, id+".key");
				
				try (
						FileWriter fwTxt  = new FileWriter(outputTxt);
						FileWriter fwSubj = new FileWriter(outputSubj);
						FileWriter fwKey  = new FileWriter(outputKey)) {
					IOUtils.write(text, fwTxt);
				
					for (String keyword : doc.getListValue("annotated_keywords_uri", String.class)) {
						
						String label = reverseDict.get(keyword);
						fwSubj.write(keyword+"\t"+label+"\n");
						fwKey.write(label+"\n");
					}
					
				}
				/*
				System.out.println(text);
				//System.out.println(doc.getFields().get("abstract"));
				for (String keyword : doc.getListValue("annotated_keywords_uri", String.class)) {
					String label = reverseDict.get(keyword);
					System.out.println(keyword+"\t"+label);
				}
				*/
				
			}
		}
		
	}

	private static String join(List<String> listValue, String separator) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < listValue.size(); i++) {
			if (i > 0) {
				buf.append(separator);
			}
			buf.append(listValue.get(i));
		}
		return buf.toString();
	}
}
