package fi.maanmittauslaitos.pta.search.metadata;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.DocumentQuery;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.QueryResult;
import fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants;
import fi.maanmittauslaitos.pta.search.metadata.extractor.GeographicBoundingBoxXmlCustomExtractor;
import fi.maanmittauslaitos.pta.search.utils.Region;
import fi.maanmittauslaitos.pta.search.utils.RegionFactory;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.xml.xpath.XPathException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static fi.maanmittauslaitos.pta.search.utils.Region.RegionScore.EMPTY_SCORE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.data.Percentage.withPercentage;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class BestMatchingRegionListCustomExtractorTest {


	private static final ImmutableMap<String, Region> countries = ImmutableMap.of(
			"Suomi", RegionFactory.create(Arrays.asList(19.45, 59.78, 31.61, 70.12)));
	private static final ImmutableMap<String, Region> regions = ImmutableMap.of(
			"Keski-Suomi", RegionFactory.create(Arrays.asList(24.01, 61.45, 26.78, 63.61)));
	private static final ImmutableMap<String, Region> subregions = ImmutableMap.of(
			"Jyväskylä", RegionFactory.create(Arrays.asList(25.26, 61.84, 26.05, 62.43)));
	private static final ImmutableMap<String, Region> municipalities = ImmutableMap.of(
			"Jyväskylä", RegionFactory.create(Arrays.asList(25.26078, 61.83952, 26.25, 62.5)),
			"Jämsä", RegionFactory.create(Arrays.asList(24.52, 61.63, 25.60, 62.21)));

	private static final String nameField = PTAElasticSearchMetadataConstants.FIELD_BEST_MATCHING_REGIONS_NAME;
	private static final String scoreField = PTAElasticSearchMetadataConstants.FIELD_BEST_MATCHING_REGIONS_SCORE;
	private static final String countryField = PTAElasticSearchMetadataConstants.FIELD_BEST_MATCHING_REGIONS_COUNTRY;
	private static final String regionField = PTAElasticSearchMetadataConstants.FIELD_BEST_MATCHING_REGIONS_REGION;
	private static final String subregionfield = PTAElasticSearchMetadataConstants.FIELD_BEST_MATCHING_REGIONS_SUBREGION;
	private static final String municipalityField = PTAElasticSearchMetadataConstants.FIELD_BEST_MATCHING_REGIONS_MUNICIPALITY;

	@Rule
	public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

	@Mock
	private GeographicBoundingBoxXmlCustomExtractor mockBBoxExtractor;
	@Mock
	private DocumentQuery mockDocumentQuery;
	@Mock
	private List<QueryResult> mockQueryResultList;


	private BestMatchingRegionListCustomExtractor extractor;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(mockQueryResultList.size()).thenReturn(1);
		ObjectMapper objectMapper = new ObjectMapper();
		extractor = BestMatchingRegionListCustomExtractor.create(
				objectMapper, countries, regions, subregions, municipalities, mockBBoxExtractor, Collections.emptyList());
	}


	@Test
	public void testWithSingleBbox() throws XPathException, DocumentProcessingException {
		when(mockBBoxExtractor.process(any(DocumentQuery.class), any(QueryResult.class))).thenReturn(Arrays.asList(25.26078, 61.83952, 26.25, 62.5));

		JsonNode processed = (JsonNode) extractor.process(mockDocumentQuery, mockQueryResultList);
		assertThat(processed).isNotEmpty();

		softly.assertThat(processed.get(countryField).get(nameField).textValue()).isEqualTo("Suomi");
		softly.assertThat(processed.get(regionField).get(nameField).textValue()).isEqualTo("Keski-Suomi");
		softly.assertThat(processed.get(subregionfield).get(nameField).textValue()).isEqualTo("Jyväskylä");
		softly.assertThat(processed.get(subregionfield).get(scoreField).asDouble())
				.isCloseTo(1.0, withPercentage(5));
		softly.assertThat(processed.get(municipalityField).get(nameField).textValue()).isEqualTo(EMPTY_SCORE.getRegionName());
		softly.assertThat(processed.get(municipalityField).get(scoreField).asDouble()).isEqualTo(EMPTY_SCORE.getScore());

	}

	@Test
	public void testWithMultipleBbox() throws XPathException, DocumentProcessingException {
		when(mockQueryResultList.size()).thenReturn(7);
		when(mockBBoxExtractor.process(any(DocumentQuery.class), any(QueryResult.class))).thenReturn(
				Arrays.asList(25.1141, 62.0671, 25.1604, 62.0902),
				Arrays.asList(25.135, 61.9691, 25.1652, 61.9898),
				Arrays.asList(25.1065, 61.8406, 25.2356, 61.9622),
				Arrays.asList(25.2507, 61.8688, 25.3983, 61.9011),
				Arrays.asList(25.1879, 61.8025, 25.2658, 61.8246),
				Arrays.asList(24.7598, 61.7171, 24.815, 61.7429),
				Arrays.asList(24.7446, 61.8451, 24.8593, 61.8709)
		);

		JsonNode processed = (JsonNode) extractor.process(mockDocumentQuery, mockQueryResultList);
		assertThat(processed).isNotEmpty();

		softly.assertThat(processed.get(countryField).get(nameField).textValue()).isEqualTo("Suomi");
		softly.assertThat(processed.get(regionField).get(nameField).textValue()).isEqualTo("Keski-Suomi");
		softly.assertThat(processed.get(municipalityField).get(nameField).textValue()).isEqualTo("Jämsä");
		softly.assertThat(processed.get(municipalityField).get(scoreField).asDouble())
				.isCloseTo(0.4, withPercentage(5));
	}

	@Test
	public void testNullBBox() throws XPathException, DocumentProcessingException {
		when(mockBBoxExtractor.process(any(DocumentQuery.class), any(QueryResult.class))).thenReturn(null);
		assertThat((JsonNode) extractor.process(mockDocumentQuery, mockQueryResultList)).isEmpty();
	}

	@Test
	public void testEmptyBBox() throws XPathException, DocumentProcessingException {
		when(mockBBoxExtractor.process(any(DocumentQuery.class), any(QueryResult.class))).thenReturn(Collections.emptyList());
		assertThat((JsonNode) extractor.process(mockDocumentQuery, mockQueryResultList)).isEmpty();
	}

	@Test
	public void testXPathException() throws XPathException, DocumentProcessingException {
		String message = "testException";
		when(mockBBoxExtractor.process(any(DocumentQuery.class), any(QueryResult.class))).thenThrow(new XPathException(message));
		assertThatExceptionOfType(XPathException.class).isThrownBy(() -> extractor.process(mockDocumentQuery, mockQueryResultList))
				.withMessage(message);
	}
}