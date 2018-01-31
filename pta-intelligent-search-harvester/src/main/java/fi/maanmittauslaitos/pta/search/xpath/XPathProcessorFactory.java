package fi.maanmittauslaitos.pta.search.xpath;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.SAXException;

import fi.maanmittauslaitos.pta.search.Document;
import fi.maanmittauslaitos.pta.search.text.TextProcessingChain;

public class XPathProcessorFactory {

	
	public XPathProcessorFactory() {
	}
	
	public DocumentProcessor createProcessor(XPathExtractionConfiguration configuration) throws ParserConfigurationException {
		
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
		
		return new DocumentProcessor() {
			
			@Override
			public Document processDocument(InputStream is) throws IOException, SAXException, XPathException
			{
				
				org.w3c.dom.Document doc = builder.parse(is);
				
				Document ret = new Document();
				ret.setDom(doc);
				
				for (FieldExtractorConfiguration fec : configuration.getFieldExtractors()) {
					Object value = fec.process(doc, xPath);

					if (fec.getTextProcessorName() != null) {
						TextProcessingChain chain = textProcessingChains.get(fec.getTextProcessorName());
						
						if (chain == null) {
							throw new IllegalArgumentException("Text processor chain '"+fec.getTextProcessorName()+"' not declared");
						}
						
						@SuppressWarnings("unchecked")
						List<String> asList = (List<String>)value;
						
						value = chain.process(asList);
					}
					
					ret.getFields().put(fec.getField(), value);
				}
				
				return ret;
			}
			
			
		};
	}

}
