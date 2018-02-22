package fi.maanmittauslaitos.pta.search.xpath;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * This is a mock since the metadata is not reliable enough to gather this information. Instead
 * of reading the data from the metadata, this uses a configured location from where it loads
 * the names of the fields.
 * 
 * The real implementation must be able to load the XML schema of a feature type taking into consideration
 * the fact that a schema might point to another schema etc. So xpath extraction is not really the 
 * correct way of proceeding, instead a proper schema libraby is required.
 * 
 * @author v2
 *
 */
public class MockWFSFeatureTypeFieldExtractorConfiguration extends AbstractFieldExtractorConfiguration {
	private static Logger logger = Logger.getLogger(MockWFSFeatureTypeFieldExtractorConfiguration.class);
	
	// Injected via Spring
	private String idXPathExpression = "//gmd:fileIdentifier/*/text()";
	private Map<String, List<String>> injectedFieldsById = new HashMap<>();
	
	public void setIdXPathExpression(String idXPathExpression) {
		this.idXPathExpression = idXPathExpression;
	}
	
	public String getIdXPathExpression() {
		return idXPathExpression;
	}
	
	public void setInjectedFieldsById(Map<String, List<String>> injectedFieldsById) {
		this.injectedFieldsById = injectedFieldsById;
	}
	
	public Map<String, List<String>> getInjectedFieldsById() {
		return injectedFieldsById;
	}
	
	@Override
	public Object process(Document doc, XPath metadataXPath) throws DocumentProcessingException {
		try {
			NodeList nodeList = (NodeList) metadataXPath.compile(getIdXPathExpression()).evaluate(doc, XPathConstants.NODESET);
			
			if (nodeList.getLength() == 0) {
				return null;
			}
			
			String id = nodeList.item(0).getNodeValue();
			
			// Load document from wherever and then parse it etc...
			
			List<String> fields = getInjectedFieldsById().get(id);
			if (fields == null) {
				logger.debug("No injected fieds for service id "+id);
				fields = Collections.emptyList();
			}
			
			return fields;
		} catch(XPathException e) {
			throw new DocumentProcessingException(e);
		}
	}


}
