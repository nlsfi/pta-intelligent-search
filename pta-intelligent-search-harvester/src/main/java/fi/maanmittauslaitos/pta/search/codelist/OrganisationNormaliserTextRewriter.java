package fi.maanmittauslaitos.pta.search.codelist;

import fi.maanmittauslaitos.pta.search.metadata.model.TextRewriter;

public class OrganisationNormaliserTextRewriter implements TextRewriter {
	private OrganisationNormaliser organisationNormaliser;
	
	public void setOrganisationNormaliser(OrganisationNormaliser organisationNormaliser) {
		this.organisationNormaliser = organisationNormaliser;
	}
	
	public OrganisationNormaliser getOrganisationNormaliser() {
		return organisationNormaliser;
	}
	
	@Override
	public String rewrite(String name) {
		String ret = getOrganisationNormaliser().getCanonicalOrganisationName(name, "fi");
		
		if (ret == null) {
			ret = getOrganisationNormaliser().getCanonicalOrganisationName(name, "sv");
		}
		
		if (ret == null) {
			ret = getOrganisationNormaliser().getCanonicalOrganisationName(name, "en");
		}

		if (ret == null) {
			ret = name;
		}
		
		return ret;
	}

	@Override
	public String rewrite(String name, String language) {
		language = language.toLowerCase();
		String value = getOrganisationNormaliser().getCanonicalOrganisationName(name, language);
		if (value == null) {
			return name;
		}

		return value;
	}

}
