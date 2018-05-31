package fi.maanmittauslaitos.pta.search.elasticsearch;

public class PTAElasticSearchMetadataConstants {
	/**
	 * Name of the ElasticSearch index
	 */
	public static final String INDEX = "pta";
	
	/**
	 * Name of the document type used for storing documents in
	 */
	public static final String TYPE = "metadata";
	
	/**
	 * Name of the source field where ontology terms (as uris) identified by maui from
	 * the abstract are stored 
	 */
	public static final String FIELD_ABSTRACT_MAUI_URI = "abstract_maui_uri";
	
	/**
	 * Name of the source field where ontology terms (as uris) matched from the abstract
	 * are stored
	 */
	public static final String FIELD_ABSTRACT_URI = "abstract_uri";
	
	/**
	 * Name of the source field where ontology terms (as uris) matched from the keywords
	 * are stored 
	 */
	public static final String FIELD_KEYWORDS_URI = "keywords_uri";
	
}
