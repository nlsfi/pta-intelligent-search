package fi.maanmittauslaitos.pta.search.metadata.extractor;

import fi.maanmittauslaitos.pta.search.documentprocessor.DocumentProcessingException;
import fi.maanmittauslaitos.pta.search.metadata.model.EmptyNodeList;
import fi.maanmittauslaitos.pta.search.metadata.model.ResponsibleParty;
import fi.maanmittauslaitos.pta.search.metadata.model.TextRewriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static fi.maanmittauslaitos.pta.search.metadata.utils.XPathHelper.matches;

public class ResponsiblePartyXmlCustomExtractor extends XmlCustomExtractor {

	private static final Logger logger = LoggerFactory.getLogger(ResponsiblePartyXmlCustomExtractor.class);

	private TextRewriter organisationNameRewriter = new TextRewriter() {
		
		@Override
		public String rewrite(String name, String language) {
			return name;
		}
		
		@Override
		public String rewrite(String name) {
			return name;
		}
	};

	public void setOrganisationNameRewriter(TextRewriter organisationNameRewriter) {
		this.organisationNameRewriter = organisationNameRewriter;
	}
	
	public TextRewriter getOrganisationNameRewriter() {
		return organisationNameRewriter;
	}

	@Override
	public Object process(XPath xPath, Node node) throws DocumentProcessingException {
		ResponsibleParty ret = null;

		try {
			String organisationName;
			String isoRole;

			if (logger.isTraceEnabled()) {
				logger.trace("Reading organisation information");
			}

			XPathExpression nameExpr =
					xPath.compile("./gmd:organisationName/*/text()");
			organisationName = (String)nameExpr.evaluate(node, XPathConstants.STRING);

			organisationName = getOrganisationNameRewriter().rewrite(organisationName);

			XPathExpression isoRoleExpr =
					xPath.compile("./gmd:role/gmd:CI_RoleCode[" +
							matches("@codeList", "'http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_RoleCode'") +
							"]/@codeListValue");
			isoRole = (String)isoRoleExpr.evaluate(node, XPathConstants.STRING);

			if (logger.isTraceEnabled()) {
				logger.trace("\tOrganisation role: "+isoRole);
				logger.trace("\tCanonical name: "+organisationName);
			}

			XPathExpression localisedNameExpr =
					xPath.compile("./gmd:organisationName/gmd:PT_FreeText/*/gmd:LocalisedCharacterString");

			NodeList names = (NodeList)localisedNameExpr.evaluate(node, XPathConstants.NODESET);
			ret = new ResponsibleParty();

			for (int i = 0; i < names.getLength(); i++) {
				Node localisedNameNode = names.item(i);
				try {

					String language = localisedNameNode.getAttributes().getNamedItem("locale").getNodeValue();
					if (language.startsWith("#")) {
						language = language.substring(1);
					}
					String value = localisedNameNode.getTextContent();

					value = getOrganisationNameRewriter().rewrite(value, language);

					ret.getLocalizedPartyName().put(language, value);

					if (logger.isTraceEnabled()) {
						logger.trace("\tLocalized organisation name: "+value+" ("+language+")");
					}

				} catch(Exception e) {
					logger.info("Unable to extract localized organisation name", e);
				}
			}

			XPathExpression emailsExpr =
					xPath.compile("./gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/gco:CharacterString");
			NodeList emailsNodeList = Optional.of((NodeList) emailsExpr.evaluate(node, XPathConstants.NODESET)).orElse(new EmptyNodeList());
			List<String> emails = IntStream.range(0, emailsNodeList.getLength())
					.mapToObj(emailsNodeList::item)
					.map(Node::getTextContent)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			ret.setEmail(emails);

			ret.setPartyName(organisationName);
			ret.setIsoRole(isoRole);

		} catch (XPathException e) {
			throw new DocumentProcessingException(e);
		}
		return ret;
	}

}
