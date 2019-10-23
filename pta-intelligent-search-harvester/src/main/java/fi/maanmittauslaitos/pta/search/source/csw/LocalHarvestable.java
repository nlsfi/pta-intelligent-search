package fi.maanmittauslaitos.pta.search.source.csw;

import fi.maanmittauslaitos.pta.search.source.Harvestable;

import java.io.File;

public class LocalHarvestable implements Harvestable {

	private String identifier;
	private File file;

	private LocalHarvestable(File file, String identifier) {
		this.file = file;
		this.identifier = identifier;
	}

	public static Harvestable create(File cswFile, String identifier) {
		return new LocalHarvestable(cswFile, identifier);
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}


	public File getFile() {
		return file;
	}
}
