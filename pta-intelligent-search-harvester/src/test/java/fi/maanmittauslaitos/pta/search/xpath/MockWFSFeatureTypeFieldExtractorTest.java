package fi.maanmittauslaitos.pta.search.xpath;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import fi.maanmittauslaitos.pta.search.Document;

public class MockWFSFeatureTypeFieldExtractorTest {

	DocumentProcessor processor;
	
	@Before
	public void setUp() throws Exception {
		MockWFSFeatureTypeFieldExtractorConfiguration extractor = new MockWFSFeatureTypeFieldExtractorConfiguration();
		extractor.setField("wfs-fields");
		
		extractor.getInjectedFieldsById().put("1719dcdd-0f24-4406-a347-354532c97bde", Arrays.asList("foo", "bar"));

		DocumentProcessingConfiguration configuration = new DocumentProcessingConfiguration();
		configuration.getNamespaces().put("gmd", "http://www.isotc211.org/2005/gmd");
		
		configuration.getFieldExtractors().add(extractor);
		
		DocumentProcessorFactory dpf = new DocumentProcessorFactory();
		processor = dpf.createProcessor(configuration);
	}

	@Test
	public void test() throws Exception {
		InputStream is = MockWFSFeatureTypeFieldExtractorTest.class.getResourceAsStream("/metadata/1719dcdd-0f24-4406-a347-354532c97bde.xml");
		
		Document doc = processor.processDocument(is);
		assertNotNull(doc);
		
		List<String> wfsFields = doc.getListValue("wfs-fields", String.class);
		
		assertNotNull(wfsFields);
		assertEquals(2, wfsFields.size());
		assertEquals("foo", wfsFields.get(0));
		assertEquals("bar", wfsFields.get(1));
	}

}
