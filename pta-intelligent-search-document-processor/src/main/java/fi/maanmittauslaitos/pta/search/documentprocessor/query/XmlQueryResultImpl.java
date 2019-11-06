package fi.maanmittauslaitos.pta.search.documentprocessor.query;

import org.w3c.dom.Node;

public class XmlQueryResultImpl implements QueryResult {
	private Node node;

	private XmlQueryResultImpl(Node node) {
		this.node = node;
	}

	public static XmlQueryResultImpl create(Node node) {
		return new XmlQueryResultImpl(node);
	}

	public Node getNode() {
		return node;
	}

	@Override
	public String getValue() {
		return node.getNodeValue();
	}
}
