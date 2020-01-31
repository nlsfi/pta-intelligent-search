package fi.maanmittauslaitos.pta.search.metadata.utils;

public class StringUtils {

    /**
     * Copy of commons-lang String utils method. Copied to reduce jar size.
     * @param cs
     * @return
     */
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
}
