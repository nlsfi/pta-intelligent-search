package fi.maanmittauslaitos.pta.search.metadata;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessorFactory;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.XPathFieldExtractorConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.XPathFieldExtractorConfiguration.FieldExtractorType;


public class ISOMetadataExtractorConfigurationFactory {
	private DocumentProcessorFactory documentProcessorFactory = new DocumentProcessorFactory();
	
	public void setDocumentProcessorFactory(DocumentProcessorFactory documentProcessorFactory) {
		this.documentProcessorFactory = documentProcessorFactory;
	}
	
	public DocumentProcessorFactory getDocumentProcessorFactory() {
		return documentProcessorFactory;
	}
	
	
	public DocumentProcessor createMetadataDocumentProcessor() throws ParserConfigurationException
	{
		DocumentProcessingConfiguration configuration = new DocumentProcessingConfiguration();
		configuration.getNamespaces().put("gmd", "http://www.isotc211.org/2005/gmd");
		configuration.getNamespaces().put("gco", "http://www.isotc211.org/2005/gco");
		configuration.getNamespaces().put("srv", "http://www.isotc211.org/2005/srv");
		configuration.getNamespaces().put("gmx", "http://www.isotc211.org/2005/gmx");
		
		configuration.getNamespaces().put("xlink", "http://www.w3.org/1999/xlink");
		
		
		List<FieldExtractorConfiguration> extractors = configuration.getFieldExtractors();
		
		// Title extractors
		extractors.add(createXPathExtractor(
				ISOMetadataFields.TITLE,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(" +
				  "//gmd:identificationInfo/*/gmd:citation/*/gmd:title//gmd:LocalisedCharacterString[@locale='#FI']" +
				  "|" +
				  "//gmd:identificationInfo/*/gmd:citation/*/gmd:title/*[self::gco:CharacterString]" +
				")/text()"));

		extractors.add(createXPathExtractor(
				ISOMetadataFields.TITLE_SV,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(" +
				  "//gmd:identificationInfo/*/gmd:citation/*/gmd:title//gmd:LocalisedCharacterString[@locale='#SV']" +
				")/text()"));

		extractors.add(createXPathExtractor(
				ISOMetadataFields.TITLE_EN,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(" +
				  "//gmd:identificationInfo/*/gmd:citation/*/gmd:title//gmd:LocalisedCharacterString[@locale='#EN']" +
				")/text()"));

		// Abstract extractors
		
		
		
		return getDocumentProcessorFactory().createProcessor(configuration);
	}

	private FieldExtractorConfiguration createXPathExtractor(String field, FieldExtractorType type, String xpath)
	{
		XPathFieldExtractorConfiguration ret = new XPathFieldExtractorConfiguration();
		ret.setField(field);
		ret.setType(type);
		ret.setXpath(xpath);
		
		return ret;
	}
}
