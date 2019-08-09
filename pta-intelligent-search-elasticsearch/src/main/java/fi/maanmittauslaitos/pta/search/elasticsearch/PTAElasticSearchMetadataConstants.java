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
	 * Name of the source field that contains ontology terms (as uris) that are the parents
	 * of the terms annotated by maui from the abstract field
	 */
	public static final String FIELD_ABSTRACT_MAUI_URI_PARENTS = "abstract_maui_uri_parents";
	
	/**
	 * Name of the source field where ontology terms (as uris) matched from the abstract
	 * are stored
	 */
	public static final String FIELD_ABSTRACT_URI = "abstract_uri";
	
	/**
	 * Name of the source field that contains ontology terms (as uris) that are the parents
	 * of the terms found in the abstract
	 */
	public static final String FIELD_ABSTRACT_URI_PARENTS = "abstract_uri_parents";
	
	/**
	 * Name of the source field where ontology terms (as uris) matched from the keywords
	 * are stored 
	 */
	public static final String FIELD_KEYWORDS_URI = "keywords_uri";

	/**
	 * Name of the source object where names and scores of best matching country, region, sub region and municipality
	 * are stored
	 */
	public static final String FIELD_BEST_MACTCHING_REGIONS = "bestMatchingRegion";

	/**
	 * Name of the source field where the name of best matching country, region, sub region or municipality
	 * is stored
	 */
	public static final String FIELD_BEST_MACTCHING_REGIONS_NAME = "location_name";

	/**
	 * Name of the source field where the score of best matching country, region, sub region or municipality
	 * is stored
	 */
	public static final String FIELD_BEST_MACTCHING_REGIONS_SCORE = "location_score";
	
}
