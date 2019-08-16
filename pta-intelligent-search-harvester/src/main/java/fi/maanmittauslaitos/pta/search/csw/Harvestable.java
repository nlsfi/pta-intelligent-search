package fi.maanmittauslaitos.pta.search.csw;

import org.w3c.dom.Node;

import java.io.File;

public class Harvestable {

	private static final String FILE_IDENTIFIER_XPATH = "//gmd:fileIdentifier/*/text()";

	private String identifier;
	private Node node;
	private File file;

	private Harvestable(String identifier) {
		this.identifier = identifier;
	}

	private Harvestable(Node node) {
		this.node = node;
	}

	private Harvestable(File file, String identifier) {
		this.file = file;
		this.identifier = identifier;
	}

	public static Harvestable create(File cswFile, String identifier) {
		return new Harvestable(cswFile, identifier);
	}

	public static Harvestable create(Node node) {
		return new Harvestable(node);
	}

	public static Harvestable create(String identifier) {
		return new Harvestable(identifier);
	}

	public String getIdentifier() {
		return identifier;
	}

	public Node getNode() {
		return node;
	}

	public File getFile() {
		return file;
	}
}
