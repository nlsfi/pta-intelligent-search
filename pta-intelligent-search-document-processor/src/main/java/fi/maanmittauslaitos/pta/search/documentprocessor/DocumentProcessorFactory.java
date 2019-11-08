package fi.maanmittauslaitos.pta.search.documentprocessor;

import javax.xml.parsers.ParserConfigurationException;

public class DocumentProcessorFactory {

	private static DocumentProcessorFactory instance;


	private DocumentProcessorFactory() {
	}

	public static DocumentProcessorFactory getInstance() {
		if (instance == null) {
			synchronized (DocumentProcessorFactory.class) {
				if (instance == null) {
					instance = new DocumentProcessorFactory();
				}
			}
		}

		return instance;
	}

	public DocumentProcessor createXmlProcessor(DocumentProcessingConfiguration configuration) throws ParserConfigurationException {
		return new XmlDocumentProcessorImpl(configuration);
	}

	public DocumentProcessor createJsonProcessor(DocumentProcessingConfiguration configuration) throws ParserConfigurationException {
		return new JsonDocumentProcessorImpl(configuration);
	}

}
