package fi.maanmittauslaitos.pta.search.text;

import java.util.Collections;
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
	public List<String> process(String str) {
		if (getAcceptedStrings().contains(str)) {
			return Collections.singletonList(str);
		}
		return Collections.emptyList();
	}

}
