package fi.maanmittauslaitos.pta.search.api;

public enum Language {
	FI, SV, EN;
	
	public String getFieldPostfix() {
		String asString = this.toString();
		String ret = asString.charAt(0) + asString.substring(1).toLowerCase();
		return ret;
	}
}
