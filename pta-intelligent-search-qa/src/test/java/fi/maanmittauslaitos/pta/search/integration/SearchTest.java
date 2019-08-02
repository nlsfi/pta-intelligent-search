package fi.maanmittauslaitos.pta.search.integration;

import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants;
import fi.maanmittauslaitos.pta.search.utils.LocalResourceMetadataGenerator;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAcked;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.assertAllSuccessful;

//import static org.junit.Assert.*;

@ClusterScope(scope = Scope.SUITE)
@ThreadLeakScope(ThreadLeakScope.Scope.NONE)
@RunWith(com.carrotsearch.randomizedtesting.RandomizedRunner.class)
public class SearchTest extends ESIntegTestCase {

    private Client client;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.client = client();
    }

    @Before
    public void createIndexAndPopulate() throws IOException, URISyntaxException {
        ZipInputStream zipFile2 = new ZipInputStream(LocalResourceMetadataGenerator.class.getResourceAsStream("/fi/maanmittauslaitos/pta/search/integration/generatedResourceMetadata.zip"));

        ZipInputStream zipFile = new ZipInputStream(SearchTest.class.getResourceAsStream("/test-documents-1.zip"));



        String index = new String(Files.readAllBytes(Paths.get(getClass().getResource("index.json").toURI())));
        System.out.println(index);
        CreateIndexResponse response = client().admin().indices().prepareCreate("pta").setSource(index, XContentType.JSON).get();
        assertAcked(response);




        ZipEntry entry;
        int nDocs = 0;
        while ((entry = zipFile.getNextEntry()) != null) {

            byte[] buf = new byte[(int) entry.getSize()];
            IOUtils.readFully(zipFile, buf);
            zipFile.closeEntry();

            Map<String, Object> mapValue = XContentHelper.convertToMap(JsonXContent.jsonXContent, new String(buf), false);

            @SuppressWarnings("rawtypes")
            String id = ((Collection) mapValue.get("@id")).iterator().next().toString();

            IndexResponse insertResponse = client().prepareIndex("pta", "metadata", id)
                    .setSource(buf, XContentType.JSON)
                    .get();

            assertEquals(Result.CREATED, insertResponse.getResult());
            nDocs++;
        }
        System.out.println("Inserted " + nDocs + ", waiting until all are searchable");
        assertEquals(100, nDocs);

        refresh();
    }

    @Test
    public void searchUsingMauiParent() throws Exception {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        sourceBuilder.fetchSource("*", null);

        String queryStr = new String(Files.readAllBytes(Paths.get(getClass().getResource("1.json").toURI())));
        sourceBuilder.query(QueryBuilders.wrapperQuery(queryStr));

        SearchRequest request = new SearchRequest(PTAElasticSearchMetadataConstants.INDEX);
        request.types(PTAElasticSearchMetadataConstants.TYPE);
        request.source(sourceBuilder);

        System.out.println("Query:\n" + queryStr);

        SearchResponse response = client().search(request).actionGet();

        assertAllSuccessful(response);

        System.out.println("response:");
        System.out.println(response);


        Set<String> ids = Sets.newSet(
                "0d37d33d-86bc-49cc-9b4b-4d1f005ace56",
                "a7889960-2d49-4129-bc6a-7f871710425e",
                "a959b07c-16ff-4ed0-9a3b-72d6bbc026ff",
                "c4c69e7f-2b40-45aa-a832-46d9fce8b32c",
                "cb067ce0-9ec6-432a-8d1a-4a2944fc476a",
                "ddad3347-05ca-401a-b746-d883d4110180"
        );

        assertEquals(ids.size(), response.getHits().getTotalHits());

        for (SearchHit hit : response.getHits().getHits()) {
            assertTrue(hit.getId() + " should be one of the six known hits", ids.contains(hit.getId()));
        }
    }

}
