package fi.maanmittauslaitos.pta.search.documentprocessor;

public abstract class AbstractFieldExtractorConfiguration implements FieldExtractorConfiguration {
	private String field;
	private String textProcessorName;
	
	@Override
	public FieldExtractorConfiguration copy() {
		AbstractFieldExtractorConfiguration ret;
		try {
			ret = (AbstractFieldExtractorConfiguration)this.getClass().newInstance();
		
		} catch(IllegalAccessException | InstantiationException e) {
			throw new RuntimeException(e);
		}
		
		ret.setField(getField());
		ret.setTextProcessorName(getTextProcessorName());
		
		copyUnderlyingFeatures(ret);
		
		return ret;
	}
	
	public abstract void copyUnderlyingFeatures(AbstractFieldExtractorConfiguration object);
	
	@Override
	public void setField(String field) {
		this.field = field;
	}
	
	@Override
	public String getField() {
		return field;
	}

	@Override
	public String getTextProcessorName() {
		return textProcessorName;
	}
	
	@Override
	public void setTextProcessorName(String textProcessorName) {
		this.textProcessorName = textProcessorName;
	}

}
