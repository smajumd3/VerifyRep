package com.ibm.workday.automation.operation;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface LoadExcelFile {
	Map<Integer, List<ColumnData[]>> readFromFile(DataRule dataRule);
	Map<Integer, List<ColumnData[]>> readData(DataRule dataRule, InputStream is);
}
