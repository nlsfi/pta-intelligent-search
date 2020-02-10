package fi.maanmittauslaitos.pta.search.metadata.json.extractor;

import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.ListCustomExtractor;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.DocumentQuery;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.JsonDocumentQueryImpl;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.QueryResult;
import fi.maanmittauslaitos.pta.search.metadata.model.NonThrowingCustomExtractor;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class JsonPathListCustomExtractor extends NonThrowingCustomExtractor implements ListCustomExtractor {

    public JsonPathListCustomExtractor() {
        super();
    }

    public JsonPathListCustomExtractor(boolean isThrowException) {
        super(isThrowException);
    }

    public abstract Object process(JsonDocumentQueryImpl query, List<QueryResult> queryResult) throws DocumentProcessingException;

    protected static final String DEFAULT_PARSED_VALUE = "";


    protected String getValueSafishly(Map<String, Object> map, String key, String defaultValue) {
        Object obj = map.getOrDefault(key, defaultValue);
        return Objects.toString(obj);
    }

    @Override
    public Object process(DocumentQuery documentQuery, List<QueryResult> queryResults) throws DocumentProcessingException {
        if (!(documentQuery instanceof JsonDocumentQueryImpl)) {
            throw new DocumentProcessingException("This extractor should only be used for JSON Documents");
        }

        return process((JsonDocumentQueryImpl) documentQuery, queryResults);
    }
}
