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

public class Region {

    private static final Logger logger = Logger.getLogger(HarvesterConfig.class);

    private final List<Double> coordinates;

    public Region(List<Double> coordinates) {
        this.coordinates = coordinates;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    private Double left() {
        return coordinates.get(0);
    }

    private Double right() {
        return coordinates.get(2);
    }

    private Double top() {
        return coordinates.get(3);
    }

    private Double bottom() {
        return coordinates.get(1);
    }


    public Double getArea() {
        return (right() - left()) * (top() - bottom());
    }

    public Double getIntersection(Region r2) {
        Double deltaX = Math.max(0, Math.min(right(), r2.right()) - Math.max(left(), r2.left()));
        Double deltaY = Math.max(0, Math.min(top(), r2.top()) - Math.max(bottom(), r2.bottom()));
        return deltaX * deltaY;
    }

    public Double getIntersectionDividedByArea(Region r2) {
        return intersects(r2) ? getIntersection(r2) / getArea() : 0.0;
    }

    public boolean intersects(Region r2) {
        return !(left() > r2.right() || r2.left() > right() || bottom() > r2.top() || r2.bottom() > top());
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
                featureMap.put(feature.get("properties").get("nimi").textValue(), new Region(envelope));
            });
        } catch (IOException e) {
            logger.error("Could not read resource file as json" + resource, e);
        }
        assert !featureMap.isEmpty();
        return featureMap;
    }

    public static class RegionScore {

        private String regionName;
        private Double score;

        public static RegionScore create(String regionName, Double score) {
            return new RegionScore(regionName, score);
        }

        public static RegionScore createEmpty() {
            return new RegionScore("", 0.0);
        }

        RegionScore(String regionName, Double score) {
            this.regionName = regionName;
            this.score = score;
        }

        public String getRegionName() {
            return regionName;
        }

        public Double getScore() {
            return score;
        }
    }
}
