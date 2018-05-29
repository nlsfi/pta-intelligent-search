package fi.maanmittauslaitos.pta.search.metadata.model;

public class ResponsibleParty {
	private String organisationName;
	private String isoRole;
	
	public void setOrganisationName(String organisationName) {
		this.organisationName = organisationName;
	}
	
	public String getOrganisationName() {
		return organisationName;
	}
	
	public void setIsoRole(String isoRole) {
		this.isoRole = isoRole;
	}
	
	public String getIsoRole() {
		return isoRole;
	}
}
