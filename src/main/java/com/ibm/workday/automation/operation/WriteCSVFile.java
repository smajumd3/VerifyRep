package com.ibm.workday.automation.operation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.ibm.workday.automation.common.CommonConstants;

import au.com.bytecode.opencsv.CSVWriter;

public class WriteCSVFile implements CommonConstants {

	private FileData fileData;
	private String uniqueIdColumnName;
	private Set<String> filterIds;
	
	public WriteCSVFile() {
		
	}

	public WriteCSVFile(final FileData fileData, String uniqueIdColumnName,
			final Set<String> filterIds) {
		this.fileData = fileData;
		if (uniqueIdColumnName.startsWith("$")) {
			this.uniqueIdColumnName = uniqueIdColumnName.substring(1);
		} else {
			this.uniqueIdColumnName = uniqueIdColumnName;
		}
		this.filterIds = filterIds;
	}

	private String status;

	public File execute() {
		this.status = "Started";
		CSVWriter csvWriter = null;
		File file = null;
		try {
			String fileName = fileData.getName().substring(0, fileData.getName().indexOf("."));
			fileName = fileName + "_Error";
			file = File.createTempFile(fileName, ".csv");
			csvWriter = new CSVWriter(new FileWriter(file), ',');
			List<String[]> allData = new ArrayList<>();
			allData.add(getDerivedColumnNames());
			for (ColumnData[] row : fileData.getData()) {
				if (isInFilter(row)) {
					int index = 0;
					String[] rowData = new String[row.length];
					for (int i = 0; i < row.length; i++) {
						ColumnData colData = row[i];
						String value = "";
						if (colData != null) {
							value = colData.getValue();
						}
						rowData[index++] = value;
					}
					allData.add(rowData);
				}
			}
			csvWriter.writeAll(allData); // Finally write everything into a file.
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (csvWriter != null) {
				try {
					csvWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return file;
	}

	private String[] getDerivedColumnNames() {
		ColumnData[] firstRow = fileData.getData().get(0);
		String[] colNames = new String[firstRow.length];
		for (int i = 0; i < firstRow.length; i++) {
			ColumnData column = firstRow[i];
			colNames[i] = column.getName();
		}
		return colNames;
	}

	private boolean isInFilter(ColumnData[] row) {
		boolean inFilter = false;
		for (int i = 0; i < row.length; i++) {
			ColumnData col = row[i];
			if (col.getName().equalsIgnoreCase(uniqueIdColumnName)) {
				if (filterIds.contains(col.getValue())) {
					inFilter = true;
					break;
				}
			}
		}
		return inFilter;
	}

	public String getStatus() {
		return status;
	}
	
	public File writeValidationErrorMessages(List<WSResponse> wsResponses, String uniqueIdColumnName) {
		CSVWriter csvWriter = null;
		File file = null;
		try {
			file = File.createTempFile("Validation Errors", ".csv");
			csvWriter = new CSVWriter(new FileWriter(file), ',');
			List<String[]> messages = new ArrayList<>();
			String[] row = new String[3];
			row[0] = uniqueIdColumnName.substring(1);
			row[1] = "Message";
			row[2] = "Detail";
			messages.add(row);
			for (WSResponse response : wsResponses) {
				if (response.getResult().equalsIgnoreCase(STATUS_FAILUE)) {
					for (String message : response
							.getFaultMessages()) {
						row = new String[3];
						row[0] = response
								.getName();
						row[1] = response
								.getFaultMessage();
						row[2] = message;
						messages.add(row);
					}
				}
			}
			csvWriter.writeAll(messages);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (csvWriter != null) {
				try {
					csvWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return file;
	}

}
