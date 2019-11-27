package fi.maanmittauslaitos.pta.search.source;

public abstract class HarvesterSource implements Iterable<Harvestable> {

	private String apiPath;

	private String onlineResource;
	private int batchSize = 1024;
	private MetadataType metadataType;

	public String getApiPath() {
		return apiPath;
	}

	public String getOnlineResource() {
		return onlineResource;
	}

	public void setOnlineResource(String onlineResource) {
		this.onlineResource = onlineResource;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public abstract HarvesterInputStream getInputStream(Harvestable harvestable);

	public void setApiPath(String apiPath) {
		this.apiPath = apiPath;
	}

	public MetadataType getMetadataType() {
		return metadataType;
	}

	public void setMetadataType(MetadataType metadataType) {
		this.metadataType = metadataType;
	}

	public enum MetadataType {
		CSW,
		CKAN
	}
}
