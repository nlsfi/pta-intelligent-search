package fi.maanmittauslaitos.pta.search.api.region;

import java.util.List;

public interface RegionNameDetector {
	public boolean containsRegionalName(List<String> query);
}
