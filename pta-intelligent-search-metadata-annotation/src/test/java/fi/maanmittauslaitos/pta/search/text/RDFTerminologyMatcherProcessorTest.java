package fi.maanmittauslaitos.pta.search.text;

import static org.junit.Assert.*;

import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.Test;

import fi.maanmittauslaitos.pta.search.text.stemmer.FinnishVoikkoStemmer;

public class RDFTerminologyMatcherProcessorTest {

	@Test
	public void testKissaNoStemmer() throws Exception {
		RDFTerminologyMatcherProcessor processor = new RDFTerminologyMatcherProcessor();
		
		FileReader reader = new FileReader("src/test/resources/kissa.ttl");
		Model model = Rio.parse(reader, "", RDFFormat.TURTLE);
		
		processor.setModel(model);
		processor.setTerminologyLabels(Arrays.asList(SKOS.PREF_LABEL, SKOS.ALT_LABEL));
		
		List<String> result = processor.process(Arrays.asList("kissa"));
		
		assertEquals(2, result.size());
		assertTrue(result.contains("http://www.yso.fi/onto/ysa/Y96241"));
		assertTrue(result.contains("http://www.yso.fi/onto/ysa/Y96999"));
	}
	
	@Test
	public void testKissatNoStemmerNoMatch() throws Exception {
		RDFTerminologyMatcherProcessor processor = new RDFTerminologyMatcherProcessor();
		
		FileReader reader = new FileReader("src/test/resources/kissa.ttl");
		Model model = Rio.parse(reader, "", RDFFormat.TURTLE);
		
		processor.setModel(model);
		processor.setTerminologyLabels(Arrays.asList(SKOS.PREF_LABEL, SKOS.ALT_LABEL));
		
		List<String> result = processor.process(Arrays.asList("kissat"));
		
		assertEquals(1, result.size());
		assertTrue(result.contains("http://www.yso.fi/onto/ysa/Y96241"));
	}

	@Test
	public void testKissatWithStemmer() throws Exception {
		RDFTerminologyMatcherProcessor processor = new RDFTerminologyMatcherProcessor();
		
		FileReader reader = new FileReader("src/test/resources/kissa.ttl");
		Model model = Rio.parse(reader, "", RDFFormat.TURTLE);
		
		processor.setModel(model);
		processor.setTerminologyLabels(Arrays.asList(SKOS.PREF_LABEL, SKOS.ALT_LABEL));
		processor.setStemmer(new FinnishVoikkoStemmer());
		
		List<String> result = processor.process(Arrays.asList("kissat"));
		
		assertEquals(2, result.size());
		assertTrue(result.contains("http://www.yso.fi/onto/ysa/Y96241"));
		assertTrue(result.contains("http://www.yso.fi/onto/ysa/Y96999"));
	}

	
	@Test
	public void testVillagesNoStemmerAllLanguages() throws Exception {
		RDFTerminologyMatcherProcessor processor = new RDFTerminologyMatcherProcessor();
		
		FileReader reader = new FileReader("src/test/resources/koko-villages.ttl");
		Model model = Rio.parse(reader, "", RDFFormat.TURTLE);
		
		processor.setModel(model);
		processor.setTerminologyLabels(Arrays.asList(SKOS.PREF_LABEL, SKOS.ALT_LABEL));
		
		List<String> result;
		
		result = processor.process(Arrays.asList("villages"));
		
		assertEquals(1, result.size());
		assertTrue(result.contains("http://www.yso.fi/onto/koko/p32440"));
		
		result = processor.process(Arrays.asList("kylät"));
		
		assertEquals(1, result.size());
		assertTrue(result.contains("http://www.yso.fi/onto/koko/p32440"));
	}
	
	@Test
	public void testVillagesNoStemmerOnlyFinnish() throws Exception {
		RDFTerminologyMatcherProcessor processor = new RDFTerminologyMatcherProcessor();
		
		FileReader reader = new FileReader("src/test/resources/koko-villages.ttl");
		Model model = Rio.parse(reader, "", RDFFormat.TURTLE);
		
		processor.setModel(model);
		processor.setTerminologyLabels(Arrays.asList(SKOS.PREF_LABEL, SKOS.ALT_LABEL));
		processor.setLanguage("fi");
		
		List<String> result;
		
		result = processor.process(Arrays.asList("villages"));
		
		assertEquals(0, result.size());
		
		result = processor.process(Arrays.asList("kylät"));
		
		assertEquals(1, result.size());
		assertTrue(result.contains("http://www.yso.fi/onto/koko/p32440"));
	}
	
}
