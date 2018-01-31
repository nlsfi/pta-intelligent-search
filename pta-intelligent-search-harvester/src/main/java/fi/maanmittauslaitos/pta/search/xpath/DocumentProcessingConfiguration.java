package fi.maanmittauslaitos.pta.search.xpath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.maanmittauslaitos.pta.search.text.TextProcessingChain;

public class DocumentProcessingConfiguration {
	private Map<String, String> namespaces = new HashMap<>();
	private List<FieldExtractorConfiguration> fieldExtractors = new ArrayList<>();
	private Map<String, TextProcessingChain> textProcessingChains = new HashMap<>();
	
	public void setFieldExtractors(List<FieldExtractorConfiguration> fieldExtractors) {
		this.fieldExtractors = fieldExtractors;
	}
	
	public List<FieldExtractorConfiguration> getFieldExtractors() {
		return fieldExtractors;
	}
	
	public void setNamespaces(Map<String, String> namespaces) {
		this.namespaces = namespaces;
	}
	
	public Map<String, String> getNamespaces() {
		return namespaces;
	}
	
	public void setTextProcessingChains(Map<String, TextProcessingChain> textProcessingChains) {
		this.textProcessingChains = textProcessingChains;
	}
	
	public Map<String, TextProcessingChain> getTextProcessingChains() {
		return textProcessingChains;
	}
}
