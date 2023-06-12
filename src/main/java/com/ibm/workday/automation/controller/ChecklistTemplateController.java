package com.ibm.workday.automation.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.workday.automation.common.CommonConstants;
import com.ibm.workday.automation.model.File;
import com.ibm.workday.automation.model.Operation;
import com.ibm.workday.automation.model.Page;
import com.ibm.workday.automation.model.Section;
import com.ibm.workday.automation.model.User;
import com.ibm.workday.automation.operation.WorkbookUtil;
import com.ibm.workday.automation.service.FileService;
import com.ibm.workday.automation.service.OperationService;
import com.ibm.workday.automation.service.PageService;
import com.ibm.workday.automation.service.SectionService;
import com.ibm.workday.automation.service.UserService;

@RestController
public class ChecklistTemplateController implements CommonConstants {
	
	@Autowired
	FileService fileService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	OperationService operationService;
	
	@Autowired
	PageService pageService;
	
	@Autowired
	SectionService sectionService;
	
	@Autowired
	WorkbookUtil workbookUtil;
	
	@RequestMapping(value = "/getPagesByUserId", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<Page> getPagesByUserId(HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);		
		return pageService.getPagesByUser(userId);
	}
	
	@RequestMapping(value = "/updateSectionAccess/{id}/{checked}", method = RequestMethod.PUT, headers = "Accept=application/json")
	public void updateSectionAccess(@PathVariable("id") Long id, @PathVariable("checked") Boolean checked, HttpSession httpSession) {	
		Section section = sectionService.getSection(id);
		section.setExecute(checked);

		if(checked) {
			String operationName = section.getOperationName();
			Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
			Operation operation = operationService.getOperation(operationName, userId);
			if(operation != null && operation.getRuleFileData() != null) {
				section.setIsDownload(true);
			} else {
				section.setIsDownload(false);
			}
		} else {
			section.setIsDownload(false);
		}

		sectionService.updateSection(section);
	}
	
	@RequestMapping(value = "/updateSectionStatus/{id}/{value}", method = RequestMethod.PUT, headers = "Accept=application/json")
	public void updateSectionStatus(@PathVariable("id") Long id, @PathVariable("value") Integer value) {	
		Section section = sectionService.getSection(id);
		section.setStatus(value);
		if(value == 1) {
			section.setValidateDate(new Date(System.currentTimeMillis()).toString());
		}
		sectionService.updateSection(section);
	}	

	@RequestMapping(value = "saveTemplateFileData", method = RequestMethod.POST, headers = "Accept=application/json")
	public void saveTemplateFileData(HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		
		List<Page> pages = pageService.getPagesByUser(userId);
		
		if(pages.isEmpty()) {
			File file = fileService.getFileByFileNameClient(PROJECT_CHECKLIST_TEMPLATE, user.getClient());
			Workbook workbook = workbookUtil.parseToWorkBook(file);
			
//			for (int sheetIndex = 0; sheetIndex < 5/*workbook.getNumberOfSheets()*/; ++sheetIndex) {		// Suman : need to change later
			for (int sheetIndex = 0; sheetIndex < 2; ++sheetIndex) {
			    Sheet sheet = workbook.getSheetAt(sheetIndex);
			    List<Section> sections = workbookUtil.getSections(sheet);
			    Page page = new Page();
			    page.setIndex(sheetIndex);
			    page.setPageName(sheet.getSheetName());
			    page.setUserId(userId);
			    for(Section section : sections) {
			    	section.setPage(page);
			    	sectionService.addSection(section);
			    }
			}
		}
	}
	
	@RequestMapping(value = "/getWorkbookPages", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<String> getWorkbookPages(HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		
		File file = fileService.getFileByFileNameClient(PROJECT_CHECKLIST_TEMPLATE, user.getClient());
		if(file == null) {
			return new ArrayList<>();
		}
		Workbook workbook = workbookUtil.parseToWorkBook(file);
		List<String> pages = new ArrayList<>();
		
//		for (int sheetIndex = 0; sheetIndex < 5/*workbook.getNumberOfSheets()*/; ++sheetIndex) {		// Suman : need to change later
		for (int sheetIndex = 0; sheetIndex < 1; ++sheetIndex) {
			Sheet sheet = workbook.getSheetAt(sheetIndex);
			pages.add(sheet.getSheetName());
		}
		
		return pages;
	}
	
}
