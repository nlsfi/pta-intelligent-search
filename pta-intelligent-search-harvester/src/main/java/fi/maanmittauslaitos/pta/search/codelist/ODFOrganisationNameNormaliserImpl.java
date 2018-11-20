package fi.maanmittauslaitos.pta.search.codelist;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class ODFOrganisationNameNormaliserImpl implements OrganisationNormaliser {

	/**
	 * This map is keyed with the language code ("fi", "sv", "en") and for each language
	 * it will contain a map where the key is an alternative name for an orgnisation and
	 * the value is the canonical name.
	 */
	private Map<String, Map<String, String>> canonicalNamesByLanguage;
	
	/**
	 * Loads an ODF workbook (.ods) with multiple worksheets. Sheets represent languages with the
	 * sheet name is the language code. The contents of the sheets contain one row for human-readable
	 * headers and after that the data model is as follows: column A contains canonical names
	 * for organisations and column B contains alternative names for that organisation. If column
	 * A is empty on the next row, then column B should contain an alternative name for the 
	 * same organisation as for the previous row. If column A is set but B is empty, then no alternative
	 * names are available for that organisation. If both columns A and B are empty, a ParseException
	 * will be thrown unless columns A and B are empty for all subsequent rows in that sheet.
	 * 
	 * Example data:
	 * |  A                 |       B         |
	 * ----------------------------------------
	 * | Tampereen kaupunki | Tampere         |
     * |                    | Tampereen Infra |
     * | Pori               | Porin kaupunki  |
     *
	 * 
	 * @param is InputStream for the ODF workbook data.
	 * @throws IOException
	 */
	public void loadWorkbook(InputStream is) throws IOException, ParseException {
		try {
			// TODO: each worksheet of the excel file as a language 
			canonicalNamesByLanguage = new HashMap<>();
		} finally {
			is.close();
		}
	}
	
	@Override
	public String getCanonicalOrganisationName(String orgName, String language) {
		Map<String, String> canonicalNames = canonicalNamesByLanguage.get(language);
		if (canonicalNames == null) {
			return null;
		}
		
		return canonicalNames.get(orgName);
	}

}
