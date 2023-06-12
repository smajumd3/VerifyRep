package com.ibm.workday.automation.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.json.JSONArray;
import org.apache.commons.json.JSONException;
import org.apache.commons.json.JSONObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.workday.automation.common.CommonConstants;
import com.ibm.workday.automation.model.ExclusionReference;
import com.ibm.workday.automation.model.Operation;
import com.ibm.workday.automation.model.Section;
import com.ibm.workday.automation.model.Tenant;
import com.ibm.workday.automation.model.User;
import com.ibm.workday.automation.operation.LoadDataRules;
import com.ibm.workday.automation.operation.Validate;
import com.ibm.workday.automation.operation.ValidateError;
import com.ibm.workday.automation.operation.ValidateRules;
import com.ibm.workday.automation.operation.ValidationMessage;
import com.ibm.workday.automation.operation.ValidationStatus;
import com.ibm.workday.automation.operation.ValidationUtil;
import com.ibm.workday.automation.service.ExclusionReferenceService;
import com.ibm.workday.automation.service.FileService;
import com.ibm.workday.automation.service.OperationService;
import com.ibm.workday.automation.service.SectionService;
import com.ibm.workday.automation.service.TenantMappingService;
import com.ibm.workday.automation.service.TenantService;
import com.ibm.workday.automation.service.UserService;

@RestController
public class ValidationController implements CommonConstants {
	
	Operation operation;
	
	Tenant tenant;
	
	ValidateRules validateRules;
	
	List<Validate> validationMessages;
	
	@Autowired
	LoadDataRules loadDataRules;
	
	@Autowired
	OperationService operationService;
	
	@Autowired
	TenantService tenantService;
	
	@Autowired
	SectionService sectionService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	TenantMappingService tenantMappingService;
	
	@Autowired
	ValidationUtil validation;
	
	@Autowired
	ValidationStatus validationStatus;
	
	@Autowired
	FileService fileService;
	
	@Autowired
	ExclusionReferenceService exclusionReferenceService;
	
	private Map<String, JSONArray> messageMap = new HashMap<String, JSONArray>();
	
	private static String[] columnsErrorLog = {"Type", "Value", "Column Name", "Message"};
	
	@RequestMapping(value = "/validateError/{operationId}/{tenantId}", 
            method = RequestMethod.POST, headers = "Accept=application/json")
    public ValidateError validateError(@PathVariable("operationId") Long operationId,
                 @PathVariable("tenantId") Long tenantId, HttpSession httpSession) {
		ValidateError validateError = new ValidateError();
		operation = operationService.getOperation(operationId);
		tenant = tenantService.getTenant(tenantId);
		
		if(!validation.areAllFilesMapped(operation)) {
			validateError.setError(true);
			validateError.setErrorMsg("Please map all required files.");
		} else {
			Map<String, List<String>> colExistenceMapping = validation.checkFileColumnExistence(operation);
			if (colExistenceMapping.size() > 0) {
				validateError.setError(true);
				validateError.setErrorMsg("Columns are missing in mapped file(s).");
			} else {
				Map<String, List<String>> dupColsMap = validation.checkFileColumnForDuplicates(operation);
				if (dupColsMap.size() > 0) {
					validateError.setError(true);
					validateError.setErrorMsg("Duplicate columns are present in mapped file(s).");
				} else {
					Map<String, List<String>> refIdColsMap = validation.checkFileForUniqueId(operation);
					if (refIdColsMap.size() > 0) {
						validateError.setError(true);
						validateError.setErrorMsg("Missing unique id in mapped file(s).");
						updateValidationStatus();
					} else {
						validateError.setError(false);
						validateError.setErrorMsg("OK");						
					}
				}
			}
		}
		return validateError;
	}
	
	@RequestMapping(value = "/validateRequest/{operationId}/{tenantId}", 
	                method = RequestMethod.POST, headers = "Accept=application/json")
	public List<Validate> validate(@PathVariable("operationId") Long operationId,
                         @PathVariable("tenantId") Long tenantId, HttpSession httpSession) {
		
		String excRefStr = "";
		
		validationMessages = new ArrayList<>();
		operation = operationService.getOperation(operationId);
		tenant = tenantService.getTenant(tenantId);
		
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		
		List<ExclusionReference> refList = exclusionReferenceService.getReferencesByClient(user.getClient());
		for(ExclusionReference reference : refList)
		{
			excRefStr = excRefStr + reference.getExclusionRefName() + ";";
		}
		
		String xmlStr = null;
		try 
		{
			List<com.ibm.workday.automation.model.File> files = fileService.getFilesByClient(user.getClient());				
			for(com.ibm.workday.automation.model.File file : files) 
			{
				if(file.getFileLink().endsWith(".xml") && file.getFileName().equals(REFERENCE_ID_WS_REQUEST)) 
				{
					xmlStr = new String(file.getFileData(), "UTF-8");;
				}
			}
		} 
		catch (UnsupportedEncodingException e1) 
		{
			e1.printStackTrace();
		}
		
		validateRules = new ValidateRules(loadDataRules, operation, tenant, excRefStr, xmlStr);

//		validateRules.execute();
		Thread validationThread = new Thread(validateRules);
		validationThread.start();
		
		validationMessages = validateRules.getValidationMessages();			
		updateValidationStatus(validationMessages);
//		for(Validate validate : validationMessages)
//		{
//			listAllValidationErrors(validate);
//		}
		
		return validationMessages;
	}
	
	@RequestMapping(value = "/getvalidationResponseStatus", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<Validate> getvalidationResponseStatus() {
		if(validateRules.isComplete()) {
			for(Validate validate : validationMessages)
			{
				listAllValidationErrors(validate);
			}
		}
		
		return validationMessages;
	}
	
	@RequestMapping(value = "/validationIsAllComplete", method = RequestMethod.GET, headers = "Accept=application/json")
	public Boolean validationIsAllComplete() {
		return validateRules.isComplete();
	}	
	
	private void listAllValidationErrors(Validate validate) {
		
		JSONArray msgArr = new JSONArray();
		List<ValidationMessage> validMsgList = validate.getMessages();
		for(ValidationMessage validationMessage : validMsgList)
		{
			JSONObject obj = new JSONObject();
			try 
			{
				obj.put("type", validationMessage.getType());
				obj.put("value", validationMessage.getValue());
				obj.put("column", validationMessage.getColumnName());
				obj.put("detMsg", validationMessage.getMessage());
				msgArr.put(obj);
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
			messageMap.put(validate.getFileName(), msgArr);
		}
		System.out.println("messageMap: "+messageMap);
		
	}
	
	@RequestMapping(value = "/validateMapData/{sectionId}/{tenantId}", method = RequestMethod.POST, headers = "Accept=application/json")
	public List<Validate> validateMapData(@PathVariable("sectionId") Long sectionId, @PathVariable("tenantId") Long tenantId,
			                       HttpSession httpSession) {
		Section section = sectionService.getSection(sectionId);
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		operation = operationService.getOperation(section.getOperationName(), userId);
		return validate(operation.getOperationId(), tenantId, httpSession);
	}

	@RequestMapping(value = "/validateMapDataRequest/{sectionId}/{tenantId}", method = RequestMethod.POST, headers = "Accept=application/json")
    public ValidateError validateMappedData(@PathVariable("sectionId") Long sectionId,
    		                                @PathVariable("tenantId") Long tenantId, 
    		                                 HttpSession httpSession) {
		Section section = sectionService.getSection(sectionId);
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
/*		User user = userService.getUser(userId);
		Page page = section.getPage();

		TenantMapping mapping = tenantMappingService.getTenantMappingByPageClient(page.getIndex(), user.getClient());
		List<Tenant> tenants = tenantService.getTenantListByUser(userId, user.getClient());
		Tenant tenant = null;
		for(Tenant t : tenants) {
			if(t.getTenantName().equals(mapping.getTenantName())) {
				tenant = t;
				break;
			}
		}
		if(tenant == null) {
			return new ArrayList<>();
		}
*/
		operation = operationService.getOperation(section.getOperationName(), userId);

		return validateError(operation.getOperationId(), tenantId, httpSession);
	}
	
	private void updateValidationStatus() {
		validationStatus.setSeverity("CRITICAL");
		validationStatus.setStatus(false);
		validationStatus.setLastUpdatedDateTime(new Date(System.currentTimeMillis()));
	}
	
	private void updateValidationStatus(List<Validate> validations) {
		boolean isAllOk = true;
		if (validations.size() > 0) {
			for (Validate validation : validations) {
				if (!validation.isValid()) {
					isAllOk = false;
					break;
				}
			}

			validationStatus.setStatus(isAllOk);
			validationStatus.setLastUpdatedDateTime(new Date(System.currentTimeMillis()));
		}
	}
	
	@RequestMapping(value = "/exportErrorData/{fileName}", method = RequestMethod.GET, headers = "Accept=application/json")
	public void exportErrorData(@PathVariable("fileName") String fileName, HttpServletResponse response) {
		
		File file = null;
		FileOutputStream fileOut = null;
		System.out.println("fileName-"+fileName);
		JSONArray jsonArray = messageMap.get(fileName);
		
		Workbook workbook = new XSSFWorkbook(); 
        Sheet sheet = workbook.createSheet("Error Log");
        
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.AUTOMATIC.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        
        Row headerRow = sheet.createRow(0);
        
        for(int i = 0; i < columnsErrorLog.length; i++) {
            Cell hCell = headerRow.createCell(i);
            hCell.setCellValue(columnsErrorLog[i]);
            hCell.setCellStyle(headerCellStyle);
        }
        
        int rowNum = 1;
        
        for(int i = 0; i<jsonArray.length(); i++) 
        {
        	Row row = sheet.createRow(rowNum++);
        	JSONObject objects = null;
			try 
			{
				objects = jsonArray.getJSONObject(i);
	        	row.createCell(0).setCellValue(objects.getString("type"));
	        	row.createCell(1).setCellValue(objects.getString("value"));
	        	row.createCell(2).setCellValue(objects.getString("column"));
	        	row.createCell(3).setCellValue(objects.getString("detMsg"));
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			}

        }
        
        for(int i = 0; i < columnsErrorLog.length; i++) {
            sheet.autoSizeColumn(i);
        }
               
		try 
		{
			file = File.createTempFile("ErrorLog", ".xlsx");
			fileOut = new FileOutputStream(file);
			workbook.write(fileOut);
		} 
		catch (FileNotFoundException e1) 
		{
			e1.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
                
		FileInputStream fis = null;
		response.setHeader("Content-Disposition", "attachment;filename=" + "ErrorLog" + ".xlsx" + "");
		response.setContentType("application/vnd.ms-excel");
		try
		{
			fis = new FileInputStream(file);
			IOUtils.copy(fis, response.getOutputStream());
			fis.close();		
			workbook.close();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

}
