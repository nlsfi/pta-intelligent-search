package fi.maanmittauslaitos.pta.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessor;
import fi.maanmittauslaitos.pta.search.index.DocumentSink;
import fi.maanmittauslaitos.pta.search.metadata.ISOMetadataFields;
import org.springframework.boot.CommandLineRunner;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractHarvester implements CommandLineRunner {

	abstract protected DocumentSink getDocumentSink(HarvesterConfig config, String[] args);

	abstract protected DocumentProcessor getDocumentProcessor(HarvesterConfig config) throws ParserConfigurationException, IOException;

	abstract protected HarvesterSource getHarvesterSource(HarvesterConfig config);


	@Override
	public void run(String... args) throws Exception {
		HarvesterConfig config = new HarvesterConfig();

		HarvesterSource source = getHarvesterSource(config);

		DocumentProcessor processor = getDocumentProcessor(config);

		DocumentSink sink = getDocumentSink(config, args);

		boolean store = args.length > 0 && args[0].equals("store");

		int updated = 0;
		int inserted = 0;

		sink.startIndexing();

		for (InputStream is : source) {
			if (is == null) {
				System.out.println("Source is null, skipping, or stopping");
				return;
			}
			try {
				Document doc = processor.processDocument(is);

				if (store && (updated + inserted) < 100) {
					writeDocumentToFile(doc);
				}

				DocumentSink.IndexResult result = sink.indexDocument(doc);
				switch (result) {
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


		int deleted = sink.stopIndexing();

		System.out.println("Inserted " + inserted + " documents");
		System.out.println("Updated " + updated + " documents");
		System.out.println("Deleted " + deleted + " documents");
	}

	private void writeDocumentToFile(Document doc) throws IOException {
		String id = doc.getValue(ISOMetadataFields.ID, String.class);
		ObjectMapper objectMapper = new ObjectMapper();
		try (FileOutputStream out = new FileOutputStream("indexed-documents/" + id + ".json")) {
			objectMapper.writeValue(out, doc.getFields());
		}
	}

}
