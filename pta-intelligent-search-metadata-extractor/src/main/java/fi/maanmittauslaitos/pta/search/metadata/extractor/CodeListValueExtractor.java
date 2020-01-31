package fi.maanmittauslaitos.pta.search.metadata.extractor;

import fi.maanmittauslaitos.pta.search.metadata.model.CodeListValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;

public class CodeListValueExtractor extends XPathCustomExtractor {

    private static Logger logger = LogManager.getLogger(CodeListValueExtractor.class);

    @Override
    public Object process(XPath xPath, Node node) throws XPathException {
        logger.trace("Processing related resources");

        CodeListValue value = new CodeListValue();

        XPathExpression listExpr =
                xPath.compile("./@codeList");
        String list = (String) listExpr.evaluate(node, XPathConstants.STRING);
        value.setList(list);

        XPathExpression typeExpr =
                xPath.compile("name(.)");
        String type = (String) typeExpr.evaluate(node, XPathConstants.STRING);
        value.setType(type);

        XPathExpression valueExpr =
                xPath.compile("./@codeListValue");
        String val = (String) valueExpr.evaluate(node, XPathConstants.STRING);
        value.setValue(val);

        return value;
    }
}
