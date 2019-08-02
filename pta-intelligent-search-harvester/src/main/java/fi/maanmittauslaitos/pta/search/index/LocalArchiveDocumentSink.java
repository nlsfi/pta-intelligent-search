package fi.maanmittauslaitos.pta.search.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.maanmittauslaitos.pta.search.documentprocessor.Document;
import fi.maanmittauslaitos.pta.search.metadata.ISOMetadataFields;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class LocalArchiveDocumentSink implements DocumentSink {

    private String outputArchive;
    private Path localArchiveDocumentSink = null;
    private String outputFileName;


    @Override
    public void startIndexing() throws SinkProcessingException {
        try {
            localArchiveDocumentSink = Files.createTempDirectory("LocalArchiveDocumentSink");
        } catch (IOException e) {
            throw new SinkProcessingException(e);
        }
    }

    @Override
    public IndexResult indexDocument(Document doc) {
        try {
            writeDocumentToFile(doc);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return IndexResult.INSERTED;
    }

    private void writeDocumentToFile(Document doc) throws IOException {
        String id = doc.getValue(ISOMetadataFields.ID, String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        try (FileOutputStream out = new FileOutputStream(new File(localArchiveDocumentSink.toFile(), id + ".json"))) {
            objectMapper.writeValue(out, doc.getFields());
        }
    }

    @Override
    public int stopIndexing() {
        Path zipFile = Paths.get(outputFileName);
        try {
            Files.deleteIfExists(zipFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(Files.createFile(zipFile)))) {
            Files.walk(localArchiveDocumentSink).filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry entry = new ZipEntry(localArchiveDocumentSink.relativize(path).toString());
                        try {
                            zipOutputStream.putNextEntry(entry);
                            zipOutputStream.write(Files.readAllBytes(path));
                            zipOutputStream.closeEntry();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

            localArchiveDocumentSink.toFile().deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public String getOutputFileName() {
        return outputFileName;
    }
}
