package fi.maanmittauslaitos.pta.search.source.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fi.maanmittauslaitos.pta.search.HarvestingException;
import fi.maanmittauslaitos.pta.search.source.Harvestable;
import fi.maanmittauslaitos.pta.search.source.HarvesterInputStream;
import fi.maanmittauslaitos.pta.search.source.HarvesterSource;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
		return null;
	}


	List<JsonNode> readJsonToNodes(JsonNode json) {
		ArrayList<JsonNode> jsons = new ArrayList<>();

		ObjectNode objectNodeJson = (ObjectNode) json;
		// removes also from original JsonNode object "json"
		JsonNode res = objectNodeJson.remove("resources");
		jsons.add(json);

		if (res != null) {
			if (res.isArray()) {
				res.forEach(jsons::add);
			} else {
				jsons.add(res);
			}
		}
		return jsons;
	}

	private String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	private class CKANIterator implements Iterator<Harvestable> {
		private int numberOfRecordsProcessed = 0;
		private int numberOfRowsProcessed = 0;
		private Integer numberOfRecordsInService;
		private LinkedList<JSONHarvestable> localItems = null;

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

			if (!harvestable.getContent().has("package_id")) {
				// service, not offering
				numberOfRecordsProcessed++;
			}

			return harvestable;
		}

		private void getNextRow() {
			int startPosition = numberOfRowsProcessed;
			int maxRows = getBatchSize();
			logger.debug("Requesting records startPosition = " + startPosition + ",maxRecords = " + maxRows);

			StringBuilder reqUrl = new StringBuilder(getOnlineResource());
			reqUrl.append("?");
			reqUrl.append(String.format("q=%s", getQuery()));
			reqUrl.append(String.format("&rows=%s&start=%s", maxRows, startPosition));

			logger.trace("JSON GetRecords URL: " + reqUrl);

			try (InputStream is = new URL(reqUrl.toString()).openStream()) {
				JsonNode response = objectMapper.readTree(is);
				if (response.has("success") && response.get("success").booleanValue()) {
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
								.map(node -> JSONHarvestable.create(node.get("id").textValue(), node))
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


