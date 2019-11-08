package fi.maanmittauslaitos.pta.search.documentprocessor;

@FunctionalInterface
public interface ExtractorTrimmer {

	String trim(String value);
}
