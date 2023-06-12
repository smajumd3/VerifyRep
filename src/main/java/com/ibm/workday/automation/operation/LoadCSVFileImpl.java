package com.ibm.workday.automation.operation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

@Component
public class LoadCSVFileImpl implements LoadCSVFile {

	private LoadCSVFileImpl() {

	}

	@Override
	public List<String> getFileColumnLabels(InputStream stream) {
		List<String> colLabels = null;
		CSVReader csvReader = null;

		try {
			Reader reader = new InputStreamReader(stream);
			csvReader = new CSVReader(reader, ',', '\"', 0);
			String[] cols = csvReader.readNext();
			colLabels = Arrays.asList(cols);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (csvReader != null) {
				try {
					csvReader.close();
				} catch (IOException e) {
				}
			}
		}
		return colLabels;
	}

	@Override
	public List<ColumnData[]> getData(Reader reader) {
		List<ColumnData[]> dataRows = new ArrayList<>();
		CSVReader csvReader = null;

		try {
			if (reader != null) {
				csvReader = new CSVReader(reader, ',', '\"', 0);
				List<String[]> rows = csvReader.readAll();
				String[] columnNames = rows.get(0);
				for (int i = 1; i < rows.size(); i++) {
					String[] row = rows.get(i);
					ColumnData[] rowData = new ColumnData[row.length];
					for (int j = 0; j < row.length && j < columnNames.length; j++) {
						String value = row[j];
						// System.out.println("Value : " + value);
						String name = columnNames[j];
						// System.out.println("Name : " + name);
						rowData[j] = new ColumnData(name, value);
					}
					dataRows.add(rowData);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
					csvReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		return dataRows;
	}

	/**
	 * 
	 * @param reader
	 * @param rowIdColumnName
	 * @return
	 */
	@Override
	public Map<Integer, List<ColumnData[]>> getData(Reader reader,
			String rowIdColumnName) {
		Map<Integer, List<ColumnData[]>> contents = new LinkedHashMap<>();
		CSVReader csvReader = null;

		try {
			if (reader != null) {
				csvReader = new CSVReader(reader, ',', '\"', 0);
				List<String[]> rows = csvReader.readAll();
				String[] columnNames = rows.get(0);
				for (int i = 1; i < rows.size(); i++) {
					String[] row = rows.get(i);
					Integer rowIdentifier = 0;
					ColumnData[] rowData = new ColumnData[row.length];
					for (int j = 0; j < row.length; j++) {
						String value = row[j];
						String name = columnNames[j];
						if (name.equalsIgnoreCase(rowIdColumnName)) {
							rowIdentifier = new Integer(value);
						}
						rowData[j] = new ColumnData(name, value);
					}
					if (rowIdentifier > 0) {
						List<ColumnData[]> dataRows = contents
								.get(rowIdentifier);
						if (dataRows == null) {
							dataRows = new ArrayList<>();
							contents.put(rowIdentifier, dataRows);
						}
						dataRows.add(rowData);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
					csvReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return contents;
	}
}
