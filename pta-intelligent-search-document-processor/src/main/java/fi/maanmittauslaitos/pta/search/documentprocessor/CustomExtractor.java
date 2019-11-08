package fi.maanmittauslaitos.pta.search.documentprocessor;

import fi.maanmittauslaitos.pta.search.documentprocessor.query.DocumentQuery;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.QueryResult;

import javax.xml.xpath.XPathException;

@FunctionalInterface
public interface CustomExtractor {

	Object process(DocumentQuery documentQuery, QueryResult queryResult) throws XPathException, DocumentProcessingException;
}
