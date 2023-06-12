package com.ibm.workday.automation.operation;

public class DataRule {

	private String fileName;
	private String uniqueID;
	private String refID;
	private String columnName;
	private boolean multiple;
	private boolean required;
	private String formatDate;
	private boolean autoFormatDate;
	private boolean mapToBoolean;
	private int fieldLength = -1;
	private String prependChar;
	private boolean numericOnly;
	private boolean mappedValue;
	private boolean email;
	private boolean phoneNumber;
	private String rowIdColumnName;
	private int skipRows;
	private int headerRowNumber;
	private String fileType;
	private Condition condition;
	private int inpsectFromCurrentRow;
	private int minimumNonEmptyChildren;
	private boolean stripOffBeyondMaxLength;
	private boolean convertToBase64;
	private boolean convertToUpperCase;
	private boolean convertToLowerCase;
	private String numberFormat;
	private String stripOffBeforeChar;
	private String appendChar;
	private boolean checkFileExistence;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getUniqueID() {
		return uniqueID;
	}

	public void setUniqueID(String uniqueID) {
		this.uniqueID = uniqueID;
	}

	public String getRefID() {
		return refID;
	}

	public void setRefID(String refID) {
		this.refID = refID;
	}

	public boolean isMultiple() {
		return multiple;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getFormatDate() {
		return formatDate;
	}

	public void setFormatDate(String formatDate) {
		this.formatDate = formatDate;
	}

	public int getFieldLength() {
		return fieldLength;
	}

	public void setFieldLength(int fieldLength) {
		this.fieldLength = fieldLength;
	}

	public boolean isMappedValue() {
		return mappedValue;
	}

	public void setMappedValue(boolean mappedValue) {
		this.mappedValue = mappedValue;
	}

	public boolean isNumericOnly() {
		return numericOnly;
	}

	public void setNumericOnly(boolean numericOnly) {
		this.numericOnly = numericOnly;
	}

	public boolean isEmail() {
		return email;
	}

	public void setEmail(boolean email) {
		this.email = email;
	}

	public boolean isPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(boolean phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public boolean isAutoFormatDate() {
		return autoFormatDate;
	}

	public void setAutoFormatDate(boolean autoFormatDate) {
		this.autoFormatDate = autoFormatDate;
	}

	public boolean isMapToBoolean() {
		return mapToBoolean;
	}

	public void setMapToBoolean(boolean mapToBoolean) {
		this.mapToBoolean = mapToBoolean;
	}

	public String getPrependChar() {
		return prependChar;
	}

	public void setPrependChar(String prependChar) {
		this.prependChar = prependChar;
	}

	public String getRowIdColumnName() {
		return rowIdColumnName;
	}

	public void setRowIdColumnName(String rowIdColumnName) {
		this.rowIdColumnName = rowIdColumnName;
	}

	public int getSkipRows() {
		return skipRows;
	}

	public void setSkipRows(int skipRows) {
		this.skipRows = skipRows;
	}

	public int getHeaderRowNumber() {
		return headerRowNumber;
	}

	public void setHeaderRowNumber(int headerRowNumber) {
		this.headerRowNumber = headerRowNumber;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	public int getInpsectFromCurrentRow() {
		return inpsectFromCurrentRow;
	}

	public void setInpsectFromCurrentRow(int inpsectFromCurrentRow) {
		this.inpsectFromCurrentRow = inpsectFromCurrentRow;
	}

	public int getMinimumNonEmptyChildren() {
		return minimumNonEmptyChildren;
	}

	public void setMinimumNonEmptyChildren(int minimumNonEmptyChildren) {
		this.minimumNonEmptyChildren = minimumNonEmptyChildren;
	}

	public boolean isStripOffBeyondMaxLength() {
		return stripOffBeyondMaxLength;
	}

	public void setStripOffBeyondMaxLength(boolean stripOffBeyondMaxLength) {
		this.stripOffBeyondMaxLength = stripOffBeyondMaxLength;
	}

	public boolean isConvertToBase64() {
		return convertToBase64;
	}

	public void setConvertToBase64(boolean convertToBase64) {
		this.convertToBase64 = convertToBase64;
	}

	public boolean isConvertToUpperCase() {
		return convertToUpperCase;
	}

	public void setConvertToUpperCase(boolean convertToUpperCase) {
		this.convertToUpperCase = convertToUpperCase;
	}

	public boolean isConvertToLowerCase() {
		return convertToLowerCase;
	}

	public void setConvertToLowerCase(boolean convertToLowerCase) {
		this.convertToLowerCase = convertToLowerCase;
	}

	public String getNumberFormat() {
		return numberFormat;
	}

	public void setNumberFormat(String numberFormat) {
		this.numberFormat = numberFormat;
	}

	public String getStripOffBeforeChar() {
		return stripOffBeforeChar;
	}

	public void setStripOffBeforeChar(String stripOffBeforeChar) {
		this.stripOffBeforeChar = stripOffBeforeChar;
	}

	public String getAppendChar() {
		return appendChar;
	}

	public void setAppendChar(String appendChar) {
		this.appendChar = appendChar;
	}

	public boolean isCheckFileExistence() {
		return checkFileExistence;
	}

	public void setCheckFileExistence(boolean checkFileExistence) {
		this.checkFileExistence = checkFileExistence;
	}
}
