package fi.maanmittauslaitos.pta.search.xpath;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fi.maanmittauslaitos.pta.search.Document;
import fi.maanmittauslaitos.pta.search.text.TextProcessingChain;

public class XPathProcessorFactory {

	
	public XPathProcessorFactory() {
	}
	
	public XPathProcessor createProcessor(XPathExtractionConfiguration configuration) throws ParserConfigurationException {
		
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		

		HashmapNamespaceContextImpl nsContext = new HashmapNamespaceContextImpl();
		for (Entry<String, String> e : configuration.getNamespaces().entrySet()) {
			nsContext.registerNamespace(e.getKey(), e.getValue());
		}

		XPath xPath = XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(nsContext);
		
		Map<String, TextProcessingChain> textProcessingChains = configuration.getTextProcessingChains();
		
		return new XPathProcessor() {
			
			@Override
			public Document processDocument(InputStream is) throws IOException, SAXException, XPathException
			{
				
				org.w3c.dom.Document doc = builder.parse(is);
				
				Document ret = new Document();
				ret.setDom(doc);
				
				for (FieldExtractorConfiguration fec : configuration.getFieldExtractors()) {
					Object value = processField(doc, fec);

					if (fec.getTextProcessorName() != null) {
						TextProcessingChain chain = textProcessingChains.get(fec.getTextProcessorName());
						
						@SuppressWarnings("unchecked")
						List<String> asList = (List<String>)value;
						
						value = chain.process(asList);
					}
					
					ret.getFields().put(fec.getField(), value);
				}
				
				return ret;
			}
			
			private Object processField(org.w3c.dom.Document doc, FieldExtractorConfiguration fec) throws XPathException 
			{
				NodeList nodeList = (NodeList) xPath.compile(fec.getXpath()).evaluate(doc, XPathConstants.NODESET);
				
				
				switch(fec.getType()) {
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
					throw new IllegalArgumentException("Unknown type of field extractor: "+fec.getType());
				}
				
			}
		};
	}

}
