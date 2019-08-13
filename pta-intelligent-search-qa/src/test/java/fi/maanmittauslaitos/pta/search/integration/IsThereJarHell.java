package fi.maanmittauslaitos.pta.search.integration;

import org.elasticsearch.bootstrap.JarHell;
import org.junit.Test;

public class IsThereJarHell {

	@Test
	public void test() throws Exception {
		JarHell.checkJarHell();
	}

}
