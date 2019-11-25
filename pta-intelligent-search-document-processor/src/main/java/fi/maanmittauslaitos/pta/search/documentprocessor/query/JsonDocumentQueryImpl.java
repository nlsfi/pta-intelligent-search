package fi.maanmittauslaitos.pta.search.documentprocessor.query;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.mapper.MappingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.JsonDocument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class JsonDocumentQueryImpl implements DocumentQuery {
	private final Configuration configuration;
	private final TypeRef<List<String>> typeRef = new TypeRef<List<String>>() {
	};

	private JsonDocumentQueryImpl(Configuration configuration) {
		this.configuration = configuration;
	}

	public static JsonDocumentQueryImpl create(Configuration configuration) {
		return new JsonDocumentQueryImpl(configuration);
	}

	public DocumentContext parseJsonString(String content) {
		return JsonPath.using(configuration).parse(content);
	}

	@Override
	public List<QueryResult> process(String query, Document document) throws DocumentProcessingException {
		if (!(document instanceof JsonDocument)) {
			throw new DocumentProcessingException("Document was not instance of JsonDocument");
		}
		List<QueryResult> results;
		DocumentContext ctx = ((JsonDocument) document).getDocumentContext();

		List<Object> rawValue = ctx.read(query);

		try {
			results = ctx
					.read(query, typeRef)
					.stream()
					.filter(Objects::nonNull)
					.map((String value) -> JsonQueryResultImpl.create(value, rawValue))
					.collect(Collectors.toList());
		} catch (MappingException e) {
			// In case of mapping exception, just store the raw value and let post processing take care of it
			results = Collections.singletonList(JsonQueryResultImpl.create(null, rawValue));
		} catch (IllegalArgumentException e) {
			results = new ArrayList<>();
		}
		return results;
	}

	@Override
	public List<QueryResult> process(String query, QueryResult queryResult) throws DocumentProcessingException {
		if (!(queryResult instanceof JsonQueryResultImpl)) {
			throw new DocumentProcessingException("This method is only meant for Json objects");
		}

		try {
			return ((JsonQueryResultImpl) queryResult).getRawValue().stream()
					.filter(Objects::nonNull)
					.map(this::flatten)
					.flatMap(Collection::stream)
					.filter(Objects::nonNull)
					.map(this::flatten)
					.flatMap(Collection::stream)
					.map(o -> JsonQueryResultImpl.create(String.valueOf(o), Collections.singletonList(o)))
					.collect(Collectors.toList());
		} catch (RuntimeException e) {
			throw new DocumentProcessingException(e);
		}
	}

	private Collection flatten(Object o) {
		// The child can be list, map, string or number
		if (o instanceof List) {
			return (List) o;
		} else if (o instanceof Map) {
			return ((Map) o).values();
		}
		return Collections.singleton(o);
	}
}
