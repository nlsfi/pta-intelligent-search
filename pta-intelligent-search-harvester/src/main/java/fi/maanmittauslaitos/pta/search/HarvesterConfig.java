package fi.maanmittauslaitos.pta.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import com.entopix.maui.stemmers.FinnishStemmer;
import com.entopix.maui.stopwords.StopwordsFinnish;

import fi.maanmittauslaitos.pta.documentprocessor.DocumentProcessingConfiguration;
import fi.maanmittauslaitos.pta.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.documentprocessor.DocumentProcessorFactory;
import fi.maanmittauslaitos.pta.documentprocessor.MockWFSFeatureTypeFieldExtractorConfiguration;
import fi.maanmittauslaitos.pta.documentprocessor.XPathFieldExtractorConfiguration;
import fi.maanmittauslaitos.pta.documentprocessor.XPathFieldExtractorConfiguration.FieldExtractorType;
import fi.maanmittauslaitos.pta.search.csw.CSWHarvesterSource;
import fi.maanmittauslaitos.pta.search.index.DocumentSink;
import fi.maanmittauslaitos.pta.search.index.ElasticsearchDocumentSink;
import fi.maanmittauslaitos.pta.search.text.ExistsInSetProcessor;
import fi.maanmittauslaitos.pta.search.text.MauiTextProcessor;
import fi.maanmittauslaitos.pta.search.text.RDFTerminologyMatcherProcessor;
import fi.maanmittauslaitos.pta.search.text.RegexProcessor;
import fi.maanmittauslaitos.pta.search.text.StopWordsProcessor;
import fi.maanmittauslaitos.pta.search.text.TextProcessingChain;
import fi.maanmittauslaitos.pta.search.text.TextSplitterProcessor;
import fi.maanmittauslaitos.pta.search.text.WordCombinationProcessor;
import fi.maanmittauslaitos.pta.search.text.stemmer.StemmerFactor;

public class HarvesterConfig {
	public HarvesterSource getCSWSource() {
		HarvesterSource source = new CSWHarvesterSource();
		source.setBatchSize(10);
		source.setOnlineResource("http://paikkatietohakemisto.fi/geonetwork/srv/en/csw");
		//source.setOnlineResource("http://demo.paikkatietohakemisto.fi/geonetwork/srv/en/csw");
		
		return source;
	}
	
	
	public DocumentProcessor getCSWRecordProcessor() throws ParserConfigurationException, IOException {
		DocumentProcessingConfiguration configuration = new DocumentProcessingConfiguration();
		configuration.getNamespaces().put("gmd", "http://www.isotc211.org/2005/gmd");
		configuration.getNamespaces().put("gco", "http://www.isotc211.org/2005/gco");
		configuration.getNamespaces().put("srv", "http://www.isotc211.org/2005/srv");
		configuration.getNamespaces().put("gmx", "http://www.isotc211.org/2005/gmx");
		
		configuration.getNamespaces().put("xlink", "http://www.w3.org/1999/xlink");
		
		
		Model model = getTerminologyModel();
		RDFTerminologyMatcherProcessor terminologyProcessor = createTerminologyMatcher(model);
		WordCombinationProcessor wordCombinationProcessor = createWordCombinationProcessor(model);
		
		{
			TextProcessingChain abstractChain = new TextProcessingChain();
			abstractChain.getChain().add(new TextSplitterProcessor());
			abstractChain.getChain().add(wordCombinationProcessor);
			
			StopWordsProcessor stopWordsProcessor = new StopWordsProcessor();
			try (InputStreamReader isr = new InputStreamReader(HarvesterConfig.class.getResourceAsStream("/stopwords-fi.txt"))) {
				List<String> stopWords = new ArrayList<>();
				BufferedReader br = new BufferedReader(isr);
				String line;
				while ((line = br.readLine()) != null) {
					String tmp = line.toLowerCase().trim();
					if (tmp.length() > 0) {
						stopWords.add(tmp);
					}
				}
				stopWordsProcessor.setStopwords(stopWords);
			}
			abstractChain.getChain().add(stopWordsProcessor);
			
			abstractChain.getChain().add(terminologyProcessor);
			
			configuration.getTextProcessingChains().put("abstractProcessor", abstractChain);
		}
		
		{
			TextProcessingChain mauiChain = new TextProcessingChain();
			
			MauiTextProcessor mauiTextProcessor = new MauiTextProcessor();
			mauiTextProcessor.setMauiStemmer(new FinnishStemmer());
			mauiTextProcessor.setMauiStopWords(new StopwordsFinnish());
			
			mauiTextProcessor.setModelResource("/paikkatietohakemisto-pto.model");
			mauiTextProcessor.setVocabularyName("pto-skos.rdf.gz");
			mauiTextProcessor.setVocabularyFormat("skos");
			mauiTextProcessor.setLanguage("fi");
			
			mauiTextProcessor.init();
			mauiChain.getChain().add(mauiTextProcessor);
			
			configuration.getTextProcessingChains().put("mauiProcessor", mauiChain);
		}
		

		TextProcessingChain keywordChain = new TextProcessingChain();
		RegexProcessor whitespaceRemoval = new RegexProcessor();
		whitespaceRemoval.setPattern(Pattern.compile("^\\s*$"));
		whitespaceRemoval.setIncludeMatches(false);
		
		keywordChain.getChain().add(new TextSplitterProcessor());
		keywordChain.getChain().add(wordCombinationProcessor);
		keywordChain.getChain().add(whitespaceRemoval);
		keywordChain.getChain().add(terminologyProcessor);
		
		
		configuration.getTextProcessingChains().put("keywordProcessor", keywordChain);
		
		{
			XPathFieldExtractorConfiguration idExtractor = new XPathFieldExtractorConfiguration();
			idExtractor.setField("@id");
			idExtractor.setType(FieldExtractorType.FIRST_MATCHING_VALUE);
			idExtractor.setXpath("//gmd:fileIdentifier/*/text()");
			
			configuration.getFieldExtractors().add(idExtractor);
		}
		
		{
			XPathFieldExtractorConfiguration titleExtractor = new XPathFieldExtractorConfiguration();
			titleExtractor.setField("title");
			titleExtractor.setType(FieldExtractorType.FIRST_MATCHING_VALUE);
			titleExtractor.setXpath("//gmd:identificationInfo/*/gmd:citation/*/gmd:title/*/text()");
			
			configuration.getFieldExtractors().add(titleExtractor);
		}
		
		{
			XPathFieldExtractorConfiguration keywordExtractor = new XPathFieldExtractorConfiguration();
			keywordExtractor.setField("avainsanat_uri");
			keywordExtractor.setType(FieldExtractorType.ALL_MATCHING_VALUES);
			keywordExtractor.setXpath("//gmd:MD_Keywords/gmd:keyword/*/text()");
			
			keywordExtractor.setTextProcessorName("keywordProcessor");
			
			configuration.getFieldExtractors().add(keywordExtractor);
		}
		
		{
			XPathFieldExtractorConfiguration annotatedKeywordExtractor = new XPathFieldExtractorConfiguration();
			annotatedKeywordExtractor.setField("annotoidut_avainsanat_uri");
			annotatedKeywordExtractor.setType(FieldExtractorType.ALL_MATCHING_VALUES);
			annotatedKeywordExtractor.setXpath("//gmd:descriptiveKeywords/*/gmd:keyword/gmx:Anchor/@xlink:href");
			
			annotatedKeywordExtractor.setTextProcessorName("isInOntologyFilterProcessor");
			
			configuration.getFieldExtractors().add(annotatedKeywordExtractor);
		}
		
		TextProcessingChain isInOntologyFilterProcessor = new TextProcessingChain();
		
		ExistsInSetProcessor allowInOntology = new ExistsInSetProcessor();
		allowInOntology.setAcceptedStrings(terminologyProcessor.getAllKnownTerms());
		isInOntologyFilterProcessor.getChain().add(allowInOntology);
		
		configuration.getTextProcessingChains().put("isInOntologyFilterProcessor", isInOntologyFilterProcessor);
		
		
		{
			XPathFieldExtractorConfiguration abstractExtractor = new XPathFieldExtractorConfiguration();
			abstractExtractor.setField("abstract_uri");
			abstractExtractor.setType(FieldExtractorType.ALL_MATCHING_VALUES);
			abstractExtractor.setXpath("//gmd:abstract/*/text()");
			
			abstractExtractor.setTextProcessorName("abstractProcessor");
			
			configuration.getFieldExtractors().add(abstractExtractor);
		}
		
		{
			XPathFieldExtractorConfiguration abstractMauiExtractor = new XPathFieldExtractorConfiguration();
			abstractMauiExtractor.setField("abstract_maui_uri");
			abstractMauiExtractor.setType(FieldExtractorType.ALL_MATCHING_VALUES);
			abstractMauiExtractor.setXpath("//gmd:abstract/*/text()");
			
			abstractMauiExtractor.setTextProcessorName("mauiProcessor");
			
			configuration.getFieldExtractors().add(abstractMauiExtractor);
		}
		
		{
			XPathFieldExtractorConfiguration abstractAsTextExtractor = new XPathFieldExtractorConfiguration();
			abstractAsTextExtractor.setField("abstract");
			abstractAsTextExtractor.setType(FieldExtractorType.ALL_MATCHING_VALUES);
			abstractAsTextExtractor.setXpath("//gmd:abstract/*/text()");
			
			configuration.getFieldExtractors().add(abstractAsTextExtractor);
		}
		
		{
			XPathFieldExtractorConfiguration onlineResourceExtractor = new XPathFieldExtractorConfiguration();
			onlineResourceExtractor.setField("onlineResource");
			onlineResourceExtractor.setType(FieldExtractorType.ALL_MATCHING_VALUES);
			// Select linkage URL in onLine transferoptions where protocol contains "wfs"
			//onlineResourceExtractor.setXpath("//gmd:distributionInfo/*/gmd:transferOptions/*/gmd:onLine/*[contains(translate(gmd:protocol/*/text(),\"WFS\",\"wfs\"),\"wfs\")]/gmd:linkage/gmd:URL/text()");
			onlineResourceExtractor.setXpath("//gmd:distributionInfo/*/gmd:transferOptions/*/gmd:onLine/gmd:CI_OnlineResource/*/gmd:URL/text()");
			
			configuration.getFieldExtractors().add(onlineResourceExtractor);
		}
		
		{
			XPathFieldExtractorConfiguration isServiceExtractor = new XPathFieldExtractorConfiguration();
			isServiceExtractor.setField("isService");
			isServiceExtractor.setType(FieldExtractorType.TRUE_IF_MATCHES_OTHERWISE_FALSE);
			isServiceExtractor.setXpath("//gmd:identificationInfo/srv:SV_ServiceIdentification");
			
			configuration.getFieldExtractors().add(isServiceExtractor);
		}
		
		{
			XPathFieldExtractorConfiguration isDatasetExtractor = new XPathFieldExtractorConfiguration();
			isDatasetExtractor.setField("isDataset");
			isDatasetExtractor.setType(FieldExtractorType.TRUE_IF_MATCHES_OTHERWISE_FALSE);
			isDatasetExtractor.setXpath("//gmd:identificationInfo/gmd:MD_DataIdentification");
			
			configuration.getFieldExtractors().add(isDatasetExtractor);
			
		}
		
		{
			MockWFSFeatureTypeFieldExtractorConfiguration wfsFeatureTypeExtractor = new MockWFSFeatureTypeFieldExtractorConfiguration();
			wfsFeatureTypeExtractor.setField("wfs_fields");
			wfsFeatureTypeExtractor.setInjectedFieldsById(loadMockWFSFields());
			
			configuration.getFieldExtractors().add(wfsFeatureTypeExtractor);
		}
		
		return new DocumentProcessorFactory().createProcessor(configuration);
	}
	
	/**
	 * wget -O- --quiet \
	 * 	'https://inspire-wfs.maanmittauslaitos.fi/inspire-wfs/gn?service=wfs&request=DescribeFeatureType&featureType=gn:NamedPlace&version=2.0.0' | \
	 *   xmlstarlet sel \
	 *     -N xsd=http://www.w3.org/2001/XMLSchema -t \
	 *     -v "(//xsd:attribute|//xsd:element)/@name" | \
	 *   sed 's/\(.*\)/"\1",/' | tr '\n' ' ' | sed 's/,$/\n/'
	 * 
	 * @return
	 */
	private Map<String, List<String>> loadMockWFSFields() {
		Map<String, List<String>> ret = new HashMap<>();
		// NLS-FI INSPIRE Download Service (WFS) for Geographical Names
		// https://inspire-wfs.maanmittauslaitos.fi/inspire-wfs/gn?service=wfs&request=DescribeFeatureType&featureType=gn:NamedPlace&version=2.0.0
		ret.put("9f3ecf73-1c76-42f7-aa9e-815c55752803", Arrays.asList("AbstractFeature", "SpatialDataSet", "identifier", "metadata", "type", "href", "role", "arcrole", "title", "show", "actuate", "nilReason", "remoteSchema", "owns", "member", "Identifier", "type", "href", "role", "arcrole", "title", "show", "actuate", "nilReason", "remoteSchema", "owns", "localId", "namespace", "versionId", "nilReason"));
		
		// Road traffic accidents
		// http://geo.stat.fi/geoserver/tieliikenne/wfs?service=WFS&request=DescribeFeatureType&version=1.0.0
		ret.put("de71e0a1-4516-4d50-bd54-e384e5174546", Arrays.asList("geom", "vvonn", "kkonn", "kello", "vakav", "onntyyppi", "lkmhapa", "lkmlaka", "lkmjk", "lkmpp", "lkmmo", "lkmmp", "lkmmuukulk", "x", "y", "tieliikenne_2011", "geom", "vvonn", "kkonn", "kello", "vakav", "onntyyppi", "lkmhapa", "lkmlaka", "lkmjk", "lkmpp", "lkmmo", "lkmmp", "lkmmuukulk", "x", "y", "tieliikenne_2012", "geom", "vvonn", "kkonn", "kello", "vakav", "onntyyppi", "lkmhapa", "lkmlaka", "lkmjk", "lkmpp", "lkmmo", "lkmmp", "lkmmuukulk", "x", "y", "tieliikenne_2013", "geom", "vvonn", "kkonn", "kello", "vakav", "onntyyppi", "lkmhapa", "lkmlaka", "lkmjk", "lkmpp", "lkmmo", "lkmmp", "lkmmuukulk", "x", "y", "tieliikenne_2014", "geom", "vvonn", "kkonn", "kello", "vakav", "onntyyppi", "lkmhapa", "lkmlaka", "lkmjk", "lkmpp", "lkmmo", "lkmmp", "lkmmuukulk", "x", "y", "tieliikenne_2015", "geom", "vvonn", "kkonn", "kello", "vakav", "onntyyppi", "lkmhapa", "lkmlaka", "lkmjk", "lkmpp", "lkmmo", "lkmmp", "lkmmuukulk", "x", "y", "tieliikenne_2016"));
		
		// Oriveden WFS
		// http://192.89.47.210/arcgis/services/orivesi/palvelut/MapServer/WFSServer?request=GetCapabilities&service=WFS
		ret.put("5d2fe968-f0dd-48fe-8d2b-34dc2e2fd77e", Arrays.asList("uimahallit", "OBJECTID", "SHAPE", "PALVELU", "PALVELUTYYPPI", "NIMI", "KUNTA", "KATUOSOITE", "POSTINUMERO", "POSTITOIMIPAIKKA", "TUOTTAJATYYPPI", "TUOTTAJA", "URL", "ESTEETTÖMYYSKUVAUS", "TUNNUS", "kirjastot", "OBJECTID", "SHAPE", "PALVELU", "PALVELUTYYPPI", "NIMI", "KUNTA", "KATUOSOITE", "POSTINUMERO", "POSTITOIMIPAIKKA", "TUOTTAJATYYPPI", "TUOTTAJA", "URL", "ESTEETTÖMYYSKUVAUS", "TUNNUS", "perusopetus", "OBJECTID", "SHAPE", "PALVELU", "PALVELUTYYPPI", "NIMI", "KUNTA", "KATUOSOITE", "POSTINUMERO", "POSTITOIMIPAIKKA", "TUOTTAJATYYPPI", "TUOTTAJA", "URL", "ESTEETTÖMYYSKUVAUS", "LUOKKA_1", "LUOKKA_2", "LUOKKA_3", "LUOKKA_4", "LUOKKA_5", "LUOKKA_6", "LUOKKA_7", "LUOKKA_8", "LUOKKA_9", "LUOKKA_10", "TUNNUS", "päiväkotihoito", "OBJECTID", "SHAPE", "PALVELU", "PALVELUTYYPPI", "NIMI", "KUNTA", "KATUOSOITE", "POSTINUMERO", "POSTITOIMIPAIKKA", "TUOTTAJATYYPPI", "TUOTTAJA", "URL", "ESTEETTÖMYYSKUVAUS", "TUNNUS", "VUOROHOITO", "esiopetus", "OBJECTID", "SHAPE", "PALVELU", "PALVELUTYYPPI", "NIMI", "KUNTA", "KATUOSOITE", "POSTINUMERO", "POSTITOIMIPAIKKA", "TUOTTAJATYYPPI", "TUOTTAJA", "URL", "ESTEETTÖMYYSKUVAUS", "LUOKKA_1", "LUOKKA_2", "LUOKKA_3", "LUOKKA_4", "LUOKKA_5", "LUOKKA_6", "LUOKKA_7", "LUOKKA_8", "LUOKKA_9", "LUOKKA_10", "TUNNUS", "nuorten_tilat", "OBJECTID", "SHAPE", "PALVELU", "PALVELUTYYPPI", "NIMI", "KUNTA", "KATUOSOITE", "POSTINUMERO", "POSTITOIMIPAIKKA", "TUOTTAJATYYPPI", "TUOTTAJA", "URL", "ESTEETTÖMYYSKUVAUS", "TUNNUS"));
		
		// Hyvinkään WFS-palvelu
		// http://kartta.hyvinkaa.fi/wfs_1/ows.ashx?SERVICE=WFS&REQUEST=DescribeFeatureType
		ret.put("c6e0b43a-3bfd-4c8e-80a9-2d21d6111e08", Arrays.asList("Ajantasa_asemakaava_avain", "AdMapKey", "KAA_TU", "KAA_PDF", "URL_PDF", "GAVPrimaryKey", "Geometry", "Kulttuuriympäristö_2013", "AdMapKey", "ALUE", "NIMI", "PDF", "Shape_Leng", "Shape_Area", "GAVPrimaryKey", "Geometry", "ku_sote_aluerajat", "Nro", "Nimi", "GAVPrimaryKey", "Geometry", "osoitteet_access_view", "ways_wid", "ways_town", "ways_wna1", "ways_wna2", "mv_nimisto", "locx_aid", "locx_wid", "locx_nul", "locx_num", "locx_pnu", "locx_maps", "locx_regv", "locx_regs", "locx_schf", "locx_schs", "locx_doct", "locx_ixs", "locx_ixl", "locx_iyl", "mslink", "geometry", "Osoitteet_sql", "ID", "StreetName_fi", "StreetName_sv", "NumberPart", "NumberSortable", "City_fi", "City_sv", "geometry", "pohjavesialueet", "AREA", "PERIMETER", "PVESI_", "PVESI_ID", "PV_TUNNUS", "PV_LK", "MA_TUNNUS", "MA_LK", "SUSU", "YKENRO98", "YKENIMI98", "YKELYH98", "DIGMK", "MUUTOSPVM", "DIGITOINTI", "PVNRO", "HUOM", "KUVAUS", "PAIVITYS", "ID", "GAVPrimaryKey", "Geometry", "maalampokaivojen_rakentamista_rajoittavatalueet_Yh", "ID", "TEKSTI", "Shape_Leng", "Shape_Area", "GAVPrimaryKey", "Geometry", "Rakennuskiellot", "Id", "VOIMASSA", "ALKANUT", "PAATOS", "PERUSTE", "NIMI", "PTPJUPVM", "KPJUPVM", "GEOM", "ohjeet_rakentajille", "Id", "OHJ_TUN", "OHJ_PDF", "URL_PDF", "GAVPrimaryKey", "Geometry", "Suunnittelutarvealue", "Id", "GAVPrimaryKey", "Geometry", "aj_tonttijako", "ID", "Laji", "Lajin_seli", "Teksti", "Maanomistu", "Korotettu_", "GEOM", "vesih_t_alue_jv", "Id", "NIMI", "ALUE", "GAVPrimaryKey", "Geometry", "vesih_t_alue_sv", "Id", "NIMI", "ALUE", "GAVPrimaryKey", "Geometry", "vesih_t_alue_vj", "Id", "NIMI", "ALUE", "GAVPrimaryKey", "Geometry", "Vesiosuuskunnat_Ritasjarven_alue", "Id", "KOHDE", "PDF_1", "PDF_2", "URL_PDF2", "URL_PDF1", "GAVPrimaryKey", "Geometry", "aj_rakennus_VIIVA", "ID", "Laji", "Lajin_seli", "Teksti", "Julkinen_r", "Sokkelin_k", "GEOM", "aj_rakennus_valmiit_VIIVA", "ID", "Laji", "Lajin_seli", "Sokkelin_k", "GEOM", "aj_rakennus_ALUE", "ID", "Laji", "Lajin_seli", "Teksti", "Julkinen_r", "Sokkelin_k", "GEOM", "Kiinteistot_alueina", "ID", "Laji", "Lajin_seli", "Tunnus", "Maanomistus", "Korotettu_", "Koodi_omistus", "GEOM", "aj_kiinteisto_VIIVA", "ID", "Laji", "Lajin_seli", "Tunnus", "Maanomistus", "Korotettu_", "Koodi_omistus", "GEOM", "asunnot16", "KG_K_GEOM_", "KG_KALUEJA", "C_KUNTA", "C_ALUEJAKO", "C_ALUEJAON", "C_MILLOIN", "C_KUKA", "ID", "C_ALUE", "NRO", "NIMI", "OTSIKKO", "SELITE", "TILASTO_PV", "YHTEENSA", "OSUUS_1_2", "OSUUS_3_4", "OSUUS_5", "OSUUS_MUU", "PIENTALO_O", "OSUUS_ASU", "TAULUKKO", "LINKKI", "OSSI", "MUUTOS_PVM", "LUOKKA_VUO", "VUOKRA_A_O", "KARTTA", "URL_KARTTA", "GAVPrimaryKey", "Geometry", "asuntokunnat16", "KG_K_GEOM_", "KG_KALUEJA", "C_KUNTA", "C_ALUEJAKO", "C_ALUEJAON", "C_MILLOIN", "C_KUKA", "ID", "C_ALUE", "NRO", "TK", "NIMI", "OTSIKKO", "SELITE", "TILASTO_PV", "YHT_2015", "OSUUS_1_2", "LUOKKA_1_2", "LUOKKA_1_3", "OSUUS_3_4", "OSUUS_5_", "AS_KUNTIA", "TAULUKKO", "OSSI", "MUUTOS_PVM", "KARTTA", "URL_KARTTA", "GEOM", "keskitulot16", "KG_K_GEOM_", "KG_KALUEJA", "C_KUNTA", "C_ALUEJAKO", "C_ALUE", "C_ALUEJAON", "C_ALUEJA_1", "C_AKKOODI", "C_MILLOIN", "C_KUKA", "AREA", "LEN", "ID", "NRO", "TK", "NIMI", "OTSIKKO", "SELITE", "KESKITULOT", "KESKITUL_1", "LUOKKA_KT_", "TAULUKKO", "LINKKI", "OSSI", "MUUTOS_PVM", "TILASTO_PV", "KARTTA", "URL_KARTTA", "GAVPrimaryKey", "Geometry", "koulutusaste16", "KG_K_GEOM_", "KG_KALUEJA", "C_KUNTA", "C_ALUEJAKO", "C_ALUE", "C_ALUEJAON", "C_ALUEJA_1", "C_AKKOODI", "C_MILLOIN", "C_KUKA", "AREA", "LEN", "ID", "NRO", "NRO1", "Tk", "Kaikki_kou", "Vain_perus", "Keskiaste", "Korkea_ast", "OSUUS_PERU", "OSUUS_KESK", "KORKEA_A_O", "OTSIKKO", "SELITE", "TILASTO_PV", "TAULUKKO", "LINKKI", "OSSI", "MUUTOS_PVM", "LUOKKA", "KARTTA", "URL_KARTTA", "GAVPrimaryKey", "Geometry", "nettomuutto16", "KG_K_GEOM_", "KG_KALUEJA", "C_KUNTA", "C_ALUEJAKO", "C_ALUE", "C_ALUEJAON", "C_ALUEJA_1", "C_AKKOODI", "C_MILLOIN", "C_KUKA", "AREA", "LEN", "ID", "OTSIKKO", "SELITE", "NRO", "VALIKKO", "tk", "NIMI", "TILASTO_PV", "NETTOMUUTT", "YHT_NETTOM", "F0_19", "F20_39", "F40_59", "F60", "NM_11_15", "TAULUKKO", "LINKKI", "OSSI", "MUUTOS_PVM", "GAVPrimaryKey", "Geometry", "perhevaesto16", "KG_K_GEOM_", "KG_KALUEJA", "C_KUNTA", "C_ALUEJAKO", "C_ALUEJAON", "C_MILLOIN", "C_KUKA", "ID", "C_ALUE", "NRO", "NIMI", "OTSIKKO", "SELITE", "TILASTOPV", "LAPSIPERHE", "LP_OS_PER", "LUO_LPOA", "LP_OS_AS", "YKSINHUOLT", "VAH_3LAP", "TAULUKKO", "MUUTOS_PVM", "KARTTA", "URL_KARTTA", "GAVPrimaryKey", "Geometry", "tyopaikat16", "KG_K_GEOM_", "KG_KALUEJA", "C_KUNTA", "C_ALUEJAKO", "C_ALUEJAON", "C_MILLOIN", "C_KUKA", "ID", "C_ALUE", "NRO", "TK", "NIMI", "OTSIKKO", "SELITE", "TILASTO_PV", "YHT_TYOPAI", "LUOK_TP", "OSUUS_TYOP", "OSUUS_AT", "OSUUS_JA", "OSUUS_PA", "TAULUKKO", "LINKKI", "OSSI", "MUUTOS_PVM", "KARTTA", "URL_KARTTA", "GAVPrimaryKey", "Geometry", "tyovoima16", "KG_K_GEOM_", "KG_KALUEJA", "C_KUNTA", "C_ALUEJAKO", "C_ALUEJAON", "C_MILLOIN", "C_KUKA", "ID", "C_ALUE", "NRO", "OTSIKKO", "NIMI", "TILASTO_PV", "VAESTO", "TYOVOIMA", "TYOLLISET", "OSUUS_TYOL", "TyÃ_ttÃ_m", "TYOT_ASTE", "LUOK_T", "ULKOPUOLEL", "ALLE_14", "OPISKELIJA", "ELAKELAISE", "MUUT", "TAULUKKO", "SELITE", "LINKKI", "OSSI", "MUUTOS_PVM", "KARTTA", "URL_KARTTA", "GAVPrimaryKey", "Geometry", "vaesto16", "KG_K_GEOM_", "KG_KALUEJA", "C_KUNTA", "C_ALUEJAKO", "C_ALUEJAON", "C_MILLOIN", "C_KUKA", "ID", "C_ALUE", "NRO", "NIMI", "OTSIKKO", "LUOKKA_VAE", "TILASTOPV", "VAESTO_YHT", "OSUUS_N", "OSUUS_M", "OSUUS_0_14", "OSUUS_15_6", "OSUUS_65_", "KESKI_IKA", "HUOLTOSUHD", "SELITE", "TAULUKKO", "OSSI", "LINKKI", "MUUTOS_PVM", "KARTTA", "URL_KARTTA", "GAVPrimaryKey", "Geometry", "vaestolisays16", "KG_K_GEOM_", "KG_KALUEJA", "C_KUNTA", "C_ALUEJAKO", "C_ALUEJAON", "C_MILLOIN", "C_KUKA", "ID", "C_ALUE", "NRO", "OTSIKKO", "NIMI", "TILASTO_PV", "VAESTOLISA", "LUOKKA_V", "VMU_OS_12", "OS_KPKI_15", "VMU_11_15", "OS_KP_1115", "SELITE", "TAULUKKO", "LINKKI", "MUUTOS_PVM", "KARTTA", "URL_KARTTA", "GAVPrimaryKey", "Geometry"));
		
		return ret;
	}


	private RDFTerminologyMatcherProcessor createTerminologyMatcher(Model model) throws IOException {
		RDFTerminologyMatcherProcessor ret = new RDFTerminologyMatcherProcessor();
		ret.setModel(model);
		ret.setTerminologyLabels(Arrays.asList(SKOS.PREF_LABEL, SKOS.ALT_LABEL));
		ret.setStemmer(StemmerFactor.createStemmer());
		ret.setLanguage("fi");
		return ret;
	}

	private WordCombinationProcessor createWordCombinationProcessor(Model model) throws IOException {
		WordCombinationProcessor ret = new WordCombinationProcessor();
		ret.setModel(model);
		ret.setTerminologyLabels(Arrays.asList(SKOS.PREF_LABEL, SKOS.ALT_LABEL));
		ret.setStemmer(StemmerFactor.createStemmer());
		ret.setLanguage("fi");
		return ret;
	}
	
	public DocumentSink getDocumentSink() {
		ElasticsearchDocumentSink ret = new ElasticsearchDocumentSink();
		ret.setHostname("localhost");
		ret.setPort(9200);
		ret.setProtocol("http");
		ret.setIndex("catalog");
		ret.setType("doc");
		
		ret.setIdField("@id");
		
		return ret;
	}
	

	Model getTerminologyModel() throws IOException {
		return loadModels(getTerminologyModelResourceName());
	}

	private String getTerminologyModelResourceName() {
		return "/pto-skos.ttl.gz";
	}
	
	private static Model loadModels(String...files) throws IOException {
		Model ret = null;
		
		for (String file : files) {
			try (Reader reader = new InputStreamReader(new GZIPInputStream(HarvesterConfig.class.getResourceAsStream(file)))) {
				Model model = Rio.parse(reader, "", RDFFormat.TURTLE);
				
				if (ret == null) {
					ret = model;
				} else {
					ret.addAll(model);
				}
			}
		}
		
		return ret;
	}
	
}
