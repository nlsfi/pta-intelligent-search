package fi.maanmittauslaitos.pta.search.text;

import java.util.ArrayList;
import java.util.List;

public class TextProcessingChain {
	private List<TextProcessor> chain = new ArrayList<>();
	
	public List<TextProcessor> getChain() {
		return chain;
	}
	
	public void setChain(List<TextProcessor> chain) {
		this.chain = chain;
	}
	
	public List<String> process(List<String> input) {
		List<String> ret = input;
		
		for (TextProcessor processor : getChain()) {
			ret = processor.process(ret);
		}
		
		return ret;
	}
	
}
