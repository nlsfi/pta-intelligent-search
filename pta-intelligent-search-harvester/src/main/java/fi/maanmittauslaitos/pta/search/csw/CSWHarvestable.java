package fi.maanmittauslaitos.pta.search.csw;

public class CSWHarvestable implements Harvestable {


	private String identifier;

	private CSWHarvestable(String identifier) {
		this.identifier = identifier;
	}

	public static CSWHarvestable create(String identifier) {
		return new CSWHarvestable(identifier);
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}
}
