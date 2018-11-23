package fi.maanmittauslaitos.pta.search.codelist;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;

public class ODFOrganisationNameNormaliserImpl implements OrganisationNormaliser {

	private static Logger logger = Logger.getLogger(ODFOrganisationNameNormaliserImpl.class);
	
	static {
		// odftoolkit produces a lot of unnecessary logging output that cannot be
		// suppressed via log4j configuration
		// See: https://gist.github.com/simon04/510117db931e159891f9
		try {
			java.util.logging.LogManager.getLogManager().readConfiguration(
				new java.io.ByteArrayInputStream("org.odftoolkit.level=WARNING".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
		} catch(IOException ie) {
			logger.warn("Could not suppress logging for odftoolkit", ie);
		}
		
	}
	
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
			Map<String, Map<String, String>> ret = new HashMap<>();

			OdfSpreadsheetDocument document = OdfSpreadsheetDocument.loadDocument(is);
			
			for (OdfTable table : document.getTableList()) {
				String tableName = table.getTableName();
				tableName = tableName.trim();
				HashMap<String, String> nameHashMap = new HashMap<>();
				String oldCellA = "";
				
				for (Integer rowIndex = 1; rowIndex < table.getRowList().size(); rowIndex++) {
					
					String cellA = table.getCellByPosition(0, rowIndex).getStringValue();
					cellA = cellA.trim();
					Optional.ofNullable(cellA).orElse("");
					if (!cellA.equals("")) {
						oldCellA = cellA;
					}
					
					String cellB = table.getCellByPosition(1, rowIndex).getStringValue();
					cellB = cellB.trim();
					Optional.ofNullable(cellB).orElse("");
					if (cellB.equals("")) {
						cellB = oldCellA;
					}
					
					nameHashMap.put(cellB, oldCellA);
				}
				ret.put(tableName, nameHashMap);
			}
			
			canonicalNamesByLanguage = ret;
		} catch(Exception e) {
			if (e instanceof IOException) {
				throw (IOException)e;
			}
			throw new IOException("Could not read canonical organisation names", e);
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
