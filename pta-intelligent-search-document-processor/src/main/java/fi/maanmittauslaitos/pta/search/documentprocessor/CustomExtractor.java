package fi.maanmittauslaitos.pta.search.documentprocessor;

import fi.maanmittauslaitos.pta.search.documentprocessor.query.DocumentQuerier;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.QueryResult;

import javax.xml.xpath.XPathException;

@FunctionalInterface
public interface CustomExtractor {

	Object process(DocumentQuerier documentQuerier, QueryResult queryResult) throws XPathException, DocumentProcessingException;
}
