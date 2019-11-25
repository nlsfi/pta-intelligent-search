package fi.maanmittauslaitos.pta.search.utils;

import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.source.HarvesterSource;

public class HarvesterContainer {


	private final HarvesterSource source;
	private final DocumentProcessor processor;

	private HarvesterContainer(HarvesterSource source, DocumentProcessor processor) {
		this.source = source;
		this.processor = processor;
	}

	public static HarvesterContainer create(HarvesterSource source, DocumentProcessor processor) {
		return new HarvesterContainer(source, processor);
	}

	public HarvesterSource getSource() {
		return source;
	}

	public DocumentProcessor getProcessor() {
		return processor;
	}

}
