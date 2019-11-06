package fi.maanmittauslaitos.pta.search.documentprocessor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlDocument implements Document {
	private Map<String, Object> fields = new HashMap<>();
	private org.w3c.dom.Document dom;

	@Override
	public Map<String, Object> getFields() {
		return fields;
	}

	public void setFields(Map<String, Object> fields) {
		this.fields = fields;
	}

	public org.w3c.dom.Document getDom() {
		return dom;
	}

	public void setDom(org.w3c.dom.Document dom) {
		this.dom = dom;
	}

	@Override
	public <T> List<T> getListValue(String field, Class<T> type) {
		Object val = getFields().get(field);
		if (val == null) {
			return null;
		}

		List<T> ret = new ArrayList<>();
		if (val instanceof Iterable) {
			@SuppressWarnings("rawtypes")
			Iterable<?> iterable = (Iterable) val;
			for (Object o : iterable) {
				T converted = convertObject(o, type);
				ret.add(converted);
			}

		} else {
			ret.add(convertObject(val, type));

		}
		return ret;
	}

	@Override
	public <T> T getValue(String field, Class<T> type) {
		Object val = getFields().get(field);
		if (val == null) {
			return null;
		}

		if (val instanceof Collection) {
			@SuppressWarnings("rawtypes")
			Collection<?> c = (Collection) val;
			if (c.size() == 0) {
				return null;
			}
			if (c.size() > 1) {
				throw new IllegalArgumentException("Value in field " + field + " is a collection with " + c.size() + " entries instead of 1, cannot get singular value");
			}
			val = c.iterator().next();
		}

		return convertObject(val, type);
	}

	@SuppressWarnings("unchecked")
	private <T> T convertObject(Object o, Class<T> type) {
		T converted;
		if (o == null) {
			converted = null;

		} else if (type.isAssignableFrom(o.getClass())) {
			converted = (T) o;

		} else if (type.isAssignableFrom(LocalDateTime.class)) {
			String asString = o.toString();
			converted = (T) LocalDateTime.parse(asString);

		} else if (type.isAssignableFrom(OffsetDateTime.class)) {
			String asString = o.toString();
			converted = (T) OffsetDateTime.parse(asString);

		} else if (type == String.class) {
			converted = (T) o.toString();

		} else {
			throw new IllegalArgumentException("Cannot convert " + o.getClass() + " into " + type.getClass());
		}
		return converted;
	}

	@Override
	public boolean isFieldTrue(String field) {
		Boolean value = (Boolean) getFields().get(field);
		if (value == null) {
			return false;
		}

		return value.booleanValue();
	}
}
