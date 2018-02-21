package fi.maanmittauslaitos.pta.search.xpath;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import fi.maanmittauslaitos.pta.search.Document;
import fi.maanmittauslaitos.pta.search.text.TextProcessingChain;

public class DocumentProcessorFactory {

	
	public DocumentProcessorFactory() {
	}
	
	public DocumentProcessor createProcessor(DocumentProcessingConfiguration configuration) throws ParserConfigurationException {
		
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
			public Document processDocument(InputStream is) throws DocumentProcessingException
			{
				
				org.w3c.dom.Document doc;
				
				try {
					doc = builder.parse(is);
				} catch(Exception e) {
					throw new DocumentProcessingException(e);
				}
				
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
