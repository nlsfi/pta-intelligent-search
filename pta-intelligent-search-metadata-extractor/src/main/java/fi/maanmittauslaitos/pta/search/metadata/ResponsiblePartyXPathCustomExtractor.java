package fi.maanmittauslaitos.pta.search.metadata;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fi.maanmittauslaitos.pta.search.documentprocessor.XPathCustomExtractor;
import fi.maanmittauslaitos.pta.search.metadata.model.ResponsibleParty;

public class ResponsiblePartyXPathCustomExtractor implements XPathCustomExtractor {
	private static Logger logger = Logger.getLogger(ResponsiblePartyXPathCustomExtractor.class);
	
	@Override
	public Object process(XPath xPath, Node node) throws XPathException {
		ResponsibleParty ret = new ResponsibleParty();
		
		String organisationName;
		String isoRole;
		
		if (logger.isTraceEnabled()) {
			logger.trace("Reading organisation information");
		}
		
		XPathExpression nameExpr =
				xPath.compile("./gmd:organisationName/gco:CharacterString/text()");
		organisationName = (String)nameExpr.evaluate(node, XPathConstants.STRING);
		
		XPathExpression isoRoleExpr = 
				xPath.compile("./gmd:role/gmd:CI_RoleCode[@codeList = 'http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_RoleCode']/@codeListValue");
		isoRole = (String)isoRoleExpr.evaluate(node, XPathConstants.STRING);
		
		if (logger.isTraceEnabled()) {
			logger.trace("\tOrganisation role: "+isoRole);
			logger.trace("\tCanonical name: "+organisationName);
		}
		
		XPathExpression localisedNameExpr = 
				xPath.compile("./gmd:organisationName/gmd:PT_FreeText/*/gmd:LocalisedCharacterString");
		
		NodeList names = (NodeList)localisedNameExpr.evaluate(node, XPathConstants.NODESET);
		for (int i = 0; i < names.getLength(); i++) {
			Node localisedNameNode = names.item(i);
			try {
				
				String language = localisedNameNode.getAttributes().getNamedItem("locale").getNodeValue();
				if (language.startsWith("#")) {
					language = language.substring(1);
				}
				String value = localisedNameNode.getTextContent();
				
				ret.getLocalisedOrganisationName().put(language, value);
				
				if (logger.isTraceEnabled()) {
					logger.trace("\tLocalized organisation name: "+value+" ("+language+")");
				}
				
			} catch(Exception e) {
				logger.info("Unable to extract localized organisation name", e);
			}
		}
		
		ret.setOrganisationName(organisationName);
		ret.setIsoRole(isoRole);
		
		return ret;
	}

}
