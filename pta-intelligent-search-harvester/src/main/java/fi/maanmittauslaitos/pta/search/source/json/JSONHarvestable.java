package fi.maanmittauslaitos.pta.search.source.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import fi.maanmittauslaitos.pta.search.source.Harvestable;

public class JSONHarvestable implements Harvestable {

	private final String identifier;
	private final JsonNode content;
	private boolean isService;

	private JSONHarvestable(String identifier, JsonNode content, boolean isService) {
		this.identifier = identifier;
		this.content = content;
		this.isService = isService;
	}

	public static JSONHarvestable create(String identifier, JsonNode content) {
		return new JSONHarvestable(identifier, content, ((ArrayNode) content.get("resources")).size() == 0);
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	public JsonNode getContent() {
		return content;
	}

	public boolean isService() {
		return isService;
	}
}
