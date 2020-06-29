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
	@Schema(description = "The starting index of paginated results.", example = "0")
	private Long skip;
	@Schema(description = "Determines how many results are returned per page", example = "10")
	private Long pageSize;
	@Schema(description = "The list of strings you wish to search", example = "[\"laser\", \"keilaus\"]")
	private List<String> query = new ArrayList<>();
	@Schema(required = false, description = "The language of search terms can be forced to be one of FI, SV, or EN", example="FI", allowableValues = {"FI", "SV", "EN"})
	private String queryLanguage;
	@Schema(description = "List of facets to filter the search by", example = "{\"organisations\": [\"organisations\"]}")
	private Map<String, List<String>> facets = new HashMap<String, List<String>>();
	@Schema(description = "Sorting order")
	private List<Sort> sort = new ArrayList<>();
	@Schema(description = "This field is unused at the moment", hidden = true)
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

	@Schema(description = "This object indicates the sorting order according to field name and ascending or descending order.")
	public static class Sort {
		@Schema(description = "Field name to sort by", example="title", allowableValues = {"title", "datestamp", "score"})
		private String field;
		@Schema(description = "Ascending or descending order", example="asc", allowableValues = {"asc", "desc"})
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
