package fi.maanmittauslaitos.pta.search.api;

import java.util.Set;

import org.elasticsearch.search.builder.SearchSourceBuilder;

public interface ElasticsearchQueryProvider {
	public SearchSourceBuilder buildSearchSource(Set<SearchTerm> termit);
	
	public Set<SearchTerm> getSearchTerms(HakuPyynto pyynto);
	
	public class SearchTerm {
		public final String resource;
		public final double weight;
		
		public SearchTerm(String term, double weight) {
			this.resource = term;
			this.weight = weight;
		}
		
		public String toString() {
			return resource+"#"+weight;
		}
	}
}
