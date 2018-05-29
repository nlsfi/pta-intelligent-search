package fi.maanmittauslaitos.pta.search.documentprocessor;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.NodeList;

public class XPathFieldExtractorConfiguration extends AbstractFieldExtractorConfiguration {
	private FieldExtractorType type;
	private String xpath;
	private XPathCustomExtractor customExtractor;
	
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
	
	
	public enum FieldExtractorType {
		FIRST_MATCHING_VALUE,
		ALL_MATCHING_VALUES,
		TRUE_IF_MATCHES_OTHERWISE_FALSE,
		CUSTOM_CLASS
	}

	public void setCustomExtractor(XPathCustomExtractor customExtractor) {
		this.customExtractor = customExtractor;
	}
	
	public XPathCustomExtractor getCustomExtractor() {
		return customExtractor;
	}

	@Override
	public Object process(org.w3c.dom.Document doc, XPath xPath) throws DocumentProcessingException 
	{
		NodeList nodeList;
		try {
			nodeList = (NodeList) xPath.compile(getXpath()).evaluate(doc, XPathConstants.NODESET);
		} catch(XPathExpressionException e) {
			throw new DocumentProcessingException(e);
		}
		
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
			
		case CUSTOM_CLASS:
			{
				XPathCustomExtractor extractor = getCustomExtractor();
				if (extractor == null) {
					throw new IllegalArgumentException("Missing XPathCustomExtractor in CUSTOM_CLASS configuration");
				}
				try {
					List<Object> ret = new ArrayList<>();
					for (int i = 0; i < nodeList.getLength(); i++) {
						Object obj = extractor.process(xPath, nodeList.item(i));
						ret.add(obj);
					}
					return ret;
				} catch(XPathException xpe) {
					throw new DocumentProcessingException(xpe);
				}
			}
			
		default:
			throw new IllegalArgumentException("Unknown type of field extractor: "+getType());
		}
		
	}

}
