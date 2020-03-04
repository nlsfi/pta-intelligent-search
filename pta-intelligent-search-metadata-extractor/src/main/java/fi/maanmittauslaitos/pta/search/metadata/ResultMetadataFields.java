package fi.maanmittauslaitos.pta.search.metadata;

public class ResultMetadataFields {
	public static final String ID = "@id";
	
	public static final String DATESTAMP = "datestamp";
	
	public static final String TITLE = "title";
	public static final String TITLE_SV = "title_sv";
	public static final String TITLE_EN = "title_en";
	
	public static final String ABSTRACT = "abstract";
	public static final String ABSTRACT_SV = "abstract_sv";
	public static final String ABSTRACT_EN = "abstract_en";
	
	// TODO: Kai voisi auttaa siinä miten tätä kannttaa tulkita
	// TODO: olisiko [{ name:'foo', role:'bar', section:'zonk' }]
	
	public static final String ORGANISATIONS = "organisations";
	
	public static final String TOPIC_CATEGORIES = "topicCategories";
	
	public static final String KEYWORDS_ALL = "keywords";
	public static final String KEYWORDS_INSPIRE = "keywordsInspire";
	
	public static final String DISTRIBUTION_FORMATS = "distributionFormats";
	
	public static final String GEOGRAPHIC_BOUNDING_BOX = "geographicBoundingBox";
	
	public static final String IS_SERVICE = "isService";
	public static final String IS_DATASET = "isDataset";
	public static final String IS_AVOINDATA = "isAvoindata";
	
	// HUOM! On vielä epäselvää kannattaako tämä mallintaa samalla tavalla 
	// kuin IS_SERVICE, IS_DATASET ja IS_AVOINDATA
	public static final String IS_PTA = "isPtaAineisto";

	public static final String IMAGE_OVERVIEW_URL = "imageOverviewUrl";
	public static final String RESOURCE_ID = "resourceId";
	public static final String LINEAGE = "lineage";
	public static final String CRS_CODE = "crsCode";
	public static final String CRS_CODE_SPACE = "crsCodeSpace";
	public static final String CRS_VERSION= "crsVersion";
	public static final String DATE_PUBLISHED = "datePublished";
	public static final String CONSTRAINT_USE_LIMITATION = "constraintUseLimitation";
	public static final String CONSTRAINT_ACCESS =  "constraintAccess";
	public static final String CONSTRAINT_OTHER =  "constraintOther";
	public static final String LANGUAGE_METADATA = "languageMetadata";
	public static final String LANGUAGE_RESOURCE = "languageResource";
	public static final String SCALE_DENOMINATOR = "scaleDenominator";
	public static final String DOWNLOAD_LINKS = "downloadLinks";
	public static final String CLASSIFICATION = "classification";
	public static final String SERVICE_ASSOCIATED_RESOURCES = "serviceAssociatedResources";
	public static final String ORGANISATIONS_METADATA = "organisationsMetadata";
	public static final String ORGANISATIONS_RESOURCE = "organisationsResource";
	public static final String ORGANISATIONS_OTHER = "organisationsOther";
	public static final String MAINTENANCE_FREQUENCY = "maintenanceFrequency";
	public static final String DATE_IDENTIFICATION_INFO = "dateIdentificationInfo";
	public static final String CKAN_CREATION_DATE = "ckanDateCreation";

	/**
	 * Fields relating to metadata, but not found in the normal query response
	 */
	public class ADDITIONAL {
		public static final String ASSOCIATED_RESOURCES = "associatedResources";
	}
	
}
