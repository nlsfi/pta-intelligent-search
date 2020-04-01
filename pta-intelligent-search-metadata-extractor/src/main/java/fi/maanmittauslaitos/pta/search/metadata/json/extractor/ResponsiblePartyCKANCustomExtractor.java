package fi.maanmittauslaitos.pta.search.metadata.json.extractor;

import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.JsonDocumentQueryImpl;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.JsonQueryResultImpl;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.QueryResult;
import fi.maanmittauslaitos.pta.search.metadata.model.ResponsibleParty;
import fi.maanmittauslaitos.pta.search.metadata.model.TextRewriter;
import fi.maanmittauslaitos.pta.search.metadata.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ResponsiblePartyCKANCustomExtractor extends JsonPathListCustomExtractor {

	private TextRewriter organisationNameRewriter = new TextRewriter() {

		@Override
		public String rewrite(String name, String language) {
			return name;
		}

		@Override
		public String rewrite(String name) {
			return name;
		}
	};

	public TextRewriter getOrganisationNameRewriter() {
		return organisationNameRewriter;
	}

	public void setOrganisationNameRewriter(TextRewriter organisationNameRewriter) {
		this.organisationNameRewriter = organisationNameRewriter;
	}

	@Override
	public Object process(JsonDocumentQueryImpl query, List<QueryResult> queryResults) throws DocumentProcessingException {
		List<ResponsibleParty> responsibleParties = new ArrayList<>();

		try {
			ResponsibleParty rp = new ResponsibleParty();
			queryResults.stream()
					.filter(r -> r instanceof JsonQueryResultImpl)
					.map(r -> (JsonQueryResultImpl) r)
					.forEach(result -> {
						List<Object> rawResList = result.getRawValue();
						rawResList.stream()
								.filter(rawResult -> rawResult instanceof Map || rawResult instanceof String)
								.forEach(rawResult -> {
									String org = DEFAULT_PARSED_VALUE;
									String email = null;
									String role = DEFAULT_PARSED_VALUE;
									// Two values are grabbed for the reporting organisation (resource responsible party), so it is parsed as a map
									if (rawResult instanceof Map) {
										Map<String, Object> res = (Map<String,Object>) rawResult;
										org = getValueSafely(res, "reporting_organization", DEFAULT_PARSED_VALUE);
										email = getValueSafely(res, "reporting_person_email", DEFAULT_PARSED_VALUE);
										// hard-coded to owner (is the resource owner"
										role = "owner";
									}

									if(StringUtils.isEmpty(org) && StringUtils.isEmpty(email)) {
										Map<String, Object> res = (Map<String,Object>) rawResult;
										org = getValueSafely(res, "author", DEFAULT_PARSED_VALUE);
										email = getValueSafely(res, "author_email", DEFAULT_PARSED_VALUE);
									}
									String cleanedOrganisationName = getOrganisationNameRewriter().rewrite(org);

									rp.setIsoRole(role);
									rp.setEmail(Collections.singletonList(email));
									rp.setPartyName(cleanedOrganisationName);
									rp.getLocalizedPartyName().put("FI", getOrganisationNameRewriter().rewrite(org, "FI"));

								});

					});

			responsibleParties.add(rp);

		} catch (Exception  e) {
			throw new DocumentProcessingException(e);
		}

		return responsibleParties;

	}
}
