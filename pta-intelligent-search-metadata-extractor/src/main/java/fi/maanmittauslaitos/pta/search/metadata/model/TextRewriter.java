package fi.maanmittauslaitos.pta.search.metadata.model;

public interface TextRewriter {
	/**
	 * Rewrite name without knowledge of the language
	 * 
	 * @param name The rewritten name
	 * @return
	 */
	public String rewrite(String name);
	
	/**
	 * Rewrite name in a known language
	 * 
	 * @param name The rewritten name
	 * @param language
	 * @return
	 */
	public String rewrite(String name, String language);
}
