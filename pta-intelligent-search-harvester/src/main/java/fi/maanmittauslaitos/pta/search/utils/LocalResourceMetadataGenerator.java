package fi.maanmittauslaitos.pta.search.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.maanmittauslaitos.pta.search.HarvesterConfig;
import fi.maanmittauslaitos.pta.search.HarvesterSource;
import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.index.DocumentSink;
import fi.maanmittauslaitos.pta.search.metadata.ISOMetadataFields;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.CommandLineRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LocalResourceMetadataGenerator implements CommandLineRunner {
	public static void main(String[] args) throws Exception
	{
		LocalResourceMetadataGenerator localResourceMetadataGenerator = new LocalResourceMetadataGenerator();
		localResourceMetadataGenerator.run(args);
	}

	@Override
	public void run(String... args) throws Exception {
		HarvesterConfig config = new HarvesterConfig();
		
		HarvesterSource source = config.getLocalCSWSource();
		
		DocumentProcessor processor = config.getCSWRecordProcessor();

		String sinkfile = args.length > 0 ? args[0] : "generatedResourceMetatada.zip";

		System.out.println("Sinkfile is " + sinkfile);

		DocumentSink sink = config.getLocalDocumentSink(sinkfile);


		int inserted = 0;
		sink.startIndexing();
		for (InputStream is : source) {
			if (is == null) {
				System.out.println("Source is null, skipping, or stopping");
				return;
			}
			try {

				Document doc = processor.processDocument(is);
				DocumentSink.IndexResult result = sink.indexDocument(doc);

				inserted++;

			} finally {
				is.close();
			}
		}

		sink.stopIndexing();
		System.out.println("Inserted "+inserted+" documents");
	}

	private void writeDocumentToFile(Document doc) throws IOException, JsonProcessingException
	{
		String id = doc.getValue(ISOMetadataFields.ID, String.class);
		ObjectMapper objectMapper = new ObjectMapper();
		try (FileOutputStream out = new FileOutputStream("indexed-documents/"+id+".json")) {
			objectMapper.writeValue(out, doc.getFields());
		}
	}

	private void downloadXmlFiles() throws IOException {
		HarvesterConfig config = new HarvesterConfig();
		HarvesterSource source = config.getCSWSource();

		int inserted = 0;

		for (InputStream is : source) {
			if (is == null) {
				System.out.println("Source is null, skipping, or stopping");
				return;
			}
			try {

				File targetFile = new File("csws/"+ inserted + ".xml");
				FileUtils.copyInputStreamToFile(is, targetFile);
				inserted++;
				if (inserted == 50) {
					return;
				}

			} finally {
				is.close();
			}
		}

		System.out.println("Inserted "+inserted+" documents");
	}

}
