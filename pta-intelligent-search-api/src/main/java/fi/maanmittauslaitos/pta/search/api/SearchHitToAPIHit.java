package fi.maanmittauslaitos.pta.search.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.elasticsearch.search.SearchHit;

import fi.maanmittauslaitos.pta.search.api.HakuTulos.Hit;
import fi.maanmittauslaitos.pta.search.api.HakuTulos.HitText;
import fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants;

public class SearchHitToAPIHit implements Consumer<SearchHit> {
	static Logger logger = Logger.getLogger(SearchHitToAPIHit.class);
	
	private AtomicInteger hitCount = new AtomicInteger(0);
	private HakuTulos tulos;

	public SearchHitToAPIHit(HakuTulos tulos) {
		this.tulos = tulos;
	}

	@Override
	public void accept(SearchHit t) {
		if (hitCount.getAndIncrement() == 0 && logger.isTraceEnabled()) {
			logger.trace("Explanation for why first hit matched:");
			logger.trace(t.getExplanation());
		}
		Hit osuma = new Hit();

		// TODO: organisations are a mess at the moment
		osuma.getText().add(HitText.create(
				"FI",
				extractStringValue(t.getSourceAsMap().get("title")),
				extractStringValue(t.getSourceAsMap().get("abstract")),
				"TODO")); // TODO: <- organisation name
		
		osuma.getText().add(HitText.create(
				"SV",
				extractStringValue(t.getSourceAsMap().get("title_sv")),
				extractStringValue(t.getSourceAsMap().get("abstract_sv")),
				"TODO")); // TODO: <- organisation name

		osuma.getText().add(HitText.create(
				"EN",
				extractStringValue(t.getSourceAsMap().get("title_en")),
				extractStringValue(t.getSourceAsMap().get("abstract_en")),
				"TODO")); // TODO: <- organisation name

		
		osuma.setAbstractUris(extractListValue(t.getSourceAsMap().get(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_URI)));
		osuma.setAbstractTopicUris(extractListValue(t.getSourceAsMap().get(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_MAUI_URI)));
		osuma.setUrl("http://www.paikkatietohakemisto.fi/geonetwork/srv/eng/catalog.search#/metadata/" + t.getId());
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