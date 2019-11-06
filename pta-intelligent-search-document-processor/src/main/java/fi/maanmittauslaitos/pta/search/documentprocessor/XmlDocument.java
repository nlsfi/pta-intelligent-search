package fi.maanmittauslaitos.pta.search.documentprocessor;

public class XmlDocument extends Document {
	private org.w3c.dom.Document dom;

	public org.w3c.dom.Document getDom() {
		return dom;
	}

	public void setDom(org.w3c.dom.Document dom) {
		this.dom = dom;
	}

}
