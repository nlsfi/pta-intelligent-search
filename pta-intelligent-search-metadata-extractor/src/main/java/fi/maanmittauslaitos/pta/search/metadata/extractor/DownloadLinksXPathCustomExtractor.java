package fi.maanmittauslaitos.pta.search.metadata.extractor;

import fi.maanmittauslaitos.pta.search.metadata.model.MetadataDownloadLink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import javax.xml.xpath.*;

public class DownloadLinksXPathCustomExtractor extends XPathCustomExtractor {
    private static Logger logger = LogManager.getLogger(DownloadLinksXPathCustomExtractor.class);
    
    @Override
    public Object process(XPath xPath, Node node) throws XPathException {

        MetadataDownloadLink link = null;

        try {
            link = new MetadataDownloadLink();
            XPathExpression urlExpr =
                    xPath.compile("./*/gmd:linkage/gmd:URL/text()");
            String url = (String) urlExpr.evaluate(node, XPathConstants.STRING);

            XPathExpression titleExpr =
                    xPath.compile("./*/gmd:name/gco:CharacterString/text()");
            String title = (String) titleExpr.evaluate(node, XPathConstants.STRING);

            XPathExpression protocolExpr =
                    xPath.compile("./*/gmd:protocol/gco:CharacterString/text()");
            String protocol = (String) protocolExpr.evaluate(node, XPathConstants.STRING);

            XPathExpression descExpr =
                    xPath.compile("./*/gmd:description/gco:CharacterString/text()");
            String desc = (String) descExpr.evaluate(node, XPathConstants.STRING);


            link.setDesc(desc);
            link.setProtocol(protocol);
            link.setTitle(title);
            link.setUrl(url);
        } catch (XPathExpressionException e) {
            logger.debug("Error reading data from node", e);
        }

        return link;
    }
}
