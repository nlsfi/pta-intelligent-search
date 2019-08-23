package fi.maanmittauslaitos.pta.search.documentprocessor;

import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathException;

@FunctionalInterface
public interface XPathNodeListCustomExtractor {

	Object process(XPath xPath, NodeList nodeList) throws XPathException;

}
