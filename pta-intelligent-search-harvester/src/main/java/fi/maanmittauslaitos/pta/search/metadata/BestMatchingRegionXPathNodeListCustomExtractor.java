package fi.maanmittauslaitos.pta.search.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.maanmittauslaitos.pta.search.documentprocessor.XPathNodeListCustomExtractor;
import fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants;
import fi.maanmittauslaitos.pta.search.utils.Region;
import fi.maanmittauslaitos.pta.search.utils.RegionFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static fi.maanmittauslaitos.pta.search.utils.Region.RegionScore.EMPTY_SCORE;

public class BestMatchingRegionXPathNodeListCustomExtractor implements XPathNodeListCustomExtractor {

	private static final double WHOLE_COUNTRY_SCORE_THRESHOLD = 0.6;
	private static final double WHOLE_REGION_SCORE_THRESHOLD = 0.75;
	private static final double WHOLE_SUBREGION_SCORE_THRESHOLD = 0.75;

	private static final Logger logger = Logger.getLogger(BestMatchingRegionXPathNodeListCustomExtractor.class);

	private final ObjectMapper objectMapper;
	private final Map<String, Region> countries;
	private final Map<String, Region> regions;
	private final Map<String, Region> subregions;
	private final Map<String, Region> municipalities;
	private final GeographicBoundingBoxXPathCustomExtractor originalBBoxExtractor;

	private BestMatchingRegionXPathNodeListCustomExtractor(ObjectMapper objectMapper, Map<String, Region> countries, Map<String, Region> regions,
														   Map<String, Region> subregions, Map<String, Region> municipalities,
														   GeographicBoundingBoxXPathCustomExtractor originalBBoxExtractor) {
		this.objectMapper = objectMapper;
		this.countries = countries;
		this.regions = regions;
		this.subregions = subregions;
		this.municipalities = municipalities;
		this.originalBBoxExtractor = originalBBoxExtractor;
	}

	public static BestMatchingRegionXPathNodeListCustomExtractor create(ObjectMapper objectMapper, Map<String, Region> countries, Map<String, Region> regions,
																		Map<String, Region> subregions, Map<String, Region> municipalities,
																		GeographicBoundingBoxXPathCustomExtractor originalExtractor) {
		return new BestMatchingRegionXPathNodeListCustomExtractor(objectMapper, countries, regions, subregions, municipalities, originalExtractor);
	}

	@Override
	public Object process(XPath xPath, NodeList nodeList) throws XPathException {
		Region dataRegion = getCommonDataRegion(xPath, nodeList);
		if (dataRegion == null) {
			return objectMapper.createObjectNode();
		}

		Region.RegionScore country = getBestRegionScore(countries, dataRegion);
		Region.RegionScore region = getBestRegionScore(regions, dataRegion);
		Region.RegionScore subregion = getBestRegionScore(subregions, dataRegion);
		Region.RegionScore municipality = getBestRegionScore(municipalities, dataRegion);

		String nameField = PTAElasticSearchMetadataConstants.FIELD_BEST_MATCHING_REGIONS_NAME;
		String scoreField = PTAElasticSearchMetadataConstants.FIELD_BEST_MATCHING_REGIONS_SCORE;
		String countryField = PTAElasticSearchMetadataConstants.FIELD_BEST_MATCHING_REGIONS_COUNTRY;
		String regionField = PTAElasticSearchMetadataConstants.FIELD_BEST_MATCHING_REGIONS_REGION;
		String subregionfield = PTAElasticSearchMetadataConstants.FIELD_BEST_MATCHING_REGIONS_SUBREGION;
		String municipalityField = PTAElasticSearchMetadataConstants.FIELD_BEST_MATCHING_REGIONS_MUNICIPALITY;


		String template = "{\n" +
				"  \"" + countryField + "\": {\n" +
				"    \"" + nameField + "\": \"%s\",\n" +
				"    \"" + scoreField + "\": %.4f\n" +
				"  },\n" +
				"  \"" + regionField + "\": {\n" +
				"    \"" + nameField + "\": \"%s\",\n" +
				"    \"" + scoreField + "\": %.4f\n" +
				"  },\n" +
				"  \"" + subregionfield + "\": {\n" +
				"    \"" + nameField + "\": \"%s\",\n" +
				"    \"" + scoreField + "\": %.4f\n" +
				"  },\n" +
				"  \"" + municipalityField + "\": {\n" +
				"    \"" + nameField + "\": \"%s\",\n" +
				"    \"" + scoreField + "\": %.4f\n" +
				"  }\n" +
				"}";
		String jsonString = "";
		JsonNode value = null;
		try {
			if (country.getScore() >= WHOLE_COUNTRY_SCORE_THRESHOLD) {
				jsonString = String.format(template, country.getRegionName(), country.getScore(),
						EMPTY_SCORE.getRegionName(), EMPTY_SCORE.getScore(),
						EMPTY_SCORE.getRegionName(), EMPTY_SCORE.getScore(),
						EMPTY_SCORE.getRegionName(), EMPTY_SCORE.getScore());
			} else if (region.getScore() >= WHOLE_REGION_SCORE_THRESHOLD) {
				jsonString = String.format(template, country.getRegionName(), country.getScore(),
						region.getRegionName(), region.getScore(),
						EMPTY_SCORE.getRegionName(), EMPTY_SCORE.getScore(),
						EMPTY_SCORE.getRegionName(), EMPTY_SCORE.getScore());
			} else if (subregion.getScore() >= WHOLE_SUBREGION_SCORE_THRESHOLD) {
				jsonString = String.format(template, country.getRegionName(), country.getScore(),
						region.getRegionName(), region.getScore(),
						subregion.getRegionName(), subregion.getScore(),
						EMPTY_SCORE.getRegionName(), EMPTY_SCORE.getScore());
			} else {
				jsonString = String.format(template, country.getRegionName(), country.getScore(),
						region.getRegionName(), region.getScore(),
						subregion.getRegionName(), subregion.getScore(),
						municipality.getRegionName(), municipality.getScore());
			}
			value = objectMapper.readTree(jsonString);
		} catch (IOException e) {
			logger.error("Could not parse string to json: " + jsonString);
		}
		return Optional.ofNullable(value).orElse(objectMapper.createObjectNode());
	}

	private Region.RegionScore getBestRegionScore(Map<String, Region> regionType, Region dataRegion) {
		return regionType.entrySet().stream()//
				.filter(nameRegionEntry -> nameRegionEntry.getValue().intersects(dataRegion))
				.max(Comparator.comparing(entry -> entry.getValue().getIntersection(dataRegion)))
				.map(entry -> Region.RegionScore.create(entry.getKey(), entry.getValue().getIntersectionDividedByArea(dataRegion)))
				.orElse(EMPTY_SCORE);
	}

	@SuppressWarnings("unchecked")
	private Region getCommonDataRegion(XPath xPath, NodeList nodeList) throws XPathException {
		//For loop to get the XPathException thrown
		List<List<Double>> coordinatesList = new ArrayList<>();
		for (int i = 0; i < nodeList.getLength(); ++i) {
			coordinatesList.add((List<Double>) originalBBoxExtractor.process(xPath, nodeList.item(i)));
		}

		return coordinatesList.stream()
				.filter(obj -> !Objects.isNull(obj)).filter(t -> !t.isEmpty())
				.map(RegionFactory::create)
				.reduce(Region::getCommonRegion)
				.orElse(null);
	}
}
