package fi.maanmittauslaitos.pta.search.metadata.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 Organisation name has been deprecated in favor of partName, since the responsible party might not always be an organisation.
 SYKE CKAN data is an example of this since, the responsible party is generally a person - or the contact details refer to a person.

 While we offer both APIs, there will be double fields in the Elasticsearch index, which isn't desirable.

 */
// TODO Remove usages and references to organisation name elsewhere in favor of partyName. See comment above for details
public class ResponsibleParty {
	private String isoRole;
	private List<String> email;
	private String partyName;
	private Map<String, String> localizedPartyName;

	public ResponsibleParty() {
		email = new ArrayList<>();
		localizedPartyName = new HashMap<>();
	}

	public ResponsibleParty(ResponsibleParty responsibleParty) {
		if (responsibleParty != null) {
			this.email = responsibleParty.getEmail();
			this.partyName = responsibleParty.getPartyName();
			this.isoRole = responsibleParty.getIsoRole();
			this.localizedPartyName = responsibleParty.getLocalizedPartyName();
		}
	}

	/**
	 * Deprecated in favor of more general semantics.
	 *
	 * @deprecated use {@link #setPartyName(String)} ()} instead.
	 */
	@Deprecated
	public void setOrganisationName(String organisationName) {
		setPartyName(organisationName);
	}

	/**
	 * Deprecated in favor of more general semantics.
	 *
	 * @deprecated use {@link #getPartyName()} ()} instead.
	 */
	@Deprecated
	public String getOrganisationName() {
		return getPartyName();
	}

	/**
	 * Deprecated in favor of more general semantics.
	 *
	 * @deprecated use {@link #setLocalizedPartyName(Map)} ()} instead.
	 */
	@Deprecated
	public void setLocalisedOrganisationName(Map<String, String> localisedOrganisationName) {
		setLocalizedPartyName(localisedOrganisationName);
	}

	/**
	 * Deprecated in favor of more general semantics.
	 *
	 * @deprecated use {@link #getLocalizedPartyName()} instead.
	 */
	@Deprecated
	public Map<String, String> getLocalisedOrganisationName() {
		return getLocalizedPartyName();
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

	public String getPartyName() {
		return partyName;
	}

	public void setPartyName(String partyName) {
		this.partyName = partyName;
	}

	public Map<String, String> getLocalizedPartyName() {
		return localizedPartyName;
	}

	public void setLocalizedPartyName(Map<String, String> localizedPartyName) {
		this.localizedPartyName = localizedPartyName;
	}
}
