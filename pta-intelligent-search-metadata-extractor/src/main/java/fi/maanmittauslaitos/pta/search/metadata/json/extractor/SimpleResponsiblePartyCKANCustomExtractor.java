package fi.maanmittauslaitos.pta.search.metadata.json.extractor;

import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.JsonDocumentQueryImpl;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.QueryResult;
import fi.maanmittauslaitos.pta.search.metadata.model.ResponsibleParty;
import fi.maanmittauslaitos.pta.search.metadata.model.TextRewriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Does not parse email for given query result
 */
public class SimpleResponsiblePartyCKANCustomExtractor extends JsonPathListCustomExtractor {

	private static final Logger logger = LoggerFactory.getLogger(SimpleResponsiblePartyCKANCustomExtractor.class);

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
	public Object process(JsonDocumentQueryImpl query, List<QueryResult> queryResult) throws DocumentProcessingException {
		if (logger.isTraceEnabled()) {
			logger.trace("Reading organisation information");
		}
		List<ResponsibleParty> responsibleParties = new ArrayList<>();
		try {
			for (QueryResult mainQueryResult : queryResult) {
				query.process("", mainQueryResult).stream()
						.map(QueryResult::getValue)
						.map(organisationName -> {
							ResponsibleParty ret = new ResponsibleParty();
							String cleanedOrganisationName = getOrganisationNameRewriter().rewrite(organisationName);

							if (logger.isTraceEnabled()) {
								//logger.trace("\tOrganisation role: "+isoRole);
								logger.trace("\tCanonical name: " + cleanedOrganisationName);
							}
							ret.setPartyName(cleanedOrganisationName);
							ret.getLocalizedPartyName().put("FI",
									getOrganisationNameRewriter().rewrite(organisationName, "FI"));

							return ret;
						})
						.forEach(responsibleParties::add);
			}
		} catch (Exception e) {
			throw new DocumentProcessingException(e);
		}

		return responsibleParties;

	}
}
