package fi.maanmittauslaitos.pta.search.metadata.model;

import java.util.HashMap;
import java.util.Map;

public class ResponsibleParty {
	private String organisationNameDefaultLanguage;
	private String isoRole;
	private Map<String, String> localisedOrganisationName = new HashMap<>();
	
	public void setOrganisationNameDefaultLanguage(String organisationNameDefaultLanguage) {
		this.organisationNameDefaultLanguage = organisationNameDefaultLanguage;
	}
	
	public String getOrganisationNameDefaultLanguage() {
		return organisationNameDefaultLanguage;
	}
	
	public void setLocalisedOrganisationName(Map<String, String> localisedOrganisationName) {
		this.localisedOrganisationName = localisedOrganisationName;
	}
	
	public Map<String, String> getLocalisedOrganisationName() {
		return localisedOrganisationName;
	}
	
	public void setIsoRole(String isoRole) {
		this.isoRole = isoRole;
	}
	
	public String getIsoRole() {
		return isoRole;
	}
}
