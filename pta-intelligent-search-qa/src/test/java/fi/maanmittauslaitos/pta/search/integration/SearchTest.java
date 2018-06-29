package fi.maanmittauslaitos.pta.search.integration;

import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.*;
import static org.junit.Assert.*;

import org.apache.lucene.util.LuceneTestCase;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.ESIntegTestCase.ClusterScope;
import org.elasticsearch.test.ESIntegTestCase.Scope;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.carrotsearch.randomizedtesting.RandomizedContext;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;


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
	public void createIndex() {
		CreateIndexResponse response = client().admin().indices().prepareCreate("pta").setSource(index, XContentType.JSON).get();
		assertAcked(response);
	}
	
	@Test
	public void insertDocs() {
		System.out.println("********************************************************************************************** TODO");
		
	}
	
}
