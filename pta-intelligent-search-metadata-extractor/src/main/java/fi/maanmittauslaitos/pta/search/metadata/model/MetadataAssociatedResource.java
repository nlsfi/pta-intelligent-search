package fi.maanmittauslaitos.pta.search.metadata.model;

public class MetadataAssociatedResource {

    public static final String PTH_RESOURCE_TYPE_SERVICE = "service";
    public static final String PTH_RESOURCE_TYPE_DATASET = "dataset";
    public static final String PTH_RESOURCE_TYPE_SERIES = "series";
    public static final String PTH_RESOURCE_TYPE_OTHER = "other";


    private String metadataId;
    private String title;
    private String type;
    private String url;

    public String getMetadataId() {
        return metadataId;
    }

    public void setMetadataId(String metadataId) {
        this.metadataId = metadataId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isService() {
        return PTH_RESOURCE_TYPE_SERVICE.equals(this.type);
    }

    public boolean isDataset() {
        return PTH_RESOURCE_TYPE_DATASET.equals(this.type) || PTH_RESOURCE_TYPE_SERIES.equals(type);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
