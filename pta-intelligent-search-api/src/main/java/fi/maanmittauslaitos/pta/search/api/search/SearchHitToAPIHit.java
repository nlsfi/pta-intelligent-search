package fi.maanmittauslaitos.pta.search.api.search;

import fi.maanmittauslaitos.pta.search.api.Language;
import fi.maanmittauslaitos.pta.search.api.model.SearchResult;
import fi.maanmittauslaitos.pta.search.api.model.SearchResult.Hit;
import fi.maanmittauslaitos.pta.search.api.model.SearchResult.HitText;
import fi.maanmittauslaitos.pta.search.api.model.SearchResult.HitText.HitOrganisation;
import fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants;
import org.apache.log4j.Logger;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants.FIELD_CATALOG;
import static fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants.FIELD_CATALOG_TYPE;
import static fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants.FIELD_CATALOG_URL;

public class SearchHitToAPIHit implements Consumer<SearchHit> {
	static Logger logger = Logger.getLogger(SearchHitToAPIHit.class);
	
	private AtomicInteger hitCount = new AtomicInteger(0);
	private SearchResult tulos;

	public SearchHitToAPIHit(SearchResult tulos) {
		this.tulos = tulos;
	}

	@Override
	public void accept(SearchHit t) {
		if (hitCount.getAndIncrement() == 0 && logger.isTraceEnabled()) {
			logger.trace("Explanation for why first hit matched:");
			logger.trace(t.getExplanation());
		}
		Hit osuma = new Hit();

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> organisations = (List<Map<String, Object>>)t.getSourceAsMap().get("organisations");

		for (Language lang : Language.values()) {
			String titleField = "title";
			String abstractField = "abstract";
			
			if (lang != Language.FI) {
				titleField += lang.getFieldPostfix();
				abstractField += lang.getFieldPostfix();
			}
			HitText text = HitText.create(
					lang.toString(),
					extractStringValue(t.getSourceAsMap().get(titleField)),
					extractStringValue(t.getSourceAsMap().get(abstractField)));
			
			
			for (Map<String, Object> o : organisations) {
				
				String name;
				if (lang == Language.FI) {
					name = o.get("organisationName").toString();
				} else {
					@SuppressWarnings("unchecked")
					Map<String,String> localisedOrganisationName = (Map<String,String>)o.get("localisedOrganisationName");
					name = localisedOrganisationName.get(lang.toString());
				}
				String role = String.valueOf(o.get("isoRole"));
				
				HitOrganisation org = new HitOrganisation();
				org.setName(name);
				org.setRole(role);
				
				text.getOrganisations().add(org);
			}


			osuma.getText().add(text);
		}

		
		processTypeField(t, "isService", osuma);
		processTypeField(t, "isDataset", osuma);
		processTypeField(t, "isAvoindata", osuma);
		processTypeField(t, "isPtaAineisto", osuma);
		
		osuma.setId(t.getId());
		osuma.setScore((double)t.getScore());
		osuma.setDateStamp(extractStringValue(t.getSourceAsMap().get("datestamp")));
		osuma.setDistributionFormats(extractListValue(t.getSourceAsMap().get("distributionFormats")));
		osuma.setKeywordsInspire(extractListValue(t.getSourceAsMap().get("keywordsInspire")));
		osuma.setTopicCategories(extractListValue(t.getSourceAsMap().get("topicCategories")));

		@SuppressWarnings("unchecked")
		Map<String, Object> catalog = (Map<String, Object>) t.getSourceAsMap().get(FIELD_CATALOG);

		SearchResult.Catalog hitCatalog = new SearchResult.Catalog();
		hitCatalog.setType(String.valueOf(catalog.getOrDefault(FIELD_CATALOG_TYPE, "")));
		hitCatalog.setUrl(String.valueOf(catalog.getOrDefault(FIELD_CATALOG_URL, "")));

		osuma.setCatalog(hitCatalog);
		
		tulos.getHits().add(osuma);
	}

	private List<String> extractListValue(Object obj) {
		List<String> ret = new ArrayList<>();
		if (obj != null) {
			if (obj instanceof Collection<?>) {
				Collection<?> tmp = (Collection<?>)obj;
				
				for (Object o : tmp) {
					ret.add(o.toString());
				}
			} else {
				ret.add(obj.toString());
			}
		}
		return ret;
	}
	
	private boolean isSet(SearchHit t, String field) {
		Object o = t.getSourceAsMap().get(field);
		if (o == null) {
			return false;
		}
		if (o instanceof String) {
			return Boolean.valueOf((String)o);
		}
		if (o instanceof Boolean) {
			return (Boolean)o;
		}
		throw new IllegalArgumentException("Can not determine boolean value of field '"+field+"', is type "+o.getClass()+", value = '"+o+"'");
	}
	
	private void processTypeField(SearchHit t, String field, Hit osuma) {
		if (isSet(t, field)) {
			osuma.getTypes().add(field);
		}
	}

	private String extractStringValue(Object obj) {
		String title;
		if (obj != null) {
			if (obj instanceof Collection<?>) {
				Collection<?> tmp = (Collection<?>)obj;
				if (tmp.size() > 0) {
					StringBuffer buf = new StringBuffer();
					int i = 0;
					for (Object o : tmp) {
						if (i > 0) {
							buf.append('\n');
						}
						buf.append(o.toString());
						i++;
					}
					title = buf.toString();
				} else {
					title = null;
				}
			} else {
				title = obj.toString();
			}
		} else {
			title = null;
		}
		return title;
	}
}