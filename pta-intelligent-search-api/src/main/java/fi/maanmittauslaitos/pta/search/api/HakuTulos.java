package fi.maanmittauslaitos.pta.search.api;

import java.util.ArrayList;
import java.util.List;

public class HakuTulos {
	private Long startIndex;
	private Long totalHits;
	private List<Hit> hits = new ArrayList<>();
	private List<String> hints = new ArrayList<>();
	
	public void setStartIndex(Long startIndex) {
		this.startIndex = startIndex;
	}
	
	public Long getStartIndex() {
		return startIndex;
	}
	
	public void setTotalHits(Long totalResults) {
		this.totalHits = totalResults;
	}
	
	public Long getTotalHits() {
		return totalHits;
	}
	
	public List<String> getHints() {
		return hints;
	}
	
	public void setHints(List<String> hints) {
		this.hints = hints;
	}
	
	public List<Hit> getHits() {
		return hits;
	}
	
	public void setHits(List<Hit> hits) {
		this.hits = hits;
	}
	
	public static class Hit {
		private String title;
		private String abstractText;
		private List<String> abstractUris = new ArrayList<>();
		private List<String> abstractTopicUris = new ArrayList<>();
		private String url;
		private Double score;
		
		public void setTitle(String title) {
			this.title = title;
		}
		
		public String getTitle() {
			return title;
		}
		
		public void setAbstractText(String abstractText) {
			this.abstractText = abstractText;
		}
		
		public String getAbstractText() {
			return abstractText;
		}
		
		public void setScore(Double score) {
			this.score = score;
		}
		
		public Double getScore() {
			return score;
		}
		
		public void setUrl(String url) {
			this.url = url;
		}
		
		public String getUrl() {
			return url;
		}
		
		public Hit withRelevanssi(Double relevanssi) {
			setScore(relevanssi);
			return this;
		}
		
		public Hit withUrl(String url) {
			setUrl(url);
			return this;
		}

		public void setAbstractUris(List<String> abstractUris) {
			this.abstractUris = abstractUris;
		}
		
		public List<String> getAbstractUris() {
			return abstractUris;
		}
		
		public void setAbstractTopicUris(List<String> abstractTopicUris) {
			this.abstractTopicUris = abstractTopicUris;
		}
		
		public List<String> getAbstractTopicUris() {
			return abstractTopicUris;
		}
	}
}
