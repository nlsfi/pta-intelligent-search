package fi.maanmittauslaitos.pta.search.metadata;

public class ISOMetadataFields {
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
	
	public static final String IS_SERVICE = "isService";
	public static final String IS_DATASET = "isDataset";
	public static final String IS_AVOINDATA = "isAvoindata";
	
	// HUOM! On vielä epäselvää kannattaako tämä mallintaa samalla tavalla 
	// kuin IS_SERVICE, IS_DATASET ja IS_AVOINDATA
	public static final String IS_PTA = "isPta";
	
}
