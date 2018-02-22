package fi.maanmittauslaitos.pta.search.text;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TextSplitterProcessorTest {

	private TextProcessor processor;
	
	@Before
	public void setUp() throws Exception {
		processor =  new TextSplitterProcessor();
	}

	@Test
	public void test() {
		List<String> result = processor.process(Arrays.asList("ABC Kissa kävelee, tikapuita pitkin - taivaaseen!"));
		
		assertArrayEquals(new String[] { "ABC", "Kissa", "kävelee", "tikapuita", "pitkin", "taivaaseen" }, result.toArray());
	}

	@Test
	public void testWhitespacePrefix() {
		List<String> result = processor.process(Arrays.asList(" ABC Kissa kävelee, tikapuita pitkin - taivaaseen!"));
		
		assertArrayEquals(new String[] { "ABC", "Kissa", "kävelee", "tikapuita", "pitkin", "taivaaseen" }, result.toArray());
	}

}
