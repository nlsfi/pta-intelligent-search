package fi.maanmittauslaitos.pta.search.text;

import java.util.ArrayList;
import java.util.List;

public class TextSplitterProcessor implements TextProcessor {

	@Override
	public List<String> process(List<String> input) {
		List<String> ret = new ArrayList<>();
		
		for (String str : input) {
			String [] words = str.split("(?U)\\W+");
			
			for (String w : words) {
				if (w == null || w.length() == 0) continue;
				
				ret.add(w);
			}
		}
		
		return ret;
	}

}
