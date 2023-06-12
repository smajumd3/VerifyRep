package com.ibm.workday.automation.operation;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.stereotype.Component;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Component
public class WriteDataRulesImpl implements WriteDataRules {

	private static final String ATTRIBUTE = "Attribute";
	private static final String WS_NAMESPACE = "urn:com.workday/bsvc";
	private static final String RULES_NAMESPACE = "com.ibm.conversion.tool/rules";
	private static final String WS_PREFIX = "wd";
	private static final String RULES_PREFIX = "ct";
	private static final String ATTRIBUTE_MAPPING_COLUMN_NAME = "mappingColumnName";
	private static final String ATTRIBUTE_CONSTANT_VALUE = "constantValue";
	
	private WriteDataRulesImpl() {

	}

	@Override
	public InputStream getDataRules(DataElement dataElement) {
		InputStream inputStream = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (updateResult(dataElement, baos)) {
			inputStream = new BufferedInputStream(new ByteArrayInputStream(
					baos.toByteArray()));
		}

		return inputStream;
	}

	@Override
	public boolean writeToFile(final DataElement dataElement, String fileName) {
		boolean result = false;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(fileName));
			result = updateResult(dataElement, fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	private boolean updateResult(final DataElement dataElement,
			final OutputStream os) {
		boolean result = false;
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		docFactory.setNamespaceAware(true);
		try {
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			Document document = docBuilder.newDocument();
			Element rootElement = document.createElementNS(WS_NAMESPACE,
					dataElement.getName());
			rootElement.setPrefix(WS_PREFIX);
			updateAttributes(dataElement.getAttributes(), document, rootElement);
			createElement(dataElement.getChildren(), document, rootElement);

			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource dom = new DOMSource(rootElement);
			StreamResult streamResult = new StreamResult(os);
			transformer.transform(dom, streamResult);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private Element createElement(final List<DataElement> elements,
			Document document, final Element parentElement) {
		Element anElement = null;

		for (DataElement dataElement : elements) {

			Element newElement = document.createElementNS(WS_NAMESPACE,
					dataElement.getName());
			newElement.setPrefix(WS_PREFIX);
			/*
			 * if (dataElement.getValue() != null &&
			 * dataElement.getValue().trim().length() > 0) {
			 * newElement.appendChild(document.createTextNode(dataElement
			 * .getValue())); }
			 */
			if (!dataElement.getAttributes().isEmpty()) {
				updateAttributes(dataElement.getAttributes(), document,
						newElement);
			}
			if (!dataElement.getChildren().isEmpty()) {
				createElement(dataElement.getChildren(), document, newElement);
			}
			parentElement.appendChild(newElement);
		}
		return anElement;
	}

	private void updateAttributes(final List<DataAttribute> dataAttributes,
			final Document document, final Element parentElement) {
		for (DataAttribute dataAttribute : dataAttributes) {
			String namespace = "";
			String prefix = "";
			boolean isAttribute = false;
			if (dataAttribute.getType().equalsIgnoreCase(ATTRIBUTE)) {
				namespace = WS_NAMESPACE;
				prefix = WS_PREFIX;
			} else {
				namespace = RULES_NAMESPACE;
				prefix = RULES_PREFIX;
			}

			if (dataAttribute.getName().equalsIgnoreCase(
					ATTRIBUTE_MAPPING_COLUMN_NAME)
					|| dataAttribute.getName().equalsIgnoreCase(
							ATTRIBUTE_CONSTANT_VALUE)) {
				isAttribute = false;
			} else {
				isAttribute = true;
			}
			if (isAttribute) {
				Attr attr = document.createAttributeNS(namespace,
						dataAttribute.getName());
				attr.setPrefix(prefix);
				attr.setValue(dataAttribute.getValue());
				parentElement.setAttributeNode(attr);
			} else {
				String value = dataAttribute.getValue();
				if (ATTRIBUTE_MAPPING_COLUMN_NAME.equalsIgnoreCase(dataAttribute
						.getName())) {
					if (!dataAttribute.getValue().startsWith("$")) {
						value = "$" + value;
					}
				}
				parentElement.appendChild(document.createTextNode(value));
			}
		}
	}

}
