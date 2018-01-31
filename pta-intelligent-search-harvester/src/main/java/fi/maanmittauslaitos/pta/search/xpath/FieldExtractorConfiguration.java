package fi.maanmittauslaitos.pta.search.xpath;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathException;

import org.w3c.dom.Document;

public interface FieldExtractorConfiguration {

	/**
	 * Name of the field where the extracted value should be stored
	 * @return
	 */
	public String getField();

	/**
	 * Process the document and returns the raw value. If this configuration has a text processor configured,
	 * that is not applied within this function.
	 * 
	 * @param doc
	 * @return
	 */
	public Object process(Document doc, XPath xpath) throws XPathException;

	
	public Object getTextProcessorName();

}
