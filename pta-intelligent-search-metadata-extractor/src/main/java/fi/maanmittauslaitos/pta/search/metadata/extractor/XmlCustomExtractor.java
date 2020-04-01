package fi.maanmittauslaitos.pta.search.metadata.extractor;

import fi.maanmittauslaitos.pta.search.documentprocessor.CustomExtractor;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.DocumentQuery;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.QueryResult;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.XmlDocumentQueryImpl;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.XmlQueryResultImpl;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathException;

public abstract class XmlCustomExtractor implements CustomExtractor {

    public abstract Object process(XPath xPath, Node node) throws DocumentProcessingException;

    @Override
    public Object process(DocumentQuery documentQuery, QueryResult queryResult) throws XPathException, DocumentProcessingException {
        if (!(documentQuery instanceof XmlDocumentQueryImpl)) {
            throw new DocumentProcessingException("This extractor should only be used for XML Documents");
        }

        return process(((XmlDocumentQueryImpl) documentQuery).getxPath(), ((XmlQueryResultImpl) queryResult).getNode());
    }
}
