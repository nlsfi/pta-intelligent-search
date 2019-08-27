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
		then(response.getHits()).hasSize(nDocs - 3);
	}

	@Test
	public void nationwideReturnsAllNationalData() throws Exception {
		// In the testcase the serach them was "nationwide" and language set to English
		SearchResponse response = getSearchResponse("testcase_kansallinen.json", nDocs);
		then(response.getHits()).hasSize(nDocs - 3);
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

}
