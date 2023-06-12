package com.ibm.workday.automation.operation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

//import com.ibm.wd.conversion.tool.dao.transfer.MappingConfigDO;

public class ProcessRules {
	private static final String XML_DATE_FORMAT = "yyyy-MM-dd";

	private static final String AUTO_GENERATED_COL_PREFIX = "Col-";

	private static final String FILE_TYPE_EXCEL = "EXCEL";

	private static final Map<String, String> MONTHS_MAP = new HashMap<>();

//	private List<MappingConfigDO> mappingConfigData;

	private Map<String, FileData> data;

	private String type;

	private boolean ignoreEmptyValue;

	private boolean attachRule;

	private String filterId;

	private Map<String, Map<String, List<ColumnData[]>>> cacheData = new HashMap<>();

	private SimpleDateFormat wsFormatDate;

	static {
		MONTHS_MAP.put("Jan", "01");
		MONTHS_MAP.put("Feb", "02");
		MONTHS_MAP.put("Mar", "03");
		MONTHS_MAP.put("Apr", "04");
		MONTHS_MAP.put("May", "05");
		MONTHS_MAP.put("Jun", "06");
		MONTHS_MAP.put("Jul", "07");
		MONTHS_MAP.put("Aug", "08");
		MONTHS_MAP.put("Sep", "09");
		MONTHS_MAP.put("Oct", "10");
		MONTHS_MAP.put("Nov", "11");
		MONTHS_MAP.put("Dec", "12");
	}

	public ProcessRules(final Map<String, FileData> data, String type) {
		this(data, type, false, false);
	}

	public ProcessRules(final Map<String, FileData> data, String type,
			            String filterId) {
		this(data, type, false, false);
		this.filterId = filterId;
	}

	public ProcessRules(Map<String, FileData> data, String type,
			            boolean attachRule, boolean ignoreEmptyValue) {
		super();
		this.data = data;
		this.type = type;
		this.attachRule = attachRule;
		this.ignoreEmptyValue = ignoreEmptyValue;

		wsFormatDate = new SimpleDateFormat(XML_DATE_FORMAT);
	}

	/**
	 * 
	 * @param rulesRoot
	 * @return
	 */
	public DataElement populateElement(DataElement rulesRoot) {

		DataElement aRoot = cloneObject(rulesRoot, null);
		aRoot.setName(rulesRoot.getName());
		if (attachRule) {
			aRoot.setRule(rulesRoot.getRule());
		}
		for (DataAttribute anAttribute : rulesRoot.getAttributes()) {
			aRoot.addAttribute(new DataAttribute(anAttribute.getName(),
					anAttribute.getValue()));
		}

		buildCache(rulesRoot.getRule().getUniqueID());

		populateChildren(rulesRoot.getChildren(), aRoot, "1", 0);

		return aRoot;
	}

	/**
	 * 
	 * @param children
	 * @param aRoot
	 * @param uniqueValue
	 */
	private void populateChildren(List<DataElement> children,
			DataElement aRoot, String uniqueValue, int currentIndex) {
		for (DataElement aDataElement : children) {
			String fileName = aDataElement.getRule().getFileName();
			// System.out.println("Using file - " + fileName);
			String columnName = aDataElement.getRule().getColumnName() != null ? aDataElement
					.getRule().getColumnName() : aDataElement.getValue();
			String uniqueId = aDataElement.getRule().getUniqueID();
			List<ColumnData[]> matchingData = cacheData.get(fileName).get(
					uniqueValue);
			if (matchingData != null) {
				if (aDataElement.getRule().isMultiple()) {
					int index = 0;
					for (ColumnData[] aColData : matchingData) {
						String identifier = getValueByColumnName(aColData,
								uniqueId, aDataElement);
						if (identifier != null) {
							DataElement newData = new DataElement();
							newData.setName(aDataElement.getName());
							newData.setIdentifierValue(aRoot
									.getIdentifierValue());
							populateAttributeValue(aDataElement, newData,
									aColData);
							if (attachRule) {
								newData.setRule(aDataElement.getRule());
							}
							if (!aDataElement.getChildren().isEmpty()) {
								populateChildren(aDataElement.getChildren(),
										newData, identifier, index);
								if (!isEmptyChild(newData)) {
									aRoot.addChild(newData);
								}
							} else {
								String value = getValueByColumnName(aColData,
										aDataElement.getValue(), aDataElement);
								newData.setValue(value);
								if (value != null && !value.trim().isEmpty()) {
									aRoot.addChild(newData);
								}
							}
						}
						index++;
					}
				} else {
					if (matchingData.size() > 0
							&& currentIndex < matchingData.size()) {
						ColumnData[] someColData = matchingData
								.get(currentIndex);
						String identifier = getValueByColumnName(someColData,
								uniqueId, aDataElement);
						DataElement newData = new DataElement();
						newData.setIdentifierValue(aRoot.getIdentifierValue());
						if (attachRule) {
							newData.setRule(aDataElement.getRule());
						}
						populateElementValue(aRoot, aDataElement, newData,
								someColData, columnName);
						if (!aDataElement.getChildren().isEmpty()) {
							populateChildren(aDataElement.getChildren(),
									newData, identifier, currentIndex);
						}
						if (!isEmptyChild(newData)) {
							boolean isAdd = true;
							if (aDataElement.getRule()
									.getMinimumNonEmptyChildren() > 0) {
								isAdd = isMinimumChildElementsExists(newData,
										aDataElement.getRule()
												.getMinimumNonEmptyChildren());
							}
							if (isAdd) {
								aRoot.addChild(newData);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param children
	 * @param aRoot
	 * @param uniqueValue
	 * @param matchingData
	 * @param rowIndex
	 */
	private void populateChildren(final List<DataElement> children,
			DataElement aRoot, String uniqueValue,
			final List<ColumnData[]> matchingData, int rowIndex) {
		for (DataElement aDataElement : children) {
			String columnName = aDataElement.getRule().getColumnName() != null ? aDataElement
					.getRule().getColumnName() : aDataElement.getValue();
			if (aDataElement.getRule().isMultiple()) {
				int index = 0;
				for (ColumnData[] aColData : matchingData) {
					DataElement newData = new DataElement();
					newData.setName(aDataElement.getName());
					newData.setIdentifierValue(uniqueValue);
					populateAttributeValue(aDataElement, newData, aColData);
					if (attachRule) {
						newData.setRule(aDataElement.getRule());
					}
					if (!aDataElement.getChildren().isEmpty()) {
						populateChildren(aDataElement.getChildren(), newData,
								uniqueValue, matchingData, index);
						if (!isEmptyChild(newData)) {
							aRoot.addChild(newData);
						}
					} else {
						String value = getValueByColumnName(aColData,
								aDataElement.getValue(), aDataElement);
						newData.setValue(value);
						if (value != null && !value.trim().isEmpty()) {
							aRoot.addChild(newData);
						}
					}
					index++;
				}
			} else {
				if (rowIndex < matchingData.size()) {
					ColumnData[] someColData = matchingData.get(rowIndex);
					DataElement newData = new DataElement();
					newData.setIdentifierValue(uniqueValue);
					populateElementValue(aRoot, aDataElement, newData,
							someColData, columnName);

					// Find another way to fix this
					if (aDataElement.getRule().getCondition() != null
							&& (newData.getValue() == null || newData
									.getValue().equals(""))) {
						int inspectFromCurrentRow = aDataElement.getRule()
								.getInpsectFromCurrentRow();
						if (inspectFromCurrentRow > 0
								&& (rowIndex + inspectFromCurrentRow) < matchingData
										.size()) {
							populateElementValue(
									aRoot,
									aDataElement,
									newData,
									matchingData.get(rowIndex
											+ inspectFromCurrentRow),
									columnName);
						}
					}
					if (!aDataElement.getChildren().isEmpty()) {
						populateChildren(aDataElement.getChildren(), newData,
								uniqueValue, matchingData, rowIndex);
					}

					if (!isEmptyChild(newData)) {
						aRoot.addChild(newData);
					}
					if (attachRule) {
						newData.setRule(aDataElement.getRule());
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param source
	 * @param value
	 * @return
	 */
	private DataElement cloneObject(DataElement source, String value) {
		DataElement destination = new DataElement();
		destination.setName(source.getName());
		destination.setValue(value);

		return destination;
	}

	/**
	 * 
	 * @param singleRow
	 * @param colName
	 * @param aDataElement
	 * @return
	 */
	private String getValueByColumnName(ColumnData[] singleRow, String colName,
			DataElement aDataElement) {
		if (hasConditionMet(singleRow, aDataElement.getRule().getCondition())) {
			for (int i = 0; i < singleRow.length; i++) {
				ColumnData colData = singleRow[i];
				if (colData != null) {
					String colNamePrefixed = colName;
					if (FILE_TYPE_EXCEL.equalsIgnoreCase(type)
							&& colData.getName().startsWith(
									AUTO_GENERATED_COL_PREFIX)
							&& !colName.contains(AUTO_GENERATED_COL_PREFIX)) {
						colNamePrefixed = AUTO_GENERATED_COL_PREFIX
								+ colName.substring(1);
					} else {
						if (colName.startsWith("$")) {
							colNamePrefixed = colName.substring(1);
						}
					}
					if (colData.getName().equalsIgnoreCase(colNamePrefixed)) {
							return colData.getValue().trim();
					}
				}
			}
		}
		return null;
	}

	private boolean hasConditionMet(ColumnData[] singleRow, Condition condition) {
		if (condition == null) {
			return true;
		}
		boolean conditionMet = false;
		for (int i = 0; i < singleRow.length; i++) {
			ColumnData colData = singleRow[i];
			if (colData != null) {
				String colDataColName = colData.getName();
				String targetColName = condition.getCheckFrom().substring(1);
				if (FILE_TYPE_EXCEL.equalsIgnoreCase(type)) {
					if (!colDataColName.contains(AUTO_GENERATED_COL_PREFIX)) {
						colDataColName = AUTO_GENERATED_COL_PREFIX
								+ colDataColName;
					}

					if (!targetColName.contains(AUTO_GENERATED_COL_PREFIX)) {
						targetColName = AUTO_GENERATED_COL_PREFIX
								+ targetColName;
					}
				}
				if (colDataColName.equalsIgnoreCase(targetColName)) {
					if (condition.getValues().contains(colData.getValue())) {
						conditionMet = true;
						break;
					}
				}
			}
		}
		return conditionMet;
	}

	/**
	 * 
	 * @param singleRow
	 * @param colName
	 * @return
	 */
	private String getValueByColumnName(ColumnData[] singleRow, String colName) {
		for (int i = 0; i < singleRow.length; i++) {
			ColumnData colData = singleRow[i];
			if (colData != null) {
				String colNamePrefixed = colName;
				if (FILE_TYPE_EXCEL.equalsIgnoreCase(type)
						&& colData.getName().startsWith(
								AUTO_GENERATED_COL_PREFIX)
						&& !colName.contains(AUTO_GENERATED_COL_PREFIX)) {
					colNamePrefixed = AUTO_GENERATED_COL_PREFIX
							+ colName.substring(1);
				} else {
					if (colName.startsWith("$")) {
						colNamePrefixed = colName.substring(1).trim();
					}
				}
				if (colData.getName().trim().equalsIgnoreCase(colNamePrefixed)) {
					return colData.getValue().trim();
				}
			}
		}
		return null;
	}

/*	
	private String getMappedValue(String wdFieldName, String legacyFieldName,
			String legacyFieldValue) {
		String value = "";
		for (MappingConfigDO mapping : mappingConfigData) {
			if (mapping.getWorkdayField() != null
					&& mapping.getLegacyField() != null
					&& mapping.getWorkdayField().getName().equals(wdFieldName)
					&& mapping.getLegacyField().getName()
							.equals(legacyFieldName)
					&& mapping.getLegacyField().getValue()
							.equals(legacyFieldValue)) {
				value = mapping.getWorkdayField().getValue();
			}
		}
		return value;

	}
*/
	/**
	 * 
	 * @param ruleRoot
	 * @return
	 */
	public List<DataElement> populateElements(DataElement ruleRoot) {
		long startTime = System.currentTimeMillis();
		List<DataElement> rulesAll = new ArrayList<>();
		DataRule aRule = ruleRoot.getRule();
		if (aRule != null && aRule.isMultiple()) {
			String mainFile = aRule.getFileName();
			String uniqueIDCol = aRule.getUniqueID();
			if (mainFile != null && !mainFile.trim().equals("")
					&& uniqueIDCol != null && !uniqueIDCol.equals("")) {
				FileData fileData = data.get(mainFile);
				if (aRule.getRowIdColumnName() != null
						&& !aRule.getRowIdColumnName().trim().isEmpty()) {
					Map<Integer, List<ColumnData[]>> contents = fileData
							.getContents();
					for (Integer rowId : contents.keySet()) {
						List<ColumnData[]> rows = contents.get(rowId);
						if (rows.size() > 0) {
							ColumnData[] firstRow = rows.get(0);
							String uniqueIdVal = getValueByColumnName(firstRow,
									uniqueIDCol);
							DataElement aRootElement = new DataElement();
							aRootElement.setName(ruleRoot.getName());
							aRootElement.setIdentifierValue(uniqueIdVal);
							if (attachRule) {
								aRootElement.setRule(aRule);
							}
							for (DataAttribute anAttribute : ruleRoot
									.getAttributes()) {
								aRootElement.addAttribute(new DataAttribute(
										anAttribute.getName(), anAttribute
												.getValue()));
							}

							// Populate children here
							populateChildren(ruleRoot.getChildren(),
									aRootElement, uniqueIdVal, rows, 0);

							rulesAll.add(aRootElement);
						}
					}
				} else {
					buildCache(uniqueIDCol);
					boolean isFilterRequired = filterId != null
							&& !filterId.trim().equals("");
					List<ColumnData[]> rows = fileData.getData();
					for (ColumnData[] aColData : rows) {
						String value = getValueByColumnName(aColData,
								uniqueIDCol);
						if ((isFilterRequired && filterId
								.equalsIgnoreCase(value)) || !isFilterRequired) {
							DataElement aRootElement = new DataElement();
							aRootElement.setName(ruleRoot.getName());
							aRootElement.setIdentifierValue(value);
							for (DataAttribute anAttribute : ruleRoot
									.getAttributes()) {
								aRootElement.addAttribute(new DataAttribute(
										anAttribute.getName(), anAttribute
												.getValue()));
							}
							if (attachRule) {
								aRootElement.setRule(aRule);
							}
							populateChildren(ruleRoot.getChildren(),
									aRootElement, value, 0);
							rulesAll.add(aRootElement);
						}
					}
				}
			}
		}
		System.out.println("Total time to update data : "
				+ (System.currentTimeMillis() - startTime));
		return rulesAll;
	}

	/**
	 * 
	 * @param inputDate
	 * @param dateFormat
	 * @return
	 */
	private String getWSFormatDate(String inputDate, String dateFormat) {
		String wsDate = "";
		SimpleDateFormat inputFormat = new SimpleDateFormat(dateFormat);
		try {
			wsDate = wsFormatDate.format(inputFormat.parse(inputDate));
		} catch (ParseException e) {
		}
		return wsDate;
	}

	private String getNumberFormat(String number, String numberFormat) {
		DecimalFormat df = new DecimalFormat(numberFormat);
		return df.format(new Double(number).doubleValue());
	}

	/**
	 * 
	 * @param inputDate
	 * @return
	 */
	private String getAutoFormatDate(String inputDate) {
		StringBuilder formattedDate = new StringBuilder();
		if (inputDate != null) {
			String[] inputDateParts = inputDate.split("/");
			if (inputDateParts.length == 3) {
				String month = inputDateParts[0];
				String day = inputDateParts[1];
				String year = inputDateParts[2];
				String timePart = null;
				if (year.contains(" ")) {
					timePart = year.substring(year.indexOf(" "));
					year = year.substring(0, year.indexOf(" "));
				}
				formattedDate.append(year);
				formattedDate.append("-");
				try {
					int nMonth = Integer.parseInt(month);
					if (nMonth < 10) {
						formattedDate.append("0");
					}
					formattedDate.append(nMonth);
					formattedDate.append("-");
					int nDay = Integer.parseInt(day);
					if (nDay < 10) {
						formattedDate.append("0");
					}
					formattedDate.append(nDay);
					if (timePart != null) {
						// formattedDate.append(" ");
						// formattedDate.append(timePart.trim());
					}
				} catch (NumberFormatException ne) {
					formattedDate = new StringBuilder();
				}
			} else {
				String[] date = inputDate.split("-");
				if (date.length == 3) {
					formattedDate.append(date[2]);
					formattedDate.append("-");
					formattedDate.append(MONTHS_MAP.get(date[1]));
					formattedDate.append("-");
					formattedDate.append(date[0]);
				} else {
					formattedDate.append(inputDate);
				}
			}
		}
		return formattedDate.toString();
	}

	/**
	 * 
	 * @param anElement
	 * @return
	 */
	private boolean isEmptyChild(DataElement anElement) {
		if (ignoreEmptyValue)
			return false;
		boolean isEmptyChild = false;
		if (!anElement.getChildren().isEmpty()) {
			for (DataElement de : anElement.getChildren()) {
				if (!de.getChildren().isEmpty()) {
					isEmptyChild = isEmptyChild(de);
				} else {
					isEmptyChild = null == de.getValue()
							|| "".equalsIgnoreCase(de.getValue().trim());
				}
			}
		} else {
			isEmptyChild = null == anElement.getValue()
					|| "".equalsIgnoreCase(anElement.getValue().trim());
		}
		return isEmptyChild;
	}

	private boolean isMinimumChildElementsExists(DataElement anElement,
			int minCount) {
		int count = 0;
		if (!anElement.getChildren().isEmpty()) {
			for (DataElement de : anElement.getChildren()) {
				if (de.getChildren().isEmpty()) {
					if (!(null == de.getValue() || "".equalsIgnoreCase(de
							.getValue().trim()))) {
						count++;
					}
				}
			}
		}
		return minCount <= count;
	}

	/**
	 * 
	 * @param input
	 * @return
	 */
	private String getBooleanValue(String input) {
		String retVal = input;
		if ("n".equalsIgnoreCase(input) || "no".equalsIgnoreCase(input)) {
			retVal = "false";
		} else if ("y".equalsIgnoreCase(input) || "yes".equalsIgnoreCase(input)) {
			retVal = "true";
		}
		return retVal;
	}

	/**
	 * 
	 * @param value
	 * @param prependChar
	 * @param length
	 * @return
	 */
	private String getPrependString(String value, String prependChar, int length) {
		int currentFieldLength = length - value.length();
		String retValue = value;
		if (currentFieldLength > 0) {
			StringBuilder aBuilder = new StringBuilder();
			for (int i = 0; i < currentFieldLength; i++) {
				aBuilder.append(prependChar);
			}
			aBuilder.append(value);
			retValue = aBuilder.toString();
		}
		return retValue;
	}

	/**
	 * 
	 * @param value
	 * @param prependChar
	 * @return
	 */
	private String getPrependString(String value, String prependChar) {
		return prependChar + value;
	}

	/**
	 * 
	 * @param aDataElement
	 * @param newData
	 * @param aColData
	 */
	private void populateAttributeValue(final DataElement aDataElement,
			final DataElement newData, final ColumnData[] aColData) {
		for (DataAttribute aDataAttribute : aDataElement.getAttributes()) {
			if (aDataAttribute.getValue().startsWith("$")) {
				String someValue = getValueByColumnName(aColData,
						aDataAttribute.getValue(), aDataElement);
				if (aDataElement.getRule().isAutoFormatDate()) {
					someValue = getAutoFormatDate(someValue);
				} else if (aDataElement.getRule().isMapToBoolean()) {
					someValue = getBooleanValue(someValue);
				}
				newData.addAttribute(new DataAttribute(
						aDataAttribute.getName(), someValue));
			} else {
				newData.addAttribute(new DataAttribute(
						aDataAttribute.getName(), aDataAttribute.getValue()));
			}
		}
	}

	/***
	 * 
	 * @param aRoot
	 * @param aDataElement
	 * @param newData
	 * @param someColData
	 * @param columnName
	 */
	private void populateElementValue(final DataElement aRoot,
			final DataElement aDataElement, final DataElement newData,
			final ColumnData[] someColData, String columnName) {
		String colValue = "";
		if (columnName != null && columnName.startsWith("$")) {
			colValue = getValueByColumnName(someColData, columnName,
					aDataElement);
		} else {
			colValue = aDataElement.getValue();
		}

		// Replace the new line & tab characters in the string
		colValue = colValue != null ? colValue.replaceAll("\n", "").replaceAll(
				"\t", "") : "";
		if (aDataElement.getRule().getFormatDate() != null) {
			colValue = getWSFormatDate(colValue, aDataElement.getRule()
					.getFormatDate());
		} else if (aDataElement.getRule().isAutoFormatDate()) {
			colValue = getAutoFormatDate(colValue);
			//colValue = returnOrininalDateformat(colValue);
		} else if (aDataElement.getRule().isMapToBoolean()) {
			colValue = getBooleanValue(colValue);
		} else if (aDataElement.getRule().getNumberFormat() != null) {
			try {
				colValue = getNumberFormat(colValue, aDataElement.getRule()
						.getNumberFormat());
			} catch (Exception e) {
				System.err.println("Number format exception!");
				colValue = "";
			}
		}

		if (aDataElement.getRule().getPrependChar() != null
				&& !colValue.trim().equals("")) {
			if (aDataElement.getRule().getFieldLength() > 0) {
				colValue = getPrependString(colValue, aDataElement.getRule()
						.getPrependChar(), aDataElement.getRule()
						.getFieldLength());
			} else {
				colValue = getPrependString(colValue, aDataElement.getRule()
						.getPrependChar());
			}
		}

		if (aDataElement.getRule().getAppendChar() != null
				&& !colValue.trim().equals("")) {
			colValue = colValue + aDataElement.getRule().getAppendChar();
		}

		if (aDataElement.getRule().isStripOffBeyondMaxLength()
				&& aDataElement.getRule().getFieldLength() > 0
				&& colValue.length() > aDataElement.getRule().getFieldLength()) {
			colValue = colValue.substring(0, aDataElement.getRule()
					.getFieldLength());
		}

		if (aDataElement.getRule().getStripOffBeforeChar() != null
				&& !aDataElement.getRule().getStripOffBeforeChar().isEmpty()) {
			int index = colValue.indexOf(aDataElement.getRule()
					.getStripOffBeforeChar());
			if (index >= 0) {
				colValue = colValue.substring(index + 1).trim();
			}
		}
		if (aDataElement.getRule().isConvertToUpperCase()) {
			colValue = colValue.toUpperCase();
		}

		if (aDataElement.getRule().isConvertToLowerCase()) {
			colValue = colValue.toLowerCase();
		}

		if (aDataElement.getRule().isConvertToBase64() && !attachRule) {
			colValue = encodeFileToBase64Binary(colValue);
		}

		newData.setName(aDataElement.getName());
		newData.setFieldName(aDataElement.getValue());
		newData.setValue(colValue);

		for (DataAttribute aDataAttribute : aDataElement.getAttributes()) {
			if (aDataAttribute.getValue().startsWith("$")) {
				String someValue = getValueByColumnName(someColData,
						aDataAttribute.getValue(), aDataElement);
				if (aDataElement.getRule().isAutoFormatDate()) {
					someValue = getAutoFormatDate(someValue);
				} else if (aDataElement.getRule().isMapToBoolean()) {
					someValue = getBooleanValue(someValue);
				} else if (aDataElement.getRule().getFormatDate() != null) {
					try {
						someValue = getWSFormatDate(someValue, aDataElement
								.getRule().getFormatDate());
					} catch (Exception e) {
						// Do nothing
					}
				}
				newData.addAttribute(new DataAttribute(
						aDataAttribute.getName(), someValue));
			} else {
				newData.addAttribute(new DataAttribute(
						aDataAttribute.getName(), aDataAttribute.getValue()));
			}
		}
	}

	private void buildCache(String uniqueColumnName) {
		boolean isFilterRequired = filterId != null
				&& !filterId.trim().equals("");

		for (FileData aFileData : data.values()) {
			String fileName = aFileData.getName();
			Map<String, List<ColumnData[]>> fileData = new HashMap<>();
			List<ColumnData[]> rows = aFileData.getData();

			for (ColumnData[] columns : rows) {
				String value = getValueByColumnName(columns, uniqueColumnName);
				if ((isFilterRequired && filterId.equalsIgnoreCase(value))
						|| !isFilterRequired) {
					List<ColumnData[]> colData = fileData.get(value);
					if (colData == null) {
						colData = new ArrayList<>();
						fileData.put(value, colData);
					}
					colData.add(columns);
				}
			}
			cacheData.put(fileName, fileData);
		}
	}

	private String encodeFileToBase64Binary(String fileName) {
		String encodedString = "";

		File file = new File(fileName);
		byte[] bytes;
		try {
			bytes = loadFile(file);
			byte[] encoded = Base64.encodeBase64(bytes);
			encodedString = new String(encoded);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return encodedString;
	}

	private byte[] loadFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		long length = file.length();
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}
		byte[] bytes = new byte[(int) length];

		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		is.close();
		is = null;
		return bytes;
	}
	
	private String returnOrininalDateformat(String inputDate) {
		String wsDate = inputDate;
		
		return wsDate;
	}

}
