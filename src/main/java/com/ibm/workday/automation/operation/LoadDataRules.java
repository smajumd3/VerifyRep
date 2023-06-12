package com.ibm.workday.automation.operation;

import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

import com.ibm.workday.automation.model.Operation;

public interface LoadDataRules {
	DataElement getDataRules(String ruleFileName);
	DataElement getDataRulesFromString(String stringValue);
	DataElement getDataRules(Reader reader);
	DataElement getDataRules(Reader reader, boolean asInFile);
	DataElement getDataRules(Operation operation, boolean asInFile);
	byte[] readRuleFileContents(Operation operation);
	Map<String, InputStream> getFileContentMap(Operation operation);
	Map<String, FileData> getFileDataMap(Operation operation);
	Map<String, FileData> getFileDataMap(Operation operation, String rowIdColName);
	Map<String, FileData> getFileDataMap(Operation operation, DataRule dataRule);
}
