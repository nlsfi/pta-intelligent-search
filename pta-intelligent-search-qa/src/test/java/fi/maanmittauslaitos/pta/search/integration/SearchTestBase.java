/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package fi.maanmittauslaitos.pta.search.integration;

import fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.main.MainResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.BDDAssertions.then;

/**
 * This class is adapted from https://github.com/dadoonet/elasticsearch-integration-tests
 */

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class SearchTestBase {

	private static final Logger logger = LogManager.getLogger(SearchTestBase.class);
	private static final String INDEX = "ptatestidx";
	private static RestHighLevelClient client;
	private static ElasticsearchContainer container;

	private static final String INDEX_FILE = "index.json";
	private static final String PREFIX = "BOOT-INF/classes/";
	private static final String COMMON_CLASSPATH = PREFIX + "fi/maanmittauslaitos/pta/search/integration/";
	private static final String METADATA_ZIP = COMMON_CLASSPATH + "generatedResourceMetadata.zip";
	private static final String TESTCASE_DIR = COMMON_CLASSPATH + "testcases/";
	private static final int RESULT_SIZE = 10;
	protected static int nDocs;
	protected List<FieldSortBuilder> sortBuilders = Collections.emptyList();

	@BeforeClass
	public static void startElasticsearchRestClient() throws IOException, URISyntaxException {
		int testClusterPort = 9201;
		String testClusterHost = "localhost";
		String testClusterScheme = "http";

		try {
			Properties properties = new Properties();
			try (InputStream is = SearchTestBase.getResourceAsStream("elasticsearch.properties")) {
				properties.load(is);
			}
			testClusterHost = Optional.of(properties.getProperty("integ.elasticsearch.host"))
					.filter(s -> !s.contains("$"))
					.orElse(testClusterHost);
			testClusterScheme = Optional.of(properties.getProperty("integ.elasticsearch.scheme"))
					.filter(s -> !s.contains("$"))
					.orElse(testClusterScheme);
			testClusterPort = Optional.of(properties.getProperty("integ.elasticsearch.port"))
					.filter(s -> !s.contains("$"))
					.map(Integer::parseInt)
					.orElse(testClusterPort);

		} catch (IOException e) {
			logger.error("Error occurred when loading properties file", e);
		}

		// We start a client
		RestClientBuilder builder = getClientBuilder(new HttpHost(testClusterHost, testClusterPort, testClusterScheme));

		// We check that the client is running
		try (RestHighLevelClient elasticsearchClientTemporary = new RestHighLevelClient(builder)) {
			elasticsearchClientTemporary.info();
			logger.info("A node is already running. No need to start a Docker instance.");
		} catch (ConnectException e) {
			logger.info("No node running. We need to start a Docker instance.");
			container = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch-oss:6.2.4");
			container.start();
			logger.info("Docker instance started.");
			testClusterHost = container.getContainerIpAddress();
			testClusterPort = container.getFirstMappedPort();
			testClusterScheme = "http";
		}

		logger.info("Starting a client on {}://{}:{}", testClusterScheme, testClusterHost, testClusterPort);

		// We build the elasticsearch High Level Client based on the parameters
		builder = getClientBuilder(new HttpHost(testClusterHost, testClusterPort, testClusterScheme));
		client = new RestHighLevelClient(builder);

		// We make sure the cluster is running
		MainResponse info = client.info();
		logger.info("Client is running against an elasticsearch cluster {}.", info.getVersion().toString());

		createIndexAndPopulate();
	}

	@AfterClass
	public static void stopElasticsearchRestClient() throws IOException {
		if (client != null) {
			logger.info("Closing elasticsearch client.");
			client.close();
		}
		if (container != null) {
			logger.info("Stopping Docker instance.");
			container.close();
		}
	}

	private static RestClientBuilder getClientBuilder(HttpHost host) {
		return RestClient.builder(host);
	}


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

	protected static void createIndexAndPopulate() throws IOException, URISyntaxException {
		// We remove any existing index
		try {
			logger.info("-> Removing index {}.", INDEX);
			client.indices().delete(new DeleteIndexRequest(INDEX));
		} catch (ElasticsearchStatusException e) {
			then(e.status().getStatus()).isEqualTo(404);
		}

		nDocs = 0;
		File tmpDir = Files.createTempDirectory("pta-SearchTest").toFile();
		tmpDir.deleteOnExit();

		ZipInputStream zipFile = new ZipInputStream(getResourceAsStream(METADATA_ZIP));

		URI uri = getResource(INDEX_FILE).toURI();
		requireNonNull(uri);
		String index = IOUtils.toString(uri.toURL().openStream(), StandardCharsets.UTF_8);

		logger.debug(index);

		CreateIndexRequest createIndexRequest = new CreateIndexRequest(INDEX);
		createIndexRequest.source(index, XContentType.JSON);

		client.indices().create(createIndexRequest);

		ZipEntry entry;

		BulkRequest bulkRequest = new BulkRequest()
				.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

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

			bulkRequest.add(new IndexRequest(INDEX, PTAElasticSearchMetadataConstants.TYPE, id)
					.source(content, XContentType.JSON)

			);
			nDocs++;
		}

		BulkResponse r = client.bulk(bulkRequest);

		then(r.hasFailures()).isFalse();

		then(r.getItems())
				.hasSize(nDocs)
				.allMatch(bulkItemResponse ->
						bulkItemResponse.getResponse().getResult() == DocWriteResponse.Result.CREATED);

		logger.info("Inserted " + nDocs + ", waiting until all are searchable");
	}


	SearchResponse getSearchResponse(String testCaseName) throws IOException, URISyntaxException {
		return getSearchResponse(testCaseName, RESULT_SIZE, Optional.empty());
	}

	SearchResponse getSearchResponse(String testCaseName, int resultMaxSize) throws IOException, URISyntaxException {
		return getSearchResponse(testCaseName, resultMaxSize, Optional.empty());
	}

	SearchResponse getSearchResponse(String testCaseName, String explainId) throws IOException, URISyntaxException {
		return getSearchResponse(testCaseName, RESULT_SIZE, Optional.of(explainId));
	}

	SearchResponse getSearchResponse(String testCaseName, int resultMaxSize, String explainId) throws IOException, URISyntaxException {
		return getSearchResponse(testCaseName, resultMaxSize, Optional.of(explainId));
	}

	SearchResponse getSearchResponse(String testCaseName, int resultMaxSize, Optional<String> explainId) throws IOException, URISyntaxException {
		URL testCase = getResource(TESTCASE_DIR + testCaseName);
		requireNonNull(testCase, "testCase");
		String queryStr = IOUtils.toString(testCase.openStream(), StandardCharsets.UTF_8);
		return getSearchResponseFromString(queryStr, resultMaxSize, explainId);
	}

	SearchResponse getSearchResponseFromString(String queryStr, int resultMaxSize, Optional<String> explainId) throws IOException {

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.size(resultMaxSize);
		sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		sourceBuilder.fetchSource("*", null);

		sourceBuilder.query(QueryBuilders.wrapperQuery(queryStr));

		sortBuilders.forEach(sourceBuilder::sort);

		//TODO: Fix explain
        /*explainId.ifPresent(id -> {
            ExplainRequest explainRequest = new ExplainRequest(PTAElasticSearchMetadataConstants.INDEX, "metadata", id);
            explainRequest.query(sourceBuilder.query());
            client.search(explainRequest)
            Explanation explanation = client.explain(explainRequest).actionGet().getExplanation();
            logger.debug("-------------------------\n" +
                    "EXPLANATION:\n\n" + explanation + "" +
                    "-------------------------");
        });*/

		SearchRequest request = new SearchRequest(INDEX);
		request.types(PTAElasticSearchMetadataConstants.TYPE);
		request.source(sourceBuilder);

		logger.debug("Query:\n" + queryStr);

		SearchResponse response = client.search(request);

		List<SearchHit> collect = Stream.of(response.getHits().getHits())
				.collect(Collectors.toList());
		collect
				.forEach(hit -> logger.debug("Id: " + hit.getId() + "- - - score: " + hit.getScore()));

		return response;
	}
}
