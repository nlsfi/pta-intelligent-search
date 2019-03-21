package fi.maanmittauslaitos.pta.search.index;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticsearchSearchIdsResponse {
	private Hits hits;
	
	public void setHits(Hits hits) {
		this.hits = hits;
	}
	
	public Hits getHits() {
		return hits;
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Hits {
		private List<Hit> hits;
		
		public void setHits(List<Hit> hits) {
			this.hits = hits;
		}
		
		public List<Hit> getHits() {
			return hits;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Hit {
		@JsonProperty("_id")
		private String id;
		
		public void setId(String id) {
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
	}
}
