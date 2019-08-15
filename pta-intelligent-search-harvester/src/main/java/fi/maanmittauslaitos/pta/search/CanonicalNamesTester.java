package fi.maanmittauslaitos.pta.search;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;

import fi.maanmittauslaitos.pta.search.codelist.ODFOrganisationNameNormaliserImpl;

public class CanonicalNamesTester {
	public static void main(String[] args) throws IOException, ParseException {
		if (args.length < 3) {
			System.out.println("usage: CanonicalNamesTester canonical-names-file.ods language name [name name ...]");
			System.out.println("\tThis tester reads the canonical names ODS file and then translates each the given 'name' parameter to the canonical version");
			System.out.println("\tFor example: CanonicalNamesTester canonical_names.ods fi HELCOM GTK HSY");
			return;
		}
		
		String filename = args[0];
		System.out.println(String.format("Loading file '%s'", filename));
		
		ODFOrganisationNameNormaliserImpl tmp = new ODFOrganisationNameNormaliserImpl();
		try (FileInputStream fis = new FileInputStream(filename)) {
			tmp.loadWorkbook(fis);
		}
		
		String language = args[1];
		
		System.out.println(String.format("Getting canonical names in language %s", language));
		
		for (int i = 2; i < args.length; i++) {
			String name = args[i];
			String canonical = tmp.getCanonicalOrganisationName(name, language);
			System.out.println(String.format("'%s' => '%s'", name, canonical));
		}
	}
}
