package fi.maanmittauslaitos.pta.search.metadata.extractor;

import fi.maanmittauslaitos.pta.search.metadata.model.CodeListValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import javax.xml.xpath.*;

public class CodeListValueXmlCustomExtractor extends XmlCustomExtractor {

    private static Logger logger = LogManager.getLogger(CodeListValueXmlCustomExtractor.class);

    public CodeListValueXmlCustomExtractor() {
        super();
    }

    public CodeListValueXmlCustomExtractor(boolean isThrowException) {
        super(isThrowException);
    }

    @Override
    public Object process(XPath xPath, Node node) throws XPathException {
        logger.trace("Processing related resources");
        CodeListValue value = null;
        try {
            XPathExpression listExpr =  xPath.compile("./@codeList");
            String list = (String) listExpr.evaluate(node, XPathConstants.STRING);

            XPathExpression typeExpr = xPath.compile("name(.)");
            String type = (String) typeExpr.evaluate(node, XPathConstants.STRING);

            XPathExpression valueExpr = xPath.compile("./@codeListValue");
            String val = (String) valueExpr.evaluate(node, XPathConstants.STRING);

            value = new CodeListValue();
            value.setList(list);
            value.setType(type);
            value.setValue(val);

        }catch (XPathExpressionException e) {
            handleExtractorException(e, null);
        }
        return value;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
