package fi.maanmittauslaitos.pta.search.codelist;

public interface InspireThemes {
	public void setCanonicalLanguage(String canonicalLanguage);
	public String getCanonicalLanguage();
	public String getCanonicalName(String text, String language);
	
	// Try to guess the language
	public String getCanonicalName(String text);
}
