package fi.maanmittauslaitos.pta.search;

import fi.maanmittauslaitos.pta.search.csw.Harvestable;
import fi.maanmittauslaitos.pta.search.csw.HarvesterInputStream;
import fi.maanmittauslaitos.pta.search.wfs.WFS200HarvesterSource;
import org.apache.commons.io.IOUtils;

public class WFSSourceTest {
	public static void main(String[] args) throws Exception {
		WFS200HarvesterSource source = new WFS200HarvesterSource();
		source.setBatchSize(10);
		//source.setOnlineResource("http://geo.stat.fi:8080/geoserver/wfs");
		//source.setFeatureType("vaestoalue:kunta_vaki2012");

		source.setOnlineResource("http://lipas.cc.jyu.fi:80/geoserver/wfs");
		source.setFeatureType("lipas:lipas_1550_luistelureitti");


		for (Harvestable harvestable : source) {
			HarvesterInputStream is = source.getInputStream(harvestable);
			try {
				IOUtils.copy(is, System.out);
				//if (true) return;
			} finally {
				is.close();
			}
		}
	}
}
