package fi.maanmittauslaitos.pta.search.metadata.utils;

public class XPathHelper {

	private static final String TO_LOWER = "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'";

	public static String matches(String attribute, String value) {
		return String.format("translate(%s, " + TO_LOWER + ")=%s", attribute, value.toLowerCase());
	}

	public static String doesntMatch(String attribute, String value) {
		return String.format("translate(%s, " + TO_LOWER + ")!=%s", attribute, value.toLowerCase());
	}

	/**
	 * Matches that the given attribute's value ends with the given value
	 * @param attribute the attribute which value you want to match to
	 * @param value to match to. Note that the given value should be wrapped in single quotes,
	 *                 i.e. to match the end of the attribute's value to "foobar", you need to pass "'foobar'"
	 * @param isIgnoreCase defines if case should be ignored when matching
	 * @return
	 */
	public static String endsWith(String attribute, String value, boolean isIgnoreCase) {
		String endString = String.format("substring(%s, string-length(%s) - string-length(%s) + 1)", attribute, attribute, value);

		return isIgnoreCase ? matches(endString, value) : String.format("%s = %s", endString, value);
	}
}
