package com.ibm.workday.automation.operation;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.ibm.workday.automation.model.File;
import com.ibm.workday.automation.model.Section;

public interface WorkbookUtil {

	Workbook parseToWorkBook(File file);
	Boolean executeSheet(Sheet sheet);
	List<Section> getSections(Sheet sheet);
}
