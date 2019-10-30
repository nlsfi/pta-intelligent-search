package fi.maanmittauslaitos.pta.search.source.json;

import com.fasterxml.jackson.databind.JsonNode;
import fi.maanmittauslaitos.pta.search.source.Harvestable;

public class JSONHarvestable implements Harvestable {

	private final String identifier;
	private final JsonNode content;

	private JSONHarvestable(String identifier, JsonNode content) {
		this.identifier = identifier;
		this.content = content;
	}

	public static JSONHarvestable create(String identifier, JsonNode content) {
		return new JSONHarvestable(identifier, content);
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	public JsonNode getContent() {
		return content;
	}
}
