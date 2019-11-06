package fi.maanmittauslaitos.pta.search;

import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.XmlDocumentProcessorImpl;
import fi.maanmittauslaitos.pta.search.text.TextProcessingChain;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OntologyProcessingTest {
	private static XmlDocumentProcessorImpl processor;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		processor = (XmlDocumentProcessorImpl)
				new HarvesterConfig().getCSWRecordProcessor();
	}

	@Test
	public void testParentsResolvedUntilTopmostTerm() {
		DocumentProcessingConfiguration config = processor.getConfiguration();
		FieldExtractorConfiguration fec = config.getFieldExtractor("abstract_uri_parents");
		TextProcessingChain chain = config.getTextProcessingChains().get(fec.getTextProcessorName());
		
		List<String> output = chain.process(Arrays.asList("kissa"));
		
		assertTrue(output.contains("http://www.yso.fi/onto/yso/p864")); // Kissaeläimet
		assertTrue(output.contains("http://www.yso.fi/onto/yso/p14567")); // Petoeläimet
		assertTrue(output.contains("http://www.yso.fi/onto/yso/p308")); // Nisäkkäät
		assertTrue(output.contains("http://www.yso.fi/onto/yso/p309")); // Selkärankaiset
		assertTrue(output.contains("http://www.yso.fi/onto/yso/p834")); // Selkäjänteiset
		assertTrue(output.contains("http://www.yso.fi/onto/yso/p2023")); // Eläimet
		assertTrue(output.contains("http://www.yso.fi/onto/yso/p20765")); // Aitotumaiset
		assertTrue(output.contains("http://www.yso.fi/onto/yso/p2024")); // Eliöt
		assertTrue(output.contains("http://www.yso.fi/onto/yso/p174")); // Orgaaninen rakenne
		assertTrue(output.contains("http://www.yso.fi/onto/yso/p1435")); // Fyysinen objekti
		assertTrue(output.contains("http://www.yso.fi/onto/yso/p4762")); // Pysyvä
		assertTrue(output.contains("http://www.yso.fi/onto/yso/p4205")); // YSO-käsitteet
		
		assertEquals(12, output.size());
	}

}
