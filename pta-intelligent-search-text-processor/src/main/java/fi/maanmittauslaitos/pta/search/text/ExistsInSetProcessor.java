package fi.maanmittauslaitos.pta.search.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExistsInSetProcessor implements TextProcessor {

	private Set<String> acceptedStrings;
	
	public void setAcceptedStrings(Set<String> acceptedStrings) {
		this.acceptedStrings = acceptedStrings;
	}
	
	public Set<String> getAcceptedStrings() {
		return acceptedStrings;
	}
	
	@Override
	public List<String> process(List<String> input) {
		List<String> ret = new ArrayList<>();
		
		for (String str : input) {
			if (getAcceptedStrings().contains(str)) {
				ret.add(str);
			}
		}
		return ret;
	}

}
