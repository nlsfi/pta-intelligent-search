package fi.maanmittauslaitos.pta.search.text;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class StopWordsTest {

	@Test
	public void test() {
		StopWordsProcessor processor = new StopWordsProcessor();
		processor.setStopwords(Arrays.asList("hey"));
		
		TextProcessingChain chain = new TextProcessingChain();
		chain.getChain().add(processor);
		
		
		List<String> result = chain.process(Arrays.asList("hey", "hey", "my", "my"));
		
		assertArrayEquals(new String[] { "my", "my" }, result.toArray());
		
	}

}
