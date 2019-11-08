package fi.maanmittauslaitos.pta.search.documentprocessor;

import fi.maanmittauslaitos.pta.search.documentprocessor.query.DocumentQuery;

public interface FieldExtractorConfiguration {

	/**
	 * Name of the field where the extracted value should be stored
	 *
	 * @return
	 */
	String getField();

	void setField(String field);

	/**
	 * Process the document and returns the raw value. If this configuration has a text processor configured,
	 * that is not applied within this function.
	 *
	 * @param document
	 * @return
	 */
	Object process(Document document, DocumentQuery documentQuery) throws DocumentProcessingException;


	String getTextProcessorName();

	void setTextProcessorName(String textProcessorName);

	FieldExtractorConfiguration copy();
}
