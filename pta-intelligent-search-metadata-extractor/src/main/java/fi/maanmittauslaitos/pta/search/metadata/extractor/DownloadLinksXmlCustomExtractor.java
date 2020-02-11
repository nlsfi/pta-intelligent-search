package fi.maanmittauslaitos.pta.search.metadata.extractor;

import fi.maanmittauslaitos.pta.search.metadata.model.MetadataDownloadLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.xml.xpath.*;

public class DownloadLinksXmlCustomExtractor extends XmlCustomExtractor {
    private static Logger logger = LoggerFactory.getLogger(DownloadLinksXmlCustomExtractor.class);

    public DownloadLinksXmlCustomExtractor() {
        super();
    }

    public DownloadLinksXmlCustomExtractor(boolean isThrowException) {
        super(isThrowException);
    }

    @Override
    public Object process(XPath xPath, Node node) throws XPathException {

        MetadataDownloadLink link = null;

        try {
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

            link = new MetadataDownloadLink();
            link.setDesc(desc);
            link.setProtocol(protocol);
            link.setTitle(title);
            link.setUrl(url);
        } catch (XPathExpressionException e) {
            handleExtractorException(e, null);
        }

        return link;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
