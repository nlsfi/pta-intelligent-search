package fi.maanmittauslaitos.pta.search.metadata;

import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfigurationImpl.FieldExtractorType;
import fi.maanmittauslaitos.pta.search.metadata.extractor.*;

import javax.xml.parsers.ParserConfigurationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fi.maanmittauslaitos.pta.search.metadata.utils.ISOMetadataExtractorUtil.createXPathExtractor;
import static fi.maanmittauslaitos.pta.search.metadata.utils.XPathHelper.doesntMatch;
import static fi.maanmittauslaitos.pta.search.metadata.utils.XPathHelper.matches;


public class ISOMetadataExtractorConfigurationFactory extends MetadataExtractorConfigurationFactory {


	private static final boolean DEFAULT_CUSTOM_EXTRACTOR_IS_EXCEPTION_THROW = true;

	private Map<String, Boolean> customExtractorExceptionThrowingConfig;

	public ISOMetadataExtractorConfigurationFactory() {
		this.customExtractorExceptionThrowingConfig = new HashMap<>();

	}

	public ISOMetadataExtractorConfigurationFactory(Map<String, Boolean> customExtractorExceptionSettings) {
		if (customExtractorExceptionSettings == null) {
			customExtractorExceptionSettings = new HashMap<>();
		}
		this.customExtractorExceptionThrowingConfig = customExtractorExceptionSettings;
	}


	@Override
	public DocumentProcessingConfiguration createMetadataDocumentProcessingConfiguration() throws ParserConfigurationException {
		DocumentProcessingConfiguration configuration = new DocumentProcessingConfiguration();
		configuration.getNamespaces().put("gmd", "http://www.isotc211.org/2005/gmd");
		configuration.getNamespaces().put("gco", "http://www.isotc211.org/2005/gco");
		configuration.getNamespaces().put("srv", "http://www.isotc211.org/2005/srv");
		configuration.getNamespaces().put("gmx", "http://www.isotc211.org/2005/gmx");

		configuration.getNamespaces().put("xlink", "http://www.w3.org/1999/xlink");

		configuration.getNamespaces().put("csw", "http://www.opengis.net/cat/csw/2.0.2");
		configuration.getNamespaces().put("dc", "http://purl.org/dc/elements/1.1/");


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
				"//gmd:identificationInfo/*/gmd:descriptiveKeywords/*/gmd:keyword/*[" +
						doesntMatch("text()", "'avoindata.fi'") + "and normalize-space(.//text())]/text()"));

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

		// Additional needed extractors:
		// Images of the data
		extractors.add(createXPathExtractor(
				ResultMetadataFields.IMAGE_OVERVIEW_URL,
				FieldExtractorType.ALL_MATCHING_VALUES,
//                "(//gmd:identificationInfo/*/gmd:graphicOverview/gmd:MD_BrowseGraphic[gmd:fileDescription/gco:CharacterString[text()='thumbnail']]/gmd:fileName/gco:CharacterString/text())"));
				"(//gmd:identificationInfo/*/gmd:graphicOverview/*/gmd:fileName/gco:CharacterString/text())"));

		// rerource id
		extractors.add(createXPathExtractor(
				ResultMetadataFields.RESOURCE_ID,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(//gmd:identificationInfo/*/gmd:citation/*/gmd:identifier/*/gmd:code/gco:CharacterString/text())"));



		// Lineage / source of the metadata
		extractors.add(createXPathExtractor(
				ResultMetadataFields.LINEAGE,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(//gmd:dataQualityInfo/*/gmd:lineage/*/gmd:statement/gco:CharacterString/text())"));


		/*
			Dates
		*/

		// Datestamp
		extractors.add(createXPathExtractor(
				ResultMetadataFields.DATESTAMP,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"//gmd:MD_Metadata/gmd:dateStamp/*/text()"));

		// Published date
		extractors.add(createXPathExtractor(
				ResultMetadataFields.DATE_PUBLISHED,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(//gmd:identificationInfo/*/gmd:citation/*/gmd:date/*/gmd:date/gco:Date/text())"));

		// Get all identification info dates
		extractors.add(createXPathExtractor(
				ResultMetadataFields.DATE_IDENTIFICATION_INFO,
				new DateXmlCustomExtractor(customExtractorExceptionThrowingConfig.getOrDefault(ResultMetadataFields.DATE_IDENTIFICATION_INFO, DEFAULT_CUSTOM_EXTRACTOR_IS_EXCEPTION_THROW)),
				"(//gmd:identificationInfo/*/gmd:citation/*/gmd:date)"));

		// maintenance frequency

		extractors.add(createXPathExtractor(
				ResultMetadataFields.MAINENANCE_FREQUENCY,
				new CodeListValueXmlCustomExtractor(customExtractorExceptionThrowingConfig.getOrDefault(ResultMetadataFields.MAINENANCE_FREQUENCY, DEFAULT_CUSTOM_EXTRACTOR_IS_EXCEPTION_THROW)),
				"(//gmd:identificationInfo/*/gmd:resourceMaintenance/*/gmd:maintenanceAndUpdateFrequency/*[@codeListValue])"));

		/*
			Organisations
		 */

		// Organisation names + roles
		extractors.add(createXPathExtractor(
				ResultMetadataFields.ORGANISATIONS,
				new ResponsiblePartyXmlCustomExtractor(customExtractorExceptionThrowingConfig.getOrDefault(ResultMetadataFields.ORGANISATIONS, DEFAULT_CUSTOM_EXTRACTOR_IS_EXCEPTION_THROW)),
				"//gmd:MD_Metadata/gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty"));


		extractors.add(createXPathExtractor(
				ResultMetadataFields.ORGANISATIONS_METADATA,
				new ResponsiblePartyXmlCustomExtractor(customExtractorExceptionThrowingConfig.getOrDefault(ResultMetadataFields.ORGANISATIONS_METADATA, DEFAULT_CUSTOM_EXTRACTOR_IS_EXCEPTION_THROW)),
				"//gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty"));

		/*
			Language data
		 */
		// Metadata language
		extractors.add(createXPathExtractor(
				ResultMetadataFields.LANGUAGE_METADATA,
				new LanguageXmlCustomExtractor(customExtractorExceptionThrowingConfig.getOrDefault(ResultMetadataFields.LANGUAGE_METADATA, DEFAULT_CUSTOM_EXTRACTOR_IS_EXCEPTION_THROW)),
				"(/csw:GetRecordByIdResponse/*/gmd:language)"));

		//Resource language
		extractors.add(createXPathExtractor(
				ResultMetadataFields.LANGUAGE_RESOURCE,
				new LanguageXmlCustomExtractor(customExtractorExceptionThrowingConfig.getOrDefault(ResultMetadataFields.LANGUAGE_RESOURCE, DEFAULT_CUSTOM_EXTRACTOR_IS_EXCEPTION_THROW)),
				"(//gmd:identificationInfo/*/gmd:language)"));



		/*
		 Constraints
		*/
		// use limitation
		extractors.add(createXPathExtractor(
				ResultMetadataFields.CONSTRAINT_USE_LIMITATION,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(//gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:useLimitation/gco:CharacterString/text())"));

		// access limitation
		extractors.add(createXPathExtractor(
				ResultMetadataFields.CONSTRAINT_ACCESS,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(//gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue)"));

		extractors.add(createXPathExtractor(
				ResultMetadataFields.CONSTRAINT_OTHER,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(//gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:otherConstraints/gco:CharacterString/text())"));

		/*
			Spatial data
		 */
		final String XPATH_CRS_ROOT = "//gmd:referenceSystemInfo/*/gmd:referenceSystemIdentifier";
		// CRS code
		extractors.add(createXPathExtractor(
				ResultMetadataFields.CRS_CODE,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(" +
						XPATH_CRS_ROOT +
						"/*/gmd:code/gco:CharacterString/text())"));

		// CRS code space
		extractors.add(createXPathExtractor(
				ResultMetadataFields.CRS_CODE_SPACE,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(" +
						XPATH_CRS_ROOT +
						"/*/gmd:codeSpace/gco:CharacterString/text())"));

		// CRS version
		extractors.add(createXPathExtractor(
				ResultMetadataFields.CRS_VERSION,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(" +
						XPATH_CRS_ROOT +
						"/*/gmd:version/gco:CharacterString/text())"));
		// Scale data
		extractors.add(createXPathExtractor(
				ResultMetadataFields.SCALE_DENOMINATOR,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(//gmd:identificationInfo/*/gmd:spatialResolution/*/gmd:equivalentScale/*/gmd:denominator/gco:Integer/text())"));

		// Extent
		// North / Top
		final String XPATH_EXTENT_ROOT = "//gmd:identificationInfo/*/*[self::gmd:extent or self::srv:extent]/*/gmd:geographicElement";


		// Geographic bounding box
		extractors.add(createXPathExtractor(
				ResultMetadataFields.GEOGRAPHIC_BOUNDING_BOX,
				new GeographicBoundingBoxXmlCustomExtractor(customExtractorExceptionThrowingConfig.getOrDefault(ResultMetadataFields.GEOGRAPHIC_BOUNDING_BOX, DEFAULT_CUSTOM_EXTRACTOR_IS_EXCEPTION_THROW)),
				"//gmd:MD_Metadata/gmd:identificationInfo/*/*[self::gmd:extent or self::srv:extent]/*/gmd:geographicElement/gmd:EX_GeographicBoundingBox"));

		extractors.add(createXPathExtractor(
				ResultMetadataFields.EXTENT_NORTH_BOUND,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(" +
						XPATH_EXTENT_ROOT +
						"/*/gmd:northBoundLatitude/gco:Decimal/text())"));

		// South / bottom
		extractors.add(createXPathExtractor(
				ResultMetadataFields.EXTENT_SOUTH_BOUND,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(" +
						XPATH_EXTENT_ROOT +
						"/*/gmd:southBoundLatitude/gco:Decimal/text())"));

		// East / right
		extractors.add(createXPathExtractor(
				ResultMetadataFields.EXTENT_EAST_BOUND,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(" +
						XPATH_EXTENT_ROOT +
						"/*/gmd:eastBoundLongitude/gco:Decimal/text())"));

		// West / left
		extractors.add(createXPathExtractor(
				ResultMetadataFields.EXTENT_WEST_BOUND,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(" +
						XPATH_EXTENT_ROOT +
						"/*/gmd:westBoundLongitude/gco:Decimal/text())"));


		// Classification
		// List of possible values found at http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_ClassificationCode
		extractors.add(createXPathExtractor(
				ResultMetadataFields.CLASSIFICATION,
				FieldExtractorType.FIRST_MATCHING_VALUE,
				"(//gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:classification/*/@codeListValue)"));

		extractors.add(createXPathExtractor(
				ResultMetadataFields.DOWNLOAD_LINKS,
				new DownloadLinksXmlCustomExtractor(customExtractorExceptionThrowingConfig.getOrDefault(ResultMetadataFields.DOWNLOAD_LINKS, DEFAULT_CUSTOM_EXTRACTOR_IS_EXCEPTION_THROW)),
				"(//gmd:distributionInfo/*/gmd:transferOptions/*/gmd:onLine)"));

		extractors.add(createXPathExtractor(
				ResultMetadataFields.SERVICE_ASSOCIATED_RESOURCES,
				new ServiceAssociatedResourcesXmlCustomExtractor(customExtractorExceptionThrowingConfig.getOrDefault(ResultMetadataFields.SERVICE_ASSOCIATED_RESOURCES, DEFAULT_CUSTOM_EXTRACTOR_IS_EXCEPTION_THROW)),
				"(//gmd:identificationInfo/*/srv:operatesOn)"));


		// Additional extractors for other documents
		// A resources additional resources need to be queried in a separate query, so its included under a separate topic/group
		// Associated resources
		extractors.add(createXPathExtractor(
				ResultMetadataFields.ADDITIONAL.ASSOCIATED_RESOURCES,
				new AssociatedResourcesXmlCustomExtractor(customExtractorExceptionThrowingConfig.getOrDefault(ResultMetadataFields.ADDITIONAL.ASSOCIATED_RESOURCES, DEFAULT_CUSTOM_EXTRACTOR_IS_EXCEPTION_THROW)),
				"(//csw:SearchResults/csw:BriefRecord)"));

		return configuration;
	}


	@Override
	public DocumentProcessor createMetadataDocumentProcessor() throws ParserConfigurationException {
		return createMetadataDocumentProcessor(null);
	}

	@Override
	public DocumentProcessor createMetadataDocumentProcessor(DocumentProcessingConfiguration configuration) throws ParserConfigurationException {
		if (configuration == null ) {
			configuration = createMetadataDocumentProcessingConfiguration();
		}
		return getDocumentProcessorFactory().createXmlProcessor(configuration);
	}
}
