package fi.maanmittauslaitos.pta.search.metadata.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponsibleParty {
	private String organisationName;
	private String isoRole;
	private Map<String, String> localisedOrganisationName;
	private List<String> email;

	public ResponsibleParty() {
		email = new ArrayList<>();
		localisedOrganisationName = new HashMap<>();
	}

	public ResponsibleParty(ResponsibleParty responsibleParty) {
		if (responsibleParty != null) {
			this.email = responsibleParty.getEmail();
			this.organisationName = responsibleParty.getOrganisationName();
			this.isoRole = responsibleParty.getIsoRole();
			this.localisedOrganisationName = responsibleParty.getLocalisedOrganisationName();
		}
	}

	public void setOrganisationName(String organisationName) {
		this.organisationName = organisationName;
	}
	
	public String getOrganisationName() {
		return organisationName;
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

	public List<String> getEmail() {
		return email;
	}

	public void setEmail(List<String> email) {
		this.email = email;
	}

	public void addEmail(String email){
		if(email != null){
			this.email.add(email);
		}
	}
}
