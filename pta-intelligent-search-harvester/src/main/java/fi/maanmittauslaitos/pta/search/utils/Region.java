package fi.maanmittauslaitos.pta.search.utils;

import java.util.List;

public class Region {

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

    public boolean intersects(Region r2) {
        return !(left() > r2.right() || r2.left() > right()) || (bottom() > r2.top() || r2.bottom() > top());
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
