package fi.maanmittauslaitos.pta.search.documentprocessor;

import java.util.List;
import java.util.Map;

public interface Document {
	Map<String, Object> getFields();

	<T> List<T> getListValue(String field, Class<T> type);

	<T> T getValue(String field, Class<T> type);

	boolean isFieldTrue(String field);
}
