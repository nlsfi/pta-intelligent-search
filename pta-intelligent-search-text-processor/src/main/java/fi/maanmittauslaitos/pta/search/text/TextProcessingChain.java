package fi.maanmittauslaitos.pta.search.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

public class TextProcessingChain {
	private static Logger logger = Logger.getLogger(TextProcessingChain.class);
	
	private List<TextProcessor> chain = new ArrayList<>();
	
	public List<TextProcessor> getChain() {
		return chain;
	}
	
	public void setChain(List<TextProcessor> chain) {
		this.chain = chain;
	}
	
	public List<String> process(List<String> input) {
		List<String> ret = new ArrayList<>();
		
		for (String str : input) {
			List<String> tmp = processString(str);
			
			ret.addAll(tmp);
		}
		
		return ret;
	}

	private List<String> processString(String inputStr) {
		List<String> buffer = Collections.singletonList(inputStr);
		
		if (logger.isTraceEnabled()) {
			logger.trace("Prosessoidaan sana "+inputStr);
		}
		
		int i = 0;
		for (TextProcessor processor : getChain()) {
			List<String> nextBuffer = new ArrayList<>();
			
			for (String str : buffer) {
				List<String> tmp = processor.process(str);
				
				nextBuffer.addAll(tmp);
			}
			
			if (logger.isTraceEnabled()) {
				logger.trace("\t#"+i+", "+processor.getClass().getSimpleName()+": "+buffer+" => "+nextBuffer);
			}
			
			buffer = nextBuffer;
			i++;
		}
		
		return buffer;
	}
	
	
}
