package fi.maanmittauslaitos.pta.search.source;

public abstract class HarvesterSource implements Iterable<Harvestable> {
	private String onlineResource;
	private int batchSize = 1024;

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
}
