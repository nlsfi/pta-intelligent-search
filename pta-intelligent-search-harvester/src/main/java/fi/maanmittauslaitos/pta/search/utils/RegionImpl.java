package fi.maanmittauslaitos.pta.search.utils;

import java.util.List;

public class RegionImpl implements Region {

	private final List<Double> coordinates;

	RegionImpl(List<Double> coordinates) {
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


	@Override
	public Double getArea() {
		return (right() - left()) * (top() - bottom());
	}

	@Override
	public Double getIntersection(Region other) {
		RegionImpl r2 = (RegionImpl) other;
		Double deltaX = Math.max(0, Math.min(right(), r2.right()) - Math.max(left(), r2.left()));
		Double deltaY = Math.max(0, Math.min(top(), r2.top()) - Math.max(bottom(), r2.bottom()));
		return deltaX * deltaY;
	}

	@Override
	public Double getIntersectionDividedByArea(Region r2) {
		return intersects(r2) ? getIntersection(r2) / getArea() : 0.0;
	}

	@Override
	public boolean intersects(Region other) {
		RegionImpl r2 = (RegionImpl) other;
		return !(left() > r2.right() || r2.left() > right() || bottom() > r2.top() || r2.bottom() > top());
	}

}
