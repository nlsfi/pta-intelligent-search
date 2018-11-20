package fi.maanmittauslaitos.pta.search.codelist;

public interface OrganisationNormaliser {

	/**
	 * Returns a canonical name for the organisation. If the supplied organisation name
	 * is the canonical name, the canonical name should be returned.
	 *  
	 * @param orgName Organisation name as it is written in the metadata document
	 * @param language The language of this organisation name 
	 * @return Canonical name of the organisation or null if no canonical name found
	 */
	public String getCanonicalOrganisationName(String orgName, String language);
}
