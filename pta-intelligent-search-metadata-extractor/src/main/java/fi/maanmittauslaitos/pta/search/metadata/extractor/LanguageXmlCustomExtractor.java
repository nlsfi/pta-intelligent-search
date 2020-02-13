package fi.maanmittauslaitos.pta.search.metadata.extractor;

import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.metadata.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

public class LanguageXmlCustomExtractor extends XmlCustomExtractor {

    private Logger logger = LoggerFactory.getLogger(LanguageXmlCustomExtractor.class);

    /**
     * Languages can apperantly be  in two different formats (at least).
     *
     *
     * https://www.paikkatietohakemisto.fi/geonetwork/srv/fin/csw?request=GetRecordById&service=CSW&version=2.0.2&elementSetName=full&id=9f13a9c0-7cea-4398-a401-35dbc24bfe4f&outputSchema=http://www.isotc211.org/2005/gmd
     *
     * <gmd:language>
     *  <gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/" codeListValue="fin"/>
     * </gmd:language>
     *
     *
     * https://www.paikkatietohakemisto.fi/geonetwork/srv/fin/csw?request=GetRecordById&service=CSW&version=2.0.2&elementSetName=full&id=7f2019c1-25a3-4568-9eb2-8d0107358bd3&outputSchema=http://www.isotc211.org/2005/gmd
     *
     * <gmd:language>
     *  <gco:CharacterString>fin</gco:CharacterString>
     * </gmd:language>
     *
     * @param xPath
     * @param node
     * @return
     */
    @Override
    public Object process(XPath xPath, Node node) throws DocumentProcessingException {
        String language = null;
        try {
            XPathExpression langGcoExpr =
                    xPath.compile("./gco:CharacterString/text()");
            String langGco = (String) langGcoExpr.evaluate(node, XPathConstants.STRING);

            XPathExpression langGmdExpr =
                    xPath.compile(" ./gmd:LanguageCode/@codeListValue");
            String langGmd = (String) langGmdExpr.evaluate(node, XPathConstants.STRING);

            if(!StringUtils.isEmpty(langGco) && !StringUtils.isEmpty(langGmd)){
                //prefer gmd over gco (for no real reason, but it seems more official, since it has a codeList)
                language = langGmd;
            }
            else if (!StringUtils.isEmpty(langGmd)) {
                language = langGmd;

            }
            else if (!StringUtils.isEmpty(langGco)) {
                language = langGco;

            }

        } catch (XPathExpressionException e) {
            throw new DocumentProcessingException(e);
        }

        return language;
    }
}