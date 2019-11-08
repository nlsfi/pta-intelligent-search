package fi.maanmittauslaitos.pta.search.metadata.json;

import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.ListCustomExtractor;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.DocumentQuery;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.JsonDocumentQueryImpl;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.QueryResult;
import fi.maanmittauslaitos.pta.search.metadata.model.ResponsibleParty;
import fi.maanmittauslaitos.pta.search.metadata.model.TextRewriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ResponsiblePartyCKANCustomExtractor implements ListCustomExtractor {

	private static final Logger logger = LogManager.getLogger(ResponsiblePartyCKANCustomExtractor.class);

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
	public Object process(DocumentQuery documentQuery, List<QueryResult> queryResults) throws DocumentProcessingException {
		if (!(documentQuery instanceof JsonDocumentQueryImpl)) {
			throw new DocumentProcessingException("This extractor should only be used for Json Documents");
		}

		JsonDocumentQueryImpl jsonDocumentQuery = (JsonDocumentQueryImpl) documentQuery;


		if (logger.isTraceEnabled()) {
			logger.trace("Reading organisation information");
		}

		try {
			List<ResponsibleParty> responsibleParties = new ArrayList<>();
			for (QueryResult mainQueryResult : queryResults) {
				jsonDocumentQuery.process("", mainQueryResult).stream()
						.map(QueryResult::getValue)
						.map(organisationName -> {
							ResponsibleParty ret = new ResponsibleParty();
							String cleanedOrganisationName = getOrganisationNameRewriter().rewrite(organisationName);

							if (logger.isTraceEnabled()) {
								//logger.trace("\tOrganisation role: "+isoRole);
								logger.trace("\tCanonical name: " + cleanedOrganisationName);
							}
							ret.setOrganisationName(cleanedOrganisationName);
							ret.getLocalisedOrganisationName().put("FI",
									getOrganisationNameRewriter().rewrite(organisationName, "FI"));

							return ret;
						})
						.forEach(responsibleParties::add);
			}

			return responsibleParties;
		} catch (RuntimeException e) {
			throw new DocumentProcessingException(e);
		}


	}

}
