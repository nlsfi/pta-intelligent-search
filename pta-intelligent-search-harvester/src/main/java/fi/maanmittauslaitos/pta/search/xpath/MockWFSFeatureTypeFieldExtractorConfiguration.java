package fi.maanmittauslaitos.pta.search.xpath;

import java.util.Collections;
import java.util.HashMap;
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

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * This is a mock since the metadata is not reliable enough to gather this information. Instead
 * of reading the data from the metadata, this uses a configured location from where it loads
 * the names of the fields.
 * 
 * @author v2
 *
 */
public class MockWFSFeatureTypeFieldExtractorConfiguration extends AbstractFieldExtractorConfiguration {
	private static Logger logger = Logger.getLogger(MockWFSFeatureTypeFieldExtractorConfiguration.class);
	
	// Injected via Spring
	private String idXPathExpression = "//gmd:fileIdentifier/*/text()";
	private Map<String, String> gmlSchemaNamespaces = new HashMap<>();
	private Map<String, List<String>> injectedFieldsById = new HashMap<>();
	
	// Lazy initialized
	private DocumentBuilder gmlSchemaDocumentBuilder;
	private XPath gmlSchemaXPath;
	
	public void setIdXPathExpression(String idXPathExpression) {
		this.idXPathExpression = idXPathExpression;
	}
	
	public String getIdXPathExpression() {
		return idXPathExpression;
	}
	
	public Map<String, String> getGmlSchemaNamespaces() {
		return gmlSchemaNamespaces;
	}
	
	public void setGmlSchemaNamespaces(Map<String, String> gmlSchemaNamespaces) {
		gmlSchemaDocumentBuilder = null;
		gmlSchemaXPath = null;
		
		this.gmlSchemaNamespaces = gmlSchemaNamespaces;
	}
	
	public void setInjectedFieldsById(Map<String, List<String>> injectedFieldsById) {
		this.injectedFieldsById = injectedFieldsById;
	}
	
	public Map<String, List<String>> getInjectedFieldsById() {
		return injectedFieldsById;
	}
	
	/**
	 * Only required once we really parse schema documents, now we'll just take it easy.
	 * 
	 * @throws ParserConfigurationException
	 */
	private void ensureXmlTools() throws ParserConfigurationException {
		if (gmlSchemaXPath != null && gmlSchemaDocumentBuilder != null) {
			return;
		}
		
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		
		gmlSchemaDocumentBuilder = builderFactory.newDocumentBuilder();
		

		HashmapNamespaceContextImpl nsContext = new HashmapNamespaceContextImpl();
		for (Entry<String, String> e : getGmlSchemaNamespaces().entrySet()) {
			nsContext.registerNamespace(e.getKey(), e.getValue());
		}
		
		gmlSchemaXPath = XPathFactory.newInstance().newXPath();
		gmlSchemaXPath.setNamespaceContext(nsContext);
	}
	
	@Override
	public Object process(Document doc, XPath metadataXPath) throws DocumentProcessingException {
		try {
			NodeList nodeList = (NodeList) metadataXPath.compile(getIdXPathExpression()).evaluate(doc, XPathConstants.NODESET);
			
			if (nodeList.getLength() == 0) {
				return null;
			}
			
			String id = nodeList.item(0).getNodeValue();
			
			ensureXmlTools();
			
			// Load document from wherever and then parse it etc...
			
			List<String> fields = getInjectedFieldsById().get(id);
			if (fields == null) {
				logger.debug("No injected fieds for service id "+id);
				fields = Collections.emptyList();
			}
			
			return fields;
		} catch(XPathException | ParserConfigurationException e) {
			throw new DocumentProcessingException(e);
		}
	}


}
