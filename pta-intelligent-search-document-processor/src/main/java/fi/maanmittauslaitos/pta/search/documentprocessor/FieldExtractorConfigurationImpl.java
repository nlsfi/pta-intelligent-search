package fi.maanmittauslaitos.pta.search.documentprocessor;

import fi.maanmittauslaitos.pta.search.documentprocessor.query.DocumentQuery;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.QueryResult;

import javax.xml.xpath.XPathException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FieldExtractorConfigurationImpl extends AbstractFieldExtractorConfiguration {
	private FieldExtractorType type;
	private String query;
	private CustomExtractor customExtractor;
	private ListCustomExtractor customNodeListExtractor;

	@Override
	public void copyUnderlyingFeatures(AbstractFieldExtractorConfiguration object) {
		FieldExtractorConfigurationImpl ret = (FieldExtractorConfigurationImpl) object;

		ret.setType(getType());
		ret.setQuery(getQuery());
		ret.setCustomExtractor(getCustomExtractor());
	}

	public void setType(FieldExtractorType type) {
		this.type = type;
	}

	public FieldExtractorType getType() {
		return type;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}


	public enum FieldExtractorType {
		FIRST_MATCHING_VALUE,
		ALL_MATCHING_VALUES,
		TRUE_IF_MATCHES_OTHERWISE_FALSE,
		CUSTOM_CLASS,
		CUSTOM_CLASS_SINGLE_VALUE
	}

	public CustomExtractor getCustomExtractor() {
		return customExtractor;
	}

	public void setCustomExtractor(CustomExtractor customExtractor) {
		this.customExtractor = customExtractor;
	}

	public ListCustomExtractor getListCustomExtractor() {
		return customNodeListExtractor;
	}

	public void setCustomNodeListExtractor(ListCustomExtractor customNodeListExtractor) {
		this.customNodeListExtractor = customNodeListExtractor;
	}

	@Override
	public Object process(Document doc, DocumentQuery documentQuery) throws DocumentProcessingException {
		List<QueryResult> queryResultList = documentQuery.process(getQuery(), doc);


		switch (getType()) {
			case FIRST_MATCHING_VALUE: {
				List<String> ret = new ArrayList<>();
				if (queryResultList.size() > 0) {
					String value = queryResultList.get(0).getValue();
					if (value != null) {
						value = value.trim();
					}
					ret.add(value);
				}
				return ret;
			}
			case ALL_MATCHING_VALUES: {
				List<String> ret = new ArrayList<>();
				for (QueryResult queryResult : queryResultList) {
					String value = queryResult.getValue();
					if (value != null) {
						value = value.trim();
					}
					ret.add(value);
				}
				return ret;
			}
			case TRUE_IF_MATCHES_OTHERWISE_FALSE: {
				boolean matches = queryResultList.size() > 0;
				return matches;
			}

			case CUSTOM_CLASS: {
				CustomExtractor extractor = getCustomExtractor();
				if (extractor == null) {
					throw new IllegalArgumentException("Missing CustomExtractor in CUSTOM_CLASS configuration");
				}
				try {
					List<Object> ret = new ArrayList<>();
					for (QueryResult queryResult : queryResultList) {
						Object obj = extractor.process(documentQuery, queryResult);
						if (obj != null) {
							ret.add(obj);
						}
					}
					return ret;
				} catch (XPathException xpe) {
					throw new DocumentProcessingException(xpe);
				}
			}

			case CUSTOM_CLASS_SINGLE_VALUE: {
				ListCustomExtractor extractor = getListCustomExtractor();
				if (extractor == null) {
					throw new IllegalArgumentException("Missing CustomListExtractor in CUSTOM_CLASS_SINGLE_VALUE configuration");
				}
				try {
					Object obj = extractor.process(documentQuery, queryResultList);
					return obj != null ? obj : Collections.emptyList();
				} catch (XPathException xpe) {
					throw new DocumentProcessingException(xpe);
				}
			}

			default:
				throw new IllegalArgumentException("Unknown type of field extractor: " + getType());
		}

	}

}
