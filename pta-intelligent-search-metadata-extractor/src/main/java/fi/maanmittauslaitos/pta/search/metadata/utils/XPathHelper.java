package fi.maanmittauslaitos.pta.search.metadata.utils;

public class XPathHelper {

	private static final String TO_LOWER = "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'";

	public static String matches(String attribute, String value) {
		return String.format("translate(%s, " + TO_LOWER + ")=%s", attribute, value.toLowerCase());
	}

	public static String doesntMatch(String attribute, String value) {
		return String.format("translate(%s, " + TO_LOWER + ")!=%s", attribute, value.toLowerCase());
	}
}
