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

import fi.maanmittauslaitos.pta.search.text.stemmer.FinnishShowballStemmerImpl;

public class RDFTerminologyMatcherProcessorTest {

	@Test
	public void testKissaNoStemmer() throws Exception {
		RDFTerminologyMatcherProcessor processor = new RDFTerminologyMatcherProcessor();
		
		FileReader reader = new FileReader("src/test/resources/kissa.ttl");
		Model model = Rio.parse(reader, "", RDFFormat.TURTLE);
		
		processor.setModel(model);
		processor.setTerminologyLabels(Arrays.asList(SKOS.PREF_LABEL, SKOS.ALT_LABEL));
		
		List<String> result = processor.process("kissa");
		
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
		
		List<String> result = processor.process("kissat");
		
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
		processor.setStemmer(new FinnishShowballStemmerImpl());
		
		List<String> result = processor.process("kissat");
		
		assertEquals(2, result.size());
		assertTrue(result.contains("http://www.yso.fi/onto/ysa/Y96241"));
		assertTrue(result.contains("http://www.yso.fi/onto/ysa/Y96999"));
	}

}
