package fi.maanmittauslaitos.pta.search.api;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HakuPyynto {
	private List<String> hakusanat = new ArrayList<>();
	
	public void setHakusanat(List<String> hakusanat) {
		this.hakusanat = hakusanat;
	}
	
	public List<String> getHakusanat() {
		return hakusanat;
	}
}
