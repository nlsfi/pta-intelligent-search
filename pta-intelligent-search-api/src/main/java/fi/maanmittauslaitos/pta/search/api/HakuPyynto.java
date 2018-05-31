package fi.maanmittauslaitos.pta.search.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HakuPyynto {
	private Long skip;
	private Long pageSize;
	private List<String> query = new ArrayList<>();
	
	private Map<String, List<String>> facets = new HashMap<String, List<String>>();
	private List<Sort> sort = new ArrayList<>();
	private List<String> optionalFields = new ArrayList<>();
	
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
	
	public Map<String, List<String>> getFacets() {
		return facets;
	}
	
	public void setFacets(Map<String, List<String>> facets) {
		this.facets = facets;
	}
	
	public List<Sort> getSort() {
		return sort;
	}
	
	public void setSort(List<Sort> sort) {
		this.sort = sort;
	}
	
	public List<String> getOptionalFields() {
		return optionalFields;
	}
	
	public void setOptionalFields(List<String> optionalFields) {
		this.optionalFields = optionalFields;
	}
	
	public static class Sort {
		private String field;
		private String order;
		
		public void setField(String field) {
			this.field = field;
		}
		
		public String getField() {
			return field;
		}
		
		public void setOrder(String order) {
			this.order = order;
		}
		
		public String getOrder() {
			return order;
		}
	}
}
