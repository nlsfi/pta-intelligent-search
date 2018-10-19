package fi.maanmittauslaitos.pta.search.api;

public enum Language {
	FI("fi"), SV("sv"), EN("en");
	
	private final String lowerCaseCode;
	
	private Language(String lowerCaseCode) {
		this.lowerCaseCode = lowerCaseCode;
	}
	
	public String getFieldPostfix() {
		String asString = this.toString();
		String ret = asString.charAt(0) + asString.substring(1).toLowerCase();
		return ret;
	}
	
	public String getLowercaseLanguageCode() {
		return this.lowerCaseCode;
	}
}
