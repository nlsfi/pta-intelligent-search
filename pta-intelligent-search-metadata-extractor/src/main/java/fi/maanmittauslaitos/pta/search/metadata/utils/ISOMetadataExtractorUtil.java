package fi.maanmittauslaitos.pta.search.metadata.utils;

import fi.maanmittauslaitos.pta.search.documentprocessor.CustomExtractor;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfiguration;
import fi.maanmittauslaitos.pta.search.documentprocessor.FieldExtractorConfigurationImpl;

public class ISOMetadataExtractorUtil {

    public static FieldExtractorConfiguration createXPathExtractor(String field, FieldExtractorConfigurationImpl.FieldExtractorType type, String xpath) {
        FieldExtractorConfigurationImpl ret = new FieldExtractorConfigurationImpl();
        ret.setField(field);
        ret.setType(type);
        ret.setQuery(xpath);

        return ret;
    }

    public static  FieldExtractorConfiguration createXPathExtractor(String field, CustomExtractor extractor, String xpath) {
        FieldExtractorConfigurationImpl ret = new FieldExtractorConfigurationImpl();
        ret.setField(field);
        ret.setType(FieldExtractorConfigurationImpl.FieldExtractorType.CUSTOM_CLASS);
        ret.setQuery(xpath);
        ret.setCustomExtractor(extractor);

        return ret;
    }
}
