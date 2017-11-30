package fi.maanmittauslaitos.pta.search.text;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class TextProcessorChainTest {

	@Test
	public void test1to1() {
		TextProcessingChain chain = new TextProcessingChain();
		
		chain.getChain().add(new TextProcessor() {
			
			@Override
			public List<String> process(String str) {
				return Collections.singletonList(str+"#");
			}
		});
		
		List<String> output = chain.process(Arrays.asList("one", "two"));
		
		assertArrayEquals(output.toArray(), new Object[] { "one#", "two#" });
	}

	@Test
	public void test1to2() {
		TextProcessingChain chain = new TextProcessingChain();
		
		chain.getChain().add(new TextProcessor() {
			
			@Override
			public List<String> process(String str) {
				return Arrays.asList(str+"1", str+"2");
			}
		});
		
		List<String> output = chain.process(Arrays.asList("one", "two"));
		
		assertArrayEquals(output.toArray(), new Object[] { "one1", "one2", "two1", "two2" });
	}
	

	@Test
	public void test1to2to4() {
		TextProcessingChain chain = new TextProcessingChain();
		
		chain.getChain().add(new TextProcessor() {
			
			@Override
			public List<String> process(String str) {
				return Arrays.asList(str+"-1", str+"-2");
			}
		});
		
		chain.getChain().add(new TextProcessor() {
			
			@Override
			public List<String> process(String str) {
				return Arrays.asList(str+"-1", str+"-2");
			}
		});
		
		List<String> output = chain.process(Arrays.asList("one", "two"));
		
		assertArrayEquals(output.toArray(), new Object[] { 
				"one-1-1", "one-1-2", "one-2-1", "one-2-2",
				"two-1-1", "two-1-2", "two-2-1", "two-2-2"
				});
	}
	
	@Test
	public void testEmptyChain() {
		TextProcessingChain chain = new TextProcessingChain();
		
		List<String> output = chain.process(Arrays.asList("one", "two"));
		
		assertArrayEquals(output.toArray(), new Object[] { "one", "two" });
	}

	
	
}
