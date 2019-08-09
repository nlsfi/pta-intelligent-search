package fi.maanmittauslaitos.pta.search.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import fi.maanmittauslaitos.pta.search.HarvesterConfig;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionFactory {
	private static final Logger logger = Logger.getLogger(RegionImpl.class);

	public static Region create(List<Double> coordinates) {
		return new RegionImpl(coordinates);
	}


	public static Map<String, Region> readRegionResource(ObjectMapper objectMapper, ObjectReader listReader, String resource) {
		Map<String, Region> featureMap = new HashMap<>();

		try {
			JsonNode jsonFile = objectMapper.readTree(HarvesterConfig.class.getClassLoader().getResource(resource));
			JsonNode features = jsonFile.get("features");

			features.forEach(feature -> {
				List<Double> envelope = Collections.emptyList();
				try {
					envelope = listReader.readValue(feature.get("properties").get("envelope"));
				} catch (IOException e) {
					logger.error("Could not read envelope field as list in resource file " + resource, e);
				}
				featureMap.put(feature.get("properties").get("nimi").textValue(), new RegionImpl(envelope));
			});
		} catch (IOException e) {
			logger.error("Could not read resource file as json" + resource, e);
		}
		assert !featureMap.isEmpty();
		return featureMap;
	}
}
