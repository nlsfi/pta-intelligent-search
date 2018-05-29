package fi.maanmittauslaitos.pta.search.metadata;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;

import org.w3c.dom.Node;

import fi.maanmittauslaitos.pta.search.documentprocessor.XPathCustomExtractor;
import fi.maanmittauslaitos.pta.search.metadata.model.ResponsibleParty;

public class ResponsiblePartyXPathCustomExtractor implements XPathCustomExtractor {

	@Override
	public Object process(XPath xPath, Node node) throws XPathException {
		ResponsibleParty ret = new ResponsibleParty();
		
		String organisationName;
		String isoRole;
		
		XPathExpression nameExpr =
				xPath.compile("./gmd:organisationName/gco:CharacterString/text()");
		organisationName = (String)nameExpr.evaluate(node, XPathConstants.STRING);
		
		XPathExpression isoRoleExpr = 
				xPath.compile("./gmd:role/gmd:CI_RoleCode[@codeList = 'http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_RoleCode']/@codeListValue");
		isoRole = (String)isoRoleExpr.evaluate(node, XPathConstants.STRING);
		
		ret.setOrganisationName(organisationName);
		ret.setIsoRole(isoRole);
		
		return ret;
	}

}
