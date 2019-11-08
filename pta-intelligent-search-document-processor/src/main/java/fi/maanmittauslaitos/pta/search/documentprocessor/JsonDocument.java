package fi.maanmittauslaitos.pta.search.documentprocessor;

import com.jayway.jsonpath.DocumentContext;

public class JsonDocument extends Document {
	private DocumentContext documentContext;


	public DocumentContext getDocumentContext() {
		return documentContext;
	}

	void setDocumentContext(DocumentContext documentContext) {
		this.documentContext = documentContext;
	}
}
