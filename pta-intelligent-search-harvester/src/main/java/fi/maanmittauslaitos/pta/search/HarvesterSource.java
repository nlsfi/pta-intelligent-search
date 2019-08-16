package fi.maanmittauslaitos.pta.search;

import fi.maanmittauslaitos.pta.search.csw.Harvestable;
import fi.maanmittauslaitos.pta.search.csw.HarvesterInputStream;
import fi.maanmittauslaitos.pta.search.utils.HarvesterTracker;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPathExpression;

public abstract class HarvesterSource implements Iterable<Harvestable> {
	private String onlineResource;
	private int batchSize = 1024;
	protected HarvesterTracker harvesterTracker;
	private DocumentBuilder builder;
	private XPathExpression xPathExpression;

	public String getOnlineResource() {
		return onlineResource;
	}
	
	public void setOnlineResource(String onlineResource) {
		this.onlineResource = onlineResource;
	}

	public void setTracker(HarvesterTracker harvesterTracker) {
		this.harvesterTracker = harvesterTracker;
	}


	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public abstract HarvesterInputStream getInputStream(Harvestable harvestable);
}
