package fi.maanmittauslaitos.pta.search.api;

import fi.maanmittauslaitos.pta.search.api.HakuTulos.Hit;

public class HardcodedHakuKoneImpl implements HakuKone {
	@Override
	public HakuTulos haku(HakuPyynto pyynto, Language lang) {

		HakuTulos tulos = new HakuTulos();
		tulos.getHints().add("koti"+pyynto.getQuery().get(0));
		tulos.getHints().add("koira");
		tulos.getHints().add("lemmikki");
		
		tulos.getHits().add(new Hit().withUrl("http://www.google.fi").withRelevanssi(0.9994));
		tulos.getHits().add(new Hit().withUrl("http://www.paikkatietohakemisto.fi").withRelevanssi(0.9594));
		tulos.getHits().add(new Hit().withUrl("http://www.bing.com").withRelevanssi(0.04));

		return tulos;
	}
}
