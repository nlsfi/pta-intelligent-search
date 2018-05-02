package fi.maanmittauslaitos.pta.search.documentprocessor;

public abstract class AbstractFieldExtractorConfiguration implements FieldExtractorConfiguration {
	private String field;
	private String textProcessorName;
	
	@Override
	public String getField() {
		return field;
	}

	@Override
	public Object getTextProcessorName() {
		return textProcessorName;
	}
	
	public void setField(String field) {
		this.field = field;
	}
	
	public void setTextProcessorName(String textProcessorName) {
		this.textProcessorName = textProcessorName;
	}


}
