package fi.maanmittauslaitos.pta.search;

import java.io.InputStream;

import fi.maanmittauslaitos.pta.search.index.DocumentSink;
import fi.maanmittauslaitos.pta.search.index.DocumentSink.IndexResult;
import fi.maanmittauslaitos.pta.search.xpath.XPathProcessor;

public class Harvester {
	public static void main(String[] args) throws Exception
	{		
		HarvesterConfig config = new HarvesterConfig();
		
		HarvesterSource source = config.getCSWSource();
		
		XPathProcessor processor = config.getCSWRecordProcessor();
		
		DocumentSink sink = config.getDocumentSink();
		
		
		for (InputStream is : source) {
			if (is == null) {
				System.out.println("Source is null, skipping, or stopping");
				return;
			}
			try {
				Document doc = processor.processDocument(is);
				
				System.out.println(doc.getFields().get("abstract_maui_uri"));
				 
				//if (true) return;
				
				IndexResult result = sink.indexDocument(doc);
				System.out.println(" => "+result);
			} finally {
				is.close();
			}
			
			
		}
	}

}
