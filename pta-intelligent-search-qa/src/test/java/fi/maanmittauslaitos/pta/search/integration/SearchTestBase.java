package fi.maanmittauslaitos.pta.search.integration;

import fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.test.ESIntegTestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Objects.requireNonNull;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAllSuccessful;


public abstract class SearchTestBase extends ESIntegTestCase {
	private static final String INDEX_FILE = "index.json";
	private static final String PREFIX = "BOOT-INF/classes/";
	private static final String COMMON_CLASSPATH = PREFIX + "fi/maanmittauslaitos/pta/search/integration/";
	private static final String METADATA_ZIP = COMMON_CLASSPATH + "generatedResourceMetadata.zip";
	private static final String TESTCASE_DIR = COMMON_CLASSPATH + "testcases/";
	private static final int RESULT_SIZE = 10;
	protected static int nDocs;

	protected static URL getResource(String resource) {
		return SearchTestBase.class.getClassLoader().getResource(resource);
	}

	protected static InputStream getResourceAsStream(String resource) {
		return requireNonNull(SearchTestBase.class.getClassLoader().getResourceAsStream(resource));
	}

	private static File newTempOutputFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}

	protected void createIndexAndPopulate() throws IOException, URISyntaxException {
		nDocs = 0;
		File tmpDir = Files.createTempDirectory("pta-SearchTest").toFile();
		tmpDir.deleteOnExit();

		ZipInputStream zipFile = new ZipInputStream(getResourceAsStream(METADATA_ZIP));

		URI uri = getResource(INDEX_FILE).toURI();
		requireNonNull(uri);
		String index = IOUtils.toString(uri.toURL().openStream(), StandardCharsets.UTF_8);

		System.out.println(index);
		CreateIndexResponse response = client().admin().indices().prepareCreate("pta").setSource(index, XContentType.JSON).get();
		assertAcked(response);

		ZipEntry entry;

		while ((entry = zipFile.getNextEntry()) != null) {
			File newFile = newTempOutputFile(tmpDir, entry);
			byte[] buf = new byte[1024];

			FileOutputStream fos = new FileOutputStream(newFile);
			int len;
			while ((len = zipFile.read(buf)) > 0) {
				fos.write(buf, 0, len);
			}
			fos.close();
			zipFile.closeEntry();

			byte[] content = Files.readAllBytes(newFile.toPath());

			Map<String, Object> mapValue = XContentHelper.convertToMap(JsonXContent.jsonXContent, new String(content), false);

			@SuppressWarnings("rawtypes")
			String id = ((Collection) mapValue.get("@id")).iterator().next().toString();

			IndexResponse insertResponse = client().prepareIndex(PTAElasticSearchMetadataConstants.INDEX, PTAElasticSearchMetadataConstants.TYPE, id)
					.setSource(content, XContentType.JSON)
					.get();

			assertEquals(Result.CREATED, insertResponse.getResult());
			nDocs++;
		}
		System.out.println("Inserted " + nDocs + ", waiting until all are searchable");

		refresh();
	}

	protected SearchResponse getSearchResponse(String testCaseName) throws IOException, URISyntaxException {
		return getSearchResponse(testCaseName, RESULT_SIZE);
	}

	protected SearchResponse getSearchResponse(String testCaseName, int resultMaxSize) throws IOException, URISyntaxException {
		URL testCase = getResource(TESTCASE_DIR + testCaseName);
		requireNonNull(testCase, "testCase");
		String queryStr = IOUtils.toString(testCase.openStream(), StandardCharsets.UTF_8);
		return getSearchResponseFromString(queryStr, resultMaxSize);
	}

	protected SearchResponse getSearchResponseFromString(String queryStr, int resultMaxSize) throws IOException, URISyntaxException {

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.size(resultMaxSize);
		sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		sourceBuilder.fetchSource("*", null);

		sourceBuilder.query(QueryBuilders.wrapperQuery(queryStr));

        /*ExplainRequest explainRequest = new ExplainRequest(PTAElasticSearchMetadataConstants.INDEX, "metadata", "03e4a0d0-ee3d-4664-a612-bdf5046679fc");
        explainRequest.query(sourceBuilder.query());
        ExplainResponse explainResponse = client().explain(explainRequest).actionGet();
        Explanation explanation = explainResponse.getExplanation();*/

		SearchRequest request = new SearchRequest(PTAElasticSearchMetadataConstants.INDEX);
		request.types(PTAElasticSearchMetadataConstants.TYPE);
		request.source(sourceBuilder);

		System.out.println("Query:\n" + queryStr);

		SearchResponse response = client().search(request).actionGet();

		assertAllSuccessful(response);


		List<SearchHit> collect = Stream.of(response.getHits().getHits())
				.collect(Collectors.toList());
		collect
				.forEach(hit -> System.out.println("Id: " + hit.getId() + "- - - score: " + hit.getScore()));

		return response;
	}
}
