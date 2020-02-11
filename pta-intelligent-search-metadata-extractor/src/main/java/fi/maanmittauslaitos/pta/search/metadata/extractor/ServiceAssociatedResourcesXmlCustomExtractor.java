package fi.maanmittauslaitos.pta.search.metadata.extractor;

import fi.maanmittauslaitos.pta.search.metadata.model.MetadataAssociatedResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Node;

import javax.xml.xpath.*;

public class ServiceAssociatedResourcesXmlCustomExtractor extends XmlCustomExtractor {

    private Logger logger = LoggerFactory.getLogger(ServiceAssociatedResourcesXmlCustomExtractor.class);

    public ServiceAssociatedResourcesXmlCustomExtractor() {
        super();
    }

    public ServiceAssociatedResourcesXmlCustomExtractor(boolean isThrowException) {
        super(isThrowException);
    }

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

            XPathExpression urlExpr =
                    xPath.compile("./@xlink:href");
            String url = (String)urlExpr.evaluate(node, XPathConstants.STRING);
            associatedResource.setUrl(url);

        } catch (XPathExpressionException e) {
            handleExtractorException(e, null);
        }


        return associatedResource;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
