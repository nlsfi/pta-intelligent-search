package fi.maanmittauslaitos.pta.search.metadata;

import fi.maanmittauslaitos.pta.search.documentprocessor.CustomExtractor;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.DocumentQuery;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.QueryResult;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.XmlDocumentQueryImpl;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.XmlQueryResultImpl;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import java.util.Arrays;

public class GeographicBoundingBoxXmlCustomExtractor implements CustomExtractor {

	private Double getDoubleValue(String elementName, XPath xPath, Node node)
			throws XPathException, MissingCoordException
	{
		XPathExpression nameExpr =
				xPath.compile("./gmd:"+elementName+"/gco:Decimal/text()");
		Double ret = (Double)nameExpr.evaluate(node, XPathConstants.NUMBER);
		if (ret == null || ret.isNaN()) {
			throw new MissingCoordException();
		}
		return ret;
	}
	
	@Override
	public Object process(DocumentQuery documentQuery, QueryResult queryResult) throws XPathException, DocumentProcessingException {
		Double [] ret = new Double[4];
		if (!(documentQuery instanceof XmlDocumentQueryImpl)) {
			throw new DocumentProcessingException("This extractor should only be used for XML Documents");
		}
		XPath xPath = ((XmlDocumentQueryImpl) documentQuery).getxPath();
		Node node = ((XmlQueryResultImpl) queryResult).getNode();

		try {
			ret[0] = getDoubleValue("westBoundLongitude", xPath, node);
			ret[1] = getDoubleValue("southBoundLatitude", xPath, node);
			ret[2] = getDoubleValue("eastBoundLongitude", xPath, node);
			ret[3] = getDoubleValue("northBoundLatitude", xPath, node);
		} catch(MissingCoordException e) {
			return null;
		}
		return Arrays.asList(ret);
	}

	private static class MissingCoordException extends Exception {
		private static final long serialVersionUID = 1L;
		
	}
}
