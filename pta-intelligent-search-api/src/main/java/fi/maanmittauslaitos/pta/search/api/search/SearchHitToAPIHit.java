package fi.maanmittauslaitos.pta.search.api.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.elasticsearch.search.SearchHit;

import fi.maanmittauslaitos.pta.search.api.Language;
import fi.maanmittauslaitos.pta.search.api.model.SearchResult;
import fi.maanmittauslaitos.pta.search.api.model.SearchResult.Hit;
import fi.maanmittauslaitos.pta.search.api.model.SearchResult.HitText;
import fi.maanmittauslaitos.pta.search.api.model.SearchResult.HitText.HitOrganisation;
import fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants;

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
				String role = o.get("isoRole").toString();
				
				HitOrganisation org = new HitOrganisation();
				org.setName(name);
				org.setRole(role);
				
				text.getOrganisations().add(org);
			}

			osuma.getText().add(text);
		}

		osuma.setId(t.getId());
		osuma.setAbstractUris(extractListValue(t.getSourceAsMap().get(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_URI)));
		osuma.setAbstractTopicUris(extractListValue(t.getSourceAsMap().get(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_MAUI_URI)));
		osuma.setScore((double)t.getScore());
		osuma.setDateStamp(extractStringValue(t.getSourceAsMap().get("datestamp")));
		osuma.setDistributionFormats(extractListValue(t.getSourceAsMap().get("distributionFormats")));
		osuma.setKeywordsInspire(extractListValue(t.getSourceAsMap().get("keywordsInspire")));
		osuma.setTopicCategories(extractListValue(t.getSourceAsMap().get("topicCategories")));
		
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