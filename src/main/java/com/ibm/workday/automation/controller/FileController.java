package com.ibm.workday.automation.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ibm.workday.automation.common.CommonConstants;
import com.ibm.workday.automation.model.File;
import com.ibm.workday.automation.model.OperationDir;
import com.ibm.workday.automation.model.User;
import com.ibm.workday.automation.service.FileService;
import com.ibm.workday.automation.service.OperationDirService;
import com.ibm.workday.automation.service.UserService;

@RestController
public class FileController implements CommonConstants {

	@Autowired
	FileService fileService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	OperationDirService operationDirService;
	
	@RequestMapping(value = "/getFileList", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<File> getFileList() {
		return fileService.getFileList();
	}
	
	@RequestMapping(value = "/getFile/{fileId}", method = RequestMethod.GET, headers = "Accept=application/json")
	public File getFile(@PathVariable("fileId") Long fileId) {
		return fileService.getFileById(fileId);
	}
	
	@RequestMapping(value = "/getFilesByUserId", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<File> getFilesByUserId(HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		return fileService.getFilesByUserId(userId);
	}
	
	@RequestMapping(value = "/getFileByClient", method = RequestMethod.GET, headers = "Accept=application/json")
	public File getFileByClient(HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		return fileService.getFileByClient(user.getClient());
	}
	
	@RequestMapping(value = "/getFilesByClient", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<File> getFilesByClient(HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		return fileService.getFilesByClient(user.getClient());
	}
	
	@RequestMapping(value = "/getProjectTemplateByClient", method = RequestMethod.GET, headers = "Accept=application/json")
	public File getProjectTemplateByClient(HttpSession httpSession) {
		List<File> files = getFilesByClient(httpSession);
		for(File file : files) {
			if(file.getFileLink().endsWith(".xlsx") && file.getFileName().equals(PROJECT_CHECKLIST_TEMPLATE)) {
				return file;
			}
		}
		
		return null;
	}

	@RequestMapping(value = "/getXmlFilesByClient", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<File> getXmlFilesByClient(HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		List<File> files = fileService.getFilesByClient(user.getClient());
		List<File> xmlFiles = new ArrayList<>();
		
		for(File file : files) {
			if(file.getFileLink().endsWith(".xml")) {
				xmlFiles.add(file);
			}
		}
		
		return xmlFiles;
	}
	
	@RequestMapping(value = "saveFileData/{fileName}", method = RequestMethod.POST)
	public void saveFileData(@PathVariable("fileName")String fileName, 
			                 @RequestParam("selectedFile") MultipartFile selectedFile,
			                 HttpSession httpSession) {
		byte[] fileData = null;

		System.out.println(selectedFile.getContentType());
		
		try {
			fileData = selectedFile.getBytes();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		
		File newFile = new File();
		newFile.setFileName(fileName);
		newFile.setFileLink(selectedFile.getOriginalFilename());
		newFile.setFileData(fileData);
		newFile.setUserId(userId);
		newFile.setClient(user.getClient());
		fileService.addFile(newFile);
	}
/*	
	@RequestMapping(value = "saveBuildFileData/{fileName}", method = RequestMethod.POST)
	public void saveBuildFileData(@PathVariable("fileName")String fileName, 
			                 @RequestParam("selectedBuildFile") MultipartFile selectedBuildFile,
			                 HttpSession httpSession) {
		saveFileData(fileName, selectedBuildFile, httpSession);
	}
*/	
	@RequestMapping(value = "submitOperationDirForm/{id}", method = RequestMethod.POST)
	public void submitOperationDirForm(@PathVariable("id")Long id) {
		File file = fileService.getFileById(id);
		Workbook workbook = parseToWorkBook(file);
		Sheet sheet = workbook.getSheetAt(0);
		
		for(int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); ++rowIndex) {
			Row row = sheet.getRow(rowIndex);
			if(row == null) {
				continue;
			}
			Cell cell = row.getCell(0);
			OperationDir operationDir = new OperationDir();
			if ((cell != null) && (cell.getStringCellValue() != null && !cell.getStringCellValue().trim().isEmpty())) {			
			    operationDir.setOperationName(cell.getStringCellValue().trim());
			}
			cell = row.getCell(1);
			if ((cell != null) && (cell.getStringCellValue() != null && !cell.getStringCellValue().trim().isEmpty())) {			
			    operationDir.setApplicationName(cell.getStringCellValue().trim());
			}
			operationDirService.addOperationDir(operationDir);
		}
	}
	
	@RequestMapping(value = "/deleteSavedFile/{fileId}", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public void deleteSavedFile(@PathVariable("fileId") Long fileId) {
		fileService.deleteFile(fileId);
	}

	@RequestMapping(value = "/downloadSavedFile/{fileId}", method = RequestMethod.GET, headers = "Accept=application/json")
	public void downloadSavedFile(@PathVariable("fileId") Long fileId, HttpServletResponse response) {
		File file = fileService.getFileById(fileId);
		response.setHeader("Content-Disposition", "attachment;filename=" + file.getFileLink() + "");
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		try (ByteArrayInputStream bis = new ByteArrayInputStream(file.getFileData())) {
			IOUtils.copy(bis, response.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
/*	
	@RequestMapping(value = "/downloadBuildRuleFile/{fileId}", method = RequestMethod.GET, headers = "Accept=application/json")
	public void downloadBuildRuleFile(@PathVariable("fileId") Long fileId, HttpServletResponse response) {
		downloadSavedFile(fileId, response);
	}
*/	
	private Workbook parseToWorkBook(File file) {
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
	
	@RequestMapping(value = "executeReferenceFile/{fileName}/{refName}/{refId}", method = RequestMethod.POST)
	public void executeRequestFile(@PathVariable("fileName")String fileName, @PathVariable("refName") String refName, 
			@PathVariable("refId") String refId, @RequestParam("referenceFile") MultipartFile referenceFile, HttpSession httpSession) {
		
		System.out.println("fileName-"+fileName);
		System.out.println("reqName-"+refName);
		Long referenceId = Long.parseLong(refId);		
		byte[] refFileData = null;
		try 
		{
			refFileData = referenceFile.getBytes();			
			Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
			User user = userService.getUser(userId);
			File file = fileService.getFileById(referenceId);
			if(file == null)
			{
				file = new File();
			}
			file.setFileName(refName);
			file.setFileLink(referenceFile.getOriginalFilename());
			file.setFileData(refFileData);
			file.setUserId(userId);
			file.setClient(user.getClient());
			fileService.addFile(file);			
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/getAllReferenceXML", method = RequestMethod.GET, headers = "Accept=application/json")
	public File getAllReferenceXML(HttpSession httpSession) {
		
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		List<File> files = fileService.getFilesByClient(user.getClient());
		
		for(File file : files) {
			if(file.getFileLink().endsWith(".xml") && file.getFileName().equals(REFERENCE_ID_WS_REQUEST)) {
				return file;
			}
		}
		
		return null;		
	}
	
	@RequestMapping(value = "/deleteReference/{refId}", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public void deleteReference(@PathVariable("refId") Long refId) {
		fileService.deleteFile(refId);
	}
	
}
