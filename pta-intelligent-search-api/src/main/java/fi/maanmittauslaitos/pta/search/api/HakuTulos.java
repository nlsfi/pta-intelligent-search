package fi.maanmittauslaitos.pta.search.api;

import java.util.ArrayList;
import java.util.List;

public class HakuTulos {
	private List<Osuma> osumat = new ArrayList<>();
	private List<String> hakusanavinkit = new ArrayList<>();
	
	public List<String> getHakusanavinkit() {
		return hakusanavinkit;
	}
	
	public void setHakusanavinkit(List<String> hakusanavinkit) {
		this.hakusanavinkit = hakusanavinkit;
	}
	
	public List<Osuma> getOsumat() {
		return osumat;
	}
	
	public void setOsumat(List<Osuma> osumat) {
		this.osumat = osumat;
	}
	
	public static class Osuma {
		private String title;
		private String abstractText;
		private String url;
		private Double relevanssi;
		
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
		
		public void setRelevanssi(Double relevanssi) {
			this.relevanssi = relevanssi;
		}
		
		public Double getRelevanssi() {
			return relevanssi;
		}
		
		public void setUrl(String url) {
			this.url = url;
		}
		
		public String getUrl() {
			return url;
		}
		
		public Osuma withRelevanssi(Double relevanssi) {
			setRelevanssi(relevanssi);
			return this;
		}
		
		public Osuma withUrl(String url) {
			setUrl(url);
			return this;
		}
	}
}
