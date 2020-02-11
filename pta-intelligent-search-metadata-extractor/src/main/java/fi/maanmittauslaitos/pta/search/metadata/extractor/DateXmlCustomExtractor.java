package fi.maanmittauslaitos.pta.search.metadata.extractor;

import fi.maanmittauslaitos.pta.search.metadata.model.CodeListValue;
import fi.maanmittauslaitos.pta.search.metadata.model.MetadataDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;

public class DateXmlCustomExtractor extends XmlCustomExtractor {

    private static Logger logger = LoggerFactory.getLogger(DateXmlCustomExtractor.class);

    public DateXmlCustomExtractor() {
        super();
    }

    public DateXmlCustomExtractor(boolean isThrowException) {
        super(isThrowException);
    }

    @Override
    public Object process(XPath xPath, Node node) throws XPathException {
        logger.trace("Processing related resources");

        MetadataDate date = null;
        try {

            XPathExpression dateExpr =
                    xPath.compile("./*/gmd:date/gco:Date/text()");
            String dateValue = (String) dateExpr.evaluate(node, XPathConstants.STRING);

            XPathExpression codeListValueEpr =
                    xPath.compile("./*/gmd:dateType/gmd:CI_DateTypeCode[@codeListValue]");
            Node codeListValueNode = (Node) codeListValueEpr.evaluate(node, XPathConstants.NODE);

            CodeListValueXmlCustomExtractor ext = new CodeListValueXmlCustomExtractor();
            CodeListValue dateTypeValue = (CodeListValue) ext.process(xPath, codeListValueNode);

            date = new MetadataDate();
            date.setDate(dateValue);
            date.setType(dateTypeValue);

        } catch (XPathException e) {
            handleExtractorException(e, null);
        }

        return date;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
