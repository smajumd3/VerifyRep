package com.ibm.workday.automation.operation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.ibm.workday.automation.common.CommonConstants;
import com.ibm.workday.automation.model.File;
import com.ibm.workday.automation.model.Section;

@Component
public class WorkbookUtilImpl implements WorkbookUtil, CommonConstants {

	@Override
	public Workbook parseToWorkBook(File file) {
		ByteArrayInputStream inputStream = null;
		Workbook workbook = null;
		try {
			inputStream = new ByteArrayInputStream(file.getFileData());
			workbook = new XSSFWorkbook(inputStream);
		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if(inputStream != null) inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return workbook;
	}

	@Override
	public Boolean executeSheet(Sheet sheet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Section> getSections(Sheet sheet) {
		int startRow = 1;
		Section section = null;
		List<Section> sections = new ArrayList<>();
		int rows = sheet.getLastRowNum();
		int index = 0;
		
		for (int r = startRow; r <= rows; ++r) {
			section = new Section();
			Row row = sheet.getRow(r);
			if(row == null) {
				continue;
			}
			section.setIndex(++index);
			Cell cell = row.getCell(4);
			if (cell != null && cell.getStringCellValue() != null 
					&& cell.getStringCellValue().equals(CLIENT_WORKBOOK)) {
				cell = row.getCell(1);
				if (cell != null && cell.getStringCellValue() != null) {
					section.setAreaName(cell.getStringCellValue().trim());
				}
				cell = row.getCell(2);
				if (cell != null && cell.getStringCellValue() != null) {
					section.setTaskName(cell.getStringCellValue().trim());
				}
				cell = row.getCell(3);
				if (cell != null && cell.getStringCellValue() != null) {
					section.setOperationName(cell.getStringCellValue().trim());
				}
				cell = row.getCell(5);
				if (cell != null && cell.getStringCellValue() != null) {
					section.setStatus(0);
				}
				cell = row.getCell(6);
				if (cell != null && cell.getStringCellValue() != null) {
					section.setAssignedTo(cell.getStringCellValue().trim());
				}
/*				cell = row.getCell(7);
				if (cell != null && cell.getStringCellValue() != null) {
					section.setValidateDate(cell.getStringCellValue().trim());
				}
				cell = row.getCell(8);
				if (cell != null && cell.getStringCellValue() != null) {
					section.setLoadDate(cell.getStringCellValue().trim());
				}

				cell = row.getCell(9);
				if (cell != null && cell.getStringCellValue() != null) {
					section.setTotalRecords((int)cell.getNumericCellValue());
				}
				cell = row.getCell(10);
				if (cell != null && cell.getStringCellValue() != null) {
					section.setTotalSuccess((int)cell.getNumericCellValue());
				}
				cell = row.getCell(11);
				if (cell != null && cell.getStringCellValue() != null) {
					section.setTotalFailures((int)cell.getNumericCellValue());
				}
*/				
				section.setExecute(false);
				sections.add(section);
			}
		}
		
		return sections;
	}

}
