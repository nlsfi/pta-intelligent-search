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

public class SimpleResponsiblePartyCKANCustomExtractor extends JsonPathListCustomExtractor {

	private static final Logger logger = LoggerFactory.getLogger(SimpleResponsiblePartyCKANCustomExtractor.class);

	public SimpleResponsiblePartyCKANCustomExtractor() {
		super();
	}

	public SimpleResponsiblePartyCKANCustomExtractor(boolean isThrowException) {
		super(isThrowException);
	}

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
		} catch (RuntimeException e) {
			handleExtractorException(e, null);
		}

		return responsibleParties;

	}

	@Override
	protected Logger getLogger() {
		return logger;
	}
}
