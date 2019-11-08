package fi.maanmittauslaitos.pta.search.metadata;

import fi.maanmittauslaitos.pta.search.documentprocessor.CustomExtractor;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfigurationImpl;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfigurationImpl.FieldExtractorType;

import javax.xml.parsers.ParserConfigurationException;
import java.util.List;

import static fi.maanmittauslaitos.pta.search.metadata.utils.XPathHelper.doesntMatch;
import static fi.maanmittauslaitos.pta.search.metadata.utils.XPathHelper.matches;


public class ISOMetadataExtractorConfigurationFactory extends MetadataExtractorConfigurationFactory {


	@Override
	public DocumentProcessingConfiguration createMetadataDocumentProcessingConfiguration() throws ParserConfigurationException {
		DocumentProcessingConfiguration configuration = new DocumentProcessingConfiguration();
		configuration.getNamespaces().put("gmd", "http://www.isotc211.org/2005/gmd");
		configuration.getNamespaces().put("gco", "http://www.isotc211.org/2005/gco");
		configuration.getNamespaces().put("srv", "http://www.isotc211.org/2005/srv");
		configuration.getNamespaces().put("gmx", "http://www.isotc211.org/2005/gmx");

		configuration.getNamespaces().put("xlink", "http://www.w3.org/1999/xlink");


		List<FieldExtractorConfiguration> extractors = configuration.getFieldExtractors();

		// Id extractor
		extractors.add(createXPathExtractor(
				ResultMetadataFields.ID,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"//gmd:fileIdentifier/*/text()"));

		// Title extractors
		extractors.add(createXPathExtractor(
				ResultMetadataFields.TITLE,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(" +
						"//gmd:identificationInfo/*/gmd:citation/*/gmd:title//gmd:LocalisedCharacterString[" +
						matches("@locale", "'#FI'") + "]" +
						"|" +
						"//gmd:identificationInfo/*/gmd:citation/*/gmd:title/*[self::gco:CharacterString]" +
						")/text()"));

		extractors.add(createXPathExtractor(
				ResultMetadataFields.TITLE_SV,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(" +
						"//gmd:identificationInfo/*/gmd:citation/*/gmd:title//gmd:LocalisedCharacterString[" +
						matches("@locale", "'#SV'") + "]" +
						")/text()"));

		extractors.add(createXPathExtractor(
				ResultMetadataFields.TITLE_EN,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(" +
						"//gmd:identificationInfo/*/gmd:citation/*/gmd:title//gmd:LocalisedCharacterString[" +
						matches("@locale", "'#EN'") + "])/text()"));

		// Abstract extractors
		extractors.add(createXPathExtractor(
				ResultMetadataFields.ABSTRACT,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(" +
						"//gmd:identificationInfo/*/gmd:abstract//gmd:LocalisedCharacterString[" +
						matches("@locale", "'#FI'") + "]" +
						"|" +
						"//gmd:identificationInfo/*/gmd:abstract/*[self::gco:CharacterString]" +
						")/text()"));

		extractors.add(createXPathExtractor(
				ResultMetadataFields.ABSTRACT_SV,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(" +
						"//gmd:identificationInfo/*/gmd:abstract//gmd:LocalisedCharacterString[" +
						matches("@locale", "'#SV'") + "])/text()"));

		extractors.add(createXPathExtractor(
				ResultMetadataFields.ABSTRACT_EN,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(" +
						"//gmd:identificationInfo/*/gmd:abstract//gmd:LocalisedCharacterString[" +
						matches("@locale", "'#EN'") + "])/text()"));

		// isService 
		extractors.add(createXPathExtractor(
				ResultMetadataFields.IS_SERVICE,
				FieldExtractorType.TRUE_IF_MATCHES_OTHERWISE_FALSE,
				"//gmd:hierarchyLevel/gmd:MD_ScopeCode["
						+ matches("@codeList", "'http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_ScopeCode'")
						+ " and "
						+ matches("@codeListValue", "'service'")
						+ "]"));

		// isDataset
		extractors.add(createXPathExtractor(
				ResultMetadataFields.IS_DATASET,
				FieldExtractorType.TRUE_IF_MATCHES_OTHERWISE_FALSE,
				"//gmd:hierarchyLevel/gmd:MD_ScopeCode["
						+ matches("@codeList", "'http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_ScopeCode'")
						+ " and "
						+ "(" + matches("@codeListValue", "'dataset'") + " or " + matches("@codeListValue", "'series'") + ")"
						+ "]"));


		// isAvoindata
		extractors.add(createXPathExtractor(
				ResultMetadataFields.IS_AVOINDATA,
				FieldExtractorType.TRUE_IF_MATCHES_OTHERWISE_FALSE,
				"//gmd:identificationInfo/*/gmd:descriptiveKeywords/*/gmd:keyword/*[" +
						matches("text()", "'avoindata.fi'") +
						"]"));

		// Topic categories
		extractors.add(createXPathExtractor(
				ResultMetadataFields.TOPIC_CATEGORIES,
				FieldExtractorType.ALL_MATCHING_VALUES,
				"//gmd:identificationInfo/*/gmd:topicCategory/gmd:MD_TopicCategoryCode/text()"));

		// Keywords
		extractors.add(createXPathExtractor(
				ResultMetadataFields.KEYWORDS_ALL,
				FieldExtractorType.ALL_MATCHING_VALUES,
				"//gmd:identificationInfo/*/gmd:descriptiveKeywords/*/gmd:keyword/gco:CharacterString[" +
						doesntMatch("text()", "'avoindata.fi'") + "]/text()"));

		// Inspire themes
		extractors.add(createXPathExtractor(
				ResultMetadataFields.KEYWORDS_INSPIRE,
				FieldExtractorType.ALL_MATCHING_VALUES,
				"//gmd:identificationInfo/*/gmd:descriptiveKeywords/*[" +
						matches("gmd:thesaurusName/gmd:CI_Citation/gmd:title/*/text()", "'GEMET - INSPIRE themes, version 1.0'") +
						"]/gmd:keyword/gco:CharacterString/text()"));

		// Distribution Formats
		extractors.add(createXPathExtractor(
				ResultMetadataFields.DISTRIBUTION_FORMATS,
				FieldExtractorType.ALL_MATCHING_VALUES,
				"//gmd:distributionInfo/*/gmd:distributionFormat/*/gmd:name/gco:*/text()"));


		// Datestamp
		extractors.add(createXPathExtractor(
				ResultMetadataFields.DATESTAMP,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"//gmd:MD_Metadata/gmd:dateStamp/*/text()"));


		// Organisation names + roles
		extractors.add(createXPathExtractor(
				ResultMetadataFields.ORGANISATIONS,
				new ResponsiblePartyXmlCustomExtractor(),
				"//gmd:MD_Metadata/gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty"));

		// Geographic bounding box
		extractors.add(createXPathExtractor(
				ResultMetadataFields.GEOGRAPHIC_BOUNDING_BOX,
				new GeographicBoundingBoxXmlCustomExtractor(),
				"//gmd:MD_Metadata/gmd:identificationInfo/*/*[self::gmd:extent or self::srv:extent]/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox"));

		return configuration;
	}


	@Override
	public DocumentProcessor createMetadataDocumentProcessor() throws ParserConfigurationException {
		DocumentProcessingConfiguration configuration = createMetadataDocumentProcessingConfiguration();
		return getDocumentProcessorFactory().createXmlProcessor(configuration);
	}

	private FieldExtractorConfiguration createXPathExtractor(String field, FieldExtractorType type, String xpath) {
		FieldExtractorConfigurationImpl ret = new FieldExtractorConfigurationImpl();
		ret.setField(field);
		ret.setType(type);
		ret.setQuery(xpath);

		return ret;
	}

	private FieldExtractorConfiguration createXPathExtractor(String field, CustomExtractor extractor, String xpath) {
		FieldExtractorConfigurationImpl ret = new FieldExtractorConfigurationImpl();
		ret.setField(field);
		ret.setType(FieldExtractorType.CUSTOM_CLASS);
		ret.setQuery(xpath);
		ret.setCustomExtractor(extractor);

		return ret;
	}
}
