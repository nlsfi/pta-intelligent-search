package fi.maanmittauslaitos.pta.search.documentprocessor.query;

public class JsonQueryResultImpl implements QueryResult {
	String value;

	private JsonQueryResultImpl(String value) {
		this.value = value;
	}

	public static JsonQueryResultImpl create(String value) {
		return new JsonQueryResultImpl(value);
	}

	@Override
	public String getValue() {
		return value;
	}
}
