package com.ibm.workday.automation.operation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class LoadExcelFileImpl implements LoadExcelFile {

	private static final Map<Integer, String> AUTO_GENERATED_COLUMN_MAP = new HashMap<>();
	private static final String AUTO_GENERATED_COL_PREFIX = "Col-";
	private static final int MAX_CHAR_KEY = 26;
	private static final int MIN_CHAR_KEY = 1;

	public LoadExcelFileImpl() {
		AUTO_GENERATED_COLUMN_MAP.put(1, "A");
		AUTO_GENERATED_COLUMN_MAP.put(2, "B");
		AUTO_GENERATED_COLUMN_MAP.put(3, "C");
		AUTO_GENERATED_COLUMN_MAP.put(4, "D");
		AUTO_GENERATED_COLUMN_MAP.put(5, "E");
		AUTO_GENERATED_COLUMN_MAP.put(6, "F");
		AUTO_GENERATED_COLUMN_MAP.put(7, "G");
		AUTO_GENERATED_COLUMN_MAP.put(8, "H");
		AUTO_GENERATED_COLUMN_MAP.put(9, "I");
		AUTO_GENERATED_COLUMN_MAP.put(10, "J");
		AUTO_GENERATED_COLUMN_MAP.put(11, "K");
		AUTO_GENERATED_COLUMN_MAP.put(12, "L");
		AUTO_GENERATED_COLUMN_MAP.put(13, "M");
		AUTO_GENERATED_COLUMN_MAP.put(14, "N");
		AUTO_GENERATED_COLUMN_MAP.put(15, "O");
		AUTO_GENERATED_COLUMN_MAP.put(16, "P");
		AUTO_GENERATED_COLUMN_MAP.put(17, "Q");
		AUTO_GENERATED_COLUMN_MAP.put(18, "R");
		AUTO_GENERATED_COLUMN_MAP.put(19, "S");
		AUTO_GENERATED_COLUMN_MAP.put(20, "T");
		AUTO_GENERATED_COLUMN_MAP.put(21, "U");
		AUTO_GENERATED_COLUMN_MAP.put(22, "V");
		AUTO_GENERATED_COLUMN_MAP.put(23, "W");
		AUTO_GENERATED_COLUMN_MAP.put(24, "X");
		AUTO_GENERATED_COLUMN_MAP.put(25, "Y");
		AUTO_GENERATED_COLUMN_MAP.put(26, "Z");
	}

	/**
	 * 
	 * @param dataRule
	 * @return
	 */
	@Override
	public Map<Integer, List<ColumnData[]>> readFromFile(DataRule dataRule) {
		File excelFile = new File(dataRule.getFileName());
		Map<Integer, List<ColumnData[]>> data = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(excelFile);
			data = readData(dataRule, fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return data;
	}

	/**
	 * 
	 * @param dataRule
	 * @return
	 */
	@Override
	public Map<Integer, List<ColumnData[]>> readData(DataRule dataRule,
			InputStream is) {
		long startTime = System.currentTimeMillis();
		int headerRowNumber = dataRule.getHeaderRowNumber();
		int skipRows = dataRule.getSkipRows();
		String rowIdColLabel = dataRule.getRowIdColumnName();
		Map<Integer, List<ColumnData[]>> data = new LinkedHashMap<>();
		if (headerRowNumber < 1) {
			if (rowIdColLabel != null
					&& !rowIdColLabel.contains(AUTO_GENERATED_COL_PREFIX)) {
				rowIdColLabel = AUTO_GENERATED_COL_PREFIX + rowIdColLabel;
			}
		}
		XSSFWorkbook wb = null;
		try {
			wb = new XSSFWorkbook(is);
			XSSFSheet ws = wb.getSheetAt(0);
			XSSFRow headerRow = null;
			if (headerRowNumber > 0) {
				headerRow = ws.getRow(headerRowNumber - 1);
			}
			int rowId = 1;
			for (int i = skipRows; i <= ws.getLastRowNum(); i++) {
				XSSFRow row = ws.getRow(i);
				int colVal = MIN_CHAR_KEY;
				int k = 0;
				ColumnData[] colDatas = new ColumnData[row.getLastCellNum() + 1];
				for (int j = 0; j <= row.getLastCellNum(); j++) {
					XSSFCell cell = row.getCell(j);
					if (cell != null) {
						StringBuilder colName = new StringBuilder();
						String labelName = "";
						if (headerRow == null) {
							colName.append(AUTO_GENERATED_COL_PREFIX);
							int realVal = colVal % MAX_CHAR_KEY;
							if (colVal > MAX_CHAR_KEY) {
								int divider = colVal;
								int reminder = realVal;
								if (realVal == 0) {
									divider -= 1;
									reminder = MAX_CHAR_KEY;
								}
								colName.append(AUTO_GENERATED_COLUMN_MAP
										.get(divider / MAX_CHAR_KEY));
								colName.append(AUTO_GENERATED_COLUMN_MAP
										.get(reminder));
							} else if (colVal == MAX_CHAR_KEY) {
								colName.append(AUTO_GENERATED_COLUMN_MAP
										.get(colVal));
							} else if (colVal == 0) {
								colName.append(AUTO_GENERATED_COLUMN_MAP.get(1));
							} else {
								colName.append(AUTO_GENERATED_COLUMN_MAP
										.get(realVal));
							}
							labelName = colName.toString();
						} else {
							XSSFCell headerCell = headerRow.getCell(j);
							labelName = headerCell.toString();
						}

						if (rowIdColLabel != null) {
							if (labelName.equalsIgnoreCase(rowIdColLabel)) {
								try {
									// Possible that rowId is stored as string
									// in Excel sheet
									rowId = Integer.parseInt(cell.toString());
								} catch (NumberFormatException nf) {
									// nf.printStackTrace();
								}
							}
						}
						String aVal = cell.toString();
						colDatas[k] = new ColumnData(labelName, aVal,
								cell.getCellType());
						k++;
					}
					colVal++;
				}
				colVal = MIN_CHAR_KEY;
				List<ColumnData[]> currentRows = data.get(rowId);
				if (currentRows == null) {
					currentRows = new ArrayList<>();
					data.put(rowId, currentRows);
				}
				currentRows.add(colDatas);
				if (rowIdColLabel == null) {
					rowId++;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(wb != null) {
				try {
					wb.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Total time to prepare Excel Data : " + (System.currentTimeMillis() - startTime));
		return data;
	}

}
