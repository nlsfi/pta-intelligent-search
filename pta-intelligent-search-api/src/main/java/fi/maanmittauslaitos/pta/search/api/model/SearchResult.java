package fi.maanmittauslaitos.pta.search.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchResult {
	@Schema(description = "Start index of pagination", example = "0")
	private Long startIndex;
	@Schema(description = "Number of hits contained in the response", example = "1")
	private Long totalHits;
	@Schema(description = "The language used for the query")
	private QueryLanguage queryLanguage;
	@Schema(description = "List of hits")
	private List<Hit> hits = new ArrayList<>();
	@Schema(description = "Hints used for the search")
	private List<String> hints = new ArrayList<>();
	@Schema(description = "Facets that are contained in the results")
	private Map<String, List<Facet>> facets = new HashMap<>();

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

	public void setQueryLanguage(QueryLanguage queryLanguage) {
		this.queryLanguage = queryLanguage;
	}

	public QueryLanguage getQueryLanguage() {
		return queryLanguage;
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

	public Map<String, List<Facet>> getFacets() {
		return facets;
	}

	public void setFacets(Map<String, List<Facet>> facets) {
		this.facets = facets;
	}

	public static class Hit {
		@Schema(description = "")
		private List<HitText> text = new ArrayList<>();
		@Schema(description = "")
		private Double score;
		@Schema(description = "")
		private String id;
		@Schema(description = "")
		private String dateStamp;
		@Schema(description = "")
		private Catalog catalog;
		@Schema(description = "")
		private List<String> types = new ArrayList<>();
		@Schema(description = "")
		private List<String> topicCategories = new ArrayList<>();
		@Schema(description = "")
		private List<String> keywordsInspire = new ArrayList<>();
		@Schema(description = "")
		private List<String> distributionFormats = new ArrayList<>();


		// Possibly hidden in API response
		private List<String> abstractUris = new ArrayList<>();
		private List<String> abstractTopicUris = new ArrayList<>();


		public void setText(List<HitText> text) {
			this.text = text;
		}

		public List<HitText> getText() {
			return text;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public void setScore(Double score) {
			this.score = score;
		}

		public Double getScore() {
			return score;
		}

		public void setDateStamp(String dateStamp) {
			this.dateStamp = dateStamp;
		}

		public String getDateStamp() {
			return dateStamp;
		}

		public void setDistributionFormats(List<String> distributionFormats) {
			this.distributionFormats = distributionFormats;
		}

		public List<String> getDistributionFormats() {
			return distributionFormats;
		}

		public void setKeywordsInspire(List<String> keywordsInspire) {
			this.keywordsInspire = keywordsInspire;
		}

		public List<String> getKeywordsInspire() {
			return keywordsInspire;
		}

		public void setTopicCategories(List<String> topicCategories) {
			this.topicCategories = topicCategories;
		}

		public List<String> getTopicCategories() {
			return topicCategories;
		}

		public void setTypes(List<String> types) {
			this.types = types;
		}

		public List<String> getTypes() {
			return types;
		}

		public void setAbstractTopicUris(List<String> abstractTopicUris) {
			this.abstractTopicUris = abstractTopicUris;
		}

		public List<String> getAbstractTopicUris() {
			return abstractTopicUris;
		}

		public void setAbstractUris(List<String> abstractUris) {
			this.abstractUris = abstractUris;
		}

		public List<String> getAbstractUris() {
			return abstractUris;
		}

		public Catalog getCatalog() {
			return catalog;
		}

		public void setCatalog(Catalog catalog) {
			this.catalog = catalog;
		}

	}

	@Schema(description = "")
	public static class HitText {
		private String lang;
		private String title;
		private String abstractText;
		private List<HitOrganisation> organisations = new ArrayList<>();

		public void setAbstractText(String abstractText) {
			this.abstractText = abstractText;
		}

		public String getAbstractText() {
			return abstractText;
		}

		public void setLang(String lang) {
			this.lang = lang;
		}

		public String getLang() {
			return lang;
		}

		public void setOrganisations(List<HitOrganisation> organisations) {
			this.organisations = organisations;
		}

		public List<HitOrganisation> getOrganisations() {
			return organisations;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}

		public static HitText create(String lang, String title, String abstractText) {
			HitText ret = new HitText();
			ret.setLang(lang);
			ret.setTitle(title);
			ret.setAbstractText(abstractText);
			return ret;
		}

		@Schema(description = "")
		public static class HitOrganisation {
			private String name;
			private String role;

			public void setName(String name) {
				this.name = name;
			}

			public String getName() {
				return name;
			}

			public void setRole(String role) {
				this.role = role;
			}

			public String getRole() {
				return role;
			}
		}
	}

	@Schema(description = "")
	public static class Facet {
		@Schema(description = "")
		private String id;
		@Schema(description = "")
		private Long count;

		public void setCount(Long count) {
			this.count = count;
		}

		public Long getCount() {
			return count;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public static Facet create(String id, Long count) {
			Facet ret = new Facet();
			ret.setId(id);
			ret.setCount(count);
			return ret;
		}
	}

	@Schema(description = "Describes the language used for the search.")
	public static class QueryLanguage {
		@Schema(description = "The language used for querying", example = "FI")
		private String used;
		@Schema(description = "The language that was deduced by the search", example = "FI")
		private String deduced;
		@Schema(description = "The score", example = "1")
		private List<QueryLanguageScore> scores;

		public void setUsed(String used) {
			this.used = used;
		}

		public String getUsed() {
			return used;
		}

		public void setDeduced(String deduced) {
			this.deduced = deduced;
		}

		public String getDeduced() {
			return deduced;
		}

		public void setScores(List<QueryLanguageScore> scores) {
			this.scores = scores;
		}

		public List<QueryLanguageScore> getScores() {
			return scores;
		}
	}

	public static class QueryLanguageScore {
		private String language;
		private Integer score;

		public void setLanguage(String language) {
			this.language = language;
		}

		public String getLanguage() {
			return language;
		}

		public void setScore(Integer score) {
			this.score = score;
		}

		public Integer getScore() {
			return score;
		}
	}

	public static class Catalog {
		private String url;
		private String type;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
	}
}
