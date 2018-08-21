package fi.maanmittauslaitos.pta.search.integration;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.*;
//import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.ESIntegTestCase.ClusterScope;
import org.elasticsearch.test.ESIntegTestCase.Scope;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;


import fi.maanmittauslaitos.pta.search.elasticsearch.PTAElasticSearchMetadataConstants;

@Ignore
@ClusterScope(scope = Scope.SUITE)
@ThreadLeakScope(ThreadLeakScope.Scope.NONE)
public class SearchTest extends ESIntegTestCase  {
	
	String index = "{\n" + 
			"  \"mappings\": {\n" + 
			"    \"metadata\": {\n" + 
			"      \"properties\": {\n" + 
			"        \"abstract_uri\":              { \"type\": \"keyword\" },\n" + 
			"        \"abstract_uri_parents\":      { \"type\": \"keyword\" },\n" + 
			"        \"keywords_uri\":              { \"type\": \"keyword\" },\n" + 
			"        \"annotated_keywords_uri\":    { \"type\": \"keyword\" },\n" + 
			"        \"abstract_maui_uri\":         { \"type\": \"keyword\" },\n" + 
			"        \"abstract_maui_uri_parents\": { \"type\": \"keyword\" },\n" + 
			"        \"keywords\":                  { \"type\": \"keyword\" },\n" + 
			"        \"keywordsInspire\":           { \"type\": \"keyword\" },\n" + 
			"        \"topicCategories\":           { \"type\": \"keyword\" },\n" + 
			"        \"distributionFormats\":       { \"type\": \"keyword\" },\n" + 
			"        \"titleFiSort\":               { \"type\": \"keyword\" },\n" + 
			"        \"titleSvSort\":               { \"type\": \"keyword\" },\n" + 
			"        \"titleEnSort\":               { \"type\": \"keyword\" },\n" + 
			"        \"organisations\": {\n" + 
			"          \"type\": \"object\",\n" + 
			"          \"properties\": {\n" + 
			"            \"organisationName\":      { \"type\": \"keyword\" }\n" + 
			"          }\n" + 
			"        }\n" + 
			"      }\n" + 
			"    }\n" + 
			"  }\n" + 
			"}";
	
	@Before
	public void createIndexAndPopulate() throws IOException
	{
		CreateIndexResponse response = client().admin().indices().prepareCreate("pta").setSource(index, XContentType.JSON).get();
		assertAcked(response);

		ZipInputStream zipFile = new ZipInputStream(SearchTest.class.getResourceAsStream("/test-documents-1.zip"));


		ZipEntry entry;
		int nDocs = 0;
	    while((entry = zipFile.getNextEntry()) != null){
	    
	        byte [] buf = new byte[(int)entry.getSize()];
	        IOUtils.readFully(zipFile, buf);
	        zipFile.closeEntry();
	        
	        Map<String, Object> mapValue = XContentHelper.convertToMap(JsonXContent.jsonXContent, new String(buf), false);
	        
	        @SuppressWarnings("rawtypes")
			String id = ((Collection)mapValue.get("@id")).iterator().next().toString();
	        
	        IndexResponse insertResponse = client().prepareIndex("pta", "metadata", id)
	                .setSource(buf, XContentType.JSON)
	                .get();
	        
	        assertEquals(Result.CREATED, insertResponse.getResult());
	        nDocs++;
	    }
		System.out.println("Inserted "+nDocs+", waiting until all are searchable");
		assertEquals(100, nDocs);
		
		refresh();
	}
	
	@Test
	public void searchUsingMauiParent() throws Exception {
		
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		sourceBuilder.fetchSource("*", null);
		
		BoolQueryBuilder query = QueryBuilders.boolQuery();
		
		QueryBuilder tmp = QueryBuilders.termQuery(PTAElasticSearchMetadataConstants.FIELD_ABSTRACT_MAUI_URI_PARENTS, "http://www.yso.fi/onto/yso/p2536");
		tmp.boost(1.0f);
		query.should().add(tmp);
		
		sourceBuilder.query(query);
		
		SearchRequest request = new SearchRequest(PTAElasticSearchMetadataConstants.INDEX);
		request.types(PTAElasticSearchMetadataConstants.TYPE);
		request.source(sourceBuilder);
		
		System.out.println("query:");
		System.out.println(query);
		
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
			assertTrue(hit.getId()+" should be one of the six known hits", ids.contains(hit.getId()));
		}
	}
	
}
