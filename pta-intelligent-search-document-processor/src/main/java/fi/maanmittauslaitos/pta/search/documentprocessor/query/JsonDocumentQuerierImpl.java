package fi.maanmittauslaitos.pta.search.documentprocessor.query;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.JsonDocument;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JsonDocumentQuerierImpl implements DocumentQuerier {
	private Configuration configuration;
	private TypeRef<List<String>> typeRef = new TypeRef<List<String>>() {
	};

	private JsonDocumentQuerierImpl(Configuration configuration) {
		this.configuration = configuration;
	}

	public static JsonDocumentQuerierImpl create(Configuration configuration) {
		return new JsonDocumentQuerierImpl(configuration);
	}

	@Override
	public List<QueryResult> process(String query, Document document) throws DocumentProcessingException {
		if (!(document instanceof JsonDocument)) {
			throw new DocumentProcessingException("Document was not instance of JsonDocument");
		}
		List<QueryResult> results;
		try {
			results = JsonPath.using(configuration)
					.parse(((JsonDocument) document).getContent())
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
