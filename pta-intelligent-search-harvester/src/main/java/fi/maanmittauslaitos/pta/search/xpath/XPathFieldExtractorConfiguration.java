package fi.maanmittauslaitos.pta.search.xpath;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;

import org.w3c.dom.NodeList;

public class XPathFieldExtractorConfiguration implements FieldExtractorConfiguration {
	private String field;
	private FieldExtractorType type;
	private String xpath;
	private String textProcessorName;
	
	public void setField(String field) {
		this.field = field;
	}
	
	@Override
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
	
	@Override
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
	

	@Override
	public Object process(org.w3c.dom.Document doc, XPath xPath) throws XPathException 
	{
		NodeList nodeList = (NodeList) xPath.compile(getXpath()).evaluate(doc, XPathConstants.NODESET);
		
		
		switch(getType()) {
		case FIRST_MATCHING_VALUE:
			{
				List<String> ret = new ArrayList<>();
				if (nodeList.getLength() > 0) {
					String value = nodeList.item(0).getNodeValue();
					if (value != null) {
						value = value.trim();
					}
					ret.add(value);
				}
				return ret;
			}
		case ALL_MATCHING_VALUES:
			{
				List<String> ret = new ArrayList<>();
				for (int i = 0; i < nodeList.getLength(); i++) {
					String value = nodeList.item(i).getNodeValue();
					if (value != null) {
						value = value.trim();
					}
					ret.add(value);
				}
				return ret;
			}
		case TRUE_IF_MATCHES_OTHERWISE_FALSE:
			{
				boolean matches = nodeList.getLength() > 0;
				return Boolean.valueOf(matches);
			}
			
		default:
			throw new IllegalArgumentException("Unknown type of field extractor: "+getType());
		}
		
	}
}
