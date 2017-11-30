package fi.maanmittauslaitos.pta.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Document {
	private Map<String, List<String>> fields = new HashMap<>();
	
	public Map<String, List<String>> getFields() {
		return fields;
	}
	
	public void setFields(Map<String, List<String>> fields) {
		this.fields = fields;
	}
}
