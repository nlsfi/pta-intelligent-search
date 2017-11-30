package fi.maanmittauslaitos.pta.search;

import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.geotoolkit.client.AbstractClientFactory;
import org.geotoolkit.csw.CSWClientFactory;
import org.geotoolkit.csw.CatalogServicesClient;
import org.geotoolkit.csw.GetRecordsRequest;
import org.geotoolkit.csw.xml.AbstractCapabilities;
import org.geotoolkit.csw.xml.ResultType;
import org.opengis.parameter.ParameterValueGroup;

import fi.maanmittauslaitos.pta.search.xpath.FieldExtractorConfiguration;
import fi.maanmittauslaitos.pta.search.xpath.FieldExtractorConfiguration.FieldExtractorType;
import fi.maanmittauslaitos.pta.search.xpath.XPathExtractionConfiguration;
import fi.maanmittauslaitos.pta.search.xpath.XPathProcessor;
import fi.maanmittauslaitos.pta.search.xpath.XPathProcessorFactory;

public class CSWClientTest {
	public static void main(String[] args) throws Exception {
		
		String serviceURL = "http://paikkatietohakemisto.fi/geonetwork/srv/en/csw";
		
		CSWClientFactory cswClientFactory = new CSWClientFactory();
		
		ParameterValueGroup clientFactoryParams = CSWClientFactory.PARAMETERS.createValue();

		clientFactoryParams.parameter(AbstractClientFactory.URL.getName().getCode()).setValue(new URL(serviceURL));


		CatalogServicesClient catalogServicesClient = (CatalogServicesClient) cswClientFactory.open(clientFactoryParams);
		
		
		GetRecordsRequest req = catalogServicesClient.createGetRecords();
		
		req.setMaxRecords(10);
		req.setStartPosition(2000);
		req.setTypeNames("gmd:MD_Metadata");
		req.setNamespace("gmd:http://www.isotc211.org/2005/gmd");
		req.setConstraintLanguage("Filter");
		req.setConstraintLanguageVersion("1.1.0");
		req.setResultType(ResultType.RESULTS);
		//req.setOutputFormat(arg0);
		
		
		System.out.println(req);
		try (InputStream is = req.getResponseStream()) {
			XPathExtractionConfiguration configuration = new XPathExtractionConfiguration();
			configuration.getNamespaces().put("dc", "http://purl.org/dc/elements/1.1/");
			configuration.getNamespaces().put("csw", "http://www.opengis.net/cat/csw/2.0.2");
			
			FieldExtractorConfiguration numberOfRecordsMatched = new FieldExtractorConfiguration();
			numberOfRecordsMatched.setField("numberOfRecordsMatched");
			numberOfRecordsMatched.setType(FieldExtractorType.FIRST_MATCHING_VALUE);
			numberOfRecordsMatched.setXpath("//csw:SearchResults/@numberOfRecordsMatched");
			configuration.getFieldExtractors().add(numberOfRecordsMatched);
			
			FieldExtractorConfiguration ids = new FieldExtractorConfiguration();
			ids.setField("ids");
			ids.setType(FieldExtractorType.ALL_MATCHING_VALUES);
			ids.setXpath("//dc:identifier/text()");
			configuration.getFieldExtractors().add(ids);
			
			XPathProcessorFactory xppf = new XPathProcessorFactory();
			XPathProcessor processor = xppf.createProcessor(configuration);
			Document doc = processor.processDocument(is);
			System.out.println("ids = "+doc.getFields().get("ids"));
			System.out.println("numberOfRecordsMatched = "+doc.getFields().get("numberOfRecordsMatched"));
		}
		/*
		//catalogServicesClient.createHarvest().
		AbstractCapabilities caps = catalogServicesClient.getCapabilities();
		System.out.println(caps);
		System.out.println(caps.getVersion());
		*/
	}
}
