package fi.maanmittauslaitos.pta.search.metadata.extractor;

import fi.maanmittauslaitos.pta.search.metadata.model.MetadataAssociatedResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import javax.xml.xpath.*;

public class AssociatedResourcesXPathCustomExtractor extends XPathCustomExtractor {

    private static Logger logger = LogManager.getLogger(AssociatedResourcesXPathCustomExtractor.class);
    private static final String PTH_RESOURCE_TYPE_SERVICE = "service";
    @SuppressWarnings("unused")
    private static final String PTH_RESOURCE_TYPE_DATASET = "dataset";
    @SuppressWarnings("unused")
    private static final String PTH_RESOURCE_TYPE_SERIES = "series";

    @Override
    public Object process(XPath xPath, Node node) throws XPathException {
        logger.trace("Processing related resources");

        MetadataAssociatedResource associatedResource = null;

        try {
            associatedResource = new MetadataAssociatedResource();
            XPathExpression titleExpr =
                    xPath.compile("./dc:title/text()");
            String title = (String)titleExpr.evaluate(node, XPathConstants.STRING);

            XPathExpression metadataIdExpr =
                    xPath.compile("./dc:identifier/text()");
            String metadataId = (String)metadataIdExpr.evaluate(node, XPathConstants.STRING);

            XPathExpression typeExpr =
                    xPath.compile("./dc:type/text()");
            String type = (String)typeExpr.evaluate(node, XPathConstants.STRING);

            associatedResource.setMetadataId(metadataId);
            associatedResource.setTitle(title);
            associatedResource.setType(resolvePtaResourceType(type));

        } catch (XPathExpressionException e) {
            logger.debug("Error reading data from node", e);
        }


        return associatedResource;
    }

    /**
     * Convert the type to the type used by PTA. Paikkatietohakemisto offers the following types, "series", "dataset" and "series". Currently PTA only uses differentiates between service and data sets.
     *
     * @param type Paikkatietohakemisto resource type
     * @return the PTA resource type. either PortalCswReaderConfig.TYPE_SERVICE or PortalCswReaderConfig.TYPE_DATASET.
     */
    private String resolvePtaResourceType(String type) {
        switch (type) {
            case PTH_RESOURCE_TYPE_SERVICE:
                return PortalCswReaderConfig.TYPE_SERVICE;
            default:
                return PortalCswReaderConfig.TYPE_DATASET;
        }
    }
}
