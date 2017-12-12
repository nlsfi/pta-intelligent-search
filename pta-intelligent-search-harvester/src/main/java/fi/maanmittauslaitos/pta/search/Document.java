package fi.maanmittauslaitos.pta.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Document {
	private Map<String, List<String>> fields = new HashMap<>();
	private org.w3c.dom.Document dom;
	
	public Map<String, List<String>> getFields() {
		return fields;
	}
	
	public void setFields(Map<String, List<String>> fields) {
		this.fields = fields;
	}
	
	public org.w3c.dom.Document getDom() {
		return dom;
	}
	
	public void setDom(org.w3c.dom.Document dom) {
		this.dom = dom;
	}
}
