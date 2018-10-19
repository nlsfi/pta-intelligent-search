package fi.maanmittauslaitos.pta.search.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.SKOS;

import fi.maanmittauslaitos.pta.search.text.RDFTerminologyMatcherProcessor;
import fi.maanmittauslaitos.pta.search.text.stemmer.Stemmer;

/**
 * Read IRI's from the command prompt and display their pref labels
 * 
 * @author v2
 *
 */
public class PrintTerms {
	public static void main(String[] args) throws Exception {
		
		ApplicationConfiguration config = new ApplicationConfiguration();
		
		Model model = config.terminologyModel();
		Stemmer stemmer = config.stemmer_FI();
		RDFTerminologyMatcherProcessor terminologyMatcher = config.terminologyMatcher_FI(model, stemmer, Arrays.asList(SKOS.PREF_LABEL));
		
		Map<String, String> iriToString = terminologyMatcher.getReverseDict();

		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		String line;
		while ( (line = br.readLine()) != null) {
			
			Pattern p = Pattern.compile("https?://[^\"'#? ,]+");
			
			Matcher m = p.matcher(line);
			while (m.find()) {
				String iri = m.group();
				
				System.out.println(iri + " => "+iriToString.get(iri));
			}
		}
	}
}

