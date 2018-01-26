package fi.maanmittauslaitos.pta.search.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import fi.maanmittauslaitos.pta.search.text.RDFTerminologyMatcherProcessor;

public class StemTest {
	public static void main(String[] args) throws IOException 
	{
		ApplicationConfiguration config = new ApplicationConfiguration();
		RDFTerminologyMatcherProcessor proc = config.terminologyMatcher(config.terminologyModel(), config.stemmer(), config.terminologyLabels());
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.print("> ");
			String str = br.readLine();
			if (str.length() == 0) continue;
			
			String stemmed = proc.getStemmer().stem(str);
			System.out.println("'"+str+"' => '"+stemmed+"'");
			System.out.println("YSA: "+proc.process(str));
		}
		
		//proc.getStemmer().stem(str)
	}
}
