package fi.maanmittauslaitos.pta.search.metadata;

import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessorFactory;

import javax.xml.parsers.ParserConfigurationException;

public abstract class MetadataExtractorConfigurationFactory {

	public DocumentProcessorFactory getDocumentProcessorFactory() {
		return DocumentProcessorFactory.getInstance();
	}

	public abstract DocumentProcessingConfiguration createMetadataDocumentProcessingConfiguration() throws ParserConfigurationException;

	public abstract DocumentProcessor createMetadataDocumentProcessor() throws ParserConfigurationException;
}
