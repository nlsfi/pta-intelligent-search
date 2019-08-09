package fi.maanmittauslaitos.pta.search.utils;

public interface Region {
	public Double getArea();

	Double getIntersection(Region r2);

	Double getIntersectionDividedByArea(Region r2);

	boolean intersects(Region r2);

	class RegionScore {
		public static final RegionScore EMPTY_SCORE = new RegionScore("", 0.0);
		private String regionName;
		private Double score;

		public static RegionScore create(String regionName, Double score) {
			return new RegionScore(regionName, score);
		}

		private RegionScore(String regionName, Double score) {
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
