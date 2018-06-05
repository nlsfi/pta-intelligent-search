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

public class TerminologyExpansionProcessorTest {

	@Test
	public void testKesykissaBroader() throws Exception {
		TerminologyExpansionProcessor processor = new TerminologyExpansionProcessor();
		
		FileReader reader = new FileReader("src/test/resources/kissa.ttl");
		Model model = Rio.parse(reader, "", RDFFormat.TURTLE);
		
		processor.setModel(model);
		processor.setPredicates(Arrays.asList(SKOS.BROADER));
		
		List<String> result = processor.process(Arrays.asList("http://www.yso.fi/onto/ysa/Y96241"));
		
		assertEquals(3, result.size());
		
		assertTrue(result.contains("http://www.yso.fi/onto/ysa/Y101826"));
		assertTrue(result.contains("http://www.yso.fi/onto/ysa/Y96840"));
		
		// 2nd level match
		assertTrue(result.contains("http://www.yso.fi/onto/ysa/Y109999"));
		
	}
	
}
