package fi.maanmittauslaitos.pta.search.source.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fi.maanmittauslaitos.pta.search.HarvestingException;
import fi.maanmittauslaitos.pta.search.source.Harvestable;
import fi.maanmittauslaitos.pta.search.source.HarvesterInputStream;
import fi.maanmittauslaitos.pta.search.source.HarvesterSource;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CKANHarvesterSource extends HarvesterSource {
	private static final Logger logger = Logger.getLogger(CKANHarvesterSource.class);
	private final ObjectMapper objectMapper;
	private String query;

	public CKANHarvesterSource(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public Iterator<Harvestable> iterator() {
		return new CKANIterator();
	}

	@Override
	public HarvesterInputStream getInputStream(Harvestable harvestable) {
		JSONHarvestable jsonHarvestable = (JSONHarvestable) harvestable;
		return HarvesterInputStream.wrap(IOUtils.toInputStream(jsonHarvestable.getContent().toString()));
	}


	/**
	 * Read JSON to nodes including service and datasets
	 *
	 * @param serviceJson Parsed JSON as JsonNode
	 * @return list of original service and all datasets (resources) including the original service as a field
	 */
	List<JsonNode> readJsonToNodes(JsonNode serviceJson) {
		ArrayList<JsonNode> jsons = new ArrayList<>();
		ObjectNode serviceJsonObjNode = (ObjectNode) serviceJson;
		// removes also from original JsonNode object "serviceJson"
		JsonNode datasets = serviceJsonObjNode.remove("resources");

		// Service metadata will contain empty array of resources
		serviceJsonObjNode.set("resources", objectMapper.createArrayNode());
		jsons.add(serviceJson);

		if (datasets != null) {
			if (datasets.isArray()) {
				datasets.forEach(datasetNode -> {

					// Dataset metadata contains service metadata and only the related dataset metadata
					// in the resources array
					JsonNode modifiedDatasetNode = serviceJson.deepCopy();
					ArrayNode resources = objectMapper.createArrayNode();
					resources.add(datasetNode);
					ObjectNode resObjNode = (ObjectNode) modifiedDatasetNode;
					resObjNode.set("resources", resources);

					jsons.add(modifiedDatasetNode);
				});
			} else {
				jsons.add(datasets);
			}
		}
		return jsons;
	}

	String getIdentifierFromJsonNode(JsonNode node) {
		return Optional.ofNullable((ArrayNode) node.get("resources"))
				.filter(arrNode -> arrNode.size() > 0)
				.map(arr -> arr.get(0))
				.map(firstRes -> firstRes.get("id").textValue())
				.orElse(node.get("id").textValue());
	}

	private String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		try {
			this.query = URLEncoder.encode(query, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			this.query = URLEncoder.encode(query);
		}
	}

	private class CKANIterator implements Iterator<Harvestable> {
		private int numberOfRecordsProcessed = 0;
		private int numberOfRowsProcessed = 0;
		private Integer numberOfRecordsInService;

		private LinkedList<JSONHarvestable> localItems;

		CKANIterator() {
			localItems = new LinkedList<>();
			getNextRow();
		}

		@Override
		public boolean hasNext() {
			return (numberOfRecordsProcessed < numberOfRecordsInService || localItems.size() > 0);
		}


		@Override
		public Harvestable next() {
			if (localItems.size() == 0) {
				getNextRow();
			}

			if (localItems.size() == 0) {
				return null;
			}
			JSONHarvestable harvestable = localItems.removeFirst();

			if (harvestable.isService()) {
				numberOfRecordsProcessed++;
			}

			return harvestable;
		}

		private void getNextRow() {
			int startPosition = numberOfRecordsProcessed;
			int maxRows = getBatchSize();
			logger.debug("Requesting records startPosition = " + startPosition + ",maxRecords = " + maxRows);

			StringBuilder reqUrl = new StringBuilder(getOnlineResource());
			reqUrl.append("?");
			reqUrl.append(String.format("q=%s", getQuery()));
			reqUrl.append(String.format("&rows=%s&start=%s", maxRows, startPosition));

			logger.trace("JSON GetRecords URL: " + reqUrl);

			try (InputStream is = new URL(reqUrl.toString()).openStream()) {
				JsonNode response = objectMapper.readTree(is);
				if (response.has("success") && response.get("success") != null && response.get("success").booleanValue()) {
					JsonNode result = response.get("result");
					int count = result.get("count").intValue();
					if (count > 0) {
						numberOfRecordsInService = count;
					}

					if (numberOfRecordsInService == null) {
						throw new IOException("Unable to determine how many records in CKAN service");
					}

					JsonNode results = result.get("results");
					if (results.isArray()) {
						localItems = StreamSupport.stream(results.spliterator(), false)
								.map(CKANHarvesterSource.this::readJsonToNodes)
								.flatMap(Collection::stream)
								.map(node -> JSONHarvestable.create(getIdentifierFromJsonNode(node), node))
								.collect(Collectors.toCollection(LinkedList::new));
					}
				} else {
					throw new IOException("CKAN response was not successful: " + response.toString());
				}

			} catch (IOException e) {
				throw new HarvestingException(e);
			}
			numberOfRowsProcessed++;

		}

	}


}


