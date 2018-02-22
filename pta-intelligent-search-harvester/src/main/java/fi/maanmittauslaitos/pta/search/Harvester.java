package fi.maanmittauslaitos.pta.search;

import java.io.InputStream;

import fi.maanmittauslaitos.pta.search.index.DocumentSink;
import fi.maanmittauslaitos.pta.search.index.DocumentSink.IndexResult;
import fi.maanmittauslaitos.pta.search.xpath.DocumentProcessor;

public class Harvester {
	public static void main(String[] args) throws Exception
	{		
		HarvesterConfig config = new HarvesterConfig();
		
		HarvesterSource source = config.getCSWSource();
		
		DocumentProcessor processor = config.getCSWRecordProcessor();
		
		DocumentSink sink = config.getDocumentSink();
		
		int updated = 0;
		int inserted = 0;
		
		for (InputStream is : source) {
			if (is == null) {
				System.out.println("Source is null, skipping, or stopping");
				return;
			}
			try {
				Document doc = processor.processDocument(is);
				
				IndexResult result = sink.indexDocument(doc);
				switch(result) {
				case UPDATED:
					updated++;
					break;
				case INSERTED:
					inserted++;
					break;
				}

			} finally {
				is.close();
			}
		}
		
		System.out.println("Inserted "+inserted+" documents");
		System.out.println("Updated "+updated+" documents");
	}

}
