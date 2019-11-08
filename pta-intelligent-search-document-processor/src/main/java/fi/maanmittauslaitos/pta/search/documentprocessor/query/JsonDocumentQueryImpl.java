package fi.maanmittauslaitos.pta.search.documentprocessor.query;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.JsonDocument;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JsonDocumentQueryImpl implements DocumentQuery {
	private Configuration configuration;
	private TypeRef<List<String>> typeRef = new TypeRef<List<String>>() {
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
		try {
			results = ((JsonDocument) document).getDocumentContext()
					.read(query, typeRef)
					.stream()
					.filter(Objects::nonNull)
					.map(JsonQueryResultImpl::create)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new DocumentProcessingException(e);
		}
		return results;
	}

	@Override
	public List<QueryResult> process(String query, QueryResult queryResult) {
		//TODO: is this needed?
		return null;
	}
}
