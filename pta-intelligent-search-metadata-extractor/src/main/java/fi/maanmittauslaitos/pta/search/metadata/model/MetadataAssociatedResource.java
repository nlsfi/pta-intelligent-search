package fi.maanmittauslaitos.pta.search.metadata.model;

public class MetadataAssociatedResource {

    private String metadataId;
    private String title;
    private String type;
    private String portalMetadataLink;

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

    public String getPortalMetadataLink() {
        return portalMetadataLink;
    }

    public void setPortalMetadataLink(String portalMetadataLink) {
        this.portalMetadataLink = portalMetadataLink;
    }
}
