package fi.maanmittauslaitos.pta.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.documentprocessor.XmlDocument;
import fi.maanmittauslaitos.pta.search.metadata.ResultMetadataFields;
import fi.maanmittauslaitos.pta.search.metadata.model.ResponsibleParty;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class JSONSerialisationTest {
	private ObjectMapper objectMapper;
	
	@Before
	public void setUp() throws Exception {
		objectMapper = new ObjectMapper();
	}

	@Test
	public void testBasics() throws JsonProcessingException {
		Document document = new XmlDocument();
		document.getFields().put(ResultMetadataFields.ID, "1234");
		
		String jsonString = objectMapper.writeValueAsString(document);
		
		assertEquals("{\"fields\":{\"@id\":\"1234\"},\"dom\":null}", jsonString);
	}
	
	@Test
	public void testOrganisation() throws JsonProcessingException {
		ResponsibleParty org = new ResponsibleParty();
		org.setOrganisationName("foo");
		org.setIsoRole("bar");

		Document document = new XmlDocument();
		document.getFields().put(ResultMetadataFields.ORGANISATIONS, Arrays.asList(org));
		
		String jsonString = objectMapper.writeValueAsString(document);
		
		assertEquals("{\"fields\":{\"organisations\":[{\"isoRole\":\"bar\",\"email\":[],\"partyName\":\"foo\",\"localizedPartyName\":{},\"organisationName\":\"foo\",\"localisedOrganisationName\":{}}]},\"dom\":null}", jsonString);
	}

}
