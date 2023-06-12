package com.ibm.workday.automation.operation;

import java.io.InputStream;

public interface WriteDataRules {
	InputStream getDataRules(DataElement dataElement);
	boolean writeToFile(final DataElement dataElement, String fileName);
}
