package fi.maanmittauslaitos.pta.search.api.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Schema for Developer object...")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchQuery {
	private Long skip;
	private Long pageSize;
	private List<String> query = new ArrayList<>();
	@Schema(required = false, description = "The language of search terms can be forced to be one of FI, SV, or EN")
	private String queryLanguage;
	@Schema(description = "")
	private Map<String, List<String>> facets = new HashMap<String, List<String>>();
	@Schema(description = "")
	private List<Sort> sort = new ArrayList<>();
	private List<String> optionalFields = new ArrayList<>();
	
	public void setQuery(List<String> query) {
		this.query = query;
	}
	
	public List<String> getQuery() {
		return query;
	}
	
	public void setQueryLanguage(String queryLanguage) {
		this.queryLanguage = queryLanguage;
	}
	
	public String getQueryLanguage() {
		return queryLanguage;
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

	@Schema(description = "Schema for Developer object...")
	public static class Sort {
		@Schema(description = "Schema for Developer object...")
		private String field;
		@Schema(description = "Schema for Developer object...")
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
