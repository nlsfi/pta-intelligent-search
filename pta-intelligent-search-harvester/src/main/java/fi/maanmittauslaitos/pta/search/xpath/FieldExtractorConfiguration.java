package fi.maanmittauslaitos.pta.search.xpath;

public class FieldExtractorConfiguration {
	private String field;
	private FieldExtractorType type;
	private String xpath;
	private String textProcessorName;
	
	public void setField(String field) {
		this.field = field;
	}
	
	public String getField() {
		return field;
	}
	
	public void setType(FieldExtractorType type) {
		this.type = type;
	}
	
	public FieldExtractorType getType() {
		return type;
	}
	
	public void setXpath(String xpath) {
		this.xpath = xpath;
	}
	
	public String getXpath() {
		return xpath;
	}
	
	public String getTextProcessorName() {
		return textProcessorName;
	}
	
	public void setTextProcessorName(String textProcessorName) {
		this.textProcessorName = textProcessorName;
	}
	
	public enum FieldExtractorType {
		FIRST_MATCHING_VALUE,
		ALL_MATCHING_VALUES,
		TRUE_IF_MATCHES_OTHERWISE_FALSE
	}
}
