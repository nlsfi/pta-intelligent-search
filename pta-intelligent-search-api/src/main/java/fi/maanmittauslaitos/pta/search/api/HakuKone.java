package fi.maanmittauslaitos.pta.search.api;

import java.io.IOException;

import fi.maanmittauslaitos.pta.search.api.model.SearchQuery;
import fi.maanmittauslaitos.pta.search.api.model.SearchResult;

public interface HakuKone {
	public SearchResult haku(SearchQuery pyynto, Language lang) throws IOException;
}
