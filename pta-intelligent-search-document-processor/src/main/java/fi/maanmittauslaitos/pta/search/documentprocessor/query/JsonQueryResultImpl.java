package fi.maanmittauslaitos.pta.search.documentprocessor.query;

import java.util.List;

public class JsonQueryResultImpl implements QueryResult {
	private final List<Object> rawValue;
	private final String value;


	private JsonQueryResultImpl(String value, List<Object> rawValue) {
		this.value = value;
		this.rawValue = rawValue;
	}

	public static JsonQueryResultImpl create(String value, List<Object> rawValue) {
		return new JsonQueryResultImpl(value, rawValue);
	}

	@Override
	public String getValue() {
		return value;
	}

	public List<Object> getRawValue() {
		return rawValue;
	}

	public List getGenericRawValue() {
		return rawValue;
	}
}
