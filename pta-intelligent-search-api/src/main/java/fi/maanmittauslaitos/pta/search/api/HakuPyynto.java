package fi.maanmittauslaitos.pta.search.api;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HakuPyynto {
	private List<String> query = new ArrayList<>();
	
	public void setQuery(List<String> query) {
		this.query = query;
	}
	
	public List<String> getQuery() {
		return query;
	}
	
}
