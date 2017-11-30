package fi.maanmittauslaitos.pta.search.index;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Example response document that this class is for. Not all fields are included
 * 
 * {
 * 		"_index":"catalog",
 * 		"_type":"doc",
 * 		"_id":"4d82a421-da28-4278-92f2-c3532a7c2df9",
 * 		"_version":7,
 * 		"result":"updated",
 * 		"_shards":{"total":2,"successful":1,"failed":0},
 * 		"created":false
 * }
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticsearchIndexingResponse {
	private String id;
	private int version;
	private String result;
	private Boolean created;
	
	@JsonProperty("_id")
	public String getId() {
		return id;
	}
	
	@JsonProperty("_id")
	public void setId(String id) {
		this.id = id;
	}
	
	@JsonProperty("_version")
	public int getVersion() {
		return version;
	}
	
	@JsonProperty("_version")
	public void setVersion(int version) {
		this.version = version;
	}
	
	public void setResult(String result) {
		this.result = result;
	}
	
	public String getResult() {
		return result;
	}
	
	public void setCreated(Boolean created) {
		this.created = created;
	}
	
	public Boolean isCreated() {
		return created;
	}
}
