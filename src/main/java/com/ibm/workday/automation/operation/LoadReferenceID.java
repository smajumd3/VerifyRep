package com.ibm.workday.automation.operation;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.workday.automation.common.CommonConstants;
import com.ibm.workday.automation.model.File;
import com.ibm.workday.automation.model.Tenant;
import com.ibm.workday.automation.service.FileService;

public class LoadReferenceID implements CommonConstants {

	private static final String REFERENCE_ID_TYPE_ELEMENT = "Reference_ID_Type";
//	private static final String REFERENCE_ID_WS_REQUEST_FILE = "config/reference-id-ws-request.xml";
	private static final String COMPARE_REF_ID_NODE_NAME = "wd:Reference_ID_Data";
	private static final String COMPARE_REF_ID_VALUE = "wd:ID";
	private static final String APPLICATION_NAME = "Integrations";
	private static final String ELEMENT_NAME_PAGE = "Page";
	private static final String ELEMENT_RESPONSE_RESULTS = "wd:Response_Results";
	private static final String ELEMENT_TOTAL_PAGES = "wd:Total_Pages";
	private DataElement ruleRoot;
	private Tenant tenant;

	private Set<String> referenceIdTypes;
	private Set<String> exclusionIdTypes;

	private SOAPRequestBuilder soapRequestBuilder;

	private DataElement requestElement;

	private SOAPConnection soapConnection;
	private String endPointURL;
	
	LoadDataRules loadDataRules;

	public LoadReferenceID(LoadDataRules loadDataRules, DataElement ruleRoot,
			               Tenant tenant,
			               Set<String> exclusionIdTypes, String xmlStr) {
		super();
		this.loadDataRules = loadDataRules;
		this.ruleRoot = ruleRoot;
		this.tenant = tenant;
		this.exclusionIdTypes = exclusionIdTypes;

		initialize(xmlStr);
	}

	private void initialize(String xmlStr) {
		requestElement = loadDataRules.getDataRulesFromString(xmlStr);
//		requestElement = loadDataRules.getDataRulesFromString(REFERENCE_ID_WS_REQUEST);			//Suman
		referenceIdTypes = new HashSet<String>();
		String url = SERVICE_URL_PROTOCOL + tenant.getTenantUrl();
		if (!url.endsWith("/")) {
			url = url + "/";
		}
		this.endPointURL = url + APPLICATION_NAME;
		prepareRefIdTypes(ruleRoot);
		try {
			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory
					.newInstance();
			soapConnection = soapConnectionFactory.createConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void prepareRefIdTypes(DataElement element) {
		for (DataAttribute da : element.getAttributes()) {
			if (da.getName().equalsIgnoreCase(REFERENCE_ID_TYPE)) {
				if (!exclusionIdTypes.contains(da.getValue())) {
					referenceIdTypes.add(da.getValue());
				}
			}
		}
		if (!element.getChildren().isEmpty()) {
			for (DataElement de : element.getChildren()) {
				prepareRefIdTypes(de);
			}
		}
	}

	public Map<String, Set<String>> getReferenceIds() {
		System.out.println("Retrieving reference ids");
		Map<String, Set<String>> referenceIds = new HashMap<String, Set<String>>();
		soapRequestBuilder = new SOAPRequestBuilder(tenant);
		for (String refId : referenceIdTypes) {
			//System.out.println("Reference ID Value : " + refId);
			updateRefIdType(requestElement, refId);
			getElementByName(requestElement.getChildren(), ELEMENT_NAME_PAGE)
					.setValue("1");
			SOAPMessage soapMessage = soapRequestBuilder
					.buildSOAPRequest(requestElement);
			try {
				//soapMessage.writeTo(System.out);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Set<String> refIdSet = executeSOAP(soapMessage, refId);
			referenceIds.put(refId, refIdSet);

		}
		System.out.println("Done retrieving reference ids!");
		return referenceIds;
	}

	private boolean updateRefIdType(DataElement anElement, String refIdType) {
		boolean updated = false;
		if (REFERENCE_ID_TYPE_ELEMENT.equals(anElement.getName())) {
			anElement.setValue(refIdType);
			updated = true;
		} else {
			if (!anElement.getChildren().isEmpty()) {
				for (DataElement children : anElement.getChildren()) {
					updateRefIdType(children, refIdType);
				}
			}
		}
		return updated;
	}

	private Set<String> executeSOAP(SOAPMessage soapMessage, String refIdType) {
		Set<String> reponseSet = new HashSet<String>();
		try {
			SOAPMessage soapResponse = soapConnection.call(soapMessage,
					endPointURL);
			Document doc = soapResponse.getSOAPBody()
					.extractContentAsDocument();
			updateReferenceIdValues(doc.getChildNodes(), refIdType, reponseSet);
			System.out.println("Completed ref id type : " + refIdType);
			if (isNextPageRequired(doc.getChildNodes())) {
				System.out.println("Required to process next page");
				reponseSet.addAll(executeSOAP(
						soapRequestBuilder.buildSOAPRequest(requestElement),
						refIdType));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reponseSet;
	}

	private void updateReferenceIdValues(NodeList nodes, String refIdType,
			final Set<String> responseSet) {
		for (int i = 0; i < nodes.getLength(); i++) {
			Node topNode = nodes.item(i);
			if (topNode.getNodeType() == Node.ELEMENT_NODE) {
				if (topNode.getNodeName().equalsIgnoreCase(
						COMPARE_REF_ID_NODE_NAME)) {
					NodeList childNodes = topNode.getChildNodes();
					for (int j = 0; j < childNodes.getLength(); j++) {
						Node node = childNodes.item(j);
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							NodeList subChildNodes = node.getChildNodes();
							for (int k = 0; k < subChildNodes.getLength(); k++) {
								Node subChild = subChildNodes.item(k);
								if (subChild.getParentNode().getNodeName()
										.equalsIgnoreCase(COMPARE_REF_ID_VALUE)) {
									responseSet.add(subChild.getNodeValue());
									if (refIdType.equalsIgnoreCase("Location_ID")) {
										System.out.println("Location : " + subChild.getNodeValue());
									}
								}
							}
						}
					}

				} else {
					if (topNode.hasChildNodes()) {
						updateReferenceIdValues(topNode.getChildNodes(),
								refIdType, responseSet);
					}
				}
			}
		}
	}

	private boolean isNextPageRequired(NodeList nodes) {
		int currentPage = 0;
		int totalPages = 0;
		DataElement matcghingElement = getElementByName(
				requestElement.getChildren(), ELEMENT_NAME_PAGE);
		if (matcghingElement != null) {
			currentPage = Integer.parseInt(matcghingElement.getValue());
			for (int i = 0; i < nodes.getLength(); i++) {
				Node topNode = nodes.item(i);
				if (topNode.getNodeType() == Node.ELEMENT_NODE) {
					NodeList childNodes = topNode.getChildNodes();
					for (int j = 0; j < childNodes.getLength(); j++) {
						Node node = childNodes.item(j);
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							if (node.getNodeName().equals(
									ELEMENT_RESPONSE_RESULTS)) {
								NodeList subChildren = node.getChildNodes();
								for (int k = 0; k < subChildren.getLength(); k++) {
									Node subNode = subChildren.item(k);
									if (subNode.getNodeName().equals(
											ELEMENT_TOTAL_PAGES)) {
										String value = subNode.getFirstChild()
												.getNodeValue();
										if (value != null) {
											totalPages = Integer
													.parseInt(value);
										}
										break;
									}
								}
							}
						}
					}
				}
			}

			if (currentPage > 0 && currentPage < totalPages) {
				currentPage++;
				matcghingElement.setValue(String.valueOf(currentPage));
				return true;
			}
		}
		return false;
	}

	private DataElement getElementByName(List<DataElement> elements, String name) {
		DataElement anElement = null;
		for (DataElement element : elements) {
			if (element.getName().equals(name)) {
				anElement = element;
				break;
			} else if (!element.getChildren().isEmpty()) {
				anElement = getElementByName(element.getChildren(), name);
				if (anElement != null) {
					break;
				}
			}
		}
		return anElement;
	}

}
