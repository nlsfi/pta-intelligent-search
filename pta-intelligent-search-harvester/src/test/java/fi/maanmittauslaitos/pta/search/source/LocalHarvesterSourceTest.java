package fi.maanmittauslaitos.pta.search.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import fi.maanmittauslaitos.pta.search.source.csw.LocalCSWHarvesterSource;
import fi.maanmittauslaitos.pta.search.source.json.LocalCKANHarvesterSource;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalHarvesterSourceTest {
	private static final String LOCAL_JSON_SOURCE_DIR = "testckans";
	private static final String LOCAL_CSW_SOURCE_DIR = "testcsws";
	private static final ObjectMapper objectMapper = new ObjectMapper();


	@Test
	public void testLocalCKANSource() throws IOException {
		URL localDir = getClass().getClassLoader().getResource(LOCAL_JSON_SOURCE_DIR);
		assertThat(localDir).isNotNull();


		LocalCKANHarvesterSource source = new LocalCKANHarvesterSource(objectMapper);
		source.setResourceRootURL(localDir);
		Iterator<Harvestable> iterator = source.iterator();
		assertThat(iterator.hasNext()).isTrue();

		ArrayList<String> ids = new ArrayList<>();
		while (iterator.hasNext()) {
			Harvestable next = iterator.next();
			HarvesterInputStream inputStream = source.getInputStream(next);
			JsonNode jsonNode = objectMapper.readTree(inputStream);
			ArrayNode datasetArray = (ArrayNode) jsonNode.get("resources");
			assertThat(datasetArray.size()).isLessThanOrEqualTo(1);

			String id = Optional.of(datasetArray)
					.filter(arrNode -> arrNode.size() > 0)
					.map(arrNode -> arrNode.get(0))
					.map(node -> node.get("id").textValue())
					.orElse(jsonNode.get("id").textValue());
			ids.add(id);
			assertThat(id).isEqualTo(next.getIdentifier());
		}
		assertThat(ids).containsExactlyInAnyOrder(
				"kissa-service-id", "kissa-dataset-1", "kissa-dataset-2",
				"koira-service-id", "koira-dataset-1", "koira-dataset-2"
		);
	}

	@Test
	public void testCswSource() {
		URL localDir = getClass().getClassLoader().getResource(LOCAL_CSW_SOURCE_DIR);
		assertThat(localDir).isNotNull();


		LocalCSWHarvesterSource source = new LocalCSWHarvesterSource();
		source.setResourceRootURL(localDir);
		Iterator<Harvestable> iterator = source.iterator();
		assertThat(iterator.hasNext()).isTrue();
		Harvestable harvestable = iterator.next();

		assertThat(harvestable.getIdentifier()).isEqualTo("kissa");
	}
}