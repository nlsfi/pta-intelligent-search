package fi.maanmittauslaitos.pta.search.metadata.extractor;

import fi.maanmittauslaitos.pta.search.metadata.model.MetadataAssociatedResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import javax.xml.xpath.*;

public class ServiceAssociatedResourcesXPathCustomExtractor extends XPathCustomExtractor {

    private Logger logger = LogManager.getLogger(ServiceAssociatedResourcesXPathCustomExtractor.class);

    @Override
    public Object process(XPath xPath, Node node) throws XPathException {
        MetadataAssociatedResource associatedResource = null;

        try {
            associatedResource = new MetadataAssociatedResource();

            XPathExpression metadataIdExpr =
                    xPath.compile("./@uuidref");
            String metadataId = (String)metadataIdExpr.evaluate(node, XPathConstants.STRING);

            associatedResource.setMetadataId(metadataId);
            // A service can't be linked to another service -> always data sets, so we hard-code it here
            associatedResource.setType("isDataset");

        } catch (XPathExpressionException e) {
            logger.debug("Error reading data from node", e);
        }


        return associatedResource;
    }
}
