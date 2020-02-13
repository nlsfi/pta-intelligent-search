package fi.maanmittauslaitos.pta.search.metadata.extractor;

import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.metadata.model.MetadataAssociatedResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.xml.xpath.*;

public class AssociatedResourcesXmlCustomExtractor extends XmlCustomExtractor {

    private static Logger logger = LoggerFactory.getLogger(AssociatedResourcesXmlCustomExtractor.class);

    @Override
    public Object process(XPath xPath, Node node) throws DocumentProcessingException {
        logger.trace("Processing related resources");

        MetadataAssociatedResource associatedResource = null;

        try {
            XPathExpression titleExpr =
                    xPath.compile("./dc:title/text()");
            String title = (String)titleExpr.evaluate(node, XPathConstants.STRING);

            XPathExpression metadataIdExpr =
                    xPath.compile("./dc:identifier/text()");
            String metadataId = (String)metadataIdExpr.evaluate(node, XPathConstants.STRING);

            XPathExpression typeExpr =
                    xPath.compile("./dc:type/text()");
            String type = (String)typeExpr.evaluate(node, XPathConstants.STRING);

            associatedResource = new MetadataAssociatedResource();
            associatedResource.setMetadataId(metadataId);
            associatedResource.setTitle(title);
            associatedResource.setType(type);

        } catch (XPathExpressionException e) {
            throw new DocumentProcessingException(e);

        }

        return associatedResource;
    }
}
