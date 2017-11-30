package fi.maanmittauslaitos.pta.search;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

public class RDFTest {
	public static void main(String[] args) throws Exception {
		
		ValueFactory vf = SimpleValueFactory.getInstance();

		final String labelNamespace = "http://www.w3.org/2004/02/skos/core#";
		final String labelField = "prefLabel";
		
		final String altlabelNamespace = "http://www.w3.org/2004/02/skos/core#";
		final String altlabelField = "altLabel";

		List<IRI> labelResources = new ArrayList<>();
		labelResources.add(vf.createIRI(labelNamespace, labelField));
		labelResources.add(vf.createIRI(altlabelNamespace, altlabelField));
	
		
		//FileReader reader = new FileReader("src/test/resources/ysa-skos.ttl");
		FileReader reader = new FileReader("src/test/resources/kissa.ttl");
		
		
		Model model = Rio.parse(reader, "", RDFFormat.TURTLE);
		
		// Poista filtteri jossain välissä
		for (Resource r : model.subjects()) {
			//System.out.println(r.stringValue());
			String resourceName = r.stringValue();
			System.out.println(resourceName);
			
			for (IRI resource : labelResources) {
				for (Statement statement : model.filter(r, resource, null)) {
					System.out.println("\t + "+statement.getObject().stringValue());
				}
			}
			
			//if (true) return;
		}
	}
}
