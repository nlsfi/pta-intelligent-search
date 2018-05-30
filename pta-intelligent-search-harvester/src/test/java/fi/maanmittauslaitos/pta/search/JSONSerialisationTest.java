package fi.maanmittauslaitos.pta.search;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.metadata.ISOMetadataFields;
import fi.maanmittauslaitos.pta.search.metadata.model.ResponsibleParty;

public class JSONSerialisationTest {
	private ObjectMapper objectMapper;
	
	@Before
	public void setUp() throws Exception {
		objectMapper = new ObjectMapper();
	}

	@Test
	public void testBasics() throws JsonProcessingException {
		Document document = new Document();
		document.getFields().put(ISOMetadataFields.ID, "1234");
		
		String jsonString = objectMapper.writeValueAsString(document);
		
		assertEquals("{\"fields\":{\"@id\":\"1234\"},\"dom\":null}", jsonString);
	}
	
	@Test
	public void testOrganisation() throws JsonProcessingException {
		ResponsibleParty org = new ResponsibleParty();
		org.setOrganisationName("foo");
		org.setIsoRole("bar");
		
		Document document = new Document();
		document.getFields().put(ISOMetadataFields.ORGANISATIONS, Arrays.asList(org));
		
		String jsonString = objectMapper.writeValueAsString(document);
		
		assertEquals("{\"fields\":{\"organisations\":[{\"organisationName\":\"foo\",\"isoRole\":\"bar\"}]},\"dom\":null}", jsonString);
	}

}
