package com.ibm.workday.automation.controller;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.soap.SOAPMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.workday.automation.common.CommonConstants;
import com.ibm.workday.automation.model.Operation;
import com.ibm.workday.automation.model.Page;
import com.ibm.workday.automation.model.Result;
import com.ibm.workday.automation.model.Section;
import com.ibm.workday.automation.model.Tenant;
import com.ibm.workday.automation.operation.DataElement;
import com.ibm.workday.automation.operation.FileData;
import com.ibm.workday.automation.operation.LoadDataRules;
import com.ibm.workday.automation.operation.ProcessBatchRequest;
import com.ibm.workday.automation.operation.ProcessRules;
import com.ibm.workday.automation.operation.ResponseDataUtil;
import com.ibm.workday.automation.operation.ResponseStatus;
import com.ibm.workday.automation.operation.SOAPRequestBuilder;
import com.ibm.workday.automation.operation.ValidationUtil;
import com.ibm.workday.automation.operation.WSResponse;
import com.ibm.workday.automation.operation.WSXPathExpression;
import com.ibm.workday.automation.operation.WriteCSVFile;
import com.ibm.workday.automation.service.OperationService;
import com.ibm.workday.automation.service.PageService;
import com.ibm.workday.automation.service.ResultService;
import com.ibm.workday.automation.service.TenantMappingService;
import com.ibm.workday.automation.service.TenantService;
import com.ibm.workday.automation.service.UserService;

@RestController
public class SendWWSRequestController implements CommonConstants {
	
	Operation operation;
	Tenant tenant;
	Page page;
	Section section;
	Map<String, FileData> fileDataAll;
	List<WSResponse> wsResponse;
	Integer batchSize;
	ProcessBatchRequest batchRequest;
	ResponseDataUtil responseData;
	
	@Autowired
	OperationService operationService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	TenantService tenantService;
	
	@Autowired
	TenantMappingService tenantMappingService;
	
	@Autowired
	PageService pageService;
	
	@Autowired
	ResultService resultService;
	
	@Autowired
	LoadDataRules loadDataRules;
	
	@Autowired
	ResponseStatus responseStatus;
	
	@Autowired
	ValidationUtil validation;
	
	
	Map<String, SOAPMessage> soapMessages;
	
	@RequestMapping(value = "/sendWWSRequest/{operationId}/{tenantId}/{pageId}", 
			        method = RequestMethod.POST, headers = "Accept=application/json")
	public List<WSResponse> sendWWSRequest(@PathVariable("operationId") Long operationId,
			                   @PathVariable("tenantId") Long tenantId,
			                   @PathVariable("pageId") Long pageId) {

		operation = operationService.getOperation(operationId);
		tenant = tenantService.getTenant(tenantId);
		page = pageService.getPage(pageId);
		section = getSection(page, operation);
		int[] csvCounts = null;
		String topLevelRequest = null;
		SOAPMessage soapMessage = null;
		
		String endPointUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl();
		
		if(!endPointUrl.endsWith("/")) {
			endPointUrl = endPointUrl + "/";
		}
		
		endPointUrl = endPointUrl + operation.getApplication().getApplicationName();
		
		DataElement dataRoot = loadDataRules.getDataRules(operation, false);
		
		List<WSResponse> response = new ArrayList<>();
		
		if(!validation.areAllFilesMapped(operation)) {
			return response;
		} else {
			Map<String, List<String>> colExistenceMapping = validation.checkFileColumnExistence(operation);
			if (colExistenceMapping.size() > 0) {
				return response;
			} else {
				Map<String, List<String>> dupColsMap = validation.checkFileColumnForDuplicates(operation);
				if (dupColsMap.size() > 0) {
					return response;
				} else {
					Map<String, List<String>> refIdColsMap = validation.checkFileForUniqueId(operation);
					if (refIdColsMap.size() > 0) {
						return response;
					}
				}
			}
		}
		
		SOAPRequestBuilder soapRequestBuilder = new SOAPRequestBuilder(tenant);
		//Map<String, SOAPMessage> soapMessages = new LinkedHashMap<>();
		soapMessages = new LinkedHashMap<>();
		Map<String, FileData> fileDataMap = null;
		String rowIdColumnName = dataRoot.getRule().getRowIdColumnName();
		
		if (rowIdColumnName != null) {
			fileDataMap = loadDataRules.getFileDataMap(operation, dataRoot.getRule());
		} else {
			fileDataMap = loadDataRules.getFileDataMap(operation);
		}
		
		fileDataAll = new HashMap<>(fileDataMap);
		ProcessRules processRules = new ProcessRules(fileDataMap, dataRoot.getRule().getFileType());
		
		if (dataRoot.getRule() != null && dataRoot.getRule().isMultiple()) {
			List<DataElement> dataRules = processRules.populateElements(dataRoot);
			long startTime = System.currentTimeMillis();
			csvCounts = new int[dataRules.size()];
			for(int i = 0; i < csvCounts.length; i++) {
		    	csvCounts[i] = i;
		    }
			System.out.println("csvCounts : " + csvCounts.length);
			
			batchSize = getBatchRequestSize(dataRules.size());
			
			ForkJoinPool forkJoinPool = new ForkJoinPool();
		    forkJoinPool.invoke(new Sum(csvCounts, 0, csvCounts.length, batchSize, dataRules, soapRequestBuilder, soapMessages));
			
			/*
			 * for (DataElement dataElement : dataRules) { soapMessage =
			 * soapRequestBuilder.buildSOAPRequest(dataElement);
			 * soapMessages.put(dataElement.getIdentifierValue(), soapMessage);
			 * 
			 * if (topLevelRequest == null) { topLevelRequest = dataElement.getName(); } }
			 */
			System.out.println("Total time taken to get SOAPRequest : " + (System.currentTimeMillis() - startTime));
		} else {
			DataElement dataElement = processRules.populateElement(dataRoot);
			soapMessage = soapRequestBuilder.buildSOAPRequest(dataElement);
			soapMessages.put("Request_1", soapMessage);
			
			if (topLevelRequest == null) {
				topLevelRequest = dataElement.getName();
			}
		}
		
		System.out.println("Processing batch request");
		
		WSXPathExpression expression = new WSXPathExpression();
		expression.setRequestName(operation.getRuleName());
		expression.setResponseExpression(operation.getResponsePath());
		expression.setFaultExpression(SOAP_FAULT_XPATH);
		responseData = new ResponseDataUtil();
		batchRequest = new ProcessBatchRequest(soapMessages, responseData, 
				                               expression, endPointUrl, 
				                               batchSize, 500);
		System.out.println("Done processing batch request");
		wsResponse = batchRequest.getResponseMessages();
		new Thread(batchRequest).start();
		
		return getWWSResponse(1);
	}
	
	@RequestMapping(value = "/sendToWWSRequest/{operationName}/{pageId}/{tenantId}", 
	        method = RequestMethod.POST, headers = "Accept=application/json")	
	public List<WSResponse> sendWWSRequest(@PathVariable("operationName") String operationName,
                                           @PathVariable("pageId") Long pageId,
                                           @PathVariable("tenantId") Long tenantId,
                                           HttpSession httpSession) {
		
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);	
		operation = operationService.getOperation(operationName, userId);			
		return sendWWSRequest(operation.getOperationId(), tenantId, pageId);		
	}
	
	@RequestMapping(value = "/getWWSBatchResponse/{batchNumber}", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<WSResponse> getWWSResponse(@PathVariable("batchNumber") Integer batchNumber) {
		
		List<WSResponse> batchResponse = new ArrayList<>();

		if(batchNumber > 0 && batchNumber <= responseData.getBatchCount()) {
		    int startIndex = ((batchNumber - 1) * batchSize);
		    int endIndex = (batchNumber * batchSize);
		    int total = soapMessages.size();
		    for(int i = startIndex; i < endIndex && i < total; ++i ) {
			    batchResponse.add(wsResponse.get(i));
		    }
		}
		return batchResponse;
	}
	
	@RequestMapping(value = "/getPercentageComplete", method = RequestMethod.GET, headers = "Accept=application/json")
	public Integer getPercentageComplete() {
		int total = soapMessages.size();
		int pending = responseData.getTotalSoapExecutionPending();
		
		return ((total - pending) * 100) / total;
	}
	
	@RequestMapping(value = "/wwsIsAllComplete", method = RequestMethod.GET, headers = "Accept=application/json")
	public Boolean wwsIsAllComplete() {
		return responseData.isAllComplete();
	}
	
	@RequestMapping(value = "/publishResult", method = RequestMethod.POST, headers = "Accept=application/json")
	public void publishResult() {
		if(section == null) {
			return;
		}
		Result result = new Result();
		result.setSection(section);
		result.setTotalRecords(soapMessages.size());
		result.setTotalSuccess(responseData.getSuccessCount());
		result.setTotalFailures(responseData.getFailureCount());
		result.setLoadDate(new Date(System.currentTimeMillis()));
		try {
			result.setWsResponseData(wsResponseToByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		resultService.addResult(result);
	}
	
	public byte[] wsResponseToByteArray() throws IOException {
		FileOutputStream fos = new FileOutputStream("t.tmp");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(wsResponse);
		oos.close();
		fos.close();
		
		FileInputStream fis = new FileInputStream("t.tmp");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        for (int readNum; (readNum = fis.read(buf)) != -1;) {
            bos.write(buf, 0, readNum); //no doubt here is 0
        }
        byte[] bytes = bos.toByteArray();
        fis.close();
        return bytes;
	}
		
	@RequestMapping(value = "/getWWSResponseStatus", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseDataUtil getWWSResponseStatus() {
		
		responseData.setTotalBatches(responseData.getBatchCount());
		responseData.setTotalRecords(soapMessages.size());
		responseData.setCurrentRunningBatch(responseData.getRunningbatchNumber());
		responseData.setTotalSuccess(responseData.getSuccessCount());
		responseData.setTotalFailures(responseData.getFailureCount());
		return responseData;
	}
	
	@RequestMapping(value = "/stopSWExecution", method = RequestMethod.GET, headers = "Accept=application/json")
	public void stopSWExecution(HttpSession session) {

		batchRequest.stopRequest();
	}
	
	@RequestMapping(value = "/getWWSXmlFiles", method = RequestMethod.GET, headers = "Accept=application/json")
	public void getWWSXmlFiles(HttpServletResponse response) {
		
		List<SOAPMessage> soapMessageList = new ArrayList<>();
		SOAPRequestBuilder soapRequestBuilder = new SOAPRequestBuilder();
		Map<String, FileData> fileDataMap = null;
		SOAPMessage soapMessage = null;
		
		DataElement dataRoot = loadDataRules.getDataRules(operation, false);
		String rowIdColumnName = dataRoot.getRule().getRowIdColumnName();
		
		if (rowIdColumnName != null) {
			fileDataMap = loadDataRules.getFileDataMap(operation, dataRoot.getRule());
		} else {
			fileDataMap = loadDataRules.getFileDataMap(operation);
		}
		
		ProcessRules processRules = new ProcessRules(fileDataMap, dataRoot.getRule().getFileType());
		
		if (dataRoot.getRule() != null && dataRoot.getRule().isMultiple()) {
			List<DataElement> dataRules = processRules.populateElements(dataRoot);
			
			for (DataElement dataElement : dataRules) {
				soapMessage = soapRequestBuilder.buildSOAPRequest(dataElement);
				soapMessageList.add(soapMessage);
			}
		} else {
			DataElement topElement = processRules.populateElement(dataRoot);
			soapMessage = soapRequestBuilder.buildSOAPRequest(topElement);
			soapMessageList.add(soapMessage);
		}
		
		List<File> fileList = getXmlFileList(soapMessageList, operation.getRuleName());
		generateZipFile(response, fileList, operation.getRuleName());
	}
	
	@RequestMapping(value = "/getWWSErrorDataFiles", method = RequestMethod.GET, headers = "Accept=application/json")
	public void getWWSErrorDataFiles(HttpServletResponse response) {
		
		int failureCount = responseStatus.getStatus(wsResponse, STATUS_FAILUE);
		DataElement dataRoot = loadDataRules.getDataRules(operation, false);
		final boolean isCSVFileType = dataRoot.getRule().getFileType() == null
                || dataRoot.getRule().getFileType().trim().equals("")
                || dataRoot.getRule().getFileType().equalsIgnoreCase("CSV");
		
		if(failureCount > 0 && isCSVFileType) {
		    List<File> fileList = new ArrayList<>();
		    String uniqueIdColumnName = dataRoot.getRule().getUniqueID();
		    List<WriteCSVFile> csvFileWriters = new ArrayList<WriteCSVFile>();
		    Set<String> filteredData = responseStatus.getErrorIdentifiers(wsResponse);
		
		    for (FileData aFileData : fileDataAll.values()) {
			    csvFileWriters.add(new WriteCSVFile(aFileData, uniqueIdColumnName, filteredData));
		    }
		    for (WriteCSVFile csvFile : csvFileWriters) {
			    fileList.add(csvFile.execute());
		    }
		    
		    WriteCSVFile csvFile = new WriteCSVFile();
		    fileList.add(csvFile.writeValidationErrorMessages(wsResponse, uniqueIdColumnName));
		    generateZipFile(response, fileList, ERROR_FILE);
		}
	}
	
	@RequestMapping(value = "/getWSXmlFiles/{resId}", method = RequestMethod.GET, headers = "Accept=application/json")
	public void getWSXmlFiles(@PathVariable("resId") Long resId, HttpServletResponse response, HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		Result result = resultService.getResult(resId);
		String operationName = result.getSection().getOperationName();
		Operation operation = operationService.getOperation(operationName, userId);
		
		List<SOAPMessage> soapMessageList = new ArrayList<>();
		SOAPRequestBuilder soapRequestBuilder = new SOAPRequestBuilder();
		Map<String, FileData> fileDataMap = null;
		SOAPMessage soapMessage = null;
		
		DataElement dataRoot = loadDataRules.getDataRules(operation, false);
		String rowIdColumnName = dataRoot.getRule().getRowIdColumnName();
		
		if (rowIdColumnName != null) {
			fileDataMap = loadDataRules.getFileDataMap(operation, dataRoot.getRule());
		} else {
			fileDataMap = loadDataRules.getFileDataMap(operation);
		}
		
		ProcessRules processRules = new ProcessRules(fileDataMap, dataRoot.getRule().getFileType());
		
		if (dataRoot.getRule() != null && dataRoot.getRule().isMultiple()) {
			List<DataElement> dataRules = processRules.populateElements(dataRoot);
			
			for (DataElement dataElement : dataRules) {
				soapMessage = soapRequestBuilder.buildSOAPRequest(dataElement);
				soapMessageList.add(soapMessage);
			}
		} else {
			DataElement topElement = processRules.populateElement(dataRoot);
			soapMessage = soapRequestBuilder.buildSOAPRequest(topElement);
			soapMessageList.add(soapMessage);
		}
		
		List<File> fileList = getXmlFileList(soapMessageList, operation.getRuleName());
		generateZipFile(response, fileList, operation.getRuleName());		
	}
	
	@RequestMapping(value = "/getWSErrorDataFiles/{resId}", method = RequestMethod.GET, headers = "Accept=application/json")
	public void getWSErrorDataFiles(@PathVariable("resId") Long resId, HttpServletResponse response, HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		
		Result result = resultService.getResult(resId);
		List<WSResponse> responses = getWsReponsesFromResult(result);
		if(responses == null) {
			return;
		}
		String operationName = result.getSection().getOperationName();
		Operation operation = operationService.getOperation(operationName, userId);
		
		int failureCount = responseStatus.getStatus(responses, STATUS_FAILUE);
		DataElement dataRoot = loadDataRules.getDataRules(operation, false);
		
		final boolean isCSVFileType = dataRoot.getRule().getFileType() == null
                || dataRoot.getRule().getFileType().trim().equals("")
                || dataRoot.getRule().getFileType().equalsIgnoreCase("CSV");

		Map<String, FileData> fileDataMap = null;
		String rowIdColumnName = dataRoot.getRule().getRowIdColumnName();
		
		if (rowIdColumnName != null) {
			fileDataMap = loadDataRules.getFileDataMap(operation, dataRoot.getRule());
		} else {
			fileDataMap = loadDataRules.getFileDataMap(operation);
		}
		
		if(failureCount > 0 && isCSVFileType) {
		    List<File> fileList = new ArrayList<>();
		    String uniqueIdColumnName = dataRoot.getRule().getUniqueID();
		    List<WriteCSVFile> csvFileWriters = new ArrayList<WriteCSVFile>();
		    Set<String> filteredData = responseStatus.getErrorIdentifiers(responses);
		
		    for (FileData aFileData : fileDataMap.values()) {
			    csvFileWriters.add(new WriteCSVFile(aFileData, uniqueIdColumnName, filteredData));
		    }
		    for (WriteCSVFile csvFile : csvFileWriters) {
			    fileList.add(csvFile.execute());
		    }
		    
		    WriteCSVFile csvFile = new WriteCSVFile();
		    fileList.add(csvFile.writeValidationErrorMessages(responses, uniqueIdColumnName));
		    generateZipFile(response, fileList, ERROR_FILE);
		}
	}
	
	@SuppressWarnings("unchecked")
	private List<WSResponse> getWsReponsesFromResult(Result result) {
		ByteArrayInputStream bis = new ByteArrayInputStream(result.getWsResponseData());
		List<WSResponse> responses = null;
		try {
			ObjectInputStream ois = new ObjectInputStream(bis);
			responses = (List<WSResponse>) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return responses;
	}
	
	private Section getSection(Page page, Operation operation) {
		List<Section> sections = page.getSections();
		for(Section section : sections) {
			if(section.getOperationName().equals(operation.getOperationName())) {
				return section;
			}
		}
		return null;
	}
	
	private int getBatchRequestSize(int dataCount) {
		int batchReqSize = 0;
		
		if(dataCount >= 5000) {
			batchReqSize = (dataCount / 100) + ((dataCount % 100) > 0 ? 1 : 0);
		} else if(dataCount >= 3000 && dataCount < 5000) {
			batchReqSize = (dataCount / 70) + ((dataCount % 70) > 0 ? 1 : 0);
		} else if(dataCount >= 1000 && dataCount < 3000) {
			batchReqSize = (dataCount / 40) + ((dataCount % 40) > 0 ? 1 : 0);
		} else if(dataCount >= 100 && dataCount < 1000) {
			batchReqSize = (dataCount / 20) + ((dataCount % 20) > 0 ? 1 : 0);
		} else if(dataCount >= 10 && dataCount < 100) {
			batchReqSize = (dataCount / 10) + ((dataCount % 10) > 0 ? 1 : 0);
		} else if (dataCount < 10) {
			batchReqSize = 3;
		}
		
		return batchReqSize;		
	}
	
	private List<File> getXmlFileList(List<SOAPMessage> soapMessageList, String ruleName) {
		
		List<File> fileList = new ArrayList<>();
		int index = 1;
		
		try {
		    for (SOAPMessage soapMessage : soapMessageList) {
			    String prefix = ruleName + "-" + index + "_";
			    File file = File.createTempFile(prefix, ".xml");
			    FileOutputStream fos = new FileOutputStream(file);
			    DataOutputStream dos = new DataOutputStream(fos);
			    soapMessage.writeTo(dos);
			    dos.flush();
			    dos.close();
			    fos.close();
			    fileList.add(file);
			    index++;
		    }
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return fileList;
	}
	
	private void generateZipFile(HttpServletResponse response, List<File> fileList, String fileName) {
		
        FileOutputStream fos = null;
        ZipOutputStream zipOut = null;
        FileInputStream fis = null;        
        File zipFile = null;

        try {
        	zipFile = File.createTempFile(fileName, ".zip");
        	fos = new FileOutputStream(zipFile);
            zipOut = new ZipOutputStream(new BufferedOutputStream(fos));
			for (File file : fileList) {
	            fis = new FileInputStream(file);
	            ZipEntry ze = new ZipEntry(file.getName());
                zipOut.putNextEntry(ze);
                byte[] tmp = new byte[4*1024];
                int size = 0;
                while((size = fis.read(tmp)) != -1) {
                    zipOut.write(tmp, 0, size);
                }
                zipOut.flush();
                fis.close();
            }
			zipOut.close();
            
            response.setContentType("APPLICATION/OCTET-STREAM");
            response.setHeader("Content-Disposition","attachment; filename=" + fileName + ".zip" + "");

            OutputStream out = response.getOutputStream();
            FileInputStream in = new FileInputStream(zipFile);
            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) > 0) {
               out.write(buffer, 0, length);
            }
            out.flush();
            out.close();
            in.close();
		} 
        catch (IOException e) {
			e.printStackTrace();
		}        
	}
	
	private static class Sum extends RecursiveAction {

		private static final long serialVersionUID = 1L;
			int low;
		      int high;
		      int[] array;
		      int reqSize;
		      List<DataElement> dataRules;
		      SOAPRequestBuilder soapRequestBuilder;
		      Map<String, SOAPMessage> soapMessages;

		      Sum(int[] array, int low, int high, int reqSize, List<DataElement> dataRules, 
		    	  SOAPRequestBuilder soapRequestBuilder, Map<String, SOAPMessage> soapMessages) {
		         this.array = array;
		         this.low   = low;
		         this.high  = high;
		         this.reqSize = reqSize;
		         this.dataRules = dataRules;
		         this.soapRequestBuilder = soapRequestBuilder;
		         this.soapMessages = soapMessages;
		      }

		      protected void compute() {
		         
		         if(high - low <= reqSize) 
		         {		            
		            for(int i = low; i < high; ++i) 
		            {
		            	DataElement dataElement = dataRules.get(i);
		            	SOAPMessage soapMessage = soapRequestBuilder.buildSOAPRequest(dataElement);
						soapMessages.put(dataElement.getIdentifierValue(),soapMessage);
		            }
		         } 
		         else 
		         {	    	
		            int mid = low + (high - low) / 2;
		            Sum left  = new Sum(array, low, mid, reqSize, dataRules, soapRequestBuilder, soapMessages);
		            Sum right = new Sum(array, mid, high, reqSize, dataRules, soapRequestBuilder, soapMessages);
		            left.fork();
		            right.compute();
		            left.join();
		         }
		      }
		   }

}
