package fi.maanmittauslaitos.pta.search.api;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HakuPyynto {
	private Long skip;
	private Long pageSize;
	private List<String> query = new ArrayList<>();
	
	public void setQuery(List<String> query) {
		this.query = query;
	}
	
	public List<String> getQuery() {
		return query;
	}
	
	public Long getSkip() {
		return skip;
	}
	
	public void setSkip(Long skip) {
		this.skip = skip;
	}
	
	public Long getPageSize() {
		return pageSize;
	}
	
	public void setPageSize(Long pageSize) {
		this.pageSize = pageSize;
	}
	
}
