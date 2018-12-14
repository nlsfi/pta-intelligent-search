package fi.maanmittauslaitos.pta.search.api.search;

import java.io.IOException;

import fi.maanmittauslaitos.pta.search.api.Language;
import fi.maanmittauslaitos.pta.search.api.model.SearchQuery;
import fi.maanmittauslaitos.pta.search.api.model.SearchResult;

public interface HakuKone {
	public SearchResult haku(SearchQuery pyynto, Language lang, boolean focusOnRegionalHits) throws IOException;
}
