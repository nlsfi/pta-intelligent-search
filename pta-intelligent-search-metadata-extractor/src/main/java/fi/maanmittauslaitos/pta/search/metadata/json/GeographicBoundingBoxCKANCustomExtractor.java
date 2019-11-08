package fi.maanmittauslaitos.pta.search.metadata.json;

import fi.maanmittauslaitos.pta.search.documentprocessor.CustomExtractor;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.DocumentQuery;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.JsonDocumentQueryImpl;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.JsonQueryResultImpl;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.QueryResult;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GeographicBoundingBoxCKANCustomExtractor implements CustomExtractor {

	@Override
	public Object process(DocumentQuery documentQuery, QueryResult queryResult) throws DocumentProcessingException {
		Double[] ret = new Double[4];

		if (!(documentQuery instanceof JsonDocumentQueryImpl)) {
			throw new DocumentProcessingException("This extractor should only be used for Json Documents");
		}

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
			return null;
		}

		return Arrays.asList(ret);
	}

	private static class MissingCoordException extends RuntimeException {
		private static final long serialVersionUID = 1L;

	}
}
