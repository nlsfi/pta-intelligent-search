package fi.maanmittauslaitos.pta.search.text;

import static org.junit.Assert.*;

import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.Before;
import org.junit.Test;

import fi.maanmittauslaitos.pta.search.text.stemmer.StemmerFactor;

public class WordCombinationProcessorTest {
	private WordCombinationProcessor processor;
	
	@Before
	public void setup() throws Exception {
		processor = new WordCombinationProcessor();
		
		FileReader reader = new FileReader("src/test/resources/kissa.ttl");
		Model model = Rio.parse(reader, "", RDFFormat.TURTLE);
		
		processor.setModel(model);
		processor.setTerminologyLabels(Arrays.asList(SKOS.PREF_LABEL, SKOS.ALT_LABEL));
		processor.setStemmer(StemmerFactor.createStemmer());
	}
	
	@Test
	public void testWordCombinationDetected() throws Exception {
		
		List<String> result = processor.process(Arrays.asList("yhdistelmä", "kissa"));
		
		assertEquals(1, result.size());
		assertTrue(result.contains("yhdistelmä kissa"));
	}
	

	@Test
	public void testWordCombinationDetectedMidSentence() throws Exception {
		
		List<String> result = processor.process(Arrays.asList("tämä", "yhdistelmä", "kissa", "kiipeää"));
		
		assertEquals(3, result.size());
		assertEquals("tämä", result.get(0));
		assertEquals("yhdistelmä kissa", result.get(1));
		assertEquals("kiipeää", result.get(2));
	}
	

	@Test
	public void testWordCombinationDetectedStartOfSentence() throws Exception {
		
		List<String> result = processor.process(Arrays.asList("yhdistelmä", "kissa", "kiipeää", "puuhun"));
		
		assertEquals(3, result.size());
		assertEquals("yhdistelmä kissa", result.get(0));
		assertEquals("kiipeää", result.get(1));
		assertEquals("puuhun", result.get(2));
	}
	
	@Test
	public void testWordCombinationDetectedEndOfSentence() throws Exception {
		
		List<String> result = processor.process(Arrays.asList("puuhun", "kiipeää", "yhdistelmä", "kissa"));
		
		assertEquals(3, result.size());
		assertEquals("puuhun", result.get(0));
		assertEquals("kiipeää", result.get(1));
		assertEquals("yhdistelmä kissa", result.get(2));
	}
	
	@Test
	public void testWordCombinationDetectedFirstWordConjugated() throws Exception {
		
		List<String> result = processor.process(Arrays.asList("yhdistelmän", "kissa"));
		
		assertEquals(1, result.size());
		assertTrue(result.contains("yhdistelmä kissa"));
	}
	

	@Test
	public void testWordCombinationDetectedSecondWordConjugated() throws Exception {
		
		List<String> result = processor.process(Arrays.asList("yhdistelmä", "kissat"));
		
		assertEquals(1, result.size());
		assertTrue(result.contains("yhdistelmä kissa"));
	}
	
	
	@Test
	public void testWordCombinationDetectedBothWordsConjugated() throws Exception {
		
		List<String> result = processor.process(Arrays.asList("yhdistelmän", "kissat"));
		
		assertEquals(1, result.size());
		assertTrue(result.contains("yhdistelmä kissa"));
	}

	
	@Test
	public void testNoCombinationDetected() throws Exception {
		
		List<String> result = processor.process(Arrays.asList("kissa", "yhdistelmä"));
		
		assertEquals(2, result.size());
		assertEquals("kissa", result.get(0));
		assertEquals("yhdistelmä", result.get(1));
	}
	

	@Test
	public void testNoCombinationDetectedStartWithSameWord() throws Exception {
		
		List<String> result = processor.process(Arrays.asList("yhdistelmä", "vaunu"));
		
		assertEquals(2, result.size());
		assertEquals("yhdistelmä", result.get(0));
		assertEquals("vaunu", result.get(1));
	}


}
