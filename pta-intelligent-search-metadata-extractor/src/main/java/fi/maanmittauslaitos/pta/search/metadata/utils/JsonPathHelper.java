package fi.maanmittauslaitos.pta.search.metadata.utils;

import fi.maanmittauslaitos.pta.search.documentprocessor.CustomExtractor;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfigurationImpl;
import fi.maanmittauslaitos.pta.search.documentprocessor.ListCustomExtractor;

import java.util.Arrays;

public class JsonPathHelper {

    public static FieldExtractorConfiguration createJsonPathExtractor(String field, FieldExtractorConfigurationImpl.FieldExtractorType type, String jsonPath) {
        FieldExtractorConfigurationImpl ret = new FieldExtractorConfigurationImpl();
        ret.setField(field);
        ret.setType(type);
        ret.setQuery(jsonPath);
        return ret;
    }

    public static FieldExtractorConfiguration createCustomJsonPathExtractor(String field, CustomExtractor extractor, String jsonPath) {
        FieldExtractorConfigurationImpl ret = (FieldExtractorConfigurationImpl) createJsonPathExtractor(field,
                FieldExtractorConfigurationImpl.FieldExtractorType.CUSTOM_CLASS, jsonPath);
        ret.setCustomExtractor(extractor);
        return ret;
    }

    public static FieldExtractorConfiguration createCustomListJsonPathExtractor(String field, ListCustomExtractor extractor, String jsonPath) {
        FieldExtractorConfigurationImpl ret = (FieldExtractorConfigurationImpl) createJsonPathExtractor(field,
                FieldExtractorConfigurationImpl.FieldExtractorType.CUSTOM_CLASS_SINGLE_VALUE, jsonPath);
        ret.setListCustomExtractor(extractor);
        return ret;
    }

    public static FieldExtractorConfiguration createFirstMatchingJsonPathExtractor(String field, String... jsonPaths) {
        FieldExtractorConfigurationImpl ret = (FieldExtractorConfigurationImpl) createJsonPathExtractor(field,
                FieldExtractorConfigurationImpl.FieldExtractorType.FIRST_MATCHING_FROM_MULTIPLE_QUERIES, jsonPaths[0]);
        ret.setExtraQueries(Arrays.asList(jsonPaths).subList(1, jsonPaths.length));
        return ret;
    }

}
