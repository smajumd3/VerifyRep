package com.ibm.workday.automation.operation;

import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Map;

public interface LoadCSVFile {
	List<String> getFileColumnLabels(InputStream stream);
	List<ColumnData[]> getData(Reader reader);
	Map<Integer, List<ColumnData[]>> getData(Reader reader, String rowIdColumnName);
}
