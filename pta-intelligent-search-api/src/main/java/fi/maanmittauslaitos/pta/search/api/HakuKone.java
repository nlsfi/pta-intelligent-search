package fi.maanmittauslaitos.pta.search.api;

import java.io.IOException;

public interface HakuKone {
	public HakuTulos haku(HakuPyynto pyynto, Language lang) throws IOException;
}
