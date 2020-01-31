package fi.maanmittauslaitos.pta.search.metadata.extractor;

import fi.maanmittauslaitos.pta.search.metadata.model.CodeListValue;
import fi.maanmittauslaitos.pta.search.metadata.model.MetadataDate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;

public class DateExtractor extends XPathCustomExtractor {

    private static Logger logger = LogManager.getLogger(DateExtractor.class);

    @Override
    public Object process(XPath xPath, Node node) throws XPathException {
        logger.trace("Processing related resources");

        MetadataDate date = new MetadataDate();
        XPathExpression dateExpr =
                xPath.compile("./*/gmd:date/gco:Date/text()");
        String dateValue = (String) dateExpr.evaluate(node, XPathConstants.STRING);

        date.setDate(dateValue);

        XPathExpression codeListValueEpr =
                xPath.compile("./*/gmd:dateType/gmd:CI_DateTypeCode[@codeListValue]");
        Node codeListValueNode = (Node) codeListValueEpr.evaluate(node, XPathConstants.NODE);
        CodeListValueExtractor ext = new CodeListValueExtractor();
        CodeListValue dateTypeValue = (CodeListValue) ext.process(xPath, codeListValueNode);
        date.setType(dateTypeValue);

        return date;
    }
}
