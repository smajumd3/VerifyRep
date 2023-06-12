package com.ibm.workday.automation.operation;

import java.util.List;
import java.util.Map;

import com.ibm.workday.automation.model.Operation;

public interface ValidationUtil {

	public boolean areAllFilesMapped(Operation operation);
	public Map<String, List<String>> checkFileColumnExistence(Operation operation);
	public Map<String, List<String>> checkFileColumnForDuplicates(Operation operation);
	public Map<String, List<String>> checkFileForUniqueId(Operation operation);
}
