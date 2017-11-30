package fi.maanmittauslaitos.pta.search.api;

import fi.maanmittauslaitos.pta.search.api.HakuTulos.Osuma;

public class HardcodedHakuKoneImpl implements HakuKone {
	@Override
	public HakuTulos haku(HakuPyynto pyynto) {

		HakuTulos tulos = new HakuTulos();
		tulos.getHakusanavinkit().add("koti"+pyynto.getHakusanat().get(0));
		tulos.getHakusanavinkit().add("koira");
		tulos.getHakusanavinkit().add("lemmikki");
		
		tulos.getOsumat().add(new Osuma().withUrl("http://www.google.fi").withRelevanssi(0.9994));
		tulos.getOsumat().add(new Osuma().withUrl("http://www.paikkatietohakemisto.fi").withRelevanssi(0.9594));
		tulos.getOsumat().add(new Osuma().withUrl("http://www.bing.com").withRelevanssi(0.04));

		return tulos;
	}
}
