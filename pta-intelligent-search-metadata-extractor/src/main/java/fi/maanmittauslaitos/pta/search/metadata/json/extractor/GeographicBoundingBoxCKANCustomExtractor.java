package fi.maanmittauslaitos.pta.search.metadata.json.extractor;

import fi.maanmittauslaitos.pta.search.documentprocessor.query.JsonDocumentQueryImpl;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.JsonQueryResultImpl;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GeographicBoundingBoxCKANCustomExtractor extends JsonPathCustomExtractor {

	private static Logger logger = LoggerFactory.getLogger(GeographicBoundingBoxCKANCustomExtractor.class);

	public GeographicBoundingBoxCKANCustomExtractor() {
		super();
	}

	public GeographicBoundingBoxCKANCustomExtractor(boolean isThrowException) {
		super(isThrowException);
	}

	@Override
	public Object process(JsonDocumentQueryImpl query, QueryResult queryResult) throws RuntimeException {
		Double[] ret = new Double[4];

		List<List<Double>> coordinates;
		try {
			// From Geojson spec:
			// 	longitude and latitude, or easting and northing, precisely in that order and
			// 	using decimal numbers
			//noinspection unchecked
			coordinates = (List<List<Double>>) ((JsonQueryResultImpl) queryResult).getGenericRawValue();

			if (coordinates.isEmpty()) {
				return null;
			}

			Set<Double> Xs = coordinates.stream()
					.map(doubles -> doubles.get(0))
					.collect(Collectors.toSet());

			Set<Double> Ys = coordinates.stream()
					.map(doubles -> doubles.get(1))
					.collect(Collectors.toSet());

			ret[0] = Xs.stream().min(Double::compareTo).orElseThrow(MissingCoordException::new);
			ret[1] = Ys.stream().min(Double::compareTo).orElseThrow(MissingCoordException::new);
			ret[2] = Xs.stream().max(Double::compareTo).orElseThrow(MissingCoordException::new);
			ret[3] = Ys.stream().max(Double::compareTo).orElseThrow(MissingCoordException::new);

		} catch (RuntimeException e) {
			if (e instanceof MissingCoordException) {
				return null;
			} else {
				handleExtractorException(e, null);
			}
		}

		return Arrays.asList(ret);
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

	private static class MissingCoordException extends RuntimeException {
		private static final long serialVersionUID = 1L;

	}
}
