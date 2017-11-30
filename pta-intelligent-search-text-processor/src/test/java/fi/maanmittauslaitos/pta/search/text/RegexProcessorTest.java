package fi.maanmittauslaitos.pta.search.text;

import static org.junit.Assert.*;

import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

public class RegexProcessorTest {

	// includesMatches = true, string matches => string is returned
	@Test
	public void testMatchIncludes() {
		RegexProcessor processor = new RegexProcessor();
		processor.setIncludeMatches(true);
		processor.setPattern(Pattern.compile("^abc$"));
		
		List<String> result = processor.process("abc");
		assertArrayEquals(new String[] { "abc" }, result.toArray());
	}

	// includesMatches = false, string matches => nothing is returned
	@Test
	public void testMatchNotIncludes() {
		RegexProcessor processor = new RegexProcessor();
		processor.setIncludeMatches(false);
		processor.setPattern(Pattern.compile("^abc$"));
		
		List<String> result = processor.process("abc");
		assertEquals(0, result.size());
	}
	
	// includesMatches = false, string does not match => string is returned
	@Test
	public void testNotMatchNotIncludes() {
		RegexProcessor processor = new RegexProcessor();
		processor.setIncludeMatches(false);
		processor.setPattern(Pattern.compile("^abc$"));
		
		List<String> result = processor.process("def");
		assertArrayEquals(new String[] { "def" }, result.toArray());
	}
	

	// includesMatches = true, string does not match => nothing is returned
	@Test
	public void testNotMatchIncludes() {
		RegexProcessor processor = new RegexProcessor();
		processor.setIncludeMatches(true);
		processor.setPattern(Pattern.compile("^abc$"));
		
		List<String> result = processor.process("def");
		assertEquals(0, result.size());
	}
	

}
