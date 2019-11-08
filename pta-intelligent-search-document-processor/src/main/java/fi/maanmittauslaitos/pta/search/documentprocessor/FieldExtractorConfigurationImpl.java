package fi.maanmittauslaitos.pta.search.documentprocessor;

import fi.maanmittauslaitos.pta.search.documentprocessor.query.DocumentQuery;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.QueryResult;

import javax.xml.xpath.XPathException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FieldExtractorConfigurationImpl extends AbstractFieldExtractorConfiguration {
	private FieldExtractorType type;
	private String query;
	private Object defaultValue;
	private List<String> extraQueries = Collections.emptyList();
	private CustomExtractor customExtractor;
	private ListCustomExtractor customNodeListExtractor;
	private ExtractorTrimmer trimmer = String::trim;

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

	public Optional<Object> getDefaultValue() {
		return Optional.ofNullable(defaultValue);
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	public List<String> getExtraQueries() {
		return extraQueries;
	}

	public void setExtraQueries(List<String> extraQueries) {
		this.extraQueries = extraQueries;
	}

	public ExtractorTrimmer getTrimmer() {
		return trimmer;
	}

	public void setTrimmer(ExtractorTrimmer trimmer) {
		this.trimmer = trimmer;
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
						value = getTrimmer().trim(value);
					}
					ret.add(value);
				}

				populateWithDefault(ret);

				return ret;
			}
			case ALL_MATCHING_VALUES: {
				List<String> ret = new ArrayList<>();
				for (QueryResult queryResult : queryResultList) {
					String value = queryResult.getValue();
					if (value != null) {
						value = getTrimmer().trim(value);
					}
					ret.add(value);
				}

				populateWithDefault(ret);

				return ret;
			}
			case TRUE_IF_MATCHES_OTHERWISE_FALSE: {
				return queryResultList.size() > 0;
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

					if (ret.isEmpty() || ret.contains(null)) {
						getDefaultValue().ifPresent(e -> {
							ret.add(e);
							ret.removeAll(Collections.singleton(null));
						});
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
					return obj != null ? obj : getDefaultValue().orElse(Collections.emptyList());
				} catch (XPathException xpe) {
					throw new DocumentProcessingException(xpe);
				}
			}
			case FIRST_MATCHING_FROM_MULTIPLE_QUERIES: {
				List<String> extraQueries = getExtraQueries();
				if (extraQueries.isEmpty()) {
					throw new IllegalArgumentException("Missing multiple queries in FIRST_MATCHING_FROM_MULTIPLE_QUERIES configuration");
				}

				QueryResult queryResult = queryResultList.stream()
						.filter(Objects::nonNull)
						.findFirst()
						.orElse(null);

				if (queryResult == null) {
					for (String query : extraQueries) {
						queryResult = documentQuery.process(query, doc).stream()
								.filter(Objects::nonNull)
								.findFirst()
								.orElse(null);
						if (queryResult != null) {
							break;
						}
					}
				}

				List<String> ret = new ArrayList<>();
				if (queryResult != null) {
					String value = queryResult.getValue();
					if (value != null) {
						value = getTrimmer().trim(value);
					}
					ret.add(value);
				}

				populateWithDefault(ret);

				return ret;
			}

			default:
				throw new IllegalArgumentException("Unknown type of field extractor: " + getType());
		}

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

	private void populateWithDefault(List<String> ret) {
		if (ret.isEmpty() || ret.contains(null)) {
			getDefaultValue()
					.filter(val -> val instanceof String)
					.ifPresent(val -> {
						ret.removeIf(Objects::isNull);
						ret.add((String) val);
					});
		}
	}

	public enum FieldExtractorType {
		FIRST_MATCHING_VALUE,
		ALL_MATCHING_VALUES,
		TRUE_IF_MATCHES_OTHERWISE_FALSE,
		CUSTOM_CLASS,
		CUSTOM_CLASS_SINGLE_VALUE,
		FIRST_MATCHING_FROM_MULTIPLE_QUERIES
	}

}
