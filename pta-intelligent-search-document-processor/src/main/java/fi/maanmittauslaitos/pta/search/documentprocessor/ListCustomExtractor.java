package fi.maanmittauslaitos.pta.search.documentprocessor;

import fi.maanmittauslaitos.pta.search.documentprocessor.query.DocumentQuery;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.QueryResult;

import javax.xml.xpath.XPathException;
import java.util.List;

@FunctionalInterface
public interface ListCustomExtractor {

	Object process(DocumentQuery documentQuery, List<QueryResult> queryResults) throws XPathException, DocumentProcessingException;

}
