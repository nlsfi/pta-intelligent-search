package fi.maanmittauslaitos.pta.search.integration;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;


public class SearchTest extends SearchTestBase {

	@Test
	public void emptyQueryReturnsAll() throws Exception {
		SearchResponse response = getSearchResponse("testcase_empty.json", nDocs);

		then(response.getHits()).hasSize(nDocs);
	}

	@Test
	public void suomiReturnsAllNationalData() throws Exception {
		SearchResponse response = getSearchResponse("testcase_suomi.json", nDocs);

		List<String> ids = Arrays.asList(
				"495e0dca-c439-4447-948b-171b976a2863",
				"c57bd9a5-feb7-4d31-8ace-526054fabe0e",
				"55a6c54a-2647-4e17-bb90-a0751a29f5c9",
				"49491ac8-2d28-4483-b9a4-c5e689765e57",
				"199db687-fca8-4860-bde5-7e6d1b1af15d",
				"00168bde-de18-4760-b452-967fbb1c9845",
				"44204afc-9951-4c25-ab18-646afd78a89a"
		);

		then(response.getHits())
				.hasSize(nDocs - 3)
				.extracting(SearchHit::getId)
				.containsAll(ids);
	}

	@Test
	public void nationwideReturnsAllNationalData() throws Exception {
		// In the testcase the serach them was "nationwide" and language set to English
		SearchResponse response = getSearchResponse("testcase_kansallinen.json", nDocs);
		then(response.getHits()).hasSize(nDocs - 3);
	}

	@Test
	public void ckanDocumentsHarvestedAsWell() throws IOException, URISyntaxException {
		SearchResponse response = getSearchResponse("testcase_vesien_laatu.json", 1000);
		then(response.getHits())
				.extracting(SearchHit::getId)
				.contains(
						"c57bd9a5-feb7-4d31-8ace-526054fabe0e", //CKAN
						"55a6c54a-2647-4e17-bb90-a0751a29f5c9", //CKAN
						"5e0b6e6c-5122-489d-9d6c-57dd4a1bfcdf", //CKAN
						"67f47ee3-cd16-44d3-b6fd-8eb9faa374a5",  //CSW
						"ade1283b-0e69-4166-a52a-261d04f32cd0"
				);
	}

	@Test
	public void middleFinlandSearch() throws IOException, URISyntaxException {
		SearchResponse response = getSearchResponse("testcase_keski-suomi.json");
		then(response.getHits())
				.extracting(SearchHit::getId)
				.containsExactly(
						"89c6a379-776f-4529-b79d-a456177fb64d", //jkl
						"d527c231-0c7f-4ef0-89df-e5a34888caa4" //jämsä
				);
	}


	@Test
	public void hyphenedKeyWordSearch() throws IOException, URISyntaxException {
		SearchResponse response = getSearchResponse("testcase_liito-orava.json");

		then(response.getHits())
				.extracting(SearchHit::getId)
				.containsExactlyInAnyOrder(
						"2d8394c5-8cd3-434e-993d-1160851ff665",
						"67f47ee3-cd16-44d3-b6fd-8eb9faa374a5");
	}

	@Test
	public void testSwedishMunicipalityName() throws IOException, URISyntaxException {
		SearchResponse response = getSearchResponse("testcase_tammerfors.json");
		then(response.getHits())
				.extracting(SearchHit::getId)
				.containsExactlyInAnyOrder(
						"2d8394c5-8cd3-434e-993d-1160851ff665", //Tampereen Lajihavainnot
						"67f47ee3-cd16-44d3-b6fd-8eb9faa374a5" //Tampereen Liito-oravalle soveltuva elinympäristö
				);
	}

	@Test
	public void testOldMunicipalityName() throws IOException, URISyntaxException {
		SearchResponse response = getSearchResponse("testcase_korpilahti.json");
		then(response.getHits())
				.extracting(SearchHit::getId)
				.containsOnly(
						"89c6a379-776f-4529-b79d-a456177fb64d" //Jyväskylä
				);
	}

	@Test
	public void hyphenedKeyWordParentSearch() throws IOException, URISyntaxException {
		SearchResponse response = getSearchResponse("testcase_orava.json");

		then(response.getHits())
				.extracting(SearchHit::getId)
				.containsExactlyInAnyOrder(
						"2d8394c5-8cd3-434e-993d-1160851ff665", //Tampereen Lajihavainnot
						"67f47ee3-cd16-44d3-b6fd-8eb9faa374a5" //Tampereen Liito-oravalle soveltuva elinympäristö
				);
	}

	@Test
	public void jklBeforeSaloSearch() throws Exception {
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
	public void HSLBeforeHSY() throws Exception {
		SearchResponse response = getSearchResponse("testcase_hsl.json", "a93a10c6-a3dc-46f3-8ab9-260f423a4b9e");

		List<String> hslIds = Arrays.asList(
				"d52b5fae-6139-4182-858c-8602608dd0a4",
				"4d260bcd-eaf7-4bb1-bdbb-ddc924000089",
				"109589be-37cd-49a5-b950-4453a2a16c3b",
				"c176b773-9672-4f39-8ae8-3647d2f54ab4",
				"fd06055c-31e1-476f-b91c-8f8f50548660");

		List<String> hsyIds = Collections.singletonList(
				"a93a10c6-a3dc-46f3-8ab9-260f423a4b9e"
		);

		then(response.getHits())
				.extracting(SearchHit::getId)
				.containsAll(hslIds)
				.containsSubsequence(hslIds.get(0), hsyIds.get(0))
				.containsSubsequence(hslIds.get(1), hsyIds.get(0))
				.containsSubsequence(hslIds.get(1), hsyIds.get(0))
				.containsSubsequence(hslIds.get(2), hsyIds.get(0))
				.containsSubsequence(hslIds.get(3), hsyIds.get(0))
				.containsSubsequence(hslIds.get(4), hsyIds.get(0))
		;
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

	@Test
	public void suomenTietPrioritiesDigiroad() throws Exception {
		SearchResponse response = getSearchResponse("testcase_suomen_tiet.json", 200);
		List<String> ids = Arrays.asList(
				"34155a94-b58b-4ad0-87e6-f96d2db0f3ba", // Digiroad
				"3827015f-546c-4aee-b5c4-2de0509e47ed",
				"52bf65f7-db98-44ac-8da3-0b06fdf71d65"// salo
		);

		then(response.getHits())
				.extracting(SearchHit::getId)
				.containsAll(ids)
				.containsSubsequence(ids.get(0), ids.get(1), ids.get(2));
	}

	@Test
	public void suomenLiikenneverkotPrioritiesDigiroad() throws Exception {
		SearchResponse response = getSearchResponse("testcase_suomen_liikenneverkot.json", 200);
		List<String> ids = Arrays.asList(
				"34155a94-b58b-4ad0-87e6-f96d2db0f3ba", // Digiroad
				"3827015f-546c-4aee-b5c4-2de0509e47ed",
				"52bf65f7-db98-44ac-8da3-0b06fdf71d65"// salo
		);

		then(response.getHits())
				.extracting(SearchHit::getId)
				.containsAll(ids)
				.containsSubsequence(ids.get(0), ids.get(1), ids.get(2));
	}

	@Test
	public void testExactStopWords() throws Exception {
		// Ranta in fuzzy search matches with rauta as well
		SearchResponse response = getSearchResponse("testcase_ranta.json");

		List<String> rantaIds = Arrays.asList(
				"88242599-24ab-4ec1-8cd4-53fc6b9b3212"
		);

		List<String> rautaIds = Arrays.asList(
				"8dd86d67-5afc-4b51-86c1-15a4889f208b"
		);

		then(response.getHits())
				.extracting(SearchHit::getId)
				.containsExactlyInAnyOrderElementsOf(rantaIds)
				.doesNotContainAnyElementsOf(rautaIds);
	}

	@Test
	public void testExactStopWords2() throws Exception {
		// Rauta in fuzzy search matches with ranta as well
		SearchResponse response = getSearchResponse("testcase_rauta.json");

		List<String> rantaIds = Arrays.asList(
				"88242599-24ab-4ec1-8cd4-53fc6b9b3212"
		);

		List<String> rautaIds = Arrays.asList(
				"8dd86d67-5afc-4b51-86c1-15a4889f208b"
		);

		then(response.getHits())
				.extracting(SearchHit::getId)
				.containsExactlyInAnyOrderElementsOf(rautaIds)
				.doesNotContainAnyElementsOf(rantaIds);
	}

	@Test
	public void sortedByTitleAsc() throws IOException, URISyntaxException {
		FieldSortBuilder sortBuilder = SortBuilders.fieldSort("titleFiSort");
		sortBuilder.order(SortOrder.ASC);
		sortBuilders = Collections.singletonList(sortBuilder);

		SearchResponse response = getSearchResponse("testcase_oulu.json");

		then(response.getHits())
				.extracting(SearchHit::getId)
				.containsSubsequence(
						"2c7ca8c6-a47d-4209-8696-6545f2fae8b7", //Oulun kaupungin ajantasa-asemakaava
						"58440189-8048-45aa-81d2-4604a471d179" //Oulun kaupungin WMS-palvelu
				);
	}

	@Test
	public void sortedByTitleDesc() throws IOException, URISyntaxException {
		FieldSortBuilder sortBuilder = SortBuilders.fieldSort("titleFiSort");
		sortBuilder.order(SortOrder.DESC);
		sortBuilders = Collections.singletonList(sortBuilder);

		SearchResponse response = getSearchResponse("testcase_oulu.json");

		then(response.getHits())
				.extracting(SearchHit::getId)
				.containsSubsequence(
						"58440189-8048-45aa-81d2-4604a471d179",//Oulun kaupungin WMS-palvelu
						"2c7ca8c6-a47d-4209-8696-6545f2fae8b7" //Oulun kaupungin ajantasa-asemakaava
				);
	}

	@Test
	public void rakennuksetSearch() throws IOException, URISyntaxException {
		SearchResponse response = getSearchResponse("testcase_rakennukset.json", 200);
		then(response.getHits())
				.extracting(SearchHit::getId)
				.hasSize(13)
				.contains(
						"61aab717-9e54-4973-b195-4b7d9b0754f9",
						"2d8394c5-8cd3-434e-993d-1160851ff665" //Tampereen Lajihavainnot
				);
	}

	@Test
	public void rakennuksetFacetedSearch() throws IOException, URISyntaxException {
		SearchResponse response = getSearchResponse("testcase_rakennukset_biota.json");
		then(response.getHits())
				.extracting(SearchHit::getId)
				.containsExactly(
						"2d8394c5-8cd3-434e-993d-1160851ff665" //Tampereen Lajihavainnot
				);
	}

}
