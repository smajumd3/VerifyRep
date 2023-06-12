package com.ibm.workday.automation.operation;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ibm.workday.automation.model.MapFile;
import com.ibm.workday.automation.model.Operation;
import com.ibm.workday.automation.service.MapFileService;

@Component
public class LoadDataRulesImpl implements LoadDataRules {

	private static final String OPERATOR_EQUAL_TO = "=";
	private static final String CONDITION_OR = "OR";
	private static final String RULES_NAMESPACE = "com.ibm.conversion.tool/rules";
	private static final String RULE_FILE_NAME = "fileName";
	private static final String RULE_MULTIPLE = "multiple";
	private static final String RULE_UNIQUE_ID = "uniqueID";
	private static final String RULE_REFERENCE_ID = "refId";
	private static final String RULE_REQUIRED = "required";
	private static final String RULE_FIELD_LENGTH = "fieldLength";
	private static final String RULE_FORMAT_DATE = "formatDate";
	private static final String RULE_AUTO_FORMAT_DATE = "autoFormatDate";
	private static final String RULE_MAP_TO_BOOLEAN = "mapToBoolean";
	private static final String RULE_NUMERIC_ONLY = "numericOnly";
	private static final String RULE_EMAIL = "email";
	private static final String RULE_PHONE_NUMBER = "phoneNumber";
	private static final String RULE_MAPPED_VALUE = "mappedValue";
	private static final String RULE_PREPEND_CHAR = "prependChar";
	private static final String RULE_APPEND_CHAR = "appendChar";
	private static final String RULE_ROW_ID_COLUMN_NAME = "rowIdColumnName";
	private static final String RULE_SKIP_FIRST_ROWS = "skipRows";
	private static final String RULE_HEADER_ROW_NUMBER = "headerRowNumber";
	private static final String RULE_FILE_TYPE = "fileType";
	private static final String RULE_CONDITION = "condition";
	private static final String RULE_INSPECT_FROM_CURRENT_ROW = "inspectfromCurrentRow";
	private static final String RULE_MINIMUM_NON_EMPTY_CHILDREN = "minimumNonEmptyChildren";
	private static final String RULE_STRIP_OFF_BEYOND_MAX_LENGTH = "stripOffBeyondMaxLength";
	private static final String RULE_CONVERT_TO_BAS64_FORMAT = "convertToBase64";
	private static final String RULE_CONVERT_TO_UPPER_CASE = "convertToUpperCase";
	private static final String RULE_CONVERT_TO_LOWER_CASE = "convertToLowerCase";
	private static final String RULE_NUMBER_FORMAT = "numberFormat";
	private static final String RULE_STRIP_OFF_BEFORE_CHAR = "stripOffBeforeChar";
	private static final String RULE_CHECK_FILE_EXISTENCE = "checkFileExistence";

	private AtomicInteger automicInt = new AtomicInteger(0);
	
	@Autowired
	MapFileService mapFileService;
	
	@Autowired
	LoadCSVFile loadCsvFile;
	
	@Autowired
	LoadExcelFile loadExcelFile;

	private LoadDataRulesImpl() {

	}
	
	@Override
	public DataElement getDataRulesFromString(String stringValue) {

		DataElement rootElement = null;
		Reader aReader = null;
		try {
			aReader = new StringReader(stringValue);
			rootElement = getDataRules(aReader);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (aReader != null) {
				try {
					aReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return rootElement;
	}

	@Override
	public DataElement getDataRules(String ruleFileName) {

		DataElement rootElement = null;
		Reader aReader = null;
		try {
			aReader = new FileReader(ruleFileName);
			rootElement = getDataRules(aReader);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (aReader != null) {
				try {
					aReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return rootElement;
	}

	@Override
	public DataElement getDataRules(Reader reader) {

		return getDataRules(reader, false);

	}

	@Override
	public DataElement getDataRules(Reader reader, boolean asInFile) {

		DataElement rootElement = null;
		try {
			if (reader != null) {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory
						.newInstance();
				dbFactory.setNamespaceAware(true);
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(new InputSource(reader));
				if (asInFile) {
					rootElement = addNodeAsInFile(doc.getChildNodes(), null);
				} else {
					rootElement = addNodes(doc.getChildNodes(), null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rootElement;

	}
	
	@Override
	public DataElement getDataRules(Operation operation, boolean asInFile) {
		DataElement rootElement = null;
	    DocumentBuilder dBuilder = null;
		
		byte[] fileContent = readRuleFileContents(operation);
		
		if(fileContent != null) {
		    InputStream inStream = new ByteArrayInputStream(fileContent);
		    InputSource insource = new InputSource(inStream);
		
		    DocumentBuilderFactory dbFactory = DocumentBuilderFactory
				.newInstance();
		    dbFactory.setNamespaceAware(true);
		    
		    try {
			    dBuilder = dbFactory.newDocumentBuilder();
		    } catch (ParserConfigurationException e) {
			    e.printStackTrace();
		    }
		    try {
			    Document doc = dBuilder.parse(insource);
			    if (asInFile) {
				    rootElement = addNodeAsInFile(doc.getChildNodes(), null);
			    } else {
				    rootElement = addNodes(doc.getChildNodes(), null);
			    }
		    } catch (SAXException | IOException e) {
			    e.printStackTrace();
		    }
		}
		
		return rootElement;
	}

	@Override
	public byte[] readRuleFileContents(Operation operation) {
		return operation.getRuleFileData();
	}

	private DataElement addNodes(NodeList nodes, DataElement rootElement) {

		DataElement rootDataElement = null;
		for (int i = 0; i < nodes.getLength(); i++) {
			Node aNode = nodes.item(i);
			if (aNode.getNodeType() == Node.ELEMENT_NODE) {
				String aNodeName = aNode.getNodeName();
				int index = aNodeName.indexOf(":");
				index = index >= 0 ? index + 1 : 0;
				String columnName = aNodeName.substring(index);
				columnName = columnName.replaceAll("\n", "").replaceAll("\t",
						"");
				DataElement aDataElement = new DataElement(rootElement,
						columnName, "");
				// aDataElement.setId(automicInt.incrementAndGet());
				NamedNodeMap nodeMap = aNode.getAttributes();
				if (nodeMap != null) {
					for (int j = 0; j < nodeMap.getLength(); j++) {
						Node attrNode = nodeMap.item(j);
						String attrName = attrNode.getNodeName();
						int attrNSPrefixIndex = attrName.indexOf(":");
						attrNSPrefixIndex = attrNSPrefixIndex >= 0 ? attrNSPrefixIndex + 1
								: 0;
						if (RULES_NAMESPACE.equals(attrNode.getNamespaceURI())) {
							DataRule dataRule = aDataElement.getRule();
							if (dataRule == null) {
								dataRule = new DataRule();
								aDataElement.setRule(dataRule);
							}
							String attrValue = attrNode.getNodeValue();
							String ruleName = attrName
									.substring(attrNSPrefixIndex);
							if (RULE_FILE_NAME.equalsIgnoreCase(ruleName)) {
								dataRule.setFileName(attrValue);
							} else if (RULE_UNIQUE_ID
									.equalsIgnoreCase(ruleName)) {
								dataRule.setUniqueID(attrValue);
							} else if (RULE_REFERENCE_ID
									.equalsIgnoreCase(ruleName)) {
								dataRule.setRefID(attrValue);
							} else if (RULE_MULTIPLE.equalsIgnoreCase(ruleName)) {
								dataRule.setMultiple(Boolean
										.parseBoolean(attrValue));
							} else if (RULE_REQUIRED.equalsIgnoreCase(ruleName)) {
								dataRule.setRequired(Boolean
										.parseBoolean(attrValue));
							} else if (RULE_FORMAT_DATE
									.equalsIgnoreCase(ruleName)) {
								dataRule.setFormatDate(attrValue);
							} else if (RULE_AUTO_FORMAT_DATE
									.equalsIgnoreCase(ruleName)) {
								dataRule.setAutoFormatDate(new Boolean(
										attrValue));
							} else if (RULE_MAP_TO_BOOLEAN
									.equalsIgnoreCase(ruleName)) {
								dataRule.setMapToBoolean(new Boolean(attrValue));
							} else if (RULE_FIELD_LENGTH
									.equalsIgnoreCase(ruleName)) {
								if (attrValue != null && !attrValue.isEmpty()) {
									try {
										dataRule.setFieldLength(Integer
												.parseInt(attrValue));
									} catch (NumberFormatException e) {
									}
								}
							} else if (RULE_NUMERIC_ONLY
									.equalsIgnoreCase(ruleName)) {
								if (attrValue != null && !attrValue.isEmpty()) {
									dataRule.setNumericOnly(Boolean
											.parseBoolean(attrValue));
								}
							} else if (RULE_MAPPED_VALUE
									.equalsIgnoreCase(ruleName)) {
								dataRule.setMappedValue(Boolean
										.parseBoolean(attrValue));
							} else if (RULE_EMAIL.equalsIgnoreCase(ruleName)) {
								dataRule.setEmail(Boolean
										.parseBoolean(attrValue));
							} else if (RULE_PHONE_NUMBER
									.equalsIgnoreCase(ruleName)) {
								dataRule.setPhoneNumber(Boolean
										.parseBoolean(attrValue));
							} else if (RULE_PREPEND_CHAR
									.equalsIgnoreCase(ruleName)) {
								dataRule.setPrependChar(attrValue);
							} else if (RULE_ROW_ID_COLUMN_NAME
									.equalsIgnoreCase(ruleName)) {
								dataRule.setRowIdColumnName(attrValue);
							} else if (RULE_SKIP_FIRST_ROWS
									.equalsIgnoreCase(ruleName)) {
								try {
									dataRule.setSkipRows(Integer
											.parseInt(attrValue));
								} catch (NumberFormatException e) {

								}
							} else if (RULE_HEADER_ROW_NUMBER
									.equalsIgnoreCase(ruleName)) {
								try {
									dataRule.setHeaderRowNumber(Integer
											.parseInt(attrValue));
								} catch (NumberFormatException e) {

								}
							} else if (RULE_FILE_TYPE
									.equalsIgnoreCase(ruleName)) {
								dataRule.setFileType(attrValue);
							} else if (RULE_CONDITION
									.equalsIgnoreCase(ruleName)) {
								dataRule.setCondition(getCondition(attrValue));
							} else if (RULE_INSPECT_FROM_CURRENT_ROW
									.equalsIgnoreCase(ruleName)) {
								try {
									dataRule.setInpsectFromCurrentRow(Integer
											.parseInt(attrValue));
								} catch (NumberFormatException ne) {
									ne.printStackTrace();
								}
							} else if (RULE_MINIMUM_NON_EMPTY_CHILDREN
									.equalsIgnoreCase(ruleName)) {
								try {
									dataRule.setMinimumNonEmptyChildren(Integer
											.parseInt(attrValue));
								} catch (NumberFormatException e) {
									e.printStackTrace();
								}
							} else if (RULE_STRIP_OFF_BEYOND_MAX_LENGTH
									.equalsIgnoreCase(ruleName)) {
								dataRule.setStripOffBeyondMaxLength(Boolean
										.parseBoolean(attrValue));
							} else if (RULE_CONVERT_TO_BAS64_FORMAT
									.equalsIgnoreCase(ruleName)) {
								dataRule.setConvertToBase64(Boolean
										.parseBoolean(attrValue));
							} else if (RULE_CONVERT_TO_UPPER_CASE
									.equalsIgnoreCase(ruleName)) {
								dataRule.setConvertToUpperCase(Boolean
										.parseBoolean(attrValue));
							} else if (RULE_CONVERT_TO_LOWER_CASE
									.equalsIgnoreCase(ruleName)) {
								dataRule.setConvertToLowerCase(Boolean
										.parseBoolean(attrValue));
							} else if (RULE_NUMBER_FORMAT
									.equalsIgnoreCase(ruleName)) {
								dataRule.setNumberFormat(attrValue);
							} else if (RULE_STRIP_OFF_BEFORE_CHAR
									.equalsIgnoreCase(ruleName)) {
								dataRule.setStripOffBeforeChar(attrValue);
							} else if (RULE_APPEND_CHAR
									.equalsIgnoreCase(ruleName)) {
								dataRule.setAppendChar(attrValue);
							} else if (RULE_CHECK_FILE_EXISTENCE
									.equalsIgnoreCase(ruleName)) {
								dataRule.setCheckFileExistence(Boolean
										.parseBoolean(attrValue));
							}
							if (aDataElement.getValue() != null
									&& aDataElement.getValue().startsWith("$")) {
								dataRule.setColumnName(columnName);
							}

						} else if (attrNode.getPrefix().equalsIgnoreCase(
								"XMLNS")) {
							// do nothing
						} else {
							aDataElement.addAttribute(new DataAttribute(
									attrName.substring(attrNSPrefixIndex),
									attrNode.getNodeValue()));
						}
					}
				}
				if (aDataElement.getRule() == null) {
					DataRule aNewRule = new DataRule();
					DataRule existingRule = rootElement.getRule();
					aNewRule.setUniqueID(existingRule.getUniqueID());
					aNewRule.setFileName(existingRule.getFileName());

					aDataElement.setRule(aNewRule);
				} else {
					DataRule dataRule = aDataElement.getRule();
					if (rootElement != null) {
						if (dataRule.getFileName() == null) {
							dataRule.setFileName(rootElement.getRule()
									.getFileName());
						}
						if (dataRule.getUniqueID() == null) {
							dataRule.setUniqueID(rootElement.getRule()
									.getUniqueID());
						}
						if (dataRule.getCondition() == null) {
							dataRule.setCondition(rootElement.getRule()
									.getCondition());
						}
					}
				}
				if (aNode.hasChildNodes()) {
					NodeList childNodes = aNode.getChildNodes();
					for (int j = 0; j < childNodes.getLength(); j++) {
						Node aChildNode = childNodes.item(j);
						if (aChildNode.getNodeType() == Node.TEXT_NODE) {
							aDataElement.setValue(aChildNode.getTextContent());
							if (aDataElement.getValue() != null
									&& aDataElement.getValue().startsWith("$")) {
								aDataElement.getRule().setColumnName(
										aDataElement.getValue());
							}
						}
					}
					addNodes(childNodes, aDataElement);
				}

				if (rootElement == null) {
					rootDataElement = aDataElement;
				}
			}
		}
		return rootDataElement;
	}

	private DataElement addNodeAsInFile(NodeList nodes, DataElement rootElement) {
		DataElement rootDataElement = null;
		for (int i = 0; i < nodes.getLength(); i++) {
			Node aNode = nodes.item(i);
			if (aNode.getNodeType() == Node.ELEMENT_NODE) {
				String aNodeName = aNode.getNodeName();
				int index = aNodeName.indexOf(":");
				index = index >= 0 ? index + 1 : 0;
				String columnName = aNodeName.substring(index);
				columnName = columnName.replaceAll("\n", "").replaceAll("\t",
						"");
				DataElement aDataElement = new DataElement(rootElement,
						columnName, "");
				aDataElement.setId(automicInt.incrementAndGet());
				NamedNodeMap nodeMap = aNode.getAttributes();
				if (nodeMap != null) {
					for (int j = 0; j < nodeMap.getLength(); j++) {
						Node attrNode = nodeMap.item(j);
						String attrName = attrNode.getNodeName();
						int attrNSPrefixIndex = attrName.indexOf(":");
						attrNSPrefixIndex = attrNSPrefixIndex >= 0 ? attrNSPrefixIndex + 1
								: 0;

						if (!attrNode.getPrefix().equalsIgnoreCase("XMLNS")) {
							String type = "Attribute";
							if (RULES_NAMESPACE.equals(attrNode
									.getNamespaceURI())) {
								type = "Rule";
							}
							aDataElement.addAttribute(new DataAttribute(
									attrName.substring(attrNSPrefixIndex),
									attrNode.getNodeValue(), type, null));
						}

					}
				}
				if (aNode.hasChildNodes()) {
					NodeList childNodes = aNode.getChildNodes();
					for (int j = 0; j < childNodes.getLength(); j++) {
						Node aChildNode = childNodes.item(j);
						if (aChildNode.getNodeType() == Node.TEXT_NODE) {
							aDataElement.setValue(aChildNode.getTextContent()
									.trim());
							if (aDataElement.getValue() != null) {
								if (aDataElement.getValue().startsWith("$")) {
									DataAttribute dataAttribute = new DataAttribute(
											"mappingColumnName", aDataElement
													.getValue().substring(1),
											"Rule", "");
									aDataElement.addAttribute(dataAttribute);
								} else {
									if (aDataElement.getValue().length() > 0) {
										DataAttribute dataAttribute = new DataAttribute(
												"constantValue",
												aDataElement.getValue(),
												"Rule", null);
										aDataElement
												.addAttribute(dataAttribute);
									}
								}
							}
						}
					}
					addNodeAsInFile(childNodes, aDataElement);
				}

				if (rootElement == null) {
					rootDataElement = aDataElement;
				}
			}
		}
		return rootDataElement;
	}

	private Condition getCondition(String value) {
		Condition condition = new Condition();
		String[] values = value.split(OPERATOR_EQUAL_TO);
		if (values.length > 1) {
			condition.setCheckFrom(values[0].trim());
			condition.setOperator(OPERATOR_EQUAL_TO);
			String[] condValues = values[1].split(CONDITION_OR);
			for (int i = 0; i < condValues.length; i++) {
				condition.getValues().add(condValues[i].trim());
			}
		}
		return condition;
	}

	@Override
	public Map<String, FileData> getFileDataMap(Operation operation) {
		return getFileDataMap(operation, "");
	}
	
	@Override
	public Map<String, InputStream> getFileContentMap(Operation operation) {
		List<MapFile> mapFileList = mapFileService.getMapFileListByOeration(operation.getOperationId());
		Map<String, InputStream> importContents = new HashMap<>();
		
		if(mapFileList != null) {
			for(MapFile mapFile : mapFileList ) {
				InputStream in = new ByteArrayInputStream(mapFile.getMapFileData());
				InputStream iStream = new BufferedInputStream(in);
				
				if(iStream != null) {
					importContents.put(mapFile.getFileName(), iStream);
				}
			}
		}
		
		return importContents;
	}

	@Override
	public Map<String, FileData> getFileDataMap(Operation operation, String rowIdColName) {
		Map<String, FileData> allFileData = new HashMap<>();
		Map<String, InputStream> importContents = getFileContentMap(operation);
		
		if (importContents != null && importContents.size() > 0) {
			for (String fileName : importContents.keySet()) {
				Reader fileData = new InputStreamReader(importContents.get(fileName));
				FileData aFileData = new FileData();
				aFileData.setName(fileName);
				if (rowIdColName == null || rowIdColName.isEmpty()) {
					aFileData.setData(loadCsvFile.getData(fileData));
				} else {
					aFileData.setContents(loadCsvFile.getData(fileData, rowIdColName));
				}
				allFileData.put(fileName, aFileData);
			}
		}
		return allFileData;
	}

	@Override
	public Map<String, FileData> getFileDataMap(Operation operation, DataRule dataRule) {
		Map<String, FileData> allFileData = new HashMap<>();
		Map<String, InputStream> importContents = getFileContentMap(operation);
		
		if (importContents != null && importContents.size() > 0) {
			for (String fileName : importContents.keySet()) {
				InputStream fileData = importContents.get(fileName);
				FileData aFileData = new FileData();
				aFileData.setName(fileName);
				if (dataRule.getRowIdColumnName() == null
						|| dataRule.getRowIdColumnName().isEmpty()) {
					if ("excel".equalsIgnoreCase(dataRule.getFileType())) {
						aFileData.setContents(loadExcelFile.readData(dataRule, fileData));
					} else {
						Reader aReader;
						try {
							aReader = new InputStreamReader(fileData, Charset.forName("UTF-8"));
							aFileData.setData(loadCsvFile.getData(aReader));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} else {
					if ("CSV".equalsIgnoreCase(dataRule.getFileType())) {
						Reader aReader = new InputStreamReader(fileData);
						aFileData.setContents(loadCsvFile.getData(aReader, dataRule.getRowIdColumnName()));
					} else {
						aFileData.setContents(loadExcelFile.readData(dataRule, fileData));
					}

				}
				allFileData.put(fileName, aFileData);
				try {
					if(fileData != null) {
						fileData.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return allFileData;
	}

}
