package com.ibm.workday.automation.operation;

import java.util.List;
import java.util.Map;

import com.ibm.workday.automation.model.Application;

public interface ApplicationUtil {
	
	List<String> getAvailableOperations(Application application);
	byte[] generateWsdlData(Application application);
	byte[] generateXsdData(Application application);
//	void generateXmlData(Application application, Operation operation);
	Map<String, String> getKeyValueURLs();

}
