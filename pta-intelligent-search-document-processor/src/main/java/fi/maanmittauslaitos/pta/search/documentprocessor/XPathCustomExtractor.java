package fi.maanmittauslaitos.pta.search.documentprocessor;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathException;

import org.w3c.dom.Node;

public interface XPathCustomExtractor {

	public Object process(XPath xPath, Node node) throws XPathException;

}
