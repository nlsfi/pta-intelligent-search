package fi.maanmittauslaitos.pta.search.api.region;

import fi.maanmittauslaitos.pta.search.api.Language;
import fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants;

import java.util.List;
import java.util.Map;

public interface RegionNameContainer {
	Map<RegionType, List<String>> getRegionNamesByRegionType();

	Map<Language, Map<String, String>> getStemmedRegionNames();

	enum RegionType {
		COUNTRY(PTAElasticSearchMetadataConstants.FIELD_BEST_MATCHING_REGIONS_COUNTRY),
		REGION(PTAElasticSearchMetadataConstants.FIELD_BEST_MATCHING_REGIONS_REGION),
		SUBREGION(PTAElasticSearchMetadataConstants.FIELD_BEST_MATCHING_REGIONS_SUBREGION),
		MUNICIPALITY(PTAElasticSearchMetadataConstants.FIELD_BEST_MATCHING_REGIONS_MUNICIPALITY);

		private final String type;

		RegionType(String type) {
			this.type = type;
		}

		public String getType() {
			return this.type;
		}
	}
}
