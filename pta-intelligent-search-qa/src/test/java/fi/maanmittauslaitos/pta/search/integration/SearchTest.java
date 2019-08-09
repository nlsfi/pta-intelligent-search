package fi.maanmittauslaitos.pta.search.integration;

import com.carrotsearch.randomizedtesting.annotations.Seed;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap;
import fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants;
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
import org.elasticsearch.test.ESIntegTestCase.ClusterScope;
import org.elasticsearch.test.ESIntegTestCase.Scope;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.BDDAssertions.then;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAllSuccessful;


@Ignore
@ClusterScope(scope = Scope.SUITE)
@ThreadLeakScope(ThreadLeakScope.Scope.NONE)
@Seed("2A")
@RunWith(com.carrotsearch.randomizedtesting.RandomizedRunner.class)
public class SearchTest extends ESIntegTestCase {

    private static final String RESOURCE_CLASSPATH = "/fi/maanmittauslaitos/pta/search/integration/testcases/";
    private static final int RESULT_SIZE = 10;
    private int nDocs;

    @Before
    public void createIndexAndPopulate() throws IOException, URISyntaxException {
        nDocs = 0;
        File tmpDir = Files.createTempDirectory("pta-SearchTest").toFile();
        tmpDir.deleteOnExit();

        ZipInputStream zipFile = new ZipInputStream(SearchTest.class.getResourceAsStream("/fi/maanmittauslaitos/pta/search/integration/generatedResourceMetadata.zip"));

        String index = new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("index.json").toURI())));
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

            IndexResponse insertResponse = client().prepareIndex("pta", "metadata", id)
                    .setSource(content, XContentType.JSON)
                    .get();

            assertEquals(Result.CREATED, insertResponse.getResult());
            nDocs++;
        }
        System.out.println("Inserted " + nDocs + ", waiting until all are searchable");

        refresh();
    }

    public static File newTempOutputFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private SearchResponse getSearchResponse(String testCaseName) throws IOException, URISyntaxException {
        return getSearchResponse(testCaseName, RESULT_SIZE);
    }

    private SearchResponse getSearchResponse(String testCaseName, int resultMaxSize) throws IOException, URISyntaxException {
        URL testCase = getClass().getResource(RESOURCE_CLASSPATH + testCaseName);
        Objects.requireNonNull(testCase, "testCase");
        String queryStr = new String(Files.readAllBytes(Paths.get(testCase.toURI())));
        return getSearchResponseFromString(queryStr, resultMaxSize);
    }

    private SearchResponse getSearchResponseFromString(String queryStr, int resultMaxSize) throws IOException, URISyntaxException {

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

    @Test
    public void emptyQueryReturnsAll() throws Exception {
        SearchResponse response = getSearchResponse("testcase_empty.json", nDocs);

        then(response.getHits()).hasSize(nDocs);
    }

    @Test
    public void suomiReturnsAllNationalData() throws Exception {
        SearchResponse response = getSearchResponse("testcase_suomi.json", nDocs);
        then(response.getHits()).hasSize(61);
    }

    @Test
    public void nationalReturnsAllNationalData() throws Exception {
        SearchResponse response = getSearchResponse("testcase_kansallinen.json", nDocs);
        then(response.getHits()).hasSize(61);
    }


    @Test
    public void testMiddleFinland() throws IOException, URISyntaxException {
        SearchResponse response = getSearchResponse("testcase_keski-suomi.json");

        then(response.getHits())
                .extracting(SearchHit::getId)
                .containsExactly("89c6a379-776f-4529-b79d-a456177fb64d"); //jkl

    }

    @Test
    public void jklBeforeSalo() throws Exception {
        SearchResponse response = getSearchResponse("testcase_jyväskylä_tiet.json");

        List<String> ids = Arrays.asList(
                "89c6a379-776f-4529-b79d-a456177fb64d", //jkl
                "52bf65f7-db98-44ac-8da3-0b06fdf71d65" // salo
        );

        then(response.getHits())
                .extracting(SearchHit::getId)
                .containsSubsequence(ids.get(0), ids.get(1));
    }

    @Test
    @Ignore("Does not work just yet")
    public void HSLBeforeHSY() throws Exception {
        SearchResponse response = getSearchResponse("testcase_hsl.json");

        List<String> hslIds = Arrays.asList(
                "d52b5fae-6139-4182-858c-8602608dd0a4",
                "4d260bcd-eaf7-4bb1-bdbb-ddc924000089",
                "109589be-37cd-49a5-b950-4453a2a16c3b",
                "c176b773-9672-4f39-8ae8-3647d2f54ab4",
                "fd06055c-31e1-476f-b91c-8f8f50548660");

        List<String> hsyIds = Arrays.asList(
                "a93a10c6-a3dc-46f3-8ab9-260f423a4b9e"
        );

        then(response.getHits())
                .extracting(SearchHit::getId)
                .containsAll(hslIds)
                .containsSubsequence(hslIds.get(0), hsyIds.get(0));
    }

    @Test
    public void uusimaaContainsMunicipalities() throws Exception {
        SearchResponse response = getSearchResponse("testcase_uusimaa.json", 200);
        List<String> ids = Arrays.asList(
                "be2440a5-b31c-482b-be2b-e59f98f49272", // Uusimaa
                "eca4aba3-d145-46f7-9547-a2ccfe4bf1b3", // pk-seutu
                "03e4a0d0-ee3d-4664-a612-bdf5046679fc", // Helsinki
                "c163fe28-263a-4372-9078-6941646c6be2", // Kerava
                "fdca5145-bc5d-4c67-b029-99a2d5801d9e" // Porvoo
        );

        then(response.getHits())
                .extracting(SearchHit::getId)
                .containsAll(ids)
                .containsSubsequence(ids.get(0), ids.get(1), ids.get(2));
    }

}
