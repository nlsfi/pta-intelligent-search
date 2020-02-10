package fi.maanmittauslaitos.pta.search.metadata.extractor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import java.util.Arrays;

public class GeographicBoundingBoxXmlCustomExtractor extends XmlCustomExtractor {

	private static Logger logger = LogManager.getLogger(GeographicBoundingBoxXmlCustomExtractor.class);

	public GeographicBoundingBoxXmlCustomExtractor() {
		super();
	}

	public GeographicBoundingBoxXmlCustomExtractor(boolean isThrowException) {
		super(isThrowException);
	}

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
	public Object process(XPath xPath, Node node) throws RuntimeException {
		Double [] ret = new Double[4];

		try {
			ret[0] = getDoubleValue("westBoundLongitude", xPath, node);
			ret[1] = getDoubleValue("southBoundLatitude", xPath, node);
			ret[2] = getDoubleValue("eastBoundLongitude", xPath, node);
			ret[3] = getDoubleValue("northBoundLatitude", xPath, node);
		} catch(MissingCoordException | XPathException e) {
			if (e instanceof MissingCoordException) {
				return null;
			} else {
				handleExtractorException(e, null);
			}
		}
		return Arrays.asList(ret);
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

	private static class MissingCoordException extends Exception {
		private static final long serialVersionUID = 1L;
		
	}
}
