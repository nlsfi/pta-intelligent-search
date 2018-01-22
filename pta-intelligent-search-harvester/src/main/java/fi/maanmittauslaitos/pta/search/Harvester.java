package fi.maanmittauslaitos.pta.search;

import java.io.InputStream;
import java.util.List;

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
				//continue;
			}
			try {
				Document doc = processor.processDocument(is);
				
				//System.out.println(doc.getFields());
				
				List<String> tmp = doc.getListValue("onlineResource", String.class);
				if (tmp != null && tmp.size() != 0) {
					boolean isService = doc.isFieldTrue("isService");
					boolean isDataset = doc.isFieldTrue("isDataset");
					String type;
					if (isService && isDataset) {
						type = "BOTH!?!";
					} else if (isService) {
						type = "service";
					} else if (isDataset) {
						type = "dataset";
					} else {
						type = "UNKNOWN!?";
					}
					System.out.println(doc.getFields().get("@id")+" ("+type+"): "+tmp);
				}
				
				System.out.println(doc.getFields().get("abstract_maui_uri"));
			
				//if (true) return;
				
				IndexResult result = sink.indexDocument(doc);
				//System.out.println(" => "+result);
			} finally {
				is.close();
			}
			
			
		}
	}

}
