package fi.maanmittauslaitos.pta.search.metadata.json.extractor;

import fi.maanmittauslaitos.pta.search.documentprocessor.CustomExtractor;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.DocumentQuery;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.JsonDocumentQueryImpl;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.QueryResult;
import fi.maanmittauslaitos.pta.search.metadata.model.NonThrowingCustomExtractor;

public abstract class JsonPathCustomExtractor extends NonThrowingCustomExtractor implements CustomExtractor {

    public JsonPathCustomExtractor() {
        super();
    }

    public JsonPathCustomExtractor(boolean isThrowException) {
        super(isThrowException);
    }

    public abstract Object process(JsonDocumentQueryImpl query, QueryResult queryResult) throws DocumentProcessingException;

    @Override
    public Object process(DocumentQuery documentQuery, QueryResult queryResult) throws DocumentProcessingException {
        if (!(documentQuery instanceof JsonDocumentQueryImpl)) {
            throw new DocumentProcessingException("This extractor should only be used for JSON Documents");
        }

        return process((JsonDocumentQueryImpl) documentQuery, queryResult);
    }
}
