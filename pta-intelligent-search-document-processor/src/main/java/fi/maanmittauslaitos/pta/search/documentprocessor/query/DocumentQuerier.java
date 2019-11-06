package fi.maanmittauslaitos.pta.search.documentprocessor.query;


import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;

import java.util.List;

public interface DocumentQuerier {

	List<QueryResult> process(String query, Document document) throws DocumentProcessingException;

	List<QueryResult> process(String query, QueryResult queryResult) throws DocumentProcessingException;
}
