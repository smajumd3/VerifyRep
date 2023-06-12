package com.ibm.workday.automation.operation;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import com.ibm.workday.automation.common.CommonConstants;
import com.ibm.workday.automation.model.Tenant;

public class SOAPRequestBuilder implements CommonConstants {

	private static final String EMPTY_VALUE = "";
	private static final String HEADER_SECURITY_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	private static final String HEADER_SECURITY_NS_PREFIX = "wsse";
	private static final String PASSWORD_TYPE_ATTR_VALUE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText";

	private Tenant tenant;
	private MessageFactory messageFactory = null;
	
	public SOAPRequestBuilder() {
		try {
			messageFactory = MessageFactory.newInstance();
			System.out.println(messageFactory.toString());
		} catch (SOAPException e) {
			e.printStackTrace();
		}
	}

	public SOAPRequestBuilder(Tenant tenant) {
		this();
		this.tenant = tenant;
	}

	public SOAPMessage buildSOAPRequest(DataElement rootElement) {
		SOAPMessage soapMessage = null;
		long startTime = System.currentTimeMillis();
		try {
			soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);

			if (tenant != null) {
				envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX,
						HEADER_SECURITY_NAMESPACE);
				createSOAPHeader(envelope.getHeader());
			}

			SOAPBody soapBody = envelope.getBody();

			createElement(rootElement, soapBody);
			long createMessage = System.currentTimeMillis();

			soapMessage.saveChanges();
			System.out.println("Total time in create message: " + (System.currentTimeMillis() - createMessage));


		} catch (SOAPException e) {
			e.printStackTrace();
		}
		System.out.println("Total time in build SOAP Message : " + (System.currentTimeMillis() - startTime));
		return soapMessage;

	}

	private SOAPElement createElement(DataElement element,
			SOAPElement parentElement) {
		SOAPElement mainElement = null;
		try {
			mainElement = parentElement.addChildElement(element.getName(),
					NAMESPACE_PREFIX);
			if (element.getValue() != null && element.getValue() != EMPTY_VALUE) {
				mainElement.addTextNode(element.getValue());
			}
			if (element.getAttributes().size() > 0) {
				createAttribute(element.getAttributes(), mainElement);
			}

			if (element.getChildren().size() > 0) {
				for (DataElement childDataElement : element.getChildren()) {
					SOAPElement aChildElement = createElement(childDataElement,
							mainElement);
					if (childDataElement.getAttributes().size() > 0) {
						createAttribute(childDataElement.getAttributes(),
								aChildElement);
					}
				}
			}

		} catch (SOAPException e) {
			e.printStackTrace();
		}
		return mainElement;
	}

	private void createAttribute(List<DataAttribute> attributes,
			SOAPElement element) {
		for (DataAttribute attribute : attributes) {
			try {
				element.addAttribute(
						new QName(NAMESPACE_URL, attribute.getName(),
								NAMESPACE_PREFIX), attribute.getValue());
			} catch (SOAPException e) {
				e.printStackTrace();
			}
		}
	}

	private void createSOAPHeader(SOAPHeader soapHeader) throws SOAPException {
		QName security = soapHeader.createQName("Security",
				HEADER_SECURITY_NS_PREFIX);
		SOAPHeaderElement headerElement = soapHeader.addHeaderElement(security);
		SOAPElement usernameToken = headerElement.addChildElement(
				"UsernameToken", HEADER_SECURITY_NS_PREFIX);
		SOAPElement username = usernameToken.addChildElement("Username",
				HEADER_SECURITY_NS_PREFIX);
		username.addTextNode(tenant.getTenantUser() + "@"
				+ tenant.getTenantName());
		SOAPElement password = usernameToken.addChildElement("Password",
				HEADER_SECURITY_NS_PREFIX);
		password.addTextNode(tenant.getTenantUserPassword());
		password.addAttribute(new QName("Type"), PASSWORD_TYPE_ATTR_VALUE);
	}

}
