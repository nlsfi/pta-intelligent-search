package fi.maanmittauslaitos.pta.search.xpath;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.xpath.XPathException;

import org.xml.sax.SAXException;

import fi.maanmittauslaitos.pta.search.Document;

public interface XPathProcessor {
	public Document processDocument(InputStream is) throws IOException, SAXException, XPathException;
}
