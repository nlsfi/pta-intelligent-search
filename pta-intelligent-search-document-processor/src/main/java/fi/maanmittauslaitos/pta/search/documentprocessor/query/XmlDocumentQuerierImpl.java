package fi.maanmittauslaitos.pta.search.documentprocessor.query;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.XmlDocument;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class XmlDocumentQuerierImpl implements DocumentQuerier {

	private final XPath xPath;

	private XmlDocumentQuerierImpl(XPath xPath) {
		this.xPath = xPath;
	}

	public static XmlDocumentQuerierImpl create(XPath xPath) {
		return new XmlDocumentQuerierImpl(xPath);
	}

	public XPath getxPath() {
		return xPath;
	}

	@Override
	public List<QueryResult> process(String query, Document document) throws DocumentProcessingException {
		if (!(document instanceof XmlDocument)) {
			throw new DocumentProcessingException("Document was not instance of XmlDocument");
		}

		NodeList nodeList;
		try {
			nodeList = (NodeList) xPath.compile(query)
					.evaluate(((XmlDocument) document).getDom(), XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new DocumentProcessingException(e);
		}
		return nodeListToQueryResultList(nodeList);
	}

	@Override
	public List<QueryResult> process(String query, QueryResult queryResult) throws DocumentProcessingException {
		if (!(queryResult instanceof XmlQueryResultImpl)) {
			throw new DocumentProcessingException("queryResult was not instance of XmlQueryResultImpl");
		}
		NodeList nodeList;
		try {
			nodeList = (NodeList) xPath.compile(query)
					.evaluate(((XmlQueryResultImpl) queryResult).getNode(), XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new DocumentProcessingException(e);
		}
		return nodeListToQueryResultList(nodeList);

	}

	private List<QueryResult> nodeListToQueryResultList(NodeList nodeList) {
		return IntStream.range(0, nodeList.getLength())
				.mapToObj(nodeList::item)
				.map(XmlQueryResultImpl::create)
				.collect(Collectors.toList());
	}

}
