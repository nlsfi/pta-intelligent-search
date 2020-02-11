package fi.maanmittauslaitos.pta.search.metadata.json.extractor;

import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.JsonDocumentQueryImpl;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.JsonQueryResultImpl;
import fi.maanmittauslaitos.pta.search.documentprocessor.query.QueryResult;
import fi.maanmittauslaitos.pta.search.metadata.model.MetadataDownloadLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DownloadLinksCkanCustomExtractor extends JsonPathListCustomExtractor {

    private static final Logger logger = LoggerFactory.getLogger(DownloadLinksCkanCustomExtractor.class);

    public DownloadLinksCkanCustomExtractor() {
        super();
    }

    public DownloadLinksCkanCustomExtractor(boolean isThrowException) {
        super(isThrowException);
    }

    private String parseUrlProtocol(String url) {
        try {
            URL tempUrl = new URL(url);
            return tempUrl.getProtocol();
        } catch (MalformedURLException | NullPointerException e) {
            logger.debug("Error parsing protocol from url: '{}'. Error message: {}", url, e.getMessage());
        }

        return DEFAULT_PARSED_VALUE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object process(JsonDocumentQueryImpl query, List<QueryResult> queryResults) throws DocumentProcessingException {
        List<MetadataDownloadLink> links = new ArrayList<>();
        try {
            queryResults.stream()
                .filter(r -> r instanceof JsonQueryResultImpl)
                .map(r -> (JsonQueryResultImpl) r)
                .forEach(result -> {
                    List<Object> rawResList = result.getRawValue();
                    rawResList.stream()
                            .filter(rawResult -> rawResult instanceof Map)
                            .forEach(rawResult -> {
                                MetadataDownloadLink link = new MetadataDownloadLink();
                                Map<String, Object> res = (Map<String,Object>) rawResult;
                                String url = getValueSafishly(res, "url", DEFAULT_PARSED_VALUE);
                                String protocol = parseUrlProtocol(url);
                                String title = getValueSafishly(res, "name", DEFAULT_PARSED_VALUE);

                                link.setDesc(DEFAULT_PARSED_VALUE);
                                link.setProtocol(protocol);
                                link.setTitle(title);
                                link.setUrl(url);
                                links.add(link);
                            });
                });
        } catch (RuntimeException e) {
            handleExtractorException(e, null);
        }

        return links;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
