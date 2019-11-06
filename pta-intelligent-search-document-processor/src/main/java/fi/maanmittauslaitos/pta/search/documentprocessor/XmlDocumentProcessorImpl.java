package fi.maanmittauslaitos.pta.search.documentprocessor;

import fi.maanmittauslaitos.pta.search.documentprocessor.query.XmlDocumentQuerierImpl;
import fi.maanmittauslaitos.pta.search.text.TextProcessingChain;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class XmlDocumentProcessorImpl implements DocumentProcessor {
	private final DocumentBuilder builder;
	private final DocumentProcessingConfiguration configuration;
	private XmlDocumentQuerierImpl documentQuery;

	XmlDocumentProcessorImpl(DocumentProcessingConfiguration configuration) throws ParserConfigurationException {
		this.configuration = configuration;

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);

		this.builder = builderFactory.newDocumentBuilder();


		HashmapNamespaceContextImpl nsContext = new HashmapNamespaceContextImpl();
		for (Map.Entry<String, String> e : configuration.getNamespaces().entrySet()) {
			nsContext.registerNamespace(e.getKey(), e.getValue());
		}

		XPath xPath = XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(nsContext);
		this.documentQuery = XmlDocumentQuerierImpl.create(xPath);

	}

	@Override
	public DocumentProcessingConfiguration getDocumentProcessingConfiguration() {
		return configuration;
	}

	public DocumentProcessingConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public Document processDocument(InputStream is) throws DocumentProcessingException {
		Map<String, TextProcessingChain> textProcessingChains = configuration.getTextProcessingChains();
		org.w3c.dom.Document doc;

		try {
			synchronized (builder) {
				doc = builder.parse(is);
			}
		} catch (Exception e) {
			throw new DocumentProcessingException(e);
		}

		XmlDocument ret = new XmlDocument();
		ret.setDom(doc);

		for (FieldExtractorConfiguration fec : configuration.getFieldExtractors()) {
			Object value = fec.process(ret, documentQuery);

			if (fec.getTextProcessorName() != null) {
				TextProcessingChain chain = textProcessingChains.get(fec.getTextProcessorName());

				if (chain == null) {
					throw new IllegalArgumentException("Text processor chain '" + fec.getTextProcessorName() + "' not declared");
				}

				@SuppressWarnings("unchecked")
				List<String> asList = (List<String>) value;

				value = chain.process(asList);
			}

			ret.getFields().put(fec.getField(), value);
		}

		return ret;
	}

}
