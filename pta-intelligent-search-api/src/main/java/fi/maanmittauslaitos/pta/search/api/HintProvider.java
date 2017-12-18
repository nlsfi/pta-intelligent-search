package fi.maanmittauslaitos.pta.search.api;

import java.util.List;

import fi.maanmittauslaitos.pta.search.api.HakuTulos.Hit;

public interface HintProvider {
	public List<String> getHints(HakuPyynto pyynto, List<Hit> hits);
}
