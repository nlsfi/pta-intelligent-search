package fi.maanmittauslaitos.pta.search.api.region;

import fi.maanmittauslaitos.pta.search.api.Language;

import java.util.List;
import java.util.Map;

public interface RegionNameContainer {
	Map<RegionType, List<String>> getRegionNamesByRegionType();

	Map<Language, Map<String, String>> getStemmedRegionNames();

	enum RegionType {
		COUNTRY("country"),
		REGION("region"),
		SUBREGION("subregion"),
		MUNICIPALITY("municipality");

		private final String type;

		RegionType(String type) {
			this.type = type;
		}

		public String getType() {
			return this.type;
		}
	}
}
