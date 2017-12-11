package fi.maanmittauslaitos.pta.search;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import fi.maanmittauslaitos.pta.search.csw.CSWHarvesterSource;
import fi.maanmittauslaitos.pta.search.csw.HarvesterSource;

public class CSWSourceTest {
	public static void main(String[] args) throws Exception {
		HarvesterSource source = new CSWHarvesterSource();
		source.setBatchSize(10);
		source.setOnlineResource("http://paikkatietohakemisto.fi/geonetwork/srv/en/csw");
		
		for (InputStream is : source) {
			try {
				IOUtils.copy(is, System.out);
				if (true) return;
			} finally {
				is.close();
			}
		}
	}
}
