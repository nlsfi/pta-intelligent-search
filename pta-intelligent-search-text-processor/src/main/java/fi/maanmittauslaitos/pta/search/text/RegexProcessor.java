package fi.maanmittauslaitos.pta.search.text;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class RegexProcessor implements TextProcessor {
	private Pattern pattern;
	private boolean includeMatches;
	
	public void setIncludeMatches(boolean includeMatches) {
		this.includeMatches = includeMatches;
	}
	
	public boolean isIncludeMatches() {
		return includeMatches;
	}
	
	public Pattern getPattern() {
		return pattern;
	}
	
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}
	
	
	
	@Override
	public List<String> process(String str) {
		boolean allow = getPattern().matcher(str).matches();
		
		if (!includeMatches) {
			allow = !allow;
		}
		
		if (allow) {
			return Collections.singletonList(str);
		} else {
			return Collections.emptyList();
		}
	}

}
