package com.ibm.workday.automation.controller;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.json.JSONArray;
import org.apache.commons.json.JSONException;
import org.apache.commons.json.JSONObject;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ibm.workday.automation.common.CommonConstants;
import com.ibm.workday.automation.model.GetRequest;
import com.ibm.workday.automation.model.MapFile;
import com.ibm.workday.automation.model.Operation;
import com.ibm.workday.automation.model.Page;
import com.ibm.workday.automation.model.PostLoad;
import com.ibm.workday.automation.model.Result;
import com.ibm.workday.automation.model.Section;
import com.ibm.workday.automation.model.Tenant;
import com.ibm.workday.automation.model.User;
import com.ibm.workday.automation.operation.DataElement;
import com.ibm.workday.automation.operation.HttpBasicAuthentication;
import com.ibm.workday.automation.operation.LoadDataRules;
import com.ibm.workday.automation.operation.ReportElement;
import com.ibm.workday.automation.operation.XmlParserManager;
import com.ibm.workday.automation.service.GetRequestService;
import com.ibm.workday.automation.service.MapFileService;
import com.ibm.workday.automation.service.OperationService;
import com.ibm.workday.automation.service.PageService;
import com.ibm.workday.automation.service.PostLoadService;
import com.ibm.workday.automation.service.SectionService;
import com.ibm.workday.automation.service.TenantService;
import com.ibm.workday.automation.service.UserService;

import au.com.bytecode.opencsv.CSVReader;

@RestController
public class PostLoadController implements CommonConstants{
	
	Tenant tenant;
	
	@Autowired
	TenantService tenantService;
	
	@Autowired
	PostLoadService postLoadService;
	
	@Autowired
	GetRequestService getRequestService;
	
	@Autowired
	PageService pageService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	OperationService operationService;
	
	@Autowired
	MapFileService mapFileService;
	
	@Autowired
	LoadDataRules loadDataRules;
	
	@Autowired
	SectionService sectionService;
	
	Operation operation;
	
	String currentCSVfileName = null;
	
	String oldCSVFileName = "";
	
	String uniqueIdVal = null;
	
	Map<String, List<String>> csvFileMap = new HashMap<>();
	
	Map<String, String> sourceEntryMap = new HashMap<String, String>();
	Map<String, String> sourceTenantRowMap = new HashMap<String, String>();
	Map<String, String> targetTenantRowMap = new HashMap<String, String>();
	
	//private List<String> sourceTenantColumnList = new ArrayList<String>();
	//private List<String> targetTenantColumnList = new ArrayList<String>();
	
	List<String> csvValueList = null;
	
	private static final String NAMESPACE_URL = "urn:com.workday/bsvc";
	private static final String NAMESPACE_PREFIX = "bsvc";
	private static final String HEADER_SECURITY_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	private static final String HEADER_SECURITY_NS_PREFIX = "wsse";
	private static final String PASSWORD_TYPE_ATTR_VALUE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText";
	private static final String EMPLOYEE_APPLICANT_MAPPING_RESPONSE_EXPRESSION = "/Report_Data";
	//private int count = 0;
	private String headingFromWD = "";
	private String headingFromSource = "";
	//private String headingFromSourceTenant = "";
	//private String headingFromTargetTenant = "";
	protected DocumentBuilderFactory domFactory = null;
	protected DocumentBuilder domBuilder = null;
	private List<String> columnList = new ArrayList<String>();
	private List<String> errorList = new ArrayList<String>();
	//private List<String> postloadErrorList = new ArrayList<String>();
	private List<String> wdColumnList = new ArrayList<String>();
	private List<String> idTypeList = new ArrayList<String>();
	private StringBuffer sbFinal = null;
	private Map<String, String> ccMap = new HashMap<>();
	//private String finalCSVHeader = "";
	private int sourceCount = 0;
	private int wdCount = 0;
	private int mismatchCount = 0;
	boolean complete = false;
	JSONArray headingWd = null;
	private byte[] sourceContent = null;
	private byte[] targetContent = null;
	private byte[] sourceXMLContent = null;
	private byte[] targetXMLContent = null;
	
	private String GET_APPLICANT_REQUEST_FILE = "";
	private String GET_POSITION_REQUEST_FILE = "";
	private String GET_HIRE_REQUEST_FILE = "";
	private String GET_HIRE_CW_REQUEST_FILE = "";
	private String GET_TERMINATION_REQUEST_FILE = "";
	private String GET_WORKER_ADDRESS_REQUEST_FILE = "";
	private String GET_END_CONTINGENT_WORKER_FILE = "";
	private String GET_WORKER_BIOGRAPHIC_FILE = "";
	private String GET_WORKER_DEMOGRAPHIC_FILE = "";
	private String GET_WORKER_SERVICE_DATES = "";
	private String GET_APPLICANT_PHONE_FILE = "";
	private String GET_LEAVE_OF_ABSENCE_FILE = "";
	private String GET_EE_BASE_COMPENSATION_FILE = "";
	private String GET_BONUS_PLAN_FILE = "";
	private String GET_ALLOWANCE_PLAN_FILE = "";
	private String GET_STOCK_PLAN_FILE = "";
	private String GET_MERIT_PLAN_FILE = "";
	private String GET_PAY_GROUP_FILE = "";
	private String GET_GOVERNMENT_ID_FILE = "";
	private String GET_WORKER_PHOTO_FILE = "";
	private String GET_PERFORMANCE_REVIEW_FILE = "";
	private String GET_COST_CENTER_REQUEST_FILE = "";
	private String GET_SUP_ORG_REQUEST_FILE = "";
	private String GET_COST_CENTER_HIERARCHY_REQUEST_FILE = "";
	private String GET_COMPANY_HIERARCHY_REQUEST_FILE = "";
	private String GET_ASSIGN_ORG_ROLE_REQUEST_FILE = "";
	private String GET_HOST_CNUM_REQUEST_FILE = "";
	private String GET_CARRYOVER_BALANCE_REQUEST_FILE = "";
	private String GET_SYSTEM_USER_ACCOUNT_REQUEST_FILE = "";
	private String GET_MATRIX_ORGANIZATION_REQUEST_FILE = "";
	private String GET_EDIT_WORKER_ADDN_DATA_REQUEST_FILE = "";
	private String GET_START_INTERNATIONAL_ASSIGNMENT_REQUEST_FILE = "";
	private String GET_END_INTERNATIONAL_ASSIGNMENT_REQUEST_FILE = "";
	private String GET_EMPLOYEE_CONTRACT_REQUEST_FILE = "";
	private String GET_PAYEE_TAX_CODE_FILE = "";
	private String GET_UK_PAYROLL_ID_REQUEST_FILE = "";
	private String GET_PAYROLL_PAYEE_NI_DATA_FILE = "";
	private String GET_CHANGE_BENEFITS_LIFE_EVENT_REQUEST_FILE = "";
	private String GET_ASSIGN_ORG_FILE = "";
	private String  GET_LOCATION_REQUEST_FILE = "";
	private String GET_PAYEE_INPUT_DATA_REQUEST_FILE = "";
	private String GET_ESTABLISHMENT_REQUEST_FILE = "";
	
	private JSONArray createCSVFromWDCompanyHierarchy(Tenant tenant, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_COMPANY_HIERARCHY_REQUEST_FILE = requestfile.getAbsolutePath();
				 //String outputfile = GET_COMPANY_HIERARCHY_REQUEST_FILE;
				 String outputfile = addCostCenterIdList(GET_COMPANY_HIERARCHY_REQUEST_FILE, columnList, ruleName, startIndex, columnList.size(), "Organization_Reference_ID");

				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");			 
				 
				 ReportElement soapResp = XmlParserManager.parseXml(strMsg);
				 
				 ReportElement pageData = soapResp.getChild("env:Body")
							.getChild("wd:Get_Organizations_Response")
							.getChild("wd:Response_Results");
				 
				 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
				 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
				 int totalResult = Integer.parseInt(totalResults);
				 System.out.println("totalNoOfPages-"+totalNoOfPages);
				 System.out.println("totalResult-"+totalResult);
				 wdCount = wdCount + totalResult;
				 
				 String finalStr = "";
				 String headerStr = "";
				 String compHierarchyId = "";
				 String compHierarchyName = "";
				 String compSuperiorHierarchyId = "";
				 String compSuperiorHierarchyName = "";
				 
				 Map<String,String> compHierarchyIdMap = null;
				 
				 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
				 {					 
					 if(j == 1)
					 {
						startIndex = 0;
						if(999 > totalResult)
						{
							endIndex = totalResult;
						}
						else
						{
							endIndex = j*999;
						}
					 }
					 else
					 {
						int lastVal = (j - 1);
						startIndex = (endIndex - lastVal);
						if(j*999 > totalResult)
						{
							endIndex = totalResult;
						}
						else
						{
							endIndex = (j*startIndex) + 1;
						}
					 }
					 outputfile = addCostCenterIdList(GET_COMPANY_HIERARCHY_REQUEST_FILE, columnList, ruleName, startIndex, endIndex, "Organization_Reference_ID");
					 is = new FileInputStream(outputfile);
				     soapMessage = MessageFactory.newInstance().createMessage(null, is);
				     soapPart = soapMessage.getSOAPPart();
				     envelope = soapPart.getEnvelope();
					 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
					 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
					 {
							envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
					 }
					 soapMessage.saveChanges();
			         out = new ByteArrayOutputStream();
			         soapMessage.writeTo(out);
			         strMsg = new String(out.toByteArray());
			        
			         soapConnectionFactory = SOAPConnectionFactory.newInstance();
					 soapConnection = soapConnectionFactory.createConnection();
			         soapResponse = soapConnection.call(soapMessage, sourceUrl);
			         out = new ByteArrayOutputStream();
			         soapResponse.writeTo(out);
			         strMsg = new String(out.toByteArray(), "utf-8");
			        
			         soapResp = XmlParserManager.parseXml(strMsg);
				 
					 ReportElement responseData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Organizations_Response")
								.getChild("wd:Response_Data");
					 
					 List<ReportElement> orgData = responseData.getChildren("wd:Organization");
						
					 for(ReportElement reportElement : orgData)
					 {
						 ReportElement element1 = reportElement.getChild("wd:Organization_Data");
						 if(element1 != null)
						 {
							 compHierarchyId = element1.getChild("wd:Reference_ID") != null?element1.getChild("wd:Reference_ID").getValue().trim():"";
							 compHierarchyName = element1.getChild("wd:Name") != null?element1.getChild("wd:Name").getValue().trim():"";
							 if(compHierarchyName.contains(","))
							 {
								 compHierarchyName =  compHierarchyName.replace(",", "|");
							 }
							 
							 ReportElement element2 = element1.getChild("wd:Hierarchy_Data");
							 if(element2 != null)
							 {
								 ReportElement element3 = element2.getChild("wd:Superior_Organization_Reference");
								 if(element3 != null)
								 {									 
									 List<ReportElement> ccIdData = element3.getChildren("wd:ID");					 
									 for(ReportElement ccIdElement:ccIdData)
									 {
										 compHierarchyIdMap = ccIdElement.getAllAttributes();
										 if(compHierarchyIdMap.get("wd:type").equals("Organization_Reference_ID"))
										 {
											 compSuperiorHierarchyId = ccIdElement.getValue().trim();
											 compSuperiorHierarchyName = getSuperiorHierarchyName(GET_COMPANY_HIERARCHY_REQUEST_FILE, compSuperiorHierarchyId, "Organization_Reference_ID");
											 if(compSuperiorHierarchyName.contains(","))
											 {
												 compSuperiorHierarchyName =  compSuperiorHierarchyName.replace(",", "|");
											 }												
										 }
									 }										
								 }
								 else
								 {
									 compSuperiorHierarchyId = "";
									 compSuperiorHierarchyName = "";
								 }
							 }
							 
							 headingFromWD = "Company_Hierarchy_ID,Company_Hierarchy_Name,Superior_Company_Hierarchy_ID,Superior_Company_Hierarchy_Name";							 
							 headerStr = compHierarchyId + "," + compHierarchyName + "," + compSuperiorHierarchyId + "," + compSuperiorHierarchyName;
							 
							 if(finalStr.equals(""))
							 {
								 finalStr = headingFromWD + "\n" + headerStr;
							 }
							 else
							 {
								 finalStr = finalStr + "\n" + headerStr;
							 }
						 }					 
					 }
				 }
				 
				 System.out.println(finalStr);
				 
				 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
				 PrintWriter writer = new PrintWriter(wdCSVfile);
				 writer.write(finalStr.toString());
				 writer.flush();
				 writer.close();
				 
				 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
				 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
				 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
				 postLoadService.updatePostLoad(postLoad);
				 
				 headingWd = selectColumnMapping(loadCycle, ruleName, "Company_Hierarchy_ID");
				 
				 /*String wdCSVfile1 = "config/" + loadCycle + "_" + ruleName + ".csv";			 
				 PrintWriter writer1 = new PrintWriter(new File(wdCSVfile1));
				 writer1.write(finalStr.toString());
				 writer1.flush();
				 writer1.close();*/
			 }
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}
	
	private JSONArray createCSVFromWDCostCenterHierarchy(Tenant tenant, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		//String faultStr = null;
		String checkFile = null;
		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_COST_CENTER_HIERARCHY_REQUEST_FILE = requestfile.getAbsolutePath();
				 //String outputfile = GET_COST_CENTER_HIERARCHY_REQUEST_FILE;
				 //columnList.removeAll(errorList);
				 if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addMasterDataListToFindError(GET_COST_CENTER_HIERARCHY_REQUEST_FILE, columnList.get(i), ruleName, "Organization_Reference_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement reportElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = reportElement.getChild("env:Body")
										.getChild("wd:Get_Organizations_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
						 }
					 }
					 columnList.removeAll(errorList);
				 }
				 String outputfile = addCostCenterIdList(GET_COST_CENTER_HIERARCHY_REQUEST_FILE, columnList, ruleName, startIndex, columnList.size(), "Organization_Reference_ID");

				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");
				 
				 /*if(strMsg.contains("faultstring"))
				 {
					 ReportElement msgResponse = XmlParserManager.parseXml(strMsg);
					 ReportElement elementData = msgResponse.getChild("SOAP-ENV:Body")
								.getChild("SOAP-ENV:Fault");
					 faultStr = elementData.getChild("faultstring").getValue().trim();
					 System.out.println("faultStr-"+faultStr);
				 }*/
				 
				 //if(faultStr == null)
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Organizations_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String finalStr = "";
					 String headerStr = "";
					 String ccHierarchyId = "";
					 String ccHierarchyName = "";
					 String ccSuperiorHierarchyId = "";
					 String ccSuperiorHierarchyName = "";
					 
					 Map<String,String> ccHierarchyIdMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {					 
						 if(j == 1)
						 {
							startIndex = 0;
							if(999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*999;
							}
						 }
						 else
						 {
							startIndex = endIndex;
							if(j*999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = (j*999);
							}
						 }
						 outputfile = addCostCenterIdList(GET_COST_CENTER_HIERARCHY_REQUEST_FILE, columnList, ruleName, startIndex, endIndex, "Organization_Reference_ID");
						 is = new FileInputStream(outputfile);
					     soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     soapPart = soapMessage.getSOAPPart();
					     envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
				         out = new ByteArrayOutputStream();
				         soapMessage.writeTo(out);
				         strMsg = new String(out.toByteArray());
				        
				         soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
				         soapResponse = soapConnection.call(soapMessage, sourceUrl);
				         out = new ByteArrayOutputStream();
				         soapResponse.writeTo(out);
				         strMsg = new String(out.toByteArray(), "utf-8");
				        
				         soapResp = XmlParserManager.parseXml(strMsg);
					 
						 ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Organizations_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> orgData = responseData.getChildren("wd:Organization");
							
						 for(ReportElement reportElement : orgData)
						 {
							 ReportElement element1 = reportElement.getChild("wd:Organization_Data");
							 if(element1 != null)
							 {
								 ccHierarchyId = element1.getChild("wd:Reference_ID") != null?element1.getChild("wd:Reference_ID").getValue().trim():"";
								 ccHierarchyName = element1.getChild("wd:Name") != null?element1.getChild("wd:Name").getValue().trim():"";
								 if(ccHierarchyName.contains(","))
								 {
									 ccHierarchyName =  ccHierarchyName.replace(",", "|");
								 }
								 
								 ReportElement element2 = element1.getChild("wd:Hierarchy_Data");
								 if(element2 != null)
								 {
									 ReportElement element3 = element2.getChild("wd:Superior_Organization_Reference");
									 if(element3 != null)
									 {									 
										 List<ReportElement> ccIdData = element3.getChildren("wd:ID");					 
										 for(ReportElement ccIdElement:ccIdData)
										 {
											 ccHierarchyIdMap = ccIdElement.getAllAttributes();
											 if(ccHierarchyIdMap.get("wd:type").equals("Organization_Reference_ID"))
											 {
												 ccSuperiorHierarchyId = ccIdElement.getValue().trim();
												 ccSuperiorHierarchyName = getSuperiorHierarchyName(GET_COST_CENTER_HIERARCHY_REQUEST_FILE, ccSuperiorHierarchyId, "Organization_Reference_ID");
												 if(ccSuperiorHierarchyName.contains(","))
												 {
													 ccSuperiorHierarchyName = ccSuperiorHierarchyName.replace(",", "|");
												 }												
											 }
										 }										
									 }
									 else
									 {
										 ccSuperiorHierarchyId = "";
										 ccSuperiorHierarchyName = "";
									 }
								 }
								 
								 headingFromWD = "Cost_Center_Hierarchy_ID,Cost_Center_Hierarchy_Name,Superior_Cost_Center_Hierarchy_ID,Superior_Cost_Center_Hierarchy_Name";							 
								 headerStr = ccHierarchyId + "," + ccHierarchyName + "," + ccSuperiorHierarchyId + "," + ccSuperiorHierarchyName;
								 
								 if(finalStr.equals(""))
								 {
									 finalStr = headingFromWD + "\n" + headerStr;
								 }
								 else
								 {
									 finalStr = finalStr + "\n" + headerStr;
								 }
							 }					 
						 }
					 }
					 
					 System.out.println(finalStr);
					 
					 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
					 PrintWriter writer = new PrintWriter(wdCSVfile);
					 writer.write(finalStr.toString());
					 writer.flush();
					 writer.close();
					 
					 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
					 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
					 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
					 postLoadService.updatePostLoad(postLoad);
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Cost_Center_Hierarchy_ID");
					 
					 /*String wdCSVfile1 = "config/" + loadCycle + "_" + ruleName + ".csv";			 
					 PrintWriter writer1 = new PrintWriter(new File(wdCSVfile1));
					 writer1.write(finalStr.toString());
					 writer1.flush();
					 writer1.close();*/
				 }
				 /*else
				 {
					 headingWd = new JSONArray();
					 JSONObject obj = new JSONObject();
					 obj.put("errorMsg", faultStr.substring(faultStr.indexOf("."), faultStr.length()));
					 headingWd.put(obj); 
				 }*/
			 }
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}

	private JSONArray createCSVFromWDSupervisoryOrganization(Tenant tenant, InputStream is,
			SOAPConnection soapConnection, int startIndex, int endIndex, JSONArray headingWd, String loadCycle,
			String ruleName, String client) {

		targetContent = null;
		String checkFile = null;
		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_SUP_ORG_REQUEST_FILE = requestfile.getAbsolutePath();
				 //String outputfile = GET_SUP_ORG_REQUEST_FILE;
				 //columnList.removeAll(errorList);
				 if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addMasterDataListToFindError(GET_SUP_ORG_REQUEST_FILE, columnList.get(i), ruleName, "Organization_Reference_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement reportElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = reportElement.getChild("env:Body")
										.getChild("wd:Get_Organizations_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
						 }
					 }
					 columnList.removeAll(errorList);
				 }
				 String outputfile = addCostCenterIdList(GET_SUP_ORG_REQUEST_FILE, columnList, ruleName, startIndex, columnList.size(), "Organization_Reference_ID");

				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");
				 
				 /*if(strMsg.contains("faultstring"))
				 {
					 ReportElement msgResponse = XmlParserManager.parseXml(strMsg);
					 ReportElement elementData = msgResponse.getChild("SOAP-ENV:Body")
								.getChild("SOAP-ENV:Fault");
					 faultStr = elementData.getChild("faultstring").getValue().trim();
					 System.out.println("faultStr-"+faultStr);
				 }*/
				 
				 //if(faultStr == null)
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Organizations_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String finalStr = "";
					 String headerStr = "";
					 String orgRefId = "";
					 String supOrgCode = "";
					 String supOrgName = "";
					 String managerId = "";
					 String locationId = "";
					 String staffingModel = "";
					 String subType = "";
					 String superiorSupOrgId = "";
					 String superiorSupOrgName = "";
					 
					 Map<String,String> managerMap = null;
					 Map<String,String> subTypeMap = null;
					 Map<String,String> locationMap = null;
					 Map<String,String> superiorIdMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {					 
						 if(j == 1)
						 {
							startIndex = 0;
							if(999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*999;
							}
						 }
						 else
						 {
							startIndex = endIndex;
							if(j*999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = (j*999);
							}
						 }
						 outputfile = addCostCenterIdList(GET_SUP_ORG_REQUEST_FILE, columnList, ruleName, startIndex, endIndex, "Organization_Reference_ID");
						 is = new FileInputStream(outputfile);
					     soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     soapPart = soapMessage.getSOAPPart();
					     envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
				         out = new ByteArrayOutputStream();
				         soapMessage.writeTo(out);
				         strMsg = new String(out.toByteArray());
				        
				         soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
				         soapResponse = soapConnection.call(soapMessage, sourceUrl);
				         out = new ByteArrayOutputStream();
				         soapResponse.writeTo(out);
				         strMsg = new String(out.toByteArray(), "utf-8");
				        
				         soapResp = XmlParserManager.parseXml(strMsg);
					 
						 ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Organizations_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> orgData = responseData.getChildren("wd:Organization");
							
						 for(ReportElement reportElement : orgData)
						 {
							 ReportElement element1 = reportElement.getChild("wd:Organization_Data");
							 if(element1 != null)
							 {
								 orgRefId = element1.getChild("wd:Reference_ID") != null?element1.getChild("wd:Reference_ID").getValue().trim():"";
								 supOrgCode = element1.getChild("wd:Organization_Code") != null?element1.getChild("wd:Organization_Code").getValue().trim():"";
								 supOrgName = element1.getChild("wd:Name") != null?element1.getChild("wd:Name").getValue().trim():"";
								 
								 ReportElement element2 = element1.getChild("wd:Organization_Subtype_Reference");
								 if(element2 != null)
								 {
									 List<ReportElement> subTypeData = element2.getChildren("wd:ID");					 
									 for(ReportElement subTypeElement:subTypeData)
									 {
										 subTypeMap = subTypeElement.getAllAttributes();
										 if(subTypeMap.get("wd:type").equals("Organization_Subtype_ID"))
										 {
											 subType = subTypeElement.getValue().trim();
										 }
									 }
								 }
								 
								 ReportElement element3 = element1.getChild("wd:Manager_Reference");
								 if(element3 != null)
								 {
									 List<ReportElement> managerData = element3.getChildren("wd:ID");					 
									 for(ReportElement managerElement:managerData)
									 {
										 managerMap = managerElement.getAllAttributes();
										 if(managerMap.get("wd:type").equals("Employee_ID"))
										 {
											 managerId = managerElement.getValue().trim();
										 }
									 }
								 }
								 
								 ReportElement element4 = element1.getChild("wd:Supervisory_Data");
								 if(element4 != null)
								 {
									 staffingModel = element4.getChild("wd:Staffing_Model") != null?element4.getChild("wd:Staffing_Model").getValue().trim():"";
									 
									 ReportElement element5 = element4.getChild("wd:Location_Reference");
									 if(element5 != null)
									 {
										 List<ReportElement> locationData = element5.getChildren("wd:ID");					 
										 for(ReportElement locationElement:locationData)
										 {
											 locationMap = locationElement.getAllAttributes();
											 if(locationMap.get("wd:type").equals("Location_ID"))
											 {
												 locationId = locationElement.getValue().trim();
											 }
										 }
									 }
								 }
								 
								 ReportElement element6 = element1.getChild("wd:Hierarchy_Data");
								 if(element6 != null)
								 {
									 ReportElement element7 = element6.getChild("wd:Superior_Organization_Reference");
									 if(element7 != null)
									 {
										 List<ReportElement> supIdData = element7.getChildren("wd:ID");					 
										 for(ReportElement supIdElement:supIdData)
										 {
											 superiorIdMap = supIdElement.getAllAttributes();
											 if(superiorIdMap.get("wd:type").equals("Organization_Reference_ID"))
											 {
												 superiorSupOrgId = supIdElement.getValue().trim();
											 }
										 }
									 }
								 }
								 
								 if(superiorSupOrgId.length() > 0)
								 {
									 superiorSupOrgName = getSuperiorHierarchyName(GET_SUP_ORG_REQUEST_FILE, superiorSupOrgId, "Organization_Reference_ID");
								 }
								 
								 headingFromWD = "Organization_Reference_ID,Supervisory_Organization_Code,Supervisory_Organization_Name,Sup_Org_Location_ID,Manager_ID,Superior_Sup_Org_ID,Superior_Sup_Org_Name,Staffing_Model,Sub_Type";
								 if(orgRefId.length() > 0)
								 {
									 headerStr = orgRefId + "," + supOrgCode + "," + supOrgName + "," + locationId + "," + managerId + "," + superiorSupOrgId + "," + superiorSupOrgName + "," + staffingModel + "," + subType;
									 if(finalStr.equals(""))
									 {
										 finalStr = headingFromWD + "\n" + headerStr;
									 }
									 else
									 {
										 finalStr = finalStr + "\n" + headerStr;
									 }
								 }							 							 
							 }					 
						 }
					 }
					 
					 System.out.println(finalStr);
					 targetContent = finalStr.toString().getBytes();
					 
					 /*File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
					 PrintWriter writer = new PrintWriter(wdCSVfile);
					 writer.write(finalStr.toString());
					 writer.flush();
					 writer.close();
					 
					 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
					 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
					 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
					 postLoadService.updatePostLoad(postLoad);*/
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Organization_Reference_ID");
					 complete = true;
					 
					 /*String wdCSVfile1 = "config/" + loadCycle + "_" + ruleName + ".csv";			 
					 PrintWriter writer1 = new PrintWriter(new File(wdCSVfile1));
					 writer1.write(finalStr.toString());
					 writer1.flush();
					 writer1.close();*/
				 }
				 /*else
				 {
					 headingWd = new JSONArray();
					 JSONObject obj = new JSONObject();
					 obj.put("errorMsg", faultStr.substring(faultStr.indexOf("."), faultStr.length()));
					 headingWd.put(obj);
				 }*/
			 }
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}

	private String addMasterDataListToFindError(String xmlFile, String columnVal, String ruleName, String idVal) {
		
		File updatedRequestfile = null;
		try  
		{  
			File file = new File(xmlFile);     
			FileReader fr = new FileReader(file);   
			BufferedReader br = new BufferedReader(fr);   
			StringBuffer sb = new StringBuffer();     
			String line; 
			boolean executed = true;
			while((line=br.readLine())!=null)  
			{  
				sb.append(line);  
				sb.append("\n");
				if(line.contains("bsvc:Get_Organizations_Request") && executed)
				{
					executed = false;
					sb.append(" <bsvc:Request_References>"); 
					sb.append("\n");
						sb.append("  <bsvc:Organization_Reference bsvc:Descriptor=" + "\"" + idVal + "\"" + ">");
						sb.append("\n");
						sb.append("   <bsvc:ID bsvc:type=" + "\"" + idVal + "\"" + ">" + columnVal + "</bsvc:ID>");
						sb.append("\n");
						sb.append("  </bsvc:Organization_Reference>");
						sb.append("\n");					
					sb.append(" </bsvc:Request_References>");
					sb.append("\n");
				}
			}  
			fr.close();
			br.close();
			
			updatedRequestfile = File.createTempFile(ruleName + "_" + columnVal , ".xml");
			PrintWriter writer = new PrintWriter(updatedRequestfile);
		    writer.write(sb.toString());
			writer.flush();
			writer.close();
		}  
		catch(IOException e)  
		{  
			e.printStackTrace();  
		}
		return updatedRequestfile.getAbsolutePath(); 
	}

	private String getSuperiorHierarchyName(String GET_SUP_ORG_REQUEST_FILE, String superiorSupOrgId, String referenceId) {

		File updatedRequestfile = null;
		String superiorSupOrgName = "";
		try  
		{  
			File file = new File(GET_SUP_ORG_REQUEST_FILE);     
			FileReader fr = new FileReader(file);   
			BufferedReader br = new BufferedReader(fr);   
			StringBuffer sb = new StringBuffer();     
			String line; 
			boolean executed = true;
			while((line=br.readLine())!=null)  
			{  
				sb.append(line);  
				sb.append("\n");
				if(line.contains("bsvc:Get_Organizations_Request") && executed)
				{
					executed = false;
					sb.append(" <bsvc:Request_References>"); 
					sb.append("\n");
					sb.append("  <bsvc:Organization_Reference bsvc:Descriptor=" + "\"" + referenceId + "\"" + ">");
					sb.append("\n");
					sb.append("   <bsvc:ID bsvc:type=" + "\"" + referenceId + "\"" + ">" + superiorSupOrgId + "</bsvc:ID>");
					sb.append("\n");
					sb.append("  </bsvc:Organization_Reference>");
					sb.append("\n");					
					sb.append(" </bsvc:Request_References>");
					sb.append("\n");
				}
			}  
			fr.close();
			br.close();
			
			updatedRequestfile = File.createTempFile("Superior_" + superiorSupOrgId , ".xml");
			PrintWriter writer = new PrintWriter(updatedRequestfile);
		    writer.write(sb.toString());
			writer.flush();
			writer.close();
			
			String outputfile = updatedRequestfile.getAbsolutePath();

			InputStream is = new FileInputStream(outputfile);
		    SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
		    SOAPPart soapPart = soapMessage.getSOAPPart();
		    SOAPEnvelope envelope = soapPart.getEnvelope();
			envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
			if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
			{
				 envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
				 createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
			}
			soapMessage.saveChanges();
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    soapMessage.writeTo(out);
		    String strMsg = new String(out.toByteArray());	
		     
		    String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
		    SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
		    SOAPConnection soapConnection = soapConnectionFactory.createConnection();
			SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
			out = new ByteArrayOutputStream();
			soapResponse.writeTo(out);
			strMsg = new String(out.toByteArray(), "utf-8");			 
			 
			ReportElement soapResp = XmlParserManager.parseXml(strMsg);
			ReportElement responseData = soapResp.getChild("env:Body")
					.getChild("wd:Get_Organizations_Response")
					.getChild("wd:Response_Data");
		 
		    List<ReportElement> orgData = responseData.getChildren("wd:Organization");
			
		    for(ReportElement reportElement : orgData)
		    {
		    	ReportElement element1 = reportElement.getChild("wd:Organization_Data");
				 if(element1 != null)
				 {
					 superiorSupOrgName = element1.getChild("wd:Name") != null?element1.getChild("wd:Name").getValue().trim():"";					 
				 }
		    }
		}  
		catch(IOException e)  
		{  
			e.printStackTrace();  
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return superiorSupOrgName;
	}

	private JSONArray createCSVFromWDCostCenter(Tenant tenant, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		//String faultStr = null;
		String checkFile = null;
		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_COST_CENTER_REQUEST_FILE = requestfile.getAbsolutePath();
				 //String outputfile = GET_COST_CENTER_REQUEST_FILE;
				 //columnList.removeAll(errorList);
				 if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addMasterDataListToFindError(GET_COST_CENTER_REQUEST_FILE, columnList.get(i), ruleName, "Cost_Center_Reference_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement reportElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = reportElement.getChild("env:Body")
										.getChild("wd:Get_Organizations_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
						 }
					 }
					 columnList.removeAll(errorList);
				 }
				 String outputfile = addCostCenterIdList(GET_COST_CENTER_REQUEST_FILE, columnList, ruleName, startIndex, columnList.size(), "Cost_Center_Reference_ID");

				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");
				 
				 /*if(strMsg.contains("faultstring"))
				 {
					 ReportElement msgResponse = XmlParserManager.parseXml(strMsg);
					 ReportElement elementData = msgResponse.getChild("SOAP-ENV:Body")
								.getChild("SOAP-ENV:Fault");
					 faultStr = elementData.getChild("faultstring").getValue().trim();
					 System.out.println("faultStr-"+faultStr);
				 }*/
				 
				 //if(faultStr == null)
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Organizations_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String finalStr = "";
					 String headerStr = "";
					 String costCenterId = "";
					 String costCenterName = "";
					 String includeOrgCode = "";
					 String ccHierarchyId = "";
					 String ccHierarchyName = "";
					 
					 Map<String,String> ccHierarchyIdMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {					 
						 if(j == 1)
						 {
							startIndex = 0;
							if(999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*999;
							}
						 }
						 else
						 {
							startIndex = endIndex;
							if(j*1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = (j*999);
							}
						 }
						 outputfile = addCostCenterIdList(GET_COST_CENTER_REQUEST_FILE, columnList, ruleName, startIndex, endIndex, "Cost_Center_Reference_ID");
						 is = new FileInputStream(outputfile);
					     soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     soapPart = soapMessage.getSOAPPart();
					     envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
				         out = new ByteArrayOutputStream();
				         soapMessage.writeTo(out);
				         strMsg = new String(out.toByteArray());
				        
				         soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
				         soapResponse = soapConnection.call(soapMessage, sourceUrl);
				         out = new ByteArrayOutputStream();
				         soapResponse.writeTo(out);
				         strMsg = new String(out.toByteArray(), "utf-8");
				        
				         soapResp = XmlParserManager.parseXml(strMsg);
					 
						 ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Organizations_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> orgData = responseData.getChildren("wd:Organization");
							
						 for(ReportElement reportElement : orgData)
						 {
							 ReportElement element1 = reportElement.getChild("wd:Organization_Data");
							 if(element1 != null)
							 {
								 costCenterId = element1.getChild("wd:Reference_ID") != null?element1.getChild("wd:Reference_ID").getValue().trim():"";
								 wdColumnList.add(costCenterId);
								 costCenterName = element1.getChild("wd:Name") != null?element1.getChild("wd:Name").getValue().trim():"";
								 if(costCenterName.contains(","))
								 {
									 costCenterName =  costCenterName.replace(",", "|");
								 }
								 includeOrgCode = element1.getChild("wd:Include_Organization_Code_in_Name") != null?element1.getChild("wd:Include_Organization_Code_in_Name").getValue().trim():"";
								 if(includeOrgCode.equals("1"))
								 {
									 includeOrgCode = "Yes";
								 }
								 else if(includeOrgCode.equals("0"))
								 {
									 includeOrgCode = "No";
								 }
								 
								 ReportElement element2 = element1.getChild("wd:Hierarchy_Data");
								 if(element2 != null)
								 {
									 List<ReportElement> element3 = element2.getChildren("wd:Included_In_Organization_Reference");
									 if(element3 != null)
									 {
										 for(ReportElement ccHierarchyElement:element3)
										 {
											 List<ReportElement> ccIdData = ccHierarchyElement.getChildren("wd:ID");					 
											 for(ReportElement ccIdElement:ccIdData)
											 {
												 ccHierarchyIdMap = ccIdElement.getAllAttributes();
												 if(ccHierarchyIdMap.get("wd:type").equals("Organization_Reference_ID"))
												 {
													 ccHierarchyId = ccIdElement.getValue().trim();
													 ccHierarchyName = getSuperiorHierarchyName(GET_COST_CENTER_REQUEST_FILE, ccHierarchyId, "Organization_Reference_ID");
													 if(ccHierarchyName.contains(","))
													 {
														 ccHierarchyName =  ccHierarchyName.replace(",", "|");
													 }												
												 }
											 }
											 if(ccMap.get(costCenterId).equalsIgnoreCase(ccHierarchyName))
											 {
												 break;
											 }
										 }
									 }
								 }
								 
								 headingFromWD = "Cost_Center_ID,Cost_Center_Name,Cost_Center_Hierarchy_ID,Cost_Center_Hierarchy_Name,Include_Organization_Code_in_Name";							 
								 headerStr = costCenterId + "," + costCenterName + "," + ccHierarchyId + "," + ccHierarchyName + "," + includeOrgCode ;
								 
								 if(finalStr.equals(""))
								 {
									 finalStr = headingFromWD + "\n" + headerStr;
								 }
								 else
								 {
									 finalStr = finalStr + "\n" + headerStr;
								 }
							 }					 
						 }
					 }
					 
					 System.out.println(finalStr);
					 
					 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
					 PrintWriter writer = new PrintWriter(wdCSVfile);
					 writer.write(finalStr.toString());
					 writer.flush();
					 writer.close();
					 
					 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
					 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
					 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
					 postLoadService.updatePostLoad(postLoad);
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Cost_Center_ID");
					 
					 /*String wdCSVfile1 = "config/" + loadCycle + "_" + ruleName + ".csv";			 
					 PrintWriter writer1 = new PrintWriter(new File(wdCSVfile1));
					 writer1.write(finalStr.toString());
					 writer1.flush();
					 writer1.close();*/
				 }
				 /*else
				 {
					 headingWd = new JSONArray();
					 JSONObject obj = new JSONObject();
					 obj.put("errorMsg", faultStr.substring(faultStr.indexOf(".")+1, faultStr.length()));
					 headingWd.put(obj);
				 }*/
			 }
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}

	private JSONArray createCSVFromWDPerformanceReview(Tenant tenant2, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_PERFORMANCE_REVIEW_FILE = requestfile.getAbsolutePath();
				 //String outputfile = GET_PERFORMANCE_REVIEW_FILE;
				 String outputfile = addBiographicList(GET_PERFORMANCE_REVIEW_FILE, columnList, ruleName, startIndex, columnList.size(), idTypeList);

				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");			 
				 
				 ReportElement soapResp = XmlParserManager.parseXml(strMsg);
				 
				 ReportElement pageData = soapResp.getChild("env:Body")
							.getChild("wd:Get_Workers_Response")
							.getChild("wd:Response_Results");
				 
				 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
				 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
				 int totalResult = Integer.parseInt(totalResults);
				 System.out.println("totalNoOfPages-"+totalNoOfPages);
				 System.out.println("totalResult-"+totalResult);
				 wdCount = totalResult;
				 
				 String finalStr = "";
				 String headerStr = "";
				 String workerId = "";
				 String workerType = "";
				 String managerId = "";
				 String reviewTemplate = "";
				 String reviewStartDate = "";
				 String reviewEndDate = "";
				 String managerRating = "";
				 String employeeRating = "";
				 
				 Map<String,String> idMap = null;
				 Map<String,String> managerMap = null;
				 Map<String,String> templateMap = null;
				 Map<String,String> revEmpMap = null;
				 Map<String,String> revManMap = null;
				 
				 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
				 {					 
					 if(j == 1)
					 {
						startIndex = 0;
						if(999 > totalResult)
						{
							endIndex = totalResult;
						}
						else
						{
							endIndex = j*999;
						}
					 }
					 else
					 {
						startIndex = endIndex;
						if(j*999 > totalResult)
						{
							endIndex = totalResult;
						}
						else
						{
							endIndex = (j*999);
						}
					 }
					 outputfile = addBiographicList(GET_PERFORMANCE_REVIEW_FILE, columnList, ruleName, startIndex, endIndex, idTypeList);
					 is = new FileInputStream(outputfile);
				     soapMessage = MessageFactory.newInstance().createMessage(null, is);
				     soapPart = soapMessage.getSOAPPart();
				     envelope = soapPart.getEnvelope();
					 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
					 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
					 {
							envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
					 }
					 soapMessage.saveChanges();
			         out = new ByteArrayOutputStream();
			         soapMessage.writeTo(out);
			         strMsg = new String(out.toByteArray());
			        
			         soapConnectionFactory = SOAPConnectionFactory.newInstance();
					 soapConnection = soapConnectionFactory.createConnection();
			         soapResponse = soapConnection.call(soapMessage, sourceUrl);
			         out = new ByteArrayOutputStream();
			         soapResponse.writeTo(out);
			         strMsg = new String(out.toByteArray(), "utf-8");
			        
			         soapResp = XmlParserManager.parseXml(strMsg);
				 
					 ReportElement responseData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Data");
					 
					 List<ReportElement> workerData = responseData.getChildren("wd:Worker");
						
					 for(ReportElement reportElement : workerData)
					 {
						 ReportElement element1 = reportElement.getChild("wd:Worker_Reference");
						 if(element1 != null)
						 {
							 List<ReportElement> idData = element1.getChildren("wd:ID");					 
							 for(ReportElement wdElement:idData)
							 {
								 idMap = wdElement.getAllAttributes();
								 if(idMap.get("wd:type").equals("Employee_ID"))
								 {
									 workerId = wdElement.getValue().trim();
									 workerType = "Employee_ID";
								 }
								 else if(idMap.get("wd:type").equals("Contingent_Worker_ID"))
								 {
									 workerId = wdElement.getValue().trim();
									 workerType = "Contingent_Worker_ID";
								 }
							 }
						 }

						 ReportElement element2 = reportElement.getChild("wd:Worker_Data")
		 							.getChild("wd:Employee_Review_Data");
						 
						 if(element2 != null)
						 {	
							 ReportElement element3 = element2.getChild("wd:Performance_Review_Data");
							 if(element3 != null)
							 {
								 ReportElement element4 = element3.getChild("wd:Review_Data");
								 if(element4 != null)
								 {
									 ReportElement element5 = element4.getChild("wd:Manager_Reference");
									 if(element5 != null)
									 {
										 List<ReportElement> managerData = element5.getChildren("wd:ID");					 
										 for(ReportElement managerElement:managerData)
										 {
											 managerMap = managerElement.getAllAttributes();
											 if(managerMap.get("wd:type").equals("Employee_ID"))
											 {
												 managerId = managerElement.getValue().trim();
											 }
										 }
									 }
									 
									 ReportElement element6 = element4.getChild("wd:Review_Template_Reference");
									 if(element6 != null)
									 {
										 List<ReportElement> templateData = element6.getChildren("wd:ID");					 
										 for(ReportElement templateElement:templateData)
										 {
											 templateMap = templateElement.getAllAttributes();
											 if(templateMap.get("wd:type").equals("Employee_Review_Template_ID"))
											 {
												 reviewTemplate = templateElement.getValue().trim();
											 }
										 }
									 }
									 
									 reviewStartDate = element4.getChild("wd:Period_Start_Date") != null?element4.getChild("wd:Period_Start_Date").getValue().trim():"";
									 reviewEndDate = element4.getChild("wd:Period_End_Date") != null?element4.getChild("wd:Period_End_Date").getValue().trim():"";
									 
									 ReportElement element7 = element4.getChild("wd:Self_Evaluation_Data");
									 if(element7 != null)
									 {
										 ReportElement element8 = element7.getChild("wd:Overall_Data")
												                  .getChild("wd:Rating_Reference");
										 if(element8 != null)
										 {
											 List<ReportElement> revEmpData = element8.getChildren("wd:ID");					 
											 for(ReportElement revEmpElement:revEmpData)
											 {
												 revEmpMap = revEmpElement.getAllAttributes();
												 if(revEmpMap.get("wd:type").equals("Review_Rating_ID"))
												 {
												    employeeRating = revEmpElement.getValue().trim();
												 }
											 }
										 }
									 }
									 
									 ReportElement element9 = element4.getChild("wd:Manager_Evaluation_Data");
									 if(element9 != null)
									 {
										 ReportElement element10 = element9.getChild("wd:Overall_Data")
												                  .getChild("wd:Rating_Reference");
										 if(element10 != null)
										 {
											 List<ReportElement> revManData = element10.getChildren("wd:ID");					 
											 for(ReportElement revManElement:revManData)
											 {
												 revManMap = revManElement.getAllAttributes();
												 if(revManMap.get("wd:type").equals("Review_Rating_ID"))
												 {
												    managerRating = revManElement.getValue().trim();
												 }
											 }
										 }
									 }
									 
									 headingFromWD = "Worker_ID,Worker_Type,Manager_ID,Review_Template,Review_Period_Start_Date,Review_Period_End_Date,Manager_Review_Rating,Employee_Review_Rating";						 		
									 
									 headerStr = workerId + "," + workerType + "," + managerId + "," + reviewTemplate + "," + reviewStartDate + "," + reviewEndDate + "," + managerRating
											     + "," + employeeRating;
									 
									 if(finalStr.equals(""))
									 {
										 finalStr = headingFromWD + "\n" + headerStr;
									 }
									 else
									 {
										 finalStr = finalStr + "\n" + headerStr;
									 }
								 }
							 }
						 }				 
					 }
				 }
				 
				 System.out.println(finalStr);
				 
				 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
				 PrintWriter writer = new PrintWriter(wdCSVfile);
				 writer.write(finalStr.toString());
				 writer.flush();
				 writer.close();
				 
				 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
				 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
				 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
				 postLoadService.updatePostLoad(postLoad);
				 
				 headingWd = selectColumnMapping(loadCycle, ruleName, "Worker_ID");
				 
				 /*String wdCSVfile1 = "config/" + loadCycle + "_" + ruleName + ".csv";			 
				 PrintWriter writer1 = new PrintWriter(new File(wdCSVfile1));
				 writer1.write(finalStr.toString());
				 writer1.flush();
				 writer1.close();*/
			 }
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}

	private JSONArray createCSVFromWDWorkerPhoto(Tenant tenant, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		headingFromWD = "";
		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_WORKER_PHOTO_FILE = requestfile.getAbsolutePath();
				 //String outputfile = GET_WORKER_PHOTO_FILE;
				 String outputfile = addWorkerPhotoList(GET_WORKER_PHOTO_FILE, columnList, ruleName, startIndex, columnList.size(), idTypeList);

				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Human_Resources";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");			 
				 
				 ReportElement soapResp = XmlParserManager.parseXml(strMsg);
				 
				 ReportElement pageData = soapResp.getChild("env:Body")
							.getChild("wd:Get_Worker_Photos_Response")
							.getChild("wd:Response_Results");
				 
				 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
				 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
				 int totalResult = Integer.parseInt(totalResults);
				 System.out.println("totalNoOfPages-"+totalNoOfPages);
				 System.out.println("totalResult-"+totalResult);
				 wdCount = totalResult;
				 
				 String finalStr = "";
				 String headerStr = "";
				 String workerId = "";
				 String workerType = "";
				 String fileName = "";
				 
				 Map<String,String> idMap = null;
				 
				 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
				 {					 
					 if(j == 1)
					 {
						startIndex = 0;
						if(999 > totalResult)
						{
							endIndex = totalResult;
						}
						else
						{
							endIndex = j*999;
						}
					 }
					 else
					 {
						startIndex = endIndex;
						if(j*999 > totalResult)
						{
							endIndex = totalResult;
						}
						else
						{
							endIndex = (j*999);
						}
					 }
					 outputfile = addWorkerPhotoList(GET_WORKER_PHOTO_FILE, columnList, ruleName, startIndex, endIndex, idTypeList);
					 is = new FileInputStream(outputfile);
				     soapMessage = MessageFactory.newInstance().createMessage(null, is);
				     soapPart = soapMessage.getSOAPPart();
				     envelope = soapPart.getEnvelope();
					 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
					 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
					 {
							envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
					 }
					 soapMessage.saveChanges();
			         out = new ByteArrayOutputStream();
			         soapMessage.writeTo(out);
			         strMsg = new String(out.toByteArray());
			        
			         soapConnectionFactory = SOAPConnectionFactory.newInstance();
					 soapConnection = soapConnectionFactory.createConnection();
			         soapResponse = soapConnection.call(soapMessage, sourceUrl);
			         out = new ByteArrayOutputStream();
			         soapResponse.writeTo(out);
			         strMsg = new String(out.toByteArray(), "utf-8");
			        
			         soapResp = XmlParserManager.parseXml(strMsg);
				 
					 ReportElement responseData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Worker_Photos_Response")
								.getChild("wd:Response_Data");
					 
					 List<ReportElement> workerData = responseData.getChildren("wd:Worker_Photo");
						
					 for(ReportElement reportElement : workerData)
					 {
						 ReportElement element1 = reportElement.getChild("wd:Worker_Reference");
						 if(element1 != null)
						 {
							 List<ReportElement> idData = element1.getChildren("wd:ID");					 
							 for(ReportElement wdElement:idData)
							 {
								 idMap = wdElement.getAllAttributes();
								 if(idMap.get("wd:type").equals("Employee_ID"))
								 {
									 workerId = wdElement.getValue().trim();
									 workerType = "Employee_ID";
								 }
								 else if(idMap.get("wd:type").equals("Contingent_Worker_ID"))
								 {
									 workerId = wdElement.getValue().trim();
									 workerType = "Contingent_Worker_ID";
								 }
							 }
						 }

						 ReportElement element2 = reportElement.getChild("wd:Worker_Photo_Data");
						 
						 if(element2 != null)
						 {							 							 
							 fileName = element2.getChild("wd:Filename") != null?element2.getChild("wd:Filename").getValue().trim():"";
							 
							 headingFromWD = "Worker_ID,Worker_Type,Worker_Photo";						 		
							 
							 headerStr = workerId + "," + workerType + "," + fileName;
							 
							 if(finalStr.equals(""))
							 {
								 finalStr = headingFromWD + "\n" + headerStr;
							 }
							 else
							 {
								 finalStr = finalStr + "\n" + headerStr;
							 }
						 }				 
					 }
				 }
				 
				 System.out.println(finalStr);
				 
				 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
				 PrintWriter writer = new PrintWriter(wdCSVfile);
				 writer.write(finalStr.toString());
				 writer.flush();
				 writer.close();
				 
				 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
				 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
				 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
				 postLoadService.updatePostLoad(postLoad);
				 
				 headingWd = selectColumnMapping(loadCycle, ruleName, "Worker_ID");
				 
				 /*String wdCSVfile1 = "config/" + loadCycle + "_" + ruleName + ".csv";			 
				 PrintWriter writer1 = new PrintWriter(new File(wdCSVfile1));
				 writer1.write(finalStr.toString());
				 writer1.flush();
				 writer1.close();*/
			 }
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}

	private JSONArray createCSVFromWDGovernmentId(Tenant tenant, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		//String faultStr = null;
		String checkFile = null;
		headingFromWD = "";
		try 
		{			 
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_GOVERNMENT_ID_FILE = requestfile.getAbsolutePath();
				 
				 //String outputfile = GET_GOVERNMENT_ID_FILE;
				 //columnList.removeAll(errorList);
				 if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addWorkerIdListToFindError(GET_GOVERNMENT_ID_FILE, columnList.get(i), ruleName, "Employee_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement reportElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = reportElement.getChild("env:Body")
										.getChild("wd:Get_Workers_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
						 }
					 }
					 columnList.removeAll(errorList);
				 }
				 String outputfile = addHireIdList(GET_GOVERNMENT_ID_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");
				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");
				 
				 /*if(strMsg.contains("faultstring"))
				 {
					 ReportElement msgResponse = XmlParserManager.parseXml(strMsg);
					 ReportElement elementData = msgResponse.getChild("SOAP-ENV:Body")
								.getChild("SOAP-ENV:Fault");
					 faultStr = elementData.getChild("faultstring").getValue().trim();
					 System.out.println("faultStr-"+faultStr);
				 }*/
				 
				 //if(faultStr == null)
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String countryISOCode = "";
					 String nationalId = "";
					 String nationalIdTypeCode = "";
					 String issueDate = "";
					 String expirationDate = "";
					 String verificationDate = "";
					 String issuingAgency = "";
					 String countryISOCodeArr = "";
					 String nationalIdArr = "";
					 String nationalIdTypeCodeArr = "";
					 String issueDateArr = "";
					 String expirationDateArr = "";
					 String verificationDateArr = "";
					 String issuingAgencyArr = "";
					 String finalStr = "";
					 String headerStr = "";
					 
					 Map<String,String> idTypeMap = null;
					 Map<String,String> countryMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {
						if(j == 1)
						{
							startIndex = 0;
							if(1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*1000;
							}
						}
						else
						{
							//startIndex = (j - 1)*1000;
							int lastVal = (j - 1);
							startIndex = (endIndex - lastVal);
							if(j*1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								//endIndex = j*1000;
								endIndex = (j*startIndex) + 1;
							}
						}
						outputfile = addHireIdList(GET_GOVERNMENT_ID_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
						is = new FileInputStream(outputfile);
					    soapMessage = MessageFactory.newInstance().createMessage(null, is);
					    soapPart = soapMessage.getSOAPPart();
					    envelope = soapPart.getEnvelope();
						envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						{
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						}
						soapMessage.saveChanges();
				        out = new ByteArrayOutputStream();
				        soapMessage.writeTo(out);
				        strMsg = new String(out.toByteArray());
				        
				        soapConnectionFactory = SOAPConnectionFactory.newInstance();
						soapConnection = soapConnectionFactory.createConnection();
				        soapResponse = soapConnection.call(soapMessage, sourceUrl);
				        out = new ByteArrayOutputStream();
				        soapResponse.writeTo(out);
				        strMsg = new String(out.toByteArray(), "utf-8");
				        soapResp = XmlParserManager.parseXml(strMsg);
					        
						ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Workers_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> applicantData = responseData.getChildren("wd:Worker");
							
						 for(ReportElement reportElement : applicantData)
						 {
							 ReportElement element1 = reportElement.getChild("wd:Worker_Data");
							 String applicantId = element1.getChild("wd:Worker_ID").getValue().trim();
							 
							 ReportElement element2 = reportElement.getChild("wd:Worker_Data")
							 			.getChild("wd:Personal_Data")
							 			.getChild("wd:Identification_Data");
							 
							 if(element2 != null)
							 {	
								 List<ReportElement> nationalData = element2.getChildren("wd:National_ID");
								 if(nationalData != null && nationalData.size() >0)
								 {
									 countryISOCodeArr = "";
									 nationalIdArr = "";
									 nationalIdTypeCodeArr = "";
									 issueDateArr = "";
									 expirationDateArr = "";
									 verificationDateArr = "";
									 issuingAgencyArr = "";
									 for(ReportElement nationalElement : nationalData)
									 {
										 ReportElement element3 = nationalElement.getChild("wd:National_ID_Data");
										 if(element3 != null)
										 {
											 nationalId = element3.getChild("wd:ID") != null?element3.getChild("wd:ID").getValue().trim():"";
											 if(nationalIdArr.equals(""))
											 {
												 nationalIdArr = nationalId;
											 }
											 else
											 {
												 nationalIdArr = nationalIdArr + "~" + nationalId;
											 }
											 issuingAgency = element3.getChild("wd:Issuing_Agency") != null?element3.getChild("wd:Issuing_Agency").getValue().trim():""; 
											 if(issuingAgencyArr.equals(""))
											 {
												 issuingAgencyArr = issuingAgency;
											 }
											 else
											 {
												 issuingAgencyArr = issuingAgencyArr + "~" + issuingAgency;
											 }
											 issueDate = element3.getChild("wd:Issued_Date") != null?element3.getChild("wd:Issued_Date").getValue().trim():"";
											 if(issueDateArr.equals(""))
											 {
												 issueDateArr = issueDate;
											 }
											 else
											 {
												 issueDateArr = issueDateArr + "~" + issueDate;
											 }
											 expirationDate = element3.getChild("wd:Expiration_Date") != null?element3.getChild("wd:Expiration_Date").getValue().trim():""; 
											 if(expirationDateArr.equals(""))
											 {
												 expirationDateArr = expirationDate;
											 }
											 else
											 {
												 expirationDateArr = expirationDateArr + "~" + expirationDate;
											 }
											 verificationDate = element3.getChild("wd:Verification_Date") != null?element3.getChild("wd:Verification_Date").getValue().trim():""; 
											 if(verificationDateArr.equals(""))
											 {
												 verificationDateArr = verificationDate;
											 }
											 else
											 {
												 verificationDateArr = verificationDateArr + "~" + verificationDate;
											 }
											 ReportElement element4 = element3.getChild("wd:ID_Type_Reference");
											 if(element4 != null)
											 {
												 List<ReportElement> idTypeData = element4.getChildren("wd:ID");								 
												 for(ReportElement idTypeElement:idTypeData)
												 {
													 idTypeMap = idTypeElement.getAllAttributes();
													 if(idTypeMap.get("wd:type").equals("National_ID_Type_Code"))
													 {
														 nationalIdTypeCode = idTypeElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 nationalIdTypeCode = "";
											 }
											 if(nationalIdTypeCodeArr.equals(""))
											 {
												 nationalIdTypeCodeArr = nationalIdTypeCode;
											 }
											 else
											 {
												 nationalIdTypeCodeArr = nationalIdTypeCodeArr + "~" + nationalIdTypeCode;
											 }
											 
											 ReportElement element5 = element3.getChild("wd:Country_Reference");
											 if(element5 != null)
											 {
												 List<ReportElement> countryData = element5.getChildren("wd:ID");								 
												 for(ReportElement countryElement:countryData)
												 {
													 countryMap = countryElement.getAllAttributes();
													 if(countryMap.get("wd:type").equals("ISO_3166-1_Alpha-3_Code"))
													 {
														 countryISOCode = countryElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 countryISOCode = "";
											 }
											 if(countryISOCodeArr.equals(""))
											 {
												 countryISOCodeArr = countryISOCode;
											 }
											 else
											 {
												 countryISOCodeArr = nationalIdTypeCodeArr + "~" + countryISOCode;
											 }
										 }	
									 }
								 }
								 else
								 {
									 countryISOCodeArr = "";
									 nationalIdArr = "";
									 nationalIdTypeCodeArr = "";
									 issueDateArr = "";
									 expirationDateArr = "";
									 verificationDateArr = "";
									 issuingAgencyArr = "";
								 }
							 }
							 else
							 {
								 countryISOCodeArr = "";
								 nationalIdArr = "";
								 nationalIdTypeCodeArr = "";
								 issueDateArr = "";
								 expirationDateArr = "";
								 verificationDateArr = "";
								 issuingAgencyArr = "";
							 }
							 headingFromWD = "Employee_ID,Country_ISO_Code,National_ID_Type_Code,National_ID,Track_Issuing_Agency,Issued_Date,Expiration_Date,Verification_Date";
							 
							 headerStr = applicantId + "," + countryISOCodeArr + "," + nationalIdTypeCodeArr + "," + nationalIdArr + "," + issuingAgencyArr + "," + issueDateArr + "," + 
							             expirationDateArr + "," + verificationDateArr;
							 
							 if(finalStr.equals(""))
							 {
								 finalStr = headingFromWD + "\n" + headerStr;
							 }
							 else
							 {
								 finalStr = finalStr + "\n" + headerStr;
							 }
						 }						 
					 }				 
					 
					 /*String[] headingFromWdArr = headingFromWD.split(",");
					 for(int i = 0;i<headingFromWdArr.length; i++)
					 {
						String headingValWd = headingFromWdArr[i].replace("_", " ");
						if(headingValWd.equalsIgnoreCase("National ID"))
						{
							nationalCntWD = i;
						}
					 }
					 
					 String[] headingFromSrcArr = headingFromSource.split(",");
					 for(int i = 0;i<headingFromSrcArr.length; i++)
					 {
						String headingValSrc = headingFromSrcArr[i].replace("_", " ");
						if(headingValSrc.equalsIgnoreCase("National ID"))
						{
							nationalCntSrc = i;
						}
					 }
					 
					 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
					 byte[] srcCSVFileContent = postLoad.getSrcCSVFileContent();
					 File sourceCSVfile = null;
					 try 
					 {
						 sourceCSVfile = File.createTempFile(postLoad.getSrcCSVFileName().substring(0, postLoad.getSrcCSVFileName().indexOf(".")), ".csv");
						 FileUtils.writeByteArrayToFile(sourceCSVfile, srcCSVFileContent);
					 } 
					 catch (IOException e1) 
					 {
						 e1.printStackTrace();
					 }
					 String srcFile = sourceCSVfile.getAbsolutePath();
					 
					 String line = "";
					 int count = 0;
				     BufferedReader reader = new BufferedReader(new FileReader(srcFile));
				     while ((line = reader.readLine()) != null) 
				     { 
				         if(count != 0 && line.length() > 0) 
				         {
				        	 String [] lineArr = line.split(",");
				        	 if(lineArr.length >1)
				        	 {
					        	 String existStr = updateWDCSVFileForMultipleRow(lineArr[0], lineArr[nationalCntSrc], nationalCntWD, finalStr);
					        	 if(existStr.length() > 0)
					        	 {
					        		 if(newFinalStr.equals(""))
									 {
					        			 newFinalStr = headingFromWD + "\n" + existStr;
									 }
									 else
									 {
										 newFinalStr = newFinalStr + "\n" + existStr;
									 }
					        	 }
				        	 }
				         }
				         count++;
				     }
				     reader.close();*/
				     
				     System.out.println(finalStr);
					 
					 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
					 PrintWriter writer = new PrintWriter(wdCSVfile);
					 writer.write(finalStr.toString());
					 writer.flush();
					 writer.close();
					 
					 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
					 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
					 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
					 postLoadService.updatePostLoad(postLoad);
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
					 
					 /*String wdCSVfile = "config/" + loadCycle + "_" + ruleName + ".csv";			 
					 PrintWriter writer = new PrintWriter(new File(wdCSVfile));
					 writer.write(finalStr.toString());
					 writer.flush();
					 writer.close();*/
				 }
				 /*else
				 {
					 headingWd = new JSONArray();
					 JSONObject obj = new JSONObject();
					 obj.put("errorMsg", faultStr.substring(faultStr.indexOf("."), faultStr.length()));
					 headingWd.put(obj);
				 }*/
			 }
			 
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return headingWd;
	}

	private JSONArray createCSVFromWDAssignPayGroup(Tenant tenant, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_PAY_GROUP_FILE = requestfile.getAbsolutePath();
				 //String outputfile = GET_PAY_GROUP_FILE;
				 String outputfile = addHireIdList(GET_PAY_GROUP_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");

				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");			 
				 
				 ReportElement soapResp = XmlParserManager.parseXml(strMsg);
				 
				 ReportElement pageData = soapResp.getChild("env:Body")
							.getChild("wd:Get_Workers_Response")
							.getChild("wd:Response_Results");
				 
				 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
				 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
				 int totalResult = Integer.parseInt(totalResults);
				 System.out.println("totalNoOfPages-"+totalNoOfPages);
				 System.out.println("totalResult-"+totalResult);
				 wdCount = totalResult;
				 
				 String finalStr = "";
				 String headerStr = "";
				 String empId = "";
				 String effectiveDate = "";
				 String payGroupId = "";
				 
				 Map<String,String> idMap = null;
				 Map<String,String> payGroupMap = null;
				 
				 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
				 {					 
					 if(j == 1)
					 {
						startIndex = 0;
						if(1000 > totalResult)
						{
							endIndex = totalResult;
						}
						else
						{
							endIndex = j*1000;
						}
					 }
					 else
					 {
						int lastVal = (j - 1);
						startIndex = (endIndex - lastVal);
						if(j*1000 > totalResult)
						{
							endIndex = totalResult;
						}
						else
						{
							endIndex = (j*startIndex) + 1;
						}
					 }
					 outputfile = addHireIdList(GET_PAY_GROUP_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
					 is = new FileInputStream(outputfile);
				     soapMessage = MessageFactory.newInstance().createMessage(null, is);
				     soapPart = soapMessage.getSOAPPart();
				     envelope = soapPart.getEnvelope();
					 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
					 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
					 {
							envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
					 }
					 soapMessage.saveChanges();
			         out = new ByteArrayOutputStream();
			         soapMessage.writeTo(out);
			         strMsg = new String(out.toByteArray());
			        
			         soapConnectionFactory = SOAPConnectionFactory.newInstance();
					 soapConnection = soapConnectionFactory.createConnection();
			         soapResponse = soapConnection.call(soapMessage, sourceUrl);
			         out = new ByteArrayOutputStream();
			         soapResponse.writeTo(out);
			         strMsg = new String(out.toByteArray(), "utf-8");
			        
			         soapResp = XmlParserManager.parseXml(strMsg);
				 
					 ReportElement responseData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Data");
					 
					 List<ReportElement> workerData = responseData.getChildren("wd:Worker");
						
					 for(ReportElement reportElement : workerData)
					 {
						 ReportElement element1 = reportElement.getChild("wd:Worker_Reference");
						 if(element1 != null)
						 {
							 List<ReportElement> idData = element1.getChildren("wd:ID");					 
							 for(ReportElement wdElement:idData)
							 {
								 idMap = wdElement.getAllAttributes();
								 if(idMap.get("wd:type").equals("Employee_ID"))
								 {
									 empId = wdElement.getValue().trim();
								 }
							 }
						 }
						 
						 ReportElement element2 = reportElement.getChild("wd:Worker_Data")
						 							.getChild("wd:Employment_Data")
						 							.getChild("wd:Worker_Job_Data");
						 if(element2 != null)
						 {
							 ReportElement element3 = element2.getChild("wd:Position_Data");
							 if(element3 != null)
							 {
								 ReportElement element4 = element3.getChild("wd:Payroll_Interface_Processing_Data");
								 if(element4 != null)
								 {
									 effectiveDate = element4.getChild("wd:Effective_Date") != null?element4.getChild("wd:Effective_Date").getValue().trim():"";
									 
									 ReportElement element5 = element4.getChild("wd:Pay_Group_Reference");
									 if(element5 != null)
									 {
										 List<ReportElement> payGroupData = element5.getChildren("wd:ID");					 
										 for(ReportElement payGroupElement:payGroupData)
										 {
											 payGroupMap = payGroupElement.getAllAttributes();
											 if(payGroupMap.get("wd:type").equals("External_Pay_Group_ID"))
											 {
												 payGroupId = payGroupElement.getValue().trim();
												 
												 headingFromWD = "Employee_ID,Effective_Date,Pay_Group_ID";
												 
												 headerStr = empId + "," + effectiveDate + "," + payGroupId ;
												 
												 if(finalStr.equals(""))
												 {
													 finalStr = headingFromWD + "\n" + headerStr;
												 }
												 else
												 {
													 finalStr = finalStr + "\n" + headerStr;
												 }
											 }
										 }
									 }
								 }								 
							 }
						 }					 
					 }
				 }
				 
				 System.out.println(finalStr);
				 
				 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
				 PrintWriter writer = new PrintWriter(wdCSVfile);
				 writer.write(finalStr.toString());
				 writer.flush();
				 writer.close();
				 
				 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
				 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
				 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
				 postLoadService.updatePostLoad(postLoad);
				 
				 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
				 
				 /*String wdCSVfile1 = "config/" + loadCycle + "_" + ruleName + ".csv";			 
				 PrintWriter writer1 = new PrintWriter(new File(wdCSVfile1));
				 writer1.write(finalStr.toString());
				 writer1.flush();
				 writer1.close();*/
			 }
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}

	private JSONArray createCSVFromWDMeritPlan(Tenant tenant, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_MERIT_PLAN_FILE = requestfile.getAbsolutePath();
				 //String outputfile = GET_MERIT_PLAN_FILE;
				 String outputfile = addHireIdList(GET_MERIT_PLAN_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");

				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");			 
				 
				 ReportElement soapResp = XmlParserManager.parseXml(strMsg);
				 
				 ReportElement pageData = soapResp.getChild("env:Body")
							.getChild("wd:Get_Workers_Response")
							.getChild("wd:Response_Results");
				 
				 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
				 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
				 int totalResult = Integer.parseInt(totalResults);
				 System.out.println("totalNoOfPages-"+totalNoOfPages);
				 System.out.println("totalResult-"+totalResult);
				 wdCount = totalResult;
				 
				 String finalStr = "";
				 String headerStr = "";
				 String newFinalStr = "";
				 String empId = "";
				 String effectiveDate = "";
				 String compPackageName = "";
				 String compGradeName = "";
				 String compProfileName = "";
				 String compStepName = "";
				 String meritPlanName = "";
				 String indivisualTarget = "";
				 String guaranteedMinimum = "";
				 
				 Map<String,String> idMap = null;
				 Map<String,String> packageMap = null;
				 Map<String,String> gradeMap = null;
				 Map<String,String> profileMap = null;
				 Map<String,String> stepMap = null;
				 Map<String,String> meritPlanMap = null;
				 
				 int meritCntWD = 0;
				 int meritCntSrc = 0;
				 
				 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
				 {					 
					 if(j == 1)
					 {
						startIndex = 0;
						if(1000 > totalResult)
						{
							endIndex = totalResult;
						}
						else
						{
							endIndex = j*1000;
						}
					 }
					 else
					 {
						int lastVal = (j - 1);
						startIndex = (endIndex - lastVal);
						if(j*1000 > totalResult)
						{
							endIndex = totalResult;
						}
						else
						{
							endIndex = (j*startIndex) + 1;
						}
					 }
					 outputfile = addHireIdList(GET_MERIT_PLAN_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
					 is = new FileInputStream(outputfile);
				     soapMessage = MessageFactory.newInstance().createMessage(null, is);
				     soapPart = soapMessage.getSOAPPart();
				     envelope = soapPart.getEnvelope();
					 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
					 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
					 {
							envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
					 }
					 soapMessage.saveChanges();
			         out = new ByteArrayOutputStream();
			         soapMessage.writeTo(out);
			         strMsg = new String(out.toByteArray());
			        
			         soapConnectionFactory = SOAPConnectionFactory.newInstance();
					 soapConnection = soapConnectionFactory.createConnection();
			         soapResponse = soapConnection.call(soapMessage, sourceUrl);
			         out = new ByteArrayOutputStream();
			         soapResponse.writeTo(out);
			         strMsg = new String(out.toByteArray(), "utf-8");
			        
			         soapResp = XmlParserManager.parseXml(strMsg);
				 
					 ReportElement responseData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Data");
					 
					 List<ReportElement> workerData = responseData.getChildren("wd:Worker");
						
					 for(ReportElement reportElement : workerData)
					 {
						 ReportElement element1 = reportElement.getChild("wd:Worker_Reference");
						 if(element1 != null)
						 {
							 List<ReportElement> idData = element1.getChildren("wd:ID");					 
							 for(ReportElement wdElement:idData)
							 {
								 idMap = wdElement.getAllAttributes();
								 if(idMap.get("wd:type").equals("Employee_ID"))
								 {
									 empId = wdElement.getValue().trim();
								 }
							 }
						 }
						 
						 ReportElement element2 = reportElement.getChild("wd:Worker_Data")
						 							.getChild("wd:Compensation_Data");
						 if(element2 != null)
						 {
							 effectiveDate = element2.getChild("wd:Compensation_Effective_Date") != null?element2.getChild("wd:Compensation_Effective_Date").getValue().trim():"";	
							 
							 ReportElement element3 = element2.getChild("wd:Compensation_Guidelines_Data");
							 if(element3 != null)
							 {
								 ReportElement element4 = element3.getChild("wd:Compensation_Package_Reference");
								 if(element4 != null)
								 {
									 List<ReportElement> packageData = element4.getChildren("wd:ID");					 
									 for(ReportElement packageElement:packageData)
									 {
										 packageMap = packageElement.getAllAttributes();
										 if(packageMap.get("wd:type").equals("Compensation_Package_ID"))
										 {
											 compPackageName = packageElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 compPackageName = "";
								 }
								 
								 ReportElement element5 = element3.getChild("wd:Compensation_Grade_Reference");
								 if(element5 != null)
								 {
									 List<ReportElement> gradeData = element5.getChildren("wd:ID");					 
									 for(ReportElement gradeElement:gradeData)
									 {
										 gradeMap = gradeElement.getAllAttributes();
										 if(gradeMap.get("wd:type").equals("Compensation_Grade_ID"))
										 {
											 compGradeName = gradeElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 compGradeName = "";
								 }
								 
								 ReportElement element6 = element3.getChild("wd:Compensation_Grade_Profile_Reference");
								 if(element6 != null)
								 {
									 List<ReportElement> profileData = element6.getChildren("wd:ID");					 
									 for(ReportElement profileElement:profileData)
									 {
										 profileMap = profileElement.getAllAttributes();
										 if(profileMap.get("wd:type").equals("Compensation_Grade_Profile_ID"))
										 {
											 compProfileName = profileElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 compProfileName = "";
								 }
								 
								 ReportElement element7 = element3.getChild("wd:Compensation_Step_Reference");
								 if(element7 != null)
								 {
									 List<ReportElement> stepData = element7.getChildren("wd:ID");					 
									 for(ReportElement stepElement:stepData)
									 {
										 stepMap = stepElement.getAllAttributes();
										 if(stepMap.get("wd:type").equals("Compensation_Step_ID"))
										 {
											 compStepName = stepElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 compStepName = "";
								 }
							 }
							 else
							 {
								 effectiveDate = "";
								 compPackageName = "";
								 compGradeName = "";
								 compProfileName = "";
								 compStepName = "";
							 }
							 
							 List<ReportElement> meritPlanData = element2.getChildren("wd:Merit_Plan_Data");
							 if(meritPlanData != null)
							 {
								 for(ReportElement meritPlanElement: meritPlanData)
								 {
									 ReportElement element8 = meritPlanElement.getChild("wd:Compensation_Plan_Reference");
									 if(element8 != null)
									 {
										 List<ReportElement> meritData = element8.getChildren("wd:ID");					 
										 for(ReportElement meritElement:meritData)
										 {
											 meritPlanMap = meritElement.getAllAttributes();
											 if(meritPlanMap.get("wd:type").equals("Compensation_Plan_ID"))
											 {
												 meritPlanName= meritElement.getValue().trim();											
											 }
										 }
									 }
									 else
									 {
										 meritPlanName = "";
									 }
									 
									 indivisualTarget = meritPlanElement.getChild("wd:Individual_Target_Percent") != null?meritPlanElement.getChild("wd:Individual_Target_Percent").getValue().trim():"";
									 guaranteedMinimum = meritPlanElement.getChild("wd:Guaranteed_Minimum") != null?meritPlanElement.getChild("wd:Guaranteed_Minimum").getValue().trim():"";
									 if(guaranteedMinimum.equals("1"))
									 {
										 guaranteedMinimum = "TRUE";
									 }
									 else
									 {
										 guaranteedMinimum = "FALSE";
									 }
									 
									 headingFromWD = "Employee_ID,Effective_Date,Compensation_Package_Name,Compensation_Grade_Name,Compensation_Profile_Name,Compensation_Step_Name,Merit_Plan_Name,"
							 		         + "Individual_Target,Guaranteed_Minimum";
								 
									 headerStr = empId + "," + effectiveDate + "," + compPackageName + "," + compGradeName + "," + compProfileName + "," + compStepName + "," + meritPlanName + "," + 
											     indivisualTarget + "," + guaranteedMinimum;
									 
									 if(finalStr.equals(""))
									 {
										 finalStr = headingFromWD + "\n" + headerStr;
									 }
									 else
									 {
										 finalStr = finalStr + "\n" + headerStr;
									 }					
								 }								 
							 }
						 }						 						  
					 }
				 }
				 
				 String[] headingFromWdArr = headingFromWD.split(",");
				 for(int i = 0;i<headingFromWdArr.length; i++)
				 {
					String headingValWd = headingFromWdArr[i].replace("_", " ");
					if(headingValWd.equalsIgnoreCase("Merit Plan Name"))
					{
						meritCntWD = i;
					}
				 }
				 
				 String[] headingFromSrcArr = headingFromSource.split(",");
				 for(int i = 0;i<headingFromSrcArr.length; i++)
				 {
					String headingValSrc = headingFromSrcArr[i].replace("_", " ");
					if(headingValSrc.equalsIgnoreCase("Merit Plan Name"))
					{
						meritCntSrc = i;
					}
				 }
				 
				 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
				 byte[] srcCSVFileContent = postLoad.getSrcCSVFileContent();
				 File sourceCSVfile = null;
				 try 
				 {
					 sourceCSVfile = File.createTempFile(postLoad.getSrcCSVFileName().substring(0, postLoad.getSrcCSVFileName().indexOf(".")), ".csv");
					 FileUtils.writeByteArrayToFile(sourceCSVfile, srcCSVFileContent);
				 } 
				 catch (IOException e1) 
				 {
					 e1.printStackTrace();
				 }
				 String srcFile = sourceCSVfile.getAbsolutePath();
				 
				 String line = "";
				 int count = 0;
			     BufferedReader reader = new BufferedReader(new FileReader(srcFile));
			     while ((line = reader.readLine()) != null) 
			     { 
			         if(count != 0 && line.length() > 0) 
			         {
			        	 String [] lineArr = line.split(",");
			        	 if(lineArr.length >1)
			        	 {
				        	 String existStr = updateWDCSVFileForMultipleRow(lineArr[0], lineArr[meritCntSrc], meritCntWD, finalStr);
				        	 if(existStr.length() > 0)
				        	 {
				        		 if(newFinalStr.equals(""))
								 {
				        			 newFinalStr = headingFromWD + "\n" + existStr;
								 }
								 else
								 {
									 newFinalStr = newFinalStr + "\n" + existStr;
								 }
				        	 }
			        	 }
			         }
			         count++;
			     }
			     reader.close();
			     
			     System.out.println(newFinalStr);
				 
				 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
				 PrintWriter writer = new PrintWriter(wdCSVfile);
				 writer.write(newFinalStr.toString());
				 writer.flush();
				 writer.close();
				 
				 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
				 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
				 postLoadService.updatePostLoad(postLoad);
				 
				 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
				 
				 /*String wdCSVfile1 = "config/" + loadCycle + "_" + ruleName + ".csv";			 
				 PrintWriter writer1 = new PrintWriter(new File(wdCSVfile1));
				 writer1.write(finalStr.toString());
				 writer1.flush();
				 writer1.close();*/
			 }
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}

	private JSONArray createCSVFromWDStockPlan(Tenant tenant, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_STOCK_PLAN_FILE = requestfile.getAbsolutePath();
				 //String outputfile = GET_STOCK_PLAN_FILE;
				 String outputfile = addHireIdList(GET_STOCK_PLAN_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");

				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");			 
				 
				 ReportElement soapResp = XmlParserManager.parseXml(strMsg);
				 
				 ReportElement pageData = soapResp.getChild("env:Body")
							.getChild("wd:Get_Workers_Response")
							.getChild("wd:Response_Results");
				 
				 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
				 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
				 int totalResult = Integer.parseInt(totalResults);
				 System.out.println("totalNoOfPages-"+totalNoOfPages);
				 System.out.println("totalResult-"+totalResult);
				 wdCount = totalResult;
				 
				 String finalStr = "";
				 String headerStr = "";
				 String newFinalStr = "";
				 String empId = "";
				 String effectiveDate = "";
				 String compPackageName = "";
				 String compGradeName = "";
				 String compProfileName = "";
				 String compStepName = "";
				 String stockPlanName = "";
				 String indivisualTargetShare = "";
				 String indivisualTargetAmount = "";
				 String indivisualTargetPercentage = "";
				 String currency = "";
				 
				 Map<String,String> idMap = null;
				 Map<String,String> packageMap = null;
				 Map<String,String> gradeMap = null;
				 Map<String,String> profileMap = null;
				 Map<String,String> stepMap = null;
				 Map<String,String> stockPlanMap = null;
				 Map<String,String> stockCurrencyMap = null;
				 
				 int stockCntWD = 0;
				 int stockCntSrc = 0;
				 
				 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
				 {					 
					 if(j == 1)
					 {
						startIndex = 0;
						if(1000 > totalResult)
						{
							endIndex = totalResult;
						}
						else
						{
							endIndex = j*1000;
						}
					 }
					 else
					 {
						int lastVal = (j - 1);
						startIndex = (endIndex - lastVal);
						if(j*1000 > totalResult)
						{
							endIndex = totalResult;
						}
						else
						{
							endIndex = (j*startIndex) + 1;
						}
					 }
					 outputfile = addHireIdList(GET_STOCK_PLAN_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
					 is = new FileInputStream(outputfile);
				     soapMessage = MessageFactory.newInstance().createMessage(null, is);
				     soapPart = soapMessage.getSOAPPart();
				     envelope = soapPart.getEnvelope();
					 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
					 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
					 {
							envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
					 }
					 soapMessage.saveChanges();
			         out = new ByteArrayOutputStream();
			         soapMessage.writeTo(out);
			         strMsg = new String(out.toByteArray());
			        
			         soapConnectionFactory = SOAPConnectionFactory.newInstance();
					 soapConnection = soapConnectionFactory.createConnection();
			         soapResponse = soapConnection.call(soapMessage, sourceUrl);
			         out = new ByteArrayOutputStream();
			         soapResponse.writeTo(out);
			         strMsg = new String(out.toByteArray(), "utf-8");
			        
			         soapResp = XmlParserManager.parseXml(strMsg);
				 
					 ReportElement responseData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Data");
					 
					 List<ReportElement> workerData = responseData.getChildren("wd:Worker");
						
					 for(ReportElement reportElement : workerData)
					 {
						 ReportElement element1 = reportElement.getChild("wd:Worker_Reference");
						 if(element1 != null)
						 {
							 List<ReportElement> idData = element1.getChildren("wd:ID");					 
							 for(ReportElement wdElement:idData)
							 {
								 idMap = wdElement.getAllAttributes();
								 if(idMap.get("wd:type").equals("Employee_ID"))
								 {
									 empId = wdElement.getValue().trim();
								 }
							 }
						 }
						 
						 ReportElement element2 = reportElement.getChild("wd:Worker_Data")
						 							.getChild("wd:Compensation_Data");
						 if(element2 != null)
						 {
							 effectiveDate = element2.getChild("wd:Compensation_Effective_Date") != null?element2.getChild("wd:Compensation_Effective_Date").getValue().trim():"";	
							 
							 ReportElement element3 = element2.getChild("wd:Compensation_Guidelines_Data");
							 if(element3 != null)
							 {
								 ReportElement element4 = element3.getChild("wd:Compensation_Package_Reference");
								 if(element4 != null)
								 {
									 List<ReportElement> packageData = element4.getChildren("wd:ID");					 
									 for(ReportElement packageElement:packageData)
									 {
										 packageMap = packageElement.getAllAttributes();
										 if(packageMap.get("wd:type").equals("Compensation_Package_ID"))
										 {
											 compPackageName = packageElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 compPackageName = "";
								 }
								 
								 ReportElement element5 = element3.getChild("wd:Compensation_Grade_Reference");
								 if(element5 != null)
								 {
									 List<ReportElement> gradeData = element5.getChildren("wd:ID");					 
									 for(ReportElement gradeElement:gradeData)
									 {
										 gradeMap = gradeElement.getAllAttributes();
										 if(gradeMap.get("wd:type").equals("Compensation_Grade_ID"))
										 {
											 compGradeName = gradeElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 compGradeName = "";
								 }
								 
								 ReportElement element6 = element3.getChild("wd:Compensation_Grade_Profile_Reference");
								 if(element6 != null)
								 {
									 List<ReportElement> profileData = element6.getChildren("wd:ID");					 
									 for(ReportElement profileElement:profileData)
									 {
										 profileMap = profileElement.getAllAttributes();
										 if(profileMap.get("wd:type").equals("Compensation_Grade_Profile_ID"))
										 {
											 compProfileName = profileElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 compProfileName = "";
								 }
								 
								 ReportElement element7 = element3.getChild("wd:Compensation_Step_Reference");
								 if(element7 != null)
								 {
									 List<ReportElement> stepData = element7.getChildren("wd:ID");					 
									 for(ReportElement stepElement:stepData)
									 {
										 stepMap = stepElement.getAllAttributes();
										 if(stepMap.get("wd:type").equals("Compensation_Step_ID"))
										 {
											 compStepName = stepElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 compStepName = "";
								 }
							 }
							 else
							 {
								 effectiveDate = "";
								 compPackageName = "";
								 compGradeName = "";
								 compProfileName = "";
								 compStepName = "";
							 }
							 
							 List<ReportElement> stockPlanData = element2.getChildren("wd:Stock_Plan_Data");
							 if(stockPlanData != null)
							 {
								 for(ReportElement stockPlanElement: stockPlanData)
								 {
									 ReportElement element8 = stockPlanElement.getChild("wd:Compensation_Plan_Reference");
									 if(element8 != null)
									 {
										 List<ReportElement> stockData = element8.getChildren("wd:ID");					 
										 for(ReportElement stockElement:stockData)
										 {
											 stockPlanMap = stockElement.getAllAttributes();
											 if(stockPlanMap.get("wd:type").equals("Compensation_Plan_ID"))
											 {
												 stockPlanName= stockElement.getValue().trim();											
											 }
										 }
									 }
									 else
									 {
										 stockPlanName = "";
									 }
									 
									 indivisualTargetShare = stockPlanElement.getChild("wd:Individual_Target_Shares") != null?stockPlanElement.getChild("wd:Individual_Target_Shares").getValue().trim():"";
									 indivisualTargetAmount = stockPlanElement.getChild("wd:Individual_Target_Amount") != null?stockPlanElement.getChild("wd:Individual_Target_Amount").getValue().trim():"";
									 indivisualTargetPercentage = stockPlanElement.getChild("wd:Individual_Target_Percent") != null?stockPlanElement.getChild("wd:Individual_Target_Percent").getValue().trim():"";
									 
									 ReportElement element10 = stockPlanElement.getChild("wd:Currency_Reference");
									 if(element10 != null)
									 {
										 List<ReportElement> stockCurrencyData = element10.getChildren("wd:ID");					 
										 for(ReportElement stockCurrencyElement:stockCurrencyData)
										 {
											 stockCurrencyMap = stockCurrencyElement.getAllAttributes();
											 if(stockCurrencyMap.get("wd:type").equals("Currency_ID"))
											 {
												 currency = stockCurrencyElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 currency = "";
									 }
									 
									 headingFromWD = "Employee_ID,Effective_Date,Compensation_Package_Name,Compensation_Grade_Name,Compensation_Profile_Name,Compensation_Step_Name,Stock_Plan_Name,"
							 		         + "Individual_Target_Share,Individual_Target_Percent,Individual_Target_Amount,Currency";
								 
									 headerStr = empId + "," + effectiveDate + "," + compPackageName + "," + compGradeName + "," + compProfileName + "," + compStepName + "," + stockPlanName + "," + 
											     indivisualTargetShare + "," + indivisualTargetPercentage + "," + indivisualTargetAmount + ","  + currency;
									 
									 if(finalStr.equals(""))
									 {
										 finalStr = headingFromWD + "\n" + headerStr;
									 }
									 else
									 {
										 finalStr = finalStr + "\n" + headerStr;
									 }					
								 }								 
							 }
						 }						 						  
					 }
				 }
				 
				 System.out.println(finalStr);
				 
				 String[] headingFromWdArr = headingFromWD.split(",");
				 for(int i = 0;i<headingFromWdArr.length; i++)
				 {
					String headingValWd = headingFromWdArr[i].replace("_", " ");
					if(headingValWd.equalsIgnoreCase("Stock Plan Name"))
					{
						stockCntWD = i;
					}
				 }
				 
				 String[] headingFromSrcArr = headingFromSource.split(",");
				 for(int i = 0;i<headingFromSrcArr.length; i++)
				 {
					String headingValSrc = headingFromSrcArr[i].replace("_", " ");
					if(headingValSrc.equalsIgnoreCase("Stock Plan Name"))
					{
						stockCntSrc = i;
					}
				 }
				 
				 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
				 byte[] srcCSVFileContent = postLoad.getSrcCSVFileContent();
				 File sourceCSVfile = null;
				 try 
				 {
					 sourceCSVfile = File.createTempFile(postLoad.getSrcCSVFileName().substring(0, postLoad.getSrcCSVFileName().indexOf(".")), ".csv");
					 FileUtils.writeByteArrayToFile(sourceCSVfile, srcCSVFileContent);
				 } 
				 catch (IOException e1) 
				 {
					 e1.printStackTrace();
				 }
				 String srcFile = sourceCSVfile.getAbsolutePath();
				 
				 String line = "";
				 int count = 0;
			     BufferedReader reader = new BufferedReader(new FileReader(srcFile));
			     while ((line = reader.readLine()) != null) 
			     { 
			         if(count != 0 && line.length() > 0) 
			         {
			        	 String [] lineArr = line.split(",");
			        	 if(lineArr.length >1)
			        	 {
				        	 String existStr = updateWDCSVFileForMultipleRow(lineArr[0], lineArr[stockCntSrc], stockCntWD, finalStr);
				        	 if(existStr.length() > 0)
				        	 {
				        		 if(newFinalStr.equals(""))
								 {
				        			 newFinalStr = headingFromWD + "\n" + existStr;
								 }
								 else
								 {
									 newFinalStr = newFinalStr + "\n" + existStr;
								 }
				        	 }
			        	 }
			         }
			         count++;
			     }
			     reader.close();
			     
			     System.out.println(newFinalStr);
				 
				 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
				 PrintWriter writer = new PrintWriter(wdCSVfile);
				 writer.write(newFinalStr.toString());
				 writer.flush();
				 writer.close();
				 
				 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
				 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
				 postLoadService.updatePostLoad(postLoad);
				 
				 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
				 
				 /*String wdCSVfile1 = "config/" + loadCycle + "_" + ruleName + ".csv";			 
				 PrintWriter writer1 = new PrintWriter(new File(wdCSVfile1));
				 writer1.write(finalStr.toString());
				 writer1.flush();
				 writer1.close();*/
			 }
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}

	private JSONArray createCSVFromWDAllowancePlan(Tenant tenant, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_ALLOWANCE_PLAN_FILE = requestfile.getAbsolutePath();
				 //String outputfile = GET_ALLOWANCE_PLAN_FILE;
				 String outputfile = addHireIdList(GET_ALLOWANCE_PLAN_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");

				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");			 
				 
				 ReportElement soapResp = XmlParserManager.parseXml(strMsg);
				 
				 ReportElement pageData = soapResp.getChild("env:Body")
							.getChild("wd:Get_Workers_Response")
							.getChild("wd:Response_Results");
				 
				 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
				 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
				 int totalResult = Integer.parseInt(totalResults);
				 System.out.println("totalNoOfPages-"+totalNoOfPages);
				 System.out.println("totalResult-"+totalResult);
				 wdCount = totalResult;
				 
				 String finalStr = "";
				 String headerStr = "";
				 String newFinalStr = "";
				 String empId = "";
				 String effectiveDate = "";
				 String compPackageName = "";
				 String compGradeName = "";
				 String compProfileName = "";
				 String compStepName = "";
				 String allowancePlanName = "";
				 String allowanceElementName = "";
				 String amount = "";
				 String percentage = "";
				 String allowanceCurrency = "";
				 String allowanceFrequency = "";
				 String unitAllowancePlanName = "";
				 String unitAllowanceElementName = "";
				 String unitOfMeasureName = "";
				 String perUnitAmount = "";
				 String unitAllowanceCurrency = "";
				 String noOfUnits = "";
				 String unitAllowanceFrequency = "";
				 
				 Map<String,String> idMap = null;
				 Map<String,String> packageMap = null;
				 Map<String,String> gradeMap = null;
				 Map<String,String> profileMap = null;
				 Map<String,String> stepMap = null;
				 Map<String,String> allowancePlanMap = null;
				 Map<String,String> allowanceElementMap = null;
				 Map<String,String> allowanceCurrencyMap = null;
				 Map<String,String> allowanceFrequencyMap = null;
				 Map<String,String> unitAllowanceMap = null;
				 Map<String,String> unitAllowanceElementMap = null;
				 Map<String,String> unitAllowanceMeasureMap = null;
				 Map<String,String> unitAllowanceCurrencyMap = null;
				 Map<String,String> unitAllowanceFrequencyMap = null;
				 
				 int allowanceCntWD = 0;
				 int allowanceCntSrc = 0;
				 
				 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
				 {					 
					 if(j == 1)
					 {
						startIndex = 0;
						if(1000 > totalResult)
						{
							endIndex = totalResult;
						}
						else
						{
							endIndex = j*1000;
						}
					 }
					 else
					 {
						int lastVal = (j - 1);
						startIndex = (endIndex - lastVal);
						if(j*1000 > totalResult)
						{
							endIndex = totalResult;
						}
						else
						{
							endIndex = (j*startIndex) + 1;
						}
					 }
					 outputfile = addHireIdList(GET_ALLOWANCE_PLAN_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
					 is = new FileInputStream(outputfile);
				     soapMessage = MessageFactory.newInstance().createMessage(null, is);
				     soapPart = soapMessage.getSOAPPart();
				     envelope = soapPart.getEnvelope();
					 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
					 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
					 {
							envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
					 }
					 soapMessage.saveChanges();
			         out = new ByteArrayOutputStream();
			         soapMessage.writeTo(out);
			         strMsg = new String(out.toByteArray());
			        
			         soapConnectionFactory = SOAPConnectionFactory.newInstance();
					 soapConnection = soapConnectionFactory.createConnection();
			         soapResponse = soapConnection.call(soapMessage, sourceUrl);
			         out = new ByteArrayOutputStream();
			         soapResponse.writeTo(out);
			         strMsg = new String(out.toByteArray(), "utf-8");
			        
			         soapResp = XmlParserManager.parseXml(strMsg);
				 
					 ReportElement responseData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Data");
					 
					 List<ReportElement> workerData = responseData.getChildren("wd:Worker");
						
					 for(ReportElement reportElement : workerData)
					 {
						 ReportElement element1 = reportElement.getChild("wd:Worker_Reference");
						 if(element1 != null)
						 {
							 List<ReportElement> idData = element1.getChildren("wd:ID");					 
							 for(ReportElement wdElement:idData)
							 {
								 idMap = wdElement.getAllAttributes();
								 if(idMap.get("wd:type").equals("Employee_ID"))
								 {
									 empId = wdElement.getValue().trim();
								 }
							 }
						 }
						 
						 ReportElement element2 = reportElement.getChild("wd:Worker_Data")
						 							.getChild("wd:Compensation_Data");
						 if(element2 != null)
						 {
							 effectiveDate = element2.getChild("wd:Compensation_Effective_Date") != null?element2.getChild("wd:Compensation_Effective_Date").getValue().trim():"";	
							 
							 ReportElement element3 = element2.getChild("wd:Compensation_Guidelines_Data");
							 if(element3 != null)
							 {
								 ReportElement element4 = element3.getChild("wd:Compensation_Package_Reference");
								 if(element4 != null)
								 {
									 List<ReportElement> packageData = element4.getChildren("wd:ID");					 
									 for(ReportElement packageElement:packageData)
									 {
										 packageMap = packageElement.getAllAttributes();
										 if(packageMap.get("wd:type").equals("Compensation_Package_ID"))
										 {
											 compPackageName = packageElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 compPackageName = "";
								 }
								 
								 ReportElement element5 = element3.getChild("wd:Compensation_Grade_Reference");
								 if(element5 != null)
								 {
									 List<ReportElement> gradeData = element5.getChildren("wd:ID");					 
									 for(ReportElement gradeElement:gradeData)
									 {
										 gradeMap = gradeElement.getAllAttributes();
										 if(gradeMap.get("wd:type").equals("Compensation_Grade_ID"))
										 {
											 compGradeName = gradeElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 compGradeName = "";
								 }
								 
								 ReportElement element6 = element3.getChild("wd:Compensation_Grade_Profile_Reference");
								 if(element6 != null)
								 {
									 List<ReportElement> profileData = element6.getChildren("wd:ID");					 
									 for(ReportElement profileElement:profileData)
									 {
										 profileMap = profileElement.getAllAttributes();
										 if(profileMap.get("wd:type").equals("Compensation_Grade_Profile_ID"))
										 {
											 compProfileName = profileElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 compProfileName = "";
								 }
								 
								 ReportElement element7 = element3.getChild("wd:Compensation_Step_Reference");
								 if(element7 != null)
								 {
									 List<ReportElement> stepData = element7.getChildren("wd:ID");					 
									 for(ReportElement stepElement:stepData)
									 {
										 stepMap = stepElement.getAllAttributes();
										 if(stepMap.get("wd:type").equals("Compensation_Step_ID"))
										 {
											 compStepName = stepElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 compStepName = "";
								 }
							 }
							 else
							 {
								 effectiveDate = "";
								 compPackageName = "";
								 compGradeName = "";
								 compProfileName = "";
								 compStepName = "";
							 }
							 
							 List<ReportElement> allowancePlanData = element2.getChildren("wd:Allowance_Plan_Data");
							 if(allowancePlanData != null)
							 {
								 for(ReportElement allowancePlanElement: allowancePlanData)
								 {
									 ReportElement element8 = allowancePlanElement.getChild("wd:Compensation_Plan_Reference");
									 if(element8 != null)
									 {
										 List<ReportElement> allowanceData = element8.getChildren("wd:ID");					 
										 for(ReportElement allowanceElement:allowanceData)
										 {
											 allowancePlanMap = allowanceElement.getAllAttributes();
											 if(allowancePlanMap.get("wd:type").equals("Compensation_Plan_ID"))
											 {
												 allowancePlanName= allowanceElement.getValue().trim();											
											 }
										 }
									 }
									 else
									 {
										 allowancePlanName = "";
									 }
									 
									 ReportElement element9 = allowancePlanElement.getChild("wd:Compensation_Element_Reference");
									 if(element9 != null)
									 {
										 List<ReportElement> allowanceElementData = element9.getChildren("wd:ID");					 
										 for(ReportElement allowanceElement:allowanceElementData)
										 {
											 allowanceElementMap = allowanceElement.getAllAttributes();
											 if(allowanceElementMap.get("wd:type").equals("Compensation_Element_ID"))
											 {
												 allowanceElementName= allowanceElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 allowanceElementName = "";
									 }
									 
									 amount = allowancePlanElement.getChild("wd:Amount") != null?allowancePlanElement.getChild("wd:Amount").getValue().trim():"";
									 percentage = allowancePlanElement.getChild("wd:Percent") != null?allowancePlanElement.getChild("wd:Percent").getValue().trim():"";
									 
									 ReportElement element10 = allowancePlanElement.getChild("wd:Currency_Reference");
									 if(element10 != null)
									 {
										 List<ReportElement> allowanceCurrencyData = element10.getChildren("wd:ID");					 
										 for(ReportElement allowanceCurrencyElement:allowanceCurrencyData)
										 {
											 allowanceCurrencyMap = allowanceCurrencyElement.getAllAttributes();
											 if(allowanceCurrencyMap.get("wd:type").equals("Currency_ID"))
											 {
												 allowanceCurrency = allowanceCurrencyElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 allowanceCurrency = "";
									 }
									 
									 ReportElement element11 = allowancePlanElement.getChild("wd:Frequency_Reference");
									 if(element11 != null)
									 {
										 List<ReportElement> allowanceFrequencyData = element11.getChildren("wd:ID");					 
										 for(ReportElement allowanceFrequencyElement:allowanceFrequencyData)
										 {
											 allowanceFrequencyMap = allowanceFrequencyElement.getAllAttributes();
											 if(allowanceFrequencyMap.get("wd:type").equals("Frequency_ID"))
											 {
												 allowanceFrequency = allowanceFrequencyElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 allowanceFrequency = "";
									 }
									 
									 unitAllowancePlanName = "";
									 unitAllowanceElementName = "";
									 unitOfMeasureName = "";
									 perUnitAmount = "";
									 unitAllowanceCurrency = "";
									 noOfUnits = "";
									 unitAllowanceFrequency = "";
									 
									 headingFromWD = "Employee_ID,Effective_Date,Compensation_Package_Name,Compensation_Grade_Name,Compensation_Profile_Name,Compensation_Step_Name,Allowance_Plan_Name,"
							 		         + "Allowance_Element_Name,Allowance_Amount,Allowance_Percentage,Allowance_Currency,Allowance_Frequency,Unit_Allowance_Plan_Name,Unit_Allowance_Element_Name,"
							 		         + "Number_of_Units,Unit_of_Measure_Name,Unit_Allowance_Frequency,Per_Unit_Amount,Unit_Allowance_Currency";
								 
									 headerStr = empId + "," + effectiveDate + "," + compPackageName + "," + compGradeName + "," + compProfileName + "," + compStepName + "," + allowancePlanName + "," + 
											 	 allowanceElementName + "," + amount + "," + percentage + "," + allowanceCurrency + "," + allowanceFrequency + "," + unitAllowancePlanName + "," + 
											     unitAllowanceElementName + "," + noOfUnits + "," + unitOfMeasureName + "," + unitAllowanceFrequency + "," + perUnitAmount + "," + unitAllowanceCurrency;
									 
									 if(finalStr.equals(""))
									 {
										 finalStr = headingFromWD + "\n" + headerStr;
									 }
									 else
									 {
										 finalStr = finalStr + "\n" + headerStr;
									 }					
								 }								 
							 }
							 List<ReportElement> unitAllowancePlanData = element2.getChildren("wd:Unit_Allowance_Plan_Data");
							 if(unitAllowancePlanData != null)
							 {
								 for(ReportElement unitAllowancePlanElement: unitAllowancePlanData)
								 {
									 ReportElement element14 = unitAllowancePlanElement.getChild("wd:Compensation_Plan_Reference");
									 if(element14 != null)
									 {
										 List<ReportElement> unitAllowPlanData = element14.getChildren("wd:ID");					 
										 for(ReportElement unitAllowPlanElement:unitAllowPlanData)
										 {
											 unitAllowanceMap = unitAllowPlanElement.getAllAttributes();
											 if(unitAllowanceMap.get("wd:type").equals("Compensation_Plan_ID"))
											 {
												 unitAllowancePlanName= unitAllowPlanElement.getValue().trim();											
											 }
										 }
									 }
									 else
									 {
										 unitAllowancePlanName = "";
									 }
									 
									 ReportElement element15 = unitAllowancePlanElement.getChild("wd:Compensation_Element_Reference");
									 if(element15 != null)
									 {
										 List<ReportElement> unitAllowanceElementData = element15.getChildren("wd:ID");					 
										 for(ReportElement unitAllowanceElementElement:unitAllowanceElementData)
										 {
											 unitAllowanceElementMap = unitAllowanceElementElement.getAllAttributes();
											 if(unitAllowanceElementMap.get("wd:type").equals("Compensation_Element_ID"))
											 {
												 unitAllowanceElementName= unitAllowanceElementElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 unitAllowanceElementName = "";
									 }
									 
									 ReportElement element16 = unitAllowancePlanElement.getChild("wd:Unit_Reference");
									 if(element16 != null)
									 {
										 List<ReportElement> unitMeasureElementData = element16.getChildren("wd:ID");					 
										 for(ReportElement unitSalaryMeasureElement:unitMeasureElementData)
										 {
											 unitAllowanceMeasureMap = unitSalaryMeasureElement.getAllAttributes();
											 if(unitAllowanceMeasureMap.get("wd:type").equals("UN_CEFACT_Common_Code_ID"))
											 {
												 unitOfMeasureName= unitSalaryMeasureElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 unitOfMeasureName = "";
									 }
									 
									 noOfUnits = unitAllowancePlanElement.getChild("wd:Number_of_Units") != null?unitAllowancePlanElement.getChild("wd:Number_of_Units").getValue().trim():"";
									 perUnitAmount = unitAllowancePlanElement.getChild("wd:Per_Unit_Amount") != null?unitAllowancePlanElement.getChild("wd:Per_Unit_Amount").getValue().trim():"";
									 
									 ReportElement element17 = unitAllowancePlanElement.getChild("wd:Currency_Reference");
									 if(element17 != null)
									 {
										 List<ReportElement> unitCurrencyElementData = element17.getChildren("wd:ID");					 
										 for(ReportElement unitCurrencyElement:unitCurrencyElementData)
										 {
											 unitAllowanceCurrencyMap = unitCurrencyElement.getAllAttributes();
											 if(unitAllowanceCurrencyMap.get("wd:type").equals("Currency_ID"))
											 {
												 unitAllowanceCurrency = unitCurrencyElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 unitAllowanceCurrency = "";
									 }
									 
									 ReportElement element18 = unitAllowancePlanElement.getChild("wd:Frequency_Reference");
									 if(element18 != null)
									 {
										 List<ReportElement> unitFrequencyElementData = element18.getChildren("wd:ID");					 
										 for(ReportElement unitFrequencyElement:unitFrequencyElementData)
										 {
											 unitAllowanceFrequencyMap = unitFrequencyElement.getAllAttributes();
											 if(unitAllowanceFrequencyMap.get("wd:type").equals("Frequency_ID"))
											 {
												 unitAllowanceFrequency = unitFrequencyElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 unitAllowanceFrequency = "";
									 }
									 
									 allowancePlanName = "";
									 allowanceElementName = "";
									 amount = "";
									 percentage = "";
									 allowanceCurrency = "";
									 allowanceFrequency = "";
									 
									 headingFromWD = "Employee_ID,Effective_Date,Compensation_Package_Name,Compensation_Grade_Name,Compensation_Profile_Name,Compensation_Step_Name,Allowance_Plan_Name,"
							 		         + "Allowance_Element_Name,Amount,Percentage,Allowance_Currency,Allowance_Frequency,Unit_Allowance_Plan_Name,Unit_Allowance_Element_Name,Number_of_Units,"
							 		         + "Unit_of_Measure_Name,Unit_Allowance_Frequency,Per_Unit_Amount,Unit_Allowance_Currency";
									 
									 headerStr = empId + "," + effectiveDate + "," + compPackageName + "," + compGradeName + "," + compProfileName + "," + compStepName + "," + allowancePlanName + "," + 
										 	 allowanceElementName + "," + amount + "," + percentage + "," + allowanceCurrency + "," + allowanceFrequency + "," + unitAllowancePlanName + "," + 
										     unitAllowanceElementName + "," + noOfUnits + "," + unitOfMeasureName + "," + unitAllowanceFrequency + "," + perUnitAmount + "," + unitAllowanceCurrency;
								 
									 if(finalStr.equals(""))
									 {
										 finalStr = headingFromWD + "\n" + headerStr;
									 }
									 else
									 {
										 finalStr = finalStr + "\n" + headerStr;
									 }				
								 }
							 }
						 }						 						  
					 }
				 }
				 
				 System.out.println(finalStr);
				 
				 String[] headingFromWdArr = headingFromWD.split(",");
				 for(int i = 0;i<headingFromWdArr.length; i++)
				 {
					String headingValWd = headingFromWdArr[i].replace("_", " ");
					if(headingValWd.equalsIgnoreCase("Allowance Plan Name"))
					{
						allowanceCntWD = i;
					}
				 }
				 
				 String[] headingFromSrcArr = headingFromSource.split(",");
				 for(int i = 0;i<headingFromSrcArr.length; i++)
				 {
					String headingValSrc = headingFromSrcArr[i].replace("_", " ");
					if(headingValSrc.equalsIgnoreCase("Allowance Plan Name"))
					{
						allowanceCntSrc = i;
					}
				 }
				 
				 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
				 byte[] srcCSVFileContent = postLoad.getSrcCSVFileContent();
				 File sourceCSVfile = null;
				 try 
				 {
					 sourceCSVfile = File.createTempFile(postLoad.getSrcCSVFileName().substring(0, postLoad.getSrcCSVFileName().indexOf(".")), ".csv");
					 FileUtils.writeByteArrayToFile(sourceCSVfile, srcCSVFileContent);
				 } 
				 catch (IOException e1) 
				 {
					 e1.printStackTrace();
				 }
				 String srcFile = sourceCSVfile.getAbsolutePath();
				 
				 String line = "";
				 int count = 0;
			     BufferedReader reader = new BufferedReader(new FileReader(srcFile));
			     while ((line = reader.readLine()) != null) 
			     { 
			         if(count != 0 && line.length() > 0) 
			         {
			        	 String [] lineArr = line.split(",");
			        	 if(lineArr.length >1)
			        	 {
				        	 String existStr = updateWDCSVFileForMultipleRow(lineArr[0], lineArr[allowanceCntSrc], allowanceCntWD, finalStr);
				        	 if(existStr.length() > 0)
				        	 {
				        		 if(newFinalStr.equals(""))
								 {
				        			 newFinalStr = headingFromWD + "\n" + existStr;
								 }
								 else
								 {
									 newFinalStr = newFinalStr + "\n" + existStr;
								 }
				        	 }
			        	 }
			         }
			         count++;
			     }
			     reader.close();
			     
			     System.out.println(newFinalStr);
				 
				 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
				 PrintWriter writer = new PrintWriter(wdCSVfile);
				 writer.write(newFinalStr.toString());
				 writer.flush();
				 writer.close();
				 
				 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
				 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
				 postLoadService.updatePostLoad(postLoad);
				 
				 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
				 
				 /*String wdCSVfile1 = "config/" + loadCycle + "_" + ruleName + ".csv";			 
				 PrintWriter writer1 = new PrintWriter(new File(wdCSVfile1));
				 writer1.write(finalStr.toString());
				 writer1.flush();
				 writer1.close();*/
			 }
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}

	private JSONArray createCSVFromWDBonusPlan(Tenant tenant, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_BONUS_PLAN_FILE = requestfile.getAbsolutePath();
				 //String outputfile = GET_BONUS_PLAN_FILE;
				 String outputfile = addHireIdList(GET_BONUS_PLAN_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");

				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");			 
				 
				 ReportElement soapResp = XmlParserManager.parseXml(strMsg);
				 
				 ReportElement pageData = soapResp.getChild("env:Body")
							.getChild("wd:Get_Workers_Response")
							.getChild("wd:Response_Results");
				 
				 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
				 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
				 int totalResult = Integer.parseInt(totalResults);
				 System.out.println("totalNoOfPages-"+totalNoOfPages);
				 System.out.println("totalResult-"+totalResult);
				 wdCount = totalResult;
				 
				 String finalStr = "";
				 String headerStr = "";
				 String newFinalStr = "";
				 String empId = "";
				 String effectiveDate = "";
				 String compPackageName = "";
				 String compGradeName = "";
				 String compProfileName = "";
				 String compStepName = "";
				 String bonusPlanName = "";
				 String bonusElementName = "";
				 String amount = "";
				 String percentage = "";
				 String guaranteedMinimum = "";
				 String currencyCode = "";
				 String frequencyName = "";
				 String percentageAssigned = "";
				 
				 Map<String,String> idMap = null;
				 Map<String,String> packageMap = null;
				 Map<String,String> gradeMap = null;
				 Map<String,String> profileMap = null;
				 Map<String,String> stepMap = null;
				 Map<String,String> bonusPlanMap = null;
				 Map<String,String> bonusElementMap = null;
				 Map<String,String> bonusCurrencyMap = null;
				 Map<String,String> bonusFrequencyMap = null;
				 
				 int bonusCntWD = 0;
				 int bonusCntSrc = 0;
				 
				 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
				 {					 
					 if(j == 1)
					 {
						startIndex = 0;
						if(1000 > totalResult)
						{
							endIndex = totalResult;
						}
						else
						{
							endIndex = j*1000;
						}
					 }
					 else
					 {
						int lastVal = (j - 1);
						startIndex = (endIndex - lastVal);
						if(j*1000 > totalResult)
						{
							endIndex = totalResult;
						}
						else
						{
							endIndex = (j*startIndex) + 1;
						}
					 }
					 outputfile = addHireIdList(GET_BONUS_PLAN_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
					 is = new FileInputStream(outputfile);
				     soapMessage = MessageFactory.newInstance().createMessage(null, is);
				     soapPart = soapMessage.getSOAPPart();
				     envelope = soapPart.getEnvelope();
					 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
					 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
					 {
							envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
					 }
					 soapMessage.saveChanges();
			         out = new ByteArrayOutputStream();
			         soapMessage.writeTo(out);
			         strMsg = new String(out.toByteArray());
			        
			         soapConnectionFactory = SOAPConnectionFactory.newInstance();
					 soapConnection = soapConnectionFactory.createConnection();
			         soapResponse = soapConnection.call(soapMessage, sourceUrl);
			         out = new ByteArrayOutputStream();
			         soapResponse.writeTo(out);
			         strMsg = new String(out.toByteArray(), "utf-8");
			        
			         soapResp = XmlParserManager.parseXml(strMsg);
				 
					 ReportElement responseData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Data");
					 
					 List<ReportElement> workerData = responseData.getChildren("wd:Worker");
						
					 for(ReportElement reportElement : workerData)
					 {
						 ReportElement element1 = reportElement.getChild("wd:Worker_Reference");
						 if(element1 != null)
						 {
							 List<ReportElement> idData = element1.getChildren("wd:ID");					 
							 for(ReportElement wdElement:idData)
							 {
								 idMap = wdElement.getAllAttributes();
								 if(idMap.get("wd:type").equals("Employee_ID"))
								 {
									 empId = wdElement.getValue().trim();
								 }
							 }
						 }
						 
						 ReportElement element2 = reportElement.getChild("wd:Worker_Data")
						 							.getChild("wd:Compensation_Data");
						 if(element2 != null)
						 {
							 effectiveDate = element2.getChild("wd:Compensation_Effective_Date") != null?element2.getChild("wd:Compensation_Effective_Date").getValue().trim():"";	
							 
							 ReportElement element3 = element2.getChild("wd:Compensation_Guidelines_Data");
							 if(element3 != null)
							 {
								 ReportElement element4 = element3.getChild("wd:Compensation_Package_Reference");
								 if(element4 != null)
								 {
									 List<ReportElement> packageData = element4.getChildren("wd:ID");					 
									 for(ReportElement packageElement:packageData)
									 {
										 packageMap = packageElement.getAllAttributes();
										 if(packageMap.get("wd:type").equals("Compensation_Package_ID"))
										 {
											 compPackageName = packageElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 compPackageName = "";
								 }
								 
								 ReportElement element5 = element3.getChild("wd:Compensation_Grade_Reference");
								 if(element5 != null)
								 {
									 List<ReportElement> gradeData = element5.getChildren("wd:ID");					 
									 for(ReportElement gradeElement:gradeData)
									 {
										 gradeMap = gradeElement.getAllAttributes();
										 if(gradeMap.get("wd:type").equals("Compensation_Grade_ID"))
										 {
											 compGradeName = gradeElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 compGradeName = "";
								 }
								 
								 ReportElement element6 = element3.getChild("wd:Compensation_Grade_Profile_Reference");
								 if(element6 != null)
								 {
									 List<ReportElement> profileData = element6.getChildren("wd:ID");					 
									 for(ReportElement profileElement:profileData)
									 {
										 profileMap = profileElement.getAllAttributes();
										 if(profileMap.get("wd:type").equals("Compensation_Grade_Profile_ID"))
										 {
											 compProfileName = profileElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 compProfileName = "";
								 }
								 
								 ReportElement element7 = element3.getChild("wd:Compensation_Step_Reference");
								 if(element7 != null)
								 {
									 List<ReportElement> stepData = element7.getChildren("wd:ID");					 
									 for(ReportElement stepElement:stepData)
									 {
										 stepMap = stepElement.getAllAttributes();
										 if(stepMap.get("wd:type").equals("Compensation_Step_ID"))
										 {
											 compStepName = stepElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 compStepName = "";
								 }
							 }
							 else
							 {
								 effectiveDate = "";
								 compPackageName = "";
								 compGradeName = "";
								 compProfileName = "";
								 compStepName = "";
							 }
							 
							 List<ReportElement> bonusPlanData = element2.getChildren("wd:Bonus_Plan_Data");
							 if(bonusPlanData != null)
							 {
								 for(ReportElement bonusPlanElement: bonusPlanData)
								 {
									 ReportElement element8 = bonusPlanElement.getChild("wd:Compensation_Plan_Reference");
									 if(element8 != null)
									 {
										 List<ReportElement> bonusData = element8.getChildren("wd:ID");					 
										 for(ReportElement bonusElement:bonusData)
										 {
											 bonusPlanMap = bonusElement.getAllAttributes();
											 if(bonusPlanMap.get("wd:type").equals("Compensation_Plan_ID"))
											 {
												 bonusPlanName= bonusElement.getValue().trim();											
											 }
										 }
									 }
									 else
									 {
										 bonusPlanName = "";
									 }
									 
									 ReportElement element9 = bonusPlanElement.getChild("wd:Compensation_Element_Reference");
									 if(element9 != null)
									 {
										 List<ReportElement> bonusElementData = element9.getChildren("wd:ID");					 
										 for(ReportElement bonusElement:bonusElementData)
										 {
											 bonusElementMap = bonusElement.getAllAttributes();
											 if(bonusElementMap.get("wd:type").equals("Compensation_Element_ID"))
											 {
												 bonusElementName= bonusElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 bonusElementName = "";
									 }
									 
									 amount = bonusPlanElement.getChild("wd:Default_Target_Amount") != null?bonusPlanElement.getChild("wd:Default_Target_Amount").getValue().trim():"";
									 percentage = bonusPlanElement.getChild("wd:Default_Target_Percent") != null?bonusPlanElement.getChild("wd:Default_Target_Percent").getValue().trim():"";
									 guaranteedMinimum = bonusPlanElement.getChild("wd:Guaranteed_Minimum") != null?bonusPlanElement.getChild("wd:Guaranteed_Minimum").getValue().trim():"";
									 if(guaranteedMinimum.equals("1"))
									 {
										 guaranteedMinimum = "TRUE";
									 }
									 else
									 {
										 guaranteedMinimum = "FALSE";
									 }
									 percentageAssigned = bonusPlanElement.getChild("wd:Percent_Assigned") != null?bonusPlanElement.getChild("wd:Percent_Assigned").getValue().trim():"";
									 
									 ReportElement element10 = bonusPlanElement.getChild("wd:Currency_Reference");
									 if(element10 != null)
									 {
										 List<ReportElement> bonusCurrencyData = element10.getChildren("wd:ID");					 
										 for(ReportElement bonusCurrencyElement:bonusCurrencyData)
										 {
											 bonusCurrencyMap = bonusCurrencyElement.getAllAttributes();
											 if(bonusCurrencyMap.get("wd:type").equals("Currency_ID"))
											 {
												 currencyCode = bonusCurrencyElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 currencyCode = "";
									 }
									 
									 ReportElement element11 = bonusPlanElement.getChild("wd:Frequency_Reference");
									 if(element11 != null)
									 {
										 List<ReportElement> bonusFrequencyData = element11.getChildren("wd:ID");					 
										 for(ReportElement bonusFrequencyElement:bonusFrequencyData)
										 {
											 bonusFrequencyMap = bonusFrequencyElement.getAllAttributes();
											 if(bonusFrequencyMap.get("wd:type").equals("Frequency_ID"))
											 {
												 frequencyName = bonusFrequencyElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 frequencyName = "";
									 }
									 
									 headingFromWD = "Employee_ID,Effective_Date,Compensation_Package_Name,Compensation_Grade_Name,Compensation_Profile_Name,Compensation_Step_Name,Bonus_Plan_Name,"
							 		         + "Bonus_Element_Name,Amount,Percentage,Guaranteed_Minimum,Currency_Code,Frequency_Name,Percentage_Assigned";
								 
									 headerStr = empId + "," + effectiveDate + "," + compPackageName + "," + compGradeName + "," + compProfileName + "," + compStepName + "," + bonusPlanName + "," + 
											 	 bonusElementName + "," + amount + "," + percentage + "," + guaranteedMinimum + "," + currencyCode + "," + frequencyName + "," + percentageAssigned;
									 
									 if(finalStr.equals(""))
									 {
										 finalStr = headingFromWD + "\n" + headerStr;
									 }
									 else
									 {
										 finalStr = finalStr + "\n" + headerStr;
									 }					
								 }								 
							 }
						 }						 						  
					 }
				 }
				 
				 System.out.println(finalStr);
				 
				 String[] headingFromWdArr = headingFromWD.split(",");
				 for(int i = 0;i<headingFromWdArr.length; i++)
				 {
					String headingValWd = headingFromWdArr[i].replace("_", " ");
					if(headingValWd.equalsIgnoreCase("Bonus Plan Name"))
					{
						bonusCntWD = i;
					}
				 }
				 
				 String[] headingFromSrcArr = headingFromSource.split(",");
				 for(int i = 0;i<headingFromSrcArr.length; i++)
				 {
					String headingValSrc = headingFromSrcArr[i].replace("_", " ");
					if(headingValSrc.equalsIgnoreCase("Bonus Plan Name"))
					{
						bonusCntSrc = i;
					}
				 }
				 
				 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
				 byte[] srcCSVFileContent = postLoad.getSrcCSVFileContent();
				 File sourceCSVfile = null;
				 try 
				 {
					 sourceCSVfile = File.createTempFile(postLoad.getSrcCSVFileName().substring(0, postLoad.getSrcCSVFileName().indexOf(".")), ".csv");
					 FileUtils.writeByteArrayToFile(sourceCSVfile, srcCSVFileContent);
				 } 
				 catch (IOException e1) 
				 {
					 e1.printStackTrace();
				 }
				 String srcFile = sourceCSVfile.getAbsolutePath();
				 
				 String line = "";
				 int count = 0;
			     BufferedReader reader = new BufferedReader(new FileReader(srcFile));
			     while ((line = reader.readLine()) != null) 
			     { 
			         if(count != 0 && line.length() > 0) 
			         {
			        	 String [] lineArr = line.split(",");
			        	 if(lineArr.length >1)
			        	 {
				        	 String existStr = updateWDCSVFileForMultipleRow(lineArr[0], lineArr[bonusCntSrc], bonusCntWD, finalStr);
				        	 if(existStr.length() > 0)
				        	 {
				        		 if(newFinalStr.equals(""))
								 {
				        			 newFinalStr = headingFromWD + "\n" + existStr;
								 }
								 else
								 {
									 newFinalStr = newFinalStr + "\n" + existStr;
								 }
				        	 }
			        	 }
			         }
			         count++;
			     }
			     reader.close();
			     
			     System.out.println(newFinalStr);
				 
				 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
				 PrintWriter writer = new PrintWriter(wdCSVfile);
				 writer.write(newFinalStr.toString());
				 writer.flush();
				 writer.close();
				 
				 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
				 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
				 postLoadService.updatePostLoad(postLoad);
				 
				 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
				 
				 /*String wdCSVfile1 = "config/" + loadCycle + "_" + ruleName + ".csv";			 
				 PrintWriter writer1 = new PrintWriter(new File(wdCSVfile1));
				 writer1.write(finalStr.toString());
				 writer1.flush();
				 writer1.close();*/
			 }
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}

	private JSONArray createCSVFromWDEEBaseCompensation(Tenant tenant, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_EE_BASE_COMPENSATION_FILE = requestfile.getAbsolutePath();
				 //String outputfile = GET_EE_BASE_COMPENSATION_FILE;
				 /*if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addWorkerIdListToFindError(GET_EE_BASE_COMPENSATION_FILE, columnList.get(i), ruleName, "Employee_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement reportElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = reportElement.getChild("env:Body")
										.getChild("wd:Get_Workers_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
						 }
					 }
					 columnList.removeAll(errorList);
				 }*/
				 String outputfile = addHireIdList(GET_EE_BASE_COMPENSATION_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");

				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");			 
				 
				 ReportElement soapResp = XmlParserManager.parseXml(strMsg);
				 
				 ReportElement pageData = soapResp.getChild("env:Body")
							.getChild("wd:Get_Workers_Response")
							.getChild("wd:Response_Results");
				 
				 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
				 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
				 int totalResult = Integer.parseInt(totalResults);
				 System.out.println("totalNoOfPages-"+totalNoOfPages);
				 System.out.println("totalResult-"+totalResult);
				 wdCount = totalResult;
				 
				 String finalStr = "";
				 String headerStr = "";
				 String empId = "";
				 String positionId = "";
				 String effectiveDate = "";
				 String compReasonName = "";
				 String compPackageName = "";
				 String compGradeName = "";
				 String compProfileName = "";
				 String compStepName = "";
				 String salaryPlanName = "";
				 String salaryElementName = "";
				 String salaryElementAmount = "";
				 String salaryCurrency = "";
				 String salaryFrequency = "";
				 String hourlyPlanName = "";
				 String hourlyElementName = "";
				 String hourlyElementAmount = "";
				 String hourlyCurrency = "";
				 String hourlyFrequency = "";
				 String bonusPlanName = "";
				 String bonusElementName = "";
				 String bonusAmount = "";
				 String bonusPercentage = "";
				 String bonusGuaranteedMinimum = "";
				 String bonusCurrencyCode = "";
				 String bonusFrequencyName = "";
				 String bonusPercentageAssigned = "";
				 String bonusPlanNameArr = "";
				 String bonusElementNameArr = "";
				 String bonusAmountArr = "";
				 String bonusPercentageArr = "";
				 String bonusGuaranteedMinimumArr = "";
				 String bonusCurrencyCodeArr = "";
				 String bonusFrequencyNameArr = "";
				 String bonusPercentageAssignedArr = "";
				 String bonusActualEndDate = "";
				 String bonusActualEndDateArr = "";
				 String allowancePlanName = "";
				 String allowanceElementName = "";
				 String allowanceAmount = "";
				 String allowancePercentage = "";
				 String allowanceCurrency = "";
				 String allowanceFrequency = "";
				 String allowancePlanNameArr = "";
				 String allowanceElementNameArr = "";
				 String allowanceAmountArr = "";
				 String allowancePercentageArr = "";
				 String allowanceCurrencyArr = "";
				 String allowanceFrequencyArr = "";
				 String allowanceActualEndDate = "";
				 String allowanceActualEndDateArr = "";
				 String unitAllowancePlanName = "";
				 String unitAllowanceElementName = "";
				 String unitAllowanceUnitOfMeasureName = "";
				 String unitAllowancePerUnitAmount = "";
				 String unitAllowanceCurrency = "";
				 String unitAllowanceNoOfUnits = "";
				 String unitAllowanceFrequency = "";
				 String unitAllowanceActualEndDate = "";
				 String periodSalaryPlanName = "";
				 String periodSalaryElementName = "";
				 String compensationPeriod = "";
				 String compensationPeriodMultiplier = "";
				 String periodSalaryCurrency = "";
				 String periodSalaryFrequency = "";
				 String periodActualendDate = "";
				 String calculatedPlanName = "";
				 String amountOverride = "";
				 String calculatedCurrency = "";
				 String calculatedFrequency = "";
				 String calculatedActualEndDate = "";
				 
				 Map<String,String> idMap = null;
				 Map<String,String> reasonMap = null;
				 Map<String,String> packageMap = null;
				 Map<String,String> gradeMap = null;
				 Map<String,String> profileMap = null;
				 Map<String,String> stepMap = null;
				 Map<String,String> salHourMap = null;
				 Map<String,String> salHourElementMap = null;
				 Map<String,String> salHourCurrencyMap = null;
				 Map<String,String> salHourFrequencyMap = null;
				 Map<String,String> bonusPlanMap = null;
				 Map<String,String> bonusElementMap = null;
				 Map<String,String> bonusCurrencyMap = null;
				 Map<String,String> bonusFrequencyMap = null;
				 Map<String,String> allowancePlanMap = null;
				 Map<String,String> allowanceElementMap = null;
				 Map<String,String> allowanceCurrencyMap = null;
				 Map<String,String> allowanceFrequencyMap = null;
				 Map<String,String> unitAllowanceMap = null;
				 Map<String,String> unitAllowanceElementMap = null;
				 Map<String,String> unitAllowanceMeasureMap = null;
				 Map<String,String> unitAllowanceCurrencyMap = null;
				 Map<String,String> unitAllowanceFrequencyMap = null;
				 Map<String,String> periodSalaryPlanMap = null;
				 Map<String,String> periodSalaryElementMap = null;
				 Map<String,String> compensationPeriodMap = null;
				 Map<String,String> periodSalaryCurrencyMap = null;
				 Map<String,String> periodSalaryFrequencyMap = null;
				 Map<String,String> calculatedPlanMap = null;
				 Map<String,String> calculatedCurrencyMap = null;
				 Map<String,String> calculatedFrequencyMap = null;
				 
				 boolean isHourly = false;
				 
				 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
				 {					 
					 if(j == 1)
					 {
						startIndex = 0;
						if(1000 > totalResult)
						{
							endIndex = totalResult;
						}
						else
						{
							endIndex = j*1000;
						}
					 }
					 else
					 {
						startIndex = endIndex;
						if(j*1000 > totalResult)
						{
							endIndex = totalResult;
						}
						else
						{
							endIndex = (j*1000);
						}
					 }
					 outputfile = addHireIdList(GET_EE_BASE_COMPENSATION_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
					 is = new FileInputStream(outputfile);
				     soapMessage = MessageFactory.newInstance().createMessage(null, is);
				     soapPart = soapMessage.getSOAPPart();
				     envelope = soapPart.getEnvelope();
					 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
					 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
					 {
							envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
					 }
					 soapMessage.saveChanges();
			         out = new ByteArrayOutputStream();
			         soapMessage.writeTo(out);
			         strMsg = new String(out.toByteArray());
			        
			         soapConnectionFactory = SOAPConnectionFactory.newInstance();
					 soapConnection = soapConnectionFactory.createConnection();
			         soapResponse = soapConnection.call(soapMessage, sourceUrl);
			         out = new ByteArrayOutputStream();
			         soapResponse.writeTo(out);
			         strMsg = new String(out.toByteArray(), "utf-8");
			        
			         soapResp = XmlParserManager.parseXml(strMsg);
				 
					 ReportElement responseData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Data");
					 
					 List<ReportElement> workerData = responseData.getChildren("wd:Worker");
						
					 for(ReportElement reportElement : workerData)
					 {
						 isHourly = false;
						 ReportElement element1 = reportElement.getChild("wd:Worker_Reference");
						 if(element1 != null)
						 {
							 List<ReportElement> idData = element1.getChildren("wd:ID");					 
							 for(ReportElement wdElement:idData)
							 {
								 idMap = wdElement.getAllAttributes();
								 if(idMap.get("wd:type").equals("Employee_ID"))
								 {
									 empId = wdElement.getValue().trim();
								 }
							 }
						 }
						 
						 ReportElement element11 = reportElement.getChild("wd:Worker_Data")
		 							.getChild("wd:Employment_Data")
		 							.getChild("wd:Worker_Job_Data");
						 if(element11 != null)
						 {
							 ReportElement element21 = element11.getChild("wd:Position_Data");
							 if(element21 != null)
							 {
								 positionId = element21.getChild("wd:Position_ID") != null?element21.getChild("wd:Position_ID").getValue().trim():"";
							 }
						 }
						 
						 ReportElement element2 = reportElement.getChild("wd:Worker_Data")
						 							.getChild("wd:Compensation_Data");
						 if(element2 != null)
						 {
							 effectiveDate = element2.getChild("wd:Compensation_Effective_Date") != null?element2.getChild("wd:Compensation_Effective_Date").getValue().trim():"";
							 if(!effectiveDate.isEmpty())
							 {
								 effectiveDate = effectiveDate.substring(0, 10); 
							 }
							 
							 ReportElement reasonRef = element2.getChild("wd:Reason_Reference");
							 if(reasonRef != null)
							 {
								 List<ReportElement> reasonData = reasonRef.getChildren("wd:ID");					 
								 for(ReportElement reasonElement:reasonData)
								 {
									 reasonMap = reasonElement.getAllAttributes();
									 if(reasonMap.get("wd:type").equals("Event_Classification_Subcategory_ID"))
									 {
										 compReasonName = reasonElement.getValue().trim();
									 }
								 }
							 }
							 else
							 {
								 compReasonName = "";
							 }
							 
							 ReportElement element3 = element2.getChild("wd:Compensation_Guidelines_Data");
							 if(element3 != null)
							 {
								 ReportElement element4 = element3.getChild("wd:Compensation_Package_Reference");
								 if(element4 != null)
								 {
									 List<ReportElement> packageData = element4.getChildren("wd:ID");					 
									 for(ReportElement packageElement:packageData)
									 {
										 packageMap = packageElement.getAllAttributes();
										 if(packageMap.get("wd:type").equals("Compensation_Package_ID"))
										 {
											 compPackageName = packageElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 compPackageName = "";
								 }
								 
								 ReportElement element5 = element3.getChild("wd:Compensation_Grade_Reference");
								 if(element5 != null)
								 {
									 List<ReportElement> gradeData = element5.getChildren("wd:ID");					 
									 for(ReportElement gradeElement:gradeData)
									 {
										 gradeMap = gradeElement.getAllAttributes();
										 if(gradeMap.get("wd:type").equals("Compensation_Grade_ID"))
										 {
											 compGradeName = gradeElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 compGradeName = "";
								 }
								 
								 ReportElement element6 = element3.getChild("wd:Compensation_Grade_Profile_Reference");
								 if(element6 != null)
								 {
									 List<ReportElement> profileData = element6.getChildren("wd:ID");					 
									 for(ReportElement profileElement:profileData)
									 {
										 profileMap = profileElement.getAllAttributes();
										 if(profileMap.get("wd:type").equals("Compensation_Grade_Profile_ID"))
										 {
											 compProfileName = profileElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 compProfileName = "";
								 }
								 
								 ReportElement element7 = element3.getChild("wd:Compensation_Step_Reference");
								 if(element7 != null)
								 {
									 List<ReportElement> stepData = element7.getChildren("wd:ID");					 
									 for(ReportElement stepElement:stepData)
									 {
										 stepMap = stepElement.getAllAttributes();
										 if(stepMap.get("wd:type").equals("Compensation_Step_ID"))
										 {
											 compStepName = stepElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 compStepName = "";
								 }
							 }
							 else
							 {
								 effectiveDate = "";
								 compPackageName = "";
								 compGradeName = "";
								 compProfileName = "";
								 compStepName = "";
							 }
							 
							 ReportElement salaryOrHourlyPlanData = element2.getChild("wd:Salary_and_Hourly_Data");
							 if(salaryOrHourlyPlanData != null)
							 {
								 ReportElement salHourPlanRef = salaryOrHourlyPlanData.getChild("wd:Compensation_Plan_Reference");
								 if(salHourPlanRef != null)
								 {
									 List<ReportElement> salHourData = salHourPlanRef.getChildren("wd:ID");					 
									 for(ReportElement salHourElement:salHourData)
									 {
										 salHourMap = salHourElement.getAllAttributes();
										 if(salHourMap.get("wd:type").equals("Compensation_Plan_ID"))
										 {
											 isHourly = getSalaryOrHourly(salHourElement.getValue().trim());											 
											 if(isHourly)
											 {
												 hourlyPlanName = salHourElement.getValue().trim();
												 salaryPlanName = "";
											 }
											 else
											 {
												 salaryPlanName = salHourElement.getValue().trim();
												 hourlyPlanName = "";
											 }
										 }
									 }
								 }
								 else
								 {
									 hourlyPlanName = "";
									 salaryPlanName = "";
								 }
								 
								 ReportElement salHourCompElementRef = salaryOrHourlyPlanData.getChild("wd:Compensation_Element_Reference");
								 if(salHourCompElementRef != null)
								 {
									 List<ReportElement> salHourElementData = salHourCompElementRef.getChildren("wd:ID");					 
									 for(ReportElement salHourElementElement:salHourElementData)
									 {
										 salHourElementMap = salHourElementElement.getAllAttributes();
										 if(salHourElementMap.get("wd:type").equals("Compensation_Element_ID"))
										 {
											 if(isHourly)
											 {
												 hourlyElementName = salHourElementElement.getValue().trim();
												 salaryElementName = "";
											 }
											 else
											 {
												 salaryElementName = salHourElementElement.getValue().trim();
												 hourlyElementName = "";
											 }
										 }
									 }
								 }
								 else
								 {
									 hourlyElementName = "";
									 salaryElementName = "";
								 }
								 
								 if(isHourly)
								 {
									 hourlyElementAmount = salaryOrHourlyPlanData.getChild("wd:Amount") != null?salaryOrHourlyPlanData.getChild("wd:Amount").getValue().trim():"";	
									 salaryElementAmount = "";
								 }
								 else
								 {
									 salaryElementAmount = salaryOrHourlyPlanData.getChild("wd:Amount") != null?salaryOrHourlyPlanData.getChild("wd:Amount").getValue().trim():"";
									 hourlyElementAmount = "";
								 }
								 
								 ReportElement salHourCountryRef = salaryOrHourlyPlanData.getChild("wd:Currency_Reference");
								 if(salHourCountryRef != null)
								 {
									 List<ReportElement> salHourCurrencyData = salHourCountryRef.getChildren("wd:ID");					 
									 for(ReportElement salHourCurrencyElement:salHourCurrencyData)
									 {
										 salHourCurrencyMap = salHourCurrencyElement.getAllAttributes();
										 if(salHourCurrencyMap.get("wd:type").equals("Currency_ID"))
										 {
											 if(isHourly)
											 {
												 hourlyCurrency = salHourCurrencyElement.getValue().trim();
												 salaryCurrency = "";
											 }
											 else
											 {
												 salaryCurrency = salHourCurrencyElement.getValue().trim();
												 hourlyCurrency = "";
											 }
										 }
									 }
								 }
								 else
								 {
									 hourlyCurrency = "";
									 salaryCurrency = "";
								 }
								 
								 ReportElement salHourFrequencyRef = salaryOrHourlyPlanData.getChild("wd:Frequency_Reference");
								 if(salHourFrequencyRef != null)
								 {
									 List<ReportElement> salHourFrequencyData = salHourFrequencyRef.getChildren("wd:ID");					 
									 for(ReportElement salHourFrequencyElement:salHourFrequencyData)
									 {
										 salHourFrequencyMap = salHourFrequencyElement.getAllAttributes();
										 if(salHourFrequencyMap.get("wd:type").equals("Frequency_ID"))
										 {
											 if(isHourly)
											 {
												 hourlyFrequency = salHourFrequencyElement.getValue().trim();
												 salaryFrequency = "";
											 }
											 else
											 {
												 salaryFrequency = salHourFrequencyElement.getValue().trim();
												 hourlyFrequency = "";
											 }
										 }
									 }
								 }
								 else
								 {
									 hourlyFrequency = "";
									 salaryFrequency = "";
								 }
							 }
							 else
							 {
								 hourlyPlanName = "";
								 salaryPlanName = "";
								 hourlyElementName = "";
								 salaryElementName = "";
								 salaryElementAmount = "";
								 hourlyElementAmount = "";
								 hourlyCurrency = "";
								 salaryCurrency = "";
								 hourlyFrequency = "";
								 salaryFrequency = "";
							 }							 
							 
							 List<ReportElement> bonusPlanList = element2.getChildren("wd:Bonus_Plan_Data");
							 if(bonusPlanList != null && bonusPlanList.size() >0)
							 {
								 bonusPlanNameArr = "";
								 bonusElementNameArr = "";
								 bonusAmountArr = "";
								 bonusPercentageArr = "";
								 bonusGuaranteedMinimumArr = "";
								 bonusCurrencyCodeArr = "";
								 bonusFrequencyNameArr = "";
								 bonusPercentageAssignedArr = "";
								 bonusActualEndDateArr = "";
								 
								 for(ReportElement bonusPlanElement: bonusPlanList)
								 {
									 ReportElement bonusCompPlanRef = bonusPlanElement.getChild("wd:Compensation_Plan_Reference");
									 if(bonusCompPlanRef != null)
									 {
										 List<ReportElement> bonusData = bonusCompPlanRef.getChildren("wd:ID");					 
										 for(ReportElement bonusElement:bonusData)
										 {
											 bonusPlanMap = bonusElement.getAllAttributes();
											 if(bonusPlanMap.get("wd:type").equals("Compensation_Plan_ID"))
											 {
												 bonusPlanName= bonusElement.getValue().trim();											
											 }
										 }
									 }
									 else
									 {
										 bonusPlanName = "";
									 }
									 if(bonusPlanNameArr.equals(""))
									 {
										 bonusPlanNameArr = bonusPlanName;
									 }
									 else
									 {
										 bonusPlanNameArr = bonusPlanNameArr + "~" + bonusPlanName;
									 }									 
									 
									 ReportElement bonusCompElementRef = bonusPlanElement.getChild("wd:Compensation_Element_Reference");
									 if(bonusCompElementRef != null)
									 {
										 List<ReportElement> bonusElementData = bonusCompElementRef.getChildren("wd:ID");					 
										 for(ReportElement bonusElement:bonusElementData)
										 {
											 bonusElementMap = bonusElement.getAllAttributes();
											 if(bonusElementMap.get("wd:type").equals("Compensation_Element_ID"))
											 {
												 bonusElementName= bonusElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 bonusElementName = "";
									 }
									 
									 if(bonusElementNameArr.equals(""))
									 {
										 bonusElementNameArr = bonusElementName;
									 }
									 else
									 {
										 bonusElementNameArr = bonusElementNameArr + "~" + bonusElementName;
									 }
									 bonusAmount = bonusPlanElement.getChild("wd:Individual_Target_Amount") != null?bonusPlanElement.getChild("wd:Individual_Target_Amount").getValue().trim():"";
									 if(bonusAmountArr.equals(""))
									 {
										 bonusAmountArr = bonusAmount;
									 }
									 else
									 {
										 bonusAmountArr = bonusAmountArr + "~" + bonusAmount;
									 }
									 bonusPercentage = bonusPlanElement.getChild("wd:Individual_Target_Percent") != null?bonusPlanElement.getChild("wd:Individual_Target_Percent").getValue().trim():"";
									 if(bonusPercentageArr.equals(""))
									 {
										 bonusPercentageArr = bonusPercentage;
									 }
									 else
									 {
										 bonusPercentageArr = bonusPercentageArr + "~" + bonusPercentage;
									 }
									 bonusGuaranteedMinimum = bonusPlanElement.getChild("wd:Guaranteed_Minimum") != null?bonusPlanElement.getChild("wd:Guaranteed_Minimum").getValue().trim():"";
									 if(bonusGuaranteedMinimum.equals("1"))
									 {
										 bonusGuaranteedMinimum = "true";
									 }
									 else
									 {
										 bonusGuaranteedMinimum = "false";
									 }
									 if(bonusGuaranteedMinimumArr.equals(""))
									 {
										 bonusGuaranteedMinimumArr = bonusGuaranteedMinimum;
									 }
									 else
									 {
										 bonusGuaranteedMinimumArr = bonusGuaranteedMinimumArr + "~" + bonusGuaranteedMinimum;
									 }
									 bonusPercentageAssigned = bonusPlanElement.getChild("wd:Percent_Assigned") != null?bonusPlanElement.getChild("wd:Percent_Assigned").getValue().trim():"";
									 if(bonusPercentageAssignedArr.equals(""))
									 {
										 bonusPercentageAssignedArr = bonusPercentageAssigned;
									 }
									 else
									 {
										 bonusPercentageAssignedArr = bonusPercentageAssignedArr + "~" + bonusPercentageAssigned;
									 }
									 ReportElement bonusCurrRef = bonusPlanElement.getChild("wd:Currency_Reference");
									 if(bonusCurrRef != null)
									 {
										 List<ReportElement> bonusCurrencyData = bonusCurrRef.getChildren("wd:ID");					 
										 for(ReportElement bonusCurrencyElement:bonusCurrencyData)
										 {
											 bonusCurrencyMap = bonusCurrencyElement.getAllAttributes();
											 if(bonusCurrencyMap.get("wd:type").equals("Currency_ID"))
											 {
												 bonusCurrencyCode = bonusCurrencyElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 bonusCurrencyCode = "";
									 }
									 
									 if(bonusCurrencyCodeArr.equals(""))
									 {
										 bonusCurrencyCodeArr = bonusCurrencyCode;
									 }
									 else
									 {
										 bonusCurrencyCodeArr = bonusCurrencyCodeArr + "~" + bonusCurrencyCode;
									 }
									 
									 ReportElement bonusFreqRef = bonusPlanElement.getChild("wd:Frequency_Reference");
									 if(bonusFreqRef != null)
									 {
										 List<ReportElement> bonusFrequencyData = bonusFreqRef.getChildren("wd:ID");					 
										 for(ReportElement bonusFrequencyElement:bonusFrequencyData)
										 {
											 bonusFrequencyMap = bonusFrequencyElement.getAllAttributes();
											 if(bonusFrequencyMap.get("wd:type").equals("Frequency_ID"))
											 {
												 bonusFrequencyName = bonusFrequencyElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 bonusFrequencyName = "";
									 }
									 
									 if(bonusFrequencyNameArr.equals(""))
									 {
										 bonusFrequencyNameArr = bonusFrequencyName;
									 }
									 else
									 {
										 bonusFrequencyNameArr = bonusFrequencyNameArr + "~" + bonusFrequencyName;
									 }
									 
									 bonusActualEndDate = bonusPlanElement.getChild("wd:Actual_End_Date") != null?bonusPlanElement.getChild("wd:Actual_End_Date").getValue().trim():"";
									 if(bonusActualEndDateArr.equals(""))
									 {
										 bonusActualEndDateArr = bonusActualEndDate;
									 }
									 else
									 {
										 if(!bonusActualEndDate.equals(""))
										 {
											 bonusActualEndDateArr = bonusActualEndDateArr + "~" + bonusActualEndDate;
										 }
									 }
								 }
							 }
							 else
							 {
								 bonusPlanNameArr = "";
								 bonusElementNameArr = "";
								 bonusAmountArr = "";
								 bonusPercentageArr = "";
								 bonusGuaranteedMinimumArr = "";
								 bonusCurrencyCodeArr = "";
								 bonusFrequencyNameArr = "";
								 bonusPercentageAssignedArr = "";
								 bonusActualEndDateArr = "";
							 }
							 
							 List<ReportElement> allowancePlanList = element2.getChildren("wd:Allowance_Plan_Data");
							 if(allowancePlanList != null && allowancePlanList.size() >0)
							 {
								 allowancePlanNameArr = "";
								 allowanceElementNameArr = "";
								 allowanceAmountArr = "";
								 allowancePercentageArr = "";
								 allowanceCurrencyArr = "";
								 allowanceFrequencyArr = "";
								 allowanceActualEndDateArr = "";
								 
								 for(ReportElement allowancePlanElement: allowancePlanList)
								 {
									 ReportElement allowancePlanRef = allowancePlanElement.getChild("wd:Compensation_Plan_Reference");
									 if(allowancePlanRef != null)
									 {
										 List<ReportElement> allowanceData = allowancePlanRef.getChildren("wd:ID");					 
										 for(ReportElement allowanceElement:allowanceData)
										 {
											 allowancePlanMap = allowanceElement.getAllAttributes();
											 if(allowancePlanMap.get("wd:type").equals("Compensation_Plan_ID"))
											 {
												 allowancePlanName= allowanceElement.getValue().trim();											
											 }
										 }
									 }
									 else
									 {
										 allowancePlanName = "";
									 }
									 
									 if(allowancePlanNameArr.equals(""))
									 {
										 allowancePlanNameArr = allowancePlanName;
									 }
									 else
									 {
										 allowancePlanNameArr = allowancePlanNameArr + "~" + allowancePlanName;
									 }
									 
									 ReportElement allowanceCompElementRef = allowancePlanElement.getChild("wd:Compensation_Element_Reference");
									 if(allowanceCompElementRef != null)
									 {
										 List<ReportElement> allowanceElementData = allowanceCompElementRef.getChildren("wd:ID");					 
										 for(ReportElement allowanceElement:allowanceElementData)
										 {
											 allowanceElementMap = allowanceElement.getAllAttributes();
											 if(allowanceElementMap.get("wd:type").equals("Compensation_Element_ID"))
											 {
												 allowanceElementName= allowanceElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 allowanceElementName = "";
									 }
									 
									 if(allowanceElementNameArr.equals(""))
									 {
										 allowanceElementNameArr = allowanceElementName;
									 }
									 else
									 {
										 allowanceElementNameArr = allowanceElementNameArr + "~" + allowanceElementName;
									 }
									 
									 allowanceAmount = allowancePlanElement.getChild("wd:Amount") != null?allowancePlanElement.getChild("wd:Amount").getValue().trim():"";
									 if(allowanceAmountArr.equals(""))
									 {
										 allowanceAmountArr = allowanceAmount;
									 }
									 else
									 {
										 allowanceAmountArr = allowanceAmountArr + "~" + allowanceAmount;
									 }
									 allowancePercentage = allowancePlanElement.getChild("wd:Percent") != null?allowancePlanElement.getChild("wd:Percent").getValue().trim():"";
									 if(allowancePercentageArr.equals(""))
									 {
										 allowancePercentageArr = allowancePercentage;
									 }
									 else
									 {
										 allowancePercentageArr = allowancePercentageArr + "~" + allowancePercentage;
									 }
									 
									 ReportElement allowanceCurrRef = allowancePlanElement.getChild("wd:Currency_Reference");
									 if(allowanceCurrRef != null)
									 {
										 List<ReportElement> allowanceCurrencyData = allowanceCurrRef.getChildren("wd:ID");					 
										 for(ReportElement allowanceCurrencyElement:allowanceCurrencyData)
										 {
											 allowanceCurrencyMap = allowanceCurrencyElement.getAllAttributes();
											 if(allowanceCurrencyMap.get("wd:type").equals("Currency_ID"))
											 {
												 allowanceCurrency = allowanceCurrencyElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 allowanceCurrency = "";
									 }
									 
									 if(allowanceCurrencyArr.equals(""))
									 {
										 allowanceCurrencyArr = allowanceCurrency;
									 }
									 else
									 {
										 allowanceCurrencyArr = allowanceCurrencyArr + "~" + allowanceCurrency;
									 }
									 
									 ReportElement allowanceFreqRef = allowancePlanElement.getChild("wd:Frequency_Reference");
									 if(allowanceFreqRef != null)
									 {
										 List<ReportElement> allowanceFrequencyData = allowanceFreqRef.getChildren("wd:ID");					 
										 for(ReportElement allowanceFrequencyElement:allowanceFrequencyData)
										 {
											 allowanceFrequencyMap = allowanceFrequencyElement.getAllAttributes();
											 if(allowanceFrequencyMap.get("wd:type").equals("Frequency_ID"))
											 {
												 allowanceFrequency = allowanceFrequencyElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 allowanceFrequency = "";
									 }
									 
									 if(allowanceFrequencyArr.equals(""))
									 {
										 allowanceFrequencyArr = allowanceFrequency;
									 }
									 else
									 {
										 allowanceFrequencyArr = allowanceFrequencyArr + "~" + allowanceFrequency;
									 }
									 
									 allowanceActualEndDate = allowancePlanElement.getChild("wd:Actual_End_Date") != null?allowancePlanElement.getChild("wd:Actual_End_Date").getValue().trim():"";	
									 if(allowanceActualEndDateArr.equals(""))
									 {
										 allowanceActualEndDateArr = allowanceActualEndDate;
									 }
									 else
									 {
										 if(!allowanceActualEndDate.equals(""))
										 {
											 allowanceActualEndDateArr = allowanceActualEndDateArr + "~" + allowanceActualEndDate;
										 }
									 }
								 }
							 }
							 else
							 {
								 allowancePlanNameArr = "";
								 allowanceElementNameArr = "";
								 allowanceAmountArr = "";
								 allowancePercentageArr = "";
								 allowanceCurrencyArr = "";
								 allowanceFrequencyArr = "";
								 allowanceActualEndDateArr = "";
							 }
							 
							 List<ReportElement> unitAllowancePlanList = element2.getChildren("wd:Unit_Allowance_Plan_Data");
							 if(unitAllowancePlanList != null && unitAllowancePlanList.size()>0)
							 {								 
								 for(ReportElement unitAllowancePlanElement: unitAllowancePlanList)
								 {
									 ReportElement unitAllowancePlanRef = unitAllowancePlanElement.getChild("wd:Compensation_Plan_Reference");
									 if(unitAllowancePlanRef != null)
									 {
										 List<ReportElement> unitAllowPlanData = unitAllowancePlanRef.getChildren("wd:ID");					 
										 for(ReportElement unitAllowPlanElement:unitAllowPlanData)
										 {
											 unitAllowanceMap = unitAllowPlanElement.getAllAttributes();
											 if(unitAllowanceMap.get("wd:type").equals("Compensation_Plan_ID"))
											 {
												 unitAllowancePlanName= unitAllowPlanElement.getValue().trim();											
											 }
										 }
									 }
									 else
									 {
										 unitAllowancePlanName = "";
									 }
									 
									 ReportElement unitAllowanceElementRef = unitAllowancePlanElement.getChild("wd:Compensation_Element_Reference");
									 if(unitAllowanceElementRef != null)
									 {
										 List<ReportElement> unitAllowanceElementData = unitAllowanceElementRef.getChildren("wd:ID");					 
										 for(ReportElement unitAllowanceElementElement:unitAllowanceElementData)
										 {
											 unitAllowanceElementMap = unitAllowanceElementElement.getAllAttributes();
											 if(unitAllowanceElementMap.get("wd:type").equals("Compensation_Element_ID"))
											 {
												 unitAllowanceElementName= unitAllowanceElementElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 unitAllowanceElementName = "";
									 }
									 
									 ReportElement unitAllowanceUnitRef = unitAllowancePlanElement.getChild("wd:Unit_Reference");
									 if(unitAllowanceUnitRef != null)
									 {
										 List<ReportElement> unitMeasureElementData = unitAllowanceUnitRef.getChildren("wd:ID");					 
										 for(ReportElement unitSalaryMeasureElement:unitMeasureElementData)
										 {
											 unitAllowanceMeasureMap = unitSalaryMeasureElement.getAllAttributes();
											 if(unitAllowanceMeasureMap.get("wd:type").equals("UN_CEFACT_Common_Code_ID"))
											 {
												 unitAllowanceUnitOfMeasureName = unitSalaryMeasureElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 unitAllowanceUnitOfMeasureName = "";
									 }
									 
									 unitAllowanceNoOfUnits = unitAllowancePlanElement.getChild("wd:Number_of_Units") != null?unitAllowancePlanElement.getChild("wd:Number_of_Units").getValue().trim():"";
									 unitAllowancePerUnitAmount = unitAllowancePlanElement.getChild("wd:Per_Unit_Amount") != null?unitAllowancePlanElement.getChild("wd:Per_Unit_Amount").getValue().trim():"";
									 
									 ReportElement unitAllowanceCurrRef = unitAllowancePlanElement.getChild("wd:Currency_Reference");
									 if(unitAllowanceCurrRef != null)
									 {
										 List<ReportElement> unitCurrencyElementData = unitAllowanceCurrRef.getChildren("wd:ID");					 
										 for(ReportElement unitCurrencyElement:unitCurrencyElementData)
										 {
											 unitAllowanceCurrencyMap = unitCurrencyElement.getAllAttributes();
											 if(unitAllowanceCurrencyMap.get("wd:type").equals("Currency_ID"))
											 {
												 unitAllowanceCurrency = unitCurrencyElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 unitAllowanceCurrency = "";
									 }
									 
									 ReportElement unitAllowanceFreqRef = unitAllowancePlanElement.getChild("wd:Frequency_Reference");
									 if(unitAllowanceFreqRef != null)
									 {
										 List<ReportElement> unitFrequencyElementData = unitAllowanceFreqRef.getChildren("wd:ID");					 
										 for(ReportElement unitFrequencyElement:unitFrequencyElementData)
										 {
											 unitAllowanceFrequencyMap = unitFrequencyElement.getAllAttributes();
											 if(unitAllowanceFrequencyMap.get("wd:type").equals("Frequency_ID"))
											 {
												 unitAllowanceFrequency = unitFrequencyElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 unitAllowanceFrequency = "";
									 }
									 unitAllowanceActualEndDate = unitAllowancePlanElement.getChild("wd:Actual_End_Date") != null?unitAllowancePlanElement.getChild("wd:Actual_End_Date").getValue().trim():"";	
								 }
							 }
							 else
							 {
								 unitAllowancePlanName = "";
								 unitAllowanceElementName = "";
								 unitAllowanceUnitOfMeasureName = "";
								 unitAllowancePerUnitAmount = "";
								 unitAllowanceCurrency = "";
								 unitAllowanceNoOfUnits = "";
								 unitAllowanceFrequency = "";
								 unitAllowanceActualEndDate = "";
							 }
							 
							 List<ReportElement> periodSalaryPlanList = element2.getChildren("wd:Period_Salary_Plan_Data");
							 if(periodSalaryPlanList != null)
							 {
								 for(ReportElement periodSalaryPlanElement: periodSalaryPlanList)
								 {
									 ReportElement periodSalaryPlanRef = periodSalaryPlanElement.getChild("wd:Compensation_Plan_Reference");
									 if(periodSalaryPlanRef != null)
									 {
										 List<ReportElement> periodSalaryPlanData = periodSalaryPlanRef.getChildren("wd:ID");					 
										 for(ReportElement periodSalPlanElement:periodSalaryPlanData)
										 {
											 periodSalaryPlanMap = periodSalPlanElement.getAllAttributes();
											 if(periodSalaryPlanMap.get("wd:type").equals("Compensation_Plan_ID"))
											 {
												 periodSalaryPlanName= periodSalPlanElement.getValue().trim();											
											 }
										 }
									 }
									 else
									 {
										 periodSalaryPlanName = "";
									 }
									 
									 ReportElement periodSalaryElementRef = periodSalaryPlanElement.getChild("wd:Compensation_Element_Reference");
									 if(periodSalaryElementRef != null)
									 {
										 List<ReportElement> periodSalaryElementData = periodSalaryElementRef.getChildren("wd:ID");					 
										 for(ReportElement periodSalaryElement:periodSalaryElementData)
										 {
											 periodSalaryElementMap = periodSalaryElement.getAllAttributes();
											 if(periodSalaryElementMap.get("wd:type").equals("Compensation_Element_ID"))
											 {
												 periodSalaryElementName= periodSalaryElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 periodSalaryElementName = "";
									 }
									 
									 ReportElement compPeriodRefRef = periodSalaryPlanElement.getChild("wd:Compensation_Period_Reference");
									 if(compPeriodRefRef != null)
									 {
										 List<ReportElement> compPeriodElementData = compPeriodRefRef.getChildren("wd:ID");					 
										 for(ReportElement compPeriodElement:compPeriodElementData)
										 {
											 compensationPeriodMap = compPeriodElement.getAllAttributes();
											 if(compensationPeriodMap.get("wd:type").equals("Compensation_Period_ID"))
											 {
												 compensationPeriod = compPeriodElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 compensationPeriod = "";
									 }
									 
									 compensationPeriodMultiplier = periodSalaryPlanElement.getChild("wd:Compensation_Period_Multiplier") != null?periodSalaryPlanElement.getChild("wd:Compensation_Period_Multiplier").getValue().trim():"";
									 
									 ReportElement periodSalaryCurrRef = periodSalaryPlanElement.getChild("wd:Currency_Reference");
									 if(periodSalaryCurrRef != null)
									 {
										 List<ReportElement> periodSalaryCurrData = periodSalaryCurrRef.getChildren("wd:ID");					 
										 for(ReportElement periodSalaryCurrencyElement:periodSalaryCurrData)
										 {
											 periodSalaryCurrencyMap = periodSalaryCurrencyElement.getAllAttributes();
											 if(periodSalaryCurrencyMap.get("wd:type").equals("Currency_ID"))
											 {
												 periodSalaryCurrency = periodSalaryCurrencyElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 periodSalaryCurrency = "";
									 }
									 
									 if(periodSalaryCurrency.equalsIgnoreCase("AUT"))
									 {
										 if(periodSalaryPlanName.equalsIgnoreCase("15.5 Salary Months"))
										 {
											 periodSalaryPlanName = "14 Salary Months";
										 }
									 }
									 
									 ReportElement periodSalaryFreqRef = periodSalaryPlanElement.getChild("wd:Frequency_Reference");
									 if(periodSalaryFreqRef != null)
									 {
										 List<ReportElement> periodSalaryFrequencyData = periodSalaryFreqRef.getChildren("wd:ID");					 
										 for(ReportElement periodSalaryFrequencyElement:periodSalaryFrequencyData)
										 {
											 periodSalaryFrequencyMap = periodSalaryFrequencyElement.getAllAttributes();
											 if(periodSalaryFrequencyMap.get("wd:type").equals("Frequency_ID"))
											 {
												 periodSalaryFrequency = periodSalaryFrequencyElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 periodSalaryFrequency = "";
									 }									 
									 periodActualendDate = periodSalaryPlanElement.getChild("wd:Actual_End_Date") != null?periodSalaryPlanElement.getChild("wd:Actual_End_Date").getValue().trim():"";									 
								 }
							 }
							 else
							 {
								 periodSalaryPlanName = "";
								 periodSalaryElementName = "";
								 compensationPeriod = "";
								 compensationPeriodMultiplier = "";
								 periodSalaryCurrency = "";
								 periodSalaryFrequency = "";
								 periodActualendDate = "";
							 }
							 
							 List<ReportElement> calculatedPlanList = element2.getChildren("wd:Calculated_Plan_Data");
								if(calculatedPlanList != null)
								{
									for(ReportElement calculatedPlanElement: calculatedPlanList)
									{
										ReportElement calculatedPlanRef = calculatedPlanElement.getChild("wd:Calculated_Plan_Reference");
										 if(calculatedPlanRef != null)
										 {
											 List<ReportElement> calculatedPlanData = calculatedPlanRef.getChildren("wd:ID");					 
											 for(ReportElement calcPlanElement:calculatedPlanData)
											 {
												 calculatedPlanMap = calcPlanElement.getAllAttributes();
												 if(calculatedPlanMap.get("wd:type").equals("Compensation_Plan_ID"))
												 {
													 calculatedPlanName= calcPlanElement.getValue().trim();											
												 }
											 }
										 }
										 else
										 {
											 calculatedPlanName = "";
										 }
										 
										 amountOverride = calculatedPlanElement.getChild("wd:Amount_Override") != null?calculatedPlanElement.getChild("wd:Amount_Override").getValue().trim():"";
										 calculatedActualEndDate = calculatedPlanElement.getChild("wd:Actual_End_Date") != null?calculatedPlanElement.getChild("wd:Actual_End_Date").getValue().trim():"";	
										 
										 ReportElement calculatedCurrRef = calculatedPlanElement.getChild("wd:Currency_Reference");
										 if(calculatedCurrRef != null)
										 {
											 List<ReportElement> calculatedCurrData = calculatedCurrRef.getChildren("wd:ID");					 
											 for(ReportElement calculatedCurrencyElement:calculatedCurrData)
											 {
												 calculatedCurrencyMap = calculatedCurrencyElement.getAllAttributes();
												 if(calculatedCurrencyMap.get("wd:type").equals("Currency_ID"))
												 {
													 calculatedCurrency = calculatedCurrencyElement.getValue().trim();
												 }
											 }
										 }
										 else
										 {
											 calculatedCurrency = "";
										 }
										 
										 if(calculatedCurrency.equalsIgnoreCase("AUT"))
										 {
											 if(calculatedPlanName.equalsIgnoreCase("Austria Tariff Salary (KV Handel)"))
											 {
												 calculatedPlanName = "Austria Tariff Salary (KV IT)";
											 }
											 else if(calculatedPlanName.equalsIgnoreCase("Austria Tariff Salary (KV Handel)"))
											 {
												 calculatedPlanName = "Austria Tariff Salary (KV IT)";
											 }
										 }
										 
										 ReportElement calculatedFreqRef = calculatedPlanElement.getChild("wd:Frequency_Reference");
										 if(calculatedFreqRef != null)
										 {
											 List<ReportElement> calculatedFrequencyData = calculatedFreqRef.getChildren("wd:ID");					 
											 for(ReportElement calculatedFrequencyElement:calculatedFrequencyData)
											 {
												 calculatedFrequencyMap = calculatedFrequencyElement.getAllAttributes();
												 if(calculatedFrequencyMap.get("wd:type").equals("Frequency_ID"))
												 {
													 calculatedFrequency = calculatedFrequencyElement.getValue().trim();
												 }
											 }
										 }
										 else
										 {
											 calculatedFrequency = "";
										 }
									}
								}
								else
								{
									calculatedPlanName = "";
									amountOverride = "";
									calculatedCurrency = "";
									calculatedFrequency = "";
									calculatedActualEndDate = "";
								}
						 }						 
						 
						 headingFromWD = "Employee_ID,Position_ID,Effective_Date,Compensation_Reason,Compensation_Package_Name,Compensation_Grade_Name,Compensation_Profile_Name,Compensation_Step_Name,Salary_Plan_Name,"
						 		         + "Salary_Element_Name,Salary_Element_Amount,Salary_Currency,Salary_Frequency,Hourly_Plan_Name,Hourly_Element_Name,Hourly_Element_Amount,"
						 		         + "Hourly_Currency,Hourly_Frequency,Bonus_Plan_Name,Bonus_Element_Name,Bonus_Amount,Bonus_Percentage,Bonus_Guaranteed_Minimum,Bonus_Currency,"
						 		         + "Bonus_Frequency,Bonus_Percentage_Assigned,Bonus_Actual_End_Date,Allowance_Plan_Name,Allowance_Element_Name,Allowance_Amount,Allowance_Percentage,Allowance_Currency,"
						 		         + "Allowance_Frequency,Allowance_Actual_End_Date,Unit_Allowance_Plan_Name,Unit_Allowance_Element_Name,Unit_Allowance_Unit_Of_Measure_Name,Unit_Allowance_Per_Unit_Amount,"
						 		         + "Unit_Allowance_Currency,Unit_Allowance_No_Of_Units,Unit_Allowance_Frequency,Unit_Allowance_Actual_End_Date,Period_Salary_Plan_Name,Period_Salary_Element_Name,"
						 		         + "Compensation_Period,Compensation_Period_Multiplier,Period_Salary_Currency,Period_Salary_Frequency,Period_Actual_End_Date,Calculated_Plan_Name,"
						 		         + "Calculated_Amount_Override,Calculated_Currency,Calculated_Frequency,Calculated_Actual_End_Date";
							 
						 headerStr = empId + "," + positionId + "," + effectiveDate + "," + compReasonName + "," + compPackageName + "," + compGradeName + "," + compProfileName + "," + compStepName + "," + salaryPlanName + "," + 
								 	 salaryElementName + "," + salaryElementAmount + "," + salaryCurrency + "," + salaryFrequency + "," + hourlyPlanName + "," + hourlyElementName + "," + 
								     hourlyElementAmount + "," + hourlyCurrency + "," + hourlyFrequency + "," + bonusPlanNameArr + "," + bonusElementNameArr + "," + bonusAmountArr + "," +
								 	 bonusPercentageArr + "," + bonusGuaranteedMinimumArr + "," + bonusCurrencyCodeArr + "," + bonusFrequencyNameArr + "," + bonusPercentageAssignedArr + "," + bonusActualEndDateArr + "," +
								 	 allowancePlanNameArr + "," + allowanceElementNameArr + "," + allowanceAmountArr + "," + allowancePercentageArr + "," + allowanceCurrencyArr + "," + allowanceFrequencyArr + "," + allowanceActualEndDateArr + "," +
								 	 unitAllowancePlanName + "," + unitAllowanceElementName + "," + unitAllowanceUnitOfMeasureName + "," + unitAllowancePerUnitAmount + "," + unitAllowanceCurrency + "," +
								 	 unitAllowanceNoOfUnits + "," + unitAllowanceFrequency + "," + unitAllowanceActualEndDate + "," + periodSalaryPlanName + "," + periodSalaryElementName + "," + compensationPeriod + "," +
								 	 compensationPeriodMultiplier + "," + periodSalaryCurrency + "," + periodSalaryFrequency + "," + periodActualendDate + "," + calculatedPlanName + "," +
								 	 amountOverride + "," + calculatedCurrency + "," + calculatedFrequency + "," + calculatedActualEndDate;
						 
						 if(finalStr.equals(""))
						 {
							 finalStr = headingFromWD + "\n" + headerStr;
						 }
						 else
						 {
							 finalStr = finalStr + "\n" + headerStr;
						 }							 
					 }
				 }
				 
				 System.out.println(finalStr);
				 
				 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
				 PrintWriter writer = new PrintWriter(wdCSVfile);
				 writer.write(finalStr.toString());
				 writer.flush();
				 writer.close();
				 
				 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
				 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
				 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
				 postLoadService.updatePostLoad(postLoad);
				 
				 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
				 
				 /*String wdCSVfile1 = "config/" + loadCycle + "_" + ruleName + ".csv";			 
				 PrintWriter writer1 = new PrintWriter(new File(wdCSVfile1));
				 writer1.write(finalStr.toString());
				 writer1.flush();
				 writer1.close();*/
			 }
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}

	private boolean getSalaryOrHourly(String salHourValue) {
		
		boolean isHourly = false;
		if(salHourValue.contains("_"))
		{
			String [] salHourArr = salHourValue.split("_");
			if(Arrays.asList(salHourArr).contains("Hourly"))
			{
				isHourly = true;
			}
		}
		else
		{
			if(salHourValue.contains("Hourly"))
			{
				isHourly = true;
			}
		}
		return isHourly;
	}

	private JSONArray createCSVFromWDLeaveOfAbsence(Tenant tenant, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		try 
		{			 
			 System.out.println("columnList--"+columnList);
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_LEAVE_OF_ABSENCE_FILE = requestfile.getAbsolutePath();
				 
				 //String outputfile = GET_LEAVE_OF_ABSENCE_FILE;
				 String outputfile = addHireIdList(GET_LEAVE_OF_ABSENCE_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");
				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");			 
				 
				 ReportElement soapResp = XmlParserManager.parseXml(strMsg);
				 
				 ReportElement pageData = soapResp.getChild("env:Body")
							.getChild("wd:Get_Workers_Response")
							.getChild("wd:Response_Results");
				 
				 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
				 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
				 int totalResult = Integer.parseInt(totalResults);
				 System.out.println("totalNoOfPages-"+totalNoOfPages);
				 System.out.println("totalResult-"+totalResult);
				 wdCount = totalResult;
				 
				 String empId = "";
				 String leaveTypeName = "";
				 String lastDayOfWork = "";
				 String firstDayOfLeave = "";
				 String estLastDayOfLeave = "";
				 String lastDateForWhichPaid = "";
				 String expectedDueDate = "";
				 String childsBirthDate = "";
				 String babyArrivalHomeDate = "";
				 String adoptionPlacementDate = "";
				 String multipleChildIndicator = "";
				 String noOfBabiesAdopted = "";
				 String noOfPreviousBirth = "";
				 String noOfPreviousMaternityLeaves = "";
				 String noOfChildDependents = "";
				 String singleParentIndicator = "";
				 String ageOfDependent = "";
				 String firstDayOfWork = "";
				 String lastDayOfLeave = "";
				 String finalStr = "";
				 String newFinalStr = "";
				 String headerStr = "";
				 int leaveCntWD = 0;
				 int leaveCntSrc = 0;
				 
				 Map<String,String> idMap = null;
				 Map<String,String> leaveMap = null;
				 
				 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
				 {
					if(j == 1)
					{
						startIndex = 0;
						if(1000 > totalResult)
						{
							endIndex = totalResult;
						}
						else
						{
							endIndex = j*1000;
						}
					}
					else
					{
						//startIndex = (j - 1)*1000;
						int lastVal = (j - 1);
						startIndex = (endIndex - lastVal);
						if(j*1000 > totalResult)
						{
							endIndex = totalResult;
						}
						else
						{
							//endIndex = j*1000;
							endIndex = (j*startIndex) + 1;
						}
					}
					outputfile = addHireIdList(GET_LEAVE_OF_ABSENCE_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
					is = new FileInputStream(outputfile);
				    soapMessage = MessageFactory.newInstance().createMessage(null, is);
				    soapPart = soapMessage.getSOAPPart();
				    envelope = soapPart.getEnvelope();
					envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
					if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
					{
							envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
					}
					soapMessage.saveChanges();
			        out = new ByteArrayOutputStream();
			        soapMessage.writeTo(out);
			        strMsg = new String(out.toByteArray());
			        
			        soapConnectionFactory = SOAPConnectionFactory.newInstance();
					soapConnection = soapConnectionFactory.createConnection();
			        soapResponse = soapConnection.call(soapMessage, sourceUrl);
			        out = new ByteArrayOutputStream();
			        soapResponse.writeTo(out);
			        strMsg = new String(out.toByteArray(), "utf-8");
			        
			        soapResp = XmlParserManager.parseXml(strMsg);
				        
			        ReportElement responseData = soapResp.getChild("env:Body")
							.getChild("wd:Get_Workers_Response")
							.getChild("wd:Response_Data");
				 
				    List<ReportElement> workerData = responseData.getChildren("wd:Worker");
						
					 for(ReportElement reportElement : workerData)
					 {
						 ReportElement element1 = reportElement.getChild("wd:Worker_Reference");
						 if(element1 != null)
						 {
							 List<ReportElement> idData = element1.getChildren("wd:ID");					 
							 for(ReportElement wdElement:idData)
							 {
								 idMap = wdElement.getAllAttributes();
								 if(idMap.get("wd:type").equals("Employee_ID"))
								 {
									 empId = wdElement.getValue().trim();
								 }
							 }
						 }
						 
						 List<ReportElement> leaveData = reportElement.getChild("wd:Worker_Data")
						 			.getChild("wd:Employment_Data")
						 			.getChild("wd:Worker_Status_Data")
						 			.getChildren("wd:Leave_Status_Data");
						 
						 if(leaveData != null)
						 {
							 for(ReportElement leaveElement : leaveData)
							 {
								 ReportElement element2 = leaveElement.getChild("wd:Leave_of_Absence_Type_Reference");
								 if(element2 != null)
								 {
									 List<ReportElement> leaveTypeData = element2.getChildren("wd:ID");								 
									 for(ReportElement leaveTypeElement:leaveTypeData)
									 {
										 leaveMap = leaveTypeElement.getAllAttributes();
										 if(leaveMap.get("wd:type").equals("Leave_of_Absence_Type_ID"))
										 {
											 leaveTypeName = leaveTypeElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 leaveTypeName = "";
								 }
								 
								 lastDayOfWork = leaveElement.getChild("wd:Leave_Last_Day_of_Work") != null?leaveElement.getChild("wd:Leave_Last_Day_of_Work").getValue().trim():"";
								 firstDayOfLeave = leaveElement.getChild("wd:Leave_Start_Date") != null?leaveElement.getChild("wd:Leave_Start_Date").getValue().trim():"";
								 estLastDayOfLeave = leaveElement.getChild("wd:Estimated_Leave_End_Date") != null?leaveElement.getChild("wd:Estimated_Leave_End_Date").getValue().trim():"";
								 firstDayOfWork = leaveElement.getChild("wd:First_Day_Of_Work") != null?leaveElement.getChild("wd:First_Day_Of_Work").getValue().trim():"";
								 lastDayOfLeave = leaveElement.getChild("wd:Leave_End_Date") != null?leaveElement.getChild("wd:Leave_End_Date").getValue().trim():"";
								 
								 ReportElement element3 = leaveElement.getChild("wd:Leave_Request_Additional_Fields");
								 if(element3 != null)
								 {
									 lastDateForWhichPaid = element3.getChild("wd:Last_Date_for_Which_Paid") != null?element3.getChild("wd:Last_Date_for_Which_Paid").getValue().trim():"";
									 expectedDueDate = element3.getChild("wd:Expected_Due_Date") != null?element3.getChild("wd:Expected_Due_Date").getValue().trim():"";
									 childsBirthDate = element3.getChild("wd:Child_s_Birth_Date") != null?element3.getChild("wd:Child_s_Birth_Date").getValue().trim():"";
									 babyArrivalHomeDate = element3.getChild("wd:Date_Baby_Arrived_Home_From_Hospital") != null?element3.getChild("wd:Date_Baby_Arrived_Home_From_Hospital").getValue().trim():"";
									 adoptionPlacementDate = element3.getChild("wd:Adoption_Placement_Date") != null?element3.getChild("wd:CAdoption_Placement_Date").getValue().trim():"";
									 multipleChildIndicator = element3.getChild("wd:Multiple_Child_Indicator") != null?element3.getChild("wd:Multiple_Child_Indicator").getValue().trim():"";
									 noOfBabiesAdopted = element3.getChild("wd:Number_of_Babies_Adopted_Children") != null?element3.getChild("wd:Number_of_Babies_Adopted_Children").getValue().trim():"";
									 noOfPreviousBirth = element3.getChild("wd:Number_of_Previous_Births") != null?element3.getChild("wd:Number_of_Previous_Births").getValue().trim():"";
									 noOfPreviousMaternityLeaves = element3.getChild("wd:Number_of_Previous_Maternity_Leaves") != null?element3.getChild("wd:Number_of_Previous_Maternity_Leaves").getValue().trim():"";
									 noOfChildDependents = element3.getChild("wd:Number_of_Child_Dependents") != null?element3.getChild("wd:Number_of_Child_Dependents").getValue().trim():"";
									 singleParentIndicator = element3.getChild("wd:Single_Parent_Indicator") != null?element3.getChild("wd:Single_Parent_Indicator").getValue().trim():"";
									 ageOfDependent = element3.getChild("wd:Age_of_Dependent") != null?element3.getChild("wd:Age_of_Dependent").getValue().trim():"";
								 }
								 
								 
								 headingFromWD = "Employee_ID,Leave_Type_Name,Last_Day_of_Work,First_Day_Of_Leave,Estimated_Last_Day_of_Leave,Last_Date_for_Which_Paid,Expected_Due_Date,"
								 		+ "Child_s_Birth_Date,Date_Baby_Arrived_Home_From_Hospital,Adoption_Placement_Date,Multiple_Child_Indicator,Number_of_Babies_Adopted_Children,"
								 		+ "Number_of_Previous_Births,Number_of_Previous_Maternity_Leaves,Number_of_Child_Dependents,Single_Parent_Indicator,Age_of_Dependent,"
								 		+ "Leave_Return_Event_Data_First_Day_Of_Work,Leave_Return_Event_Data_Actual_Last_Day_of_Leave";
								 
								 headerStr = empId + "," + leaveTypeName + "," + lastDayOfWork + "," + firstDayOfLeave + "," + estLastDayOfLeave + "," + lastDateForWhichPaid + "," + expectedDueDate + "," + 
								             childsBirthDate + "," + babyArrivalHomeDate + "," + adoptionPlacementDate + "," + multipleChildIndicator + "," + noOfBabiesAdopted + "," +
								             noOfPreviousBirth + "," + noOfPreviousMaternityLeaves + "," + noOfChildDependents + "," + singleParentIndicator + "," + ageOfDependent + "," +
								             firstDayOfWork + "," + lastDayOfLeave;
								 
								 if(finalStr.equals(""))
								 {
									 finalStr = headingFromWD + "\n" + headerStr;
								 }
								 else
								 {
									 finalStr = finalStr + "\n" + headerStr;
								 }
							 }							 
						 }						 						 					 				
					 }
				 }				 
				 
				 String[] headingFromWdArr = headingFromWD.split(",");
				 for(int i = 0;i<headingFromWdArr.length; i++)
				 {
					String headingValWd = headingFromWdArr[i].replace("_", " ");
					if(headingValWd.equalsIgnoreCase("First Day Of Leave"))
					{
						leaveCntWD = i;
					}
				 }
				 
				 String[] headingFromSrcArr = headingFromSource.split(",");
				 for(int i = 0;i<headingFromSrcArr.length; i++)
				 {
					String headingValSrc = headingFromSrcArr[i].replace("_", " ");
					if(headingValSrc.equalsIgnoreCase("First Day Of Leave"))
					{
						leaveCntSrc = i;
					}
				 }
				 
				 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
				 byte[] srcCSVFileContent = postLoad.getSrcCSVFileContent();
				 File sourceCSVfile = null;
				 try 
				 {
					 sourceCSVfile = File.createTempFile(postLoad.getSrcCSVFileName().substring(0, postLoad.getSrcCSVFileName().indexOf(".")), ".csv");
					 FileUtils.writeByteArrayToFile(sourceCSVfile, srcCSVFileContent);
				 } 
				 catch (IOException e1) 
				 {
					 e1.printStackTrace();
				 }
				 String srcFile = sourceCSVfile.getAbsolutePath();
				 
				 String line = "";
				 int count = 0;
			     BufferedReader reader = new BufferedReader(new FileReader(srcFile));
			     while ((line = reader.readLine()) != null) 
			     { 
			         if(count != 0 && line.length() > 0) 
			         {
			        	 String [] lineArr = line.split(",");
			        	 if(lineArr.length >1)
			        	 {
				        	 String existStr = updateWDCSVFileForMultipleRow(lineArr[0], lineArr[leaveCntSrc], leaveCntWD, finalStr);
				        	 if(existStr.length() > 0)
				        	 {
				        		 if(newFinalStr.equals(""))
								 {
				        			 newFinalStr = headingFromWD + "\n" + existStr;
								 }
								 else
								 {
									 newFinalStr = newFinalStr + "\n" + existStr;
								 }
				        	 }
			        	 }
			         }
			         count++;
			     }
			     reader.close();
			     
			     System.out.println(newFinalStr);
				 
				 /*String wdCSVfile = "config/" + loadCycle + "_" + ruleName + ".csv";			 
				 PrintWriter writer = new PrintWriter(new File(wdCSVfile));
				 writer.write(finalStr.toString());
				 writer.flush();
				 writer.close();*/
				 
				 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
				 PrintWriter writer = new PrintWriter(wdCSVfile);
				 writer.write(newFinalStr.toString());
				 writer.flush();
				 writer.close();
				 
				 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
				 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
				 postLoadService.updatePostLoad(postLoad);
				 
				 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
			 }

		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return headingWd;
	}

	private JSONArray createCSVFromWDApplicantPhone(Tenant tenant, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		//String faultStr = null;
		String checkFile = null;
		//wdCount = 0;
		try 
		{			 
			 System.out.println("columnList--"+columnList);
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_APPLICANT_PHONE_FILE = requestfile.getAbsolutePath();
				 
				 //String outputfile = GET_APPLICANT_PHONE_FILE;
				 if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addIdListToFindError(GET_APPLICANT_PHONE_FILE, columnList.get(i), ruleName, "Applicant_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement reportElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = reportElement.getChild("env:Body")
										.getChild("wd:Get_Applicants_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
						 }
					 }
					 columnList.removeAll(errorList);
				 }
				 String outputfile = addApplicantIdList(GET_APPLICANT_PHONE_FILE, columnList, ruleName, startIndex, columnList.size());
				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");
				 /*if(strMsg.contains("faultstring"))
				 {
					 ReportElement msgResponse = XmlParserManager.parseXml(strMsg);
					 ReportElement elementData = msgResponse.getChild("SOAP-ENV:Body")
								.getChild("SOAP-ENV:Fault");
					 faultStr = elementData.getChild("faultstring").getValue().trim();
					 System.out.println("faultStr-"+faultStr);
				 }*/
				 
				 //if(faultStr == null)
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Applicants_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String countryISOCode = "";
					 String intPhoneCode = "";
					 String areaCode = "";
					 String phoneNumber = "";
					 String phoneDeviceType = "";
					 String countryISOCodeArr = "";
					 String intPhoneCodeArr = "";
					 String areaCodeArr = "";
					 String phoneNumberArr = "";
					 String phoneDeviceTypeArr = "";
					 String usageType = "";
					 String usageTypeArr = "";
					 String finalStr = "";
					 String headerStr = "";
					 
					 Map<String,String> phDeviceMap = null;
					 Map<String,String> usageMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {
						if(j == 1)
						{
							startIndex = 0;
							if(1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*1000;
							}
						}
						else
						{
							//startIndex = (j - 1)*1000;
							int lastVal = (j - 1);
							startIndex = (endIndex - lastVal);
							if(j*1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								//endIndex = j*1000;
								endIndex = (j*startIndex) + 1;
							}
						}
						outputfile = addApplicantIdList(GET_APPLICANT_PHONE_FILE, columnList, ruleName, startIndex, endIndex);
						is = new FileInputStream(outputfile);
					    soapMessage = MessageFactory.newInstance().createMessage(null, is);
					    soapPart = soapMessage.getSOAPPart();
					    envelope = soapPart.getEnvelope();
						envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						{
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						}
						soapMessage.saveChanges();
				        out = new ByteArrayOutputStream();
				        soapMessage.writeTo(out);
				        strMsg = new String(out.toByteArray());
				        
				        soapConnectionFactory = SOAPConnectionFactory.newInstance();
						soapConnection = soapConnectionFactory.createConnection();
				        soapResponse = soapConnection.call(soapMessage, sourceUrl);
				        out = new ByteArrayOutputStream();
				        soapResponse.writeTo(out);
				        strMsg = new String(out.toByteArray(), "utf-8");
				        
				        soapResp = XmlParserManager.parseXml(strMsg);
					        
						ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Applicants_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> applicantData = responseData.getChildren("wd:Applicant");
							
						 for(ReportElement reportElement : applicantData)
						 {
							 ReportElement element1 = reportElement.getChild("wd:Applicant_Data");
							 String applicantId = element1.getChild("wd:Applicant_ID").getValue().trim();
							 System.out.println(applicantId);
							 
							 List<ReportElement> phoneData = reportElement.getChild("wd:Applicant_Data")
							 			.getChild("wd:Personal_Data")
							 			.getChild("wd:Contact_Data")
							 			.getChildren("wd:Phone_Data");
							 
							 if(phoneData != null && phoneData.size()>0)
							 {
								 countryISOCodeArr = "";
								 intPhoneCodeArr = "";
								 areaCodeArr = "";
								 phoneNumberArr = "";
								 phoneDeviceTypeArr = "";
								 usageTypeArr = "";
								 
								 for(ReportElement phoneElement : phoneData)
								 {
									 countryISOCode = phoneElement.getChild("wd:Country_ISO_Code") != null?phoneElement.getChild("wd:Country_ISO_Code").getValue().trim():"";
									 if(countryISOCodeArr.equals(""))
									 {
										 countryISOCodeArr = countryISOCode;
									 }
									 else
									 {
										 countryISOCodeArr = countryISOCodeArr + "~" + countryISOCode;
									 }
									 intPhoneCode = phoneElement.getChild("wd:International_Phone_Code") != null?phoneElement.getChild("wd:International_Phone_Code").getValue().trim():"";
									 if(intPhoneCodeArr.equals(""))
									 {
										 intPhoneCodeArr = intPhoneCode;
									 }
									 else
									 {
										 intPhoneCodeArr = intPhoneCodeArr + "~" + intPhoneCode;
									 }
									 areaCode = phoneElement.getChild("wd:Area_Code") != null?phoneElement.getChild("wd:Area_Code").getValue().trim():""; 
									 if(areaCodeArr.equals(""))
									 {
										 areaCodeArr = areaCode;
									 }
									 else
									 {
										 areaCodeArr = areaCodeArr + "~" + areaCode;
									 }
									 phoneNumber = phoneElement.getChild("wd:Phone_Number") != null?phoneElement.getChild("wd:Phone_Number").getValue().trim():"";
									 if(phoneNumberArr.equals(""))
									 {
										 phoneNumberArr = phoneNumber;
									 }
									 else
									 {
										 phoneNumberArr = phoneNumberArr + "~" + phoneNumber;
									 }
									 
									 ReportElement element2 = phoneElement.getChild("wd:Phone_Device_Type_Reference");
									 if(element2 != null)
									 {
										 List<ReportElement> phDeviceData = element2.getChildren("wd:ID");								 
										 for(ReportElement phDeviceElement:phDeviceData)
										 {
											 phDeviceMap = phDeviceElement.getAllAttributes();
											 if(phDeviceMap.get("wd:type").equals("Phone_Device_Type_ID"))
											 {
												 phoneDeviceType = phDeviceElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 phoneDeviceType = "";
									 }
									 
									 if(phoneDeviceTypeArr.equals(""))
									 {
										 phoneDeviceTypeArr = phoneDeviceType;
									 }
									 else
									 {
										 phoneDeviceTypeArr = phoneDeviceTypeArr + "~" + phoneDeviceType;
									 }
									 
									 ReportElement element3 = phoneElement.getChild("wd:Usage_Data");
									 if(element3 != null)
									 {
										 ReportElement element4 = element3.getChild("wd:Type_Data");
										 if(element4 != null)
										 {
											 ReportElement element5 = element4.getChild("wd:Type_Reference");
											 if(element5 !=null)
											 {
												 List<ReportElement> usageData = element5.getChildren("wd:ID");					 
												 for(ReportElement wdElement:usageData)
												 {
													 usageMap = wdElement.getAllAttributes();
													 if(usageMap.get("wd:type").equals("Communication_Usage_Type_ID"))
													 {
														 usageType = wdElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 usageType = ""; 
											 }
										 }
									 }
									 else
									 {
										 usageType = "";
									 }
									 
									 if(usageTypeArr.equals(""))
									 {
										 usageTypeArr = usageType;
									 }
									 else
									 {
										 usageTypeArr = usageTypeArr + "~" + usageType;
									 }
								 }							 
							 }
							 else
							 {
								 countryISOCodeArr = "";
								 intPhoneCodeArr = "";
								 areaCodeArr = "";
								 phoneNumberArr = "";
								 phoneDeviceTypeArr = "";
								 usageTypeArr = "";
							 }
							 
							 headingFromWD = "Applicant_ID,Country_ISO_Code,Country_Phone_Code,Area_Code,Phone_Number,Phone_Device_Type,Usage_Type_Phone";
							 
							 headerStr = applicantId + "," + countryISOCodeArr + "," + intPhoneCodeArr + "," + areaCodeArr + "," + phoneNumberArr + "," + phoneDeviceTypeArr + "," + usageTypeArr;
							 
							 if(finalStr.equals(""))
							 {
								 finalStr = headingFromWD + "\n" + headerStr;
							 }
							 else
							 {
								 finalStr = finalStr + "\n" + headerStr;
							 }
						 }
					 }				 
					 
					 /*[] headingFromWdArr = headingFromWD.split(",");
					 for(int i = 0;i<headingFromWdArr.length; i++)
					 {
						String headingValWd = headingFromWdArr[i].replace("_", " ");
						if(headingValWd.equalsIgnoreCase("Phone Number"))
						{
							phNoCntWD = i;
						}
					 }
					 
					 String[] headingFromSrcArr = headingFromSource.split(",");
					 for(int i = 0;i<headingFromSrcArr.length; i++)
					 {
						String headingValSrc = headingFromSrcArr[i].replace("_", " ");
						if(headingValSrc.equalsIgnoreCase("Phone Number"))
						{
							phNoCntSrc = i;
						}
					 }
					 
					 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
					 byte[] srcCSVFileContent = postLoad.getSrcCSVFileContent();
					 File sourceCSVfile = null;
					 try 
					 {
						 sourceCSVfile = File.createTempFile(postLoad.getSrcCSVFileName().substring(0, postLoad.getSrcCSVFileName().indexOf(".")), ".csv");
						 FileUtils.writeByteArrayToFile(sourceCSVfile, srcCSVFileContent);
					 } 
					 catch (IOException e1) 
					 {
						 e1.printStackTrace();
					 }
					 String srcFile = sourceCSVfile.getAbsolutePath();
					 
					 String line = "";
					 int count = 0;
				     BufferedReader reader = new BufferedReader(new FileReader(srcFile));
				     while ((line = reader.readLine()) != null) 
				     { 
				         if(count != 0 && line.length() > 0) 
				         {
				        	 String [] lineArr = line.split(",");
				        	 if(lineArr.length >1)
				        	 {
					        	 String existStr = updateWDCSVFileForMultipleRow(lineArr[0], lineArr[phNoCntSrc], phNoCntWD, finalStr);
					        	 if(existStr.length() > 0)
					        	 {
					        		 if(newFinalStr.equals(""))
									 {
					        			 newFinalStr = headingFromWD + "\n" + existStr;
									 }
									 else
									 {
										 newFinalStr = newFinalStr + "\n" + existStr;
									 }
					        	 }
				        	 }
				         }
				         count++;
				     }
				     reader.close();*/
				     
				     System.out.println(finalStr);
				     
				     //wdCount = sourceCount - errorList.size();
					 
					 /*String wdCSVfile = "config/" + loadCycle + "_" + ruleName + ".csv";			 
					 PrintWriter writer = new PrintWriter(new File(wdCSVfile));
					 writer.write(newFinalStr.toString());
					 writer.flush();
					 writer.close();*/
					 
					 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
					 PrintWriter writer = new PrintWriter(wdCSVfile);
					 writer.write(finalStr.toString());
					 writer.flush();
					 writer.close();
					 
					 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
					 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
					 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
					 postLoadService.updatePostLoad(postLoad);
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Applicant_ID");
				 }
				 /*else
				 {
					 headingWd = new JSONArray();
					 JSONObject obj = new JSONObject();
					 obj.put("errorMsg", faultStr.substring(faultStr.indexOf("."), faultStr.length()));
					 headingWd.put(obj); 
				 }*/
			 }

		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return headingWd;
	}

	private String updateWDCSVFileForMultipleRow(String applicantId, String phoneNumber, int phNoCntWD, String finalStr) {

		String finalString = "";
		String line = "";
		int count = 0;
		BufferedReader reader = new BufferedReader(new StringReader(finalStr));
		try 
		{
			while ((line = reader.readLine()) != null) 
			{ 
			     if(count != 0 && line.length() > 0) 
			     {
			    	 String [] lineArr = line.split(",");
			    	 if(lineArr.length >1)
			    	 {
			    		 if(lineArr[0].equalsIgnoreCase(applicantId) && lineArr[phNoCntWD].equalsIgnoreCase(phoneNumber))
			 			 {
			 				finalString = line;
			 				break;
			 			 }
			    	 }
			     }
			     count++;
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return finalString;
	}

	private JSONArray createCSVFromWDWServiceDates(Tenant tenant, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {
		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_WORKER_SERVICE_DATES = requestfile.getAbsolutePath();
				 //String outputfile = GET_WORKER_SERVICE_DATES;
				 String outputfile = addHireIdList(GET_WORKER_SERVICE_DATES, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");				 

				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");			 
				 
				 ReportElement soapResp = XmlParserManager.parseXml(strMsg);
				 
				 ReportElement pageData = soapResp.getChild("env:Body")
							.getChild("wd:Get_Workers_Response")
							.getChild("wd:Response_Results");
				 
				 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
				 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
				 int totalResult = Integer.parseInt(totalResults);
				 System.out.println("totalNoOfPages-"+totalNoOfPages);
				 System.out.println("totalResult-"+totalResult);
				 wdCount = totalResult;
				 
				 String finalStr = "";
				 String headerStr = "";
				 String workerId = "";
				 String workerType = "";
				 String originalHireDate = "";
				 String contServiceDate = "";
				 String expectedRetirementDate = "";
				 String retirementElegibilityDate = "";
				 String endEmploymentDate = "";
				 String seniorityDate = "";
				 String severanceDate = "";
				 String benefitsServiceDate = "";
				 String companyServiceDate = "";
				 String timeOffServiceDate = "";
				 String vestingDate = "";
				 String workforceEnteredDate = "";
				 String daysUnemployed = "";
				 String monthsContPriorEmp = "";
				 String contractEndDate = "";
				 
				 Map<String,String> idMap = null;
				 
				 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
				 {					 
					 if(j == 1)
					 {
						startIndex = 0;
						if(1000 > totalResult)
						{
							endIndex = totalResult;
						}
						else
						{
							endIndex = j*1000;
						}
					 }
					 else
					 {
						//int lastVal = (j - 1);
						startIndex = endIndex;
						if(j*1000 > totalResult)
						{
							endIndex = totalResult;
						}
						else
						{
							endIndex = (j*1000);
						}
					 }
					 outputfile = addHireIdList(GET_WORKER_SERVICE_DATES, columnList, ruleName, startIndex, endIndex, "Employee_ID");	
					 is = new FileInputStream(outputfile);
				     soapMessage = MessageFactory.newInstance().createMessage(null, is);
				     soapPart = soapMessage.getSOAPPart();
				     envelope = soapPart.getEnvelope();
					 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
					 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
					 {
							envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
					 }
					 soapMessage.saveChanges();
			         out = new ByteArrayOutputStream();
			         soapMessage.writeTo(out);
			         strMsg = new String(out.toByteArray());
			        
			         soapConnectionFactory = SOAPConnectionFactory.newInstance();
					 soapConnection = soapConnectionFactory.createConnection();
			         soapResponse = soapConnection.call(soapMessage, sourceUrl);
			         out = new ByteArrayOutputStream();
			         soapResponse.writeTo(out);
			         strMsg = new String(out.toByteArray(), "utf-8");
			        
			         soapResp = XmlParserManager.parseXml(strMsg);
				 
					 ReportElement responseData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Data");
					 
					 List<ReportElement> workerData = responseData.getChildren("wd:Worker");
						
					 for(ReportElement reportElement : workerData)
					 {
						 ReportElement element1 = reportElement.getChild("wd:Worker_Reference");
						 if(element1 != null)
						 {
							 List<ReportElement> idData = element1.getChildren("wd:ID");					 
							 for(ReportElement wdElement:idData)
							 {
								 idMap = wdElement.getAllAttributes();
								 if(idMap.get("wd:type").equals("Employee_ID"))
								 {
									 workerId = wdElement.getValue().trim();
									 workerType = "Employee_ID";
								 }
								 else if(idMap.get("wd:type").equals("Contingent_Worker_ID"))
								 {
									 workerId = wdElement.getValue().trim();
									 workerType = "Contingent_Worker_ID";
								 }
							 }
						 }
						 
						 ReportElement element2 = reportElement.getChild("wd:Worker_Data")
		 							.getChild("wd:Employment_Data")
		 							.getChild("wd:Worker_Status_Data");
						 
						 if(element2 != null)
						 {
							 originalHireDate = element2.getChild("wd:Original_Hire_Date") != null?element2.getChild("wd:Original_Hire_Date").getValue().trim():"";
							 if(!originalHireDate.isEmpty())
							 {
								 originalHireDate = originalHireDate.substring(0, 10);
							 }
							 contServiceDate = element2.getChild("wd:Continuous_Service_Date") != null?element2.getChild("wd:Continuous_Service_Date").getValue().trim():"";
							 if(!contServiceDate.isEmpty())
							 {
								 contServiceDate = contServiceDate.substring(0, 10);
							 }
							 expectedRetirementDate = element2.getChild("wd:Expected_Retirement_Date") != null?element2.getChild("wd:Expected_Retirement_Date").getValue().trim():"";
							 if(!expectedRetirementDate.isEmpty())
							 {
								 expectedRetirementDate = expectedRetirementDate.substring(0, 10);
							 }
							 retirementElegibilityDate = element2.getChild("wd:Retirement_Eligibility_Date") != null?element2.getChild("wd:Retirement_Eligibility_Date").getValue().trim():"";
							 if(!retirementElegibilityDate.isEmpty())
							 {
								 retirementElegibilityDate = retirementElegibilityDate.substring(0, 10);
							 }
							 endEmploymentDate = element2.getChild("wd:End_Employment_Date") != null?element2.getChild("wd:End_Employment_Date").getValue().trim():"";
							 if(!endEmploymentDate.isEmpty())
							 {
								 endEmploymentDate = endEmploymentDate.substring(0, 10);
							 }
							 seniorityDate = element2.getChild("wd:Seniority_Date") != null?element2.getChild("wd:Seniority_Date").getValue().trim():"";
							 if(!seniorityDate.isEmpty())
							 {
								 seniorityDate = seniorityDate.substring(0, 10);
							 }
							 severanceDate = element2.getChild("wd:Severance_Date") != null?element2.getChild("wd:Severance_Date").getValue().trim():""; 
							 if(!severanceDate.isEmpty())
							 {
								 severanceDate = severanceDate.substring(0, 10);
							 }
							 benefitsServiceDate = element2.getChild("wd:Benefits_Service_Date") != null?element2.getChild("wd:Benefits_Service_Date").getValue().trim():"";
							 if(!benefitsServiceDate.isEmpty())
							 {
								 benefitsServiceDate = benefitsServiceDate.substring(0, 10);
							 }
							 companyServiceDate = element2.getChild("wd:Company_Service_Date") != null?element2.getChild("wd:Company_Service_Date").getValue().trim():""; 
							 if(!companyServiceDate.isEmpty())
							 {
								 companyServiceDate = companyServiceDate.substring(0, 10);
							 }
							 timeOffServiceDate = element2.getChild("wd:Time_Off_Service_Date") != null?element2.getChild("wd:Time_Off_Service_Date").getValue().trim():"";
							 if(!timeOffServiceDate.isEmpty())
							 {
								 timeOffServiceDate = timeOffServiceDate.substring(0, 10);
							 }
							 vestingDate = element2.getChild("wd:Vesting_Date") != null?element2.getChild("wd:Vesting_Date").getValue().trim():""; 
							 if(!vestingDate.isEmpty())
							 {
								 vestingDate = vestingDate.substring(0, 10);
							 }
							 workforceEnteredDate = element2.getChild("wd:Date_Entered_Workforce") != null?element2.getChild("wd:Date_Entered_Workforce").getValue().trim():"";
							 if(!workforceEnteredDate.isEmpty())
							 {
								 workforceEnteredDate = workforceEnteredDate.substring(0, 10);
							 }
							 daysUnemployed = element2.getChild("wd:Days_Unemployed") != null?element2.getChild("wd:Days_Unemployed").getValue().trim():""; 
							 monthsContPriorEmp = element2.getChild("wd:Months_Continuous_Prior_Employment") != null?element2.getChild("wd:Months_Continuous_Prior_Employment").getValue().trim():"";
							 
						 }
						 else
						 {
							 originalHireDate = "";
							 contServiceDate = "";
							 expectedRetirementDate = "";							 
							 retirementElegibilityDate = "";
							 endEmploymentDate = "";
							 seniorityDate = "";
							 severanceDate = "";
							 benefitsServiceDate = "";
							 companyServiceDate = "";
							 timeOffServiceDate = "";
							 vestingDate = "";
							 workforceEnteredDate = "";
							 daysUnemployed = "";
							 monthsContPriorEmp = "";
						 }
						 
						 ReportElement element3 = reportElement.getChild("wd:Worker_Data")
		 							.getChild("wd:Employment_Data")
		 							.getChild("wd:Worker_Contract_Data");
						 
						 if(element3 != null)
						 {
							 contractEndDate = element3.getChild("wd:Contract_End_Date") != null?element3.getChild("wd:Contract_End_Date").getValue().trim():""; 
						 }
						 else
						 {
							 contractEndDate = "";
						 }
						 
						 headingFromWD = "Worker_ID,Worker_Type,Original_Hire_Date,Continuous_Service_Date,Expected_Retirement_Date,Retirement_Eligibility_Date,End_Employment_Date,Seniority_Date,"
						 		         + "Severance_Date,Contract_End_Date,Benefits_Service_Date,Company_Service_Date,Time_Off_Service_Date,Vesting_Date,Date_Entered_Workforce,Days_Unemployed,"
						 		         + "Months_Continuous_Prior_Employment";
							 
						 headerStr = workerId + "," + workerType + "," + originalHireDate + "," + contServiceDate + "," + expectedRetirementDate + "," + retirementElegibilityDate + "," + endEmploymentDate + ","  
								 + seniorityDate + "," + severanceDate + "," + contractEndDate + "," + benefitsServiceDate + "," + companyServiceDate + "," + timeOffServiceDate + "," + vestingDate + "," 
								 + workforceEnteredDate + "," + daysUnemployed + "," + monthsContPriorEmp;
						 
						 if(finalStr.equals(""))
						 {
							 finalStr = headingFromWD + "\n" + headerStr;
						 }
						 else
						 {
							 finalStr = finalStr + "\n" + headerStr;
						 }					 
					 }
				 }
				 
				 System.out.println(finalStr);
				 
				 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
				 PrintWriter writer = new PrintWriter(wdCSVfile);
				 writer.write(finalStr.toString());
				 writer.flush();
				 writer.close();
				 
				 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
				 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
				 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
				 postLoadService.updatePostLoad(postLoad);
				 
				 headingWd = selectColumnMapping(loadCycle, ruleName, "Worker_ID");
				 
				 /*String wdCSVfile1 = "config/" + loadCycle + "_" + ruleName + ".csv";			 
				 PrintWriter writer1 = new PrintWriter(new File(wdCSVfile1));
				 writer1.write(finalStr.toString());
				 writer1.flush();
				 writer1.close();*/
			 }
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}

	private JSONArray createCSVFromWDWorkerDemographic(Tenant tenant, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		//String faultStr = null;
		String checkFile = null;
		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_WORKER_DEMOGRAPHIC_FILE = requestfile.getAbsolutePath();
				 if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addWorkerIdListToFindError(GET_WORKER_DEMOGRAPHIC_FILE, columnList.get(i), ruleName, "Employee_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement reportElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = reportElement.getChild("env:Body")
									 .getChild("wd:Get_Workers_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
						 }
					 }
					 columnList.removeAll(errorList);
				 }
				 //String outputfile = GET_WORKER_DEMOGRAPHIC_FILE;
				 String outputfile = addHireIdList(GET_WORKER_DEMOGRAPHIC_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");

				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");			 
				 /*if(strMsg.contains("faultstring"))
				 {
					 ReportElement msgResponse = XmlParserManager.parseXml(strMsg);
					 ReportElement elementData = msgResponse.getChild("SOAP-ENV:Body")
								.getChild("SOAP-ENV:Fault");
					 faultStr = elementData.getChild("faultstring").getValue().trim();
					 System.out.println("faultStr-"+faultStr);
				 }*/
				 
				 //if(faultStr == null)
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String finalStr = "";
					 String headerStr = "";
					 String workerId = "";
					 String workerType = "";
					 String maritalStatusDate = "";
					 String ethnicityName = "";
					 String hispLatino = "";
					 String maritalStatusName = "";
					 String citizenName = "";
					 String countryISOCodeNation = "";
					 String militaryStatusName = "";
					 String dischargeDate = "";
					 String religionName = "";
					 String polAffilationName = "";
					 String countryISOCodeBusinessSite = "";
					 
					 Map<String,String> idMap = null;
					 Map<String,String> maritalMap = null;
					 Map<String,String> ethnMap = null;
					 Map<String,String> citizenMap = null;
					 Map<String,String> nationMap = null;
					 Map<String,String> militaryMap = null;
					 Map<String,String> religionMap = null;
					 Map<String,String> politicalMap = null;
					 Map<String,String> businessMap = null;
					 Map<String,String> businessCodeMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {					 
						 if(j == 1)
						 {
							startIndex = 0;
							if(1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*1000;
							}
						 }
						 else
						 {
							int lastVal = (j - 1);
							startIndex = (endIndex - lastVal);
							if(j*1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = (j*startIndex) + 1;
							}
						 }
						 outputfile = addHireIdList(GET_WORKER_DEMOGRAPHIC_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
						 is = new FileInputStream(outputfile);
					     soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     soapPart = soapMessage.getSOAPPart();
					     envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
				         out = new ByteArrayOutputStream();
				         soapMessage.writeTo(out);
				         strMsg = new String(out.toByteArray());
				        
				         soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
				         soapResponse = soapConnection.call(soapMessage, sourceUrl);
				         out = new ByteArrayOutputStream();
				         soapResponse.writeTo(out);
				         strMsg = new String(out.toByteArray(), "utf-8");
				        
				         soapResp = XmlParserManager.parseXml(strMsg);
					 
						 ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Workers_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> workerData = responseData.getChildren("wd:Worker");
							
						 for(ReportElement reportElement : workerData)
						 {
							 ReportElement element1 = reportElement.getChild("wd:Worker_Reference");
							 if(element1 != null)
							 {
								 List<ReportElement> idData = element1.getChildren("wd:ID");					 
								 for(ReportElement wdElement:idData)
								 {
									 idMap = wdElement.getAllAttributes();
									 if(idMap.get("wd:type").equals("Employee_ID"))
									 {
										 workerId = wdElement.getValue().trim();
										 workerType = "Employee_ID";
									 }
									 else if(idMap.get("wd:type").equals("Contingent_Worker_ID"))
									 {
										 workerId = wdElement.getValue().trim();
										 workerType = "Contingent_Worker_ID";
									 }
								 }
							 }
	
							 ReportElement element2 = reportElement.getChild("wd:Worker_Data")
			 							.getChild("wd:Personal_Data");
							 
							 if(element2 != null)
							 {							 
								 ReportElement element3 = element2.getChild("wd:Marital_Status_Reference");
								 if(element3 != null)
								 {
									 List<ReportElement> maritalData = element3.getChildren("wd:ID");					 
									 for(ReportElement maritalElement:maritalData)
									 {
										 maritalMap = maritalElement.getAllAttributes();
										 if(maritalMap.get("wd:type").equals("Marital_Status_ID"))
										 {
											 maritalStatusName = maritalElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 maritalStatusName = "";
								 }
								 
								 maritalStatusDate = element2.getChild("wd:Marital_Status_Date") != null?element2.getChild("wd:Marital_Status_Date").getValue().trim():"";
								 
								 ReportElement element4 = element2.getChild("wd:Ethnicity_Reference");
								 if(element4 != null)
								 {
									 List<ReportElement> ethnData = element4.getChildren("wd:ID");					 
									 for(ReportElement ethnElement:ethnData)
									 {
										 ethnMap = ethnElement.getAllAttributes();
										 if(ethnMap.get("wd:type").equals("Ethnicity_ID"))
										 {
											 ethnicityName = ethnElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 ethnicityName = "";
								 }
								 
								 hispLatino = element2.getChild("wd:Hispanic_or_Latino") != null?element2.getChild("wd:Hispanic_or_Latino").getValue().trim():"";
								 if(hispLatino.equals("1"))
								 {
									 hispLatino = "Y"; 
								 }
								 else
								 {
									 hispLatino = "N";  
								 }
								 
								 ReportElement element5 = element2.getChild("wd:Citizenship_Status_Reference");
								 if(element5 != null)
								 {
									 List<ReportElement> citizenData = element5.getChildren("wd:ID");					 
									 for(ReportElement citizenElement:citizenData)
									 {
										 citizenMap = citizenElement.getAllAttributes();
										 if(citizenMap.get("wd:type").equals("Citizenship_Status_Code"))
										 {
											 citizenName = citizenElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 citizenName = "";
								 }
								 
								 ReportElement element6 = element2.getChild("wd:Primary_Nationality_Reference");
								 if(element6 != null)
								 {
									 List<ReportElement> nationData = element6.getChildren("wd:ID");					 
									 for(ReportElement nationElement:nationData)
									 {
										 nationMap = nationElement.getAllAttributes();
										 if(nationMap.get("wd:type").equals("ISO_3166-1_Alpha-3_Code"))
										 {
											 countryISOCodeNation = nationElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 countryISOCodeNation = "";
								 }
								 
								 ReportElement element7 = element2.getChild("wd:Military_Service_Data");
								 if(element7 != null)
								 {
									 dischargeDate = element7.getChild("wd:Discharge_Date") != null?element7.getChild("wd:Discharge_Date").getValue().trim():"";
									 
									 ReportElement element8 = element7.getChild("wd:Status_Reference");
									 if(element8 != null)
									 {
										 List<ReportElement> militaryData = element8.getChildren("wd:ID");					 
										 for(ReportElement militaryElement:militaryData)
										 {
											 militaryMap = militaryElement.getAllAttributes();
											 if(militaryMap.get("wd:type").equals("Military_Status_ID"))
											 {
												 militaryStatusName = militaryElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 militaryStatusName = "";
									 }
								 }
								 else
								 {
									 militaryStatusName = "";
									 dischargeDate = "";
								 }
								 
								 ReportElement element9 = element2.getChild("wd:Religion_Reference");
								 if(element9 != null)
								 {
									 List<ReportElement> religionData = element9.getChildren("wd:ID");					 
									 for(ReportElement religionElement:religionData)
									 {
										 religionMap = religionElement.getAllAttributes();
										 if(religionMap.get("wd:type").equals("Religion_ID"))
										 {
											 religionName = religionElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 religionName = "";
								 }
								 
								 ReportElement element10 = element2.getChild("wd:Political_Affiliation_Reference");
								 if(element10 != null)
								 {
									 List<ReportElement> politicalData = element10.getChildren("wd:ID");					 
									 for(ReportElement politicalElement:politicalData)
									 {
										 politicalMap = politicalElement.getAllAttributes();
										 if(politicalMap.get("wd:type").equals("Political_Affiliation_ID"))
										 {
											 polAffilationName = politicalElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 polAffilationName = "";
								 }
								 
								 ReportElement element11 = element2.getChild("wd:Contact_Data");
								 if(element11 != null)
								 {
									 ReportElement element12 = element11.getChild("wd:Address_Data");
									 if(element12 != null)
									 {
										 businessMap = element12.getAllAttributes();
										 if(businessMap.get("wd:Defaulted_Business_Site_Address") != null && businessMap.get("wd:Defaulted_Business_Site_Address").equals("1"))
										 {
											 ReportElement element13 = element12.getChild("wd:Country_Reference");
											 if(element13 != null)
											 {
												 List<ReportElement> businessData = element13.getChildren("wd:ID");					 
												 for(ReportElement businessElement:businessData)
												 {
													 businessCodeMap = businessElement.getAllAttributes();
													 if(businessCodeMap.get("wd:type").equals("ISO_3166-1_Alpha-3_Code"))
													 {
														 countryISOCodeBusinessSite = businessElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 countryISOCodeBusinessSite = "";
											 }
										 }
										 else
										 {
											 countryISOCodeBusinessSite = "";
										 }
									 }
									 else
									 {
										 countryISOCodeBusinessSite = "";
									 }
								 }
								 else
								 {
									 countryISOCodeBusinessSite = "";
								 }
							 }
							 else
							 {
								 maritalStatusName = "";
								 maritalStatusDate = "";
								 ethnicityName = "";
								 hispLatino = "";
								 ethnicityName = "";
								 citizenName = "";
								 countryISOCodeNation = "";
								 militaryStatusName = "";
								 dischargeDate = "";
								 religionName = "";
								 polAffilationName = "";
								 countryISOCodeBusinessSite = "";
							 }
							 
							 headingFromWD = "Worker_ID,Worker_Type,Country_ISO_Code,Marital_Status_Name,Marital_Status_Date,Hispanic_or_Latino,Ethnicity_Name,"
							 		+ "Citizenship_Status_Description,Country_ISO_Code_Nationality,Military_Status_Name,Military_Discharge_Date,Religion_Name,Political_Affiliation_Name";
								 
							 headerStr = workerId + "," + workerType + "," + countryISOCodeBusinessSite + "," + maritalStatusName + "," + maritalStatusDate + "," + hispLatino + "," + ethnicityName + "," + 
									 citizenName + "," + countryISOCodeNation + "," + militaryStatusName + "," + dischargeDate + "," + religionName + "," + polAffilationName;
							 
							 if(finalStr.equals(""))
							 {
								 finalStr = headingFromWD + "\n" + headerStr;
							 }
							 else
							 {
								 finalStr = finalStr + "\n" + headerStr;
							 }					 
						 }
					 }
					 
					 System.out.println(finalStr);
					 
					 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
					 PrintWriter writer = new PrintWriter(wdCSVfile);
					 writer.write(finalStr.toString());
					 writer.flush();
					 writer.close();
					 
					 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
					 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
					 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
					 postLoadService.updatePostLoad(postLoad);
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Worker_ID");
					 
					 /*String wdCSVfile1 = "config/" + loadCycle + "_" + ruleName + ".csv";			 
					 PrintWriter writer1 = new PrintWriter(new File(wdCSVfile1));
					 writer1.write(finalStr.toString());
					 writer1.flush();
					 writer1.close();*/
				 }
			 }
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}

	private JSONArray createCSVFromWDWorkerBiographic(Tenant tenant2, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {
		
		String checkFile = null;
		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_WORKER_BIOGRAPHIC_FILE = requestfile.getAbsolutePath();
				 //String outputfile = GET_WORKER_BIOGRAPHIC_FILE;
				 if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addWorkerIdListToFindError(GET_WORKER_DEMOGRAPHIC_FILE, columnList.get(i), ruleName, "Employee_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement reportElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = reportElement.getChild("env:Body")
									 .getChild("wd:Get_Workers_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
						 }
					 }
					 columnList.removeAll(errorList);
				 }
				 String outputfile = addHireIdList(GET_WORKER_BIOGRAPHIC_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");

				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");			 
				 /*if(strMsg.contains("faultstring"))
				 {
					 ReportElement msgResponse = XmlParserManager.parseXml(strMsg);
					 ReportElement elementData = msgResponse.getChild("SOAP-ENV:Body")
								.getChild("SOAP-ENV:Fault");
					 faultStr = elementData.getChild("faultstring").getValue().trim();
					 System.out.println("faultStr-"+faultStr);
				 }*/
				 
				 //if(faultStr == null)
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String finalStr = "";
					 String headerStr = "";
					 String workerId = "";
					 String workerType = "";
					 String birthDate = "";
					 String birthCity = "";
					 String birthRegion = "";
					 String countryISOCode = "";
					 String genderDesc = "";
					 String disabilityName = "";
					 String disabilityKnownDate = "";
					 String tobaccoUse = "";
					 
					 Map<String,String> idMap = null;
					 Map<String,String> countryMap = null;
					 Map<String,String> genderMap = null;
					 Map<String,String> disabilityMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {					 
						 if(j == 1)
						 {
							startIndex = 0;
							if(1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*1000;
							}
						 }
						 else
						 {
							int lastVal = (j - 1);
							startIndex = (endIndex - lastVal);
							if(j*1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = (j*startIndex) + 1;
							}
						 }
						 outputfile = addHireIdList(GET_WORKER_BIOGRAPHIC_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
						 is = new FileInputStream(outputfile);
					     soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     soapPart = soapMessage.getSOAPPart();
					     envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
				         out = new ByteArrayOutputStream();
				         soapMessage.writeTo(out);
				         strMsg = new String(out.toByteArray());
				        
				         soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
				         soapResponse = soapConnection.call(soapMessage, sourceUrl);
				         out = new ByteArrayOutputStream();
				         soapResponse.writeTo(out);
				         strMsg = new String(out.toByteArray(), "utf-8");
				        
				         soapResp = XmlParserManager.parseXml(strMsg);
					 
						 ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Workers_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> workerData = responseData.getChildren("wd:Worker");
							
						 for(ReportElement reportElement : workerData)
						 {
							 ReportElement element1 = reportElement.getChild("wd:Worker_Reference");
							 if(element1 != null)
							 {
								 List<ReportElement> idData = element1.getChildren("wd:ID");					 
								 for(ReportElement wdElement:idData)
								 {
									 idMap = wdElement.getAllAttributes();
									 if(idMap.get("wd:type").equals("Employee_ID"))
									 {
										 workerId = wdElement.getValue().trim();
										 workerType = "Employee_ID";
									 }
									 else if(idMap.get("wd:type").equals("Contingent_Worker_ID"))
									 {
										 workerId = wdElement.getValue().trim();
										 workerType = "Contingent_Worker_ID";
									 }
								 }
							 }
	
							 ReportElement element2 = reportElement.getChild("wd:Worker_Data")
			 							.getChild("wd:Personal_Data");
							 
							 if(element2 != null)
							 {
								 birthDate = element2.getChild("wd:Birth_Date") != null?element2.getChild("wd:Birth_Date").getValue().trim():"";
								 birthCity = element2.getChild("wd:City_of_Birth") != null?element2.getChild("wd:City_of_Birth").getValue().trim():"";
								 if(birthCity.contains(","))
								 {
									 birthCity =  birthCity.replace(",", "|");
								 }
								 birthRegion = element2.getChild("wd:Region_of_Birth_Descriptor") != null?element2.getChild("wd:Region_of_Birth_Descriptor").getValue().trim():"";
								 tobaccoUse = element2.getChild("wd:Tobacco_Use") != null?element2.getChild("wd:Tobacco_Use").getValue().trim():"";
								 if(tobaccoUse.equals("1"))
								 {
									 tobaccoUse = "Y";
								 }
								 else
								 {
									 tobaccoUse = "N";
								 }
								 
								 ReportElement element3 = element2.getChild("wd:Country_of_Birth_Reference");
								 if(element3 != null)
								 {
									 List<ReportElement> countryData = element3.getChildren("wd:ID");					 
									 for(ReportElement countryElement:countryData)
									 {
										 countryMap = countryElement.getAllAttributes();
										 if(countryMap.get("wd:type").equals("ISO_3166-1_Alpha-3_Code"))
										 {
											 countryISOCode = countryElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 countryISOCode = "";
								 }
								 
								 ReportElement element4 = element2.getChild("wd:Gender_Reference");
								 if(element4 != null)
								 {
									 List<ReportElement> genderData = element4.getChildren("wd:ID");					 
									 for(ReportElement genderElement:genderData)
									 {
										 genderMap = genderElement.getAllAttributes();
										 if(genderMap.get("wd:type").equals("Gender_Code"))
										 {
											 genderDesc = genderElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 genderDesc = "";
								 }
								 
								 ReportElement element5 = element2.getChild("wd:Disability_Status_Data");
								 if(element5 != null)
								 {
									 ReportElement element6 = element5.getChild("wd:Disability_Reference");
									 if(element6 != null)
									 {
										 List<ReportElement> disabilityData = element6.getChildren("wd:ID");					 
										 for(ReportElement disabilityElement:disabilityData)
										 {
											 disabilityMap = disabilityElement.getAllAttributes();
											 if(disabilityMap.get("wd:type").equals("Disability_ID"))
											 {
												 disabilityName = disabilityElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 disabilityName = "";
									 }							 
								 }
								 else
								 {
									 disabilityName = "";
								 }
								 disabilityKnownDate = element2.getChild("wd:Disability_Date_Known") != null?element2.getChild("wd:Disability_Date_Known").getValue().trim():"";
							 }
							 else
							 {
								 birthDate = "";
								 birthCity = "";
								 birthRegion = "";
								 countryISOCode = "";
								 genderDesc = "";
								 disabilityName = "";
								 disabilityKnownDate = "";
								 tobaccoUse = "";
							 }
							 
							 headingFromWD = "Worker_ID,Worker_Type,Country_ISO_Code,Region_of_Birth,City_of_Birth,Birth_Date,Gender_Description,Disability_Name,Date_Employer_Learned_of_Disability_Status,"
							 		+ "Uses_Tobacco";
								 
							 headerStr = workerId + "," + workerType + "," + countryISOCode + "," + birthRegion + "," + birthCity + "," + birthDate + "," + genderDesc + "," + disabilityName + "," + 
									 	disabilityKnownDate + "," + tobaccoUse;
							 
							 if(finalStr.equals(""))
							 {
								 finalStr = headingFromWD + "\n" + headerStr;
							 }
							 else
							 {
								 finalStr = finalStr + "\n" + headerStr;
							 }					 
						 }
					 }
					 
					 System.out.println(finalStr);
					 
					 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
					 PrintWriter writer = new PrintWriter(wdCSVfile);
					 writer.write(finalStr.toString());
					 writer.flush();
					 writer.close();
					 
					 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
					 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
					 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
					 postLoadService.updatePostLoad(postLoad);
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Worker_ID");
					 
					 /*String wdCSVfile1 = "config/" + loadCycle + "_" + ruleName + ".csv";			 
					 PrintWriter writer1 = new PrintWriter(new File(wdCSVfile1));
					 writer1.write(finalStr.toString());
					 writer1.flush();
					 writer1.close();*/
				 }
			 }
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}
	
	private JSONArray createCSVFromWDEndContingentWorker(Tenant tenant2, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		//String faultStr = null;
		String checkFile = null;
		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_END_CONTINGENT_WORKER_FILE = requestfile.getAbsolutePath();
				 //String outputfile = GET_END_CONTINGENT_WORKER_FILE;
				 if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addWorkerIdListToFindError(GET_END_CONTINGENT_WORKER_FILE, columnList.get(i), ruleName, "Contingent_Worker_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement reportElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = reportElement.getChild("env:Body")
										.getChild("wd:Get_Workers_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
						 }
					 }
					 columnList.removeAll(errorList);
				 }
				 String outputfile = addHireIdList(GET_END_CONTINGENT_WORKER_FILE, columnList, ruleName, startIndex, columnList.size(), "Contingent_Worker_ID");

				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");			 
				 /*if(strMsg.contains("faultstring"))
				 {
					 ReportElement msgResponse = XmlParserManager.parseXml(strMsg);
					 ReportElement elementData = msgResponse.getChild("SOAP-ENV:Body")
								.getChild("SOAP-ENV:Fault");
					 faultStr = elementData.getChild("faultstring").getValue().trim();
					 System.out.println("faultStr-"+faultStr);
				 }*/
				 
				 //if(faultStr == null)
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String finalStr = "";
					 String headerStr = "";
					 String empId = "";
					 String contractEndDate = "";
					 String lastDayOfWork = "";
					 String primaryReason = "";
					 String secondaryReason = "";
					 String localReason = "";
					 
					 Map<String,String> idMap = null;
					 Map<String,String> primaryReasonMap = null;
					 Map<String,String> secReasonMap = null;
					 Map<String,String> localReasonMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {					 
						 if(j == 1)
						 {
							startIndex = 0;
							if(1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*1000;
							}
						 }
						 else
						 {
							int lastVal = (j - 1);
							startIndex = (endIndex - lastVal);
							if(j*1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = (j*startIndex) + 1;
							}
						 }
						 outputfile = addHireIdList(GET_END_CONTINGENT_WORKER_FILE, columnList, ruleName, startIndex, endIndex, "Contingent_Worker_ID");
						 is = new FileInputStream(outputfile);
					     soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     soapPart = soapMessage.getSOAPPart();
					     envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
				         out = new ByteArrayOutputStream();
				         soapMessage.writeTo(out);
				         strMsg = new String(out.toByteArray());
				        
				         soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
				         soapResponse = soapConnection.call(soapMessage, sourceUrl);
				         out = new ByteArrayOutputStream();
				         soapResponse.writeTo(out);
				         strMsg = new String(out.toByteArray(), "utf-8");
				        
				         soapResp = XmlParserManager.parseXml(strMsg);
					 
						 ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Workers_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> workerData = responseData.getChildren("wd:Worker");
							
						 for(ReportElement reportElement : workerData)
						 {
							 ReportElement element1 = reportElement.getChild("wd:Worker_Reference");
							 if(element1 != null)
							 {
								 List<ReportElement> idData = element1.getChildren("wd:ID");					 
								 for(ReportElement wdElement:idData)
								 {
									 idMap = wdElement.getAllAttributes();
									 if(idMap.get("wd:type").equals("Contingent_Worker_ID"))
									 {
										 empId = wdElement.getValue().trim();
									 }
								 }
							 }
							 
							 ReportElement element2 = reportElement.getChild("wd:Worker_Data")
			 							.getChild("wd:Employment_Data")
			 							.getChild("wd:Worker_Status_Data");
							 
							 if(element2 != null)
							 {
								 lastDayOfWork = element2.getChild("wd:Termination_Last_Day_of_Work") != null?element2.getChild("wd:Termination_Last_Day_of_Work").getValue().trim():"";							 
								 
								 ReportElement element3 = element2.getChild("wd:Primary_Termination_Reason_Reference");
								 if(element3 != null)
								 {
									 List<ReportElement> priReasonData = element3.getChildren("wd:ID");					 
									 for(ReportElement priReasonElement:priReasonData)
									 {
										 primaryReasonMap = priReasonElement.getAllAttributes();
										 if(primaryReasonMap.get("wd:type").equals("Event_Classification_Subcategory_ID") || primaryReasonMap.get("wd:type").equals("Termination_Subcategory_ID"))
										 {
											 primaryReason = priReasonElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 primaryReason = "";
								 }
								 
								 ReportElement element4 = element2.getChild("wd:Secondary_Termination_Reasons_Data");
								 if(element4 != null)
								 {
									 ReportElement element5 = element4.getChild("wd:Secondary_Termination_Reason_Reference");
									 if(element5 != null)
									 {
										 List<ReportElement> secReasonData = element5.getChildren("wd:ID");					 
										 for(ReportElement secReasonElement:secReasonData)
										 {
											 secReasonMap = secReasonElement.getAllAttributes();
											 if(secReasonMap.get("wd:type").equals("Event_Classification_Subcategory_ID") || secReasonMap.get("wd:type").equals("Termination_Subcategory_ID"))
											 {
												 secondaryReason = secReasonElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 secondaryReason = "";
									 }
								 }
								 else
								 {
									 secondaryReason = "";
								 }
								 
								 ReportElement element6 = element2.getChild("wd:Local_Termination_Reason_Reference");
								 if(element6 != null)
								 {
									 List<ReportElement> localReasonData = element6.getChildren("wd:ID");					 
									 for(ReportElement localReasonElement:localReasonData)
									 {
										 localReasonMap = localReasonElement.getAllAttributes();
										 if(localReasonMap.get("wd:type").equals("Local_Termination_Reason_ID"))
										 {
											 localReason = localReasonElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 localReason = "";
								 }
							 }
							 else
							 {
								 lastDayOfWork = "";
								 primaryReason = "";
								 secondaryReason = "";
								 localReason = "";
							 }
							 
							 ReportElement element7 = reportElement.getChild("wd:Worker_Data")
			 							.getChild("wd:Employment_Data")
			 							.getChild("wd:Worker_Contract_Data");
							 
							 if(element7 != null)
							 {
								 contractEndDate = element7.getChild("wd:Contract_End_Date") != null?element7.getChild("wd:Contract_End_Date").getValue().trim():""; 
							 }
							 else
							 {
								 contractEndDate = "";
							 }
							 
							 headingFromWD = "Contingent_Worker_ID,Contract_End_Date,Last_Day_of_Work,Primary_Reason,Secondary_Reason,Local_Termination_Reason";
								 
							 headerStr = empId + "," + contractEndDate + "," + lastDayOfWork + "," + primaryReason + "," + secondaryReason + "," + localReason ;
							 
							 if(finalStr.equals(""))
							 {
								 finalStr = headingFromWD + "\n" + headerStr;
							 }
							 else
							 {
								 finalStr = finalStr + "\n" + headerStr;
							 }					 
						 }
					 }
					 
					 System.out.println(finalStr);
					 
					 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
					 PrintWriter writer = new PrintWriter(wdCSVfile);
					 writer.write(finalStr.toString());
					 writer.flush();
					 writer.close();
					 
					 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
					 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
					 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
					 postLoadService.updatePostLoad(postLoad);
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Contingent_Worker_ID");
					 
					 /*String wdCSVfile1 = "config/" + loadCycle + "_" + ruleName + ".csv";			 
					 PrintWriter writer1 = new PrintWriter(new File(wdCSVfile1));
					 writer1.write(finalStr.toString());
					 writer1.flush();
					 writer1.close();*/
				 }
				 /*else
				 {
					 headingWd = new JSONArray();
					 JSONObject obj = new JSONObject();
					 obj.put("errorMsg", faultStr.substring(faultStr.indexOf("."), faultStr.length()));
					 headingWd.put(obj); 
				 }*/
			 }
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}

	private JSONArray createCSVFromWDWorkerAddress(Tenant tenant, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		//String faultStr = null;
		String checkFile = null;
		try 
		{			 
			 System.out.println("columnList--"+columnList);
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_WORKER_ADDRESS_REQUEST_FILE = requestfile.getAbsolutePath();
				 
				 //String outputfile = GET_WORKER_ADDRESS_REQUEST_FILE;
				 if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addWorkerIdListToFindError(GET_WORKER_ADDRESS_REQUEST_FILE, columnList.get(i), ruleName, "Employee_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement reportElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = reportElement.getChild("env:Body")
										.getChild("wd:Get_Workers_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
						 }
					 }
					 columnList.removeAll(errorList);
				 }
				 String outputfile = addHireIdList(GET_WORKER_ADDRESS_REQUEST_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");
				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");			 
				 /*if(strMsg.contains("faultstring"))
				 {
					 ReportElement msgResponse = XmlParserManager.parseXml(strMsg);
					 ReportElement elementData = msgResponse.getChild("SOAP-ENV:Body")
								.getChild("SOAP-ENV:Fault");
					 faultStr = elementData.getChild("faultstring").getValue().trim();
					 System.out.println("faultStr-"+faultStr);
				 }*/
				 
				 //if(faultStr == null)
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);
					 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String countryISOCode = "";
					 String effectiveDate = "";
					 String addrLine_1 = "";
					 String addrLine_2 = "";
					 String addrLine_3 = "";
					 String addrLine_4 = "";
					 String addrLine_5 = "";
					 String addrLine_6 = "";
					 String addrLine_7 = "";
					 String addrLine_8 = "";
					 String addrLine_9 = "";
					 String addrLine_1_Local = "";
					 String addrLine_2_Local = "";
					 String city = "";
					 String subCity1 = "";
					 String subCity2 = "";
					 String region = "";
					 String subRegion1 = "";
					 String subRegion2 = "";
					 String usageType = "";
					 String countryISOCodeArr = "";
					 String effectiveDateArr = "";
					 String addrLine_1Arr = "";
					 String addrLine_2Arr = "";
					 String addrLine_3Arr = "";
					 String addrLine_4Arr = "";
					 String addrLine_5Arr = "";
					 String addrLine_6Arr = "";
					 String addrLine_7Arr = "";
					 String addrLine_8Arr = "";
					 String addrLine_9Arr = "";
					 String addrLine_1_LocalArr = "";
					 String addrLine_2_LocalArr = "";
					 String cityArr = "";
					 String subCity1Arr = "";
					 String subCity2Arr = "";
					 String regionArr = "";
					 String subRegion1Arr = "";
					 String subRegion2Arr = "";
					 String usageTypeArr = "";
					 String finalStr = "";
					 String headerStr = "";
					 String postalCode = "";
					 String postalCodeArr = "";
					 String email = "";
					 String emailArr = "";
					 String emailUsageType = "";
					 String emailUsageTypeArr = "";
					 String primaryEmail = "";
					 String primaryEmailArr = "";
					 String visibilityEmail = "";
					 String visibilityEmailArr = "";
					 String	countryPhCode = "";
					 String	countryPhCodeArr= "";
					 String	areaCode = "";
					 String	areaCodeArr = "";
					 String	phoneNumber = "";
					 String	phoneNumberArr = "";
					 String	phCountryISOCode = "";
					 String	phCountryISOCodeArr = "";
					 String	phoneDeviceType = "";
					 String	phoneDeviceTypeArr = "";
					 String	usageTypePhone = "";
					 String	usageTypePhoneArr = "";
					 String primaryPhone = "";
					 String primaryPhoneArr = "";
					 
					 Map<String,String> isoMap = null;
					 Map<String,String> addressMap = null;
					 Map<String,String> subCityMap = null;
					 Map<String,String> subRegionMap = null;
					 Map<String,String> addrLineMap = null;
					 Map<String,String> usageMap = null;
					 Map<String,String> emailUsageMap = null;
					 Map<String,String> visibilityEmailMap = null;
					 Map<String,String> primaryEmailMap = null;
					 Map<String,String> phUsageMap = null;
					 Map<String,String> phDeviceMap = null;
					 Map<String,String> primaryPhoneMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {
						if(j == 1)
						{
							startIndex = 0;
							if(1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*1000;
							}
						}
						else
						{
							//startIndex = (j - 1)*1000;
							int lastVal = (j - 1);
							startIndex = (endIndex - lastVal);
							if(j*1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								//endIndex = j*1000;
								endIndex = (j*startIndex) + 1;
							}
						}
						outputfile = addHireIdList(GET_WORKER_ADDRESS_REQUEST_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
						is = new FileInputStream(outputfile);
					    soapMessage = MessageFactory.newInstance().createMessage(null, is);
					    soapPart = soapMessage.getSOAPPart();
					    envelope = soapPart.getEnvelope();
						envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						{
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						}
						soapMessage.saveChanges();
				        out = new ByteArrayOutputStream();
				        soapMessage.writeTo(out);
				        strMsg = new String(out.toByteArray());
				        
				        soapConnectionFactory = SOAPConnectionFactory.newInstance();
						soapConnection = soapConnectionFactory.createConnection();
				        soapResponse = soapConnection.call(soapMessage, sourceUrl);
				        out = new ByteArrayOutputStream();
				        soapResponse.writeTo(out);
				        strMsg = new String(out.toByteArray(), "utf-8");
				        
				        soapResp = XmlParserManager.parseXml(strMsg);
					        
						ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Workers_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> workerData = responseData.getChildren("wd:Worker");
							
						 for(ReportElement reportElement : workerData)
						 {
							 ReportElement element1 = reportElement.getChild("wd:Worker_Data");
							 String workerId = element1.getChild("wd:Worker_ID").getValue().trim();
							 
							 List<ReportElement> addressList = reportElement.getChild("wd:Worker_Data")
							 			.getChild("wd:Personal_Data")
							 			.getChild("wd:Contact_Data")
							 			.getChildren("wd:Address_Data");
							 
							 if(addressList != null && addressList.size() >0)
							 {
								 effectiveDateArr = "";
								 countryISOCodeArr = "";
								 addrLine_1Arr = "";
								 addrLine_2Arr = "";
								 addrLine_3Arr = "";
								 addrLine_4Arr = "";
								 addrLine_5Arr = "";
								 addrLine_6Arr = "";
								 addrLine_7Arr = "";
								 addrLine_8Arr = "";
								 addrLine_9Arr = "";
								 addrLine_1_LocalArr = "";
							     addrLine_2_LocalArr = "";
							     cityArr = "";
							     subCity1Arr = "";
							     subCity2Arr = "";
							     regionArr = "";
							     subRegion1Arr = "";
							     subRegion2Arr = "";
							     postalCodeArr = "";
								 usageTypeArr = "";
								 for(ReportElement addressData : addressList)
								 {
									 addressMap = addressData.getAllAttributes();
									 effectiveDate = addressMap.get("wd:Effective_Date");
									 if(effectiveDateArr.equals(""))
									 {
										 effectiveDateArr = effectiveDate;
									 }
									 else
									 {
										 effectiveDateArr = effectiveDateArr + "~" + effectiveDate;
									 }
									 
									 List<ReportElement> addrLineData = addressData.getChildren("wd:Address_Line_Data");
									 if(addrLineData != null)
									 {
										 for(ReportElement addrLineElement:addrLineData)
										 {
											 addrLineMap = addrLineElement.getAllAttributes();
											 if(addrLineMap.get("wd:Type").equals("ADDRESS_LINE_1"))
											 {
												addrLine_1 =  addrLineElement.getValue().trim();
												if(addrLine_1.contains(","))
												{
													addrLine_1 = addrLine_1.replaceAll(",", "|");
												}
												if(addrLine_1Arr.equals(""))
												{
													addrLine_1Arr = addrLine_1;
												}
												else
												{
													addrLine_1Arr = addrLine_1Arr + "~" + addrLine_1;
												}
											 }
											 else if(addrLineMap.get("wd:Type").equals("ADDRESS_LINE_2"))
											 {
												addrLine_2 =  addrLineElement.getValue().trim();
												if(addrLine_2.contains(","))
												{
													addrLine_2 = addrLine_2.replaceAll(",", "|");
												}
												if(addrLine_2Arr.equals(""))
												{
													addrLine_2Arr = addrLine_2;
												}
												else
												{
													addrLine_2Arr = addrLine_2Arr + "~" + addrLine_2;
												}
											 }
											 else if(addrLineMap.get("wd:Type").equals("ADDRESS_LINE_3"))
											 {
												addrLine_3 =  addrLineElement.getValue().trim();
												if(addrLine_3.contains(","))
												{
													addrLine_3 = addrLine_3.replaceAll(",", "|");
												}
												if(addrLine_3Arr.equals(""))
												{
													addrLine_3Arr = addrLine_3;
												}
												else
												{
													addrLine_3Arr = addrLine_3Arr + "~" + addrLine_3;
												}
											 }
											 else if(addrLineMap.get("wd:Type").equals("ADDRESS_LINE_4"))
											 {
												addrLine_4 =  addrLineElement.getValue().trim();
												if(addrLine_4.contains(","))
												{
													addrLine_4 = addrLine_4.replaceAll(",", "|");
												}
												if(addrLine_4Arr.equals(""))
												{
													addrLine_4Arr = addrLine_4;
												}
												else
												{
													addrLine_4Arr = addrLine_4Arr + "~" + addrLine_4;
												}
											 }
											 else if(addrLineMap.get("wd:Type").equals("ADDRESS_LINE_5"))
											 {
												addrLine_5 =  addrLineElement.getValue().trim();
												if(addrLine_5.contains(","))
												{
													addrLine_5 = addrLine_5.replaceAll(",", "|");
												}
												if(addrLine_5Arr.equals(""))
												{
													addrLine_5Arr = addrLine_5;
												}
												else
												{
													addrLine_5Arr = addrLine_5Arr + "~" + addrLine_5;
												}
											 }
											 else if(addrLineMap.get("wd:Type").equals("ADDRESS_LINE_6"))
											 {
												addrLine_6 =  addrLineElement.getValue().trim();
												if(addrLine_6.contains(","))
												{
													addrLine_6 = addrLine_6.replaceAll(",", "|");
												}
												if(addrLine_6Arr.equals(""))
												{
													addrLine_6Arr = addrLine_6;
												}
												else
												{
													addrLine_6Arr = addrLine_6Arr + "~" + addrLine_6;
												}
											 }
											 else if(addrLineMap.get("wd:Type").equals("ADDRESS_LINE_7"))
											 {
												addrLine_7 =  addrLineElement.getValue().trim();
												if(addrLine_7.contains(","))
												{
													addrLine_7 = addrLine_7.replaceAll(",", "|");
												}
												if(addrLine_7Arr.equals(""))
												{
													addrLine_7Arr = addrLine_7;
												}
												else
												{
													addrLine_7Arr = addrLine_7Arr + "~" + addrLine_7;
												}
											 }
											 else if(addrLineMap.get("wd:Type").equals("ADDRESS_LINE_8"))
											 {
												addrLine_8 =  addrLineElement.getValue().trim();
												if(addrLine_8.contains(","))
												{
													addrLine_8 = addrLine_8.replaceAll(",", "|");
												}
												if(addrLine_8Arr.equals(""))
												{
													addrLine_8Arr = addrLine_8;
												}
												else
												{
													addrLine_8Arr = addrLine_8Arr + "~" + addrLine_8;
												}
											 }
											 else if(addrLineMap.get("wd:Type").equals("ADDRESS_LINE_9"))
											 {
												addrLine_9 =  addrLineElement.getValue().trim();
												if(addrLine_9.contains(","))
												{
													addrLine_9 = addrLine_9.replaceAll(",", "|");
												}
												if(addrLine_9Arr.equals(""))
												{
													addrLine_9Arr = addrLine_9;
												}
												else
												{
													addrLine_9Arr = addrLine_9Arr + "~" + addrLine_9;
												}
											 }
											 else if(addrLineMap.get("wd:Type").equals("ADDRESS_LINE_1_LOCAL"))
											 {
												 addrLine_1_Local =  addrLineElement.getValue().trim();
												 if(addrLine_1_Local.contains(","))
												 {
													 addrLine_1_Local = addrLine_1_Local.replaceAll(",", "|");
												 }
												 if(addrLine_1_LocalArr.equals(""))
												 {
													 addrLine_1_LocalArr = addrLine_1_Local;
												 }
												 else
												 {
													 addrLine_1_LocalArr = addrLine_1_LocalArr + "~" + addrLine_1_Local;
												 }
											 }
											 else if(addrLineMap.get("wd:Type").equals("ADDRESS_LINE_2_LOCAL"))
											 {
												 addrLine_2_Local =  addrLineElement.getValue().trim();
												 if(addrLine_2_Local.contains(","))
												 {
													 addrLine_2_Local = addrLine_2_Local.replaceAll(",", "|");
												 }
												 if(addrLine_2_LocalArr.equals(""))
												 {
													 addrLine_2_LocalArr = addrLine_2_Local;
												 }
												 else
												 {
													 addrLine_2_LocalArr = addrLine_2_LocalArr + "~" + addrLine_2_Local;
												 }
											 }
										 }
									 }
									 
									 ReportElement element3 = addressData.getChild("wd:Country_Reference");
									 if(element3 != null)
									 {
										 List<ReportElement> isoData = element3.getChildren("wd:ID");								 
										 for(ReportElement isoElement:isoData)
										 {
											 isoMap = isoElement.getAllAttributes();
											 if(isoMap.get("wd:type").equals("ISO_3166-1_Alpha-3_Code"))
											 {
												 countryISOCode = isoElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 countryISOCode = "";
									 }
									 if(countryISOCodeArr.equals(""))
									 {
										 countryISOCodeArr = countryISOCode;
									 }
									 else
									 {
										 countryISOCodeArr = countryISOCodeArr + "~" + countryISOCode;
									 }
									 
									 city = addressData.getChild("wd:Municipality") != null?addressData.getChild("wd:Municipality").getValue().trim():"";
									 if(cityArr.equals(""))
									 {
										 cityArr = city;
									 }
									 else
									 {
										 cityArr = cityArr + "~" + city;
									 }
									 
									 List<ReportElement> subCityData = addressData.getChildren("wd:Submunicipality_Data");
									 if(subCityData != null)
									 {
										 for(ReportElement subCityElement:subCityData)
										 {
											 subCityMap = subCityElement.getAllAttributes();
											 if(subCityMap.get("wd:Type").equals("CITY_SUBDIVISION_1"))
											 {
												 subCity1 =  subCityElement.getValue().trim();
												 if(subCity1Arr.equals(""))
												 {
													 subCity1Arr = subCity1;
												 }
												 else
												 {
													 subCity1Arr = subCity1Arr + "~" + subCity1;
												 }
											 }
											 else if(subCityMap.get("wd:Type").equals("CITY_SUBDIVISION_2"))
											 {
												 subCity2 =  subCityElement.getValue().trim();
												 if(subCity2Arr.equals(""))
												 {
													 subCity2Arr = subCity2;
												 }
												 else
												 {
													 subCity2Arr = subCity2Arr + "~" + subCity2;
												 }
											 }
										 }
									 }
									 
									 region = addressData.getChild("wd:Country_Region_Descriptor") != null?addressData.getChild("wd:Country_Region_Descriptor").getValue().trim():"";
									 if(regionArr.equals(""))
									 {
										 regionArr = region;
									 }
									 else
									 {
										 regionArr = regionArr + "~" + region;
									 }
									 
									 List<ReportElement> subRegionData = addressData.getChildren("wd:Subregion_Data");
									 if(subRegionData != null)
									 {
										 for(ReportElement subRegionElement:subRegionData)
										 {
											 subRegionMap = subRegionElement.getAllAttributes();
											 if(subRegionMap.get("wd:Type").equals("REGION_SUBDIVISION_1"))
											 {
												subRegion1 =  subRegionElement.getValue().trim();
												if(subRegion1Arr.equals(""))
												{
													subRegion1Arr = subRegion1;
												}
												else
												{
													subRegion1Arr = subRegion1Arr + "~" + subRegion1;
												}
											 }
											 else if(subRegionMap.get("wd:Type").equals("REGION_SUBDIVISION_2"))
											 {
												 subRegion2 =  subRegionElement.getValue().trim();
												 if(subRegion2Arr.equals(""))
												 {
													subRegion2Arr = subRegion2;
												 }
												 else
												 {
													subRegion2Arr = subRegion2Arr + "~" + subRegion2;
												 }
											 }
										 }
									 }
									 
									 postalCode = addressData.getChild("wd:Postal_Code") != null?addressData.getChild("wd:Postal_Code").getValue().trim():"";
									 if(postalCodeArr.equals(""))
									 {
										 postalCodeArr = postalCode;
									 }
									 else
									 {
										 postalCodeArr = postalCodeArr + "~" + postalCode;
									 }
									 
									 ReportElement element4 = addressData.getChild("wd:Usage_Data");
									 if(element4 != null)
									 {
										 ReportElement element5 = element4.getChild("wd:Type_Data");
										 if(element5 != null)
										 {
											 ReportElement element6 = element5.getChild("wd:Type_Reference");
											 if(element6 !=null)
											 {
												 List<ReportElement> usageData = element6.getChildren("wd:ID");					 
												 for(ReportElement wdElement:usageData)
												 {
													 usageMap = wdElement.getAllAttributes();
													 if(usageMap.get("wd:type").equals("Communication_Usage_Type_ID"))
													 {
														 usageType = wdElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 usageType = ""; 
											 }
										 }
									 }
									 else
									 {
										 usageType = "";
									 }
									 if(usageTypeArr.equals(""))
									 {
										 usageTypeArr = usageType;
									 }
									 else
									 {
										 usageTypeArr = usageTypeArr + "~" + usageType;
									 }
								 }
							 }
							 else
							 {
								 effectiveDateArr = "";
								 countryISOCodeArr = "";
								 addrLine_1Arr = "";
								 addrLine_2Arr = "";
								 addrLine_3Arr = "";
								 addrLine_4Arr = "";
								 addrLine_5Arr = "";
								 addrLine_6Arr = "";
								 addrLine_7Arr = "";
								 addrLine_8Arr = "";
								 addrLine_9Arr = "";
								 addrLine_1_LocalArr = "";
							     addrLine_2_LocalArr = "";
							     cityArr = "";
							     subCity1Arr = "";
							     subCity2Arr = "";
							     regionArr = "";
							     subRegion1Arr = "";
							     subRegion2Arr = "";
							     postalCodeArr = "";
								 usageTypeArr = "";
							 }
							 
							 List<ReportElement> emailList = reportElement.getChild("wd:Worker_Data")
							 			.getChild("wd:Personal_Data")
							 			.getChild("wd:Contact_Data")
					 					.getChildren("wd:Email_Address_Data");
							 
							 if(emailList != null && emailList.size() >0)
							 {
								 emailArr = "";
								 emailUsageTypeArr = "";
								 visibilityEmailArr = "";
								 primaryEmailArr = "";
								 
								 for(ReportElement emailElement:emailList)
								 {
									 email = emailElement.getChild("wd:Email_Address") != null?emailElement.getChild("wd:Email_Address").getValue().trim():"";
									 email = email.substring(0, email.indexOf("@"));
									 if(emailArr.equals(""))
									 {
										 emailArr = email;
									 }
									 else
									 {
										 emailArr = emailArr + "~" + email;
									 }
									 ReportElement emailUsageData = emailElement.getChild("wd:Usage_Data");
									 if(emailUsageData != null)
									 {
										 visibilityEmailMap = emailUsageData.getAllAttributes();
										 if(!visibilityEmailMap.get("wd:Public").equals("1"))
										 {
											 visibilityEmail = "Y";
										 }
										 else
										 {
											 visibilityEmail = "N";
										 }
										 if(visibilityEmailArr.equals(""))
										 {
											 visibilityEmailArr = visibilityEmail;
										 }
										 else
										 {
											 visibilityEmailArr = visibilityEmailArr + "~" + visibilityEmail;
										 }
										 ReportElement emailTypeData = emailUsageData.getChild("wd:Type_Data");
										 if(emailTypeData !=null)
										 {
											 primaryEmailMap = emailTypeData.getAllAttributes();
											 if(primaryEmailMap.get("wd:Primary").equals("1"))
											 {
												 primaryEmail = "PRIMARY";
											 }
											 else
											 {
												 primaryEmail = "";
											 }
											 if(primaryEmailArr.equals(""))
											 {
												 primaryEmailArr = primaryEmail;
											 }
											 else
											 {
												 primaryEmailArr = primaryEmailArr + "~" + primaryEmail;
											 }
											 ReportElement emailTypeRef = emailTypeData.getChild("wd:Type_Reference");
											 if(emailTypeRef != null)
											 {
												 List<ReportElement> emailUsageDataList = emailTypeRef.getChildren("wd:ID");					 
												 for(ReportElement wdElement:emailUsageDataList)
												 {
													 emailUsageMap = wdElement.getAllAttributes();
													 if(emailUsageMap.get("wd:type").equals("Communication_Usage_Type_ID"))
													 {
														 emailUsageType = wdElement.getValue().trim();														
													 }
												 }
											 }											 
										 }
										 else
										 {
											 emailUsageType = "";
										 }												 
									 }
									 else
									 {
										 emailUsageType = ""; 
									 }
									 if(emailUsageTypeArr.equals(""))
									 {
										 emailUsageTypeArr = emailUsageType;
									 }
									 else
									 {
										 emailUsageTypeArr = emailUsageTypeArr + "~" + emailUsageType;
									 }
								 }
							 }
							 else
							 {
								 emailArr = "";
								 emailUsageTypeArr = "";
								 visibilityEmailArr = "";
								 primaryEmailArr = "";
							 }
							 List<ReportElement> phoneList = reportElement.getChild("wd:Worker_Data")
							 			.getChild("wd:Personal_Data")
							 			.getChild("wd:Contact_Data")
					 					.getChildren("wd:Phone_Data");						
							 
								if(phoneList != null && phoneList.size() >0)
								{
									countryPhCodeArr= "";
									areaCodeArr = "";
									phoneNumberArr = "";
									phCountryISOCodeArr = "";
									phoneDeviceTypeArr = "";
									usageTypePhoneArr = "";
									primaryPhoneArr = "";
									
									for(ReportElement phoneElement : phoneList)
									{
										 countryPhCode = phoneElement.getChild("wd:International_Phone_Code") != null?phoneElement.getChild("wd:International_Phone_Code").getValue().trim():"";
										 if(countryPhCodeArr.equals(""))
										 {
											 countryPhCodeArr = countryPhCode;
										 }
										 else
										 {
											 if(!countryPhCode.equals(""))
											 {
												 countryPhCodeArr = countryPhCodeArr + "~" + countryPhCode;
											 }
										 }
										 areaCode = phoneElement.getChild("wd:Area_Code") != null?phoneElement.getChild("wd:Area_Code").getValue().trim():"";
										 if(areaCodeArr.equals(""))
										 {
											 areaCodeArr = areaCode;
										 }
										 else
										 {
											 if(!areaCode.equals(""))
											 {
												 areaCodeArr = areaCodeArr + "~" + areaCode;
											 }
										 }
										 phoneNumber = phoneElement.getChild("wd:Phone_Number") != null?phoneElement.getChild("wd:Phone_Number").getValue().trim():""; 
										 if(phoneNumberArr.equals(""))
										 {
											 phoneNumberArr = phoneNumber;
										 }
										 else
										 {
											 phoneNumberArr = phoneNumberArr + "~" + phoneNumber;
										 }
										 
										 phCountryISOCode = phoneElement.getChild("wd:Country_ISO_Code") != null?phoneElement.getChild("wd:Country_ISO_Code").getValue().trim():""; 
										 if(phCountryISOCodeArr.equals(""))
										 {
											 phCountryISOCodeArr = phCountryISOCode;
										 }
										 else
										 {
											 if(!phCountryISOCode.equals(""))
											 {
												 phCountryISOCodeArr = phCountryISOCodeArr + "~" + phCountryISOCode;
											 }
										 }
										 
										 ReportElement phDeviceTypeData = phoneElement.getChild("wd:Phone_Device_Type_Reference");
										 if(phDeviceTypeData != null)
										 {
											 List<ReportElement> phDeviceData = phDeviceTypeData.getChildren("wd:ID");								 
											 for(ReportElement phDeviceElement:phDeviceData)
											 {
												 phDeviceMap = phDeviceElement.getAllAttributes();
												 if(phDeviceMap.get("wd:type").equals("Phone_Device_Type_ID"))
												 {
													 phoneDeviceType = phDeviceElement.getValue().trim();
												 }
											 }
										 }
										 else
										 {
											 phoneDeviceType = "";
										 }
										 
										 if(phoneDeviceTypeArr.equals(""))
										 {
											 phoneDeviceTypeArr = phoneDeviceType;
										 }
										 else
										 {
											 phoneDeviceTypeArr = phoneDeviceTypeArr + "~" + phoneDeviceType;
										 }
										 
										 ReportElement phUsageData = phoneElement.getChild("wd:Usage_Data");
										 if(phUsageData != null)
										 {
											 ReportElement phTypeData = phUsageData.getChild("wd:Type_Data");
											 if(phTypeData != null)
											 {
												 primaryPhoneMap = phTypeData.getAllAttributes();
												 if(primaryPhoneMap.get("wd:Primary").equals("1"))
												 {
													 primaryPhone = "PRIMARY";
												 }
												 else
												 {
													 primaryPhone = "";
												 }
												 if(primaryPhoneArr.equals(""))
												 {
													 primaryPhoneArr = primaryPhone;
												 }
												 else
												 {
													 primaryPhoneArr = primaryPhoneArr + "~" + primaryPhone;
												 }
												 ReportElement phTypeRef = phTypeData.getChild("wd:Type_Reference");
												 if(phTypeRef !=null)
												 {
													 List<ReportElement> phUsageList = phTypeRef.getChildren("wd:ID");					 
													 for(ReportElement wdElement:phUsageList)
													 {
														 phUsageMap = wdElement.getAllAttributes();
														 if(phUsageMap.get("wd:type").equals("Communication_Usage_Type_ID"))
														 {
															 usageTypePhone = wdElement.getValue().trim();
														 }
													 }
												 }
												 else
												 {
													 usageTypePhone = ""; 
												 }
											 }
										 }
										 else
										 {
											 usageTypePhone = "";
										 }
										 
										 if(usageTypePhoneArr.equals(""))
										 {
											 usageTypePhoneArr = usageTypePhone;
										 }
										 else
										 {
											 usageTypePhoneArr = usageTypePhoneArr + "~" + usageTypePhone;
										 }
									}		
				        	 }
							 else
							 {
									countryPhCodeArr= "";
									areaCodeArr = "";
									phoneNumberArr = "";
									phCountryISOCodeArr = "";
									phoneDeviceTypeArr = "";
									usageTypePhoneArr = "";
									primaryPhoneArr = "";
							 }
							 
							 headingFromWD = "Worker_ID,Country_ISO_Code,Effective_Date,ADDRESS_LINE_1,ADDRESS_LINE_2,ADDRESS_LINE_3,ADDRESS_LINE_4,ADDRESS_LINE_5,ADDRESS_LINE_6,ADDRESS_LINE_7,"
							 		+ "ADDRESS_LINE_8,ADDRESS_LINE_9,ADDRESS_LINE_1-LOCAL,ADDRESS_LINE_2-LOCAL,City,CITY_SUBDIVISION_1,CITY_SUBDIVISION_2,Region,REGION_SUBDIVISION_1,"
							 		+ "REGION_SUBDIVISION_2,Postal_Code,Usage_Type_Address,Email_Address,Primary_Email,Visibility_Email,Usage_Type_Email,Country_Phone_Code,Area_Code,Phone_Number,"
							 		+ "Phone_Country_ISO_Code,Phone_Device_Type,Usage_Type_Phone,Primary_Phone";
							 
							 headerStr = workerId + "," + countryISOCodeArr + "," + effectiveDateArr + "," + addrLine_1Arr + "," + addrLine_2Arr + "," + addrLine_3Arr + "," + addrLine_4Arr + "," + addrLine_5Arr + "," +
									 		addrLine_6Arr + "," + addrLine_7Arr + "," + addrLine_8Arr + "," + addrLine_9Arr + "," + addrLine_1_LocalArr + "," + addrLine_2_LocalArr + "," + cityArr + "," + subCity1Arr + "," +
									 		subCity2Arr + "," + regionArr + "," + subRegion1Arr + "," + subRegion2Arr + "," + postalCodeArr + "," + usageTypeArr + "," + emailArr + "," + primaryEmailArr + "," +
									 		visibilityEmailArr + "," + emailUsageTypeArr + "," + countryPhCodeArr + "," + areaCodeArr + "," + phoneNumberArr + "," +phCountryISOCodeArr + "," + phoneDeviceTypeArr + "," + 
									 		usageTypePhoneArr + "," + primaryPhoneArr;
							 
							 if(finalStr.equals(""))
							 {
								 finalStr = headingFromWD + "\n" + headerStr;
							 }
							 else
							 {
								 finalStr = finalStr + "\n" + headerStr;
							 }
						 }
					 }
					 System.out.println(finalStr);
					 
					 /*String wdCSVfile = "config/" + loadCycle + "_" + ruleName + ".csv";			 
					 PrintWriter writer = new PrintWriter(new File(wdCSVfile));
					 writer.write(finalStr.toString());
					 writer.flush();
					 writer.close();*/
					 
					 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
					 PrintWriter writer = new PrintWriter(wdCSVfile);
					 writer.write(finalStr.toString());
					 writer.flush();
					 writer.close();
					 
					 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
					 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
					 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
					 postLoadService.updatePostLoad(postLoad);
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Worker_ID");
				 }
				 /*else
				 {
					 headingWd = new JSONArray();
					 JSONObject obj = new JSONObject();
					 obj.put("errorMsg", faultStr.substring(faultStr.indexOf("."), faultStr.length()));
					 headingWd.put(obj);
				 }*/
			 }

		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return headingWd;
	}

	private JSONArray createCSVFromWDTermination(Tenant tenant2, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		targetContent = null;
		headingFromWD = "";
		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_TERMINATION_REQUEST_FILE = requestfile.getAbsolutePath();
				 /*if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addWorkerIdListToFindError(GET_TERMINATION_REQUEST_FILE, columnList.get(i), ruleName, "Employee_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement reportElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = reportElement.getChild("env:Body")
										.getChild("wd:Get_Workers_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
						 }
					 }
					 columnList.removeAll(errorList);
				 }*/
				 //String outputfile = GET_TERMINATION_REQUEST_FILE;
				 String outputfile = addHireIdList(GET_TERMINATION_REQUEST_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");

				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");			 
				 
				 //if(faultStr == null)
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String finalStr = "";
					 String headerStr = "";
					 String empId = "";
					 String terminationDate = "";
					 String lastDayOfWork = "";
					 String payThroughDate = "";
					 String resignationDate = "";
					 String notificationDate = "";
					 String lastPaidDate = "";
					 String expectedReturnDate = "";
					 String regretTermination = "";
					 String notReturning = "";
					 String returnUnknown = "";
					 String primaryReason = "";
					 String secondaryReason = "";
					 String localReason = "";
					 String elegibleForHire = "";
					 
					 Map<String,String> idMap = null;
					 Map<String,String> primaryReasonMap = null;
					 Map<String,String> secReasonMap = null;
					 Map<String,String> localReasonMap = null;
					 Map<String,String> rehireMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {					 
						 if(j == 1)
						 {
							startIndex = 0;
							if(999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*999;
							}
						 }
						 else
						 {
							startIndex = endIndex;
							if(j*999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = (j*999);
							}
						 }
						 outputfile = addHireIdList(GET_TERMINATION_REQUEST_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
						 is = new FileInputStream(outputfile);
					     soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     soapPart = soapMessage.getSOAPPart();
					     envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
				         out = new ByteArrayOutputStream();
				         soapMessage.writeTo(out);
				         strMsg = new String(out.toByteArray());
				        
				         soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
				         soapResponse = soapConnection.call(soapMessage, sourceUrl);
				         out = new ByteArrayOutputStream();
				         soapResponse.writeTo(out);
				         strMsg = new String(out.toByteArray(), "utf-8");
				        
				         soapResp = XmlParserManager.parseXml(strMsg);
					 
						 ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Workers_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> workerData = responseData.getChildren("wd:Worker");
							
						 for(ReportElement reportElement : workerData)
						 {
							 ReportElement element1 = reportElement.getChild("wd:Worker_Reference");
							 if(element1 != null)
							 {
								 List<ReportElement> idData = element1.getChildren("wd:ID");					 
								 for(ReportElement wdElement:idData)
								 {
									 idMap = wdElement.getAllAttributes();
									 if(idMap.get("wd:type").equals("Employee_ID"))
									 {
										 empId = wdElement.getValue().trim();
									 }
								 }
							 }
							 
							 ReportElement element2 = reportElement.getChild("wd:Worker_Data")
			 							.getChild("wd:Employment_Data")
			 							.getChild("wd:Worker_Status_Data");
							 
							 if(element2 != null)
							 {
								 terminationDate = element2.getChild("wd:Termination_Date") != null?element2.getChild("wd:Termination_Date").getValue().trim():"";
								 if(!terminationDate.isEmpty())
								 {
									 terminationDate = terminationDate.substring(0, 10);
								 }
								 lastDayOfWork = element2.getChild("wd:Termination_Last_Day_of_Work") != null?element2.getChild("wd:Termination_Last_Day_of_Work").getValue().trim():"";
								 if(!lastDayOfWork.isEmpty())
								 {
									 lastDayOfWork = lastDayOfWork.substring(0, 10);
								 }
								 payThroughDate = element2.getChild("wd:Pay_Through_Date") != null?element2.getChild("wd:Pay_Through_Date").getValue().trim():""; 
								 if(!payThroughDate.isEmpty())
								 {
									 payThroughDate = payThroughDate.substring(0, 10);
								 }
								 resignationDate = element2.getChild("wd:Resignation_Date") != null?element2.getChild("wd:Resignation_Date").getValue().trim():"";
								 if(!resignationDate.isEmpty())
								 {
									 resignationDate = resignationDate.substring(0, 10);
								 }
								 notificationDate = resignationDate;
								 lastPaidDate = element2.getChild("wd:Last_Date_for_Which_Paid") != null?element2.getChild("wd:Last_Date_for_Which_Paid").getValue().trim():"";
								 if(!lastPaidDate.isEmpty())
								 {
									 lastPaidDate = lastPaidDate.substring(0, 10);
									 //lastPaidDate = convertDate(lastPaidDate, "yyyy-MM-dd", "dd-MM-yyyy");
								 }
								 expectedReturnDate = element2.getChild("wd:Expected_Date_of_Return") != null?element2.getChild("wd:Expected_Date_of_Return").getValue().trim():"";
								 regretTermination = element2.getChild("wd:Regrettable_Termination") != null?element2.getChild("wd:Regrettable_Termination").getValue().trim():"";
								 if(regretTermination.equalsIgnoreCase("1"))
								 {
									 regretTermination = "Yes";
								 }
								 else if(regretTermination.equalsIgnoreCase("0"))
								 {
									 regretTermination = "No";
								 }
								 notReturning = element2.getChild("wd:Not_Returning") != null?element2.getChild("wd:Not_Returning").getValue().trim():"";
								 if(notReturning.equalsIgnoreCase("1"))
								 {
									 notReturning = "true";
								 }
								 else if(notReturning.equalsIgnoreCase("0"))
								 {
									 notReturning = "false";
								 }
								 returnUnknown = element2.getChild("wd:Return_Unknown") != null?element2.getChild("wd:Return_Unknown").getValue().trim():"";
								 if(returnUnknown.equalsIgnoreCase("1"))
								 {
									 returnUnknown = "Y";
								 }
								 else if(returnUnknown.equalsIgnoreCase("0"))
								 {
									 returnUnknown = "N";
								 }
								 
								 ReportElement element3 = element2.getChild("wd:Primary_Termination_Reason_Reference");
								 if(element3 != null)
								 {
									 List<ReportElement> priReasonData = element3.getChildren("wd:ID");					 
									 for(ReportElement priReasonElement:priReasonData)
									 {
										 primaryReasonMap = priReasonElement.getAllAttributes();
										 if(primaryReasonMap.get("wd:type").equals("Event_Classification_Subcategory_ID") || primaryReasonMap.get("wd:type").equals("Termination_Subcategory_ID"))
										 {
											 primaryReason = priReasonElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 primaryReason = "";
								 }
								 
								 ReportElement element4 = element2.getChild("wd:Secondary_Termination_Reasons_Data");
								 if(element4 != null)
								 {
									 ReportElement element5 = element4.getChild("wd:Secondary_Termination_Reason_Reference");
									 if(element5 != null)
									 {
										 List<ReportElement> secReasonData = element5.getChildren("wd:ID");					 
										 for(ReportElement secReasonElement:secReasonData)
										 {
											 secReasonMap = secReasonElement.getAllAttributes();
											 if(secReasonMap.get("wd:type").equals("Event_Classification_Subcategory_ID") || secReasonMap.get("wd:type").equals("Termination_Subcategory_ID"))
											 {
												 secondaryReason = secReasonElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 secondaryReason = "";
									 }
								 }
								 else
								 {
									 secondaryReason = "";
								 }
								 
								 ReportElement element6 = element2.getChild("wd:Local_Termination_Reason_Reference");
								 if(element6 != null)
								 {
									 List<ReportElement> localReasonData = element6.getChildren("wd:ID");					 
									 for(ReportElement localReasonElement:localReasonData)
									 {
										 localReasonMap = localReasonElement.getAllAttributes();
										 if(localReasonMap.get("wd:type").equals("Local_Termination_Reason_ID"))
										 {
											 localReason = localReasonElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 localReason = "";
								 }
								 
								 ReportElement element7 = element2.getChild("wd:Eligible_for_Hire_Reference");
								 if(element7 != null)
								 {
									 List<ReportElement> rehireData = element7.getChildren("wd:ID");					 
									 for(ReportElement rehireElement:rehireData)
									 {
										 rehireMap = rehireElement.getAllAttributes();
										 if(rehireMap.get("wd:type").equals("Yes_No_Type_ID"))
										 {
											 elegibleForHire = rehireElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 elegibleForHire = "";
								 }
								 
								 if(elegibleForHire.isEmpty())
								 {
									 elegibleForHire = element2.getChild("wd:Not_Eligible_for_Hire") != null?element2.getChild("wd:Not_Eligible_for_Hire").getValue().trim():"";
									 if(elegibleForHire.equalsIgnoreCase("0"))
									 {
										 elegibleForHire = "Yes";
									 }
									 else if(elegibleForHire.equalsIgnoreCase("1"))
									 {
										 elegibleForHire = "No";
									 }
								 }
							 }
							 else
							 {
								 terminationDate = "";
								 lastDayOfWork = "";
								 payThroughDate = "";
								 resignationDate = "";
								 notificationDate = "";
								 lastPaidDate = "";
								 expectedReturnDate = "";
								 regretTermination = "";
								 notReturning = "";
								 returnUnknown = "";
								 primaryReason = "";
								 secondaryReason = "";
								 localReason = "";
								 elegibleForHire = "";
							 }
							 
							 headingFromWD = "Employee_ID,Termination_Date,Last_Day_of_Work,Primary_Reason,Secondary_Reason,Local_Termination_Reason,Pay_Through_Date,Resignation_Date,Notification_Date,"
							 		         + "Regrettable,Last_Date_for_Which_Paid,Expected_Date_of_Return,Not_Returning,Return_Unknown,Elegible_For_Hire";
								 
							 headerStr = empId + "," + terminationDate + "," + lastDayOfWork + "," + primaryReason + "," + secondaryReason + "," + localReason + "," + payThroughDate + "," + 
									 resignationDate + "," + notificationDate + "," + regretTermination + "," + lastPaidDate + "," + expectedReturnDate + "," + notReturning + "," + returnUnknown + "," + elegibleForHire;
							 
							 if(finalStr.equals(""))
							 {
								 finalStr = headingFromWD + "\n" + headerStr;
							 }
							 else
							 {
								 finalStr = finalStr + "\n" + headerStr;
							 }					 
						 }
					 }
					 
					 System.out.println(finalStr);
					 targetContent = finalStr.toString().getBytes();
					 
					 /*File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
					 PrintWriter writer = new PrintWriter(wdCSVfile);
					 writer.write(finalStr.toString());
					 writer.flush();
					 writer.close();
					 
					 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
					 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
					 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
					 postLoadService.updatePostLoad(postLoad);*/
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
					 
					 complete = true;
				 }
			 }
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}
	
	@RequestMapping(value = "/postloadIsAllComplete", method = RequestMethod.GET, headers = "Accept=application/json")
	private boolean isCompleted()
	{
		return complete;
	}
	
	@RequestMapping(value = "/getMapResponse", method = RequestMethod.GET, headers = "Accept=application/json")
	private JSONArray getMapResponse()
	{
		return headingWd;
	}	

	private JSONArray createCSVFromWDHireCW(Tenant tenant, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		String checkFile = null;
		//String faultStr = null;
		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_HIRE_CW_REQUEST_FILE = requestfile.getAbsolutePath();
				 //String outputfile = GET_HIRE_CW_REQUEST_FILE;
				 if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addWorkerIdListToFindError(GET_HIRE_CW_REQUEST_FILE, columnList.get(i), ruleName, "Contingent_Worker_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement reportElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = reportElement.getChild("env:Body")
										.getChild("wd:Get_Workers_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
						 }
					 }
					 columnList.removeAll(errorList);
				 }
				 String outputfile = addHireIdList(GET_HIRE_CW_REQUEST_FILE, columnList, ruleName, startIndex, columnList.size(), "Contingent_Worker_ID");

				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");			 
				 /*if(strMsg.contains("faultstring"))
				 {
					 ReportElement msgResponse = XmlParserManager.parseXml(strMsg);
					 ReportElement elementData = msgResponse.getChild("SOAP-ENV:Body")
								.getChild("SOAP-ENV:Fault");
					 faultStr = elementData.getChild("faultstring").getValue().trim();
					 System.out.println("faultStr-"+faultStr);
				 }*/
				 
				// if(faultStr == null)
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String finalStr = "";
					 String headerStr = "";
					 String contingentWorkerId = "";
					 String positionId = "";
					 String positionTitle = "";
					 String businessTitle = "";
					 String contingentWorkerName = "";
					 String timeType = "";
					 String defaultWeeklyHours = "";
					 String scheduledWeeklyHour = "";
					 String payTypeName = "";
					 String jobCode = "";
					 String location = "";
					 String supplierName = "";
					 String contractStartDate = "";
					 String contractEndDate = "";
					 String supOrg = "";
					 String costCenter = "";
					 String company = "";
					 String region = "";
					 
					 Map<String,String> idMap = null;
					 Map<String,String> cwTypeMap = null;
					 Map<String,String> posTimeTypeMap = null;
					 Map<String,String> payRateMap = null;
					 Map<String,String> jobProfMap = null;
					 Map<String,String> supplierMap = null;
					 Map<String,String> orgMap = null;
	
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {					 
						 if(j == 1)
						 {
							startIndex = 0;
							if(1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*1000;
							}
						 }
						 else
						 {
							int lastVal = (j - 1);
							startIndex = (endIndex - lastVal);
							if(j*1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = (j*startIndex) + 1;
							}
						 }
						 outputfile = addHireIdList(GET_HIRE_CW_REQUEST_FILE, columnList, ruleName, startIndex, endIndex, "Contingent_Worker_ID");
						 is = new FileInputStream(outputfile);
					     soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     soapPart = soapMessage.getSOAPPart();
					     envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
				         out = new ByteArrayOutputStream();
				         soapMessage.writeTo(out);
				         strMsg = new String(out.toByteArray());
				        
				         soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
				         soapResponse = soapConnection.call(soapMessage, sourceUrl);
				         out = new ByteArrayOutputStream();
				         soapResponse.writeTo(out);
				         strMsg = new String(out.toByteArray(), "utf-8");
				        
				         soapResp = XmlParserManager.parseXml(strMsg);
					 
						 ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Workers_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> workerData = responseData.getChildren("wd:Worker");
							
						 for(ReportElement reportElement : workerData)
						 {
							 ReportElement element1 = reportElement.getChild("wd:Worker_Reference");
							 if(element1 != null)
							 {
								 List<ReportElement> idData = element1.getChildren("wd:ID");					 
								 for(ReportElement wdElement:idData)
								 {
									 idMap = wdElement.getAllAttributes();
									 if(idMap.get("wd:type").equals("Contingent_Worker_ID"))
									 {
										 contingentWorkerId = wdElement.getValue().trim();
									 }
								 }
							 }
							 
							 ReportElement element2 = reportElement.getChild("wd:Worker_Data")
							 							.getChild("wd:Employment_Data")
							 							.getChild("wd:Worker_Job_Data");
							 if(element2 != null)
							 {
								 ReportElement element3 = element2.getChild("wd:Position_Data");
								 if(element3 != null)
								 {
									 positionId = element3.getChild("wd:Position_ID") != null?element3.getChild("wd:Position_ID").getValue().trim():"";
									 positionTitle = element3.getChild("wd:Position_Title") != null?element3.getChild("wd:Position_Title").getValue().trim():"";
									 if(positionTitle.contains(","))
									 {
										 positionTitle = positionTitle.replaceAll(",", "|");
									 }
									 businessTitle = element3.getChild("wd:Business_Title") != null?element3.getChild("wd:Business_Title").getValue().trim():"";
									 if(businessTitle.contains(","))
									 {
										 businessTitle = businessTitle.replaceAll(",", "|");
									 }
									 defaultWeeklyHours = element3.getChild("wd:Default_Weekly_Hours") != null?element3.getChild("wd:Default_Weekly_Hours").getValue().trim():"";
									 scheduledWeeklyHour = element3.getChild("wd:Scheduled_Weekly_Hours") != null?element3.getChild("wd:Scheduled_Weekly_Hours").getValue().trim():"";
									 contractStartDate = element3.getChild("wd:Start_Date") != null?element3.getChild("wd:Start_Date").getValue().trim():"";
									 
									 ReportElement element4 = element3.getChild("wd:Worker_Type_Reference");
									 if(element4 != null)
									 {
										 List<ReportElement> cwTypeData = element4.getChildren("wd:ID");					 
										 for(ReportElement cwTypeElement:cwTypeData)
										 {
											 cwTypeMap = cwTypeElement.getAllAttributes();
											 if(cwTypeMap.get("wd:type").equals("Contingent_Worker_Type_ID"))
											 {
												 contingentWorkerName = cwTypeElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 contingentWorkerName = "";
									 }
									 
									 ReportElement element5 = element3.getChild("wd:Position_Time_Type_Reference");
									 if(element5 != null)
									 {
										 List<ReportElement> timeData = element5.getChildren("wd:ID");					 
										 for(ReportElement timeElement:timeData)
										 {
											 posTimeTypeMap = timeElement.getAllAttributes();
											 if(posTimeTypeMap.get("wd:type").equals("Position_Time_Type_ID"))
											 {
												 timeType = timeElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 timeType = "";
									 }
									 
									 ReportElement element6 = element3.getChild("wd:Pay_Rate_Type_Reference");
									 if(element6 != null)
									 {
										 List<ReportElement> payData = element6.getChildren("wd:ID");					 
										 for(ReportElement payElement:payData)
										 {
											 payRateMap = payElement.getAllAttributes();
											 if(payRateMap.get("wd:type").equals("Pay_Rate_Type_ID"))
											 {
												 payTypeName = payElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 payTypeName = "";
									 }
									 
									 ReportElement element7 = element3.getChild("wd:Job_Profile_Summary_Data");
									 if(element7 != null)
									 {
										 ReportElement element8 = element7.getChild("wd:Job_Profile_Reference");
										 if(element8 != null)
										 {
											 List<ReportElement> jobProfData = element8.getChildren("wd:ID");					 
											 for(ReportElement jobProfElement:jobProfData)
											 {
												 jobProfMap = jobProfElement.getAllAttributes();
												 if(jobProfMap.get("wd:type").equals("Job_Profile_ID"))
												 {
													 jobCode = jobProfElement.getValue().trim();
												 }
											 }
										 }
										 else
										 {
											 jobCode = "";
										 }
									 }
									 else
									 {
										 jobCode = "";
									 }
									 
									 ReportElement element9 = element3.getChild("wd:Business_Site_Summary_Data");
									 if(element9 != null)
									 {
										 location = element9.getChild("wd:Name") != null?element9.getChild("wd:Name").getValue().trim():"";
									 }
									 else
									 {
										 location = ""; 
									 }
								 }
								 else
								 {
									 positionId = "";
									 positionTitle = "";
									 businessTitle = "";
									 contingentWorkerName = "";
									 timeType = "";
									 defaultWeeklyHours = "";
									 scheduledWeeklyHour = "";
									 payTypeName = "";
									 jobCode = "";
									 location = "";
									 contractStartDate = "";
								 }
							 }
							 else
							 {
								 positionId = "";
								 positionTitle = "";
								 businessTitle = "";
								 contingentWorkerName = "";
								 timeType = "";
								 defaultWeeklyHours = "";
								 scheduledWeeklyHour = "";
								 payTypeName = "";
								 jobCode = "";
								 location = "";
								 contractStartDate = "";
							 }
							 
							 ReportElement element10 = reportElement.getChild("wd:Worker_Data")
			 							.getChild("wd:Employment_Data")
			 							.getChild("wd:Worker_Contract_Data");
							 
							 if(element10 != null)
							 {
								 contractEndDate = element10.getChild("wd:Contract_End_Date") != null?element10.getChild("wd:Contract_End_Date").getValue().trim():""; 
								 
								 ReportElement element11 = element10.getChild("wd:Supplier_Reference");
								 if(element11 != null)
								 {
									 List<ReportElement> supplierData = element11.getChildren("wd:ID");					 
									 for(ReportElement supplierElement:supplierData)
									 {
										 supplierMap = supplierElement.getAllAttributes();
										 if(supplierMap.get("wd:type").equals("Supplier_ID"))
										 {
											 supplierName = supplierElement.getValue().trim();
										 }
									 }
								 }
								 else
								 {
									 supplierName = "";
								 }
							 }
							 else
							 {
								 supplierName = "";
								 contractEndDate = "";
							 }
							 
							 ReportElement element12 = reportElement.getChild("wd:Worker_Data")
			 							.getChild("wd:Organization_Data");
							 
							 if(element12 != null)
							 {
								 List<ReportElement> orgData = element12.getChildren("wd:Worker_Organization_Data");
								 for(ReportElement orgElement:orgData)
								 {
									 ReportElement orgDataElement = orgElement.getChild("wd:Organization_Reference");
									 List<ReportElement> orgElementData = orgDataElement.getChildren("wd:ID");
									 for(ReportElement orgValElement:orgElementData)
									 {
										 orgMap = orgValElement.getAllAttributes();
										 if(orgMap.get("wd:type").equals("Cost_Center_Reference_ID"))
										 {
											 costCenter = orgValElement.getValue().trim();
										 }
										 else if(orgMap.get("wd:type").equals("Company_Reference_ID"))
										 {
											 company = orgValElement.getValue().trim();
										 }
										 else if(orgMap.get("wd:type").equals("Region_Reference_ID"))
										 {
											 region = orgValElement.getValue().trim();
										 }
										 else if(orgMap.get("wd:type").equals("Organization_Reference_ID") && orgValElement.getValue().trim().contains("SUPERVISORY"))
										 {
											 ReportElement element13 = orgElement.getChild("wd:Organization_Data");
											 supOrg = element13.getChild("wd:Organization_Code") != null?element13.getChild("wd:Organization_Code").getValue().trim():"";
										 }
									 }
								 }
							 }
							 else
							 {
								 supOrg = "";
								 costCenter = "";
								 company = "";
								 region = ""; 
							 }
							 
							 headingFromWD = "Contingent_Worker_ID,Position_ID,Supplier_Name,Contingent_Worker_Type_Name,Contract_Begin_Date,Contract_End_Date,Supervisory_Organization-Position_Management_Org,"
							 		         + "Cost_Center_Organization,Company_Organization,Region_Organization,Job_Code,Position_Title,Business_Title,Location,Time_Type,"
							 		         + "Default_Weekly_Hours,Scheduled_Weekly_Hours,Pay_Type_Name";
								 
							 headerStr = contingentWorkerId + "," + positionId + "," + supplierName + "," + contingentWorkerName + "," + contractStartDate + "," + contractEndDate + "," + 
									 supOrg + "," + costCenter + "," + company + "," + region + "," + jobCode + "," + positionTitle + "," + businessTitle + "," +
									 location + "," + timeType + "," + defaultWeeklyHours + "," + scheduledWeeklyHour + "," + payTypeName;
							 
							 if(finalStr.equals(""))
							 {
								 finalStr = headingFromWD + "\n" + headerStr;
							 }
							 else
							 {
								 finalStr = finalStr + "\n" + headerStr;
							 }					 
						 }
					 }
					 
					 System.out.println(finalStr);
					 
					 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
					 PrintWriter writer = new PrintWriter(wdCSVfile);
					 writer.write(finalStr.toString());
					 writer.flush();
					 writer.close();
					 
					 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
					 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
					 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
					 postLoadService.updatePostLoad(postLoad);
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Contingent_Worker_ID");
					 
					 /*String wdCSVfile1 = "config/" + loadCycle + "_" + ruleName + ".csv";			 
					 PrintWriter writer1 = new PrintWriter(new File(wdCSVfile1));
					 writer1.write(finalStr.toString());
					 writer1.flush();
					 writer1.close();*/
				 }
				 /*else
				 {
					 headingWd = new JSONArray();
					 JSONObject obj = new JSONObject();
					 obj.put("errorMsg", faultStr.substring(faultStr.indexOf("."), faultStr.length()));
					 headingWd.put(obj);
				 }*/
			 }			 
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}

	private JSONArray createCSVFromWDHire(Tenant tenant2, InputStream is, SOAPConnection soapConnection, int startIndex,
			int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		targetContent = null;
		headingFromWD = "";
		String checkFile = "";
		
		 String finalStr = "";
		 String headerStr = "";
		 String empId = "";
		 String positionId = "";
		 String positionTitle = "";
		 String businessTitle = "";
		 String endEmpDate = "";
		 String empTypeName = "";
		 String timeType = "";
		 String defaultWeeklyHours = "";
		 String scheduledWeeklyHour = "";
		 String payTypeName = "";
		 String jobCode = "";
		 String jobPositionTitle = "";
		 String location = "";
		 String hireDate = "";
		 String originalHireDate = "";
		 String hireReason = "";
		 String contServiceDate = "";
		 String probStartDate = "";
		 String probEndDate = "";
		 String benefitsServiceDate = "";
		 String companyServiceDate = "";
		 String supOrg = "";
		 String jobClassificationName = "";
		 String jobClassificationNameArr = "";
		 String companyInsiderTypeName = "";
		 String workHourProfileDesc = "";
		 String workShiftName = "";
		 String workSpaceName = "";
		 String email = "";
		 String emailArr = "";
		 
		 Map<String,String> idMap = null;
		 Map<String,String> empTypeMap = null;
		 Map<String,String> posTimeTypeMap = null;
		 Map<String,String> payRateMap = null;
		 Map<String,String> jobProfMap = null;
		 Map<String,String> hireReasonMap = null;
		 Map<String,String> orgMap = null;
		 Map<String,String> jobClassificationMap = null;
		 Map<String,String> companyInsiderMap = null;
		 Map<String,String> workHourProfileMap = null;
		 Map<String,String> workShiftMap = null;
		 Map<String,String> jobSpaceMap = null;
		 Map<String,String> locationMap = null;
		 Map<String,String> usageMap = null;
		 
		 try 
		 {
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_HIRE_REQUEST_FILE = requestfile.getAbsolutePath();
				 if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addWorkerIdListToFindError(GET_HIRE_REQUEST_FILE, columnList.get(i), ruleName, "Employee_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement rptElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = rptElement.getChild("env:Body")
										.getChild("wd:Get_Workers_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
							 else
							 {
								 ReportElement responseData = rptElement.getChild("env:Body")
											.getChild("wd:Get_Workers_Response")
											.getChild("wd:Response_Data");
								 
								 List<ReportElement> workerData = responseData.getChildren("wd:Worker");
									
								 for(ReportElement reportElement : workerData)
								 {
									 ReportElement element1 = reportElement.getChild("wd:Worker_Reference");
									 if(element1 != null)
									 {
										 List<ReportElement> idData = element1.getChildren("wd:ID");					 
										 for(ReportElement wdElement:idData)
										 {
											 idMap = wdElement.getAllAttributes();
											 if(idMap.get("wd:type").equals("Employee_ID"))
											 {
												 empId = wdElement.getValue().trim();
												 System.out.println("empId--"+empId);
											 }
										 }
									 }
									 
									 ReportElement element2 = reportElement.getChild("wd:Worker_Data")
									 							.getChild("wd:Employment_Data")
									 							.getChild("wd:Worker_Job_Data");
									 if(element2 != null)
									 {
										 ReportElement element3 = element2.getChild("wd:Position_Data");
										 if(element3 != null)
										 {
											 positionId = element3.getChild("wd:Position_ID") != null?element3.getChild("wd:Position_ID").getValue().trim():"";
											 positionTitle = element3.getChild("wd:Position_Title") != null?element3.getChild("wd:Position_Title").getValue().trim():"";
											 endEmpDate = element3.getChild("wd:End_Employment_Date") != null?element3.getChild("wd:End_Employment_Date").getValue().trim():"";
											 if(positionTitle.contains(","))
											 {
												 positionTitle = positionTitle.replaceAll(",", "|");
											 }
											 businessTitle = element3.getChild("wd:Business_Title") != null?element3.getChild("wd:Business_Title").getValue().trim():"";
											 if(businessTitle.contains(","))
											 {
												 businessTitle = businessTitle.replaceAll(",", "|");
											 }
											 defaultWeeklyHours = element3.getChild("wd:Default_Weekly_Hours") != null?element3.getChild("wd:Default_Weekly_Hours").getValue().trim():"";
											 scheduledWeeklyHour = element3.getChild("wd:Scheduled_Weekly_Hours") != null?element3.getChild("wd:Scheduled_Weekly_Hours").getValue().trim():"";
											 
											 ReportElement element4 = element3.getChild("wd:Worker_Type_Reference");
											 if(element4 != null)
											 {
												 List<ReportElement> empTypeData = element4.getChildren("wd:ID");					 
												 for(ReportElement empTypeElement:empTypeData)
												 {
													 empTypeMap = empTypeElement.getAllAttributes();
													 if(empTypeMap.get("wd:type").equals("Employee_Type_ID"))
													 {
														 empTypeName = empTypeElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 empTypeName = "";
											 }
											 
											 ReportElement element5 = element3.getChild("wd:Position_Time_Type_Reference");
											 if(element5 != null)
											 {
												 List<ReportElement> timeData = element5.getChildren("wd:ID");					 
												 for(ReportElement timeElement:timeData)
												 {
													 posTimeTypeMap = timeElement.getAllAttributes();
													 if(posTimeTypeMap.get("wd:type").equals("Position_Time_Type_ID"))
													 {
														 timeType = timeElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 timeType = "";
											 }
											 
											 ReportElement element6 = element3.getChild("wd:Pay_Rate_Type_Reference");
											 if(element6 != null)
											 {
												 List<ReportElement> payData = element6.getChildren("wd:ID");					 
												 for(ReportElement payElement:payData)
												 {
													 payRateMap = payElement.getAllAttributes();
													 if(payRateMap.get("wd:type").equals("Pay_Rate_Type_ID"))
													 {
														 payTypeName = payElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 payTypeName = "";
											 }
											 
											 ReportElement element7 = element3.getChild("wd:Job_Profile_Summary_Data");
											 if(element7 != null)
											 {
												 ReportElement element8 = element7.getChild("wd:Job_Profile_Reference");
												 if(element8 != null)
												 {
													 List<ReportElement> jobProfData = element8.getChildren("wd:ID");					 
													 for(ReportElement jobProfElement:jobProfData)
													 {
														 jobProfMap = jobProfElement.getAllAttributes();
														 if(jobProfMap.get("wd:type").equals("Job_Profile_ID"))
														 {
															 jobCode = jobProfElement.getValue().trim();
														 }
													 }
												 }
												 else
												 {
													 jobCode = "";
												 }
												 jobPositionTitle = element7.getChild("wd:Job_Profile_Name") != null?element7.getChild("wd:Job_Profile_Name").getValue().trim():"";
												 if(jobPositionTitle.contains(","))
												 {
													 jobPositionTitle = jobPositionTitle.replaceAll(",", "|");
												 }
											 }
											 else
											 {
												 jobCode = "";
												 jobPositionTitle = "";
											 }
											 
											 ReportElement element88 = element3.getChild("wd:Work_Space__Reference");
											 if(element88 != null)
											 {
												 List<ReportElement> jobspaceData = element88.getChildren("wd:ID");					 
												 for(ReportElement jobSpaceElement:jobspaceData)
												 {
													 jobSpaceMap = jobSpaceElement.getAllAttributes();
													 if(jobSpaceMap.get("wd:type").equals("Location_ID"))
													 {
														 workSpaceName = jobSpaceElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 workSpaceName = "";
											 }
											 
											 ReportElement element9 = element3.getChild("wd:Business_Site_Summary_Data");
											 if(element9 != null)
											 {
												 ReportElement element99 = element9.getChild("wd:Location_Reference");
												 if(element99 != null)
												 {
													 List<ReportElement> locationData = element99.getChildren("wd:ID");					 
													 for(ReportElement locationElement:locationData)
													 {
														 locationMap = locationElement.getAllAttributes();
														 if(locationMap.get("wd:type").equals("Location_ID"))
														 {
															 location = locationElement.getValue().trim();
														 }
													 }
												 }
											 }
											 else
											 {
												 location = ""; 
											 }
											 
											 List<ReportElement> jobClassSummData = element3.getChildren("wd:Job_Classification_Summary_Data");
											 if(jobClassSummData != null && jobClassSummData.size()>0)
											 {
												 jobClassificationNameArr = "";
												 for(ReportElement jobClassSummElement: jobClassSummData)
												 {
													 ReportElement jobClassRef = jobClassSummElement.getChild("wd:Job_Classification_Reference");
													 if(jobClassRef != null)
													 {
														 List<ReportElement> jobClassData = jobClassRef.getChildren("wd:ID");					 
														 for(ReportElement jobClassElement:jobClassData)
														 {
															 jobClassificationMap = jobClassElement.getAllAttributes();
															 if(jobClassificationMap.get("wd:type").equals("Job_Classification_Reference_ID"))
															 {
																 jobClassificationName = jobClassElement.getValue().trim();
																 if(jobClassificationName.contains(","))
																 {
																	 jobClassificationName = jobClassificationName.replaceAll(",", "|");
																 }
																 if(jobClassificationNameArr.equals(""))
																 {
																	 jobClassificationNameArr = jobClassificationName;
																 }
																 else
																 {
																	 jobClassificationNameArr = jobClassificationNameArr + "~" + jobClassificationName;
																 }
															 }
														 }											 
													 }													 
												 }
											 }
											 else
											 {
												 jobClassificationNameArr = "";
											 }
											 
											 ReportElement compInsiderData = element3.getChild("wd:Company_Insider_Reference");
											 if(compInsiderData != null)
											 {
												 List<ReportElement> compData = compInsiderData.getChildren("wd:ID");					 
												 for(ReportElement compElement:compData)
												 {
													 companyInsiderMap = compElement.getAllAttributes();
													 if(companyInsiderMap.get("wd:type").equals("Company_Insider_Type_ID"))
													 {
														 companyInsiderTypeName = compElement.getValue().trim();
													 }
												 }		
											 }
											 else
											 {
												 companyInsiderTypeName = ""; 
											 }
											 
											 ReportElement workShiftData = element3.getChild("wd:Work_Shift_Reference");
											 if(workShiftData != null)
											 {
												 List<ReportElement> shiftData = workShiftData.getChildren("wd:ID");					 
												 for(ReportElement shiftElement:shiftData)
												 {
													 workShiftMap = shiftElement.getAllAttributes();
													 if(workShiftMap.get("wd:type").equals("Work_Shift_ID"))
													 {
														 workShiftName = shiftElement.getValue().trim();
													 }
												 }		
											 }
											 else
											 {
												 workShiftName = ""; 
											 }
											 
											 ReportElement workHourData = element3.getChild("wd:Work_Hours_Profiles_Reference");
											 if(workHourData != null)
											 {
												 List<ReportElement> hourData = workHourData.getChildren("wd:ID");					 
												 for(ReportElement hourElement:hourData)
												 {
													 workHourProfileMap = hourElement.getAllAttributes();
													 if(workHourProfileMap.get("wd:type").equals("Work_Hours_Profile_ID"))
													 {
														 workHourProfileDesc = hourElement.getValue().trim();
													 }
												 }		
											 }
											 else
											 {
												 workHourProfileDesc = ""; 
											 }
										 }
										 else
										 {
											 positionId = "";
											 positionTitle = "";
											 businessTitle = "";
											 empTypeName = "";
											 timeType = "";
											 defaultWeeklyHours = "";
											 scheduledWeeklyHour = "";
											 payTypeName = "";
											 jobCode = "";
											 jobPositionTitle = "";
											 location = "";
											 jobClassificationNameArr = "";
											 companyInsiderTypeName = "";
											 workHourProfileDesc = "";
											 workShiftName = "";
										 }
									 }
									 else
									 {
										 positionId = "";
										 positionTitle = "";
										 businessTitle = "";
										 empTypeName = "";
										 timeType = "";
										 defaultWeeklyHours = "";
										 scheduledWeeklyHour = "";
										 payTypeName = "";
										 jobCode = "";
										 jobPositionTitle = "";
										 location = "";
										 jobClassificationName = "";
										 companyInsiderTypeName = "";
										 workHourProfileDesc = "";
										 workShiftName = "";
									 }
									 
									 ReportElement element10 = reportElement.getChild("wd:Worker_Data")
					 							.getChild("wd:Employment_Data")
					 							.getChild("wd:Worker_Status_Data");
									 
									 if(element10 != null)
									 {
										 hireDate = element10.getChild("wd:Hire_Date") != null?element10.getChild("wd:Hire_Date").getValue().trim():"";
										 if(!hireDate.isEmpty())
										 {
											 hireDate = hireDate.substring(0, 10);
											 hireDate = convertDate(hireDate, "yyyy-MM-dd", "dd-MM-yyyy");
										 }
										 originalHireDate = element10.getChild("wd:Original_Hire_Date") != null?element10.getChild("wd:Original_Hire_Date").getValue().trim():"";
										 contServiceDate = element10.getChild("wd:Continuous_Service_Date") != null?element10.getChild("wd:Continuous_Service_Date").getValue().trim():""; 
										 probStartDate = element10.getChild("wd:Probation_Start_Date") != null?element10.getChild("wd:Probation_Start_Date").getValue().trim():""; 
										 probEndDate = element10.getChild("wd:Probation_End_Date") != null?element10.getChild("wd:Probation_End_Date").getValue().trim():"";
										 benefitsServiceDate = element2.getChild("wd:Benefits_Service_Date") != null?element2.getChild("wd:Benefits_Service_Date").getValue().trim():""; 
										 companyServiceDate = element2.getChild("wd:Company_Service_Date") != null?element2.getChild("wd:Company_Service_Date").getValue().trim():""; 
										 
										 ReportElement element11 = element10.getChild("wd:Hire_Reason_Reference");
										 if(element11 != null)
										 {
											 List<ReportElement> reasonData = element11.getChildren("wd:ID");					 
											 for(ReportElement reasonElement:reasonData)
											 {
												 hireReasonMap = reasonElement.getAllAttributes();
												 if(hireReasonMap.get("wd:type").equals("Event_Classification_Subcategory_ID") || hireReasonMap.get("wd:type").equals("General_Event_Subcategory_ID"))
												 {
													 hireReason = reasonElement.getValue().trim();
												 }
											 }
										 }
										 else
										 {
											 hireReason = "";
										 }
									 }
									 else
									 {
										 hireDate = "";
										 originalHireDate = "";
										 hireReason = "";
										 contServiceDate = "";
										 probStartDate = "";
										 probEndDate = "";
										 benefitsServiceDate = "";
										 companyServiceDate = "";
									 }
									 
									 List<ReportElement> emailList = reportElement.getChild("wd:Worker_Data")
									 			.getChild("wd:Personal_Data")
									 			.getChild("wd:Contact_Data")
							 					.getChildren("wd:Email_Address_Data");
									 
									 if(emailList != null && emailList.size() >0)
									 {
										 emailArr = "";										 
										 for(ReportElement emailElement:emailList)
										 {
											 ReportElement usageData = emailElement.getChild("wd:Usage_Data");
											 if(usageData != null)
											 {
												 ReportElement typeDaya = usageData.getChild("wd:Type_Data");
												 if(typeDaya != null)
												 {
													 ReportElement typeRef = typeDaya.getChild("wd:Type_Reference");
													 if(typeRef !=null)
													 {
														 List<ReportElement> typeRefData = typeRef.getChildren("wd:ID");					 
														 for(ReportElement wdElement:typeRefData)
														 {
															 usageMap = wdElement.getAllAttributes();
															 if(usageMap.get("wd:type").equals("Communication_Usage_Type_ID"))
															 {
																 if(wdElement.getValue().trim().equalsIgnoreCase("WORK"))
																 {
																	 email = emailElement.getChild("wd:Email_Address") != null?emailElement.getChild("wd:Email_Address").getValue().trim():"";
																	 if(emailArr.equals(""))
																	 {
																		 emailArr = email;
																	 }
																	 else
																	 {
																		 emailArr = emailArr + "~" + email;
																	 }
																 }
																 														
															 }
														 }
													 }
												 }
											 }
										 }
									 }
									 
									 ReportElement element12 = reportElement.getChild("wd:Worker_Data")
					 							.getChild("wd:Organization_Data");
									 
									 if(element12 != null)
									 {
										 List<ReportElement> orgData = element12.getChildren("wd:Worker_Organization_Data");
										 for(ReportElement orgElement:orgData)
										 {
											 ReportElement orgDataElement = orgElement.getChild("wd:Organization_Reference");
											 List<ReportElement> orgElementData = orgDataElement.getChildren("wd:ID");
											 for(ReportElement orgValElement:orgElementData)
											 {
												 orgMap = orgValElement.getAllAttributes();
											     if(orgMap.get("wd:type").equals("Organization_Reference_ID") && orgValElement.getValue().trim().contains("SupOrg"))
												 {
													 ReportElement element13 = orgElement.getChild("wd:Organization_Data");
													 supOrg = element13.getChild("wd:Organization_Reference_ID") != null?element13.getChild("wd:Organization_Reference_ID").getValue().trim():"";
												 }
											 }
										 }
									 }
									 else
									 {
										 supOrg = "";
									 }
									 
									 headingFromWD = "Employee_ID,Position_ID,Employee_Type_Name,Hire_Date,Original_Hire_Date,Hire_Reason,Continuous_Service_Date,Probation_Start_Date,Probation_End_Date,"
									 		         + "End_Employment_Date,Benefit_Service_Date,Company_Service_Date,Supervisory_Organization,"
									 		         + "Job_Position_Title,Job_Profile,Position_Title,Business_Title,Location,Time_Type,Default_Weekly_Hours,"
									 		         + "Scheduled_Weekly_Hours,Pay_Type_Name,Additional_Job_Classifications,Company_Insider_Type_Name,Work_Hours_Profile,Work_Shift,Work_Space,Email";
										 
									 headerStr = empId + "," + positionId + "," + empTypeName + "," + hireDate + "," + originalHireDate + "," + hireReason + "," + contServiceDate + "," + probStartDate + "," + probEndDate + "," + 
											 endEmpDate + "," + benefitsServiceDate + "," + companyServiceDate + "," + supOrg + "," + jobPositionTitle + "," + jobCode + "," +
											 positionTitle + "," + businessTitle + "," +location + "," + timeType + "," + defaultWeeklyHours + "," + scheduledWeeklyHour + "," + payTypeName + "," + jobClassificationNameArr + "," +
											 companyInsiderTypeName + "," + workHourProfileDesc + "," + workShiftName + "," + workSpaceName + "," + emailArr;
									 
									 if(finalStr.equals(""))
									 {
										 finalStr = headingFromWD + "\n" + headerStr;
									 }
									 else
									 {
										 finalStr = finalStr + "\n" + headerStr;
									 }					 
								 }
							 }
						 }
					 }
					 columnList.removeAll(errorList);
					 wdCount = columnList.size();
				 }		 					 
				 System.out.println(finalStr);
				 targetContent = finalStr.toString().getBytes();					 					 
				 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");					 
				 complete = true;
			}			 			 
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}
	
	private JSONArray createCSVFromWDAssignOrganization(Tenant tenant, InputStream is, SOAPConnection soapConnection, int startIndex, int endIndex, JSONArray headingWd,
			String loadCycle, String ruleName, String client) {

		targetContent = null;
		headingFromWD = "";
		String checkFile = "";
		
		 String finalStr = "";
		 String headerStr = "";
		 String empId = "";
		 String costCenter = "";
		 String company = "";
		 String region = "";
		 String customOrg = "";
		 String customOrgArr = "";

		 
		 Map<String,String> idMap = null;
		 Map<String,String> orgMap = null;
		 
		 try 
		 {
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_ASSIGN_ORG_FILE = requestfile.getAbsolutePath();
				 if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addWorkerIdListToFindError(GET_ASSIGN_ORG_FILE, columnList.get(i), ruleName, "Employee_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement rptElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = rptElement.getChild("env:Body")
										.getChild("wd:Get_Workers_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
							 else
							 {
								 ReportElement responseData = rptElement.getChild("env:Body")
											.getChild("wd:Get_Workers_Response")
											.getChild("wd:Response_Data");
								 
								 List<ReportElement> workerData = responseData.getChildren("wd:Worker");
									
								 for(ReportElement reportElement : workerData)
								 {
									 ReportElement element1 = reportElement.getChild("wd:Worker_Reference");
									 if(element1 != null)
									 {
										 List<ReportElement> idData = element1.getChildren("wd:ID");					 
										 for(ReportElement wdElement:idData)
										 {
											 idMap = wdElement.getAllAttributes();
											 if(idMap.get("wd:type").equals("Employee_ID"))
											 {
												 empId = wdElement.getValue().trim();
												 System.out.println("empId--"+empId);
											 }
										 }
									 }
									 
									 ReportElement element12 = reportElement.getChild("wd:Worker_Data")
					 							.getChild("wd:Organization_Data");
									 
									 if(element12 != null)
									 {
										 List<ReportElement> orgData = element12.getChildren("wd:Worker_Organization_Data");
										 if(orgData != null && orgData.size() >0)
										 {
											 customOrgArr = "";
										     for(ReportElement orgElement:orgData)
											 {
												 ReportElement orgDataElement = orgElement.getChild("wd:Organization_Reference");
												 List<ReportElement> orgElementData = orgDataElement.getChildren("wd:ID");
												 for(ReportElement orgValElement:orgElementData)
												 {
													 orgMap = orgValElement.getAllAttributes();
													 if(orgMap.get("wd:type").equals("Cost_Center_Reference_ID"))
													 {
														 costCenter = orgValElement.getValue().trim();										 
													 }
													 else if(orgMap.get("wd:type").equals("Company_Reference_ID"))
													 {
														 company = orgValElement.getValue().trim();													 
													 }
													 else if(orgMap.get("wd:type").equals("Custom_Organization_Reference_ID"))
													 {
														 customOrg = orgValElement.getValue().trim();
														 if(customOrgArr.equals(""))
														 {
															 customOrgArr = customOrg;
														 }
														 else
														 {
															 customOrgArr = customOrgArr + "~" + customOrg;
														 }
													 }
													 else if(orgMap.get("wd:type").equals("Region_Reference_ID"))
													 {
														 region = orgValElement.getValue().trim();
													 }
												 }
											 }
										 }
										 else
										 {
											 costCenter = "";
											 company = "";
											 region = "";
											 customOrgArr = "";
										 }
									 }
									 else
									 {
										 costCenter = "";
										 company = "";
										 region = "";
										 customOrgArr = "";
									 }
									 
									 headingFromWD = "Employee_ID,Cost_Center_Organization,Company_Organization,Region_Organization,Custom_Organization";
										 
									 headerStr = empId + "," + costCenter  + "," + company  + "," + region + "," + customOrgArr;
									 
									 if(finalStr.equals(""))
									 {
										 finalStr = headingFromWD + "\n" + headerStr;
									 }
									 else
									 {
										 finalStr = finalStr + "\n" + headerStr;
									 }					 
								 }
							 }
						 }
					 }
					 columnList.removeAll(errorList);
					 wdCount = columnList.size();
				 }		 					 
				 System.out.println(finalStr);
				 targetContent = finalStr.toString().getBytes();					 					 
				 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");					 
				 complete = true;
			}			 			 
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}

	private JSONArray createCSVFromWDPosition(Tenant tenant2, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		targetContent = null;
		String checkFile = null;
		headingFromWD = "";
		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_POSITION_REQUEST_FILE = requestfile.getAbsolutePath();
				 //String outputfile = GET_POSITION_REQUEST_FILE;
				 //columnList.removeAll(errorList);
				 if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addPositionIdListToFindError(GET_POSITION_REQUEST_FILE, columnList.get(i), ruleName, "Position_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement reportElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = reportElement.getChild("env:Body")
										.getChild("wd:Get_Positions_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
						 }
					 }
					 columnList.removeAll(errorList);
				 }
				 String outputfile = addPositionIdList(GET_POSITION_REQUEST_FILE, columnList, ruleName, startIndex, columnList.size());

				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");			 
				 
				 /*if(strMsg.contains("faultstring"))
				 {
					 ReportElement msgResponse = XmlParserManager.parseXml(strMsg);
					 ReportElement elementData = msgResponse.getChild("SOAP-ENV:Body")
								.getChild("SOAP-ENV:Fault");
					 faultStr = elementData.getChild("faultstring").getValue().trim();
					 System.out.println("faultStr-"+faultStr);
				 }*/
				 
				 //if(faultStr == null)
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Positions_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String finalStr = "";
					 String headerStr = "";
					 String positionId = "";
					 String jobPostingTitle = "";
					 String orgRefId = "";
					 String availabilityDate = "";
					 String earliestHireDateDate = "";
					 String jobProfileId = "";
					 String jobFamilyId = "";
					 String locationId = "";
					 String workerTypeId = "";
					 String posTimeTypeId = "";
					 String empTypeId = "";
					 String companyId = "";
					 String costCenterId = "";
					 
					 
					 Map<String,String> idMap = null;
					 Map<String,String> orgMap = null;
					 Map<String,String> jobProfMap = null;
					 Map<String,String> jobFamilyMap = null;
					 Map<String,String> locMap = null;
					 Map<String,String> workerTypeMap = null;
					 Map<String,String> posTimeTypeMap = null;
					 Map<String,String> empTypeMap = null;
					 Map<String,String> companyMap = null;
					 Map<String,String> costCenterMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {					 
						 if(j == 1)
						 {
							startIndex = 0;
							if(1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*1000;
							}
						 }
						 else
						 {
							startIndex = endIndex;
							if(j*1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = (j*1000);
							}
						 }
						 outputfile = addPositionIdList(GET_POSITION_REQUEST_FILE, columnList, ruleName, startIndex, endIndex);
						 is = new FileInputStream(outputfile);
					     soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     soapPart = soapMessage.getSOAPPart();
					     envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
				         out = new ByteArrayOutputStream();
				         soapMessage.writeTo(out);
				         strMsg = new String(out.toByteArray());
				        
				         soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
				         soapResponse = soapConnection.call(soapMessage, sourceUrl);
				         out = new ByteArrayOutputStream();
				         soapResponse.writeTo(out);
				         strMsg = new String(out.toByteArray(), "utf-8");
				        
				         soapResp = XmlParserManager.parseXml(strMsg);
					 
						 ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Positions_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> positionData = responseData.getChildren("wd:Position");
							
						 for(ReportElement reportElement : positionData)
						 {
							 ReportElement element1 = reportElement.getChild("wd:Position_Reference");
							 if(element1 != null)
							 {
								 List<ReportElement> idData = element1.getChildren("wd:ID");					 
								 for(ReportElement wdElement:idData)
								 {
									 idMap = wdElement.getAllAttributes();
									 if(idMap.get("wd:type").equals("Position_ID"))
									 {
										 positionId = wdElement.getValue().trim();
									 }
								 }
							 }
							 ReportElement element2 = reportElement.getChild("wd:Position_Data")
							 							.getChild("wd:Position_Definition_Data");
							 if(element2 != null)
							 {
								 jobPostingTitle = element2.getChild("wd:Job_Posting_Title") != null?element2.getChild("wd:Job_Posting_Title").getValue().trim():"";
								 if(jobPostingTitle.contains(","))
								 {
									 jobPostingTitle = jobPostingTitle.replaceAll(",", "|");
								 }
							 }
							 else
							 {
								 jobPostingTitle = "";
							 }
							 
							 ReportElement element3 = reportElement.getChild("wd:Position_Data")
			 							.getChild("wd:Supervisory_Organization_Reference");
							 
							 if(element3 != null)
							 {
								 List<ReportElement> orgData = element3.getChildren("wd:ID");					 
								 for(ReportElement orgElement:orgData)
								 {
									 orgMap = orgElement.getAllAttributes();
									 if(orgMap.get("wd:type").equals("Organization_Reference_ID"))
									 {
										 orgRefId = orgElement.getValue().trim();								 
									 }
									 else
									 {
										 orgRefId = "";
									 }							 
								 }
							 }
							 else
							 {
								 orgRefId = ""; 
							 }
							 
							 ReportElement element4 = reportElement.getChild("wd:Position_Data")
			 							.getChild("wd:Position_Restrictions_Data");
							 if(element4 != null)
							 {
								 availabilityDate = element4.getChild("wd:Availability_Date") != null?element4.getChild("wd:Availability_Date").getValue().trim():"";
								 earliestHireDateDate = element4.getChild("wd:Earliest_Hire_Date") != null?element4.getChild("wd:Earliest_Hire_Date").getValue().trim():"";
								 
								 ReportElement element5 = element4.getChild("wd:Job_Profile_Restriction_Summary_Data");
								 if(element5 != null)
								 {
									 ReportElement element6 = element5.getChild("wd:Job_Profile_Reference");
									 if(element6 != null)
									 {
										 List<ReportElement> jobProfData = element6.getChildren("wd:ID");					 
										 for(ReportElement jobProfElement:jobProfData)
										 {
											 jobProfMap = jobProfElement.getAllAttributes();
											 if(jobProfMap.get("wd:type").equals("Job_Profile_ID"))
											 {
												 jobProfileId = jobProfElement.getValue().trim();								 
											 }
											 else
											 {
												 jobProfileId = "";
											 }							 
										 }
									 }
									 else
									 {
										 jobProfileId = ""; 
									 }
									 
									 ReportElement element7 = element5.getChild("wd:Job_Family_Reference");
									 if(element7 != null)
									 {
										 List<ReportElement> jobFamilyData = element7.getChildren("wd:ID");					 
										 for(ReportElement jobFamilyElement:jobFamilyData)
										 {
											 jobFamilyMap = jobFamilyElement.getAllAttributes();
											 if(jobFamilyMap.get("wd:type").equals("Job_Family_ID"))
											 {
												 jobFamilyId = jobFamilyElement.getValue().trim();								 
											 }
											 else
											 {
												 jobFamilyId = "";
											 }							 
										 }
									 }
									 else
									 {
										 jobFamilyId = ""; 
									 }
								 }
								 else
								 {
									 jobProfileId = "";
									 jobFamilyId = ""; 
								 }
								 
								 ReportElement element8 = element4.getChild("wd:Location_Reference");
								 if(element8 != null)
								 {
									 List<ReportElement> locData = element8.getChildren("wd:ID");					 
									 for(ReportElement locElement:locData)
									 {
										 locMap = locElement.getAllAttributes();
										 if(locMap.get("wd:type").equals("Location_ID"))
										 {
											 locationId = locElement.getValue().trim();								 
										 }
										 else
										 {
											 locationId = "";
										 }							 
									 }
								 }
								 else
								 {
									 locationId = ""; 
								 }
								 
								 ReportElement element9 = element4.getChild("wd:Worker_Type_Reference");
								 if(element9 != null)
								 {
									 List<ReportElement> workerData = element9.getChildren("wd:ID");					 
									 for(ReportElement workerElement:workerData)
									 {
										 workerTypeMap = workerElement.getAllAttributes();
										 if(workerTypeMap.get("wd:type").equals("Worker_Type_ID"))
										 {
											 workerTypeId = workerElement.getValue().trim();								 
										 }
										 else
										 {
											 workerTypeId = "";
										 }							 
									 }
								 }
								 else
								 {
									 workerTypeId = ""; 
								 }
								 
								 ReportElement element10 = element4.getChild("wd:Time_Type_Reference");
								 if(element10 != null)
								 {
									 List<ReportElement> timeData = element10.getChildren("wd:ID");					 
									 for(ReportElement timeElement:timeData)
									 {
										 posTimeTypeMap = timeElement.getAllAttributes();
										 if(posTimeTypeMap.get("wd:type").equals("Position_Time_Type_ID"))
										 {
											 posTimeTypeId = timeElement.getValue().trim();								 
										 }
										 else
										 {
											 posTimeTypeId = "";
										 }							 
									 }
								 }
								 else
								 {
									 posTimeTypeId = ""; 
								 }
								 
								 ReportElement element11 = element4.getChild("wd:Position_Worker_Type_Reference");
								 if(element11 != null)
								 {
									 List<ReportElement> empData = element11.getChildren("wd:ID");					 
									 for(ReportElement empElement:empData)
									 {
										 empTypeMap = empElement.getAllAttributes();
										 if(empTypeMap.get("wd:type").equals("Employee_Type_ID"))
										 {
											 empTypeId = empElement.getValue().trim();								 
										 }
										 else
										 {
											 empTypeId = "";
										 }							 
									 }
								 }
								 else
								 {
									 empTypeId = ""; 
								 }
							 }
							 else
							 {
								 availabilityDate = "";
								 earliestHireDateDate = "";
								 jobProfileId = "";
								 jobFamilyId = "";
								 locationId = "";
								 workerTypeId = "";
								 posTimeTypeId = "";
								 empTypeId = "";
							 }
							 
							 ReportElement element12 = reportElement.getChild("wd:Position_Data")
			 							.getChild("wd:Default_Position_Organization_Assignments_Data");
							 
							 if(element12 != null)
							 {
								 ReportElement element13 = element12.getChild("wd:Company_Assignments_Reference");
								 if(element13 != null)
								 {
									 List<ReportElement> compData = element13.getChildren("wd:ID");					 
									 for(ReportElement compElement:compData)
									 {
										 companyMap = compElement.getAllAttributes();
										 if(companyMap.get("wd:type").equals("Company_Reference_ID"))
										 {
											 companyId = compElement.getValue().trim();								 
										 }
										 else
										 {
											 companyId = "";
										 }							 
									 }
								 }
								 else
								 {
									 companyId = ""; 
								 }
								 
								 ReportElement element14 = element12.getChild("wd:Cost_Center_Assignments_Reference");
								 if(element14 != null)
								 {
									 List<ReportElement> ccData = element14.getChildren("wd:ID");					 
									 for(ReportElement ccElement:ccData)
									 {
										 costCenterMap = ccElement.getAllAttributes();
										 if(costCenterMap.get("wd:type").equals("Cost_Center_Reference_ID"))
										 {
											 costCenterId = ccElement.getValue().trim();								 
										 }
										 else
										 {
											 costCenterId = "";
										 }							 
									 }
								 }
								 else
								 {
									 costCenterId = ""; 
								 }
							 }
							 else
							 {
								 companyId = "";
								 costCenterId = "";
							 }
							 
							 headingFromWD = "Position_ID,Job_Posting_Title,Organization_Reference_ID,Availability_Date,Earliest_Hire_Date,Job_Family_ID,Job_Profile_ID,Location_ID,Worker_Type_ID,"
								 		+ "Position_Time_Type_ID,Employee_Type_ID,Company_ID,Cost_Center_ID";
								 
							 headerStr = positionId + "," + jobPostingTitle + "," + orgRefId + "," + availabilityDate + "," + earliestHireDateDate + "," + jobFamilyId + "," + jobProfileId + "," + 
									 locationId + "," + workerTypeId + "," + posTimeTypeId + "," + empTypeId + "," + companyId + "," + costCenterId;
							 
							 if(finalStr.equals(""))
							 {
								 finalStr = headingFromWD + "\n" + headerStr;
							 }
							 else
							 {
								 finalStr = finalStr + "\n" + headerStr;
							 }					 
						 }
					 }
					 
					 System.out.println(finalStr);
					 targetContent = finalStr.toString().getBytes();
					 
					 /*File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
					 PrintWriter writer = new PrintWriter(wdCSVfile);
					 writer.write(finalStr.toString());
					 writer.flush();
					 writer.close();
					 
					 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
					 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
					 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
					 postLoadService.updatePostLoad(postLoad);*/
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Position_ID");
					 complete = true;
					 
					 /*String wdCSVfile = "config/" + loadCycle + "_" + ruleName + ".csv";			 
					 PrintWriter writer = new PrintWriter(new File(wdCSVfile));
					 writer.write(finalStr.toString());
					 writer.flush();
					 writer.close();*/
				 }
				 /*else
				 {
					 headingWd = new JSONArray();
					 JSONObject obj = new JSONObject();
					 obj.put("errorMsg", faultStr.substring(faultStr.indexOf("."), faultStr.length()));
					 headingWd.put(obj); 
				 }*/
			 }
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}

	private String addPositionIdListToFindError(String xmlFile, String columnVal, String ruleName, String idVal) {
		File updatedRequestfile = null;
		try  
		{  
			File file = new File(xmlFile);     
			FileReader fr = new FileReader(file);   
			BufferedReader br = new BufferedReader(fr);   
			StringBuffer sb = new StringBuffer();     
			String line; 
			while((line=br.readLine())!=null)  
			{  
				sb.append(line);  
				sb.append("\n");
				if(line.contains("bsvc:Get_Positions_Request"))
				{
					sb.append(" <bsvc:Request_References>"); 
					sb.append("\n");
						sb.append("  <bsvc:Positions_Reference bsvc:Descriptor=" + "\"" + idVal + "\"" + ">");
						sb.append("\n");
						sb.append("   <bsvc:ID bsvc:type=" + "\"" + idVal + "\"" + ">" + columnVal + "</bsvc:ID>");
						sb.append("\n");
						sb.append("  </bsvc:Positions_Reference>");
						sb.append("\n");					
					sb.append(" </bsvc:Request_References>");
					sb.append("\n");
				}
			}  
			fr.close();
			br.close();	
			
			updatedRequestfile = File.createTempFile(ruleName + "_" + columnVal , ".xml");
			PrintWriter writer = new PrintWriter(updatedRequestfile);
		    writer.write(sb.toString());
			writer.flush();
			writer.close();
		}  
		catch(IOException e)  
		{  
			e.printStackTrace();  
		}
		return updatedRequestfile.getAbsolutePath(); 
	}

	private JSONArray createCSVFromWDApplicant(Tenant tenant, InputStream is, SOAPConnection soapConnection, int startIndex,
			int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {
		
		//String faultStr = null;
		headingFromWD = "";
		//wdCount = 0;
		String checkFile = null;
		//postloadErrorList.clear();
		try 
		{			 
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_APPLICANT_REQUEST_FILE = requestfile.getAbsolutePath();
				 //columnList.removeAll(errorList);
				 if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addIdListToFindError(GET_APPLICANT_REQUEST_FILE, columnList.get(i), ruleName, "Applicant_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement reportElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = reportElement.getChild("env:Body")
										.getChild("wd:Get_Applicants_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
						 }
					 }
					 columnList.removeAll(errorList);
				 }
				 String outputfile = addApplicantIdList(GET_APPLICANT_REQUEST_FILE, columnList, ruleName, startIndex, columnList.size());
				 //String outputfile = "config/" + ruleName + ".xml";
				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");
				 
				 /*if(strMsg.contains("faultstring"))
				 {
					 ReportElement msgResponse = XmlParserManager.parseXml(strMsg);
					 ReportElement elementData = msgResponse.getChild("SOAP-ENV:Body")
								.getChild("SOAP-ENV:Fault");
					 faultStr = elementData.getChild("faultstring").getValue().trim();
					 System.out.println("faultStr-"+faultStr);
				 }*/
				 
				 //if(faultStr == null)
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);
					 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Applicants_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String prefix = "";
					 String suffix = "";
					 String legalFirstName = "";
					 String legalMiddleName = "";
					 String legalLastName = "";
					 String legalSecondaryName = "";
					 String localFirstName = "";
					 String localMiddleName = "";
					 String localLastName = "";
					 String localSecondaryName = "";
					 String localFirstName2 = "";
					 String localScript = "";
					 String countryISOCode = "";
					 String country;
					 String preferredFirstName = "";
					 String preferredMiddleName = "";
					 String preferredLastName = "";
					 String preferredSecondaryName = "";
					 String email = "";
					 String visibility = "";
					 String primary = "";
					 String primaryPhone = "";
					 String usageType = "";
					 String countryPhCode = "";
					 String areaCode = "";
					 String phoneNumber = "";
					 String phoneDeviceType = "";
					 String usageTypePhone = "";
					 String finalStr = "";
					 String headerStr = "";
					 Map<String,String> keyMap = null;
					 Map<String,String> prefixMap = null;
					 Map<String,String> scriptMap = null;
					 Map<String,String> publicMap = null;
					 Map<String,String> primaryMap = null;
					 Map<String,String> primaryPhoneMap = null;
					 Map<String,String> usageMap = null;
					 Map<String,String> phUsageMap = null;
					 Map<String,String> phDeviceMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {
						if(j == 1)
						{
							startIndex = 0;
							if(1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*1000;
							}
						}
						else
						{
							//startIndex = (j - 1)*1000;
							int lastVal = (j - 1);
							startIndex = (endIndex - lastVal);
							if(j*1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								//endIndex = j*1000;
								endIndex = (j*startIndex) + 1;
							}
						}
						outputfile = addApplicantIdList(GET_APPLICANT_REQUEST_FILE, columnList, ruleName, startIndex, endIndex);
						//outputfile = "config/" + ruleName + ".xml";
						is = new FileInputStream(outputfile);
					    soapMessage = MessageFactory.newInstance().createMessage(null, is);
						//SOAPBody soapBody = soapMessage.getSOAPBody();
						//Iterator itr = soapBody.getChildElements();
						//count = 0;
						//iterateRequest(itr, j);
					    soapPart = soapMessage.getSOAPPart();
					    envelope = soapPart.getEnvelope();
						envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						{
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						}
						soapMessage.saveChanges();
				        out = new ByteArrayOutputStream();
				        soapMessage.writeTo(out);
				        strMsg = new String(out.toByteArray());
				        
				        soapConnectionFactory = SOAPConnectionFactory.newInstance();
						soapConnection = soapConnectionFactory.createConnection();
				        soapResponse = soapConnection.call(soapMessage, sourceUrl);
				        out = new ByteArrayOutputStream();
				        soapResponse.writeTo(out);
				        strMsg = new String(out.toByteArray(), "utf-8");
				        
				        soapResp = XmlParserManager.parseXml(strMsg);
					        
						ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Applicants_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> applicantData = responseData.getChildren("wd:Applicant");
							
						 for(ReportElement reportElement : applicantData)
						 {
							 ReportElement element1 = reportElement.getChild("wd:Applicant_Data");
							 String applicantId = element1.getChild("wd:Applicant_ID").getValue().trim();
							 System.out.println("applicantId-"+applicantId);
							 
							 ReportElement elementNameData = reportElement.getChild("wd:Applicant_Data")
							 			.getChild("wd:Personal_Data")
							 			.getChild("wd:Name_Data");
							 
							 if(elementNameData != null)
							 {
								 ReportElement element2 = reportElement.getChild("wd:Applicant_Data")
										 			.getChild("wd:Personal_Data")
										 			.getChild("wd:Name_Data")
								 					.getChild("wd:Legal_Name_Data")
								 					.getChild("wd:Name_Detail_Data");
								 
								 legalFirstName = element2.getChild("wd:First_Name") != null?element2.getChild("wd:First_Name").getValue().trim():"";
								 legalMiddleName = element2.getChild("wd:Middle_Name")!= null?element2.getChild("wd:Middle_Name").getValue().trim():"";
								 legalLastName = element2.getChild("wd:Last_Name") != null?element2.getChild("wd:Last_Name").getValue().trim():"";
								 legalSecondaryName = element2.getChild("wd:Secondary_Last_Name")!= null?element2.getChild("wd:Secondary_Last_Name").getValue().trim():"";
								 
								 ReportElement element3 = reportElement.getChild("wd:Applicant_Data")
								 			.getChild("wd:Personal_Data")
								 			.getChild("wd:Name_Data")
						 					.getChild("wd:Legal_Name_Data")
						 					.getChild("wd:Name_Detail_Data")
						 					.getChild("wd:Prefix_Data");								 			
								 
								 if(element3 != null)
								 {
									 ReportElement element33 = element3.getChild("wd:Title_Reference");
									 if(element33 != null)
									 {
										 List<ReportElement> prefixData = element33.getChildren("wd:ID");
										 for(ReportElement prefixElement:prefixData)
										 {
											 prefixMap = prefixElement.getAllAttributes();
											 if(prefixMap.get("wd:type").equals("Predefined_Name_Component_ID"))
											 {
												 prefix = prefixElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 prefix = "";
									 }
								 }
								 else
								 {
									 prefix = "";
								 }
								 
								 ReportElement element4 = reportElement.getChild("wd:Applicant_Data")
								 			.getChild("wd:Personal_Data")
								 			.getChild("wd:Name_Data")
						 					.getChild("wd:Legal_Name_Data")
						 					.getChild("wd:Name_Detail_Data")
						 					.getChild("wd:Suffix_Data");
								 
								 if(element4 != null)
								 {
									 suffix = element4.getChild("wd:Social_Suffix_Descriptor")!=null?element4.getChild("wd:Social_Suffix_Descriptor").getValue().trim():"";
								 }
								 else
								 {
									 suffix = ""; 
								 }
								 
								 ReportElement element5 = reportElement.getChild("wd:Applicant_Data")
								 			.getChild("wd:Personal_Data")
								 			.getChild("wd:Name_Data")
						 					.getChild("wd:Legal_Name_Data")
						 					.getChild("wd:Name_Detail_Data")
						 					.getChild("wd:Country_Reference");
								 
								 List<ReportElement> wdData = element5.getChildren("wd:ID");
								 
								 for(ReportElement wdElement:wdData)
								 {
									 keyMap = wdElement.getAllAttributes();
									 if(keyMap.get("wd:type").equals("ISO_3166-1_Alpha-3_Code"))
									 {
										 countryISOCode = wdElement.getValue().trim();
									 }
								 }
								 country = countryISOCode;
								 
								 ReportElement element6 = reportElement.getChild("wd:Applicant_Data")
								 			.getChild("wd:Personal_Data")
								 			.getChild("wd:Name_Data")
						 					.getChild("wd:Legal_Name_Data")
						 					.getChild("wd:Name_Detail_Data")
						 					.getChild("wd:Local_Name_Detail_Data");
								 
								 if(element6 != null)
								 {
									 localFirstName = element6.getChild("wd:First_Name") != null?element6.getChild("wd:First_Name").getValue().trim():"";
									 localMiddleName = element6.getChild("wd:Middle_Name")!= null?element6.getChild("wd:Middle_Name").getValue().trim():"";
									 localLastName = element6.getChild("wd:Last_Name") != null?element6.getChild("wd:Last_Name").getValue().trim():"";
									 localSecondaryName = element6.getChild("wd:Secondary_Last_Name")!= null?element6.getChild("wd:Secondary_Last_Name").getValue().trim():"";
									 localFirstName2 = element6.getChild("wd:First_Name_2")!= null?element6.getChild("wd:First_Name_2").getValue().trim():"";
									 scriptMap = element6.getAllAttributes();
									 localScript = scriptMap.get("wd:Local_Script");
								 }
								 else
								 {
									 localFirstName = "";
									 localMiddleName = "";
									 localLastName = "";
									 localSecondaryName = "";
									 localFirstName2 = "";
									 localScript = "";
								 }
								 
								 ReportElement element7 = reportElement.getChild("wd:Applicant_Data")
								 			.getChild("wd:Personal_Data")
								 			.getChild("wd:Name_Data")
						 					.getChild("wd:Preferred_Name_Data")
						 					.getChild("wd:Name_Detail_Data");
								 
								 preferredFirstName = element7.getChild("wd:First_Name") != null?element7.getChild("wd:First_Name").getValue().trim():"";
								 preferredMiddleName = element7.getChild("wd:Middle_Name")!= null?element7.getChild("wd:Middle_Name").getValue().trim():"";
								 preferredLastName = element7.getChild("wd:Last_Name") != null?element7.getChild("wd:Last_Name").getValue().trim():"";
								 preferredSecondaryName = element7.getChild("wd:Secondary_Last_Name")!= null?element7.getChild("wd:Secondary_Last_Name").getValue().trim():"";
							 }
							 else
							 {
								 localFirstName = "";
								 localMiddleName = "";
								 localLastName = "";
								 localSecondaryName = "";
								 localFirstName2 = "";
								 localScript = "";
								 countryISOCode = "";
								 country = "";
								 prefix = "";
								 suffix = "";
								 legalFirstName = "";
								 legalMiddleName = "";
								 legalLastName = "";
								 legalSecondaryName = "";
								 preferredFirstName = "";
								 preferredMiddleName = "";
								 preferredLastName = "";
								 preferredSecondaryName = "";
							 }
							 					 
							 List<ReportElement> emailList = reportElement.getChild("wd:Applicant_Data")
							 			.getChild("wd:Personal_Data")
							 			.getChild("wd:Contact_Data")
					 					.getChildren("wd:Email_Address_Data");
							 
							 if(emailList != null)
							 {
								 for(ReportElement emailElement:emailList)
								 {
									 email = emailElement.getChild("wd:Email_Address") != null?emailElement.getChild("wd:Email_Address").getValue().trim():"";
									 ReportElement element9 = emailElement.getChild("wd:Usage_Data");
									 if(element9 != null)
									 {
										 publicMap = element9.getAllAttributes();
										 if(!publicMap.get("wd:Public").equals("0"))
										 {
											 visibility = "Y";
										 }
										 else
										 {
											 visibility = "N";
										 }
										 ReportElement element10 = element9.getChild("wd:Type_Data");
										 if(element10 != null)
										 {
											 primaryMap = element10.getAllAttributes();
											 if(primaryMap.get("wd:Primary").equals("1"))
											 {
												 primary = "Y";
											 }
											 else
											 {
												 primary = "N";
											 }
											 ReportElement element11 = element10.getChild("wd:Type_Reference");
											 if(element11 !=null)
											 {
												 List<ReportElement> usageData = element11.getChildren("wd:ID");					 
												 for(ReportElement wdElement:usageData)
												 {
													 usageMap = wdElement.getAllAttributes();
													 if(usageMap.get("wd:type").equals("Communication_Usage_Type_ID"))
													 {
														 usageType = wdElement.getValue().trim();														
													 }
												 }
											 }
											 else
											 {
												 usageType = ""; 
											 }
										 }
										 else
										 {
											 primary = ""; 
										 }
									 }
									 else
									 {
										 visibility = "N";
									 }
									 
									 if(usageType.equalsIgnoreCase("WORK"))
									 {
										 break;
									 }
									 else
									 {
										 email = ""; 
										 visibility = "N";
										 primary = "";
										 usageType = ""; 
									 }
								 }
							 }
							 else
							 {
								 email = ""; 
								 visibility = "N";
								 primary = "";
								 usageType = ""; 
							 }
							 
							 List<ReportElement> phoneList = reportElement.getChild("wd:Applicant_Data")
							 			.getChild("wd:Personal_Data")
							 			.getChild("wd:Contact_Data")
					 					.getChildren("wd:Phone_Data");
							 
							if(phoneList != null)
							{
								for(ReportElement phoneElement : phoneList)
								 {
									 countryPhCode = phoneElement.getChild("wd:Country_ISO_Code") != null?phoneElement.getChild("wd:Country_ISO_Code").getValue().trim():""; 
									 areaCode = phoneElement.getChild("wd:Area_Code") != null?phoneElement.getChild("wd:Area_Code").getValue().trim():""; 
									 phoneNumber = phoneElement.getChild("wd:Phone_Number") != null?phoneElement.getChild("wd:Phone_Number").getValue().trim():""; 
									 
									 ReportElement element13 = phoneElement.getChild("wd:Phone_Device_Type_Reference");
									 if(element13 != null)
									 {
										 List<ReportElement> phDeviceData = element13.getChildren("wd:ID");								 
										 for(ReportElement phDeviceElement:phDeviceData)
										 {
											 phDeviceMap = phDeviceElement.getAllAttributes();
											 if(phDeviceMap.get("wd:type").equals("Phone_Device_Type_ID"))
											 {
												 phoneDeviceType = phDeviceElement.getValue().trim();
											 }
										 }
									 }
									 else
									 {
										 phoneDeviceType = "";
									 }
									 
									 ReportElement element14 = phoneElement.getChild("wd:Usage_Data");
									 if(element14 != null)
									 {
										 ReportElement element15 = element14.getChild("wd:Type_Data");
										 if(element15 != null)
										 {
											 primaryPhoneMap = element15.getAllAttributes();
											 if(primaryPhoneMap.get("wd:Primary").equals("1"))
											 {
												 primaryPhone = "";//Change made by Soumya
											 }
											 else
											 {
												 primaryPhone = "N";
											 }
											 ReportElement element16 = element15.getChild("wd:Type_Reference");
											 if(element16 !=null)
											 {
												 List<ReportElement> usageData = element16.getChildren("wd:ID");					 
												 for(ReportElement wdElement:usageData)
												 {
													 phUsageMap = wdElement.getAllAttributes();
													 if(phUsageMap.get("wd:type").equals("Communication_Usage_Type_ID"))
													 {
														 usageTypePhone = wdElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 usageTypePhone = ""; 
											 }
										 }
									 }
									 else
									 {
										 usageTypePhone = "";
									 }
								 }
							}
							else
							{
								countryPhCode = ""; 
								areaCode = ""; 
								phoneNumber = ""; 
								phoneDeviceType = "";
								usageTypePhone = "";
								primaryPhone = "";
							}
							 
							 ReportElement element12 = reportElement.getChild("wd:Applicant_Data")
							 			.getChild("wd:Recruiting_Data");
							 String applicantEnteredDate = element12.getChild("wd:Applicant_Entered_Date") != null?element12.getChild("wd:Applicant_Entered_Date").getValue().trim():"";
							 
							 headingFromWD = "Applicant_ID,Applicant_Entered_Date,Country_ISO_Code,Prefix,Legal_First_Name,Legal_Middle_Name,Legal_Last_Name,Legal_Secondary_Name,Suffix,"
							 		+ "Preferred_First_Name,Preferred_Middle_Name,Preferred_Last_Name,Preferred_Secondary_Name,Local_Script,Local_Script_First_Name,Local_Script_Middle_Name,Local_Script_Last_Name,"
							 		+ "Local_Script_Secondary_Name,Local_First_Name_2,Email_Address,Primary_Email,Visibility_Email,Usage_Type_Email,Country_Phone_Code,Area_Code,Phone_Number,"
							 		+ "Phone_Device_Type,Usage_Type_Phone,Country,Primary_Phone,Applicant_Source_Name,Applicant_Source_Category_Name,Local_Primary_2";
							 
							 headerStr = applicantId + "," + applicantEnteredDate + "," + countryISOCode + "," + prefix + "," + legalFirstName + "," + legalMiddleName + "," + legalLastName + "," + legalSecondaryName + "," +
									 		suffix + "," + preferredFirstName + "," + preferredMiddleName + "," + preferredLastName + "," + preferredSecondaryName + "," + localScript + "," + localFirstName +
									 		"," + localMiddleName + "," + localLastName + "," + localSecondaryName + "," + localFirstName2 + "," + email + "," + primary + "," + visibility + "," + usageType +
									 		"," + countryPhCode + "," + areaCode + "," + phoneNumber + "," + phoneDeviceType + "," + usageTypePhone + "," + country +  "," + primaryPhone + "," +  "," + ",";
							 
							 if(finalStr.equals(""))
							 {
								 finalStr = headingFromWD + "\n" + headerStr;
							 }
							 else
							 {
								 finalStr = finalStr + "\n" + headerStr;
							 }
						 }
					 }
					 System.out.println(finalStr);
					 
					 /*String wdCSVfile = "config/" + loadCycle + "_" + ruleName + ".csv";			 
					 PrintWriter writer = new PrintWriter(new File(wdCSVfile));
					 writer.write(finalStr.toString());
					 writer.flush();
					 writer.close();*/
					 
					 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
					 PrintWriter writer = new PrintWriter(wdCSVfile);
					 writer.write(finalStr.toString());
					 writer.flush();
					 writer.close();
					 
				     //File wdCSVfile = new File("config/Applicant Pitney.csv");
					 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
					 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
					 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
					 postLoadService.updatePostLoad(postLoad);
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Applicant_ID");
				 }
				 /*else
				 {
					 headingWd = new JSONArray();
					 JSONObject obj = new JSONObject();
					 obj.put("errorMsg", faultStr.substring(faultStr.indexOf("."), faultStr.length()));
					 headingWd.put(obj);
				 }*/
			 }

		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return headingWd;
	}
	
	private JSONArray createCSVFromWDApplicantMultiplePh(Tenant tenant, InputStream is, SOAPConnection soapConnection, int startIndex,
			int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {
		
		 targetContent = null;
		 headingFromWD = "";
		 String checkFile = null;
		 String prefix = "";
		 String suffix = "";
		 String legalFirstName = "";
		 String legalMiddleName = "";
		 String legalLastName = "";
		 String legalSecondaryName = "";
		 String localFirstName = "";
		 String localMiddleName = "";
		 String localLastName = "";
		 String localSecondaryName = "";
		 String localFirstName2 = "";
		 String localScript = "";
		 String countryISOCode = "";
		 String country;
		 String preferredFirstName = "";
		 String preferredMiddleName = "";
		 String preferredLastName = "";
		 String preferredSecondaryName = "";
		 String email = "";
		 String usageType = "";
		 String emailArr = "";
		 String usageTypeArr = "";					 
		 String countryPhCode = "";
		 String areaCode = "";
		 String phoneNumber = "";
		 String phoneDeviceType = "";
		 String usageTypePhone = "";
		 String countryPhCodeArr = "";
		 String areaCodeArr = "";
		 String phoneNumberArr = "";
		 String phoneDeviceTypeArr = "";
		 String usageTypePhoneArr = "";
		 String finalStr = "";
		 String headerStr = "";
		 
		 Map<String,String> keyMap = null;
		 Map<String,String> prefixMap = null;
		 Map<String,String> scriptMap = null;
		 Map<String,String> usageMap = null;
		 Map<String,String> phUsageMap = null;
		 Map<String,String> phDeviceMap = null;
		 
		 try 
		 {			 
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_APPLICANT_REQUEST_FILE = requestfile.getAbsolutePath();
				 //columnList.removeAll(errorList);
				 if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addIdListToFindError(GET_APPLICANT_REQUEST_FILE, columnList.get(i), ruleName, "Applicant_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement rptElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = rptElement.getChild("env:Body")
										.getChild("wd:Get_Applicants_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
							 else
							 {						        
								ReportElement responseData = rptElement.getChild("env:Body")
											.getChild("wd:Get_Applicants_Response")
											.getChild("wd:Response_Data");
								 
								 List<ReportElement> applicantData = responseData.getChildren("wd:Applicant");
									
								 for(ReportElement reportElement : applicantData)
								 {
									 ReportElement element1 = reportElement.getChild("wd:Applicant_Data");
									 String applicantId = element1.getChild("wd:Applicant_ID").getValue().trim();
									 System.out.println("applicantId-"+applicantId);
									 
									 ReportElement elementNameData = reportElement.getChild("wd:Applicant_Data")
									 			.getChild("wd:Personal_Data")
									 			.getChild("wd:Name_Data");
									 
									 if(elementNameData != null)
									 {
										 ReportElement element2 = reportElement.getChild("wd:Applicant_Data")
												 			.getChild("wd:Personal_Data")
												 			.getChild("wd:Name_Data")
										 					.getChild("wd:Legal_Name_Data")
										 					.getChild("wd:Name_Detail_Data");
										 
										 legalFirstName = element2.getChild("wd:First_Name") != null?element2.getChild("wd:First_Name").getValue().trim():"";
										 legalMiddleName = element2.getChild("wd:Middle_Name")!= null?element2.getChild("wd:Middle_Name").getValue().trim():"";
										 legalLastName = element2.getChild("wd:Last_Name") != null?element2.getChild("wd:Last_Name").getValue().trim():"";
										 legalSecondaryName = element2.getChild("wd:Secondary_Last_Name")!= null?element2.getChild("wd:Secondary_Last_Name").getValue().trim():"";
										 
										 ReportElement element3 = reportElement.getChild("wd:Applicant_Data")
										 			.getChild("wd:Personal_Data")
										 			.getChild("wd:Name_Data")
								 					.getChild("wd:Legal_Name_Data")
								 					.getChild("wd:Name_Detail_Data")
								 					.getChild("wd:Prefix_Data");								 			
										 
										 if(element3 != null)
										 {
											 ReportElement element33 = element3.getChild("wd:Title_Reference");
											 if(element33 != null)
											 {
												 List<ReportElement> prefixData = element33.getChildren("wd:ID");
												 for(ReportElement prefixElement:prefixData)
												 {
													 prefixMap = prefixElement.getAllAttributes();
													 if(prefixMap.get("wd:type").equals("Predefined_Name_Component_ID"))
													 {
														 prefix = prefixElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 prefix = "";
											 }
										 }
										 else
										 {
											 prefix = "";
										 }
										 
										 ReportElement element4 = reportElement.getChild("wd:Applicant_Data")
										 			.getChild("wd:Personal_Data")
										 			.getChild("wd:Name_Data")
								 					.getChild("wd:Legal_Name_Data")
								 					.getChild("wd:Name_Detail_Data")
								 					.getChild("wd:Suffix_Data");
										 
										 if(element4 != null)
										 {
											 suffix = element4.getChild("wd:Social_Suffix_Descriptor")!=null?element4.getChild("wd:Social_Suffix_Descriptor").getValue().trim():"";
										 }
										 else
										 {
											 suffix = ""; 
										 }
										 
										 ReportElement element5 = reportElement.getChild("wd:Applicant_Data")
										 			.getChild("wd:Personal_Data")
										 			.getChild("wd:Name_Data")
								 					.getChild("wd:Legal_Name_Data")
								 					.getChild("wd:Name_Detail_Data")
								 					.getChild("wd:Country_Reference");
										 
										 List<ReportElement> wdData = element5.getChildren("wd:ID");
										 
										 for(ReportElement wdElement:wdData)
										 {
											 keyMap = wdElement.getAllAttributes();
											 if(keyMap.get("wd:type").equals("ISO_3166-1_Alpha-3_Code"))
											 {
												 countryISOCode = wdElement.getValue().trim();
											 }
										 }
										 country = countryISOCode;
										 
										 ReportElement element6 = reportElement.getChild("wd:Applicant_Data")
										 			.getChild("wd:Personal_Data")
										 			.getChild("wd:Name_Data")
								 					.getChild("wd:Legal_Name_Data")
								 					.getChild("wd:Name_Detail_Data")
								 					.getChild("wd:Local_Name_Detail_Data");
										 
										 if(element6 != null)
										 {
											 localFirstName = element6.getChild("wd:First_Name") != null?element6.getChild("wd:First_Name").getValue().trim():"";
											 localMiddleName = element6.getChild("wd:Middle_Name")!= null?element6.getChild("wd:Middle_Name").getValue().trim():"";
											 localLastName = element6.getChild("wd:Last_Name") != null?element6.getChild("wd:Last_Name").getValue().trim():"";
											 localSecondaryName = element6.getChild("wd:Secondary_Last_Name")!= null?element6.getChild("wd:Secondary_Last_Name").getValue().trim():"";
											 localFirstName2 = element6.getChild("wd:First_Name_2")!= null?element6.getChild("wd:First_Name_2").getValue().trim():"";
											 scriptMap = element6.getAllAttributes();
											 localScript = scriptMap.get("wd:Local_Script");
										 }
										 else
										 {
											 localFirstName = "";
											 localMiddleName = "";
											 localLastName = "";
											 localSecondaryName = "";
											 localFirstName2 = "";
											 localScript = "";
										 }
										 
										 ReportElement element7 = reportElement.getChild("wd:Applicant_Data")
										 			.getChild("wd:Personal_Data")
										 			.getChild("wd:Name_Data")
								 					.getChild("wd:Preferred_Name_Data")
								 					.getChild("wd:Name_Detail_Data");
										 
										 preferredFirstName = element7.getChild("wd:First_Name") != null?element7.getChild("wd:First_Name").getValue().trim():"";
										 preferredMiddleName = element7.getChild("wd:Middle_Name")!= null?element7.getChild("wd:Middle_Name").getValue().trim():"";
										 preferredLastName = element7.getChild("wd:Last_Name") != null?element7.getChild("wd:Last_Name").getValue().trim():"";
										 preferredSecondaryName = element7.getChild("wd:Secondary_Last_Name")!= null?element7.getChild("wd:Secondary_Last_Name").getValue().trim():"";
									 }
									 else
									 {
										 localFirstName = "";
										 localMiddleName = "";
										 localLastName = "";
										 localSecondaryName = "";
										 localFirstName2 = "";
										 localScript = "";
										 countryISOCode = "";
										 country = "";
										 prefix = "";
										 suffix = "";
										 legalFirstName = "";
										 legalMiddleName = "";
										 legalLastName = "";
										 legalSecondaryName = "";
										 preferredFirstName = "";
										 preferredMiddleName = "";
										 preferredLastName = "";
										 preferredSecondaryName = "";
									 }
									 
									 ReportElement element12 = reportElement.getChild("wd:Applicant_Data")
									 			.getChild("wd:Recruiting_Data");
									 String applicantEnteredDate = element12.getChild("wd:Applicant_Entered_Date") != null?element12.getChild("wd:Applicant_Entered_Date").getValue().trim():"";
									 					 
									 List<ReportElement> emailList = reportElement.getChild("wd:Applicant_Data")
									 			.getChild("wd:Personal_Data")
									 			.getChild("wd:Contact_Data")
							 					.getChildren("wd:Email_Address_Data");
									 
									 if(emailList != null && emailList.size() >0)
									 {
										 emailArr = "";
										 usageTypeArr = "";
										 
										 for(ReportElement emailElement:emailList)
										 {
											 email = emailElement.getChild("wd:Email_Address") != null?emailElement.getChild("wd:Email_Address").getValue().trim():"";
											 if(emailArr.equals(""))
											 {
												 emailArr = email;
											 }
											 else
											 {
												 emailArr = emailArr + "~" + email;
											 }
											 ReportElement element9 = emailElement.getChild("wd:Usage_Data");
											 if(element9 != null)
											 {
												 ReportElement element10 = element9.getChild("wd:Type_Data");
												 if(element10 != null)
												 {
													 ReportElement element11 = element10.getChild("wd:Type_Reference");
													 if(element11 !=null)
													 {
														 List<ReportElement> usageData = element11.getChildren("wd:ID");					 
														 for(ReportElement wdElement:usageData)
														 {
															 usageMap = wdElement.getAllAttributes();
															 if(usageMap.get("wd:type").equals("Communication_Usage_Type_ID"))
															 {
																 usageType = wdElement.getValue().trim();														
															 }
														 }
													 }
													 else
													 {
														 usageType = ""; 
													 }
												 }
											 }
											 if(usageTypeArr.equals(""))
											 {
												 usageTypeArr = usageType;
											 }
											 else
											 {
												 usageTypeArr = emailArr + "~" + usageType;
											 }
										 }
									 }
									 else
									 {
										 emailArr = "";
										 usageTypeArr = "";
									 }
									 
									 List<ReportElement> phoneList = reportElement.getChild("wd:Applicant_Data")
									 			.getChild("wd:Personal_Data")
									 			.getChild("wd:Contact_Data")
							 					.getChildren("wd:Phone_Data");							
									 
									if(phoneList != null && phoneList.size() >0)
									{
										 countryPhCodeArr = "";
										 areaCodeArr = "";
										 phoneNumberArr = "";
										 phoneDeviceTypeArr = "";
										 usageTypePhoneArr = "";
										 
										for(ReportElement phoneElement : phoneList)
										{
											 countryPhCode = phoneElement.getChild("wd:Country_ISO_Code") != null?phoneElement.getChild("wd:Country_ISO_Code").getValue().trim():"";
											 if(countryPhCodeArr.equals(""))
											 {
												 countryPhCodeArr = countryPhCode;
											 }
											 else
											 {
												 countryPhCodeArr = countryPhCodeArr + "~" + countryPhCode;
											 }
											 areaCode = phoneElement.getChild("wd:Area_Code") != null?phoneElement.getChild("wd:Area_Code").getValue().trim():""; 
											 if(areaCodeArr.equals(""))
											 {
												 areaCodeArr = areaCode;
											 }
											 else
											 {
												 areaCodeArr = areaCodeArr + "~" + areaCode;
											 }
											 phoneNumber = phoneElement.getChild("wd:Phone_Number") != null?phoneElement.getChild("wd:Phone_Number").getValue().trim():""; 
											 if(phoneNumberArr.equals(""))
											 {
												 phoneNumberArr = phoneNumber;
											 }
											 else
											 {
												 phoneNumberArr = phoneNumberArr + "~" + phoneNumber;
											 }
											 
											 ReportElement element13 = phoneElement.getChild("wd:Phone_Device_Type_Reference");
											 if(element13 != null)
											 {
												 List<ReportElement> phDeviceData = element13.getChildren("wd:ID");								 
												 for(ReportElement phDeviceElement:phDeviceData)
												 {
													 phDeviceMap = phDeviceElement.getAllAttributes();
													 if(phDeviceMap.get("wd:type").equals("Phone_Device_Type_ID"))
													 {
														 phoneDeviceType = phDeviceElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 phoneDeviceType = "";
											 }
											 
											 if(phoneDeviceTypeArr.equals(""))
											 {
												 phoneDeviceTypeArr = phoneDeviceType;
											 }
											 else
											 {
												 phoneDeviceTypeArr = phoneDeviceTypeArr + "~" + phoneDeviceType;
											 }
											 
											 ReportElement element14 = phoneElement.getChild("wd:Usage_Data");
											 if(element14 != null)
											 {
												 ReportElement element15 = element14.getChild("wd:Type_Data");
												 if(element15 != null)
												 {
													 ReportElement element16 = element15.getChild("wd:Type_Reference");
													 if(element16 !=null)
													 {
														 List<ReportElement> usageData = element16.getChildren("wd:ID");					 
														 for(ReportElement wdElement:usageData)
														 {
															 phUsageMap = wdElement.getAllAttributes();
															 if(phUsageMap.get("wd:type").equals("Communication_Usage_Type_ID"))
															 {
																 usageTypePhone = wdElement.getValue().trim();
															 }
														 }
													 }
													 else
													 {
														 usageTypePhone = ""; 
													 }
												 }
											 }
											 else
											 {
												 usageTypePhone = "";
											 }
											 if(usageTypePhoneArr.equals(""))
											 {
												 usageTypePhoneArr = usageTypePhone;
											 }
											 else
											 {
												 usageTypePhoneArr = usageTypePhoneArr + "~" + usageTypePhone;
											 }									 									 							 									 
										 }								
									}
									else
									{
										 countryPhCodeArr = "";
										 areaCodeArr = "";
										 phoneNumberArr = "";
										 phoneDeviceTypeArr = "";
										 usageTypePhoneArr = "";
									}
									
									headingFromWD = "Applicant_ID,Applicant_Entered_Date,Country_ISO_Code,Prefix,Legal_First_Name,Legal_Middle_Name,Legal_Last_Name,Legal_Secondary_Name,Suffix,"
									 		+ "Preferred_First_Name,Preferred_Middle_Name,Preferred_Last_Name,Preferred_Secondary_Name,Local_Script,Local_Script_First_Name,Local_Script_Middle_Name,Local_Script_Last_Name,"
									 		+ "Local_Script_Secondary_Name,Local_First_Name_2,Email_Address,Usage_Type_Email,Country_Phone_Code,Area_Code,Phone_Number,"
									 		+ "Phone_Device_Type,Usage_Type_Phone,Country,Applicant_Source_Name,Applicant_Source_Category_Name";
									 
									 headerStr = applicantId + "," + applicantEnteredDate + "," + countryISOCode + "," + prefix + "," + legalFirstName + "," + legalMiddleName + "," + legalLastName + "," + legalSecondaryName + "," +
											 		suffix + "," + preferredFirstName + "," + preferredMiddleName + "," + preferredLastName + "," + preferredSecondaryName + "," + localScript + "," + localFirstName +
											 		"," + localMiddleName + "," + localLastName + "," + localSecondaryName + "," + localFirstName2 + "," + emailArr + ","  + usageTypeArr +
											 		"," + countryPhCodeArr + "," + areaCodeArr + "," + phoneNumberArr + "," + phoneDeviceTypeArr + "," + usageTypePhoneArr + "," + country +  "," + "," +  ",";
									 
									 if(finalStr.equals(""))
									 {
										 finalStr = headingFromWD + "\n" + headerStr;
									 }
									 else
									 {
										 finalStr = finalStr + "\n" + headerStr;
									 }
								 }
							 }
						 }
					 }
					 	wdCount = columnList.size();
					 	columnList.removeAll(errorList);
					 }
				 
					 System.out.println(finalStr);
					 targetContent = finalStr.toString().getBytes();
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Applicant_ID");
					 complete = true;
			 }

		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return headingWd;
	}

	private String addApplicantIdList(String GET_APPLICANT_REQUEST_FILE, List<String> columnList, String ruleName, int startIndex, int endIndex) {
		
		File updatedRequestfile = null;
		try  
		{  
			File file = new File(GET_APPLICANT_REQUEST_FILE);     
			FileReader fr = new FileReader(file);   
			BufferedReader br = new BufferedReader(fr);   
			StringBuffer sb = new StringBuffer();     
			String line; 
			boolean executed = true;
			while((line=br.readLine())!=null)  
			{  
				sb.append(line);  
				sb.append("\n");
				if(line.contains("bsvc:Get_Applicants_Request") && executed)
				{
					executed = false;
					sb.append(" <bsvc:Request_References>"); 
					sb.append("\n");
					for(int i = startIndex;i<endIndex;i++)
					{
						sb.append("  <bsvc:Applicant_Reference bsvc:Descriptor=" + "\"" + "Applicant_ID" + "\"" + ">");
						sb.append("\n");
						sb.append("   <bsvc:ID bsvc:type=" + "\"" + "Applicant_ID" + "\"" + ">" + columnList.get(i) + "</bsvc:ID>");
						sb.append("\n");
						sb.append("  </bsvc:Applicant_Reference>");
						sb.append("\n");
					}
					sb.append(" </bsvc:Request_References>");
					sb.append("\n");
				}
			}  
			fr.close();
			br.close();
			//System.out.println(sb.toString()); 
			
			/*String outputfile = "config/" + ruleName + ".xml";
			PrintWriter writer = new PrintWriter(new File(outputfile));
		    writer.write(sb.toString());
			writer.flush();
			writer.close();*/
			
			updatedRequestfile = File.createTempFile(ruleName + "_" + startIndex , ".xml");
			PrintWriter writer = new PrintWriter(updatedRequestfile);
		    writer.write(sb.toString());
			writer.flush();
			writer.close();
		}  
		catch(IOException e)  
		{  
			e.printStackTrace();  
		}
		return updatedRequestfile.getAbsolutePath(); 
	}
	
	private String addIdListToFindError(String GET_APPLICANT_REQUEST_FILE, String columnVal, String ruleName, String idVal) {
		
		File updatedRequestfile = null;
		try  
		{  
			File file = new File(GET_APPLICANT_REQUEST_FILE);     
			FileReader fr = new FileReader(file);   
			BufferedReader br = new BufferedReader(fr);   
			StringBuffer sb = new StringBuffer();     
			String line; 
			boolean executed = true;
			while((line=br.readLine())!=null)  
			{  
				sb.append(line);  
				sb.append("\n");
				if(line.contains("bsvc:Get_Applicants_Request") && executed)
				{
					executed = false;
					sb.append(" <bsvc:Request_References>"); 
					sb.append("\n");
					sb.append("  <bsvc:Applicant_Reference bsvc:Descriptor=" + "\"" + idVal + "\"" + ">");
					sb.append("\n");
					sb.append("   <bsvc:ID bsvc:type=" + "\"" + "Applicant_ID" + "\"" + ">" + columnVal + "</bsvc:ID>");
					sb.append("\n");
					sb.append("  </bsvc:Applicant_Reference>");
					sb.append("\n");
					
					sb.append(" </bsvc:Request_References>");
					sb.append("\n");
				}
			}  
			fr.close();
			br.close();
			//System.out.println(sb.toString()); 
			
			/*String outputfile = "config/" + ruleName + ".xml";
			PrintWriter writer = new PrintWriter(new File(outputfile));
		    writer.write(sb.toString());
			writer.flush();
			writer.close();*/
			
			updatedRequestfile = File.createTempFile(ruleName + "_" + columnVal , ".xml");
			PrintWriter writer = new PrintWriter(updatedRequestfile);
		    writer.write(sb.toString());
			writer.flush();
			writer.close();
		}  
		catch(IOException e)  
		{  
			e.printStackTrace();  
		}
		return updatedRequestfile.getAbsolutePath(); 
	}
	
	private String addCostCenterIdList(String GET_COST_CENTER_REQUEST_FILE, List<String> columnList, String ruleName, int startIndex, int endIndex, String referenceId) {
		
		File updatedRequestfile = null;
		try  
		{  
			File file = new File(GET_COST_CENTER_REQUEST_FILE);     
			FileReader fr = new FileReader(file);   
			BufferedReader br = new BufferedReader(fr);   
			StringBuffer sb = new StringBuffer();     
			String line; 
			boolean executed = true;
			while((line=br.readLine())!=null)  
			{  
				sb.append(line);  
				sb.append("\n");
				if(line.contains("bsvc:Get_Organizations_Request") && executed)
				{
					executed = false;
					sb.append(" <bsvc:Request_References>"); 
					sb.append("\n");
					for(int i = startIndex;i<endIndex;i++)
					{
						sb.append("  <bsvc:Organization_Reference bsvc:Descriptor=" + "\"" + referenceId + "\"" + ">");
						sb.append("\n");
						sb.append("   <bsvc:ID bsvc:type=" + "\"" + referenceId + "\"" + ">" + columnList.get(i) + "</bsvc:ID>");
						sb.append("\n");
						sb.append("  </bsvc:Organization_Reference>");
						sb.append("\n");
					}
					sb.append(" </bsvc:Request_References>");
					sb.append("\n");
				}
			}  
			fr.close();
			br.close();
			
			updatedRequestfile = File.createTempFile(ruleName + "_" + startIndex , ".xml");
			PrintWriter writer = new PrintWriter(updatedRequestfile);
		    writer.write(sb.toString());
			writer.flush();
			writer.close();
		}  
		catch(IOException e)  
		{  
			e.printStackTrace();  
		}
		return updatedRequestfile.getAbsolutePath(); 
	}
	
	private String addPositionIdList(String GET_POSITION_REQUEST_FILE, List<String> columnList, String ruleName, int startIndex, int endIndex) {
		
		File updatedRequestfile = null;
		try  
		{  
			File file = new File(GET_POSITION_REQUEST_FILE);     
			FileReader fr = new FileReader(file);   
			BufferedReader br = new BufferedReader(fr);   
			StringBuffer sb = new StringBuffer();     
			String line; 
			while((line=br.readLine())!=null)  
			{  
				sb.append(line);  
				sb.append("\n");
				if(line.contains("bsvc:Get_Positions_Request"))
				{
					sb.append(" <bsvc:Request_References>"); 
					sb.append("\n");
					for(int i = startIndex;i<endIndex;i++)
					{
						sb.append("  <bsvc:Positions_Reference bsvc:Descriptor=" + "\"" + "Position_ID" + "\"" + ">");
						sb.append("\n");
						sb.append("   <bsvc:ID bsvc:type=" + "\"" + "Position_ID" + "\"" + ">" + columnList.get(i) + "</bsvc:ID>");
						sb.append("\n");
						sb.append("  </bsvc:Positions_Reference>");
						sb.append("\n");
					}
					sb.append(" </bsvc:Request_References>");
					sb.append("\n");
				}
			}  
			fr.close();
			br.close();	
			
			updatedRequestfile = File.createTempFile(ruleName + "_" + startIndex , ".xml");
			PrintWriter writer = new PrintWriter(updatedRequestfile);
		    writer.write(sb.toString());
			writer.flush();
			writer.close();
		}  
		catch(IOException e)  
		{  
			e.printStackTrace();  
		}
		return updatedRequestfile.getAbsolutePath(); 
	}
	
	private String addHireIdList(String GET_HIRE_REQUEST_FILE, List<String> columnList, String ruleName, int startIndex, int endIndex, String id) {
		
		File updatedRequestfile = null;
		try  
		{  
			File file = new File(GET_HIRE_REQUEST_FILE);     
			FileReader fr = new FileReader(file);   
			BufferedReader br = new BufferedReader(fr);   
			StringBuffer sb = new StringBuffer();     
			String line;
			boolean executed = true;
			while((line=br.readLine())!=null)  
			{  
				sb.append(line);  
				sb.append("\n");
				if(line.contains("bsvc:Get_Workers_Request") && executed)
				{
					executed = false;
					sb.append(" <bsvc:Request_References>"); 
					sb.append("\n");
					for(int i = startIndex;i<endIndex;i++)
					{
						sb.append("  <bsvc:Worker_Reference bsvc:Descriptor=" + "\"" + id + "\"" + ">");
						sb.append("\n");
						sb.append("   <bsvc:ID bsvc:type=" + "\"" + id + "\"" + ">" + columnList.get(i) + "</bsvc:ID>");
						sb.append("\n");
						sb.append("  </bsvc:Worker_Reference>");
						sb.append("\n");
					}
					sb.append(" </bsvc:Request_References>");
					sb.append("\n");
				}
			}  
			fr.close();
			br.close();	
			
			updatedRequestfile = File.createTempFile(ruleName + "_" + startIndex , ".xml");
			PrintWriter writer = new PrintWriter(updatedRequestfile);
		    writer.write(sb.toString());
			writer.flush();
			writer.close();
		}  
		catch(IOException e)  
		{  
			e.printStackTrace();  
		}
		return updatedRequestfile.getAbsolutePath(); 
	}
	
	private String addWorkerIdListToFindError(String xmlFile, String columnVal, String ruleName, String idVal) {
		
		File updatedRequestfile = null;
		try  
		{  
			File file = new File(xmlFile);     
			FileReader fr = new FileReader(file);   
			BufferedReader br = new BufferedReader(fr);   
			StringBuffer sb = new StringBuffer();     
			String line;
			boolean executed = true;
			while((line=br.readLine())!=null)  
			{  
				sb.append(line);  
				sb.append("\n");
				if(line.contains("bsvc:Get_Workers_Request") && executed)
				{
					executed = false;
					sb.append(" <bsvc:Request_References>"); 
					sb.append("\n");
						sb.append("  <bsvc:Worker_Reference bsvc:Descriptor=" + "\"" + idVal + "\"" + ">");
						sb.append("\n");
						sb.append("   <bsvc:ID bsvc:type=" + "\"" + idVal + "\"" + ">" + columnVal + "</bsvc:ID>");
						sb.append("\n");
						sb.append("  </bsvc:Worker_Reference>");
						sb.append("\n");
					
					sb.append(" </bsvc:Request_References>");
					sb.append("\n");
				}
			}
			fr.close();
			br.close();	
			
			updatedRequestfile = File.createTempFile(ruleName + "_" + columnVal , ".xml");
			PrintWriter writer = new PrintWriter(updatedRequestfile);
		    writer.write(sb.toString());
			writer.flush();
			writer.close();
		}  
		catch(IOException e)  
		{  
			e.printStackTrace();  
		}
		return updatedRequestfile.getAbsolutePath(); 
	}
	
	private String addBiographicList(String GET_WORKER_BIOGRAPHIC_FILE, List<String> columnList, String ruleName, int startIndex, int endIndex, List<String> idTypeList) {
		
		File updatedRequestfile = null;
		try  
		{  
			File file = new File(GET_WORKER_BIOGRAPHIC_FILE);     
			FileReader fr = new FileReader(file);   
			BufferedReader br = new BufferedReader(fr);   
			StringBuffer sb = new StringBuffer();     
			String line;
			boolean executed = true;
			while((line=br.readLine())!=null)  
			{  
				sb.append(line);  
				sb.append("\n");
				if(line.contains("bsvc:Get_Workers_Request") && executed)
				{
					executed = false;
					sb.append(" <bsvc:Request_References>"); 
					sb.append("\n");
					for(int i = startIndex;i<endIndex;i++)
					{
						sb.append("  <bsvc:Worker_Reference bsvc:Descriptor=" + "\"" + idTypeList.get(i) + "\"" + ">");
						sb.append("\n");
						sb.append("   <bsvc:ID bsvc:type=" + "\"" + idTypeList.get(i) + "\"" + ">" + columnList.get(i) + "</bsvc:ID>");
						sb.append("\n");
						sb.append("  </bsvc:Worker_Reference>");
						sb.append("\n");
					}
					sb.append(" </bsvc:Request_References>");
					sb.append("\n");
				}
			}  
			fr.close();
			br.close();	
			
			updatedRequestfile = File.createTempFile(ruleName + "_" + startIndex , ".xml");
			PrintWriter writer = new PrintWriter(updatedRequestfile);
		    writer.write(sb.toString());
			writer.flush();
			writer.close();
		}  
		catch(IOException e)  
		{  
			e.printStackTrace();  
		}
		return updatedRequestfile.getAbsolutePath(); 
	}
	
	private String addWorkerPhotoList(String GET_WORKER_PHOTO_FILE, List<String> columnList, String ruleName, int startIndex, int endIndex, List<String> idTypeList) {
		
		File updatedRequestfile = null;
		try  
		{  
			File file = new File(GET_WORKER_PHOTO_FILE);     
			FileReader fr = new FileReader(file);   
			BufferedReader br = new BufferedReader(fr);   
			StringBuffer sb = new StringBuffer();     
			String line;
			boolean executed = true;
			while((line=br.readLine())!=null)  
			{  
				sb.append(line);  
				sb.append("\n");
				if(line.contains("bsvc:Get_Worker_Photos_Request") && executed)
				{
					executed = false;
					sb.append(" <bsvc:Request_References>"); 
					sb.append("\n");
					for(int i = startIndex;i<endIndex;i++)
					{
						sb.append("  <bsvc:Worker_Reference bsvc:Descriptor=" + "\"" + idTypeList.get(i) + "\"" + ">");
						sb.append("\n");
						sb.append("   <bsvc:ID bsvc:type=" + "\"" + idTypeList.get(i) + "\"" + ">" + columnList.get(i) + "</bsvc:ID>");
						sb.append("\n");
						sb.append("  </bsvc:Worker_Reference>");
						sb.append("\n");
					}
					sb.append(" </bsvc:Request_References>");
					sb.append("\n");
				}
			}  
			fr.close();
			br.close();	
			
			updatedRequestfile = File.createTempFile(ruleName + "_" + startIndex , ".xml");
			PrintWriter writer = new PrintWriter(updatedRequestfile);
		    writer.write(sb.toString());
			writer.flush();
			writer.close();
		}  
		catch(IOException e)  
		{  
			e.printStackTrace();  
		}
		return updatedRequestfile.getAbsolutePath(); 
	}

	private void createSOAPHeader(SOAPHeader soapHeader, String sourceTenantName, String sourceTenantUser, String sourceTenantPwd) throws SOAPException {
		
		QName security = soapHeader.createQName("Security", HEADER_SECURITY_NS_PREFIX);
		SOAPHeaderElement headerElement = soapHeader.addHeaderElement(security);
		SOAPElement usernameToken = headerElement.addChildElement("UsernameToken", HEADER_SECURITY_NS_PREFIX);
		SOAPElement username = usernameToken.addChildElement("Username",HEADER_SECURITY_NS_PREFIX);
		username.addTextNode(sourceTenantUser + "@"	+ sourceTenantName);
		SOAPElement password = usernameToken.addChildElement("Password",HEADER_SECURITY_NS_PREFIX);
		password.addTextNode(sourceTenantPwd);
		password.addAttribute(new QName("Type"), PASSWORD_TYPE_ATTR_VALUE);
	}
	
	/*private void iterateRequest(Iterator itr, Integer pageNumber) {
		while (itr.hasNext()) {
			Node node=(Node)itr.next();
			while (node.hasChildNodes()) {
				NodeList childNodes = node.getChildNodes();
				getNodeValue(childNodes, pageNumber);
				if (count > 0) {
					break;
				}
			}
			if (count > 0) {
				break;
			}
		}
	}
	
	private void getNodeValue(NodeList childNodes, Integer pageNumber) {
		if (childNodes != null) {
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node aChildNode = childNodes.item(i);
				if (aChildNode.getNodeType() == Node.ELEMENT_NODE) {
					Element ele = (Element) aChildNode;
					if (ele.hasChildNodes()) {
						NodeList childNodeList = ele.getChildNodes();
						for (int j = 0; j < childNodeList.getLength(); j++) {
							Node nNode = childNodeList.item(j);
							if (nNode.getNodeName().contains("Page")) {
								nNode.getFirstChild().setNodeValue(String.valueOf(pageNumber));
								System.out.println("value = "+nNode.getFirstChild().getNodeValue());
								count++;
								break;
							}
						}
						if (count > 0) {
							break;
						}
						getNodeValue(childNodeList, pageNumber);
					}
					//System.out.println("node = " + ele.getNodeName());
				}
			}
		}
	}*/
	
	@RequestMapping(value = "executeSourceFile/{fileName}/{loadCycle}/{ruleName}/{tenantId}", method = RequestMethod.POST)
	public JSONArray executeSourceFile(@PathVariable("fileName")String fileName, @PathVariable("loadCycle") final String loadCycle,  @PathVariable("ruleName") String ruleName, 
			@PathVariable("tenantId") Long tenantId, @RequestParam("sourceFile") MultipartFile sourceFile, HttpSession httpSession) {
		
		//Map<String, byte[]> contentMap = new HashMap<>();
		complete = false;
		sourceContent = null;
		ccMap.clear();
		headingFromSource = "";
		headingFromWD = "";
		columnList.clear();
		errorList.clear();
		//postloadErrorList.clear();
		wdColumnList.clear();
		idTypeList.clear();
		tenant = tenantService.getTenant(tenantId);
		InputStream is = null;
		SOAPConnection soapConnection = null;
		int startIndex = 0;
		int endIndex = 0;
		headingWd = null;
		System.out.println("fileName-"+fileName);
		byte[] mapFileData = null;
		String str = null;		
		 
		try 
		{
			mapFileData = sourceFile.getBytes();
			if(fileName.contains(".csv"))
			{
				str = new String(mapFileData, "UTF-8");
			}
			else
			{
				InputStream in = sourceFile.getInputStream();
				File sourceExcelFile = File.createTempFile(fileName.substring(0, fileName.indexOf(".")), ".xlsx");
			    String excelPath = sourceExcelFile.getAbsolutePath();
			    FileOutputStream f = new FileOutputStream(excelPath);
			    int ch = 0;
			    while ((ch = in.read()) != -1) 
			    {
			        f.write(ch);
			    }
			    f.flush();
			    f.close();
			    
			    InputStream inp = new FileInputStream(excelPath);
			    Workbook wb = WorkbookFactory.create(inp);
			    str = convertExcelToCSV(wb.getSheetAt(4), fileName); 
			}
			System.out.println(str);
			String line = "";
			int count = 0;
	        BufferedReader reader = new BufferedReader(new StringReader(str));
	        while ((line = reader.readLine()) != null) 
	        { 
	            if(count != 0 && line.length() > 0) 
	            {
	            	if(ruleName.equalsIgnoreCase("Cost Center"))
	            	{
	            		columnList.add(line.substring(0, line.indexOf(",")));
	            		String [] lineArr = line.split(",");
	            		if(lineArr.length >0)
	            		{
	            			ccMap.put(lineArr[0], lineArr[3]);
	            		}
	            	}
	            	else
	            	{
	            		//columnList.add(line.substring(0, line.indexOf(",")));
	            		String [] lineArr = line.split(",");
	            		if(lineArr.length >0)
	            		{
		            		//if(!columnList.contains(lineArr[0]))
		            		{
		            			columnList.add(lineArr[0]);
		            		}
	            		}
	            	}	            	
	            }
	            count++;
	        }
			//addPrimaryId(lines, columnList);
			String[] result = str.split("\\R", 2);
			System.out.println(result[0]);
			headingFromSource = result[0];
			sourceContent = str.toString().getBytes();
			
			Page page = pageService.getPage(Long.parseLong(loadCycle));
			String loadCycle1 = page.getPageName();
			Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
			User user = userService.getUser(userId);
			
			if(ruleName.equalsIgnoreCase("Cost Center"))
			{
				headingWd = createCSVFromWDCostCenter(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("Put_Applicant"))
			{				
				Thread t = new Thread( ) {
					public void run() {
						headingWd = createCSVFromWDApplicantMultiplePh(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
					}
				};
				t.start();
			}
			else if(ruleName.equalsIgnoreCase("Hire_Employee"))
			{
				Thread t = new Thread( ) {
					public void run() {
						headingWd = createCSVFromWDHire(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
					}
				};
				t.start();
				//headingWd = createCSVFromWDHire(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("Create_Position") || ruleName.equalsIgnoreCase("Edit_Position"))
			{
				Thread t = new Thread( ) {
					public void run() {
						headingWd = createCSVFromWDPosition(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
					}
				};
				t.start();				
			}
			else if(ruleName.equalsIgnoreCase("Terminate_Employee"))
			{
				Thread t = new Thread( ) {
					public void run() {
						headingWd = createCSVFromWDTermination(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
					}
				};
				t.start();
//				headingWd = createCSVFromWDTermination(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("Change_Government_IDs"))
			{
				headingWd = createCSVFromWDGovernmentId(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("Put_Worker_Photo"))
			{
				headingWd = createCSVFromWDWorkerPhoto(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("Supervisory Organization"))
			{
				Thread t = new Thread( ) {
					public void run() {
						headingWd = createCSVFromWDSupervisoryOrganization(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
					}
				};
				t.start();				
			}
			else if(ruleName.equalsIgnoreCase("Cost Center Hierarchy"))
			{
				headingWd = createCSVFromWDCostCenterHierarchy(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("Company Hierarchy"))
			{
				headingWd = createCSVFromWDCompanyHierarchy(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("Applicant Phone"))
			{
				headingWd = createCSVFromWDApplicantPhone(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("Worker Address"))
			{
				headingWd = createCSVFromWDWorkerAddress(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("Hire CW"))
			{
				headingWd = createCSVFromWDHireCW(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("End Contingent Worker"))
			{
				headingWd = createCSVFromWDEndContingentWorker(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("Worker Biographic"))
			{
				headingWd = createCSVFromWDWorkerBiographic(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("Worker Demographic"))
			{
				headingWd = createCSVFromWDWorkerDemographic(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("Service Dates"))
			{
				headingWd = createCSVFromWDWServiceDates(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("Leave of Absence"))
			{
				headingWd = createCSVFromWDLeaveOfAbsence(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("Compensation Details"))
			{
				headingWd = createCSVFromWDEEBaseCompensation(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("Bonus Plan"))
			{
				headingWd = createCSVFromWDBonusPlan(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("Allowance Plan"))
			{
				headingWd = createCSVFromWDAllowancePlan(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("Stock Plan"))
			{
				headingWd = createCSVFromWDStockPlan(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("Merit Plan"))
			{
				headingWd = createCSVFromWDMeritPlan(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("Pay Group"))
			{
				headingWd = createCSVFromWDAssignPayGroup(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("Performance Review"))
			{
				headingWd = createCSVFromWDPerformanceReview(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("Employee ID"))
			{
				headingWd = createCSVFromWDEmployeeId(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("SupOrg-Matrix Manager"))
			{
				Thread t = new Thread( ) {
					public void run() {
						headingWd = createCSVFromWDOrganizationRoles(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
					}
				};
				t.start();					
			}
			else if(ruleName.equalsIgnoreCase("Host CNUM"))
			{				
				Thread t = new Thread( ) {
					public void run() {
						headingWd = createCSVFromWDHostCNUM(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
					}
				};
				t.start();
			}
			else if(ruleName.equalsIgnoreCase("Carryover Balance"))
			{
				Thread t = new Thread( ) {
					public void run() {
						headingWd = createCSVFromWDCarryoverBalances(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
					}
				};
				t.start();
			}
			else if(ruleName.equalsIgnoreCase("System User Account"))
			{
				headingWd = createCSVFromWDSystemUserAccount(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
			}
			else if(ruleName.equalsIgnoreCase("Matrix Organization"))
			{
				Thread t = new Thread( ) {
					public void run() {
						headingWd = createCSVFromWDMatrixOrganization(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
					}
				};
				t.start();	
			}
			else if(ruleName.equalsIgnoreCase("Assign Organization"))
			{
				Thread t = new Thread( ) {
					public void run() {
						headingWd = createCSVFromWDAssignOrganization(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
					}
				};
				t.start();	
			}
			else if(ruleName.equalsIgnoreCase("Edit Worker Additional Data"))
			{
				Thread t = new Thread( ) {
					public void run() {
						headingWd = createCSVFromWDEditWorkerAddnData(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
					}
				};
				t.start();				
			}
			else if(ruleName.equalsIgnoreCase("Start International Assignment"))
			{
				Thread t = new Thread( ) {
					public void run() {
						headingWd = createCSVFromWDStartIntlAssignment(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
					}
				};
				t.start();				
			}
			else if(ruleName.equalsIgnoreCase("End International Assignment"))
			{
				Thread t = new Thread( ) {
					public void run() {
						headingWd = createCSVFromWDEndIntlAssignment(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
					}
				};
				t.start();				
			}
			else if(ruleName.equalsIgnoreCase("Employee Contract"))
			{
				Thread t = new Thread( ) {
					public void run() {
						headingWd = createCSVFromWDEmployeeContract(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
					}
				};
				t.start();					
			}
			else if(ruleName.equalsIgnoreCase("Payee Tax Code Data"))
			{
				Thread t = new Thread( ) {
					public void run() {
						headingWd = createCSVFromWDPayeeTaxCodeData(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
					}
				};
				t.start();					
			}
			else if(ruleName.equalsIgnoreCase("UK Payroll ID"))
			{
				Thread t = new Thread( ) {
					public void run() {
						headingWd = createCSVFromWDUKPayrollID(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
					}
				};
				t.start();					
			}
			else if(ruleName.equalsIgnoreCase("Payroll Payee NI Data"))
			{
				Thread t = new Thread( ) {
					public void run() {
						headingWd = createCSVFromWDPayrollPayeeNIData(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
					}
				};
				t.start();					
			}
			else if(ruleName.equalsIgnoreCase("Change Benefits Life Events"))
			{
				Thread t = new Thread( ) {
					public void run() {
						headingWd = createCSVFromWDChangeBenefitsLifeEvents(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
					}
				};
				t.start();					
			}
			else if(ruleName.equalsIgnoreCase("Payee Input Data"))
			{
				Thread t = new Thread( ) {
					public void run() {
						headingWd = createCSVFromWDPayeeInputData(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
					}
				};
				t.start();					
			}
			else if(ruleName.equalsIgnoreCase("Assign Establishment"))
			{
				Thread t = new Thread( ) {
					public void run() {
						headingWd = createCSVFromWDEstablishment(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
					}
				};
				t.start();					
			}
			else if(ruleName.equalsIgnoreCase("Location"))
			{
				Thread t = new Thread( ) {
					public void run() {
						headingWd = createCSVFromWDLocation(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle1, ruleName, user.getClient());
					}
				};
				t.start();					
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}

	@RequestMapping(value = "executeFilePostLoad/{operationName}/{loadCycle}/{fileName}", method = RequestMethod.POST)
	public JSONArray executeFilePostLoad(@PathVariable("operationName")String operationName,  @PathVariable("loadCycle")String loadCycle, @PathVariable("fileName")String fileName,
			@RequestParam("myFile") MultipartFile myFile, HttpSession httpSession) {
		
		headingFromSource = "";
		headingFromWD = "";
		columnList.clear();
		idTypeList.clear();
		System.out.println("operationName-"+operationName);
		System.out.println("loadCycle-"+loadCycle);
		System.out.println("fileName-"+fileName);
		tenant = tenantService.getTenant(2L);
		InputStream is = null;
		SOAPConnection soapConnection = null;
		int startIndex = 0;
		int endIndex = 0;
		JSONArray headingWd = null;
		byte[] mapFileData = null;
		String str = null;
		try 
		{
			mapFileData = myFile.getBytes();
			if(fileName.endsWith(".csv"))
			{
				str = new String(mapFileData, "UTF-8");
			}
			else
			{
				InputStream in = myFile.getInputStream();
				File sourceExcelFile = File.createTempFile(fileName.substring(0, fileName.indexOf(".")), ".xlsx");
			    String excelPath = sourceExcelFile.getAbsolutePath();
			    FileOutputStream f = new FileOutputStream(excelPath);
			    int ch = 0;
			    while ((ch = in.read()) != -1) 
			    {
			        f.write(ch);
			    }
			    f.flush();
			    f.close();
			    
			    InputStream inp = new FileInputStream(excelPath);
			    Workbook wb = WorkbookFactory.create(inp);
			    str = convertExcelToCSV(wb.getSheetAt(4), fileName); //change made
			}
			System.out.println(str);
			String line = "";
			int count = 0;
	        BufferedReader reader = new BufferedReader(new StringReader(str));
	        while ((line = reader.readLine()) != null) 
	        { 
	        	if(count != 0 && line.length() > 0) 
	            {
	        		if(operationName.equalsIgnoreCase("Change_Government_IDs"))
	            	{
	        			String [] lineArr = line.split(",");
	            		if(lineArr.length >0)
	            		{
		            		if(!columnList.contains(lineArr[0]))
		            		{
		            			columnList.add(lineArr[0]);
		            		}
	            		}
	            	}
	        		else if(operationName.equalsIgnoreCase("Put_Worker_Photo"))
	            	{
	        			String [] lineArr = line.split(",");
	            		if(lineArr.length >1)
	            		{
	            			if(lineArr[1] != null && lineArr[1].length() >0)
		            		{
		            			columnList.add(lineArr[0]);
		            			idTypeList.add(lineArr[1]);
		            		}
	            		}
	            	}
	        		else if(operationName.equalsIgnoreCase("Terminate_Employee"))
		        	{
	            		String [] lineArr = line.split(",");
	            		if(lineArr.length >1)
	            		{
		            		if(lineArr[1] != null && lineArr[1].length() >0)
		            		{
		            			columnList.add(line.substring(0, line.indexOf(",")));
		            		}
	            		}
		        	}
	        		else
	        		{
	        			columnList.add(line.substring(0, line.indexOf(",")));
	        		}
	            }
	        	count++;
	        }
	        System.out.println("columnList-"+columnList);
	        System.out.println("columnList.size()-"+columnList.size());
			String[] result = str.split("\\R", 2);
			headingFromSource = result[0];
			System.out.println(headingFromSource);
			
			File sourceCSVfile = File.createTempFile(fileName.substring(0, fileName.indexOf(".")), ".csv");
			PrintWriter writer = new PrintWriter(sourceCSVfile);
		    writer.write(str.toString());
			writer.flush();
			writer.close();
			
			Page page = pageService.getPage(Long.parseLong(loadCycle));
			loadCycle = page.getPageName();
			Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
			User user = userService.getUser(userId);
			
			PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, operationName);
			if(postLoad == null)
			{
				postLoad = new PostLoad();
				postLoad.setLoadCycle(loadCycle);
				postLoad.setRuleName(operationName);
				postLoad.setSrcCSVFileName(fileName);
				postLoad.setSrcCSVFileContent(Files.readAllBytes(sourceCSVfile.toPath()));
				postLoad.setUserId(userId);
				postLoad.setClient(user.getClient());
				postLoadService.addPostLoad(postLoad);
			}
			else
			{
				postLoad.setSrcCSVFileContent(Files.readAllBytes(sourceCSVfile.toPath()));
				postLoad.setClient(user.getClient());
				postLoadService.updatePostLoad(postLoad);
			}
			
			if(operationName.equalsIgnoreCase("Put_Applicant"))
			{
				headingWd = createCSVFromWDApplicant(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle, operationName, user.getClient());
			}
			else if(operationName.equalsIgnoreCase("Create_Position") || operationName.equalsIgnoreCase("Edit_Position"))
			{
				headingWd = createCSVFromWDPosition(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle, operationName, user.getClient());
			}
			else if(operationName.equalsIgnoreCase("Hire_Employee"))
			{
				headingWd = createCSVFromWDHire(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle, operationName, user.getClient());
			}
			else if(operationName.equalsIgnoreCase("Change_Government_IDs"))
			{
				headingWd = createCSVFromWDGovernmentId(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle, operationName, user.getClient());
			}
			else if(operationName.equalsIgnoreCase("Put_Worker_Photo"))
			{
				headingWd = createCSVFromWDWorkerPhoto(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle, operationName, user.getClient());
			}
			else if(operationName.equalsIgnoreCase("Terminate_Employee"))
			{
				headingWd = createCSVFromWDTermination(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle, operationName, user.getClient());
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}
	
	private String convertExcelToCSV(Sheet sheet, String fileName) {
		
		StringBuilder data = new StringBuilder();
        String[] nextRecord = null;
		String cellValue = "";
		String csvValue = "";
        try 
        {
    		File sourceCsvFile = File.createTempFile(fileName.substring(0, fileName.indexOf(".")), ".csv");
    		int maxNumOfCells = sheet.getRow(1).getLastCellNum();//change made=0
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) 
            {
            	Row row = rowIterator.next();
            	if(row.getRowNum() >=10)//change made
            	{
	                for( int cellCounter = 1 ; cellCounter < maxNumOfCells; cellCounter ++)//change made=0
	                {
	                	Cell cell = null;
	                    if( row.getCell(cellCounter) == null)
	                    {
	                    	data.append(",");
	                    } 
	                    else 
	                    {
	                        cell = row.getCell(cellCounter);
	                        if(cell.getCellType().toString().equals("STRING"))
	                        {
	                        	if(cell.getStringCellValue().contains(","))
	                        	{
	                        		data.append(cell.getStringCellValue().replace(",", "|"));
	                        	}
	                        	else
	                        	{
	                        		data.append(cell.getStringCellValue());
	                        	}
	                        }
	                        else if(cell.getCellType().toString().equals("NUMERIC"))
	                        {
	                        	if(String.valueOf(cell.getNumericCellValue()).contains("E"))
	                        	{
	                        		Object obj = cell.getNumericCellValue();
		                        	data.append(new BigDecimal(obj.toString()).toPlainString());
	                        	}
	                        	else if(String.valueOf(cell.getNumericCellValue()).contains(".0") && !String.valueOf(cell.getNumericCellValue()).contains("E"))
	                        	{
	                        		data.append(String.valueOf(cell.getNumericCellValue()).substring(0, String.valueOf(cell.getNumericCellValue()).indexOf(".")));
	                        	}
	                        	else
	                        	{
	                        		data.append(String.valueOf(cell.getNumericCellValue()));
	                        	}
	                        }
	                        else if(cell.getCellType().toString().equals("BOOLEAN"))
	                        {
	                        	data.append(cell.getBooleanCellValue());
	                        }
	                        data.append(",");
	                    }                    
	                }
	                data.append('\n');
            	}
            }

            Files.write(Paths.get(sourceCsvFile.getAbsolutePath()), data.toString().getBytes("UTF-8"));
            
			FileReader filereader = new FileReader(sourceCsvFile.getAbsolutePath());
			CSVReader csvReader = new CSVReader(filereader); 
            while ((nextRecord = csvReader.readNext()) != null) 
            {
            	cellValue = "";
                for (String cell : nextRecord) 
                {
                	if(cellValue.equals(""))
                	{
                		cellValue = cell;
                	}
                	else
                	{
                		cellValue = cellValue + "," + cell;
                	}	                	 
                }	                
            	if(csvValue.equals(""))
            	{
            		csvValue = cellValue;           		
            	}
            	else
            	{
            		csvValue = csvValue + "\n" + cellValue; 
            	}
            }
            csvReader.close();
        } 
        catch (FileNotFoundException e) 
        {
            e.printStackTrace();	
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
		return csvValue;
		
	}

	public JSONArray selectColumnMapping(String loadCycle, String ruleName, String identifier) {
		
		JSONArray headingSource = new JSONArray();
		JSONArray headingWd = new JSONArray();
		String heading = "";
		
		/*headingFromWD = "Applicant_ID,Applicant_Entered_Date,Country_ISO_Code,Prefix,Legal_First_Name,Legal_Middle_Name,Legal_Last_Name,Legal_Secondary_Name,Suffix,"
		 		+ "Preferred_First_Name,Preferred_Middle_Name,Preferred_Last_Name,Preferred_Secondary_Name,Local_Script,Local_First_Name,Local_Middle_Name,Local_Last_Name,"
		 		+ "Local_Secondary_Name,Local_First_Name2,Email_Address,Primary_Email,Visibility_Email,Usage_Type_Email";*/
		
		try 
		{
			String[] headingFromWdArr = headingFromWD.split(",");
			for(int i = 0;i<headingFromWdArr.length; i++)
			{
				JSONObject objWd = new JSONObject();
				objWd.put("headingWD", headingFromWdArr[i]);
				headingWd.put(objWd);
			}
			
			String[] headingFromSourceArr = headingFromSource.split(",");
			for(int j = 0;j<headingFromSourceArr.length; j++)
			{
				JSONObject objSrc = new JSONObject();
				heading = findHeadingName(headingFromSourceArr[j], headingWd);
				if(heading.equalsIgnoreCase(identifier))
				{
					objSrc.put("isChecked", true);
				}
				else
				{
					objSrc.put("isChecked", false);
				}
				objSrc.put("headingSource", headingFromSourceArr[j]);
				objSrc.put("headingAllWD", headingWd);
				objSrc.put("heading", heading);
				objSrc.put("isSelect", false);
				headingSource.put(objSrc);
			}
		} 
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return headingSource;
	}

	private String findHeadingName(String sourceHeader, JSONArray headingWd) {

		String headingWD = "";
		String headingWDWOU = "";
		String retStr = "";
		for (int i = 0; i < headingWd.length(); i++) 
		{            
			try 
			{
				headingWD = headingWd.getJSONObject(i).getString("headingWD");
				headingWDWOU = headingWD.replaceAll("_", " ");
				if(headingWDWOU.equalsIgnoreCase(sourceHeader.replaceAll("_", " ")))
				{
					retStr = headingWD;
					break;
				}
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
            //System.out.println(headingWD);
        }
		return retStr;
	}

	public void executeMappingHeader(String paramValue, String loadCycle, String ruleName, String client) {
		
		sourceEntryMap.clear();
		//errorList.clear();
		String newSource = "";
		String[] paramValueArr = paramValue.split(",");
		String[] headingFromSourceArr = headingFromSource.split(",");
		String str = "";
		String newStr = "";
		for(int i = 0;i<headingFromSourceArr.length; i++)
		{
			for(int j = 0;j<paramValueArr.length; j++)
			{
				if(paramValueArr[j].substring(0, paramValueArr[j].indexOf("|")).equalsIgnoreCase(headingFromSourceArr[i]))
				{
					str = paramValueArr[j].substring(paramValueArr[j].indexOf("|")+1, paramValueArr[j].length());
					if(str.equals(""))
					{
						str = paramValueArr[j].substring(0, paramValueArr[j].indexOf("|"));
						if(str.indexOf(" ") > 1)
						{
							String [] strArr = str.split(" ");
							for(int k = 0;k<strArr.length;k++)
							{
								newStr = newStr + strArr[k];								
							}
							str = newStr;
						}
					}
					if(newSource.equals(""))
					{
						newSource = str;
						break;
					}
					else
					{
						newSource = newSource + "," + str;
						str = "";
						newStr = "";
						break;
					}
				}
			}
		}
		
		//PostLoad postLoad = postLoadService.getPostLoadByLoadRuleClient(loadCycle, ruleName, client);
		//byte[] srcCSVFileContent = postLoad.getSrcCSVFileContent();
		byte[] srcCSVFileContent = sourceContent;
		File sourceCSVfile = null;
		try 
		{
			sourceCSVfile = File.createTempFile(ruleName + "_Source", ".csv");
			FileUtils.writeByteArrayToFile(sourceCSVfile, srcCSVFileContent);
		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}
		String file = sourceCSVfile.getAbsolutePath();
		
		//String file = "config/Put_Applicant - Source.csv";
		try 
		{
			FileReader filereader = new FileReader(file);
			CSVReader csvReader = new CSVReader(filereader); 
            String[] nextRecord;
            int count = 0;
			String cellValue = "";
            while ((nextRecord = csvReader.readNext()) != null) 
            {
            	cellValue = "";
            	if(count != 0)
            	{
	                for (String cell : nextRecord) 
	                {
	                	if(cellValue.equals(""))
	                	{
	                		cellValue = cell;
	                	}
	                	else
	                	{
	                		cellValue = cellValue + "," + cell;
	                	}	                	 
	                }	                
            	}
            	count++;
            	if(!cellValue.equals(""))
            	{
            		newSource = newSource + "\n" + cellValue; 
            	}
            }
            csvReader.close();
            sourceCount = count - 1;
            
            String[] sourceResult = newSource.split("\\R", 2);
            String resultNext = sourceResult[1];
			String[] resultNextArr = resultNext.split("\n");
			for(int a = 0;a<resultNextArr.length;a++)
			{
				if(errorList.contains(resultNextArr[a].substring(0, resultNextArr[a].indexOf(","))))
				{
					sourceEntryMap.put(resultNextArr[a].substring(0, resultNextArr[a].indexOf(",")), resultNextArr[a].substring(resultNextArr[a].indexOf(",")+1, resultNextArr[a].length()));
				}				
			}
			System.out.println(sourceEntryMap);
			sourceContent = null;
			sourceContent = newSource.toString().getBytes();
			
			/*File updatedSourceCSVfile = File.createTempFile(sourceCSVfile.getName().substring(0, sourceCSVfile.getName().indexOf(".")), ".csv");
			PrintWriter writer = new PrintWriter(updatedSourceCSVfile);
		    writer.write(newSource.toString());
			writer.flush();
			writer.close();
			
			postLoad.setSrcCSVFileNameNew(sourceCSVfile.getName().substring(0, sourceCSVfile.getName().indexOf(".")) + ".csv");;
			postLoad.setSrcCSVFileContentNew(Files.readAllBytes(updatedSourceCSVfile.toPath()));
			postLoadService.updatePostLoad(postLoad);*/			
			
			byte[] wdCSVFileContent = targetContent;
			byte[] srcCSVFileContentNew = sourceContent;

			File wdCSVfile = null;
			File srcCSVfileNew = null;
			try 
			{
				wdCSVfile = File.createTempFile(ruleName + "_Target", ".csv");
				FileUtils.writeByteArrayToFile(wdCSVfile, wdCSVFileContent);
				
				srcCSVfileNew = File.createTempFile(ruleName + "_SourceNew", ".csv");
				FileUtils.writeByteArrayToFile(srcCSVfileNew, srcCSVFileContentNew);
			} 
			catch (IOException e1) 
			{
				e1.printStackTrace();
			}
			File wdFile = new File(wdCSVfile.getAbsolutePath());
			File srcFile = new File(srcCSVfileNew.getAbsolutePath());
			
			File wdXMLFile = File.createTempFile("WorkdayXML", ".xml");
			File srcXMLFile = File.createTempFile("SourceXML", ".xml");
			
			String wdXML = wdXMLFile.getAbsolutePath();
			String srcXML = srcXMLFile.getAbsolutePath();
			
			convertCSVToXML(wdFile, wdXML, loadCycle, ruleName, 1, client);
			convertCSVToXML(srcFile, srcXML, loadCycle, ruleName, 2, client);
			
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	private void convertCSVToXML(File file, String filename, String loadCycle, String ruleName, int i, String client) {

		ArrayList<String> info = new ArrayList<String>(7);

	    BufferedReader readFile = null;
	    try 
	    {
	        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = df.newDocumentBuilder();

	        Document document = db.newDocument();

	        Element rootElement = document.createElement("Root");

	        document.appendChild(rootElement);
	        readFile = new BufferedReader(new FileReader(file));
	        int line = 0;

	        String information = null;
	        while ((information = readFile.readLine()) != null) 
	        {
	            String[] rowValues = information.split(",");
	            if (line == 0) 
	            {
	                for (String columnInfo : rowValues) 
	                {
	                	info.add(columnInfo);
	                }
	            } 
	            else 
	            {
	                Element childElement = document.createElement("details");
	                rootElement.appendChild(childElement);
	                for (int columnInfo = 0; columnInfo < info.size(); columnInfo++) 
	                {
	                    String header = info.get(columnInfo);
	                    String value = null;

	                    if (columnInfo < rowValues.length) 
	                    {
	                        value = rowValues[columnInfo];
	                        if(value.contains("|"))
	                        {
	                        	value = value.replace("|", "?");
	                        }
	                    } 
	                    else 
	                    {
	                        value = " ";
	                    }
	                    Element current = document.createElement(header);
	                    current.appendChild(document.createTextNode(value));
	                    childElement.appendChild(current);
	                    System.out.println(value);
	                }
	            }
	            line++;
	        }
			Transformer tf = TransformerFactory.newInstance().newTransformer();
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			Writer output = new StringWriter();
			tf.transform(new DOMSource(document), new StreamResult(output));
			
			//PostLoad postLoad = postLoadService.getPostLoadByLoadRuleClient(loadCycle, ruleName, client);
			if(i == 1)
			{
				//postLoad.setWdXMLFileName(ruleName + "_" + "WD.xml");
				//postLoad.setWdXMLFileContent(output.toString().getBytes());
				targetXMLContent = output.toString().getBytes();
			}
			else
			{
				//postLoad.setSrcXMLFileName(ruleName + "_" + "SRC.xml");
				//postLoad.setSrcXMLFileContent(output.toString().getBytes());
				sourceXMLContent = output.toString().getBytes();
			}
			//postLoadService.updatePostLoad(postLoad);
			
		    /*FileWriter fw = new FileWriter(filename,true);
		    fw.write(output.toString() + "\n");
		    fw.close();*/
		} 
	    catch (Exception e) 
	    {

	    }
	}
	
	@RequestMapping(value = "/performComparison/{primaryKey}/{colValues}/{loadCycle}/{ruleName}/{rowValues}", 
	        method = RequestMethod.GET, headers = "Accept=application/json")
	public void performComparison(@PathVariable("primaryKey") String primaryKey,  @PathVariable("colValues") String colValues, @PathVariable("loadCycle") String loadCycle,  
			@PathVariable("ruleName") String ruleName, @PathVariable("rowValues") String rowValues, HttpServletResponse response, HttpSession httpSession) {
		
		System.out.println("primaryKey-"+primaryKey);
		System.out.println("colValues-"+colValues);
		System.out.println("rowValues-"+rowValues);
		
		sourceXMLContent = null;
		targetXMLContent = null;
		
		Page page = pageService.getPage(Long.parseLong(loadCycle));
		loadCycle = page.getPageName();
		
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		
		//PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);		
		//if(postLoad.getWdXMLFileContent() == null && postLoad.getSrcXMLFileContent() == null)
		//{
			executeMappingHeader(rowValues, loadCycle, ruleName, user.getClient());
			//PostLoad postLoad = postLoadService.getPostLoadByLoadRuleClient(loadCycle, ruleName, user.getClient());
		//}
		
		String commonCol = primaryKey;
		
		//byte[] wdXMLFileContent = postLoad.getWdXMLFileContent();
		//byte[] srcXMLFileContent = postLoad.getSrcXMLFileContent();
		byte[] wdXMLFileContent = targetXMLContent;
		byte[] srcXMLFileContent = sourceXMLContent;
		
		File wdXMLfile = null;
		File srcXMLfile = null;
		try 
		{
			wdXMLfile = File.createTempFile(ruleName + "_Target", ".xml");
			FileUtils.writeByteArrayToFile(wdXMLfile, wdXMLFileContent);
			
			srcXMLfile = File.createTempFile(ruleName + "_Source", ".xml");
			FileUtils.writeByteArrayToFile(srcXMLfile, srcXMLFileContent);
		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}
		
		//String xmlFile1 = "config/Test1.xml";
		//String xmlFile2 = "config/Test2.xml";
		
		String xmlFile1 = wdXMLfile.getAbsolutePath();
		String xmlFile2 = srcXMLfile.getAbsolutePath();
		
		MultiValuedMap<String, List<String>> firstXML = null;
		MultiValuedMap<String, List<String>> secondXML = null;
		try 
		{
			if(commonCol.contains(":"))
			{
				//firstXML = extractXMLFileMultipleRow(xmlFile1, commonCol);			
				//secondXML = extractXMLFileMultipleRow(xmlFile2, commonCol);
			}
			else
			{
				firstXML =  extractXMLFile(xmlFile1, commonCol);			
				secondXML = extractXMLFile(xmlFile2, commonCol);
			}
			String [] fields = colValues.split(",");
			compareXML(firstXML, secondXML, fields, response, commonCol, ruleName, rowValues, "File");
			//headingFromWD = "";
			//headingFromSource = "";
			//finalCSVHeader = "";
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (XMLStreamException e) 
		{
			e.printStackTrace();
		}
	}
	
	private static MultiValuedMap<String, List<String>> extractXMLFile(String xmlFile, String commonCol) throws FileNotFoundException, IOException, XMLStreamException {
		
		//Map<String, List<String>> xmlMap = new HashMap<String, List<String>>();
		MultiValuedMap<String, List<String>> xmlMap = new ArrayListValuedHashMap<String, List<String>>();
		//Multimap<Integer, String> multimap = ArrayListMultimap.create();
		List<String> valueList = new ArrayList<String>();
		
		try (InputStream stream = new FileInputStream(xmlFile)) 
		{
		    XMLInputFactory inputFactory = XMLInputFactory.newFactory();
		    inputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);

		    XMLStreamReader reader = inputFactory.createXMLStreamReader(stream);
		    String attributeName = null;
		    String value = null;
		    String keyName = null;
		    String endElement = null;

		    while (reader.hasNext()) {
		        switch (reader.next()) {
		            case XMLStreamConstants.START_ELEMENT:
		                attributeName = reader.getName().toString();
		                break;
		            case XMLStreamConstants.END_ELEMENT:
		            	endElement = reader.getName().toString();
		                //System.out.println("End " + reader.getName());
		                break;
		            case XMLStreamConstants.CHARACTERS:
		            case XMLStreamConstants.SPACE:
		            	value = reader.getText();
		                if(attributeName != null && attributeName.trim().length() > 0 && value != null && value.trim().length() >0 && attributeName.equalsIgnoreCase(commonCol))
		                {
		                	keyName = value;
		                }
				        if(attributeName != null && attributeName.trim().length() > 0 && value != null && value.trim().length() >0 && !attributeName.equalsIgnoreCase(commonCol))
				        {
				        	//System.out.println(attributeName +  ":" + value);
				        	if(valueList == null || valueList.size() == 0)
				        	{
				        		valueList = new ArrayList<String>();
				        	}
				        	valueList.add(attributeName +  "=" + value);
				        }
		                break;
		        }
		        if(endElement != null && endElement.trim().length() > 0 && endElement.equals("details"))
		        {
		        	List<String> newList = new ArrayList<String>();
		        	if(valueList.size() >0)
		        	{
			        	for(int i = 0;i<valueList.size();i++)
			        	{
			        		newList.add(valueList.get(i));
			        	}
			        	xmlMap.put(keyName, newList);		        					    
					    valueList.clear();
		        	}
		        }
		    }
		}
		return xmlMap;
		
	}
	
	/*private static Map<String, List<String>> extractXMLFileMultipleRow(String xmlFile, String commonCol) throws FileNotFoundException, IOException, XMLStreamException {
		
		String [] commonColArr = commonCol.split(":");
		String mainKey = commonColArr[0];
		String secondaryKey = commonColArr[1];
		
		Map<String, List<String>> xmlMap = new HashMap<String, List<String>>();
		List<String> valueList = new ArrayList<String>();
		
		try (InputStream stream = new FileInputStream(xmlFile)) 
		{
		    XMLInputFactory inputFactory = XMLInputFactory.newFactory();
		    inputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);

		    XMLStreamReader reader = inputFactory.createXMLStreamReader(stream);
		    String attributeName = null;
		    String value = null;
		    String keyName = "";
		    String endElement = null;

		    while (reader.hasNext()) {
		        switch (reader.next()) {
		            case XMLStreamConstants.START_ELEMENT:
		                attributeName = reader.getName().toString();
		                break;
		            case XMLStreamConstants.END_ELEMENT:
		            	endElement = reader.getName().toString();
		                //System.out.println("End " + reader.getName());
		                break;
		            case XMLStreamConstants.CHARACTERS:
		            case XMLStreamConstants.SPACE:
		            	value = reader.getText();
		                if(attributeName != null && attributeName.trim().length() > 0 && value != null && value.trim().length() >0)
		                {
		                	if(attributeName.equalsIgnoreCase(mainKey) || attributeName.equalsIgnoreCase(secondaryKey))
		                	{
			                	if(keyName.equals(""))
			                	{
			                		keyName = value;
			                	}
			                	else
			                	{
			                		keyName = keyName + ":" + value;
			                	}
		                	}
		                }
				        if(attributeName != null && attributeName.trim().length() > 0 && value != null && value.trim().length() >0 && !attributeName.equalsIgnoreCase(mainKey))
				        {
				        	//System.out.println(attributeName +  ":" + value);
				        	if(valueList == null || valueList.size() == 0)
				        	{
				        		valueList = new ArrayList<String>();
				        	}
				        	valueList.add(attributeName +  "=" + value);
				        }
		                break;
		        }
		        if(endElement != null && endElement.trim().length() > 0 && endElement.equals("details"))
		        {
		        	List<String> newList = new ArrayList<String>();
		        	if(valueList.size() >0)
		        	{
			        	for(int i = 0;i<valueList.size();i++)
			        	{
			        		newList.add(valueList.get(i));
			        	}
			        	xmlMap.put(keyName, newList);		        					    
					    valueList.clear();
					    keyName = "";
		        	}
		        }
		    }
		}
		return xmlMap;
		
	}*/

	private void compareXML(MultiValuedMap<String, List<String>> firstXML, MultiValuedMap<String, List<String>> secondXML, String[] fields, HttpServletResponse response, String commonCol, 
			String ruleName, String rowValues, String type) throws IOException {
	
		sbFinal = new StringBuffer();
		StringBuffer sbTemp = new StringBuffer();
		boolean matchFound = false;
		ArrayList<String> valList1 = null;
		ArrayList<String> valList2 = null;
		/*Iterator it = firstXML.entrySet().iterator();
	    while (it.hasNext()) 
	    {
	        Map.Entry keyFirst = (Map.Entry)it.next();
	        if(secondXML.get(keyFirst.getKey()) != null)
	        {
	        	String id = keyFirst.getKey().toString();
	        	//if(id.equals("00239600"))
	        	//{
		        	ArrayList<String> valList1 = (ArrayList<String>) keyFirst.getValue();
		        	ArrayList<String> valList2 = (ArrayList<String>) secondXML.get(keyFirst.getKey());
		        	compareXMLDifference(id, valList1, valList2, fields, response, commonCol); 
	        	//}
	        }
	    }*/
		
		Iterator<String> it = firstXML.keySet().iterator();
		while (it.hasNext()) 
	    {
			matchFound = false;
			String id = (String)it.next();
			//if(id.equals("A01274"))
			if(secondXML.get(id) != null)
	        {
				Collection<List<String>> colList1 =  firstXML.get(id);
				if(colList1.size() == 1)
				{
					Iterator<List<String>> col1 = colList1.iterator();
					while(col1.hasNext()) 
					{
						valList1 = (ArrayList<String>)col1.next();
					}
		        	Collection<List<String>> colList2 =  secondXML.get(id);
		        	if(colList2 != null && colList2.size() > 0)
		        	{
			        	Iterator<List<String>> col2 = colList2.iterator();
						while(col2.hasNext()) 
						{
							valList2 = (ArrayList<String>)col2.next();
						}
		        	}
		        	else
		        	{
		        		valList2 = null;
		        	}
		        	compareXMLDifference(id, valList1, valList2, fields, response, commonCol, type, ruleName); 
				}
				else if(colList1.size() >1)
				{
					Iterator<List<String>> col1 = colList1.iterator();
					while(col1.hasNext()) 
					{
						valList1 = (ArrayList<String>)col1.next();
						Collection<List<String>> colList2 =  secondXML.get(id);
			        	Iterator<List<String>> col2 = colList2.iterator();
						while(col2.hasNext()) 
						{
							sbTemp = new StringBuffer();
							valList2 = (ArrayList<String>)col2.next();
							String fieldVal = compareXMLDifferenceMultipleRow(id, valList1, valList2, fields, response, commonCol, type, ruleName);
				        	if(fieldVal.equals(""))
				        	{
				        		matchFound = true;
				        		break;
				        	}
				        	else
				        	{
				        		sbTemp.append(fieldVal);
				        		sbTemp.append("\n");
				        	}				        	
						}
						if(matchFound)
						{
							//sbTemp = new StringBuffer();
							break;
						}
					}
					if(!matchFound)
					{
						sbFinal.append(sbTemp);
					}
				}				
	        }
	    }
	
		System.out.println(sbFinal.toString());		
		createComparisonMatrix(commonCol, ruleName, response, rowValues);
	}
	
	private void createComparisonMatrix(String commonCol, String ruleName, HttpServletResponse response, String rowValues) throws IOException {
		
		String primaryKey = "";
		String oldKey = "";
		String fieldName = "";
		String sourceVal = "";
		String wdVal = "";
		String headerName = "";
		String secondaryName = "";
		Row row = null;
		Row rowWD = null;
		String[] headingArr = rowValues.split(",");
		Map<String, Integer> columnMap = new HashMap<>();
		
		Workbook workbook = new XSSFWorkbook(); 
        Sheet sheet = workbook.createSheet(ruleName);
        File file = File.createTempFile(ruleName, ".xlsx");
        
        if(sbFinal.toString().length() > 0)
		{
        	mismatchCount = getMismatchCount(sbFinal.toString());
	        Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			
	        CellStyle headerStyle = workbook.createCellStyle();
	        headerStyle.setAlignment(HorizontalAlignment.CENTER);
	        headerStyle.setFont(headerFont);
	        headerStyle.setBorderBottom(BorderStyle.THICK);
	        
	        CellStyle descStyle = workbook.createCellStyle();
	        descStyle.setAlignment(HorizontalAlignment.LEFT);
	        descStyle.setFont(headerFont);
	        
	        Row firstRow = sheet.createRow(0);
	        Cell firstCell = firstRow.createCell(0);
	        firstCell.setCellValue("Total Number of records in Source:");
	        firstCell.setCellStyle(descStyle);	        
	        firstCell = firstRow.createCell(3);
	        firstCell.setCellValue(sourceCount);
	        firstCell.setCellStyle(descStyle);	
	        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2 ));
	        
	        Row secondRow = sheet.createRow(1);
	        Cell secondCell = secondRow.createCell(0);
	        secondCell.setCellValue("Total Number of records fetched:");
	        secondCell.setCellStyle(descStyle);
	        secondCell = secondRow.createCell(3);
	        secondCell.setCellValue(wdCount);
	        secondCell.setCellStyle(descStyle);	
	        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 2 ));
	        
	        Row thirdRow = sheet.createRow(2);
	        Cell thirdCell = thirdRow.createCell(0);
	        thirdCell.setCellValue("Total Number of record where mismatch found:");
	        thirdCell.setCellStyle(descStyle);
	        thirdCell = thirdRow.createCell(3);
	        thirdCell.setCellValue(mismatchCount);
	        thirdCell.setCellStyle(descStyle);
	        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 2 ));
	        
	        Row headerRow = sheet.createRow(4);
	        Cell cell = headerRow.createCell(0);
	        if(commonCol.contains(":"))
	        {
	        	for(int i = 0; i<headingArr.length; i++)
		        {
		        	if(commonCol.substring(0, commonCol.indexOf(":")).equalsIgnoreCase(headingArr[i].substring(headingArr[i].indexOf("|")+1, headingArr[i].length())))
		        	{
		    	        cell.setCellValue(headingArr[i].substring(0, headingArr[i].indexOf("|")));
		    	        cell.setCellStyle(headerStyle);
		    	        break;
		        	}
		        }
	        }
	        else
	        {
	        	for(int i = 0; i<headingArr.length; i++)
		        {
		        	if(commonCol.equalsIgnoreCase(headingArr[i].substring(headingArr[i].indexOf("|")+1, headingArr[i].length())))
		        	{
		    	        cell.setCellValue(headingArr[i].substring(0, headingArr[i].indexOf("|")));
		    	        cell.setCellStyle(headerStyle);
		    	        break;
		        	}
		        }
	        }
	        cell = headerRow.createCell(1);
	        cell.setCellValue("");
	        cell.setCellStyle(headerStyle);
	        for(int i = 1; i<headingArr.length; i++)
	        {
	        	cell = headerRow.createCell(i + 1);
	        	cell.setCellValue(headingArr[i].substring(0, headingArr[i].indexOf("|")));
    	        cell.setCellStyle(headerStyle);
    	        columnMap.put(headingArr[i].substring(0, headingArr[i].indexOf("|")), i + 1);	        	
	        }
	        System.out.println("columnMap--"+columnMap);
	        
			BufferedReader bufReader = new BufferedReader(new StringReader(sbFinal.toString().trim()));				    
		    String line = null;
		    int rowNum = 3;
		    int rowNumWD = 4;
			while( (line=bufReader.readLine()) != null )
			{
				String[] outputArr = line.split(",");
				if(outputArr.length >1)
				{
					if(outputArr.length == 4)
					{
						primaryKey = outputArr[0];
						fieldName = outputArr[1];
						sourceVal = outputArr[2];
						if(sourceVal.contains("?"))
                        {
							sourceVal = sourceVal.replace("?", ",");
                        }
						wdVal = outputArr[3];
						if(wdVal.contains("?"))
                        {
							wdVal = wdVal.replace("?", ",");
                        }
					}
					else
					{
						primaryKey = outputArr[0];
						fieldName = outputArr[1];
						sourceVal = outputArr[2];
						if(sourceVal.contains("?"))
                        {
							sourceVal = sourceVal.replace("?", ",");
                        }
						wdVal = "";
					}
					if(primaryKey.equalsIgnoreCase(oldKey))
					{
						for(int i = 0; i<headingArr.length; i++)
				        {
				        	if(fieldName.equalsIgnoreCase(headingArr[i].substring(headingArr[i].indexOf("|")+1, headingArr[i].length())))
				        	{
				    	        headerName = headingArr[i].substring(0, headingArr[i].indexOf("|"));
				    	        break;
				        	}
				        }
						//row.createCell(1).setCellValue(fieldName);
						row.createCell(columnMap.get(headerName)).setCellValue(sourceVal);
						rowWD.createCell(columnMap.get(headerName)).setCellValue(wdVal);
					}
					else
					{
						rowNum = rowNum + 2;
						rowNumWD = rowNumWD + 2;
						row = sheet.createRow(rowNum);
						rowWD = sheet.createRow(rowNumWD);
						for(int i = 0; i<headingArr.length; i++)
				        {
				        	if(fieldName.equalsIgnoreCase(headingArr[i].substring(headingArr[i].indexOf("|")+1, headingArr[i].length())))
				        	{
				    	        headerName = headingArr[i].substring(0, headingArr[i].indexOf("|"));
				    	        break;
				        	}
				        }
						if(primaryKey.contains(":"))
						{
							row.createCell(0).setCellValue(primaryKey.substring(0, primaryKey.indexOf(":")));
							row.createCell(1).setCellValue("Source");
							rowWD.createCell(1).setCellValue("Workday");
							for(int i = 0; i<headingArr.length; i++)
					        {
					        	if(commonCol.substring(commonCol.indexOf(":")+1, commonCol.length()).equalsIgnoreCase(headingArr[i].substring(headingArr[i].indexOf("|")+1, headingArr[i].length())))
					        	{
					        		secondaryName = headingArr[i].substring(0, headingArr[i].indexOf("|"));
					    	        break;
					        	}
					        }
							row.createCell(columnMap.get(secondaryName)).setCellValue(primaryKey.substring(primaryKey.indexOf(":")+1, primaryKey.length()));
							rowWD.createCell(columnMap.get(secondaryName)).setCellValue(primaryKey.substring(primaryKey.indexOf(":")+1, primaryKey.length()));
						}
						else
						{
							row.createCell(0).setCellValue(primaryKey);
							row.createCell(1).setCellValue("Source");
							rowWD.createCell(1).setCellValue("Workday");
						}
						//row.createCell(1).setCellValue(fieldName);
						row.createCell(columnMap.get(headerName)).setCellValue(sourceVal);
						rowWD.createCell(columnMap.get(headerName)).setCellValue(wdVal);
					}
					oldKey = primaryKey;
				}
			}
			
			if(errorList.size() >0)
			{
		        int lastRowNum = sheet.getLastRowNum();
				Row notLoadedRow = sheet.createRow(lastRowNum+3);
		        Cell notLoadedCell = notLoadedRow.createCell(0);
		        notLoadedCell.setCellValue("Total Number of records not fetched:");
		        notLoadedCell.setCellStyle(descStyle);	        
		        notLoadedCell = notLoadedRow.createCell(3);
		        notLoadedCell.setCellValue(errorList.size());
		        notLoadedCell.setCellStyle(descStyle);
		        //sheet.addMergedRegion(new CellRangeAddress(lastRowNum, lastRowNum, 0, 2 ));
		        
		        Row notLoadedDetailsRow = null;
		        Cell notLoadedDetailsCell = null;
		        String notLoadedStr = null;
		        String[] notLoadedStrArr = null;
		        int iVal = 4;
		        int iCnt = 1;
		        for (Map.Entry<String,String> entry : sourceEntryMap.entrySet())
		        {
		        	iCnt = 1;
		        	iVal++;
		        	notLoadedDetailsRow = sheet.createRow(lastRowNum+iVal);
		        	notLoadedDetailsCell = notLoadedDetailsRow.createCell(0);
		        	notLoadedDetailsCell.setCellValue(entry.getKey());
		        	notLoadedDetailsCell = notLoadedDetailsRow.createCell(1);
		        	notLoadedDetailsCell.setCellValue("Source");
		        	
		        	notLoadedStr = entry.getValue();
		        	notLoadedStrArr = notLoadedStr.split(",");
		        	for(int c=0;c<notLoadedStrArr.length;c++)
		        	{
		        		iCnt++;
		        		notLoadedDetailsCell = notLoadedDetailsRow.createCell(iCnt);
		        		notLoadedDetailsCell.setCellValue(notLoadedStrArr[c]);
		        	}
		        }
			}		         
		    			
		    for(int i = 0; i < columnMap.size() + 2; i++) {
	            sheet.autoSizeColumn(i);
	        }
		}
        else
        {
        	mismatchCount = 0;
	        Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			
	        CellStyle headerStyle = workbook.createCellStyle();
	        headerStyle.setAlignment(HorizontalAlignment.CENTER);
	        headerStyle.setFont(headerFont);
	        headerStyle.setBorderBottom(BorderStyle.THICK);
	        
	        CellStyle descStyle = workbook.createCellStyle();
	        descStyle.setAlignment(HorizontalAlignment.LEFT);
	        descStyle.setFont(headerFont);
	        
	        Row firstRow = sheet.createRow(0);
	        Cell firstCell = firstRow.createCell(0);
	        firstCell.setCellValue("Total Number of records in Source:");
	        firstCell.setCellStyle(descStyle);	        
	        firstCell = firstRow.createCell(3);
	        firstCell.setCellValue(sourceCount);
	        firstCell.setCellStyle(descStyle);
	        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2 ));
	        
	        Row secondRow = sheet.createRow(1);
	        Cell secondCell = secondRow.createCell(0);
	        secondCell.setCellValue("Total Number of records fetched:");
	        secondCell.setCellStyle(descStyle);
	        secondCell = secondRow.createCell(3);
	        secondCell.setCellValue(wdCount);
	        secondCell.setCellStyle(descStyle);
	        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 2 ));
	        
	        Row thirdRow = sheet.createRow(2);
	        Cell thirdCell = thirdRow.createCell(0);
	        thirdCell.setCellValue("Total Number of record where mismatch found:");
	        thirdCell.setCellStyle(descStyle);
	        thirdCell = thirdRow.createCell(3);
	        thirdCell.setCellValue(mismatchCount);
	        thirdCell.setCellStyle(descStyle);
	        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 2 ));
	        
	        Row notLoadedRow = sheet.createRow(3);
	        Cell notLoadedCell = notLoadedRow.createCell(0);
	        notLoadedCell.setCellValue("Total Number of records not fetched:");
	        notLoadedCell.setCellStyle(descStyle);	        
	        notLoadedCell = notLoadedRow.createCell(3);
	        notLoadedCell.setCellValue(errorList.size());
	        notLoadedCell.setCellStyle(descStyle);
	        sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 2 ));
	        
	        Row headerRow = sheet.createRow(5);
	        Cell cell = headerRow.createCell(0);
	        if(commonCol.contains(":"))
	        {
	        	for(int i = 0; i<headingArr.length; i++)
		        {
		        	if(commonCol.substring(0, commonCol.indexOf(":")).equalsIgnoreCase(headingArr[i].substring(headingArr[i].indexOf("|")+1, headingArr[i].length())))
		        	{
		    	        cell.setCellValue(headingArr[i].substring(0, headingArr[i].indexOf("|")));
		    	        cell.setCellStyle(headerStyle);
		    	        break;
		        	}
		        }
	        }
	        else
	        {
	        	for(int i = 0; i<headingArr.length; i++)
		        {
		        	if(commonCol.equalsIgnoreCase(headingArr[i].substring(headingArr[i].indexOf("|")+1, headingArr[i].length())))
		        	{
		    	        cell.setCellValue(headingArr[i].substring(0, headingArr[i].indexOf("|")));
		    	        cell.setCellStyle(headerStyle);
		    	        break;
		        	}
		        }
	        }
	        
			if(errorList.size() >0)
			{
		        headerRow = sheet.createRow(6);
		        cell = headerRow.createCell(0);
		        if(commonCol.contains(":"))
		        {
		        	for(int i = 0; i<headingArr.length; i++)
			        {
			        	if(commonCol.substring(0, commonCol.indexOf(":")).equalsIgnoreCase(headingArr[i].substring(headingArr[i].indexOf("|")+1, headingArr[i].length())))
			        	{
			    	        cell.setCellValue(headingArr[i].substring(0, headingArr[i].indexOf("|")));
			    	        cell.setCellStyle(headerStyle);
			    	        break;
			        	}
			        }
		        }
		        else
		        {
		        	for(int i = 0; i<headingArr.length; i++)
			        {
			        	if(commonCol.equalsIgnoreCase(headingArr[i].substring(headingArr[i].indexOf("|")+1, headingArr[i].length())))
			        	{
			    	        cell.setCellValue(headingArr[i].substring(0, headingArr[i].indexOf("|")));
			    	        cell.setCellStyle(headerStyle);
			    	        break;
			        	}
			        }
		        }
		        cell = headerRow.createCell(1);
		        cell.setCellValue("");
		        cell.setCellStyle(headerStyle);
		        for(int i = 1; i<headingArr.length; i++)
		        {
		        	cell = headerRow.createCell(i + 1);
		        	cell.setCellValue(headingArr[i].substring(0, headingArr[i].indexOf("|")));
	    	        cell.setCellStyle(headerStyle);
	    	        columnMap.put(headingArr[i].substring(0, headingArr[i].indexOf("|")), i + 1);	
		        }
		        
		        int lastRowNum = 5;				
		        
		        Row notLoadedDetailsRow = null;
		        Cell notLoadedDetailsCell = null;
		        String notLoadedStr = null;
		        String[] notLoadedStrArr = null;
		        int iVal = 1;
		        int iCnt = 1;
		        for (Map.Entry<String,String> entry : sourceEntryMap.entrySet())
		        {
		        	iCnt = 1;
		        	iVal++;
		        	notLoadedDetailsRow = sheet.createRow(lastRowNum+iVal);
		        	notLoadedDetailsCell = notLoadedDetailsRow.createCell(0);
		        	notLoadedDetailsCell.setCellValue(entry.getKey());
		        	notLoadedDetailsCell = notLoadedDetailsRow.createCell(1);
		        	notLoadedDetailsCell.setCellValue("Source");
		        	
		        	notLoadedStr = entry.getValue();
		        	notLoadedStrArr = notLoadedStr.split(",");
		        	for(int c=0;c<notLoadedStrArr.length;c++)
		        	{
		        		iCnt++;
		        		notLoadedDetailsCell = notLoadedDetailsRow.createCell(iCnt);
		        		notLoadedDetailsCell.setCellValue(notLoadedStrArr[c]);
		        	}
		        }
			}
			
		    for(int i = 0; i < columnMap.size() + 2; i++) {
	            sheet.autoSizeColumn(i);
	        }
        }
        
        FileOutputStream fileOut = new FileOutputStream(file);
        workbook.write(fileOut);
        
		FileInputStream fis = null;
		response.setHeader("Content-Disposition", "attachment;filename=" + ruleName + ".xlsx" + "");
		response.setContentType("application/vnd.ms-excel");
		try
		{
			fis = new FileInputStream(file);
			IOUtils.copy(fis, response.getOutputStream());
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		fis.close();		
		workbook.close();
		sbFinal = null;
		file.delete();

	}

	private int getMismatchCount(String string) throws IOException {
		
		String primaryKey = "";
		String oldKey = "";
		BufferedReader bufReader = new BufferedReader(new StringReader(sbFinal.toString()));		
	    String line = null;
	    int count = 0;
		while( (line=bufReader.readLine()) != null )
		{
			String[] outputArr = line.split(",");
			if(outputArr.length >1)
			{
				primaryKey = outputArr[0];
				if(!primaryKey.equalsIgnoreCase(oldKey))
				{
					count++;
				}
			}
			oldKey = primaryKey;				
		}
		return count;
	}

	private void compareXMLDifference(String key, ArrayList<String> valList1, ArrayList<String> valList2, String[] fields, HttpServletResponse response, 
			String commonCol, String type, String ruleName) throws IOException {

		String fieldVal1 = null;
		String fieldVal2 = null;
		for(int i = 0; i<fields.length; i++)
		{
			fieldVal1 = "";
			fieldVal2 = "";
			for(int j = 0; j<valList1.size(); j++)
			{
				if(valList1.get(j).substring(0, valList1.get(j).indexOf("=")).equalsIgnoreCase(fields[i]))
				{
					String[] valArr = valList1.get(j).split("=");
					fieldVal1 = valArr[1];
					if(fieldVal1.contains("~"))
					{
						fieldVal1 = fieldVal1.replace("~", "|");
					}
					break;
				}
			}
	
			for(int k = 0; k<valList2.size(); k++)
			{
				if(valList2.get(k).substring(0, valList2.get(k).indexOf("=")).equalsIgnoreCase(fields[i]))
				{
					String[] valArr = valList2.get(k).split("=");
					fieldVal2 = valArr[1];
					if(fieldVal2.contains("~"))
					{
						fieldVal2 = fieldVal2.replace("~", "|");
					}
					break;
				}
			}
			
			if(valList1 != null && valList1.size() > 0 && valList2 != null && valList2.size() > 0)
			{
				if(!fieldVal1.trim().equalsIgnoreCase(fieldVal2.trim()))
				{
					if(type.equalsIgnoreCase("File"))
					{
						/*if(!fieldVal2.equals(""))
						{
							sbFinal.append(key + "," + fields[i] + "," + fieldVal2 + "," + fieldVal1);
							sbFinal.append("\n");
						}
						else
						{*/
							if(fieldVal1.contains("|") && !fieldVal2.equals(""))
							{
								boolean checked = checkFieldContains(fieldVal1,fieldVal2);
								if(!checked)
								{
									sbFinal.append(key + "," + fields[i] + "," + fieldVal2 + "," + fieldVal1);
									sbFinal.append("\n");
								}
							}
							else
							{
								if(!fieldVal2.equals(""))
								{
									sbFinal.append(key + "," + fields[i] + "," + fieldVal2 + "," + fieldVal1);
									sbFinal.append("\n");
								}
							}
						//}
					}
					else
					{
						sbFinal.append(key + "," + fields[i] + "," + fieldVal2 + "," + fieldVal1);
						sbFinal.append("\n");
					}
				}
			}
		}		
	}
	
	private boolean checkFieldContains(String fieldVal1, String fieldVal2) {
		
		boolean checked = false;
		String fieldVal1New = fieldVal1.replace("|", "~");
		String [] fieldVal1Arr = fieldVal1New.split("~");
		for(int i = 0;i<fieldVal1Arr.length;i++)
		{
			if(fieldVal1Arr[i].equalsIgnoreCase(fieldVal2))
			{
				checked = true;
				break;
			}
		}
		return checked;		
	}

	private String compareXMLDifferenceMultipleRow(String key, ArrayList<String> valList1, ArrayList<String> valList2, String[] fields, HttpServletResponse response, 
			String commonCol, String type, String ruleName) throws IOException {

		String fieldVal1 = null;
		String fieldVal2 = null;
		StringBuffer sbTemp = new StringBuffer();
		for(int i = 0; i<fields.length; i++)
		{
			fieldVal1 = "";
			fieldVal2 = "";
			for(int j = 0; j<valList1.size(); j++)
			{
				if(valList1.get(j).substring(0, valList1.get(j).indexOf("=")).equalsIgnoreCase(fields[i]))
				{
					String[] valArr = valList1.get(j).split("=");
					fieldVal1 = valArr[1];
					if(fieldVal1.contains("~"))
					{
						fieldVal1 = fieldVal1.replace("~", "|");
					}
					break;
				}
			}
	
			for(int k = 0; k<valList2.size(); k++)
			{
				if(valList2.get(k).substring(0, valList2.get(k).indexOf("=")).equalsIgnoreCase(fields[i]))
				{
					String[] valArr = valList2.get(k).split("=");
					fieldVal2 = valArr[1];
					if(fieldVal2.contains("~"))
					{
						fieldVal2 = fieldVal2.replace("~", "|");
					}
					break;
				}
			}
			
			if(!fieldVal1.equalsIgnoreCase(fieldVal2))
			{
				if(ruleName.equalsIgnoreCase("Change Benefits Life Events") || ruleName.equalsIgnoreCase("Hire_Employee") || ruleName.equalsIgnoreCase("Assign Organization")
						|| ruleName.equalsIgnoreCase("Payee Input Data"))
				{
					if(fieldVal1.contains("|") && !fieldVal2.equals(""))
					{
						boolean checked = checkFieldContains(fieldVal1,fieldVal2);
						if(!checked)
						{
							sbFinal.append(key + "," + fields[i] + "," + fieldVal2 + "," + fieldVal1);
							sbFinal.append("\n");
						}
					}
					else if(!fieldVal2.equals(""))
					{
						sbFinal.append(key + "," + fields[i] + "," + fieldVal2 + "," + fieldVal1);
						sbFinal.append("\n");
					}
				}
				else
				{
					if(type.equalsIgnoreCase("File"))
					{
						if(fieldVal2.equals(""))
						{
							sbFinal.append(key + "," + fields[i] + "," + fieldVal2 + "," + fieldVal1);
							sbFinal.append("\n");
						}
						else
						{
							if(fieldVal1.contains("|"))
							{
								boolean checked = checkFieldContains(fieldVal1,fieldVal2);
								if(!checked)
								{
									sbFinal.append(key + "," + fields[i] + "," + fieldVal2 + "," + fieldVal1);
									sbFinal.append("\n");
								}
							}
							else
							{
								sbFinal.append(key + "," + fields[i] + "," + fieldVal2 + "," + fieldVal1);
								sbFinal.append("\n");								
							}
						}
					}
					else
					{
						sbFinal.append(key + "," + fields[i] + "," + fieldVal2 + "," + fieldVal1);
						sbFinal.append("\n");
					}
				}				
			}			 
		}
		return sbTemp.toString();		
	}
	
	@RequestMapping(value = "executeRequestFile/{fileName}/{reqName}/{reqId}", method = RequestMethod.POST)
	public void executeRequestFile(@PathVariable("fileName")String fileName, @PathVariable("reqName") String reqName, 
			@PathVariable("reqId") String reqId, @RequestParam("requestFile") MultipartFile requestFile, HttpSession httpSession) {
		
		System.out.println("fileName-"+fileName);
		System.out.println("reqName-"+reqName);
		Long requestId = Long.parseLong(reqId);
		
		byte[] requestFileData = null;
		String xmlStr = null;
		try 
		{
			requestFileData = requestFile.getBytes();
			xmlStr = new String(requestFileData, "UTF-8"); 
			
			File reqXMLfile = File.createTempFile(fileName.substring(0, fileName.indexOf(".")), ".xml");
			PrintWriter writer = new PrintWriter(reqXMLfile);
		    writer.write(xmlStr.toString());
			writer.flush();
			writer.close();
			
			Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
			User user = userService.getUser(userId);
			GetRequest getRequest = getRequestService.getRequestId(requestId);
			if(getRequest == null)
			{
				getRequest = new GetRequest();
				getRequest.setRequestName(reqName);
				getRequest.setRequestXMLName(fileName);
				getRequest.setRequestXMLContent(Files.readAllBytes(reqXMLfile.toPath()));
				getRequest.setUserId(userId);
				getRequest.setClient(user.getClient());
				getRequestService.addGetRequest(getRequest);
			}
			else
			{
				getRequest.setRequestName(reqName);
				getRequest.setRequestXMLName(fileName);
				getRequest.setRequestXMLContent(Files.readAllBytes(reqXMLfile.toPath()));
				getRequest.setClient(user.getClient());
				getRequestService.updateGetRequest(getRequest);
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

	}
	
	@RequestMapping(value = "/getAllRequestXML", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<GetRequest> getAllRequestXML(HttpSession httpSession) {
		
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		return getRequestService.getRequestsByClient(user.getClient());
	}
	
	@RequestMapping(value = "/deleteRequest/{getReqId}", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public void deleteTenant(@PathVariable("getReqId") Long getReqId) {
		getRequestService.deleteGetRequest(getReqId);
	}
	
	@RequestMapping(value = "/getRuleNameListTenantBased", method = RequestMethod.POST, headers = "Accept=application/json")
	public JSONArray getRuleNameListTenantBased() {
		
		JSONArray objArr = new JSONArray();
		JSONObject obj = null;					
		try 
		{			
			obj = new JSONObject();
			obj.put("operationName", "Worker Data");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Compensation Data");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "License");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "PassportVisa");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Government ID");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Other ID");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Additional Name Data");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "CIPA Brazil");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Custom Domain 8");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Employment Data");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Payment Election");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Payment Election Option");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Leave of Absence");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Worker Address Data");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Time Off Data");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Collective Agreement");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Work Schedule");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Payroll Payee TD1X");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Federal Annual Elections");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Payroll Payee CPP");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Payroll Payee EI");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Payroll Payee PTD1X");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Payroll Payee TD1");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Payroll Payee Student Loans");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Payroll Payee Province TD1");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Benefit Annual Rate");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Beneficiary");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Dependent");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Manage Education");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Calculated Salary Plan");
			objArr.put(obj);
			obj = new JSONObject();
			obj.put("operationName", "Allowance Override");
			objArr.put(obj);
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		return objArr;
	}
	
	@RequestMapping(value = "/getRuleNameList/{pageId}", method = RequestMethod.POST, headers = "Accept=application/json")
	public JSONArray getRuleNameList(HttpSession httpSession, @PathVariable("pageId") Long pageId) {
		
		JSONArray objArr = new JSONArray();
		JSONObject obj = null;			
		try 
		{	
				/*obj = new JSONObject();
				obj.put("operationName", "Import Supplier Invoice");
				objArr.put(obj);*/
				obj = new JSONObject();
				obj.put("operationName", "Put_Applicant");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Create_Position");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Location");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Hire_Employee");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Terminate_Employee");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Change_Government_IDs");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Applicant Phone");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Worker Address");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Hire CW");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "End Contingent Worker");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Worker Demographic");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Worker Biographic");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Cost Center");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Cost Center Hierarchy");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Supervisory Organization");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Compensation Details");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Employee ID");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "SupOrg-Matrix Manager");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Host CNUM");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Carryover Balance");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "System User Account");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Matrix Organization");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Employee Contract");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Edit Worker Additional Data");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Start International Assignment");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "End International Assignment");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Assign Organization");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Service Dates");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Payee Tax Code Data");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "UK Payroll ID");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Payroll Payee NI Data");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Change Benefits Life Events");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Payee Input Data");
				objArr.put(obj);
				obj = new JSONObject();
				obj.put("operationName", "Assign Establishment");
				objArr.put(obj);
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		
		return objArr;
	}
	
	@RequestMapping(value = "/performSupplierJournal/{tenantId}/{operationName}/{sectionId}", method = RequestMethod.GET, headers = "Accept=application/json")
	public void performSupplierJournal(@PathVariable("tenantId") Long tenantId, @PathVariable("operationName") String operationName, @PathVariable("sectionId") Long sectionId, HttpServletResponse response, HttpSession httpSession) {
	
		JSONArray jArr1 = new JSONArray();
		System.out.println("operationName--"+operationName);
		System.out.println("sectionId--"+sectionId);
		tenant = tenantService.getTenant(tenantId);
		List<Date> dateList = new ArrayList<Date>();
		
		
		Section section = sectionService.getSection(sectionId);
		List<Result> resultList = section.getResults();
		for(Result result: resultList)
		{
			if(result.getTotalSuccess() > 0 && result.getTotalFailures() == 0)
			{
				dateList.add(result.getLoadDate());
			}
		}
		
		List<Date> list = dateList;
		Date maxDate = Collections.max(list);
		System.out.println(maxDate);
		
		String loadDate = maxDate.toString().substring(0, maxDate.toString().indexOf(" "));
		//String loadDate = "2021-03-17";
	
		String reportURL = "https://" + tenant.getTenantUrl() + "customreport2/" + tenant.getTenantName() + "/s_majumder-impl/" + "Find_Supplier_Invoices_-_Copy?Invoice_On_Hold=0&Intercompany_Invoice=0&Direct_Intercompany=0&Tax-Only=0&Created_On="+loadDate+"-07:00&Memo=hyperloader";
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		XPath xPath = XPathFactory.newInstance().newXPath();
		HttpBasicAuthentication httpBasicAuthentication = new HttpBasicAuthentication();
		String output = httpBasicAuthentication.getWithBasicAuthentication(reportURL, tenant.getTenantUser(), tenant.getTenantUserPassword());
		Reader reader1 = new StringReader(output);
		Document doc;
		try 
		{
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(new InputSource(reader1));
			doc.getDocumentElement().normalize();
			String expression = EMPLOYEE_APPLICANT_MAPPING_RESPONSE_EXPRESSION;
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) 
			{
				try 
				{
					jArr1 = parseNodesForSupplier(nodeList);
					if(jArr1.length() == 0)
					{
						jArr1 = createArrayWithPrevDate(tenant, maxDate);
					}
					createSupplierReport(jArr1, response, loadDate);
				} 
				catch (DOMException | JSONException e)
				{
					e.printStackTrace();
				}
			}			
		} 
		catch (SAXException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (XPathExpressionException e) 
		{
			e.printStackTrace();
		} 
		catch (ParserConfigurationException e) 
		{
			e.printStackTrace();
		}
		
	}

	private JSONArray createArrayWithPrevDate(Tenant tenant, Date maxDate) {
		
		JSONArray jArr = new JSONArray();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(maxDate);
		cal1.add(Calendar.DAY_OF_YEAR, -1);
		Date previousDate = cal1.getTime();
		String loadDate = dateFormat.format(previousDate);
		
		String reportURL = "https://" + tenant.getTenantUrl() + "customreport2/" + tenant.getTenantName() + "/s_majumder-impl/" + "Find_Supplier_Invoices_-_Copy?Invoice_On_Hold=0&Intercompany_Invoice=0&Direct_Intercompany=0&Tax-Only=0&Created_On="+loadDate+"-07:00&Memo=hyperloader";
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		XPath xPath = XPathFactory.newInstance().newXPath();
		HttpBasicAuthentication httpBasicAuthentication = new HttpBasicAuthentication();
		String output = httpBasicAuthentication.getWithBasicAuthentication(reportURL, tenant.getTenantUser(), tenant.getTenantUserPassword());
		Reader reader1 = new StringReader(output);
		Document doc;
		try 
		{
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(new InputSource(reader1));
			doc.getDocumentElement().normalize();
			String expression = EMPLOYEE_APPLICANT_MAPPING_RESPONSE_EXPRESSION;
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) 
			{
				try 
				{
					jArr = parseNodesForSupplier(nodeList);
				} 
				catch (DOMException | JSONException e)
				{
					e.printStackTrace();
				}
			}			
		} 
		catch (SAXException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (XPathExpressionException e) 
		{
			e.printStackTrace();
		} 
		catch (ParserConfigurationException e) 
		{
			e.printStackTrace();
		}
		return jArr;
	}

	private JSONArray parseNodesForSupplier(NodeList nodeList) throws DOMException, JSONException  {
		
		JSONArray details = new JSONArray();
		for (int i = 0; i < nodeList.getLength(); i++) 
		{
		    Node nNode = nodeList.item(i);
		    if (nNode.getNodeName().equals("wd:Report_Data")) 
		    {
				if (nNode.getNodeType() == Node.ELEMENT_NODE) 
				{
					Element eElement = (Element) nNode;
					if (eElement.hasChildNodes()) 
					{
						NodeList childNodes = eElement.getChildNodes();
						for (int j = 0; j < childNodes.getLength(); j++) 
						{
							System.out.println(childNodes.getLength());
							System.out.println(j);
							ArrayList<String> invLineList = new ArrayList<String>();
							Node aChildNode = childNodes.item(j);
							JSONObject obj = new JSONObject();
							if (aChildNode.getNodeName().equals("wd:Report_Entry")) 
							{
								Element eElementEntry = (Element) aChildNode;
								if (eElement.hasChildNodes()) 
								{
									NodeList childNodesEntry = eElementEntry.getChildNodes();
									for (int k = 0; k < childNodesEntry.getLength(); k++) 
									{
										
										Node aChildNodeEntry = childNodesEntry.item(k);
										if(aChildNodeEntry.getNodeName().equals("wd:Invoice_Number"))
										{											
											System.out.println("invoiceNumber-"+ aChildNodeEntry.getTextContent());
											obj.put("invoiceNumber", aChildNodeEntry.getTextContent());											
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:Supplier"))
										{
											String supVal = aChildNodeEntry.getAttributes().getNamedItem("wd:Descriptor").toString();
											String supNameVal = supVal.replaceAll("\"", "");
											int pos = supNameVal.trim().indexOf("=") + 1;
											String actualSupNameVal = supNameVal.substring(pos, supNameVal.length());
											System.out.println("Supplier-"+ actualSupNameVal);
											obj.put("supplier", actualSupNameVal);	
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:Supplier_group"))
										{
											Element eElementSG = (Element) aChildNodeEntry;
											NodeList childNodesSG = eElementSG.getChildNodes();
											Node aChildNodeSG = childNodesSG.item(0);
											if(aChildNodeSG.getNodeName().equals("wd:Supplier_Approval_Status"))
											{
												String statusVal = aChildNodeSG.getAttributes().getNamedItem("wd:Descriptor").toString();
												String supStatusVal = statusVal.replaceAll("\"", "");
												int pos = supStatusVal.trim().indexOf("=") + 1;
												String actualSupStatusVal = supStatusVal.substring(pos, supStatusVal.length());
												System.out.println("supplierApprovalStatus-"+ actualSupStatusVal);
												obj.put("supplierApprovalStatus", actualSupStatusVal);
											}
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:Invoice_Date"))
										{
											System.out.println("invoiceDate-"+ aChildNodeEntry.getTextContent());
											obj.put("invoiceDate", aChildNodeEntry.getTextContent());	
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:Journal_Entry"))
										{
											String journalVal = aChildNodeEntry.getAttributes().getNamedItem("wd:Descriptor").toString();
											String journalNameVal = journalVal.replaceAll("\"", "");
											int pos = journalNameVal.trim().indexOf("=") + 1;
											String actualjournalNameVal = journalNameVal.substring(pos, journalNameVal.length());
											System.out.println("Journal-"+ actualjournalNameVal);
											obj.put("journal", actualjournalNameVal);	
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:Journal_Entry_group"))
										{
											Element eElementJG = (Element) aChildNodeEntry;
											NodeList childNodesJG = eElementJG.getChildNodes();
											Node aChildNodeJG = childNodesJG.item(0);
											if(aChildNodeJG.getNodeName().equals("wd:Journal_Status"))
											{
												String statusVal = aChildNodeJG.getAttributes().getNamedItem("wd:Descriptor").toString();
												String supStatusVal = statusVal.replaceAll("\"", "");
												int pos = supStatusVal.trim().indexOf("=") + 1;
												String actualSupStatusVal = supStatusVal.substring(pos, supStatusVal.length());
												System.out.println("journalStatus-"+ actualSupStatusVal);
												obj.put("journalStatus", actualSupStatusVal);
											}
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:Supplier_Invoice_Lines"))
										{
											String lineVal = aChildNodeEntry.getAttributes().getNamedItem("wd:Descriptor").toString();
											String invLineVal = lineVal.replaceAll("\"", "");
											int pos = invLineVal.trim().indexOf("=") + 1;
											String actualinvLineVal = invLineVal.substring(pos, invLineVal.length());
											System.out.println("invLine-"+ actualinvLineVal);
											invLineList.add(actualinvLineVal);
											obj.put("invoiceLine", getInvLineList(invLineList));
										}
										if(aChildNodeEntry.getNodeName().equals("wd:Created_Date_Time"))
										{
											String createdTime = aChildNodeEntry.getTextContent();
											createdTime = createdTime.replace("T", " ");
											createdTime = createdTime.replace("-07:00", "");
											/*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
											Date date = null;
											try 
											{
												date = sdf.parse(createdTime.trim());
											} 
											catch (ParseException e) 
											{
												e.printStackTrace();
											}
											long millis = date.getTime();
											if(createdTime.equalsIgnoreCase("2021-03-18 00:24:08"))
											{
												createdTime = "2021-03-18 02:24:08";
											}*/
											obj.put("createdTime", createdTime);
										}
									}
								}
							}
							details.put(obj);
						}
					}
				}
		    }
		}
		return details;
	}
	
	private String getInvLineList(ArrayList<String> invLineList) {
		
		Collections.sort(invLineList);
		String invVal = "";
		if(invLineList.size() == 1)
		{
			invVal = invLineList.get(0);
		}
		else
		{
			for(int i = 0; i<invLineList.size(); i++)
			{
				if(invVal.equals(""))
				{
					invVal = invLineList.get(i);
				}
				else
				{
					invVal = invVal + "\n" + invLineList.get(i);
				}
			}
		}
		return invVal;
	}

	private void createSupplierReport(JSONArray jArr, HttpServletResponse response, String loadDate) {

		File file = null;
		JSONArray jArr1 = null;
		Workbook workbook = new XSSFWorkbook(); 
		Sheet sheet = workbook.createSheet("Journal");
		
		String[] columns = {"Supplier", "Journal Entry", "Journal Status", "Supplier Approval Status", "Invoice Number", "Invoice Line", "Invoice Date"};
		
        Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		
		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFont(headerFont);
		
		CellStyle wrapStyle = workbook.createCellStyle();
		wrapStyle.setWrapText(true);
		
		Row firstRow = sheet.createRow(1);
		Cell cell = firstRow.createCell(0);
		cell.setCellValue("Invoice Date On or After: "+ loadDate);
		cell.setCellStyle(headerStyle);
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 2 ));
		
		Row dateRow = sheet.createRow(2);
		cell = dateRow.createCell(0);
		cell.setCellValue("Total number of Invoice: 1");
		cell.setCellStyle(headerStyle);
		sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 2 ));
		
		Row secondRow = sheet.createRow(3);
		cell = secondRow.createCell(0);
		cell.setCellValue("The Records where Supplier Approval Status is APPROVED but Journal is not POSTED");
		cell.setCellStyle(headerStyle);
		sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 3 ));
		
		Row headingRow = sheet.createRow(5);
        
        for(int i = 0; i < columns.length; i++) {
            Cell hCell = headingRow.createCell(i);
            hCell.setCellValue(columns[i]);
            hCell.setCellStyle(headerStyle);
        }
        
        if(jArr.length() >1)
        {
        	try 
        	{
        		jArr1 = sortJsonArray(jArr);
        		System.out.println(jArr1);
			} 
        	catch (JSONException e) 
        	{
				e.printStackTrace();
			}
        }
        else
        {
        	jArr1 = jArr;
        }
        
        int rowNum = 6;
        for(int i = 0; i<jArr1.length(); i++) 
        {
        	if(i == jArr1.length()-1)
        	{
	        	try 
	        	{
					JSONObject objects = jArr1.getJSONObject(i);
					if(!objects.isNull("supplierApprovalStatus") && !objects.isNull("journalStatus") && objects.getString("supplierApprovalStatus").equalsIgnoreCase("Approved") 
							&& !objects.getString("journalStatus").equalsIgnoreCase("Posted"))
					{
						Row row = sheet.createRow(rowNum++);
			        	row.createCell(0).setCellValue(objects.isNull("supplier")?"":objects.getString("supplier"));		        	
			        	row.createCell(1).setCellValue(objects.isNull("journal")?"":objects.getString("journal"));
			        	row.createCell(2).setCellValue(objects.isNull("journalStatus")?"":objects.getString("journalStatus"));
			        	row.createCell(3).setCellValue(objects.isNull("supplierApprovalStatus")?"":objects.getString("supplierApprovalStatus"));
			        	row.createCell(4).setCellValue(objects.isNull("invoiceNumber")?"":objects.getString("invoiceNumber"));
			        	Cell invCell = row.createCell(5);
			        	invCell.setCellValue(objects.isNull("invoiceLine")?"":objects.getString("invoiceLine"));
			        	invCell.setCellStyle(wrapStyle);
			        	row.createCell(6).setCellValue(objects.isNull("invoiceDate")?"":objects.getString("invoiceDate"));
					}
					else if(!objects.isNull("supplierApprovalStatus") && objects.isNull("journalStatus") && objects.getString("supplierApprovalStatus").equalsIgnoreCase("Approved"))
					{
						Row row = sheet.createRow(rowNum++);
			        	row.createCell(0).setCellValue(objects.isNull("supplier")?"":objects.getString("supplier"));		        	
			        	row.createCell(1).setCellValue(objects.isNull("journal")?"":objects.getString("journal"));
			        	row.createCell(2).setCellValue(objects.isNull("journalStatus")?"":objects.getString("journalStatus"));
			        	row.createCell(3).setCellValue(objects.isNull("supplierApprovalStatus")?"":objects.getString("supplierApprovalStatus"));
			        	row.createCell(4).setCellValue(objects.isNull("invoiceNumber")?"":objects.getString("invoiceNumber"));
			        	Cell invCell = row.createCell(5);
			        	invCell.setCellValue(objects.isNull("invoiceLine")?"":objects.getString("invoiceLine"));
			        	invCell.setCellStyle(wrapStyle);
			        	row.createCell(6).setCellValue(objects.isNull("invoiceDate")?"":objects.getString("invoiceDate"));
					}
				} 
	        	catch (JSONException e) 
	        	{
					e.printStackTrace();
				}
        	}
        }
        
        for(int i = 0; i < columns.length+3; i++) {
            sheet.autoSizeColumn(i);
        }
        
        boolean chkSupAppr = chkSupplierApproval(jArr);
        if(chkSupAppr)
        {
	        int lastRowNum = sheet.getLastRowNum();
			Row thirdRow = sheet.createRow(lastRowNum+2);
			cell = thirdRow.createCell(0);
			cell.setCellValue("The Records where Supplier Approval Status is NOT APPROVED but Journal is POSTED");
			cell.setCellStyle(headerStyle);
			sheet.addMergedRegion(new CellRangeAddress(lastRowNum+2, lastRowNum+2, 1, 5 ));
			
			headingRow = sheet.createRow(lastRowNum+4);
	        
	        for(int i = 0; i < columns.length; i++) {
	            Cell hCell = headingRow.createCell(i);
	            hCell.setCellValue(columns[i]);
	            hCell.setCellStyle(headerStyle);
	        }
			
			rowNum = lastRowNum+5;
	        for(int i = 0; i<jArr.length(); i++) 
	        {
	        	try 
	        	{
					JSONObject objects = jArr.getJSONObject(i);
					if(!objects.isNull("supplierApprovalStatus") && !objects.isNull("journalStatus") && !objects.getString("supplierApprovalStatus").equalsIgnoreCase("Approved") 
							&& objects.getString("journalStatus").equalsIgnoreCase("Posted"))
					{
						Row row = sheet.createRow(rowNum++);
			        	row.createCell(0).setCellValue(objects.isNull("supplier")?"":objects.getString("supplier"));
			        	row.createCell(1).setCellValue(objects.isNull("journal")?"":objects.getString("journal"));
			        	row.createCell(2).setCellValue(objects.isNull("journalStatus")?"":objects.getString("journalStatus"));
			        	row.createCell(3).setCellValue(objects.isNull("supplierApprovalStatus")?"":objects.getString("supplierApprovalStatus"));
			        	row.createCell(4).setCellValue(objects.isNull("invoiceNumber")?"":objects.getString("invoiceNumber"));
			        	Cell invCell = row.createCell(5);
			        	invCell.setCellValue(objects.isNull("invoiceLine")?"":objects.getString("invoiceLine"));
			        	invCell.setCellStyle(wrapStyle);
			        	row.createCell(6).setCellValue(objects.isNull("invoiceDate")?"":objects.getString("invoiceDate"));
					}
				} 
	        	catch (JSONException e) 
	        	{
					e.printStackTrace();
				}
	        }
	        
	        for(int i = 0; i < columns.length; i++) {
	            sheet.autoSizeColumn(i);
	        }
        }
		
		try 
		{
			file = File.createTempFile("Supplier_Journal", ".xlsx");
			
			FileOutputStream fileOut = new FileOutputStream(file);
	        workbook.write(fileOut);
	        
			FileInputStream fis = null;
			response.setHeader("Content-Disposition", "attachment;filename=" + "Supplier_Journal" + ".xlsx" + "");
			response.setContentType("application/vnd.ms-excel");
			try
			{
				fis = new FileInputStream(file);
				IOUtils.copy(fis, response.getOutputStream());
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			fis.close();		
			workbook.close();
		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}
	}
	
	private boolean chkSupplierApproval(JSONArray jArr) {
		
		boolean isChecked = false;
		for(int i = 0; i<jArr.length(); i++) 
        {
        	try 
        	{
				JSONObject objects = jArr.getJSONObject(i);
				if(!objects.isNull("supplierApprovalStatus")  && !objects.getString("supplierApprovalStatus").equalsIgnoreCase("Approved"))
				{
					isChecked = true;
					break;
				}
			} 
        	catch (JSONException e) 
        	{
				e.printStackTrace();
			}
        }
		return isChecked;
	}
	
	public JSONArray sortJsonArray(JSONArray array) throws JSONException 
	{
		List<JSONObject> jsons = new ArrayList<JSONObject>();
		for (int i = 0; i < array.length(); i++) 
		{
		    try 
		    {
				jsons.add(array.getJSONObject(i));
				Collections.sort(jsons, new Comparator<JSONObject>() {
				    @Override
				    public int compare(JSONObject lhs, JSONObject rhs) {
				        String lid = null;
				        String rid = null;;
						try 
						{
							lid = lhs.getString("createdTime");
							rid = rhs.getString("createdTime");
						} 
						catch (JSONException e) 
						{
							e.printStackTrace();
						}
				        return lid.compareTo(rid);
				    }
				});
				
			} 
		    catch (JSONException e) 
		    {
				e.printStackTrace();
			}
		}
		return new JSONArray(jsons);
		
	}

	@RequestMapping(value = "performPostLoad/{operationName}/{pageId}/{tenantId}/{sectionId}", method = RequestMethod.POST)
	public JSONArray performPostLoad(@PathVariable("operationName")String operationName,  @PathVariable("pageId")Long pageId, @PathVariable("tenantId")Long tenantId, 
			@PathVariable("sectionId")Long sectionId, HttpSession httpSession, HttpServletResponse response) {
		
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		operation = operationService.getOperation(operationName, userId);
		DataElement dataElement = loadDataRules.getDataRules(operation, true);
		csvFileMap.clear();
		csvValueList = null;
		currentCSVfileName = null;
		oldCSVFileName = "";
		uniqueIdVal = null;
		errorList.clear();
		//postloadErrorList.clear();
		
		/*Section section = sectionService.getSection(sectionId);
		List<Result> resultList = section.getResults();
		resultList.sort((Result s1, Result s2)->s2.getResultId().intValue()-s1.getResultId().intValue()); 
		for(Result result: resultList)
		{
			ByteArrayInputStream bis = new ByteArrayInputStream(result.getWsResponseData());
			List<WSResponse> responses = null;
			try 
			{
				ObjectInputStream ois = new ObjectInputStream(bis);
				responses = (List<WSResponse>) ois.readObject();
				for(WSResponse wsResponse : responses)
				{
					if(wsResponse.getStatus().equalsIgnoreCase("Failure"))
					{
						errorList.add(wsResponse.getName());
						System.out.println(wsResponse.getName());
						System.out.println(wsResponse.getStatus());
					}					
				}
			} 
			catch (IOException | ClassNotFoundException e) 
			{
				e.printStackTrace();
			}
			break;
		}*/
		
		/*createCSVFileData(dataElement);
		if(csvFileMap.get(oldCSVFileName) == null)
		{
			csvFileMap.put(oldCSVFileName, csvValueList);
		}
		else
		{
			ArrayList<String> oldValList = (ArrayList<String>) csvFileMap.get(oldCSVFileName);
			for(int i = 0;i<csvValueList.size();i++)
			{
				if(!oldValList.contains(csvValueList.get(i)))
				{
					oldValList.add(csvValueList.get(i));
				}
			}
			csvFileMap.put(oldCSVFileName, oldValList);
		}*/
		JSONArray headingWd = createCSVTemplates(response, operation.getOperationName(), operation.getOperationId(), pageId, tenantId, userId);
		return headingWd;
	}
	
	/*private void createCSVFileData(DataElement rootElement) {
		
		List<DataElement> eleList = null;
		List<DataAttribute> rootAttrList = null;
		if(rootElement != null)
		{
			rootAttrList = rootElement.getAttributes();
			for(DataAttribute dataAttributeRoot : rootAttrList)
			{
				if("fileName".equalsIgnoreCase(dataAttributeRoot.getName()))
				{
					currentCSVfileName = dataAttributeRoot.getValue();					
				}
				if("uniqueId".equalsIgnoreCase(dataAttributeRoot.getName())) 
				{
					if(csvValueList == null)
					{
						csvValueList = new ArrayList<>();
						uniqueIdVal = dataAttributeRoot.getValue().substring(1, dataAttributeRoot.getValue().length());
						csvValueList.add(uniqueIdVal);
					}
					else
					{
						uniqueIdVal = dataAttributeRoot.getValue().substring(1, dataAttributeRoot.getValue().length());
					}
				}
			}
			if(!oldCSVFileName.equals("") && !currentCSVfileName.equalsIgnoreCase(oldCSVFileName))
			{
				if(csvFileMap.get(oldCSVFileName) == null)
				{
					csvFileMap.put(oldCSVFileName, csvValueList);
				}
				else
				{
					ArrayList<String> oldValList = (ArrayList<String>) csvFileMap.get(oldCSVFileName);
					for(int i = 0;i<csvValueList.size();i++)
					{
						if(!oldValList.contains(csvValueList.get(i)))
						{
							oldValList.add(csvValueList.get(i));
						}
					}
					csvFileMap.put(oldCSVFileName, oldValList);
				}
				csvValueList = null;
			}
			oldCSVFileName = currentCSVfileName;
			eleList = rootElement.getChildren();
			if(eleList.size() > 0)
			{
				for(DataElement dataElement : eleList)
				{
					if(dataElement.getValue().contains("$"))
					{
						if(csvValueList == null)
						{
							csvValueList = new ArrayList<>();
							csvValueList.add(uniqueIdVal);							
						}
						if(!csvValueList.contains(dataElement.getValue().substring(1, dataElement.getValue().length())))
						{
							csvValueList.add(dataElement.getValue().substring(1, dataElement.getValue().length()));
						}
					}					
					createCSVFileData(dataElement);
				}
			}			
		}
	}*/
	
	private JSONArray createCSVTemplates(HttpServletResponse response, String operationName, Long operationId, Long pageId, Long tenantId, Long userId) {
		
		columnList.clear();
		SOAPConnection soapConnection = null;
		int startIndex = 0;
		int endIndex = 0;
		JSONArray headingWd = null;
		InputStream is = null;
		String fileData = null;
		String newSource = "";
		int size = 0;
		int dataSize = 0;
		int addedSize = 0;
    	File file = null;
    	FileWriter csvWriter = null;
    	String[] result = null;
    	String fileName = null;
    	
		try 
		{
			file = File.createTempFile(operationName, ".csv");
			csvWriter = new FileWriter(file);
		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}
				
		/*Iterator<Map.Entry<String, List<String>>> itr = csvFileMap.entrySet().iterator();
		while(itr.hasNext()) 
        {
			size++;
			Map.Entry<String, List<String>> entry = itr.next();
        	String csvFile = entry.getKey().substring(0,entry.getKey().indexOf("."));
        	System.out.println(csvFile);
        	ArrayList<String> valueList = (ArrayList<String>) entry.getValue();
    		
			try 
			{
				for(int i = 0; i < valueList.size(); i++)
	        	{
					if(size >1 && i == 0)
					{
						csvWriter.append(",");
					}
					else
					{
						if(i == valueList.size() - 1)
						{
							csvWriter.append(valueList.get(i));
						}
						else
						{
							csvWriter.append(valueList.get(i));
							csvWriter.append(",");
						}
					}
	        	}				
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			} 
        }*/
				
		try 
		{
			List<MapFile> mapFileList = mapFileService.getMapFileListByOeration(operationId);
			/*if(mapFileList.size()>1)
			{
				mapFileList.sort((MapFile s1, MapFile s2)->s1.getMapFileId().intValue()-s2.getMapFileId().intValue()); 
			}*/
			for(MapFile mapFile: mapFileList)
			{
				dataSize++;
				csvWriter.append('\n');
				byte[] mapFileData = mapFile.getMapFileData();
				fileName = mapFile.getFileName();
				fileData = new String(mapFileData, "UTF-8");
				result = fileData.split("\\R", 2);
				if(dataSize == 1)
				{
					csvWriter.append(fileData);
				}
				else
				{					
					String resultNext = fileData;
					String[] resultNextArr = resultNext.split("\n");
					csvWriter.flush();
					String csvFile = file.getAbsolutePath();
					FileReader filereader = new FileReader(csvFile);
					CSVReader csvReader = new CSVReader(filereader);
					String[] nextRecord;
					while ((nextRecord = csvReader.readNext()) != null) 
		            {
						addedSize++;
						String cellValue = "";
						for (String cell : nextRecord) 
		                {
							if(cellValue.equals(""))
		                	{
		                		cellValue = cell;
		                	}
		                	else
		                	{
		                		cellValue = cellValue + "," + cell;
		                	}	  
		                }
						if(!cellValue.equals("") && addedSize == 1)
		            	{
		            		newSource = newSource + "\n" + cellValue + resultNextArr[addedSize-2].substring(resultNextArr[addedSize-2].indexOf(","), resultNextArr[addedSize-2].length()); 
		            	}
						if(!cellValue.equals("") && addedSize > 1)
						{
							if(fileName.endsWith(".csv"))
							{
								newSource = newSource + "\n" + cellValue + resultNextArr[addedSize-2].substring(resultNextArr[addedSize-2].indexOf(","), resultNextArr[addedSize-2].length());
							}
							else if(fileName.endsWith(".xlsx"))
							{
								newSource = newSource + "\n" + cellValue + resultNextArr[addedSize-2].substring(resultNextArr[addedSize-2].indexOf(",")+1, resultNextArr[addedSize-2].length());
							}
						}
		            }
					csvReader.close();
				}
			}			
			
			if(newSource.isEmpty() && dataSize == 1)
			{
				csvWriter.flush();
				String csvFile = file.getAbsolutePath();
				FileReader filereader = new FileReader(csvFile);
				CSVReader csvReader = new CSVReader(filereader);
				String[] nextRecord;
				while ((nextRecord = csvReader.readNext()) != null) 
	            {
					String cellValue = "";
					for (String cell : nextRecord) 
	                {
						if(cellValue.equals(""))
	                	{
	                		cellValue = cell;
	                	}
	                	else
	                	{
	                		cellValue = cellValue + "," + cell;
	                	}	  
	                }
					if(!cellValue.equals(""))
	            	{
	            		newSource = newSource + "\n" + cellValue; 
	            	}
	            }
				csvReader.close();
			}
			
			System.out.println(newSource);
			
			String line = "";
			int count = 0;
	        BufferedReader reader = new BufferedReader(new StringReader(newSource.trim()));
	        while ((line = reader.readLine()) != null) 
	        { 
	        	if(count!=0 && line.length() > 0) 
	            {
	        		//if(operationName.equalsIgnoreCase("Put_Applicant"))
	            	{
	        			columnList.add(line.substring(0, line.indexOf(",")));
	            	}
	            }
	        	count++;
	        }
			String[] newSrcHeading = newSource.split("\\R", 2);
			String[] newSrcHeadingArr = newSrcHeading[1].split("\n");
			headingFromSource = newSrcHeadingArr[0];
			System.out.println(headingFromSource);
			
			File sourceCSVfile = File.createTempFile(operationName, ".csv");
			PrintWriter writer = new PrintWriter(sourceCSVfile);
		    writer.write(newSource.toString().trim());
			writer.flush();
			writer.close();
			
			Page page = pageService.getPage(pageId);
			String loadCycle = page.getPageName();
			User user = userService.getUser(userId);
			tenant = tenantService.getTenant(tenantId);
			
			PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, operationName);
			if(postLoad == null)
			{
				postLoad = new PostLoad();
				postLoad.setLoadCycle(loadCycle);
				postLoad.setRuleName(operationName);
				postLoad.setSrcCSVFileName(operationName + ".csv");
				postLoad.setSrcCSVFileContent(Files.readAllBytes(sourceCSVfile.toPath()));
				postLoad.setUserId(userId);
				postLoad.setClient(user.getClient());
				postLoadService.addPostLoad(postLoad);
			}
			else
			{
				postLoad.setSrcCSVFileContent(Files.readAllBytes(sourceCSVfile.toPath()));
				postLoad.setClient(user.getClient());
				postLoadService.updatePostLoad(postLoad);
			}
			
			if(operationName.equalsIgnoreCase("Put_Applicant"))
			{
				headingWd = createCSVFromWDApplicant(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle, operationName, user.getClient());
			}
			else if(operationName.equalsIgnoreCase("Create_Position") || operationName.equalsIgnoreCase("Edit_Position"))
			{
				headingWd = createCSVFromWDPosition(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle, operationName, user.getClient());
			}
			else if(operationName.equalsIgnoreCase("Hire_Employee"))
			{
				headingWd = createCSVFromWDHire(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle, operationName, user.getClient());
			}
			else if(operationName.equalsIgnoreCase("Change_Government_IDs"))
			{
				headingWd = createCSVFromWDGovernmentId(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle, operationName, user.getClient());
			}
			else if(operationName.equalsIgnoreCase("Put_Worker_Photo"))
			{
				headingWd = createCSVFromWDWorkerPhoto(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle, operationName, user.getClient());
			}
			else if(operationName.equalsIgnoreCase("Terminate_Employee"))
			{
				headingWd = createCSVFromWDTermination(tenant, is, soapConnection, startIndex, endIndex, headingWd, loadCycle, operationName, user.getClient());
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return headingWd;

	}
	
	private JSONArray createCSVFromWDEmployeeId(Tenant tenant, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		headingFromWD = "";
		try 
		{			 
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_GOVERNMENT_ID_FILE = requestfile.getAbsolutePath();
				 
				 //String outputfile = GET_GOVERNMENT_ID_FILE;
				 //columnList.removeAll(errorList);
				 /*if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addWorkerIdListToFindError(GET_GOVERNMENT_ID_FILE, columnList.get(i), ruleName, "Employee_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement reportElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = reportElement.getChild("env:Body")
										.getChild("wd:Get_Workers_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
						 }
					 }
					 columnList.removeAll(errorList);
				 }*/
				 String outputfile = addHireIdList(GET_GOVERNMENT_ID_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");
				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");
				 
				 /*if(strMsg.contains("faultstring"))
				 {
					 ReportElement msgResponse = XmlParserManager.parseXml(strMsg);
					 ReportElement elementData = msgResponse.getChild("SOAP-ENV:Body")
								.getChild("SOAP-ENV:Fault");
					 faultStr = elementData.getChild("faultstring").getValue().trim();
					 System.out.println("faultStr-"+faultStr);
				 }*/
				 
				 //if(faultStr == null)
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String nationalId = "";
					 String nationalIdTypeCode = "";
					 String issuedDateNid = "";
					 String expieryDateNid = "";
					 String verificationDateNid = "";
					 String countryISOCodeNid = "";
					 String nationalIdArr = "";
					 String nationalIdTypeCodeArr = "";
					 String issuedDateNidArr = "";
					 String expieryDateNidArr = "";
					 String verificationDateNidArr = "";
					 String countryISOCodeNidArr = "";
					 String seriesNID = "";
					 String seriesNIDArr = "";
					 String issueingAgencyNID = "";
					 String issueingAgencyNIDArr = "";
					 String passportNumber = "";
					 String passportType = "";
					 String passportCountry = "";
					 String passportIssuedDate = "";
					 String passportExpirationDate = "";
					 String passportVerificationDate = "";
					 String passportNumberArr = "";
					 String passportTypeArr = "";
					 String passportCountryArr = "";
					 String passportIssuedDateArr = "";
					 String passportExpirationDateArr = "";
					 String passportVerificationDateArr = "";
					 String licenseNumber = "";
					 String licenseType = "";
					 String licenseClass = "";
					 String licenseClassArr = "";
					 String licenseAuthority = "";
					 String licenseAuthorityArr = "";
					 String licenseCountry = "";
					 String licenseCountryArr = "";
					 String licenseCountryRegion = "";
					 String licenseCountryRegionArr = "";
					 String licenseIssuedDate = "";
					 String licenseExpirationDate = "";
					 String licenseVerificationDate = "";
					 String licenseNumberArr = "";
					 String licenseTypeArr = "";
					 String licenseIssuedDateArr = "";
					 String licenseExpirationDateArr = "";
					 String licenseVerificationDateArr = "";
					 String govtId = "";
					 String govtIdType = "";
					 String goviIdCountry = "";
					 String govtIdIssuedDate = "";
					 String govtIdExpirationDate = "";
					 String govtIdIVerificationDate = "";
					 String govtIdVerifiedBy = "";
					 String govtIdArr = "";
					 String govtIdTypeArr = "";
					 String goviIdCountryArr = "";
					 String govtIdIssuedDateArr = "";
					 String govtIdExpirationDateArr = "";
					 String govtIdIVerificationDateArr = "";
					 String govtIdVerifiedByArr = "";
					 String visaId = "";
					 String visaType = "";
					 String visaCountry = "";
					 String visaIssuedDate = "";
					 String visaExpirationDate = "";
					 String visaVerificationDate = "";
					 String visaVerifiedBy = "";
					 String visaIdArr = "";
					 String visaTypeArr = "";
					 String visaCountryArr = "";
					 String visaIssuedDateArr = "";
					 String visaExpirationDateArr = "";
					 String visaVerificationDateArr = "";
					 String visaVerifiedByArr = "";
					 String customIdNumber = "";
					 String customType = "";
					 String customIdIssuedDate = "";
					 String customIdExpirationDate = "";
					 String customIdVerificationDate = "";
					 String customIdNumberArr = "";
					 String customTypeArr = "";
					 String customIdIssuedDateArr = "";
					 String customIdExpirationDateArr = "";
					 String customIdVerificationDateArr = "";
					 String finalStr = "";
					 String headerStr = "";
					 
					 Map<String,String> nationalIdMap = null;
					 Map<String,String> countryNidMap = null;
					 Map<String,String> passportTypeMap = null;
					 Map<String,String> passportCountryMap = null;
					 Map<String,String> licenseTypeMap = null;
					 Map<String,String> licenseAuthMap = null;
					 Map<String,String> licenseCountryMap = null;
					 Map<String,String> licenseCountryRegionMap = null;
					 Map<String,String> govtIdTypeMap = null;
					 Map<String,String> govtIdVerifiedByMap = null;
					 Map<String,String> govtCountryMap = null;
					 Map<String,String> visaIdTypeMap = null;
					 Map<String,String> visaCountryMap = null;
					 Map<String,String> visaVerifiedByMap = null;
					 Map<String,String> customTypeMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {
						if(j == 1)
						{
							startIndex = 0;
							if(1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*1000;
							}
						}
						else
						{
							//startIndex = (j - 1)*1000;
							int lastVal = (j - 1);
							startIndex = (endIndex - lastVal);
							if(j*1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								//endIndex = j*1000;
								endIndex = (j*startIndex) + 1;
							}
						}
						outputfile = addHireIdList(GET_GOVERNMENT_ID_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
						is = new FileInputStream(outputfile);
					    soapMessage = MessageFactory.newInstance().createMessage(null, is);
					    soapPart = soapMessage.getSOAPPart();
					    envelope = soapPart.getEnvelope();
						envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						{
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						}
						soapMessage.saveChanges();
				        out = new ByteArrayOutputStream();
				        soapMessage.writeTo(out);
				        strMsg = new String(out.toByteArray());
				        
				        soapConnectionFactory = SOAPConnectionFactory.newInstance();
						soapConnection = soapConnectionFactory.createConnection();
				        soapResponse = soapConnection.call(soapMessage, sourceUrl);
				        out = new ByteArrayOutputStream();
				        soapResponse.writeTo(out);
				        strMsg = new String(out.toByteArray(), "utf-8");
				        soapResp = XmlParserManager.parseXml(strMsg);
					        
						ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Workers_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> applicantData = responseData.getChildren("wd:Worker");
							
						 for(ReportElement reportElement : applicantData)
						 {
							 ReportElement element1 = reportElement.getChild("wd:Worker_Data");
							 String workerId = element1.getChild("wd:Worker_ID").getValue().trim();
							 System.out.println("workerId--"+ workerId);
							 ReportElement identificationData = reportElement.getChild("wd:Worker_Data")
							 			.getChild("wd:Personal_Data")
							 			.getChild("wd:Identification_Data");
							 
							 if(identificationData != null)
							 {									 
								 List<ReportElement> nationalIDList = identificationData.getChildren("wd:National_ID");
								 if(nationalIDList != null && nationalIDList.size() >0)
								 {
									 nationalIdArr = "";
									 nationalIdTypeCodeArr = "";
									 issuedDateNidArr = "";
									 expieryDateNidArr = "";
									 verificationDateNidArr = "";
									 countryISOCodeNidArr = "";
									 seriesNIDArr = "";
									 issueingAgencyNIDArr = "";
									 for(ReportElement nationalElement : nationalIDList)
									 {
										 ReportElement nationalIdData = nationalElement.getChild("wd:National_ID_Data");
										 if(nationalIdData != null)
										 {
											 nationalId = nationalIdData.getChild("wd:ID") != null?nationalIdData.getChild("wd:ID").getValue().trim():""; 
											 if(nationalIdArr.equals(""))
											 {
												 nationalIdArr = nationalId;
											 }
											 else
											 {
												 nationalIdArr = nationalIdArr + "~" + nationalId;
											 }
											 issuedDateNid = nationalIdData.getChild("wd:Issued_Date") != null?nationalIdData.getChild("wd:Issued_Date").getValue().trim():"";
											 if(!issuedDateNid.isEmpty())
											 {
												 issuedDateNid = issuedDateNid.substring(0, 10);
											 }
											 if(issuedDateNidArr.equals(""))
											 {
												 issuedDateNidArr = issuedDateNid;
											 }
											 else
											 {
												 issuedDateNidArr = issuedDateNidArr + "~" + issuedDateNid;
											 }
											 expieryDateNid = nationalIdData.getChild("wd:Expiration_Date") != null?nationalIdData.getChild("wd:Expiration_Date").getValue().trim():"";
											 if(!expieryDateNid.isEmpty())
											 {
												 expieryDateNid = expieryDateNid.substring(0, 10);
											 }
											 if(expieryDateNidArr.equals(""))
											 {
												 expieryDateNidArr = expieryDateNid;
											 }
											 else
											 {
												 expieryDateNidArr = expieryDateNidArr + "~" + expieryDateNid;
											 }
											 verificationDateNid = nationalIdData.getChild("wd:Verification_Date") != null?nationalIdData.getChild("wd:Verification_Date").getValue().trim():"";
											 if(!verificationDateNid.isEmpty())
											 {
												 verificationDateNid = verificationDateNid.substring(0, 10);
											 }
											 if(verificationDateNidArr.equals(""))
											 {
												 verificationDateNidArr = verificationDateNid;
											 }
											 else
											 {
												 verificationDateNidArr = verificationDateNidArr + "~" + verificationDateNid;
											 }
											 
											 seriesNID = nationalIdData.getChild("wd:Series") != null?nationalIdData.getChild("wd:Series").getValue().trim():""; 
											 if(seriesNIDArr.equals(""))
											 {
												 seriesNIDArr = seriesNID;
											 }
											 else
											 {
												 seriesNIDArr = seriesNIDArr + "~" + seriesNID;
											 }
											 
											 issueingAgencyNID = nationalIdData.getChild("wd:Series") != null?nationalIdData.getChild("wd:Series").getValue().trim():""; 
											 if(issueingAgencyNIDArr.equals(""))
											 {
												 issueingAgencyNIDArr = issueingAgencyNID;
											 }
											 else
											 {
												 issueingAgencyNIDArr = issueingAgencyNIDArr + "~" + issueingAgencyNID;
											 }
											 
											 ReportElement idTypeRef = nationalIdData.getChild("wd:ID_Type_Reference");
											 if(idTypeRef != null)
											 {
												 List<ReportElement> idTypeData = idTypeRef.getChildren("wd:ID");								 
												 for(ReportElement idTypeElement:idTypeData)
												 {
													 nationalIdMap = idTypeElement.getAllAttributes();
													 if(nationalIdMap.get("wd:type").equals("National_ID_Type_Code"))
													 {
														 nationalIdTypeCode = idTypeElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 nationalIdTypeCode = "";
											 }
											 if(nationalIdTypeCodeArr.equals(""))
											 {
												 nationalIdTypeCodeArr = nationalIdTypeCode;
											 }
											 else
											 {
												 nationalIdTypeCodeArr = nationalIdTypeCodeArr + "~" + nationalIdTypeCode;
											 }
											 
											 ReportElement countryReference = nationalIdData.getChild("wd:Country_Reference");
											 if(countryReference != null)
											 {
												 List<ReportElement> countryData = countryReference.getChildren("wd:ID");								 
												 for(ReportElement countryElement:countryData)
												 {
													 countryNidMap = countryElement.getAllAttributes();
													 if(countryNidMap.get("wd:type").equals("ISO_3166-1_Alpha-3_Code"))
													 {
														 countryISOCodeNid = countryElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 countryISOCodeNid = "";
											 }
											 if(countryISOCodeNidArr.equals(""))
											 {
												 countryISOCodeNidArr = countryISOCodeNid;
											 }
											 else
											 {
												 countryISOCodeNidArr = countryISOCodeNidArr + "~" + countryISOCodeNid;
											 }
										 }
									 }
								 }
								 else
								 {
									 nationalIdArr = "";
									 nationalIdTypeCodeArr = "";
									 issuedDateNidArr = "";
									 expieryDateNidArr = "";
									 verificationDateNidArr = "";
									 countryISOCodeNidArr = "";
									 seriesNIDArr = "";
									 issueingAgencyNIDArr = "";
								 }
								 
								 /*List<ReportElement> passportIDList = identificationData.getChildren("wd:Passport_ID");
								 if(passportIDList != null && passportIDList.size() >0)
								 {
									 passportNumberArr = "";
									 passportTypeArr = "";
									 passportCountryArr = "";
									 passportIssuedDateArr = "";
									 passportExpirationDateArr = "";
									 passportVerificationDateArr = "";
									 
									 for(ReportElement passportElement : passportIDList)
									 {
										 ReportElement passportIdData = passportElement.getChild("wd:Passport_ID_Data");
										 if(passportIdData != null)
										 {
											 passportNumber = passportIdData.getChild("wd:ID") != null?passportIdData.getChild("wd:ID").getValue().trim():""; 
											 if(passportNumberArr.equals(""))
											 {
												 passportNumberArr = passportNumber;
											 }
											 else
											 {
												 passportNumberArr = passportNumberArr + "~" + passportNumber;
											 }
											 passportIssuedDate = passportIdData.getChild("wd:Issued_Date") != null?passportIdData.getChild("wd:Issued_Date").getValue().trim():"";
											 if(passportIssuedDateArr.equals(""))
											 {
												 passportIssuedDateArr = passportIssuedDate;
											 }
											 else
											 {
												 passportIssuedDateArr = passportIssuedDateArr + "~" + passportIssuedDate;
											 }
											 passportExpirationDate = passportIdData.getChild("wd:Expiration_Date") != null?passportIdData.getChild("wd:Expiration_Date").getValue().trim():"";
											 if(passportExpirationDateArr.equals(""))
											 {
												 passportExpirationDateArr = passportExpirationDate;
											 }
											 else
											 {
												 passportExpirationDateArr = passportExpirationDateArr + "~" + passportExpirationDate;
											 }
											 passportVerificationDate = passportIdData.getChild("wd:Verification_Date") != null?passportIdData.getChild("wd:Verification_Date").getValue().trim():""; 
											 if(passportVerificationDateArr.equals(""))
											 {
												 passportVerificationDateArr = passportVerificationDate;
											 }
											 else
											 {
												 passportVerificationDateArr = passportVerificationDateArr + "~" + passportVerificationDate;
											 }
											 
											 ReportElement passportTypeRef = passportIdData.getChild("wd:ID_Type_Reference");
											 if(passportTypeRef != null)
											 {
												 List<ReportElement> passportIdTypeData = passportTypeRef.getChildren("wd:ID");								 
												 for(ReportElement idTypeElement:passportIdTypeData)
												 {
													 passportTypeMap = idTypeElement.getAllAttributes();
													 if(passportTypeMap.get("wd:type").equals("Passport_ID_Type_ID"))
													 {
														 passportType = idTypeElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 passportType = "";
											 }
											 if(passportTypeArr.equals(""))
											 {
												 passportTypeArr = passportType;
											 }
											 else
											 {
												 passportTypeArr = passportTypeArr + "~" + passportType;
											 }
											 
											 ReportElement passportCountryReference = passportIdData.getChild("wd:Country_Reference");
											 if(passportCountryReference != null)
											 {
												 List<ReportElement> passportCountryData = passportCountryReference.getChildren("wd:ID");								 
												 for(ReportElement countryElement:passportCountryData)
												 {
													 passportCountryMap = countryElement.getAllAttributes();
													 if(passportCountryMap.get("wd:type").equals("ISO_3166-1_Alpha-3_Code"))
													 {
														 passportCountry = countryElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 passportCountry = "";
											 }
											 if(passportCountryArr.equals(""))
											 {
												 passportCountryArr = passportCountry;
											 }
											 else
											 {
												 passportCountryArr = passportCountryArr + "~" + passportCountry;
											 }
										 }
									 }
								 }
								 else
								 {
									 passportNumberArr = "";
									 passportTypeArr = "";
									 passportCountryArr = "";
									 passportIssuedDateArr = "";
									 passportExpirationDateArr = "";
									 passportVerificationDateArr = "";
								 }*/
								 
								 List<ReportElement> governmentIDList = identificationData.getChildren("wd:Government_ID");
								 if(governmentIDList != null && governmentIDList.size() >0)
								 {
									 govtIdArr = "";
									 govtIdTypeArr = "";
									 goviIdCountryArr = "";
									 govtIdIssuedDateArr = "";
									 govtIdExpirationDateArr = "";
									 govtIdIVerificationDateArr = "";
									 govtIdVerifiedByArr = "";
									 
									 for(ReportElement governmentIdElement : governmentIDList)
									 {
										 ReportElement governmentIdData = governmentIdElement.getChild("wd:Government_ID_Data");
										 if(governmentIdData != null)
										 {
											 govtId = governmentIdData.getChild("wd:ID") != null?governmentIdData.getChild("wd:ID").getValue().trim():""; 
											 if(govtIdArr.equals(""))
											 {
												 govtIdArr = govtId;
											 }
											 else
											 {
												 govtIdArr = govtIdArr + "~" + govtId;
											 }
											 govtIdIssuedDate = governmentIdData.getChild("wd:Issued_Date") != null?governmentIdData.getChild("wd:Issued_Date").getValue().trim():"";
											 if(!govtIdIssuedDate.isEmpty())
											 {
												 govtIdIssuedDate = govtIdIssuedDate.substring(0, 10);
											 }
											 if(govtIdIssuedDateArr.equals(""))
											 {
												 govtIdIssuedDateArr = govtIdIssuedDate;
											 }
											 else
											 {
												 govtIdIssuedDateArr = govtIdIssuedDateArr + "~" + govtIdIssuedDate;
											 }
											 govtIdExpirationDate = governmentIdData.getChild("wd:Expiration_Date") != null?governmentIdData.getChild("wd:Expiration_Date").getValue().trim():"";
											 if(!govtIdExpirationDate.isEmpty())
											 {
												 govtIdExpirationDate = govtIdExpirationDate.substring(0, 10);
											 }
											 if(govtIdExpirationDateArr.equals(""))
											 {
												 govtIdExpirationDateArr = govtIdExpirationDate;
											 }
											 else
											 {
												 govtIdExpirationDateArr = govtIdExpirationDateArr + "~" + govtIdExpirationDate;
											 }
											 govtIdIVerificationDate = governmentIdData.getChild("wd:Verification_Date") != null?governmentIdData.getChild("wd:Verification_Date").getValue().trim():""; 
											 if(!govtIdIVerificationDate.isEmpty())
											 {
												 govtIdIVerificationDate = govtIdIVerificationDate.substring(0, 10);
											 }
											 if(govtIdIVerificationDateArr.equals(""))
											 {
												 govtIdIVerificationDateArr = govtIdIVerificationDate;
											 }
											 else
											 {
												 govtIdIVerificationDateArr = govtIdIVerificationDateArr + "~" + govtIdIVerificationDate;
											 }
											 
											 ReportElement govtTypeRef = governmentIdData.getChild("wd:ID_Type_Reference");
											 if(govtTypeRef != null)
											 {
												 List<ReportElement> govtIdTypeData = govtTypeRef.getChildren("wd:ID");								 
												 for(ReportElement idTypeElement:govtIdTypeData)
												 {
													 govtIdTypeMap = idTypeElement.getAllAttributes();
													 if(govtIdTypeMap.get("wd:type").equals("WID"))
													 {
														 govtIdType = idTypeElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 govtIdType = "";
											 }
											 if(govtIdTypeArr.equals(""))
											 {
												 govtIdTypeArr = govtIdType;
											 }
											 else
											 {
												 govtIdTypeArr = govtIdTypeArr + "~" + govtIdType;
											 }
											 
											 ReportElement govtCountryReference = governmentIdData.getChild("wd:Country_Reference");
											 if(govtCountryReference != null)
											 {
												 List<ReportElement> govtCountryData = govtCountryReference.getChildren("wd:ID");								 
												 for(ReportElement countryElement:govtCountryData)
												 {
													 govtCountryMap = countryElement.getAllAttributes();
													 if(govtCountryMap.get("wd:type").equals("ISO_3166-1_Alpha-3_Code"))
													 {
														 goviIdCountry = countryElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 goviIdCountry = "";
											 }
											 if(goviIdCountryArr.equals(""))
											 {
												 goviIdCountryArr = goviIdCountry;
											 }
											 else
											 {
												 goviIdCountryArr = goviIdCountryArr + "~" + goviIdCountry;
											 }
											 
											 ReportElement govtVerifiedByRef = governmentIdData.getChild("wd:Verified_by_Reference");
											 if(govtVerifiedByRef != null)
											 {
												 List<ReportElement> govtIdVerifiedByData = govtVerifiedByRef.getChildren("wd:ID");								 
												 for(ReportElement verifiedByElement:govtIdVerifiedByData)
												 {
													 govtIdVerifiedByMap = verifiedByElement.getAllAttributes();
													 if(govtIdVerifiedByMap.get("wd:type").equals("Employee_ID"))
													 {
														 govtIdVerifiedBy = verifiedByElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 govtIdVerifiedBy = "";
											 }
											 if(govtIdVerifiedByArr.equals(""))
											 {
												 govtIdVerifiedByArr = govtIdVerifiedBy;
											 }
											 else
											 {
												 govtIdVerifiedByArr = govtIdVerifiedByArr + "~" + govtIdVerifiedBy;
											 }
										 }
									 }
								 }
								 else
								 {
									 govtIdArr = "";
									 govtIdTypeArr = "";
									 goviIdCountryArr = "";
									 govtIdIssuedDateArr = "";
									 govtIdExpirationDateArr = "";
									 govtIdIVerificationDateArr = "";
									 govtIdVerifiedByArr = "";
								 }
								 
								 /*List<ReportElement> visaIDList = identificationData.getChildren("wd:Visa_ID");
								 if(visaIDList != null && visaIDList.size() >0)
								 {
									 visaIdArr = "";
									 visaTypeArr = "";
									 visaCountryArr = "";
									 visaIssuedDateArr = "";
									 visaExpirationDateArr = "";
									 visaVerificationDateArr = "";
									 visaVerifiedByArr = "";
									 
									 for(ReportElement visaElement : visaIDList)
									 {
										 ReportElement visaIdData = visaElement.getChild("wd:Visa_ID_Data");
										 if(visaIdData != null)
										 {
											 visaId = visaIdData.getChild("wd:ID") != null?visaIdData.getChild("wd:ID").getValue().trim():""; 
											 if(visaIdArr.equals(""))
											 {
												 visaIdArr = visaId;
											 }
											 else
											 {
												 visaIdArr = visaIdArr + "~" + visaId;
											 }
											 visaIssuedDate = visaIdData.getChild("wd:Issued_Date") != null?visaIdData.getChild("wd:Issued_Date").getValue().trim():"";
											 if(govtIdIssuedDateArr.equals(""))
											 {
												 visaIssuedDateArr = visaIssuedDate;
											 }
											 else
											 {
												 visaIssuedDateArr = visaIssuedDateArr + "~" + visaIssuedDate;
											 }
											 visaExpirationDate = visaIdData.getChild("wd:Expiration_Date") != null?visaIdData.getChild("wd:Expiration_Date").getValue().trim():"";
											 if(visaExpirationDateArr.equals(""))
											 {
												 visaExpirationDateArr = visaExpirationDate;
											 }
											 else
											 {
												 visaExpirationDateArr = visaExpirationDateArr + "~" + visaExpirationDate;
											 }
											 visaVerificationDate = visaIdData.getChild("wd:Verification_Date") != null?visaIdData.getChild("wd:Verification_Date").getValue().trim():""; 
											 if(visaVerificationDateArr.equals(""))
											 {
												 visaVerificationDateArr = visaVerificationDate;
											 }
											 else
											 {
												 visaVerificationDateArr = visaVerificationDateArr + "~" + visaVerificationDate;
											 }
											 
											 ReportElement visaTypeRef = visaIdData.getChild("wd:ID_Type_Reference");
											 if(visaTypeRef != null)
											 {
												 List<ReportElement> visaTypeData = visaTypeRef.getChildren("wd:ID");								 
												 for(ReportElement idTypeElement:visaTypeData)
												 {
													 visaIdTypeMap = idTypeElement.getAllAttributes();
													 if(visaIdTypeMap.get("wd:type").equals("Visa_ID_Type_ID"))
													 {
														 visaType = idTypeElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 visaType = "";
											 }
											 if(visaTypeArr.equals(""))
											 {
												 visaTypeArr = visaType;
											 }
											 else
											 {
												 visaTypeArr = visaTypeArr + "~" + visaType;
											 }
											 
											 ReportElement visaCountryReference = visaIdData.getChild("wd:Country_Reference");
											 if(visaCountryReference != null)
											 {
												 List<ReportElement> visaCountryData = visaCountryReference.getChildren("wd:ID");								 
												 for(ReportElement countryElement:visaCountryData)
												 {
													 visaCountryMap = countryElement.getAllAttributes();
													 if(visaCountryMap.get("wd:type").equals("ISO_3166-1_Alpha-3_Code"))
													 {
														 visaCountry = countryElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 visaCountry = "";
											 }
											 if(visaCountryArr.equals(""))
											 {
												 visaCountryArr = visaCountry;
											 }
											 else
											 {
												 visaCountryArr = visaCountryArr + "~" + visaCountry;
											 }
											 
											 ReportElement visaVerifiedByRef = visaIdData.getChild("wd:Verified_By_Reference");
											 if(visaVerifiedByRef != null)
											 {
												 List<ReportElement> visaVerifiedByData = visaVerifiedByRef.getChildren("wd:ID");								 
												 for(ReportElement verifiedByElement:visaVerifiedByData)
												 {
													 visaVerifiedByMap = verifiedByElement.getAllAttributes();
													 if(visaVerifiedByMap.get("wd:type").equals("Employee_ID"))
													 {
														 visaVerifiedBy = verifiedByElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 visaVerifiedBy = "";
											 }
											 if(visaVerifiedByArr.equals(""))
											 {
												 visaVerifiedByArr = visaVerifiedBy;
											 }
											 else
											 {
												 visaVerifiedByArr = visaVerifiedByArr + "~" + visaVerifiedBy;
											 }
										 }
									 }
								 }
								 else
								 {
									 visaIdArr = "";
									 visaTypeArr = "";
									 visaCountryArr = "";
									 visaIssuedDateArr = "";
									 visaExpirationDateArr = "";
									 visaVerificationDateArr = "";
									 visaVerifiedByArr = "";
								 }
								 
								 List<ReportElement> licenseIDList = identificationData.getChildren("wd:License_ID");
								 if(licenseIDList != null && licenseIDList.size() >0)
								 {
									 licenseNumberArr = "";
									 licenseTypeArr = "";
									 licenseIssuedDateArr = "";
									 licenseExpirationDateArr = "";
									 licenseVerificationDateArr = "";
									 licenseClassArr = "";
									 licenseAuthorityArr = "";
									 licenseCountryArr = "";
									 licenseCountryRegionArr = "";
									 
									 for(ReportElement licenseElement : licenseIDList)
									 {
										 ReportElement licenseIdData = licenseElement.getChild("wd:License_ID_Data");
										 if(licenseIdData != null)
										 {
											 licenseNumber = licenseIdData.getChild("wd:ID") != null?licenseIdData.getChild("wd:ID").getValue().trim():""; 
											 if(licenseNumberArr.equals(""))
											 {
												 licenseNumberArr = licenseNumber;
											 }
											 else
											 {
												 licenseNumberArr = licenseNumberArr + "~" + licenseNumber;
											 }
											 licenseIssuedDate = licenseIdData.getChild("wd:Issued_Date") != null?licenseIdData.getChild("wd:Issued_Date").getValue().trim():"";
											 if(licenseIssuedDateArr.equals(""))
											 {
												 licenseIssuedDateArr = licenseIssuedDate;
											 }
											 else
											 {
												 licenseIssuedDateArr = licenseIssuedDateArr + "~" + licenseIssuedDate;
											 }
											 licenseExpirationDate = licenseIdData.getChild("wd:Expiration_Date") != null?licenseIdData.getChild("wd:Expiration_Date").getValue().trim():"";
											 if(licenseExpirationDateArr.equals(""))
											 {
												 licenseExpirationDateArr = licenseExpirationDate;
											 }
											 else
											 {
												 licenseExpirationDateArr = licenseExpirationDateArr + "~" + licenseExpirationDate;
											 }
											 licenseVerificationDate = licenseIdData.getChild("wd:Verification_Date") != null?licenseIdData.getChild("wd:Verification_Date").getValue().trim():""; 
											 if(licenseVerificationDateArr.equals(""))
											 {
												 licenseVerificationDateArr = licenseVerificationDate;
											 }
											 else
											 {
												 licenseVerificationDateArr = licenseVerificationDateArr + "~" + licenseVerificationDate;
											 }
											 licenseClass = licenseIdData.getChild("wd:License_Class") != null?licenseIdData.getChild("wd:License_Class").getValue().trim():"";
											 if(licenseClassArr.equals(""))
											 {
												 licenseClassArr = licenseClass;
											 }
											 else
											 {
												 licenseClassArr = licenseClassArr + "~" + licenseClass;
											 }
											 
											 ReportElement licenseTypeRef = licenseIdData.getChild("wd:ID_Type_Reference");
											 if(licenseTypeRef != null)
											 {
												 List<ReportElement> licenseIdTypeData = licenseTypeRef.getChildren("wd:ID");								 
												 for(ReportElement idTypeElement:licenseIdTypeData)
												 {
													 licenseTypeMap = idTypeElement.getAllAttributes();
													 if(licenseTypeMap.get("wd:type").equals("License_ID_Type_ID"))
													 {
														 licenseType = idTypeElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 licenseType = "";
											 }
											 if(licenseTypeArr.equals(""))
											 {
												 licenseTypeArr = licenseType;
											 }
											 else
											 {
												 licenseTypeArr = licenseTypeArr + "~" + licenseType;
											 }
											 
											 ReportElement licenseAuthorityRef = licenseIdData.getChild("wd:Authority_Reference");
											 if(licenseAuthorityRef != null)
											 {
												 List<ReportElement> licenseAuthorityData = licenseAuthorityRef.getChildren("wd:ID");								 
												 for(ReportElement licenseAuthElement:licenseAuthorityData)
												 {
													 licenseAuthMap = licenseAuthElement.getAllAttributes();
													 if(licenseAuthMap.get("wd:type").equals("Authority_ID"))
													 {
														 licenseAuthority = licenseAuthElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 licenseAuthority = "";
											 }
											 
											 if(licenseAuthorityArr.equals(""))
											 {
												 licenseAuthorityArr = licenseAuthority;
											 }
											 else
											 {
												 licenseAuthorityArr = licenseAuthorityArr + "~" + licenseAuthority;
											 }
											 
											 ReportElement licenseCountryReference = licenseIdData.getChild("wd:Country_Reference");
											 if(licenseCountryReference != null)
											 {
												 List<ReportElement> licenseCountryData = licenseCountryReference.getChildren("wd:ID");								 
												 for(ReportElement countryElement:licenseCountryData)
												 {
													 licenseCountryMap = countryElement.getAllAttributes();
													 if(licenseCountryMap.get("wd:type").equals("ISO_3166-1_Alpha-3_Code"))
													 {
														 licenseCountry = countryElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 licenseCountry = "";
											 }
											 if(licenseCountryArr.equals(""))
											 {
												 licenseCountryArr = licenseCountry;
											 }
											 else
											 {
												 licenseCountryArr = licenseCountryArr + "~" + licenseCountry;
											 }
											 
											 ReportElement licenseCountryRegionReference = licenseIdData.getChild("wd:Country_Region_Reference");
											 if(licenseCountryRegionReference != null)
											 {
												 List<ReportElement> licenseCountryRegionData = licenseCountryRegionReference.getChildren("wd:ID");								 
												 for(ReportElement countryRegionElement:licenseCountryRegionData)
												 {
													 licenseCountryRegionMap = countryRegionElement.getAllAttributes();
													 if(licenseCountryRegionMap.get("wd:type").equals("Country_Region_ID"))
													 {
														 licenseCountryRegion = countryRegionElement.getValue().trim();
													 }
												 }
											 }
											 else
											 {
												 licenseCountryRegion = "";
											 }
											 if(licenseCountryRegionArr.equals(""))
											 {
												 licenseCountryRegionArr = licenseCountryRegion;
											 }
											 else
											 {
												 licenseCountryRegionArr = licenseCountryRegionArr + "~" + licenseCountryRegion;
											 }
										 }
									 }
								 }
								 else
								 {
									 licenseNumberArr = "";
									 licenseTypeArr = "";
									 licenseClassArr = "";
									 licenseAuthorityArr = "";
									 licenseCountryArr = "";
									 licenseCountryRegionArr = "";
									 licenseIssuedDateArr = "";
									 licenseExpirationDateArr = "";
									 licenseVerificationDateArr = "";
								 }
								 
								 List<ReportElement> customIDList = identificationData.getChildren("wd:Custom_ID");
								 if(customIDList != null && customIDList.size() >0)
								 {
									 customIdNumberArr = "" ;
									 customIdIssuedDateArr = "";
									 customIdExpirationDateArr = "";
									 customIdVerificationDateArr = "";
									 customType = "";
									 for(ReportElement customElement : customIDList)
									 {
										 ReportElement customIdData = customElement.getChild("wd:Custom_ID_Data");
										 if(customIdData != null)
										 {
											 ReportElement customTypeRef = customIdData.getChild("wd:ID_Type_Reference");
											 if(customTypeRef != null)
											 {
												 List<ReportElement> customIdTypeData = customTypeRef.getChildren("wd:ID");								 
												 for(ReportElement idTypeElement:customIdTypeData)
												 {
													 customTypeMap = idTypeElement.getAllAttributes();
													 if(customTypeMap.get("wd:type").equals("Custom_ID_Type_ID"))
													 {
														customType = idTypeElement.getValue().trim();														 
													 }
												 }
											 }
											 else
											 {
												 customType = "";
											 }
											 if(customTypeArr.equals(""))
											 {
												 customTypeArr = customType;
											 }
											 else
											 {
												 customTypeArr = customTypeArr + "~" + customType;
											 }
											 customIdNumber = customIdData.getChild("wd:ID") != null?customIdData.getChild("wd:ID").getValue().trim():""; 
											 if(customIdNumberArr.equals(""))
											 {
												 customIdNumberArr = customIdNumber;
											 }
											 else
											 {
												 customIdNumberArr = customIdNumberArr + "~" + customIdNumber;
											 }
											 customIdIssuedDate = customIdData.getChild("wd:Issued_Date") != null?customIdData.getChild("wd:Issued_Date").getValue().trim():"";
											 if(customIdIssuedDateArr.equals(""))
											 {
												 customIdIssuedDateArr = customIdIssuedDate;
											 }
											 else
											 {
												 customIdIssuedDateArr = customIdIssuedDateArr + "~" + customIdIssuedDate;
											 }
											 customIdExpirationDate = customIdData.getChild("wd:Expiration_Date") != null?customIdData.getChild("wd:Expiration_Date").getValue().trim():"";
											 if(customIdExpirationDateArr.equals(""))
											 {
												 customIdExpirationDateArr = customIdExpirationDate;
											 }
											 else
											 {
												 customIdExpirationDateArr = customIdExpirationDateArr + "~" + customIdExpirationDate;
											 }
											 customIdVerificationDate = customIdData.getChild("wd:Verification_Date") != null?customIdData.getChild("wd:Verification_Date").getValue().trim():"";
											 if(customIdVerificationDateArr.equals(""))
											 {
												 customIdVerificationDateArr = customIdVerificationDate;
											 }
											 else
											 {
												 customIdVerificationDateArr = customIdVerificationDateArr + "~" + customIdVerificationDate;
											 }
										 }
									 }
								 }
								 else
								 {
									 customIdNumberArr = "" ;
									 customIdIssuedDateArr = "";
									 customIdExpirationDateArr = "";
									 customIdVerificationDateArr = "";
									 customTypeArr = "";
								 }*/
							 }
							 else
							 {
								 customIdNumberArr = "" ;
								 customIdIssuedDateArr = "";
								 customIdExpirationDateArr = "";
								 customIdVerificationDateArr = "";
								 customTypeArr = "";
								 nationalIdArr = "";
								 nationalIdTypeCodeArr = "";
								 issuedDateNidArr = "";
								 expieryDateNidArr = "";
								 verificationDateNidArr = "";
								 countryISOCodeNidArr = "";
								 seriesNIDArr = "";
								 issueingAgencyNIDArr = "";
								 passportNumberArr = "";
								 passportTypeArr = "";
								 passportCountryArr = "";
								 passportIssuedDateArr = "";
								 passportExpirationDateArr = "";
								 passportVerificationDateArr = "";
								 govtIdArr = "";
								 govtIdTypeArr = "";
								 goviIdCountryArr = "";
								 govtIdIssuedDateArr = "";
								 govtIdExpirationDateArr = "";
								 govtIdIVerificationDateArr = "";
								 visaIdArr = "";
								 visaTypeArr = "";
								 visaCountryArr = "";
								 visaIssuedDateArr = "";
								 visaExpirationDateArr = "";
								 visaVerificationDateArr = "";
								 licenseNumberArr = "";
								 licenseTypeArr = "";
								 licenseIssuedDateArr = "";
								 licenseExpirationDateArr = "";
								 licenseVerificationDateArr = "";
							 }
							 headingFromWD = "Employee_ID,National_ID_Type_Code,National_ID,National_ID_Issued_Date,National_ID_Expiration_Date,National_ID_Verification_Date,Country_Issuing_National_ID,Series_National_ID,Issueing_Agency_National_ID,"
							 		+ "Passport_Number,Passport_Type,Passport_Issued_Date,Passport_Expiration_Date,Passport_Verification_Date,Passport_Country,License_Number,License_type,"
							 		+ "License_Class,License_Issueing_Authority,License_Country,License_Country_Region,License_Issued_Date,License_Expiration_Date,License_Verification_Date,"
							 		+ "Government_ID,Government_ID_Type,Country_Issueing_Government_ID,Government_ID_Issued_Date,Government_ID_Expiration_Date,Government_ID_Verification_Date,Government_ID_Verified_By,"
							 		+ "Visa_ID,Visa_ID_Type,Visa_Country,Visa_Issued_Date,Visa_Expiration_Date,Visa_Verification_Date,Visa_Verified_By,Custom_ID_Number,Custom_ID_Type,Custom_Issued_Date,"
							 		+ "Custom_Expiration_Date,Custom_Verification_Date";
							 
							 headerStr = workerId + "," + nationalIdTypeCodeArr + "," + nationalIdArr + "," + issuedDateNidArr + "," + expieryDateNidArr + "," + verificationDateNidArr + "," + countryISOCodeNidArr + "," + seriesNIDArr + "," + issueingAgencyNIDArr +
							 		 "," + passportNumberArr + "," + passportTypeArr + "," + passportIssuedDateArr +
							 		 "," + passportExpirationDateArr + "," + passportVerificationDateArr + "," + passportCountryArr + "," + licenseNumberArr + "," + licenseTypeArr  +
							 		 "," + licenseClassArr + "," + licenseAuthorityArr + "," + licenseCountryArr + "," + licenseCountryRegionArr + "," + licenseIssuedDateArr + "," + licenseExpirationDateArr + "," + licenseVerificationDateArr +
							 		 "," + govtIdArr + "," + govtIdTypeArr + "," + goviIdCountryArr + "," + govtIdIssuedDateArr + "," + govtIdExpirationDateArr + "," + govtIdIVerificationDateArr + "," + govtIdVerifiedByArr +
							 		 "," + visaIdArr + "," + visaTypeArr + "," + visaCountryArr + "," + visaIssuedDateArr + "," + visaExpirationDateArr + "," + visaVerificationDateArr + "," + visaVerifiedByArr + "," + customIdNumberArr +
							 		 "," + customTypeArr + "," + customIdIssuedDateArr + "," + customIdExpirationDateArr + "," + customIdVerificationDateArr;
							 
							 if(finalStr.equals(""))
							 {
								 finalStr = headingFromWD + "\n" + headerStr;
							 }
							 else
							 {
								 finalStr = finalStr + "\n" + headerStr;
							 }
						 }						 
					 }				 
					 				     
				     System.out.println(finalStr);
					 
					 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
					 PrintWriter writer = new PrintWriter(wdCSVfile);
					 writer.write(finalStr.toString());
					 writer.flush();
					 writer.close();
					 
					 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
					 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
					 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
					 postLoadService.updatePostLoad(postLoad);
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
				 }
			 }
			 
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return headingWd;
	}
	
	private JSONArray createCSVFromWDOrganizationRoles(Tenant tenant, InputStream is,
			SOAPConnection soapConnection, int startIndex, int endIndex, JSONArray headingWd, String loadCycle,
			String ruleName, String client) {

		targetContent = null;
		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_ASSIGN_ORG_ROLE_REQUEST_FILE = requestfile.getAbsolutePath();
				 //String outputfile = GET_SUP_ORG_REQUEST_FILE;
				 //columnList.removeAll(errorList);
				 /*if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addMasterDataListToFindError(GET_ASSIGN_ORG_ROLE_REQUEST_FILE, columnList.get(i), ruleName, "Organization_Reference_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement reportElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = reportElement.getChild("env:Body")
										.getChild("wd:Get_Organizations_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
						 }
					 }
					 columnList.removeAll(errorList);
				 }*/
				 String outputfile = addCostCenterIdList(GET_ASSIGN_ORG_ROLE_REQUEST_FILE, columnList, ruleName, startIndex, columnList.size(), "Organization_Reference_ID");

				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");
				 
				 //if(faultStr == null)
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Organizations_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String finalStr = "";
					 String headerStr = "";
					 String orgCode = "";
					 String orgType = "";
					 String orgRef = "";
					 String managerRole = "";
					 
					 Map<String,String> managerMap = null;
					 Map<String,String> orgTypeMap = null;
					 Map<String,String> orgMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {					 
						 if(j == 1)
						 {
							startIndex = 0;
							if(999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*999;
							}
						 }
						 else
						 {
							startIndex = endIndex;
							if(j*999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = (j*999);
							}
						 }
						 outputfile = addCostCenterIdList(GET_ASSIGN_ORG_ROLE_REQUEST_FILE, columnList, ruleName, startIndex, endIndex, "Organization_Reference_ID");
						 is = new FileInputStream(outputfile);
					     soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     soapPart = soapMessage.getSOAPPart();
					     envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
				         out = new ByteArrayOutputStream();
				         soapMessage.writeTo(out);
				         strMsg = new String(out.toByteArray());
				        
				         soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
				         soapResponse = soapConnection.call(soapMessage, sourceUrl);
				         out = new ByteArrayOutputStream();
				         soapResponse.writeTo(out);
				         strMsg = new String(out.toByteArray(), "utf-8");
				        
				         soapResp = XmlParserManager.parseXml(strMsg);
					 
						 ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Organizations_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> orgData = responseData.getChildren("wd:Organization");
							
						 for(ReportElement reportElement : orgData)
						 {
							 ReportElement orgReference = reportElement.getChild("wd:Organization_Reference");
							 if(orgReference != null)
							 {
								 List<ReportElement> orgRefData = orgReference.getChildren("wd:ID");					 
								 for(ReportElement orgReferenceElement:orgRefData)
								 {
									 orgMap = orgReferenceElement.getAllAttributes();
									 if(orgMap.get("wd:type").equals("Organization_Reference_ID"))
									 {
										 orgRef = orgReferenceElement.getValue().trim();
									 }
								 }
							 }
							 
							 ReportElement organizationData = reportElement.getChild("wd:Organization_Data");
							 if(organizationData != null)
							 {								 
								 ReportElement orgTypeRef = organizationData.getChild("wd:Organization_Type_Reference");
								 if(orgTypeRef != null)
								 {
									 List<ReportElement> orgTypeData = orgTypeRef.getChildren("wd:ID");					 
									 for(ReportElement orgTypeElement:orgTypeData)
									 {
										 orgTypeMap = orgTypeElement.getAllAttributes();
										 if(orgTypeMap.get("wd:type").equals("Organization_Type_ID"))
										 {
											 orgType = orgTypeElement.getValue().trim();
										 }
									 }
								 }
								 
								 if(orgType.equalsIgnoreCase("SUPERVISORY") || orgType.equalsIgnoreCase("MATRIX"))
								 {
									 ReportElement roleData = organizationData.getChild("wd:Roles_Data");
									 if(roleData != null)
									 {
										 List<ReportElement> orgRoleList = roleData.getChildren("wd:Organization_Role_Data");
										 if(orgRoleList != null && orgRoleList.size() > 0)
										 {
											 for(ReportElement orgRoleElement : orgRoleList)
											 {
												 ReportElement roleRef = orgRoleElement.getChild("wd:Role_Reference");
												 if(roleRef != null)
												 {
													 List<ReportElement> orgRoleData = roleRef.getChildren("wd:ID");								 
													 for(ReportElement roleElement:orgRoleData)
													 {
														 managerMap = roleElement.getAllAttributes();
														 if(managerMap.get("wd:type").equals("Organization_Role_ID"))
														 {
															 if(roleElement.getValue().trim().equalsIgnoreCase("Manager") || roleElement.getValue().trim().equalsIgnoreCase("Matrix_Manager"))
															 {
																 managerRole = roleElement.getValue().trim();																 
															 }
														 }
													 }
												 }
											 }
										 }
									 }
									 
									 headingFromWD = "Organization_Reference_ID,Organization_Manager_Role";
									 headerStr = orgRef + "," + managerRole;
									 
									 if(finalStr.equals(""))
									 {
										 finalStr = headingFromWD + "\n" + headerStr;
									 }
									 else
									 {
										 finalStr = finalStr + "\n" + headerStr;
									 }
									 			
								 }								 								 				 							 
							 }					 
						 }
					 }
					 
					 System.out.println(finalStr);
					 targetContent = finalStr.toString().getBytes();
					 
					 /*File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
					 PrintWriter writer = new PrintWriter(wdCSVfile);
					 writer.write(finalStr.toString());
					 writer.flush();
					 writer.close();
					 
					 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
					 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
					 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
					 postLoadService.updatePostLoad(postLoad);*/
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Organization_Reference_ID");
					 complete = true;
				 }
			 }
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}
	
	private JSONArray createCSVFromWDHostCNUM(Tenant tenant, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		targetContent = null;
		headingFromWD = "";
		try 
		{			 
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_HOST_CNUM_REQUEST_FILE = requestfile.getAbsolutePath();
				 
				 //String outputfile = GET_GOVERNMENT_ID_FILE;
				 //columnList.removeAll(errorList);
				 /*if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addWorkerIdListToFindError(GET_HOST_CNUM_REQUEST_FILE, columnList.get(i), ruleName, "Employee_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement reportElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = reportElement.getChild("env:Body")
										.getChild("wd:Get_Workers_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
						 }
					 }
					 columnList.removeAll(errorList);
				 }*/
				 String outputfile = addHireIdList(GET_HOST_CNUM_REQUEST_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");
				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     //String sourceUrl = "https://wd5-services1.myworkday.com/ccx/service/Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");
				 
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String customIdNumber = "";
					 String customIdNumberArr = "";
					 String customDesc = "";
					 String customDescArr = "";
					 String customType = "";
					 String actCustomType = "";
					 String actCustomTypeArr = "";
					 String customIdIssuedDate = "";
					 String customIdIssuedDateArr = "";
					 String customIdExpirationDate = "";
					 String customIdExpirationDateArr = "";
					 String customShareId = "";
					 String customShareIdArr = "";
					 String finalStr = "";
					 String headerStr = "";
					 
					 Map<String,String> customTypeMap = null;
					 Map<String,String> customShareMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {
						 if(j == 1)
						 {
							startIndex = 0;
							if(999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*999;
							}
						 }
						 else
						 {
							startIndex = endIndex;
							if(j*999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = (j*999);
							}
						 }
						outputfile = addHireIdList(GET_HOST_CNUM_REQUEST_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
						is = new FileInputStream(outputfile);
					    soapMessage = MessageFactory.newInstance().createMessage(null, is);
					    soapPart = soapMessage.getSOAPPart();
					    envelope = soapPart.getEnvelope();
						envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						{
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						}
						soapMessage.saveChanges();
				        out = new ByteArrayOutputStream();
				        soapMessage.writeTo(out);
				        strMsg = new String(out.toByteArray());
				        
				        soapConnectionFactory = SOAPConnectionFactory.newInstance();
						soapConnection = soapConnectionFactory.createConnection();
				        soapResponse = soapConnection.call(soapMessage, sourceUrl);
				        out = new ByteArrayOutputStream();
				        soapResponse.writeTo(out);
				        strMsg = new String(out.toByteArray(), "utf-8");
				        soapResp = XmlParserManager.parseXml(strMsg);
					        
						ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Workers_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> applicantData = responseData.getChildren("wd:Worker");
							
						 for(ReportElement reportElement : applicantData)
						 {
							 ReportElement element1 = reportElement.getChild("wd:Worker_Data");
							 String workerId = element1.getChild("wd:Worker_ID").getValue().trim();
							 System.out.println("workerId--"+ workerId);
							 ReportElement identificationData = reportElement.getChild("wd:Worker_Data")
							 			.getChild("wd:Personal_Data")
							 			.getChild("wd:Identification_Data");
							 
							 if(identificationData != null)
							 {									 								 
								 List<ReportElement> customIDList = identificationData.getChildren("wd:Custom_ID");
								 if(customIDList != null && customIDList.size() >0)
								 {
									 customIdNumberArr = "" ;
									 customIdIssuedDateArr = "";
									 customType = "";
									 customIdExpirationDateArr = "";
									 customShareIdArr = "";
									 customDescArr = "";
									 actCustomTypeArr = "";
									 
									 for(ReportElement customElement : customIDList)
									 {
										 ReportElement customIdData = customElement.getChild("wd:Custom_ID_Data");
										 if(customIdData != null)
										 {
											 if(customIdData != null)
											 {
												 ReportElement customTypeRef = customIdData.getChild("wd:ID_Type_Reference");
												 if(customTypeRef != null)
												 {
													 List<ReportElement> customIdTypeData = customTypeRef.getChildren("wd:ID");								 
													 for(ReportElement idTypeElement:customIdTypeData)
													 {
														 customTypeMap = idTypeElement.getAllAttributes();
														 if(customTypeMap.get("wd:type").equals("Custom_ID_Type_ID"))
														 {
															customType = idTypeElement.getValue().trim();
															if(customType.equalsIgnoreCase("Global_Assignment_CNUM") || customType.equalsIgnoreCase("UK_CNUM"))
															{
																 actCustomType = customType;
																 if(actCustomTypeArr.equals(""))
																 {
																	 actCustomTypeArr = actCustomType;
																 }
																 else
																 {
																	 actCustomTypeArr = actCustomTypeArr + "~" + actCustomType;
																 }
																 customIdNumber = customIdData.getChild("wd:ID") != null?customIdData.getChild("wd:ID").getValue().trim():"";
																 if(customIdNumberArr.equals(""))
																 {
																	 customIdNumberArr = customIdNumber;
																 }
																 else
																 {
																	 customIdNumberArr = customIdNumberArr + "~" + customIdNumber;
																 }
																 customDesc = customIdData.getChild("wd:Custom_Description") != null?customIdData.getChild("wd:Custom_Description").getValue().trim():"";
																 customDesc = customDesc.replaceAll("\\s", "");
																 if(customDescArr.equals(""))
																 {
																	 customDescArr = customDesc;
																 }
																 else
																 {
																	 if(!customDesc.isEmpty())
																	 {
																		 customDescArr = customDescArr + "~" + customDesc;
																	 }
																 }
																 customIdIssuedDate = customIdData.getChild("wd:Issued_Date") != null?customIdData.getChild("wd:Issued_Date").getValue().trim():"";
																 if(!customIdIssuedDate.isEmpty())
																 {
																	 customIdIssuedDate = customIdIssuedDate.substring(0, 10);
																	 customIdIssuedDate = convertDate(customIdIssuedDate, "yyyy-MM-dd", "dd-MM-yyyy");
																 }
																 if(customIdIssuedDateArr.equals(""))
																 {
																	 customIdIssuedDateArr = customIdIssuedDate;
																 }
																 else
																 {
																	 if(!customIdIssuedDate.isEmpty())
																	 {
																		 customIdIssuedDateArr = customIdIssuedDateArr + "~" + customIdIssuedDate;
																	 }
																 }
																 customIdExpirationDate = customIdData.getChild("wd:Expiration_Date") != null?customIdData.getChild("wd:Expiration_Date").getValue().trim():"";
																 if(!customIdExpirationDate.isEmpty())
																 {
																	 customIdExpirationDate = customIdExpirationDate.substring(0, 10);
																	 customIdExpirationDate = convertDate(customIdExpirationDate, "yyyy-MM-dd", "dd-MM-yyyy");
																 }
																 if(customIdExpirationDateArr.equals(""))
																 {
																	 customIdExpirationDateArr = customIdExpirationDate;
																 }
																 else
																 {
																	 customIdExpirationDateArr = customIdExpirationDateArr + "~" + customIdExpirationDate;
																 }
																 
																 ReportElement customSharedRef = customElement.getChild("wd:Custom_ID_Shared_Reference");
																 if(customSharedRef != null)
																 {
																	 List<ReportElement> customIdSharedData = customSharedRef.getChildren("wd:ID");								 
																	 for(ReportElement sharedElement:customIdSharedData)
																	 {
																		 customShareMap = sharedElement.getAllAttributes();
																		 if(customShareMap.get("wd:type").equals("Custom_Identifier_Reference_ID"))
																		 {
																			customShareId = sharedElement.getValue().trim();															 
																		 }
																	 }
																 }
																 if(customShareIdArr.equals(""))
																 {
																	 customShareIdArr = customShareId;
																 }
																 else
																 {
																	 customShareIdArr = customShareIdArr + "~" + customShareId;
																 }
																 
																 /*headingFromWD = "Employee_ID,Host_CNUM,Custom_ID_Type,Custom_Description,Issued_Date,Expiration_Date,Custom_ID_Shared_Reference";
																 
																 headerStr = workerId + "," + customIdNumberArr + "," + customType + "," + customDescArr + "," + customIdIssuedDateArr + "," + customIdExpirationDateArr + "," + customShareIdArr;
																 
																 if(finalStr.equals(""))
																 {
																	 finalStr = headingFromWD + "\n" + headerStr;
																 }
																 else
																 {
																	 finalStr = finalStr + "\n" + headerStr;
																 }*/
															 }
														 }
													 }
												 }												 
											 }
										 }
									 }
									 headingFromWD = "Employee_ID,Host_CNUM,Custom_ID_Type,Custom_Description,Issued_Date,Expiration_Date,Custom_ID_Shared_Reference";
									 
									 headerStr = workerId + "," + customIdNumberArr + "," + actCustomTypeArr + "," + customDescArr + "," + customIdIssuedDateArr + "," + customIdExpirationDateArr + "," + customShareIdArr;
									 
									 if(finalStr.equals(""))
									 {
										 finalStr = headingFromWD + "\n" + headerStr;
									 }
									 else
									 {
										 finalStr = finalStr + "\n" + headerStr;
									 }
								 }
							 }							 
						 }						 
					 }				 
					 				     
				     System.out.println(finalStr);
				     targetContent = finalStr.toString().getBytes();
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
					 complete = true;
				 }
			 }
			 
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return headingWd;
	}
	
	private JSONArray createCSVFromWDCarryoverBalances(Tenant tenant, InputStream is,
			SOAPConnection soapConnection, int startIndex, int endIndex, JSONArray headingWd, String loadCycle,
			String ruleName, String client) {

		targetContent = null;
		String checkFile = null;
		String workerId = "";
		String timeOffPlanName = "";
		String overrideBalanceDate = "";
		String overrideBalanceUnits = "";
		String carryoverDate = "";
		String carryoverExpirationDate = "";
		String carryoverOverrideBalanceUnit = "";
		String timeOffPlanNameArr = "";
		String overrideBalanceDateArr = "";
		String overrideBalanceUnitsArr = "";
		String carryoverDateArr = "";
		String carryoverExpirationDateArr = "";
		String carryoverOverrideBalanceUnitArr = "";
		String finalStr = "";
		String headerStr = "";
		
		Map<String,String> workerMap = null;
		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_CARRYOVER_BALANCE_REQUEST_FILE = requestfile.getAbsolutePath();
				 if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addCarryoverBalanceListToFindError(GET_CARRYOVER_BALANCE_REQUEST_FILE, columnList.get(i), ruleName, "Employee_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Absence_Management";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement rptElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement resultsData = rptElement.getChild("env:Body")
										.getChild("wd:Get_Override_Balances_Response")
										.getChild("wd:Response_Results");
							 
							 String result = resultsData.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
							 else
							 {
								 ReportElement responseData = rptElement.getChild("env:Body")
											.getChild("wd:Get_Override_Balances_Response")
											.getChild("wd:Response_Data");
								 
								 List<ReportElement> carryoverBalanceList = responseData.getChildren("wd:Override_Balance");
								 if(carryoverBalanceList != null && carryoverBalanceList.size()>0)
								 {
									 timeOffPlanNameArr = "";
									 overrideBalanceDateArr = "";
								     overrideBalanceUnitsArr = "";
								     carryoverDateArr = "";
									 carryoverExpirationDateArr = "";
									 carryoverOverrideBalanceUnitArr = "";
										
									 for(ReportElement reportElement : carryoverBalanceList)
									 {
										 ReportElement overrideBalanceData = reportElement.getChild("wd:Override_Balance_Data");
										 if(overrideBalanceData != null)
										 {
											 ReportElement workerRef = overrideBalanceData.getChild("wd:Worker_Reference");
											 if(workerRef != null)
											 {
												 List<ReportElement> workerData = workerRef.getChildren("wd:ID");								 
												 for(ReportElement workerElement:workerData)
												 {
													 workerMap = workerElement.getAllAttributes();
													 if(workerMap.get("wd:type").equals("Employee_ID"))
													 {
														 workerId = workerElement.getValue().trim();													 
													 }
												 }
											 }
											 ReportElement timeOffPlanRef = overrideBalanceData.getChild("wd:Time_Off_Plan_Reference");
											 if(timeOffPlanRef != null)
											 {													 
												 timeOffPlanName = timeOffPlanRef.getChild("wd:Time_Off_Plan_Name") != null?timeOffPlanRef.getChild("wd:Time_Off_Plan_Name").getValue().trim():"";
												 if(timeOffPlanName.contains(","))
												 {
													 timeOffPlanName = timeOffPlanName.replaceAll(",", "|");
												 }
											 }
											 if(timeOffPlanNameArr.equals(""))
											 {
												 timeOffPlanNameArr = timeOffPlanName;
											 }
											 else
											 {
												 timeOffPlanNameArr = timeOffPlanNameArr + "~" + timeOffPlanName;
											 }
											 overrideBalanceDate = overrideBalanceData.getChild("wd:Override_Balance_Date") != null?overrideBalanceData.getChild("wd:Override_Balance_Date").getValue().trim():"";
											 if(!overrideBalanceDate.isEmpty())
											 {
												 overrideBalanceDate = overrideBalanceDate.substring(0, 10);
												 overrideBalanceDate = convertDate(overrideBalanceDate, "yyyy-MM-dd", "dd-MM-yyyy");
											 }
											 if(overrideBalanceDateArr.equals(""))
											 {
												 overrideBalanceDateArr = overrideBalanceDate;
											 }
											 else
											 {
												 overrideBalanceDateArr = overrideBalanceDateArr + "~" + overrideBalanceDate;
											 }
											 overrideBalanceUnits = overrideBalanceData.getChild("wd:Override_Balance_Units") != null?overrideBalanceData.getChild("wd:Override_Balance_Units").getValue().trim():"";
											 if(overrideBalanceUnitsArr.equals(""))
											 {
												 overrideBalanceUnitsArr = overrideBalanceUnits;
											 }
											 else
											 {
												 overrideBalanceUnitsArr = overrideBalanceUnitsArr + "~" + overrideBalanceUnits;
											 }
											 ReportElement overrideBalanceUnitData = overrideBalanceData.getChild("wd:Override_Balance_Units_Data");
											 if(overrideBalanceUnitData != null)
											 {
												 carryoverDate = overrideBalanceUnitData.getChild("wd:Carryover_Date") != null?overrideBalanceUnitData.getChild("wd:Carryover_Date").getValue().trim():"";
												 if(!carryoverDate.isEmpty())
												 {
													 carryoverDate = carryoverDate.substring(0, 10);
													 carryoverDate = convertDate(carryoverDate, "yyyy-MM-dd", "dd-MM-yyyy");
												 }
												 if(carryoverDateArr.equals(""))
												 {
													 carryoverDateArr = carryoverDate;
												 }
												 else
												 {
													 carryoverDateArr = carryoverDateArr + "~" + carryoverDate;
												 }
												 carryoverExpirationDate = overrideBalanceUnitData.getChild("wd:Carryover_Expiration_Date") != null?overrideBalanceUnitData.getChild("wd:Carryover_Expiration_Date").getValue().trim():"";
												 if(!carryoverExpirationDate.isEmpty())
												 {
													 carryoverExpirationDate = carryoverExpirationDate.substring(0, 10);
													 carryoverExpirationDate = convertDate(carryoverExpirationDate, "yyyy-MM-dd", "dd-MM-yyyy");
												 }
												 if(carryoverExpirationDateArr.equals(""))
												 {
													 carryoverExpirationDateArr = carryoverExpirationDate;
												 }
												 else
												 {
													 carryoverExpirationDateArr = carryoverExpirationDateArr + "~" + carryoverExpirationDate;
												 }
												 carryoverOverrideBalanceUnit = overrideBalanceUnitData.getChild("wd:Carryover_Override_Balance_Units") != null?overrideBalanceUnitData.getChild("wd:Carryover_Override_Balance_Units").getValue().trim():"";
												 if(carryoverOverrideBalanceUnitArr.equals(""))
												 {
													 carryoverOverrideBalanceUnitArr = carryoverOverrideBalanceUnit;
												 }
												 else
												 {
													 carryoverOverrideBalanceUnitArr = carryoverOverrideBalanceUnitArr + "~" + carryoverOverrideBalanceUnit;
												 }
											 }
										 }
									 }
									 headingFromWD = "Employee_ID,Time_Off_Plan_Name,Override_Balance_Date,Override_Balance_Units,Carryover_Date,Carryover_Expiration_Date,Carryover_Override_Balance_Units";
									 
									 headerStr = workerId + "," + timeOffPlanNameArr + "," + overrideBalanceDateArr + "," + overrideBalanceUnitsArr + "," + carryoverDate + "," + carryoverExpirationDate
											 		+ "," + carryoverOverrideBalanceUnitArr;
									 
									 if(finalStr.equals(""))
									 {
										 finalStr = headingFromWD + "\n" + headerStr;
									 }
									 else
									 {
										 finalStr = finalStr + "\n" + headerStr;
									 }
								 }
							 }
						 }
					 }
					 columnList.removeAll(errorList);
					 wdCount = columnList.size();
				 }
				 System.out.println(finalStr);
				 targetContent = finalStr.toString().getBytes();
				 
				 /*File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
				 PrintWriter writer = new PrintWriter(wdCSVfile);
				 writer.write(finalStr.toString());
				 writer.flush();
				 writer.close();
				 
				 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
				 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
				 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
				 postLoadService.updatePostLoad(postLoad);*/
				 
				 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
				 complete = true;
			}
			 
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}

	private String addCarryoverBalanceListToFindError(String xmlFile, String columnVal, String ruleName, String idVal) {
		
		File updatedRequestfile = null;
		try  
		{  
			File file = new File(xmlFile);     
			FileReader fr = new FileReader(file);   
			BufferedReader br = new BufferedReader(fr);   
			StringBuffer sb = new StringBuffer();     
			String line; 
			boolean executed = true;
			while((line=br.readLine())!=null)  
			{  
				sb.append(line);  
				sb.append("\n");
				if(line.contains("bsvc:Get_Override_Balances_Request") && executed)
				{
					executed = false;
					sb.append(" <bsvc:Request_Criteria>"); 
					sb.append("\n");
						sb.append("  <bsvc:Employee_Reference bsvc:Descriptor=" + "\"" + idVal + "\"" + ">");
						sb.append("\n");
						sb.append("   <bsvc:ID bsvc:type=" + "\"" + idVal + "\"" + ">" + columnVal + "</bsvc:ID>");
						sb.append("\n");
						sb.append("  </bsvc:Employee_Reference>");
						sb.append("\n");					
					sb.append(" </bsvc:Request_Criteria>");
					sb.append("\n");
				}
			}  
			fr.close();
			br.close();
			
			updatedRequestfile = File.createTempFile(ruleName + "_" + columnVal , ".xml");
			PrintWriter writer = new PrintWriter(updatedRequestfile);
		    writer.write(sb.toString());
			writer.flush();
			writer.close();
		}  
		catch(IOException e)  
		{  
			e.printStackTrace();  
		}
		return updatedRequestfile.getAbsolutePath(); 
	}
	
	private JSONArray createCSVFromWDSystemUserAccount(Tenant tenant, InputStream is,
			SOAPConnection soapConnection, int startIndex, int endIndex, JSONArray headingWd, String loadCycle,
			String ruleName, String client) {

		String checkFile = null;
		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_SYSTEM_USER_ACCOUNT_REQUEST_FILE = requestfile.getAbsolutePath();
				 if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addUserAccountToFindError(GET_SYSTEM_USER_ACCOUNT_REQUEST_FILE, columnList.get(i), ruleName, "Employee_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Human_Resources";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement reportElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = reportElement.getChild("env:Body")
										.getChild("wd:Get_Workday_Account_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
						 }
					 }
					 columnList.removeAll(errorList);
				 }
				 String outputfile = addSystemUserAccountList(GET_SYSTEM_USER_ACCOUNT_REQUEST_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");

				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Human_Resources";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");
				 
				 //if(faultStr == null)
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workday_Account_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String finalStr = "";
					 String headerStr = "";
					 
					 String empId = "";
					 String userName = "";
					 String sessionTimeoutMinutes = "";
					 String accountDisabled = "";
					 String accountExpirationDate = "";
					 String reqNewPwdAtNextLogin = "";
					 String showUserNameInBrowserWindow = "";
					 String displayXMLIconOnReports = "";
					 String allowMixedLangTransaction = "";
					 String exmptFromDelegatedAuth = "";
					 String passcodeExempt = "";
					 String passcodeGracePeriodEnabled = "";
					 String openIDIdentifier = "";
					 String notification = "";
					 String notificationArr = "";
					 
					 int empIdCount = 0;
					 
					 Map<String,String> notificationMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {					 
						 if(j == 1)
						 {
							startIndex = 0;
							if(1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*1000;
							}
						 }
						 else
						 {
							int lastVal = (j - 1);
							startIndex = (endIndex - lastVal);
							if(j*1000 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = (j*startIndex) + 1;
							}
						 }
						 outputfile = addSystemUserAccountList(GET_SYSTEM_USER_ACCOUNT_REQUEST_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
						 is = new FileInputStream(outputfile);
					     soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     soapPart = soapMessage.getSOAPPart();
					     envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
				         out = new ByteArrayOutputStream();
				         soapMessage.writeTo(out);
				         strMsg = new String(out.toByteArray());
				        
				         soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
				         soapResponse = soapConnection.call(soapMessage, sourceUrl);
				         out = new ByteArrayOutputStream();
				         soapResponse.writeTo(out);
				         strMsg = new String(out.toByteArray(), "utf-8");
				        
				         soapResp = XmlParserManager.parseXml(strMsg);
					 
						 ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Workday_Account_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> workdayAccountData = responseData.getChildren("wd:Workday_Account_Response_Data");
							
						 for(ReportElement reportElement : workdayAccountData)
						 {
							 empId = columnList.get(empIdCount);
							 empIdCount++;
							 notificationArr = "";
							 ReportElement workdayAccountWorkdayData = reportElement.getChild("wd:Workday_Account_for_Worker_Data");
							 if(workdayAccountWorkdayData != null)
							 {								 
								 userName = workdayAccountWorkdayData.getChild("wd:User_Name") != null?workdayAccountWorkdayData.getChild("wd:User_Name").getValue().trim():"";	
								 sessionTimeoutMinutes = workdayAccountWorkdayData.getChild("wd:Session_Timeout_Minutes") != null?workdayAccountWorkdayData.getChild("wd:Session_Timeout_Minutes").getValue().trim():"";	
								 accountDisabled = workdayAccountWorkdayData.getChild("wd:Account_Disabled") != null?workdayAccountWorkdayData.getChild("wd:Account_Disabled").getValue().trim():"";
								 if(accountDisabled.equals("1"))
								 {
									 accountDisabled = "true";
								 }
								 else
								 {
									 accountDisabled = "false";
								 }
								 accountExpirationDate = workdayAccountWorkdayData.getChild("wd:Account_Expiration_Date") != null?workdayAccountWorkdayData.getChild("wd:Account_Expiration_Date").getValue().trim():"";	
								 reqNewPwdAtNextLogin = workdayAccountWorkdayData.getChild("wd:Require_New_Password_at_Next_Sign_In") != null?workdayAccountWorkdayData.getChild("wd:Require_New_Password_at_Next_Sign_In").getValue().trim():"";
								 if(reqNewPwdAtNextLogin.equals("1"))
								 {
									 reqNewPwdAtNextLogin = "true";
								 }
								 else
								 {
									 reqNewPwdAtNextLogin = "false";
								 }
								 showUserNameInBrowserWindow = workdayAccountWorkdayData.getChild("wd:Show_User_Name_in_Browser_Window") != null?workdayAccountWorkdayData.getChild("wd:Show_User_Name_in_Browser_Window").getValue().trim():"";
								 if(showUserNameInBrowserWindow.equals("1"))
								 {
									 showUserNameInBrowserWindow = "true";
								 }
								 else
								 {
									 showUserNameInBrowserWindow = "false";
								 }
								 displayXMLIconOnReports = workdayAccountWorkdayData.getChild("wd:Display_XML_Icon_on_Reports") != null?workdayAccountWorkdayData.getChild("wd:Display_XML_Icon_on_Reports").getValue().trim():"";
								 if(displayXMLIconOnReports.equals("1"))
								 {
									 displayXMLIconOnReports = "true";
								 }
								 else
								 {
									 displayXMLIconOnReports = "false";
								 }
								 allowMixedLangTransaction = workdayAccountWorkdayData.getChild("wd:Allow_Mixed-Language_Transactions") != null?workdayAccountWorkdayData.getChild("wd:Allow_Mixed-Language_Transactions").getValue().trim():"";
								 if(allowMixedLangTransaction.equals("1"))
								 {
									 allowMixedLangTransaction = "true";
								 }
								 else
								 {
									 allowMixedLangTransaction = "false";
								 }
								 exmptFromDelegatedAuth = workdayAccountWorkdayData.getChild("wd:Exempt_from_Delegated_Authentication") != null?workdayAccountWorkdayData.getChild("wd:Exempt_from_Delegated_Authentication").getValue().trim():"";	
								 if(exmptFromDelegatedAuth.equals("1"))
								 {
									 exmptFromDelegatedAuth = "true";
								 }
								 else
								 {
									 exmptFromDelegatedAuth = "false";
								 }
								 passcodeExempt = workdayAccountWorkdayData.getChild("wd:One-Time_Passcode_Exemp") != null?workdayAccountWorkdayData.getChild("wd:One-Time_Passcode_Exemp").getValue().trim():"";
								 if(passcodeExempt.equals("1"))
								 {
									 passcodeExempt = "true";
								 }
								 else
								 {
									 passcodeExempt = "false";
								 }
								 passcodeGracePeriodEnabled = workdayAccountWorkdayData.getChild("wd:One-Time_Passcode_Grace_Period_Enabled") != null?workdayAccountWorkdayData.getChild("wd:One-Time_Passcode_Grace_Period_Enabled").getValue().trim():"";	
								 if(passcodeExempt.equals("1"))
								 {
									 passcodeGracePeriodEnabled = "true";
								 }
								 else
								 {
									 passcodeGracePeriodEnabled = "false";
								 }
								 openIDIdentifier = workdayAccountWorkdayData.getChild("wd:OpenID_Identifier") != null?workdayAccountWorkdayData.getChild("wd:OpenID_Identifier").getValue().trim():"";
								 
								 ReportElement notificationConfigsRef = workdayAccountWorkdayData.getChild("wd:Notification_Sub_Type_Configurations");
								 if(notificationConfigsRef != null)
								 {
									 List<ReportElement> notificationConfigsLists = notificationConfigsRef.getChildren("wd:Notification_Sub_Type_Configuration");	
									 if(notificationConfigsLists != null && notificationConfigsLists.size() >0)
									 {
										 for(ReportElement notificationsElement:notificationConfigsLists)
										 {
											 List<ReportElement> notificationSubTypeLists = notificationsElement.getChildren("wd:Notification_Sub_Type_Reference");	
											 if(notificationSubTypeLists != null && notificationSubTypeLists.size() >0)
											 {
												 for(ReportElement notificationSubTypeElement:notificationSubTypeLists)
												 {
													 List<ReportElement> notificationData = notificationSubTypeElement.getChildren("wd:ID");								 
													 for(ReportElement notificationElement:notificationData)
													 {
														 notificationMap = notificationElement.getAllAttributes();
														 if(notificationMap.get("wd:type").equals("NOTIFICATION_CATEGORY"))
														 {
															 notification = notificationElement.getValue().trim();													 
														 }
														 if(notificationArr.equals(""))
														 {
															 notificationArr = notification;
														 }
														 else
														 {
															 notificationArr = notificationArr + "~" + notification;
														 }
													 }
												 }
											 }
										 }
									 }
								 }
							 }
								 
							 else
							 {
								 userName = "";
								 sessionTimeoutMinutes = "";
								 accountDisabled = "";
								 accountExpirationDate = "";
								 reqNewPwdAtNextLogin = "";
								 showUserNameInBrowserWindow = "";
								 displayXMLIconOnReports = "";
								 allowMixedLangTransaction = "";
								 exmptFromDelegatedAuth = "";
								 passcodeExempt = "";
								 passcodeGracePeriodEnabled = "";
								 openIDIdentifier = "";
								 notificationArr = ""; 
							 }
							 
							 headingFromWD = "Employee_ID,User_Name,Session_Timeout_Minutes,Account_Disabled,Account_Expiration_Date,Require_New_Password_at_Next_Sign_In,Show_User_Name_in_Browser_Window,"
							 		+ "Display_XML_Icon_on_Reports,Allow_Mixed-Language_Transactions,Exempt_from_Delegated_Authentication,One-Time_Passcode_Exempt,One-Time_Passcode_Grace_Period_Enabled,"
							 		+ "OpenID_Identifier,Notification";
							 headerStr = empId + "," + userName + "," + sessionTimeoutMinutes + "," + accountDisabled + "," + accountExpirationDate + "," + reqNewPwdAtNextLogin + "," + showUserNameInBrowserWindow + "," +
									 displayXMLIconOnReports + "," + allowMixedLangTransaction + "," + exmptFromDelegatedAuth + "," + passcodeExempt + "," + passcodeGracePeriodEnabled + "," +
									 openIDIdentifier + "," + notificationArr;
							 
							 if(finalStr.equals(""))
							 {
								 finalStr = headingFromWD + "\n" + headerStr;
							 }
							 else
							 {
								 finalStr = finalStr + "\n" + headerStr;
							 }
						 }
					 }
					 
					 System.out.println(finalStr);
					 
					 File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
					 PrintWriter writer = new PrintWriter(wdCSVfile);
					 writer.write(finalStr.toString());
					 writer.flush();
					 writer.close();
					 
					 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
					 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
					 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
					 postLoadService.updatePostLoad(postLoad);
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
				 }
			 }
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}
	
	private String addSystemUserAccountList(String userAccountFile, List<String> columnList, String ruleName, int startIndex, int endIndex, String referenceId) {
		
		File updatedRequestfile = null;
		try  
		{  
			File file = new File(userAccountFile);     
			FileReader fr = new FileReader(file);   
			BufferedReader br = new BufferedReader(fr);   
			StringBuffer sb = new StringBuffer();     
			String line; 
			boolean executed = true;
			while((line=br.readLine())!=null)  
			{  
				sb.append(line);  
				sb.append("\n");
				if(line.contains("bsvc:Get_Workday_Account_Request") && executed)
				{
					executed = false;
					sb.append(" <bsvc:Request_References>"); 
					sb.append("\n");
					for(int i = startIndex;i<endIndex;i++)
					{
						sb.append("  <bsvc:Workday_Account_Reference bsvc:Descriptor=" + "\"" + referenceId + "\"" + ">");
						sb.append("\n");
						sb.append("   <bsvc:ID bsvc:type=" + "\"" + referenceId + "\"" + ">" + columnList.get(i) + "</bsvc:ID>");
						sb.append("\n");
						sb.append("  </bsvc:Workday_Account_Reference>");
						sb.append("\n");
					}
					sb.append(" </bsvc:Request_References>");
					sb.append("\n");
				}
			}  
			fr.close();
			br.close();
			
			updatedRequestfile = File.createTempFile(ruleName + "_" + startIndex , ".xml");
			PrintWriter writer = new PrintWriter(updatedRequestfile);
		    writer.write(sb.toString());
			writer.flush();
			writer.close();
		}  
		catch(IOException e)  
		{  
			e.printStackTrace();  
		}
		return updatedRequestfile.getAbsolutePath(); 
	}
	
	private String addUserAccountToFindError(String xmlFile, String columnVal, String ruleName, String idVal) {
		
		File updatedRequestfile = null;
		try  
		{  
			File file = new File(xmlFile);     
			FileReader fr = new FileReader(file);   
			BufferedReader br = new BufferedReader(fr);   
			StringBuffer sb = new StringBuffer();     
			String line; 
			boolean executed = true;
			while((line=br.readLine())!=null)  
			{  
				sb.append(line);  
				sb.append("\n");
				if(line.contains("bsvc:Get_Workday_Account_Request") && executed)
				{
					executed = false;
					sb.append(" <bsvc:Request_References>"); 
					sb.append("\n");
						sb.append("  <bsvc:Workday_Account_Reference bsvc:Descriptor=" + "\"" + idVal + "\"" + ">");
						sb.append("\n");
						sb.append("   <bsvc:ID bsvc:type=" + "\"" + idVal + "\"" + ">" + columnVal + "</bsvc:ID>");
						sb.append("\n");
						sb.append("  </bsvc:Workday_Account_Reference>");
						sb.append("\n");					
					sb.append(" </bsvc:Request_References>");
					sb.append("\n");
				}
			}  
			fr.close();
			br.close();
			
			updatedRequestfile = File.createTempFile(ruleName + "_" + columnVal , ".xml");
			PrintWriter writer = new PrintWriter(updatedRequestfile);
		    writer.write(sb.toString());
			writer.flush();
			writer.close();
		}  
		catch(IOException e)  
		{  
			e.printStackTrace();  
		}
		return updatedRequestfile.getAbsolutePath(); 
	}
	
	private JSONArray createCSVFromWDMatrixOrganization(Tenant tenant, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		targetContent = null;
		headingFromWD = "";
		try 
		{			 
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_MATRIX_ORGANIZATION_REQUEST_FILE = requestfile.getAbsolutePath();
				 
				 //String outputfile = GET_GOVERNMENT_ID_FILE;
				 //columnList.removeAll(errorList);
				 /*if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addWorkerIdListToFindError(GET_MATRIX_ORGANIZATION_REQUEST_FILE, columnList.get(i), ruleName, "Employee_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement reportElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = reportElement.getChild("env:Body")
										.getChild("wd:Get_Workers_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
						 }
					 }
					 columnList.removeAll(errorList);
				 }*/
				 String outputfile = addHireIdList(GET_MATRIX_ORGANIZATION_REQUEST_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");
				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");
				 
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String positionId = "";
					 String effectiveDate = "";
					 String matrixOrgRefId = "";
					 String matrixOrgRefIdArr = "";
					 String matrixOrgName = "";
					 String matrixOrgNameArr = "";
					 String matrixOrgType = "";
					 String finalStr = "";
					 String headerStr = "";
					 
					 Map<String,String> positionDataMap = null;
					 Map<String,String> positionMap = null;
					 Map<String,String> orgTypeMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {
						if(j == 1)
						{
							startIndex = 0;
							if(999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*999;
							}
						}
						else
						{
							startIndex = endIndex;
							if(j*999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*999;
							}
						}
						outputfile = addHireIdList(GET_MATRIX_ORGANIZATION_REQUEST_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
						is = new FileInputStream(outputfile);
					    soapMessage = MessageFactory.newInstance().createMessage(null, is);
					    soapPart = soapMessage.getSOAPPart();
					    envelope = soapPart.getEnvelope();
						envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						{
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						}
						soapMessage.saveChanges();
				        out = new ByteArrayOutputStream();
				        soapMessage.writeTo(out);
				        strMsg = new String(out.toByteArray());
				        
				        soapConnectionFactory = SOAPConnectionFactory.newInstance();
						soapConnection = soapConnectionFactory.createConnection();
				        soapResponse = soapConnection.call(soapMessage, sourceUrl);
				        out = new ByteArrayOutputStream();
				        soapResponse.writeTo(out);
				        strMsg = new String(out.toByteArray(), "utf-8");
				        soapResp = XmlParserManager.parseXml(strMsg);
					        
						ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Workers_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> applicantData = responseData.getChildren("wd:Worker");
							
						 for(ReportElement reportElement : applicantData)
						 {
							 ReportElement element1 = reportElement.getChild("wd:Worker_Data");
							 String workerId = element1.getChild("wd:Worker_ID").getValue().trim();
							 System.out.println("workerId--"+ workerId);
							 
							 ReportElement workerJobData = reportElement.getChild("wd:Worker_Data")
							 			.getChild("wd:Employment_Data")
							 				.getChild("wd:Worker_Job_Data");
							 
							 if(workerJobData != null)
							 {
								 ReportElement positionData = workerJobData.getChild("wd:Position_Data");
								 if(positionData != null)
								 {
									 positionDataMap = positionData.getAllAttributes();
									 effectiveDate = positionDataMap.get("wd:Effective_Date");
									 
									 ReportElement positionRef = positionData.getChild("wd:Position_Reference");
									 if(positionRef != null)
									 {
										 List<ReportElement> positionDataList = positionRef.getChildren("wd:ID");								 
										 for(ReportElement positionElement:positionDataList)
										 {
											 positionMap = positionElement.getAllAttributes();
											 if(positionMap.get("wd:type").equals("Position_ID"))
											 {
												 positionId = positionElement.getValue().trim();												 
											 }
										 }
									 }									 
								 }
								 else
								 {
									 effectiveDate = "" ;
									 positionId = "";
								 }
								 ReportElement positionOrganizationData = workerJobData.getChild("wd:Position_Organizations_Data");
								 if(positionOrganizationData != null)
								 {
									 List<ReportElement> positionOrganizationList = positionOrganizationData.getChildren("wd:Position_Organization_Data");
									 if(positionOrganizationList != null && positionOrganizationList.size() >0)
									 {
										 matrixOrgRefIdArr = "";
										 matrixOrgNameArr = "";
										 for(ReportElement positionElement:positionOrganizationList)
										 {
											 ReportElement organizationData = positionElement.getChild("wd:Organization_Data");
											 if(organizationData != null)
											 {
												 ReportElement orgTypeRef = organizationData.getChild("wd:Organization_Type_Reference");
												 if(orgTypeRef != null)
												 {
													 List<ReportElement> orgTypeList = orgTypeRef.getChildren("wd:ID");								 
													 for(ReportElement orgTypeElement:orgTypeList)
													 {
														 orgTypeMap = orgTypeElement.getAllAttributes();
														 if(orgTypeMap.get("wd:type").equals("Organization_Type_ID"))
														 {
															 matrixOrgType = orgTypeElement.getValue().trim();												 
														 }
													 }
												 }
												 if(matrixOrgType.equalsIgnoreCase("MATRIX"))
												 {
													 matrixOrgRefId = organizationData.getChild("wd:Organization_Reference_ID") != null?organizationData.getChild("wd:Organization_Reference_ID").getValue().trim():"";
													 if(matrixOrgRefIdArr.equals(""))
													 {
														 matrixOrgRefIdArr = matrixOrgRefId;
													 }
													 else
													 {
														 matrixOrgRefIdArr = matrixOrgRefIdArr + "~" + matrixOrgRefId;
													 }
													 matrixOrgName = organizationData.getChild("wd:Organization_Name") != null?organizationData.getChild("wd:Organization_Name").getValue().trim():"";
													 if(matrixOrgNameArr.equals(""))
													 {
														 matrixOrgNameArr = matrixOrgName;
													 }
													 else
													 {
														 matrixOrgNameArr = matrixOrgNameArr + "~" + matrixOrgName;
													 }
												 }
											 }
										 }
									 }
									 else
									 {
										 matrixOrgRefIdArr = "";
										 matrixOrgNameArr = "";
									 }
								 }
							 }
								 
							 headingFromWD = "Employee_ID,Position_ID,Effective_Date,Organization_Reference_Id,Organization_Name";
							 
							 headerStr = workerId + "," + positionId + "," + effectiveDate + "," + matrixOrgRefIdArr + "," + matrixOrgNameArr;
							 
							 if(finalStr.equals(""))
							 {
								 finalStr = headingFromWD + "\n" + headerStr;
							 }
							 else
							 {
								 finalStr = finalStr + "\n" + headerStr;
							 }
						 }						 
					 }				 
					 				     
				     System.out.println(finalStr);
				     targetContent = finalStr.toString().getBytes();
				     
					 /*File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
					 PrintWriter writer = new PrintWriter(wdCSVfile);
					 writer.write(finalStr.toString());
					 writer.flush();
					 writer.close();
					 
					 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
					 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
					 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
					 postLoadService.updatePostLoad(postLoad);*/
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
					 complete = true;
				 }
			 }
			 
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return headingWd;
	}
	
	private JSONArray createCSVFromWDEditWorkerAddnData(Tenant tenant, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		targetContent = null;
		headingFromWD = "";
		try 
		{			 
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_EDIT_WORKER_ADDN_DATA_REQUEST_FILE = requestfile.getAbsolutePath();
				 
				 //String outputfile = GET_GOVERNMENT_ID_FILE;
				 //columnList.removeAll(errorList);
				 /*if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addWorkerIdListToFindError(GET_EDIT_WORKER_ADDN_DATA_REQUEST_FILE, columnList.get(i), ruleName, "Employee_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement reportElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = reportElement.getChild("env:Body")
										.getChild("wd:Get_Workers_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
						 }
					 }
					 columnList.removeAll(errorList);
				 }*/
				 String outputfile = addHireIdList(GET_EDIT_WORKER_ADDN_DATA_REQUEST_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");
				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");
				 
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String effectiveDate = "";
					 String benefitEndDate = "";

					 String finalStr = "";
					 String headerStr = "";
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {
						 if(j == 1)
						 {
							startIndex = 0;
							if(999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*999;
							}
						 }
						 else
						 {
							startIndex = endIndex;
							if(j*999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = (j*999);
							}
						 }
						outputfile = addHireIdList(GET_EDIT_WORKER_ADDN_DATA_REQUEST_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
						is = new FileInputStream(outputfile);
					    soapMessage = MessageFactory.newInstance().createMessage(null, is);
					    soapPart = soapMessage.getSOAPPart();
					    envelope = soapPart.getEnvelope();
						envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						{
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						}
						soapMessage.saveChanges();
				        out = new ByteArrayOutputStream();
				        soapMessage.writeTo(out);
				        strMsg = new String(out.toByteArray());
				        
				        soapConnectionFactory = SOAPConnectionFactory.newInstance();
						soapConnection = soapConnectionFactory.createConnection();
				        soapResponse = soapConnection.call(soapMessage, sourceUrl);
				        out = new ByteArrayOutputStream();
				        soapResponse.writeTo(out);
				        strMsg = new String(out.toByteArray(), "utf-8");
				        soapResp = XmlParserManager.parseXml(strMsg);
					        
						ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Workers_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> applicantData = responseData.getChildren("wd:Worker");
							
						 for(ReportElement reportElement : applicantData)
						 {
							 ReportElement element1 = reportElement.getChild("wd:Worker_Data");
							 String workerId = element1.getChild("wd:Worker_ID").getValue().trim();
							 System.out.println("workerId--"+ workerId);
							 
							 ReportElement terminationStatusData = reportElement.getChild("wd:Worker_Data")
							 			.getChild("wd:Employment_Data")
							 			.getChild("wd:Worker_Status_Data");
					        	
					        	effectiveDate = terminationStatusData.getChild("wd:Termination_Date") != null?terminationStatusData.getChild("wd:Termination_Date").getValue().trim():"";
					        	if(!effectiveDate.isEmpty())
								{
					        		effectiveDate = effectiveDate.substring(0, 10);
								}
					        	benefitEndDate = terminationStatusData.getChild("wd:Termination_Date") != null?terminationStatusData.getChild("wd:Termination_Date").getValue().trim():"";	
					        	if(!benefitEndDate.isEmpty())
								{
					        		benefitEndDate = benefitEndDate.substring(0, 10);
								}
							 
							 headingFromWD = "Employee_ID,Effective_Date,Benefit_End_Date";
							 
							 headerStr = workerId + "," + effectiveDate + "," + benefitEndDate;
							 
							 if(finalStr.equals(""))
							 {
								 finalStr = headingFromWD + "\n" + headerStr;
							 }
							 else
							 {
								 finalStr = finalStr + "\n" + headerStr;
							 }
						 }						 
					 }				 
					 				     
				     System.out.println(finalStr);
				     targetContent = finalStr.toString().getBytes();
				     
					 /*File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
					 PrintWriter writer = new PrintWriter(wdCSVfile);
					 writer.write(finalStr.toString());
					 writer.flush();
					 writer.close();
					 
					 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
					 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
					 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
					 postLoadService.updatePostLoad(postLoad);*/
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
					 complete = true;
				 }
			 }
			 
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return headingWd;
	}
	
	private JSONArray createCSVFromWDEmployeeContract(Tenant tenant2, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

				headingFromWD = "";
				targetContent = null;
				try 
				{			 
					 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
					 if(getRequest != null)
					 {
						 byte[] requestFileContent = getRequest.getRequestXMLContent();
						 File requestfile = null;
						 try 
						 {
							 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
							 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
						 } 
						 catch (IOException e1) 
						 {
						     e1.printStackTrace();
						 }
						 GET_EMPLOYEE_CONTRACT_REQUEST_FILE = requestfile.getAbsolutePath();
						 
						 //String outputfile = GET_GOVERNMENT_ID_FILE;
						 //columnList.removeAll(errorList);
						 /*if(errorList.isEmpty())
						 {
							 for(int i = 0; i<columnList.size();i++)
							 {
								 checkFile = addWorkerIdListToFindError(GET_EMPLOYEE_CONTRACT_REQUEST_FILE, columnList.get(i), ruleName, "Employee_ID");
								 is = new FileInputStream(checkFile);
							     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
							     SOAPPart soapPart = soapMessage.getSOAPPart();
							     SOAPEnvelope envelope = soapPart.getEnvelope();
								 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
								 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
								 {
									  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
									  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
								 }
								 soapMessage.saveChanges();
							     ByteArrayOutputStream out = new ByteArrayOutputStream();
							     soapMessage.writeTo(out);
							     String strMsgChk = new String(out.toByteArray());	
							     
							     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
							     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
								 soapConnection = soapConnectionFactory.createConnection();
								 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
								 out = new ByteArrayOutputStream();
								 soapResponse.writeTo(out);
								 strMsgChk = new String(out.toByteArray(), "utf-8");
								 if(strMsgChk.contains("faultstring"))
								 {
									 errorList.add(columnList.get(i)) ;
								 }
								 else
								 {
									 ReportElement reportElement = XmlParserManager.parseXml(strMsgChk);				 
									 ReportElement pageResults = reportElement.getChild("env:Body")
												.getChild("wd:Get_Workers_Response")
												.getChild("wd:Response_Results");
									 
									 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
									 if(result.equalsIgnoreCase("0"))
									 {
										 errorList.add(columnList.get(i));
									 }
								 }
							 }
							 columnList.removeAll(errorList);
						 }*/
						 String outputfile = addHireIdList(GET_EMPLOYEE_CONTRACT_REQUEST_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");
						 is = new FileInputStream(outputfile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsg = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsg = new String(out.toByteArray(), "utf-8");
						 
						 {
							 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
							 ReportElement pageData = soapResp.getChild("env:Body")
										.getChild("wd:Get_Workers_Response")
										.getChild("wd:Response_Results");
							 
							 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
							 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
							 int totalResult = Integer.parseInt(totalResults);
							 System.out.println("totalNoOfPages-"+totalNoOfPages);
							 System.out.println("totalResult-"+totalResult);
							 wdCount = totalResult;
							 
							 String empContractId = "";
							 String contractVersionDate = "";
							 String contractId = "";
							 String contractType = "";
							 String contractStartDate = "";
							 String contractEndDate = "";
							 String contractStatus = "";
							 String contractPosition = "";
							 String contractDesc = "";
							 String dateEmployeeSigned = "";
							 String dateEmployerSigned = "";
							 String empContractIdArr = "";
							 String contractVersionDateArr = "";
							 String contractIdArr = "";
							 String contractTypeArr = "";
							 String contractStartDateArr = "";
							 String contractEndDateArr = "";
							 String contractStatusArr = "";
							 String contractPositionArr = "";
							 String contractDescArr = "";
							 String dateEmployeeSignedArr = "";
							 String dateEmployerSignedArr = "";
							 String finalStr = "";
							 String headerStr = "";
							 						 
							 Map<String,String> contractMap = null;
							 Map<String,String> contractTypeMap = null;
							 Map<String,String> statusMap = null;
							 Map<String,String> positionMap = null;
							 
							 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
							 {
								 if(j == 1)
								 {
									startIndex = 0;
									if(999 > totalResult)
									{
										endIndex = totalResult;
									}
									else
									{
										endIndex = j*999;
									}
								 }
								 else
								 {
									startIndex = endIndex;
									if(j*999 > totalResult)
									{
										endIndex = totalResult;
									}
									else
									{
										endIndex = (j*999);
									}
								 }
								outputfile = addHireIdList(GET_EMPLOYEE_CONTRACT_REQUEST_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
								is = new FileInputStream(outputfile);
							    soapMessage = MessageFactory.newInstance().createMessage(null, is);
							    soapPart = soapMessage.getSOAPPart();
							    envelope = soapPart.getEnvelope();
								envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
								if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
								{
										envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
										createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
								}
								soapMessage.saveChanges();
						        out = new ByteArrayOutputStream();
						        soapMessage.writeTo(out);
						        strMsg = new String(out.toByteArray());
						        
						        soapConnectionFactory = SOAPConnectionFactory.newInstance();
								soapConnection = soapConnectionFactory.createConnection();
						        soapResponse = soapConnection.call(soapMessage, sourceUrl);
						        out = new ByteArrayOutputStream();
						        soapResponse.writeTo(out);
						        strMsg = new String(out.toByteArray(), "utf-8");
						        soapResp = XmlParserManager.parseXml(strMsg);
							        
								ReportElement responseData = soapResp.getChild("env:Body")
											.getChild("wd:Get_Workers_Response")
											.getChild("wd:Response_Data");
								 
								 List<ReportElement> applicantData = responseData.getChildren("wd:Worker");
									
								 for(ReportElement reportElement : applicantData)
								 {
									 ReportElement element1 = reportElement.getChild("wd:Worker_Data");
									 String workerId = element1.getChild("wd:Worker_ID").getValue().trim();
									 System.out.println("workerId--"+ workerId);
									 
									 ReportElement contractData = reportElement.getChild("wd:Worker_Data")
									 			.getChild("wd:Employee_Contracts_Data");
									 
									 if(contractData != null)
									 {
										 List<ReportElement> empContractList = contractData.getChildren("wd:Employee_Contract_Data");
										 if(empContractList != null && empContractList.size() >0)
										 {
											 empContractIdArr = "";
											 contractVersionDateArr = "";
											 contractIdArr = "";
											 contractTypeArr = "";
											 contractStartDateArr = "";
											 contractEndDateArr = "";
											 contractStatusArr = "";
											 contractPositionArr = "";
											 contractDescArr = "";
											 dateEmployeeSignedArr = "";
											 dateEmployerSignedArr = "";
											 
											 for(ReportElement empContractElement : empContractList)
											 {
												 ReportElement empContractRef = empContractElement.getChild("wd:Employee_Contract_Reference");
												 if(empContractRef != null)
												 {
													 List<ReportElement> empContractData = empContractRef.getChildren("wd:ID");								 
													 for(ReportElement contractElement:empContractData)
													 {
														 contractMap = contractElement.getAllAttributes();
														 if(contractMap.get("wd:type").equals("Employee_Contract_ID"))
														 {
															empContractId = contractElement.getValue().trim();														 
														 }
													 }
												 }
												 else
												 {
													 empContractId = empContractElement.getChild("wd:Employee_Contract_ID") != null?empContractElement.getChild("wd:Employee_Contract_ID").getValue().trim():""; ;
												 }
												 if(empContractIdArr.equals(""))
												 {
													 empContractIdArr = empContractId;
												 }
												 else
												 {
													 empContractIdArr = empContractIdArr + "~" + empContractId;
												 }
												 contractVersionDate = empContractElement.getChild("wd:Effective_Date") != null?empContractElement.getChild("wd:Effective_Date").getValue().trim():""; 
												 if(!contractVersionDate.isEmpty())
												 {
													 contractVersionDate = contractVersionDate.substring(0, 10);
													 contractVersionDate = convertDate(contractVersionDate, "yyyy-MM-dd", "dd-MM-yyyy");
												 }
												 if(contractVersionDateArr.equals(""))
												 {
													 contractVersionDateArr = contractVersionDate;
												 }
												 else
												 {
													 contractVersionDateArr = contractVersionDateArr + "~" + contractVersionDate;
												 }
												 contractId = empContractElement.getChild("wd:Contract_ID") != null?empContractElement.getChild("wd:Contract_ID").getValue().trim():"";
												 if(contractIdArr.equals(""))
												 {
													 contractIdArr = contractId;
												 }
												 else
												 {
													 contractIdArr = contractIdArr + "~" + contractId;
												 }
												 ReportElement contractTypeRef = empContractElement.getChild("wd:Contract_Type_Reference");
												 if(contractTypeRef != null)
												 {
													 List<ReportElement> contractTypeData = contractTypeRef.getChildren("wd:ID");								 
													 for(ReportElement contractTypeElement:contractTypeData)
													 {
														 contractTypeMap = contractTypeElement.getAllAttributes();
														 if(contractTypeMap.get("wd:type").equals("Employee_Contract_Type_ID"))
														 {
															contractType = contractTypeElement.getValue().trim();	
															if(contractType.contains(","))
															{
																 contractType = contractType.replaceAll(",", "|");
															}
															 if(contractTypeArr.equals(""))
															 {
																 contractTypeArr = contractType;
															 }
															 else
															 {
																 contractTypeArr = contractTypeArr + "~" + contractType;
															 }
															
														 }
													 }
												 }

												 contractStartDate = empContractElement.getChild("wd:Contract_Start_Date") != null?empContractElement.getChild("wd:Contract_Start_Date").getValue().trim():"";
												 if(!contractStartDate.isEmpty())
												 {
													 contractStartDate = contractStartDate.substring(0, 10);
													 //contractStartDate = convertDate(contractStartDate, "yyyy-MM-dd", "dd-MM-yyyy");
												 }
												 if(contractStartDateArr.equals(""))
												 {
													 contractStartDateArr = contractStartDate;
												 }
												 else
												 {
													 contractStartDateArr = contractStartDateArr + "~" + contractStartDate;
												 }
												 contractEndDate = empContractElement.getChild("wd:Contract_End_Date") != null?empContractElement.getChild("wd:Contract_End_Date").getValue().trim():"";
												 if(!contractEndDate.isEmpty())
												 {
													 contractEndDate = contractEndDate.substring(0, 10);
													 //contractEndDate = convertDate(contractEndDate, "yyyy-MM-dd", "dd-MM-yyyy");
												 }
												 if(contractEndDateArr.equals(""))
												 {
													 contractEndDateArr = contractEndDate;
												 }
												 else
												 {
													 contractEndDateArr = contractEndDateArr + "~" + contractEndDate;
												 }
												 ReportElement contractStatusRef = empContractElement.getChild("wd:Contract_Status_Reference");
												 if(contractStatusRef != null)
												 {
													 List<ReportElement> contractStatusData = contractStatusRef.getChildren("wd:ID");								 
													 for(ReportElement contractStatusElement:contractStatusData)
													 {
														 statusMap = contractStatusElement.getAllAttributes();
														 if(statusMap.get("wd:type").equals("Employee_Contract_Status_ID"))
														 {
															contractStatus = contractStatusElement.getValue().trim();														 
														 }
													 }
												 }
												 else
												 {
													 contractStatus = "";
												 }
												 if(contractStatusArr.equals(""))
												 {
													 contractStatusArr = contractStatus;
												 }
												 else
												 {
													 contractStatusArr = contractStatusArr + "~" + contractStatus;
												 }
												 ReportElement contractPositionRef = empContractElement.getChild("wd:Position_Reference");
												 if(contractPositionRef != null)
												 {
													 List<ReportElement> contractPositionData = contractPositionRef.getChildren("wd:ID");								 
													 for(ReportElement contractPositionElement:contractPositionData)
													 {
														 positionMap = contractPositionElement.getAllAttributes();
														 if(positionMap.get("wd:type").equals("Position_ID"))
														 {
															contractPosition = contractPositionElement.getValue().trim();														 
														 }
													 }
												 }
												 else
												 {
													 contractPosition = "";
												 }
												 if(contractPositionArr.equals(""))
												 {
													 contractPositionArr = contractPosition;
												 }
												 else
												 {
													 contractPositionArr = contractPositionArr + "~" + contractPosition;
												 }
												 contractDesc = empContractElement.getChild("wd:Contract_Description") != null?empContractElement.getChild("wd:Contract_Description").getValue().trim():"";
												 if(contractDescArr.equals(""))
												 {
													 contractDescArr = contractDesc;
												 }
												 else
												 {
													 contractDescArr = contractDescArr + "~" + contractDesc;
												 }
												 dateEmployeeSigned = empContractElement.getChild("wd:Date_Employee_Signed") != null?empContractElement.getChild("wd:Date_Employee_Signed").getValue().trim():"";
												 if(dateEmployeeSignedArr.equals(""))
												 {
													 dateEmployeeSignedArr = dateEmployeeSigned;
												 }
												 else
												 {
													 dateEmployeeSignedArr = dateEmployeeSignedArr + "~" + dateEmployeeSigned;
												 }
												 dateEmployerSigned = empContractElement.getChild("wd:Date_Employer_Signed") != null?empContractElement.getChild("wd:Date_Employer_Signed").getValue().trim():"";
												 if(dateEmployerSignedArr.equals(""))
												 {
													 dateEmployerSignedArr = dateEmployerSigned;
												 }
												 else
												 {
													 dateEmployerSignedArr = dateEmployerSignedArr + "~" + dateEmployerSigned;
												 }
											 }										  
										 }
										 else
										 {
											 empContractIdArr = "";
											 contractVersionDateArr = "";
											 contractIdArr = "";
											 contractTypeArr = "";
											 contractStartDateArr = "";
											 contractEndDateArr = "";
											 contractStatusArr = "";
											 contractPositionArr = "";
											 contractDescArr = "";
											 dateEmployeeSignedArr = "";
											 dateEmployerSignedArr = ""; 
										 }
									 }
									 else
									 {
										 empContractIdArr = "";
										 contractVersionDateArr = "";
										 contractIdArr = "";
										 contractTypeArr = "";
										 contractStartDateArr = "";
										 contractEndDateArr = "";
										 contractStatusArr = "";
										 contractPositionArr = "";
										 contractDescArr = "";
										 dateEmployeeSignedArr = "";
										 dateEmployerSignedArr = "";
									 }
									 						        	
									 headingFromWD = "Employee_ID,Employee_Contract_ID,Effective_Date,Contract_ID,Contract_Type,Contract_Start_Date,Contract_End_Date,"
					        			+ "Contract_Status,Contract_Position,Contract_Description,Date_Employee_Signed,Date_Employer_Signed";

									 
						        	headerStr = workerId + "," +  empContractIdArr + "," + contractVersionDateArr + "," + contractIdArr + "," + contractTypeArr +
									 		 "," + contractStartDateArr + "," + contractEndDateArr + "," + contractStatusArr + "," + contractPositionArr + "," + contractDescArr +
									 		 "," + dateEmployeeSignedArr + "," + dateEmployerSignedArr;
									 
									 if(finalStr.equals(""))
									 {
										 finalStr = headingFromWD + "\n" + headerStr;
									 }
									 else
									 {
										 finalStr = finalStr + "\n" + headerStr;
									 }
								 }						 
							 }				 
							 				     
						     System.out.println(finalStr);
						     targetContent = finalStr.toString().getBytes();
							
						     /*File wdCSVfile = File.createTempFile(loadCycle + "_" + ruleName, ".csv");
							 PrintWriter writer = new PrintWriter(wdCSVfile);
							 writer.write(finalStr.toString());
							 writer.flush();
							 writer.close();
							 
							 PostLoad postLoad = postLoadService.getPostLoadByLoadRule(loadCycle, ruleName);
							 postLoad.setWdCSVFileName(loadCycle + "_" + ruleName + ".csv");
							 postLoad.setWdCSVFileContent(Files.readAllBytes(wdCSVfile.toPath()));
							 postLoadService.updatePostLoad(postLoad);*/
							 
							 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
							 complete = true;
						 }
					 }
					 
				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				} 
				catch (SOAPException e) 
				{
					e.printStackTrace();
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}

				return headingWd;
	}
	
	private JSONArray createCSVFromWDEndIntlAssignment(Tenant tenant2, InputStream is, SOAPConnection soapConnection,
			int startIndex, int endIndex, JSONArray headingWd, String loadCycle, String ruleName, String client) {

		headingFromWD = "";
		targetContent = null;
		try 
		{			 
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_END_INTERNATIONAL_ASSIGNMENT_REQUEST_FILE = requestfile.getAbsolutePath();
				 
				 String outputfile = addHireIdList(GET_END_INTERNATIONAL_ASSIGNMENT_REQUEST_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");
				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");
				 
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String WID = "";
					 String positionId = "";
					 String payThroughDate = "";
					 String endAssignmentDate = "";
					 String lastDayOfWork = "";
					 String endAssignmentReason = "";
					 String finalStr = "";
					 String headerStr = "";
					 String reportURL = "";
					 
					 Map<String,String> widMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {
						 if(j == 1)
						 {
							startIndex = 0;
							if(999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*999;
							}
						 }
						 else
						 {
							startIndex = endIndex;
							if(j*999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = (j*999);
							}
						 }
						outputfile = addHireIdList(GET_END_INTERNATIONAL_ASSIGNMENT_REQUEST_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
						is = new FileInputStream(outputfile);
					    soapMessage = MessageFactory.newInstance().createMessage(null, is);
					    soapPart = soapMessage.getSOAPPart();
					    envelope = soapPart.getEnvelope();
						envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						{
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						}
						soapMessage.saveChanges();
				        out = new ByteArrayOutputStream();
				        soapMessage.writeTo(out);
				        strMsg = new String(out.toByteArray());
				        
				        soapConnectionFactory = SOAPConnectionFactory.newInstance();
						soapConnection = soapConnectionFactory.createConnection();
				        soapResponse = soapConnection.call(soapMessage, sourceUrl);
				        out = new ByteArrayOutputStream();
				        soapResponse.writeTo(out);
				        strMsg = new String(out.toByteArray(), "utf-8");
				        soapResp = XmlParserManager.parseXml(strMsg);
					        
						ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Workers_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> applicantData = responseData.getChildren("wd:Worker");
							
						 for(ReportElement reportElement : applicantData)
						 {
							 ReportElement element1 = reportElement.getChild("wd:Worker_Data");
							 String workerId = element1.getChild("wd:Worker_ID").getValue().trim();
							 System.out.println("workerId--"+ workerId);
							 
							 ReportElement widRef = reportElement.getChild("wd:Worker_Reference");
							 if(widRef != null)
							 {
								 List<ReportElement> idList = widRef.getChildren("wd:ID");
								 for(ReportElement idElement:idList)
								 {
									 widMap = idElement.getAllAttributes();
									 if(widMap.get("wd:type").equals("WID"))
									 {
										 WID = idElement.getValue().trim();
									 }
								 }
							 }
							 else
							 {
								 WID = "";
							 }
							 
							 /*if(tenant.getTenantName().equalsIgnoreCase("ibm6"))
							 {
								 reportURL = "https://" + tenant.getTenantUrl() + "customreport2/" + tenant.getTenantName() + "/ISU_Verify_Global/" + "International_Assignment?Employee!WID=" + WID;
							 }
							 else if(tenant.getTenantName().equalsIgnoreCase("ibm10"))
							 {*/
								 reportURL = "https://" + tenant.getTenantUrl() + "customreport2/" + tenant.getTenantName() + "/ISU_Verify_Global/" + "International_Assignment?Employee!WID=" + WID;
							 //}
						     JSONArray jArr = createInternationalAssignmentData(reportURL);
						     if(jArr != null && jArr.length() >0)
							 {
						    	 for(int i = 0; i<jArr.length(); i++) 
							     {
									JSONObject objects = jArr.getJSONObject(i);
									positionId = objects.isNull("positionId")?"":objects.getString("positionId");
									payThroughDate = objects.isNull("payThroughDate")?"":objects.getString("payThroughDate");
									if(!payThroughDate.isEmpty())
									{
										payThroughDate = payThroughDate.substring(0, 10);
										payThroughDate = convertDate(payThroughDate, "yyyy-MM-dd", "dd-MM-yyyy");
									}
									endAssignmentDate = objects.isNull("endAssignmentDate")?"":objects.getString("endAssignmentDate");
									if(!endAssignmentDate.isEmpty())
									{
										endAssignmentDate = endAssignmentDate.substring(0, 10);
										endAssignmentDate = convertDate(endAssignmentDate, "yyyy-MM-dd", "dd-MM-yyyy");
									}
									lastDayOfWork = endAssignmentDate;
									endAssignmentReason = objects.isNull("endAssignmentReason")?"":objects.getString("endAssignmentReason");
							     }
							 }
						     else
						     {
									positionId = "";
									payThroughDate = "";
									endAssignmentDate = "";
									lastDayOfWork = "";
									endAssignmentReason = "";
						     }
							 headingFromWD = "Employee_ID,Position_ID,Pay_Through_Date,End_International_Assignment_Date,Last_Day_Of_Work,End_International_Assignment_Reason";
							 
							 headerStr = workerId + "," + positionId + "," + payThroughDate + "," + endAssignmentDate + "," + lastDayOfWork + "," + endAssignmentReason;
							 
							 if(finalStr.equals(""))
							 {
								 finalStr = headingFromWD + "\n" + headerStr;
							 }
							 else
							 {
								 finalStr = finalStr + "\n" + headerStr;
							 }
						 }						 
					 }				 
					 				     
				     System.out.println(finalStr);
				     targetContent = finalStr.toString().getBytes();
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
					 complete = true;
				 }
			 }
			 
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return headingWd;
	}

	private JSONArray createInternationalAssignmentData(String reportURL) throws JSONException {
		
		JSONArray jArr = new JSONArray();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		XPath xPath = XPathFactory.newInstance().newXPath();
		HttpBasicAuthentication httpBasicAuthentication = new HttpBasicAuthentication();
		String output = httpBasicAuthentication.getWithBasicAuthentication(reportURL, tenant.getTenantUser(), tenant.getTenantUserPassword());
		Reader reader1 = new StringReader(output);
		Document doc;
		try 
		{
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(new InputSource(reader1));
			doc.getDocumentElement().normalize();
			String expression = EMPLOYEE_APPLICANT_MAPPING_RESPONSE_EXPRESSION;
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) 
			{
				try 
				{
					jArr = parseNodesForInternationalAssignment(nodeList);
				} 
				catch (DOMException e)
				{
					e.printStackTrace();
				}
			}			
		} 
		catch (SAXException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (XPathExpressionException e) 
		{
			e.printStackTrace();
		} 
		catch (ParserConfigurationException e) 
		{
			e.printStackTrace();
		}
		return jArr;
	}

	private JSONArray parseNodesForInternationalAssignment(NodeList nodeList) throws DOMException, JSONException {
		
		JSONArray details = new JSONArray();
		for (int i = 0; i < nodeList.getLength(); i++) 
		{
		    Node nNode = nodeList.item(i);
		    if (nNode.getNodeName().equals("wd:Report_Data")) 
		    {
				if (nNode.getNodeType() == Node.ELEMENT_NODE) 
				{
					Element eElement = (Element) nNode;
					if (eElement.hasChildNodes()) 
					{
						NodeList childNodes = eElement.getChildNodes();
						for (int j = 0; j < childNodes.getLength(); j++) 
						{
							Node aChildNode = childNodes.item(j);
							JSONObject obj = new JSONObject();
							if (aChildNode.getNodeName().equals("wd:Report_Entry")) 
							{
								Element eElementEntry = (Element) aChildNode;
								if (eElement.hasChildNodes()) 
								{
									NodeList childNodesEntry = eElementEntry.getChildNodes();
									for (int k = 0; k < childNodesEntry.getLength(); k++) 
									{										
										Node aChildNodeEntry = childNodesEntry.item(k);
										if(aChildNodeEntry.getNodeName().equals("wd:CF_International_Assignee"))
										{											
											obj.put("positionId", aChildNodeEntry.getTextContent());											
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:CF_Pay_Through_Date"))
										{
											obj.put("payThroughDate", aChildNodeEntry.getTextContent());	
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:Last_End_International_Assignment_date"))
										{
											obj.put("endAssignmentDate", aChildNodeEntry.getTextContent());	
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:Last_End_International_Assignment_Reason"))
										{
											obj.put("endAssignmentReason", aChildNodeEntry.getTextContent());	
										}
									}
								}
							}
							details.put(obj);
						}
					}
				}
		    }
		}
		return details;
	}	
	
	private JSONArray createCSVFromWDPayeeTaxCodeData(Tenant tenant, InputStream is, SOAPConnection soapConnection, int startIndex, int endIndex, JSONArray headingWd,
			String loadCycle, String ruleName, String client) {

		targetContent = null;
		headingFromWD = "";
		try 
		{			 
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_PAYEE_TAX_CODE_FILE = requestfile.getAbsolutePath();				 
				 String outputfile = addPayeeTaxCodeToFindError(GET_PAYEE_TAX_CODE_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");
				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Payroll_GBR";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");
				 
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Payee_Tax_Codes_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String employeeId = "";					 
					 String effectiveDate = "";
					 String company = "";
					 String taxCodeNotification  = "";
					 String p45LeavingDate = "";
					 String previousEmploymentPay = "";
					 String previousEmploymentTax = "";
					 String taxCode = "";
					 String w1m1TaxBasis = "";
					 String finalStr = "";
					 String headerStr = "";
					 
					 Map<String,String> workerMap = null;
					 Map<String,String> companyMap = null;
					 Map<String,String> taxCodeMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {
						 if(j == 1)
						 {
							startIndex = 0;
							if(999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*999;
							}
						 }
						 else
						 {
							startIndex = endIndex;
							if(j*999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = (j*999);
							}
						 }
						outputfile = addPayeeTaxCodeToFindError(GET_PAYEE_TAX_CODE_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
						is = new FileInputStream(outputfile);
					    soapMessage = MessageFactory.newInstance().createMessage(null, is);
					    soapPart = soapMessage.getSOAPPart();
					    envelope = soapPart.getEnvelope();
						envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						{
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						}
						soapMessage.saveChanges();
				        out = new ByteArrayOutputStream();
				        soapMessage.writeTo(out);
				        strMsg = new String(out.toByteArray());
				        
				        soapConnectionFactory = SOAPConnectionFactory.newInstance();
						soapConnection = soapConnectionFactory.createConnection();
				        soapResponse = soapConnection.call(soapMessage, sourceUrl);
				        out = new ByteArrayOutputStream();
				        soapResponse.writeTo(out);
				        strMsg = new String(out.toByteArray(), "utf-8");
				        soapResp = XmlParserManager.parseXml(strMsg);
					        
						ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Payee_Tax_Codes_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> payeeTaxData = responseData.getChildren("wd:Payee_Tax_Code");
							
						 for(ReportElement reportElement : payeeTaxData)
						 {
							 ReportElement payeeTaxCodeData = reportElement.getChild("wd:Payee_Tax_Code_Data");
							 if(payeeTaxCodeData != null)
					         {
								 ReportElement workerRef = payeeTaxCodeData.getChild("wd:Worker_Reference");
								 if(workerRef != null)
								 {
									 List<ReportElement> workerData = workerRef.getChildren("wd:ID");					 
									 for(ReportElement workerElement:workerData)
									 {
										 workerMap = workerElement.getAllAttributes();
										 if(workerMap.get("wd:type").equals("Employee_ID"))
										 {
											 employeeId = workerElement.getValue().trim();
										 }
									 }
								 }
								 
								 ReportElement companyRef = payeeTaxCodeData.getChild("wd:Company_Reference");
								 if(companyRef != null)
								 {
									 List<ReportElement> companyData = companyRef.getChildren("wd:ID");					 
									 for(ReportElement companyElement:companyData)
									 {
										 companyMap = companyElement.getAllAttributes();
										 if(companyMap.get("wd:type").equals("Organization_Reference_ID"))
										 {
											 company = companyElement.getValue().trim();
										 }
									 }
								 }
								 effectiveDate = payeeTaxCodeData.getChild("wd:Effective_As_Of") != null?payeeTaxCodeData.getChild("wd:Effective_As_Of").getValue().trim():"";
								 
								 ReportElement taxCodeRef = payeeTaxCodeData.getChild("wd:Tax_Code_Notification_Source_Reference");
								 if(taxCodeRef != null)
								 {
									 List<ReportElement> taxCodeData = taxCodeRef.getChildren("wd:ID");					 
									 for(ReportElement taxCodeElement:taxCodeData)
									 {
										 taxCodeMap = taxCodeElement.getAllAttributes();
										 if(taxCodeMap.get("wd:type").equals("Payroll_Constant_Text_ID"))
										 {
											 taxCodeNotification = taxCodeElement.getValue().trim();
										 }
									 }
								 }
								 
								 p45LeavingDate = payeeTaxCodeData.getChild("wd:P45_Leaving_Date") != null?payeeTaxCodeData.getChild("wd:P45_Leaving_Date").getValue().trim():"";
								 if(!p45LeavingDate.isEmpty())
								 {
									 p45LeavingDate = p45LeavingDate.substring(0, 10);
								 }
								 previousEmploymentPay = payeeTaxCodeData.getChild("wd:Previous_Employment_Pay") != null?payeeTaxCodeData.getChild("wd:Previous_Employment_Pay").getValue().trim():"";
								 previousEmploymentTax = payeeTaxCodeData.getChild("wd:Previous_Employment_Tax") != null?payeeTaxCodeData.getChild("wd:Previous_Employment_Tax").getValue().trim():"";
								 taxCode = payeeTaxCodeData.getChild("wd:Tax_Code") != null?payeeTaxCodeData.getChild("wd:Tax_Code").getValue().trim():"";
								 w1m1TaxBasis = payeeTaxCodeData.getChild("wd:W1_M1_Tax_Basis") != null?payeeTaxCodeData.getChild("wd:W1_M1_Tax_Basis").getValue().trim():"";
								 if(w1m1TaxBasis.equalsIgnoreCase("0"))
								 {
									 w1m1TaxBasis = "n";
								 }
								 else
								 {
									 w1m1TaxBasis = "y"; 
								 }
					          }
							 
							 headingFromWD = "Employee_ID,Effective_As_Of,Company,Tax_Code_Notification,P45_Leaving_Date,Previous_Employment_Pay,Previous_Employment_Tax,Tax_Code,W1_M1_Tax_Basis";
							 
							 headerStr = employeeId + "," + effectiveDate + "," + company + "," + taxCodeNotification + "," + p45LeavingDate + "," + previousEmploymentPay + "," + previousEmploymentTax
									     + "," + taxCode + "," + w1m1TaxBasis;
							 
							 if(finalStr.equals(""))
							 {
								 finalStr = headingFromWD + "\n" + headerStr;
							 }
							 else
							 {
								 finalStr = finalStr + "\n" + headerStr;
							 }
						 }						 
					 }				 
					 				     
				     System.out.println(finalStr);
				     targetContent = finalStr.toString().getBytes();
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
					 complete = true;
				 }
			 }
			 
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return headingWd;
	
	}
	
	private String addPayeeTaxCodeToFindError(String xmlFile, List<String> columnList, String ruleName, int startIndex, int endIndex, String id) {
		
		File updatedRequestfile = null;
		try  
		{  
			File file = new File(xmlFile);     
			FileReader fr = new FileReader(file);   
			BufferedReader br = new BufferedReader(fr);   
			StringBuffer sb = new StringBuffer();     
			String line;
			boolean executed = true;
			while((line=br.readLine())!=null)  
			{  
				sb.append(line);  
				sb.append("\n");
				if(line.contains("bsvc:Get_Payee_Tax_Codes_Request") && executed)
				{
					executed = false;
					sb.append(" <bsvc:Request_Criteria>"); 
					sb.append("\n");
					for(int i = startIndex;i<endIndex;i++)
					{
						sb.append("  <bsvc:Worker_Reference bsvc:Descriptor=" + "\"" + id + "\"" + ">");
						sb.append("\n");
						sb.append("   <bsvc:ID bsvc:type=" + "\"" + id + "\"" + ">" + columnList.get(i) + "</bsvc:ID>");
						sb.append("\n");
						sb.append("  </bsvc:Worker_Reference>");
						sb.append("\n");
					}
					sb.append(" </bsvc:Request_Criteria>");
					sb.append("\n");
				}
			}  
			fr.close();
			br.close();	
			
			updatedRequestfile = File.createTempFile(ruleName + "_" + startIndex , ".xml");
			PrintWriter writer = new PrintWriter(updatedRequestfile);
		    writer.write(sb.toString());
			writer.flush();
			writer.close();
		}  
		catch(IOException e)  
		{  
			e.printStackTrace();  
		}
		return updatedRequestfile.getAbsolutePath(); 
	}
	
	private JSONArray createCSVFromWDUKPayrollID(Tenant tenant, InputStream is, SOAPConnection soapConnection, int startIndex, int endIndex, JSONArray headingWd,
			String loadCycle, String ruleName, String client) {

		headingFromWD = "";
		try 
		{			 
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_UK_PAYROLL_ID_REQUEST_FILE = requestfile.getAbsolutePath();
				 
				 String outputfile = addHireIdList(GET_UK_PAYROLL_ID_REQUEST_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");
				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");
				 
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String reportURL = "";
					 String WID = "";
					 String effectiveDate = "";
					 String assignmentReason = "";

					 String finalStr = "";
					 String headerStr = "";
					 
					 Map<String,String> widMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {
						 if(j == 1)
						 {
							startIndex = 0;
							if(999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*999;
							}
						 }
						 else
						 {
							startIndex = endIndex;
							if(j*999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = (j*999);
							}
						 }
						outputfile = addHireIdList(GET_UK_PAYROLL_ID_REQUEST_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
						is = new FileInputStream(outputfile);
					    soapMessage = MessageFactory.newInstance().createMessage(null, is);
					    soapPart = soapMessage.getSOAPPart();
					    envelope = soapPart.getEnvelope();
						envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						{
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						}
						soapMessage.saveChanges();
				        out = new ByteArrayOutputStream();
				        soapMessage.writeTo(out);
				        strMsg = new String(out.toByteArray());
				        
				        soapConnectionFactory = SOAPConnectionFactory.newInstance();
						soapConnection = soapConnectionFactory.createConnection();
				        soapResponse = soapConnection.call(soapMessage, sourceUrl);
				        out = new ByteArrayOutputStream();
				        soapResponse.writeTo(out);
				        strMsg = new String(out.toByteArray(), "utf-8");
				        soapResp = XmlParserManager.parseXml(strMsg);
					        
						ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Workers_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> applicantData = responseData.getChildren("wd:Worker");
							
						 for(ReportElement reportElement : applicantData)
						 {
							 ReportElement element1 = reportElement.getChild("wd:Worker_Data");
							 String workerId = element1.getChild("wd:Worker_ID").getValue().trim();
							 System.out.println("workerId--"+ workerId);
							 
							 ReportElement widRef = reportElement.getChild("wd:Worker_Reference");
							 if(widRef != null)
							 {
								 List<ReportElement> idList = widRef.getChildren("wd:ID");
								 for(ReportElement idElement:idList)
								 {
									 widMap = idElement.getAllAttributes();
									 if(widMap.get("wd:type").equals("WID"))
									 {
										 WID = idElement.getValue().trim();
									 }
								 }
							 }
							 else
							 {
								 WID = "";
							 }
							
							 /*if(tenant.getTenantName().equalsIgnoreCase("ibm6"))
							 {
								 reportURL = "https://" + tenant.getTenantUrl() + "customreport2/" + tenant.getTenantName() + "/ISU_Verify_Global/" + "UK_Payroll?Employee!WID=" + WID;
							 }
							 else if(tenant.getTenantName().equalsIgnoreCase("ibm10"))
							 {*/
								 reportURL = "https://" + tenant.getTenantUrl() + "customreport2/" + tenant.getTenantName() + "/ISU_Verify_Global/" + "UK_Payroll?Employee!WID=" + WID;
							 //}
							 
							 JSONArray jArr = createUKPayrollIDData(reportURL);
							 if(jArr != null && jArr.length() >0)
							 {
								 effectiveDate = "";
								 assignmentReason = "";
								 for(int i = 0; i<jArr.length(); i++) 
							     {
									JSONObject objects = jArr.getJSONObject(i);
									effectiveDate = objects.isNull("effectiveDate")?"":objects.getString("effectiveDate");
									if(!effectiveDate.isEmpty())
									{
										effectiveDate = effectiveDate.substring(0, 10);
										effectiveDate = convertDate(effectiveDate, "yyyy-MM-dd", "dd-MM-yyyy");
									}
									assignmentReason = objects.isNull("reason")?"":objects.getString("reason");
							     }
							 }
							 else
							 {
								 effectiveDate = "";
								 assignmentReason = ""; 
							 }
							 
							 headingFromWD = "Employee_ID,Effective_Date,Assignment_Reason";
							 
							 headerStr = workerId + "," +  effectiveDate + "," + assignmentReason;
							 
							 if(finalStr.equals(""))
							 {
								 finalStr = headingFromWD + "\n" + headerStr;
							 }
							 else
							 {
								 finalStr = finalStr + "\n" + headerStr;
							 }
						 }						 
					 }				 
					 				     
				     System.out.println(finalStr);
					 targetContent = finalStr.toString().getBytes();
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
					 complete = true;
				 }
			 }
			 
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return headingWd;
	}
	
	private JSONArray createUKPayrollIDData(String reportURL) throws JSONException {

		JSONArray jArr = new JSONArray();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		XPath xPath = XPathFactory.newInstance().newXPath();
		HttpBasicAuthentication httpBasicAuthentication = new HttpBasicAuthentication();
		String output = httpBasicAuthentication.getWithBasicAuthentication(reportURL, tenant.getTenantUser(), tenant.getTenantUserPassword());
		Reader reader1 = new StringReader(output);
		Document doc;
		try 
		{
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(new InputSource(reader1));
			doc.getDocumentElement().normalize();
			String expression = EMPLOYEE_APPLICANT_MAPPING_RESPONSE_EXPRESSION;
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) 
			{
				try 
				{
					jArr = parseNodesForUKPayrollData(nodeList);
				} 
				catch (DOMException e)
				{
					e.printStackTrace();
				}
			}			
		} 
		catch (SAXException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (XPathExpressionException e) 
		{
			e.printStackTrace();
		} 
		catch (ParserConfigurationException e) 
		{
			e.printStackTrace();
		}
		return jArr;
	}

	private JSONArray parseNodesForUKPayrollData(NodeList nodeList) throws DOMException, JSONException {
		
		JSONArray details = new JSONArray();
		for (int i = 0; i < nodeList.getLength(); i++) 
		{
		    Node nNode = nodeList.item(i);
		    if (nNode.getNodeName().equals("wd:Report_Data")) 
		    {
				if (nNode.getNodeType() == Node.ELEMENT_NODE) 
				{
					Element eElement = (Element) nNode;
					if (eElement.hasChildNodes()) 
					{
						NodeList childNodes = eElement.getChildNodes();
						for (int j = 0; j < childNodes.getLength(); j++) 
						{
							Node aChildNode = childNodes.item(j);
							if (aChildNode.getNodeName().equals("wd:Report_Entry")) 
							{
								Element eElementEntry = (Element) aChildNode;
								if (eElement.hasChildNodes()) 
								{
									NodeList childNodesEntry = eElementEntry.getChildNodes();
									for (int k = 0; k < childNodesEntry.getLength(); k++) 
									{										
										Node aChildNodeEntry = childNodesEntry.item(k);
										JSONObject obj = new JSONObject();
										if(aChildNodeEntry.getNodeName().equals("wd:Worker_UK_Payroll_IDs_group"))
										{
											Element eElemEntry = (Element) aChildNodeEntry;
											if (eElementEntry.hasChildNodes()) 
											{												
												NodeList childNodesEnt = eElemEntry.getChildNodes();
												for (int l = 0; l < childNodesEnt.getLength(); l++) 
												{													
													Node nChildNodeEntry = childNodesEnt.item(l);
													if(nChildNodeEntry.getNodeName().equals("wd:UK_Payroll_ID_Effective_Date"))
													{											
														obj.put("effectiveDate", nChildNodeEntry.getTextContent());											
													}
													else if(nChildNodeEntry.getNodeName().equals("wd:UK_Payroll_ID_Assignment_Reason"))
													{
														String assignReasonVal = nChildNodeEntry.getAttributes().getNamedItem("wd:Descriptor").toString();
														String assignmentReasonVal = assignReasonVal.replaceAll("\"", "");
														int pos = assignmentReasonVal.trim().indexOf("=") + 1;
														String actualAssignmentReasonVal = assignmentReasonVal.substring(pos, assignmentReasonVal.length());
														obj.put("reason", actualAssignmentReasonVal);	
													}													
												}
											}
										}
										details.put(obj);
									}
								}
							}
						}
					}
				}
		    }
		}
		return details;
	}
	
	private JSONArray createCSVFromWDPayrollPayeeNIData(Tenant tenant, InputStream is, SOAPConnection soapConnection, int startIndex, int endIndex, JSONArray headingWd,
			String loadCycle, String ruleName, String client) {
		
		targetContent = null;
		headingFromWD = "";
		try 
		{			 
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_PAYROLL_PAYEE_NI_DATA_FILE = requestfile.getAbsolutePath();				 
				 String outputfile = addPayrollPayeeNIToFindError(GET_PAYROLL_PAYEE_NI_DATA_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");
				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Payroll_GBR";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");
				 
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Payroll_Payee_NIs_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String employeeId = "";					 
					 String effectiveDate = "";
					 String company = "";
					 String niCategory  = "";
					 String calculationMethod = "";

					 String finalStr = "";
					 String headerStr = "";
					 
					 Map<String,String> workerMap = null;
					 Map<String,String> companyMap = null;
					 Map<String,String> categoryMap = null;
					 Map<String,String> calcMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {
						 if(j == 1)
						 {
							startIndex = 0;
							if(999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*999;
							}
						 }
						 else
						 {
							startIndex = endIndex;
							if(j*999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = (j*999);
							}
						 }
						outputfile = addPayrollPayeeNIToFindError(GET_PAYROLL_PAYEE_NI_DATA_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
						is = new FileInputStream(outputfile);
					    soapMessage = MessageFactory.newInstance().createMessage(null, is);
					    soapPart = soapMessage.getSOAPPart();
					    envelope = soapPart.getEnvelope();
						envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						{
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						}
						soapMessage.saveChanges();
				        out = new ByteArrayOutputStream();
				        soapMessage.writeTo(out);
				        strMsg = new String(out.toByteArray());
				        
				        soapConnectionFactory = SOAPConnectionFactory.newInstance();
						soapConnection = soapConnectionFactory.createConnection();
				        soapResponse = soapConnection.call(soapMessage, sourceUrl);
				        out = new ByteArrayOutputStream();
				        soapResponse.writeTo(out);
				        strMsg = new String(out.toByteArray(), "utf-8");
				        soapResp = XmlParserManager.parseXml(strMsg);
					        
						ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Payroll_Payee_NIs_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> payrollPayeeData = responseData.getChildren("wd:Payroll_Payee_NI");
							
						 for(ReportElement reportElement : payrollPayeeData)
						 {
							 ReportElement payrollPayeeNICodeData = reportElement.getChild("wd:Payroll_Payee_NI_Data");
							 if(payrollPayeeNICodeData != null)
					         {
								 ReportElement workerRef = payrollPayeeNICodeData.getChild("wd:Worker_Reference");
								 if(workerRef != null)
								 {
									 List<ReportElement> workerData = workerRef.getChildren("wd:ID");					 
									 for(ReportElement workerElement:workerData)
									 {
										 workerMap = workerElement.getAllAttributes();
										 if(workerMap.get("wd:type").equals("Employee_ID"))
										 {
											 employeeId = workerElement.getValue().trim();
										 }
									 }
								 }
								 
								 ReportElement companyRef = payrollPayeeNICodeData.getChild("wd:Company_Reference");
								 if(companyRef != null)
								 {
									 List<ReportElement> companyData = companyRef.getChildren("wd:ID");					 
									 for(ReportElement companyElement:companyData)
									 {
										 companyMap = companyElement.getAllAttributes();
										 if(companyMap.get("wd:type").equals("Organization_Reference_ID"))
										 {
											 company = companyElement.getValue().trim();
										 }
									 }
								 }
								 effectiveDate = payrollPayeeNICodeData.getChild("wd:Effective_As_Of") != null?payrollPayeeNICodeData.getChild("wd:Effective_As_Of").getValue().trim():"";
								 effectiveDate = convertDate(effectiveDate, "yyyy-MM-dd", "dd-MM-yyyy");
								 
								 ReportElement niCatRef = payrollPayeeNICodeData.getChild("wd:NI_Category_Reference");
								 if(niCatRef != null)
								 {
									 List<ReportElement> niCatData = niCatRef.getChildren("wd:ID");					 
									 for(ReportElement niCatElement:niCatData)
									 {
										 categoryMap = niCatElement.getAllAttributes();
										 if(categoryMap.get("wd:type").equals("NI_Category_Letter"))
										 {
											 niCategory = niCatElement.getValue().trim();
										 }
									 }
								 }
								 
								 ReportElement calcMethodRef = payrollPayeeNICodeData.getChild("wd:Calculation_Method_Reference");
								 if(calcMethodRef != null)
								 {
									 List<ReportElement> calcMethodData = calcMethodRef.getChildren("wd:ID");					 
									 for(ReportElement calcMethodElement:calcMethodData)
									 {
										 calcMap = calcMethodElement.getAllAttributes();
										 if(calcMap.get("wd:type").equals("Payroll_Constant_Text_ID"))
										 {
											 calculationMethod = calcMethodElement.getValue().trim();
										 }
									 }
								 }
								 
					          }
							 
							 headingFromWD = "Employee_ID,Effective_As_Of,Company,NI_Category,Calculation_Method";
							 
							 headerStr = employeeId + "," + effectiveDate + "," + company + "," + niCategory + "," + calculationMethod;
							 
							 if(finalStr.equals(""))
							 {
								 finalStr = headingFromWD + "\n" + headerStr;
							 }
							 else
							 {
								 finalStr = finalStr + "\n" + headerStr;
							 }
						 }						 
					 }				 
					 				     
				     System.out.println(finalStr);
				     targetContent = finalStr.toString().getBytes();
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
					 complete = true;
				 }
			 }
			 
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return headingWd;
	}
	
	private String addPayrollPayeeNIToFindError(String xmlFile, List<String> columnList, String ruleName, int startIndex, int endIndex, String id) {
		
		File updatedRequestfile = null;
		try  
		{  
			File file = new File(xmlFile);     
			FileReader fr = new FileReader(file);   
			BufferedReader br = new BufferedReader(fr);   
			StringBuffer sb = new StringBuffer();     
			String line;
			boolean executed = true;
			while((line=br.readLine())!=null)  
			{  
				sb.append(line);  
				sb.append("\n");
				if(line.contains("bsvc:Get_Payroll_Payee_NIs_Request") && executed)
				{
					executed = false;
					sb.append(" <bsvc:Request_Criteria>"); 
					sb.append("\n");
					for(int i = startIndex;i<endIndex;i++)
					{
						sb.append("  <bsvc:Worker_Reference bsvc:Descriptor=" + "\"" + id + "\"" + ">");
						sb.append("\n");
						sb.append("   <bsvc:ID bsvc:type=" + "\"" + id + "\"" + ">" + columnList.get(i) + "</bsvc:ID>");
						sb.append("\n");
						sb.append("  </bsvc:Worker_Reference>");
						sb.append("\n");
					}
					sb.append(" </bsvc:Request_Criteria>");
					sb.append("\n");
				}
			}  
			fr.close();
			br.close();	
			
			updatedRequestfile = File.createTempFile(ruleName + "_" + startIndex , ".xml");
			PrintWriter writer = new PrintWriter(updatedRequestfile);
		    writer.write(sb.toString());
			writer.flush();
			writer.close();
		}  
		catch(IOException e)  
		{  
			e.printStackTrace();  
		}
		return updatedRequestfile.getAbsolutePath(); 
	}
	
	private JSONArray createCSVFromWDChangeBenefitsLifeEvents(Tenant tenant, InputStream is, SOAPConnection soapConnection, int startIndex, int endIndex, JSONArray headingWd,
			String loadCycle, String ruleName, String client) {

		targetContent = null;
		String checkFile = null;
		String workerId = "";
		String coverageBeginDate = "";
		String originalCoverageBeginDate = "";
		String deductionBeginDate = "";
		String healthCarePlanName = "";
		String healthCarePlanNameArr = "";
		String dependencyName = "";
		String dependencyNameArr = "";
		String insurancePlanName = "";
		String insurancePlanNameArr = "";
		String retirementPlanName = "";
		String retirementPlanNameArr = "";
		String additionalPlanName = "";
		String additionalPlanNameArr = "";
		String additionalCoveragePlanName = "";
		String additionalCoveragePlanNameArr = "";
		String finalStr = "";
		String headerStr = "";
		
		Map<String,String> workerMap = null;
		Map<String,String> healthCareMap = null;
		Map<String,String> dependencyMap = null;
		Map<String,String> insuranceMap = null;
		Map<String,String> retirementMap = null;
		Map<String,String> additionalMap = null;
		Map<String,String> addncoverageMap = null;
		
		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_CHANGE_BENEFITS_LIFE_EVENT_REQUEST_FILE = requestfile.getAbsolutePath();
				 if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addWorkerIdListToFindError(GET_CHANGE_BENEFITS_LIFE_EVENT_REQUEST_FILE, columnList.get(i), ruleName, "Employee_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement rptElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement benefitEnrollmentData = rptElement.getChild("env:Body")
										.getChild("wd:Get_Workers_Response")
										.getChild("wd:Response_Data")
										.getChild("wd:Worker")
										.getChild("wd:Worker_Data")
			 							.getChild("wd:Benefit_Enrollment_Data");
							 
							 if(benefitEnrollmentData == null)
							 {
								 errorList.add(columnList.get(i));
							 }
							 else
							 {
								 ReportElement responseData = rptElement.getChild("env:Body")
											.getChild("wd:Get_Workers_Response")
											.getChild("wd:Response_Data");
								 
								 List<ReportElement> workerList = responseData.getChildren("wd:Worker");
								 if(workerList != null && workerList.size()>0)
								 {
										coverageBeginDate = "";
										originalCoverageBeginDate = "";
										deductionBeginDate = "";
										healthCarePlanNameArr = "";
										dependencyNameArr = "";
										insurancePlanNameArr = "";
										retirementPlanNameArr = "";
										additionalPlanNameArr = "";
										additionalCoveragePlanNameArr = "";
										
									 for(ReportElement reportElement : workerList)
									 {
										 ReportElement workerRef = reportElement.getChild("wd:Worker_Reference");
										 if(workerRef != null)
										 {
											 List<ReportElement> idData = workerRef.getChildren("wd:ID");					 
											 for(ReportElement wdElement:idData)
											 {
												 workerMap = wdElement.getAllAttributes();
												 if(workerMap.get("wd:type").equals("Employee_ID"))
												 {
													 workerId = wdElement.getValue().trim();
													 System.out.println("workerId:"+workerId);
												 }
											 }
										 }
										 ReportElement healthCareData = reportElement.getChild("wd:Worker_Data")
						 							.getChild("wd:Benefit_Enrollment_Data")
						 							.getChild("wd:Health_Care_Data");
										 
										 if(healthCareData != null)
										 {
											 List<ReportElement> benefitElectionHealthCareDataList = healthCareData.getChild("wd:Health_Care_Period_Data")
							 							.getChildren("wd:Health_Care_Coverage_Data");
											 
											 for(ReportElement benefitElectionHealthCareDataElement: benefitElectionHealthCareDataList)
											 {
												 ReportElement 	benefitElectionHealthCareData = benefitElectionHealthCareDataElement.getChild("wd:Benefit_Election_Data");
												 if(benefitElectionHealthCareData != null)
												 {
													 coverageBeginDate = benefitElectionHealthCareData.getChild("wd:Coverage_Begin_Date") != null?benefitElectionHealthCareData.getChild("wd:Coverage_Begin_Date").getValue().trim().substring(0, 10):"";
													 originalCoverageBeginDate = benefitElectionHealthCareData.getChild("wd:Original_Coverage_Begin_Date") != null?benefitElectionHealthCareData.getChild("wd:Original_Coverage_Begin_Date").getValue().trim().substring(0, 10):"";
													 deductionBeginDate = benefitElectionHealthCareData.getChild("wd:Deduction_Begin_Date") != null?benefitElectionHealthCareData.getChild("wd:Deduction_Begin_Date").getValue().trim().substring(0, 10):"";
													 
													 ReportElement benefitPlanSummaryData = benefitElectionHealthCareData.getChild("wd:Benefit_Plan_Summary_Data");
													 if(benefitPlanSummaryData != null)
													 {
														 ReportElement healthCareRef = benefitPlanSummaryData.getChild("wd:Benefit_Plan_Reference");
														 if(healthCareRef != null)
														 {
															 List<ReportElement> healthCareSummaryData = healthCareRef.getChildren("wd:ID");					 
															 for(ReportElement healthCareElement:healthCareSummaryData)
															 {
																 healthCareMap = healthCareElement.getAllAttributes();
																 if(healthCareMap.get("wd:type").equals("Health_Care_Coverage_Plan_ID"))
																 {
																	 healthCarePlanName = healthCareElement.getValue().trim();
																	 if(healthCarePlanNameArr.equals(""))
																	 {
																		 healthCarePlanNameArr = healthCarePlanName;
																	 }
																	 else
																	 {
																		 healthCarePlanNameArr = healthCarePlanNameArr + "~" + healthCarePlanName;
																	 }
																 }
															 }
														 }
													 }
												 }
												 
												 List<ReportElement> dependencyList = benefitElectionHealthCareDataElement.getChildren("wd:Dependent_Coverage_Data");
												 if(dependencyList != null && dependencyList.size()>0)
												 {
													 for(ReportElement dependencyElement:dependencyList)
													 {
														 ReportElement dependencyRef = dependencyElement.getChild("wd:Dependent_Reference");
														 if(dependencyRef != null)
														 {
															 List<ReportElement> dependencyData = dependencyRef.getChildren("wd:ID");					 
															 for(ReportElement dependencyCoverageElement:dependencyData)
															 {
																 dependencyMap = dependencyCoverageElement.getAllAttributes();
																 if(dependencyMap.get("wd:type").equals("Dependent_ID"))
																 {
																	 dependencyName = dependencyCoverageElement.getValue().trim();
																	 if(dependencyNameArr.equals(""))
																	 {
																		 dependencyNameArr = dependencyName;
																	 }
																	 else
																	 {
																		 dependencyNameArr = dependencyNameArr + "~" + dependencyName;
																	 }
																 }
															 }
														 }
													 }
												 }
											 }
										 }
										 
										 ReportElement insuranceData = reportElement.getChild("wd:Worker_Data")
						 							.getChild("wd:Benefit_Enrollment_Data")
						 							.getChild("wd:Insurance_Data");
										 
										 if(insuranceData != null)
										 {
											 List<ReportElement> insuranceCoverageList = insuranceData.getChild("wd:Insurance_Period_Data")
							 							.getChildren("wd:Insurance_Coverage_Data");

											 for(ReportElement coverageElement:insuranceCoverageList)
											 {
												 ReportElement benefitElectionInsuranceData = coverageElement.getChild("wd:Benefit_Election_Data");
												 if(benefitElectionInsuranceData != null)
												 {
													 if(coverageBeginDate.isEmpty())
													 {
														 coverageBeginDate = benefitElectionInsuranceData.getChild("wd:Coverage_Begin_Date") != null?benefitElectionInsuranceData.getChild("wd:Coverage_Begin_Date").getValue().trim().substring(0, 10):"";
													 }
													 if(originalCoverageBeginDate.isEmpty())
													 {
														 originalCoverageBeginDate = benefitElectionInsuranceData.getChild("wd:Original_Coverage_Begin_Date") != null?benefitElectionInsuranceData.getChild("wd:Original_Coverage_Begin_Date").getValue().trim().substring(0, 10):"";
													 }
													 if(deductionBeginDate.isEmpty())
													 {
														 deductionBeginDate = benefitElectionInsuranceData.getChild("wd:Deduction_Begin_Date") != null?benefitElectionInsuranceData.getChild("wd:Deduction_Begin_Date").getValue().trim().substring(0, 10):"";
													 }
													 
													 ReportElement insurancePlanSummaryData = benefitElectionInsuranceData.getChild("wd:Benefit_Plan_Summary_Data");
													 if(insurancePlanSummaryData != null)
													 {
														 ReportElement insuranceRef = insurancePlanSummaryData.getChild("wd:Benefit_Plan_Reference");
														 if(insuranceRef != null)
														 {
															 List<ReportElement> insuranceRefData = insuranceRef.getChildren("wd:ID");					 
															 for(ReportElement insuranceElement:insuranceRefData)
															 {
																 insuranceMap = insuranceElement.getAllAttributes();
																 if(insuranceMap.get("wd:type").equals("Insurance_Coverage_Plan_ID"))
																 {
																	 insurancePlanName = insuranceElement.getValue().trim();
																	 if(insurancePlanNameArr.equals(""))
																	 {
																		 insurancePlanNameArr = insurancePlanName;
																	 }
																	 else
																	 {
																		 insurancePlanNameArr = insurancePlanNameArr + "~" + insurancePlanName;
																	 }
																 }
															 }
														 }
													 }
												 }
											 }
										 }
										 
										 ReportElement retireSavingData = reportElement.getChild("wd:Worker_Data")
						 							.getChild("wd:Benefit_Enrollment_Data")
						 							.getChild("wd:Retirement_Savings_Data");
										 
										 if(retireSavingData != null)
										 {
											 List<ReportElement> benefitElectionRetirementDataList = retireSavingData.getChild("wd:Retirement_Savings_Period_Data")
							 							.getChildren("wd:Retirement_Savings_Coverage_Data");
											 
											 for(ReportElement benefitElectionRetirementDataElement:benefitElectionRetirementDataList)
											 {
												 ReportElement 	benefitElectionRetirementData = benefitElectionRetirementDataElement.getChild("wd:Benefit_Election_Data");
												 if(benefitElectionRetirementData != null)
												 {
													 if(coverageBeginDate.isEmpty())
													 {
														 coverageBeginDate = benefitElectionRetirementData.getChild("wd:Coverage_Begin_Date") != null?benefitElectionRetirementData.getChild("wd:Coverage_Begin_Date").getValue().trim().substring(0, 10):"";
													 }
													 if(originalCoverageBeginDate.isEmpty())
													 {
														 originalCoverageBeginDate = benefitElectionRetirementData.getChild("wd:Original_Coverage_Begin_Date") != null?benefitElectionRetirementData.getChild("wd:Original_Coverage_Begin_Date").getValue().trim().substring(0, 10):"";
													 }
													 if(deductionBeginDate.isEmpty())
													 {
														 deductionBeginDate = benefitElectionRetirementData.getChild("wd:Deduction_Begin_Date") != null?benefitElectionRetirementData.getChild("wd:Deduction_Begin_Date").getValue().trim().substring(0, 10):"";
													 }
													 
													 ReportElement retirementPlanSummaryData = benefitElectionRetirementData.getChild("wd:Benefit_Plan_Summary_Data");
													 if(retirementPlanSummaryData != null)
													 {
														 ReportElement retirementRef = retirementPlanSummaryData.getChild("wd:Benefit_Plan_Reference");
														 if(retirementRef != null)
														 {
															 List<ReportElement> retireSummaryData = retirementRef.getChildren("wd:ID");					 
															 for(ReportElement retirementElement:retireSummaryData)
															 {
																 retirementMap = retirementElement.getAllAttributes();
																 if(retirementMap.get("wd:type").equals("Defined_Contribution_Plan_ID"))
																 {
																	 retirementPlanName = retirementElement.getValue().trim();
																	 if(retirementPlanNameArr.equals(""))
																	 {
																		 retirementPlanNameArr = retirementPlanName;
																	 }
																	 else
																	 {
																		 retirementPlanNameArr = retirementPlanNameArr + "~" + retirementPlanName;
																	 }
																 }
															 }
														 }
													 }
												 }
											 }
										 }
										 
										 ReportElement additionalBenefitData = reportElement.getChild("wd:Worker_Data")
						 							.getChild("wd:Benefit_Enrollment_Data")
						 							.getChild("wd:Additional_Benefits_Data");
										 
										 if(additionalBenefitData != null)
										 {
											 List<ReportElement> benefitElectionAdditionalDataList = additionalBenefitData.getChild("wd:Additional_Benefits_Period_Data")
							 							.getChildren("wd:Additional_Benefits_Coverage_Data");
							 							
											 for(ReportElement benefitElectionAdditionalDataElement: benefitElectionAdditionalDataList)
											 {
												 ReportElement coverageTargetRef = benefitElectionAdditionalDataElement.getChild("wd:Additional_Benefits_Coverage_Target_Reference");
												 if(coverageTargetRef != null)
												 {
													 List<ReportElement> coverageTargetData = coverageTargetRef.getChildren("wd:ID");					 
													 for(ReportElement coverageTargetElement:coverageTargetData)
													 {
														 addncoverageMap = coverageTargetElement.getAllAttributes();
														 if(addncoverageMap.get("wd:type").equals("Additional_Benefits_Coverage_Target_ID"))
														 {
															 additionalCoveragePlanName = coverageTargetElement.getValue().trim();
															 if(additionalCoveragePlanNameArr.equals(""))
															 {
																 additionalCoveragePlanNameArr = additionalCoveragePlanName;
															 }
															 else
															 {
																 additionalCoveragePlanNameArr = additionalCoveragePlanNameArr + "~" + additionalCoveragePlanName;
															 }
														 }
													 }
												 }
												 ReportElement benefitElectionAdditionalData = benefitElectionAdditionalDataElement.getChild("wd:Benefit_Election_Data");
												 if(benefitElectionAdditionalDataElement != null)
												 {
													 if(coverageBeginDate.isEmpty())
													 {
														 coverageBeginDate = benefitElectionAdditionalData.getChild("wd:Coverage_Begin_Date") != null?benefitElectionAdditionalData.getChild("wd:Coverage_Begin_Date").getValue().trim().substring(0, 10):"";
													 }
													 if(originalCoverageBeginDate.isEmpty())
													 {
														 originalCoverageBeginDate = benefitElectionAdditionalData.getChild("wd:Original_Coverage_Begin_Date") != null?benefitElectionAdditionalData.getChild("wd:Original_Coverage_Begin_Date").getValue().trim().substring(0, 10):"";
													 }
													 if(deductionBeginDate.isEmpty())
													 {
														 deductionBeginDate = benefitElectionAdditionalData.getChild("wd:Deduction_Begin_Date") != null?benefitElectionAdditionalData.getChild("wd:Deduction_Begin_Date").getValue().trim().substring(0, 10):"";
													 }
													 
													 ReportElement additionalPlanSummaryData = benefitElectionAdditionalData.getChild("wd:Benefit_Plan_Summary_Data");
													 if(additionalPlanSummaryData != null)
													 {
														 ReportElement additionalRef = additionalPlanSummaryData.getChild("wd:Benefit_Plan_Reference");
														 if(additionalRef != null)
														 {
															 List<ReportElement> addnSummaryData = additionalRef.getChildren("wd:ID");					 
															 for(ReportElement additionalElement:addnSummaryData)
															 {
																 additionalMap = additionalElement.getAllAttributes();
																 if(additionalMap.get("wd:type").equals("Additional_Benefits_Plan_ID"))
																 {
																	 additionalPlanName = additionalElement.getValue().trim();
																	 if(additionalPlanNameArr.equals(""))
																	 {
																		 additionalPlanNameArr = additionalPlanName;
																	 }
																	 else
																	 {
																		 additionalPlanNameArr = additionalPlanNameArr + "~" + additionalPlanName;
																	 }
																 }
															 }
														 }
													 }
												 }
											 }
										 }
										 
									 }
									 headingFromWD = "Employee_ID,Coverage_Begin_Date,Original_Coverage_Begin_Date,Deduction_Begin_Date,Health_Care_Plan_Name,Dependency_Name,Insurance_Plan_Name,"
									 		          + "Retirement_Plan_Name,Additional_Benefit_Plan_Name,Additional_Benefit_Coverage_Name";
									 
									 headerStr = workerId + "," + coverageBeginDate + "," + originalCoverageBeginDate + "," + deductionBeginDate + "," + healthCarePlanNameArr + "," + dependencyNameArr
											    + "," + insurancePlanNameArr + "," + retirementPlanNameArr + "," + additionalPlanNameArr + "," + additionalCoveragePlanNameArr;
									 
									 if(finalStr.equals(""))
									 {
										 finalStr = headingFromWD + "\n" + headerStr;
									 }
									 else
									 {
										 finalStr = finalStr + "\n" + headerStr;
									 }
								 }
							 }
						 }
					 }
					 columnList.removeAll(errorList);
					 wdCount = columnList.size();
				 }
				 System.out.println(finalStr);
				 targetContent = finalStr.toString().getBytes();
				 
				 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
				 complete = true;
			}
			 
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}
	
	private JSONArray createCSVFromWDPayeeInputData(Tenant tenant, InputStream is, SOAPConnection soapConnection, int startIndex, int endIndex, JSONArray headingWd,
			String loadCycle, String ruleName, String client) {

		targetContent = null;
		String checkFile = null;
		String workerId = "";
		String WID = "";
		String reportURL = "";
		String earningCode = "";
		String deductionCode = "";
		String earnDedAmount = "";
		String earningCodeArr = "";
		String deductionCodeArr = "";
		String earnDedAmountArr = "";

		String finalStr = "";
		String headerStr = "";
		
		Map<String,String> widMap = null;
		
		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_PAYEE_INPUT_DATA_REQUEST_FILE = requestfile.getAbsolutePath();
				 if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addWorkerIdListToFindError(GET_PAYEE_INPUT_DATA_REQUEST_FILE, columnList.get(i), ruleName, "Employee_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement rptElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = rptElement.getChild("env:Body")
										.getChild("wd:Get_Workers_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
							 else
							 {
								 ReportElement responseData = rptElement.getChild("env:Body")
											.getChild("wd:Get_Workers_Response")
											.getChild("wd:Response_Data");
								 
								 List<ReportElement> workerList = responseData.getChildren("wd:Worker");
								 if(workerList != null && workerList.size()>0)
								 {										
									 for(ReportElement reportElement : workerList)
									 {
										 ReportElement element1 = reportElement.getChild("wd:Worker_Data");
										 workerId = element1.getChild("wd:Worker_ID").getValue().trim();
										 System.out.println("workerId--"+ workerId);
										 
										 ReportElement widRef = reportElement.getChild("wd:Worker_Reference");
										 if(widRef != null)
										 {
											 List<ReportElement> idList = widRef.getChildren("wd:ID");
											 for(ReportElement idElement:idList)
											 {
												 widMap = idElement.getAllAttributes();
												 if(widMap.get("wd:type").equals("WID"))
												 {
													 WID = idElement.getValue().trim();
												 }
											 }
										 }
										 else
										 {
											 WID = "";
										 }
										 
										 /*if(tenant.getTenantName().equalsIgnoreCase("ibm6"))
										 {
											 reportURL = "https://" + tenant.getTenantUrl() + "customreport2/" + tenant.getTenantName() + "/ISU_Verify_Global/" + "Payee_Input_Data?Worker!WID=" + WID;
										 }
										 else if(tenant.getTenantName().equalsIgnoreCase("ibm10"))
										 {*/
											 reportURL = "https://" + tenant.getTenantUrl() + "customreport2/" + tenant.getTenantName() + "/ISU_Verify_Global/" + "Payee_Input_Data?Worker!WID=" + WID;
										 //}
										 
										 JSONArray jArr = createPayeeInputData(reportURL);
										 
										 if(jArr != null && jArr.length() >0)
										 {
												earningCodeArr = "";
												deductionCodeArr = "";
												earnDedAmountArr = "";
												for(int j = 0; j<jArr.length(); j++) 
										        {
													JSONObject objects = jArr.getJSONObject(j);
													deductionCode = objects.isNull("deduction")?"":objects.getString("deduction");
													if(deductionCodeArr.equals(""))
													{
														deductionCodeArr = deductionCode;
													}
													else
													{
														if(!deductionCode.isEmpty())
														{
															deductionCodeArr = deductionCodeArr + "~" + deductionCode;
														}
													}
													earningCode = objects.isNull("earning")?"":objects.getString("earning");
													if(earningCodeArr.equals(""))
													{
														earningCodeArr = earningCode;
													}
													else
													{
														if(!earningCode.isEmpty())
														{
															earningCodeArr = earningCodeArr + "~" + earningCode;
														}
													}
													earnDedAmount = objects.isNull("amount")?"":objects.getString("amount");
													if(earnDedAmountArr.equals(""))
													{
														earnDedAmountArr = earnDedAmount;
													}
													else
													{
														if(!earnDedAmount.isEmpty())
														{
															earnDedAmountArr = earnDedAmountArr + "~" + earnDedAmount;
														}
													}
										        }
										 }
										 else
										 {
											 earningCodeArr = "";
											 deductionCodeArr = "";
											 earnDedAmountArr = "";
										 }
									 }
									 headingFromWD = "Employee_ID,Deduction_Code,Earning_Code,Amount";
									 
									 headerStr = workerId + "," + deductionCodeArr + "," + earningCodeArr + "," + earnDedAmountArr;
									 
									 if(finalStr.equals(""))
									 {
										 finalStr = headingFromWD + "\n" + headerStr;
									 }
									 else
									 {
										 finalStr = finalStr + "\n" + headerStr;
									 }
								 }
							 }
						 }
					 }
					 columnList.removeAll(errorList);
					 wdCount = columnList.size();
				 }
				 System.out.println(finalStr);
				 targetContent = finalStr.toString().getBytes();
				 
				 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
				 complete = true;
			}
			 
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}
	
	private JSONArray createPayeeInputData(String reportURL) throws JSONException {
		
		JSONArray jArr = new JSONArray();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		XPath xPath = XPathFactory.newInstance().newXPath();
		HttpBasicAuthentication httpBasicAuthentication = new HttpBasicAuthentication();
		String output = httpBasicAuthentication.getWithBasicAuthentication(reportURL, tenant.getTenantUser(), tenant.getTenantUserPassword());
		Reader reader1 = new StringReader(output);
		Document doc;
		try 
		{
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(new InputSource(reader1));
			doc.getDocumentElement().normalize();
			String expression = EMPLOYEE_APPLICANT_MAPPING_RESPONSE_EXPRESSION;
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) 
			{
				try 
				{
					jArr = parseNodesForPayeeInputData(nodeList);
				} 
				catch (DOMException e)
				{
					e.printStackTrace();
				}
			}			
		} 
		catch (SAXException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (XPathExpressionException e) 
		{
			e.printStackTrace();
		} 
		catch (ParserConfigurationException e) 
		{
			e.printStackTrace();
		}
		return jArr;
	}

	private JSONArray parseNodesForPayeeInputData(NodeList nodeList) throws JSONException {
		
		JSONArray details = new JSONArray();
		for (int i = 0; i < nodeList.getLength(); i++) 
		{
		    Node nNode = nodeList.item(i);
		    if (nNode.getNodeName().equals("wd:Report_Data")) 
		    {
				if (nNode.getNodeType() == Node.ELEMENT_NODE) 
				{
					Element eElement = (Element) nNode;
					if (eElement.hasChildNodes()) 
					{
						NodeList childNodes = eElement.getChildNodes();
						for (int j = 0; j < childNodes.getLength(); j++) 
						{
							Node aChildNode = childNodes.item(j);
							JSONObject obj = new JSONObject();
							if (aChildNode.getNodeName().equals("wd:Report_Entry")) 
							{
								Element eElementEntry = (Element) aChildNode;
								if (eElement.hasChildNodes()) 
								{
									NodeList childNodesEntry = eElementEntry.getChildNodes();
									for (int k = 0; k < childNodesEntry.getLength(); k++) 
									{										
										Node aChildNodeEntry = childNodesEntry.item(k);

									    if(aChildNodeEntry.getNodeName().equals("wd:Deduction"))
										{
											String dedVal = aChildNodeEntry.getAttributes().getNamedItem("wd:Descriptor").toString();
											String deducVal = dedVal.replaceAll("\"", "");
											int pos = deducVal.trim().indexOf("=") + 1;
											String deductSecVal = deducVal.substring(pos, deducVal.length());
											String deductionVal = deductSecVal.substring(0, deductSecVal.indexOf("-"));
											obj.put("deduction", deductionVal.trim());	
										}
									    else if(aChildNodeEntry.getNodeName().equals("wd:Earning"))
										{
									    	String actEarningVal = "";
											String ernVal = aChildNodeEntry.getAttributes().getNamedItem("wd:Descriptor").toString();
											String earnVal = ernVal.replaceAll("\"", "");
											int pos = earnVal.trim().indexOf("=") + 1;
											String earningVal = earnVal.substring(pos, earnVal.length());
											if(earningVal.contains("-"))
											{
												actEarningVal = earningVal.substring(0, earningVal.indexOf("-"));
											}
											obj.put("earning", actEarningVal.trim());	
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:Amount"))
										{
											obj.put("amount", aChildNodeEntry.getTextContent());	
										}										
									}
								}
							}
							details.put(obj);
						}
					}
				}
		    }
		}
		return details;
	}

	private JSONArray createCSVFromWDStartIntlAssignment(Tenant tenant, InputStream is, SOAPConnection soapConnection, int startIndex, int endIndex, JSONArray headingWd,
			String loadCycle, String ruleName, String client) {

		headingFromWD = "";
		targetContent = null;
		try 
		{			 
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_START_INTERNATIONAL_ASSIGNMENT_REQUEST_FILE = requestfile.getAbsolutePath();
				 
				 String outputfile = addHireIdList(GET_START_INTERNATIONAL_ASSIGNMENT_REQUEST_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");
				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");
				 
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String WID = "";
					 String startIADate = "";
					 String startIAReason = "";
					 String startIAType = "";
					 String expectedIAEndDate = "";
					 String supOrg = "";
					 String empType = "";
					 String jobProfile = "";
					 String location = "";
					 String workSpace = "";
					 String timeType = "";
					 String workShift = "";
					 String workHoursProfile = "";
					 String payRateType = "";
					 
					 String finalStr = "";
					 String headerStr = "";
					 String reportURL = "";
					 
					 Map<String,String> widMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {
						 if(j == 1)
						 {
							startIndex = 0;
							if(999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*999;
							}
						 }
						 else
						 {
							startIndex = endIndex;
							if(j*999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = (j*999);
							}
						 }
						outputfile = addHireIdList(GET_START_INTERNATIONAL_ASSIGNMENT_REQUEST_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
						is = new FileInputStream(outputfile);
					    soapMessage = MessageFactory.newInstance().createMessage(null, is);
					    soapPart = soapMessage.getSOAPPart();
					    envelope = soapPart.getEnvelope();
						envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						{
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						}
						soapMessage.saveChanges();
				        out = new ByteArrayOutputStream();
				        soapMessage.writeTo(out);
				        strMsg = new String(out.toByteArray());
				        
				        soapConnectionFactory = SOAPConnectionFactory.newInstance();
						soapConnection = soapConnectionFactory.createConnection();
				        soapResponse = soapConnection.call(soapMessage, sourceUrl);
				        out = new ByteArrayOutputStream();
				        soapResponse.writeTo(out);
				        strMsg = new String(out.toByteArray(), "utf-8");
				        soapResp = XmlParserManager.parseXml(strMsg);
					        
						ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Workers_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> applicantData = responseData.getChildren("wd:Worker");
							
						 for(ReportElement reportElement : applicantData)
						 {
							 ReportElement element1 = reportElement.getChild("wd:Worker_Data");
							 String workerId = element1.getChild("wd:Worker_ID").getValue().trim();
							 System.out.println("workerId--"+ workerId);
							 
							 ReportElement widRef = reportElement.getChild("wd:Worker_Reference");
							 if(widRef != null)
							 {
								 List<ReportElement> idList = widRef.getChildren("wd:ID");
								 for(ReportElement idElement:idList)
								 {
									 widMap = idElement.getAllAttributes();
									 if(widMap.get("wd:type").equals("WID"))
									 {
										 WID = idElement.getValue().trim();
									 }
								 }
							 }
							 else
							 {
								 WID = "";
							 }
							 
							 /*if(tenant.getTenantName().equalsIgnoreCase("ibm6"))
							 {
								 reportURL = "https://" + tenant.getTenantUrl() + "customreport2/" + tenant.getTenantName() + "/ISU_Verify_Global/" + "Start_IA?Effective_as_of_Date=2021-09-01-07:00&Employee!WID=" + WID;
							 }
							 else if(tenant.getTenantName().equalsIgnoreCase("ibm10"))
							 {*/
								 reportURL = "https://" + tenant.getTenantUrl() + "customreport2/" + tenant.getTenantName() + "/ISU_Verify_Global/" + "Start_IA?Effective_as_of_Date=2021-11-01-07:00&Employee!WID=" + WID;
							 //}
						     JSONArray jArr = createStartInternationalAssignmentData(reportURL);
						     if(jArr != null && jArr.length() >0)
							 {
						    	 for(int i = 0; i<jArr.length(); i++) 
							     {
									 JSONObject objects = jArr.getJSONObject(i);
									 startIADate = objects.isNull("startIADate")?"":objects.getString("startIADate");
									 if(!startIADate.isEmpty())
									 {
										 startIADate = startIADate.substring(0, 10);
										 //startIADate = convertDate(startIADate, "yyyy-MM-dd", "dd-MM-yyyy");
									 }
									 startIAReason = objects.isNull("startIAReason")?"":objects.getString("startIAReason");
									 startIAType = objects.isNull("startIAType")?"":objects.getString("startIAType");
									 expectedIAEndDate = objects.isNull("expectedIAEndDate")?"":objects.getString("expectedIAEndDate");
									 if(!expectedIAEndDate.isEmpty())
									 {
										 expectedIAEndDate = expectedIAEndDate.substring(0, 10);
										 expectedIAEndDate = convertDate(expectedIAEndDate, "yyyy-MM-dd", "dd-MM-yyyy");
									 }
									 supOrg = objects.isNull("supOrg")?"":objects.getString("supOrg");
									 empType = objects.isNull("empType")?"":objects.getString("empType");
									 jobProfile = objects.isNull("jobProfile")?"":objects.getString("jobProfile");
									 location = objects.isNull("location")?"":objects.getString("location");
									 workSpace = objects.isNull("workSpace")?"":objects.getString("workSpace");
									 timeType = objects.isNull("timeType")?"":objects.getString("timeType");
									 workShift = objects.isNull("workShift")?"":objects.getString("workShift");
									 workHoursProfile = objects.isNull("workHoursProfile")?"":objects.getString("workHoursProfile");
									 payRateType = objects.isNull("payRateType")?"":objects.getString("payRateType");
							     }
							 }
						     else
						     {
								 startIADate = "";
								 startIAReason = "";
								 startIAType = "";
								 expectedIAEndDate = "";
								 supOrg = "";
								 empType = "";
								 jobProfile = "";
								 location = "";
								 workSpace = "";
								 timeType = "";
								 workShift = "";
								 workHoursProfile = "";
								 payRateType = "";
						     }
							 headingFromWD = "Employee_ID,Start_Date,Organization,International_Assignment_Type,Expected_Assignment_End_Date,Start_International_Assignment_Reason,Employee_Type,"
							 				+ "Job_Profile,Location,Work_Space,Position_Time_Type,Work_Shift,Work_Hours_Profile,Pay_Rate_Type";
							 
							 headerStr = workerId + "," + startIADate + "," + supOrg + "," + startIAType + "," + expectedIAEndDate + "," + startIAReason + "," + empType + "," + jobProfile + "," +
							             location + "," + workSpace + "," + timeType + "," + workShift + "," + workHoursProfile + "," + payRateType;
							 
							 if(finalStr.equals(""))
							 {
								 finalStr = headingFromWD + "\n" + headerStr;
							 }
							 else
							 {
								 finalStr = finalStr + "\n" + headerStr;
							 }
						 }						 
					 }				 
					 				     
				     System.out.println(finalStr);
				     targetContent = finalStr.toString().getBytes();
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
					 complete = true;
				 }
			 }
			 
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return headingWd;
	}

	private JSONArray createStartInternationalAssignmentData(String reportURL) throws JSONException {
		
		JSONArray jArr = new JSONArray();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		XPath xPath = XPathFactory.newInstance().newXPath();
		HttpBasicAuthentication httpBasicAuthentication = new HttpBasicAuthentication();
		String output = httpBasicAuthentication.getWithBasicAuthentication(reportURL, tenant.getTenantUser(), tenant.getTenantUserPassword());
		Reader reader1 = new StringReader(output);
		Document doc;
		try 
		{
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(new InputSource(reader1));
			doc.getDocumentElement().normalize();
			String expression = EMPLOYEE_APPLICANT_MAPPING_RESPONSE_EXPRESSION;
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) 
			{
				try 
				{
					jArr = parseNodesForStartInternationalAssignment(nodeList);
				} 
				catch (DOMException e)
				{
					e.printStackTrace();
				}
			}			
		} 
		catch (SAXException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (XPathExpressionException e) 
		{
			e.printStackTrace();
		} 
		catch (ParserConfigurationException e) 
		{
			e.printStackTrace();
		}
		return jArr;
	}

	private JSONArray parseNodesForStartInternationalAssignment(NodeList nodeList) throws DOMException, JSONException {

		JSONArray details = new JSONArray();
		for (int i = 0; i < nodeList.getLength(); i++) 
		{
		    Node nNode = nodeList.item(i);
		    if (nNode.getNodeName().equals("wd:Report_Data")) 
		    {
				if (nNode.getNodeType() == Node.ELEMENT_NODE) 
				{
					Element eElement = (Element) nNode;
					if (eElement.hasChildNodes()) 
					{
						NodeList childNodes = eElement.getChildNodes();
						for (int j = 0; j < childNodes.getLength(); j++) 
						{
							Node aChildNode = childNodes.item(j);
							JSONObject obj = new JSONObject();
							if (aChildNode.getNodeName().equals("wd:Report_Entry")) 
							{
								Element eElementEntry = (Element) aChildNode;
								if (eElement.hasChildNodes()) 
								{
									NodeList childNodesEntry = eElementEntry.getChildNodes();
									for (int k = 0; k < childNodesEntry.getLength(); k++) 
									{										
										Node aChildNodeEntry = childNodesEntry.item(k);
										if(aChildNodeEntry.getNodeName().equals("wd:CF_LRV_Start_International_Assignment_date"))
										{											
											obj.put("startIADate", aChildNodeEntry.getTextContent());											
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:CF_LRV_Start_International_Assignment_Reason"))
										{
											obj.put("startIAReason", aChildNodeEntry.getTextContent());	
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:CF_LRV_Start_International_Assignment_Type"))
										{
											obj.put("startIAType", aChildNodeEntry.getTextContent());	
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:Expected_Assignment_End_Date_-_No_Evaluation"))
										{
											obj.put("expectedIAEndDate", aChildNodeEntry.getTextContent());	
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:CF_LRV_Supervisory_Organization"))
										{
											obj.put("supOrg", aChildNodeEntry.getTextContent());	
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:CF_LRV_Employee_Type"))
										{
											obj.put("empType", aChildNodeEntry.getTextContent());	
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:CF_LRV_Job_Profile"))
										{
											obj.put("jobProfile", aChildNodeEntry.getTextContent());	
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:CF_LRV_Location"))
										{
											obj.put("location", aChildNodeEntry.getTextContent());	
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:CF_LRV_Work_Space"))
										{
											obj.put("workSpace", aChildNodeEntry.getTextContent());	
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:CF_LRV_Position_Time_Type"))
										{
											obj.put("timeType", aChildNodeEntry.getTextContent());	
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:CF_LRV_Work_Shift"))
										{
											obj.put("workShift", aChildNodeEntry.getTextContent());	
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:CF_LRV_Start_IA_Work_Hours_Profile"))
										{
											obj.put("workHoursProfile", aChildNodeEntry.getTextContent());	
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:CF_LRV_Pay_Rate_Type"))
										{
											obj.put("payRateType", aChildNodeEntry.getTextContent());	
										}
									}
								}
							}
							details.put(obj);
						}
					}
				}
		    }
		}
		return details;
	}
	
	private JSONArray createCSVFromWDEstablishment(Tenant tenant, InputStream is, SOAPConnection soapConnection, int startIndex, int endIndex, JSONArray headingWd,
			String loadCycle, String ruleName, String client) {

		headingFromWD = "";
		targetContent = null;
		try 
		{			 
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_ESTABLISHMENT_REQUEST_FILE = requestfile.getAbsolutePath();
				 
				 String outputfile = addHireIdList(GET_ESTABLISHMENT_REQUEST_FILE, columnList, ruleName, startIndex, columnList.size(), "Employee_ID");
				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Staffing";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");
				 
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Workers_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String WID = "";
					 String positionId = "";
					 String establishmentId = "";

					 String finalStr = "";
					 String headerStr = "";
					 String reportURL = "";
					 
					 Map<String,String> widMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {
						 if(j == 1)
						 {
							startIndex = 0;
							if(999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*999;
							}
						 }
						 else
						 {
							startIndex = endIndex;
							if(j*999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = (j*999);
							}
						 }
						outputfile = addHireIdList(GET_ESTABLISHMENT_REQUEST_FILE, columnList, ruleName, startIndex, endIndex, "Employee_ID");
						is = new FileInputStream(outputfile);
					    soapMessage = MessageFactory.newInstance().createMessage(null, is);
					    soapPart = soapMessage.getSOAPPart();
					    envelope = soapPart.getEnvelope();
						envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						{
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						}
						soapMessage.saveChanges();
				        out = new ByteArrayOutputStream();
				        soapMessage.writeTo(out);
				        strMsg = new String(out.toByteArray());
				        
				        soapConnectionFactory = SOAPConnectionFactory.newInstance();
						soapConnection = soapConnectionFactory.createConnection();
				        soapResponse = soapConnection.call(soapMessage, sourceUrl);
				        out = new ByteArrayOutputStream();
				        soapResponse.writeTo(out);
				        strMsg = new String(out.toByteArray(), "utf-8");
				        soapResp = XmlParserManager.parseXml(strMsg);
					        
						ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Workers_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> applicantData = responseData.getChildren("wd:Worker");
							
						 for(ReportElement reportElement : applicantData)
						 {
							 ReportElement element1 = reportElement.getChild("wd:Worker_Data");
							 String workerId = element1.getChild("wd:Worker_ID").getValue().trim();
							 System.out.println("workerId--"+ workerId);
							 
							 ReportElement widRef = reportElement.getChild("wd:Worker_Reference");
							 if(widRef != null)
							 {
								 List<ReportElement> idList = widRef.getChildren("wd:ID");
								 for(ReportElement idElement:idList)
								 {
									 widMap = idElement.getAllAttributes();
									 if(widMap.get("wd:type").equals("WID"))
									 {
										 WID = idElement.getValue().trim();
									 }
								 }
							 }
							 else
							 {
								 WID = "";
							 }
							 
							 /*if(tenant.getTenantName().equalsIgnoreCase("ibm6"))
							 {
								 reportURL = "https://" + tenant.getTenantUrl() + "customreport2/" + tenant.getTenantName() + "/ISU_Verify_Global/" + "Assign_Establishment?Effective_as_of_Date=2021-09-01-07:00&Employee!WID=" + WID;
							 }
							 else if(tenant.getTenantName().equalsIgnoreCase("ibm10"))
							 {*/
								 reportURL = "https://" + tenant.getTenantUrl() + "customreport2/" + tenant.getTenantName() + "/ISU_Verify_Global/" + "Assign_Establishment?Effective_as_of_Date=2021-09-01-07:00&Employee!WID=" + WID;
							 //}
						     JSONArray jArr = createEstablishmentData(reportURL);
						     if(jArr != null && jArr.length() >0)
							 {
						    	 for(int i = 0; i<jArr.length(); i++) 
							     {
									JSONObject objects = jArr.getJSONObject(i);
									positionId = objects.isNull("positionId")?"":objects.getString("positionId");
									establishmentId = objects.isNull("establishmentId")?"":objects.getString("establishmentId");
							     }
							 }
						     else
						     {
									positionId = "";
									establishmentId = "";
						     }
							 headingFromWD = "Employee_ID,Position_ID,Establishment_ID";
							 
							 headerStr = workerId + "," + positionId + "," + establishmentId;
							 
							 if(finalStr.equals(""))
							 {
								 finalStr = headingFromWD + "\n" + headerStr;
							 }
							 else
							 {
								 finalStr = finalStr + "\n" + headerStr;
							 }
						 }						 
					 }				 
					 				     
				     System.out.println(finalStr);
				     targetContent = finalStr.toString().getBytes();
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Employee_ID");
					 complete = true;
				 }
			 }
			 
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return headingWd;
	}
	
	private JSONArray createEstablishmentData(String reportURL) throws JSONException {

		JSONArray jArr = new JSONArray();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		XPath xPath = XPathFactory.newInstance().newXPath();
		HttpBasicAuthentication httpBasicAuthentication = new HttpBasicAuthentication();
		String output = httpBasicAuthentication.getWithBasicAuthentication(reportURL, tenant.getTenantUser(), tenant.getTenantUserPassword());
		Reader reader1 = new StringReader(output);
		Document doc;
		try 
		{
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(new InputSource(reader1));
			doc.getDocumentElement().normalize();
			String expression = EMPLOYEE_APPLICANT_MAPPING_RESPONSE_EXPRESSION;
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) 
			{
				try 
				{
					jArr = parseNodesForEstablishment(nodeList);
				} 
				catch (DOMException e)
				{
					e.printStackTrace();
				}
			}			
		} 
		catch (SAXException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (XPathExpressionException e) 
		{
			e.printStackTrace();
		} 
		catch (ParserConfigurationException e) 
		{
			e.printStackTrace();
		}
		return jArr;
	}

	private JSONArray parseNodesForEstablishment(NodeList nodeList) throws DOMException, JSONException {

		JSONArray details = new JSONArray();
		for (int i = 0; i < nodeList.getLength(); i++) 
		{
		    Node nNode = nodeList.item(i);
		    if (nNode.getNodeName().equals("wd:Report_Data")) 
		    {
				if (nNode.getNodeType() == Node.ELEMENT_NODE) 
				{
					Element eElement = (Element) nNode;
					if (eElement.hasChildNodes()) 
					{
						NodeList childNodes = eElement.getChildNodes();
						for (int j = 0; j < childNodes.getLength(); j++) 
						{
							Node aChildNode = childNodes.item(j);
							if (aChildNode.getNodeName().equals("wd:Report_Entry")) 
							{
								Element eElementEntry = (Element) aChildNode;
								if (eElement.hasChildNodes()) 
								{
									NodeList childNodesEntry = eElementEntry.getChildNodes();
									JSONObject obj = new JSONObject();
									for (int k = 0; k < childNodesEntry.getLength(); k++) 
									{										
										Node aChildNodeEntry = childNodesEntry.item(k);										
										if(aChildNodeEntry.getNodeName().equals("wd:Establishment_group"))
										{
											Element eElemEntry = (Element) aChildNodeEntry;
											if (eElementEntry.hasChildNodes()) 
											{												
												NodeList childNodesEnt = eElemEntry.getChildNodes();
												for (int l = 0; l < childNodesEnt.getLength(); l++) 
												{													
													Node nChildNodeEntry = childNodesEnt.item(l);
													if(nChildNodeEntry.getNodeName().equals("wd:referenceID"))
													{
														obj.put("establishmentId", nChildNodeEntry.getTextContent());		
													}													
												}
											}
										}
										else if(aChildNodeEntry.getNodeName().equals("wd:Position_group"))
										{
											Element eElemEntry = (Element) aChildNodeEntry;
											if (eElementEntry.hasChildNodes()) 
											{												
												NodeList childNodesEnt = eElemEntry.getChildNodes();
												for (int l = 0; l < childNodesEnt.getLength(); l++) 
												{													
													Node nChildNodeEntry = childNodesEnt.item(l);
													if(nChildNodeEntry.getNodeName().equals("wd:Reference_ID"))
													{
														obj.put("positionId", nChildNodeEntry.getTextContent());		
													}	
												}
											}
										}
										//details.put(obj);
									}
									details.put(obj);
								}
							}
						}
					}
				}
		    }
		}
		return details;
	}

	private JSONArray createCSVFromWDLocation(Tenant tenant, InputStream is, SOAPConnection soapConnection, int startIndex, int endIndex, JSONArray headingWd,
			String loadCycle, String ruleName, String client) {

		targetContent = null;
		String checkFile = null;
		try 
		{
			 GetRequest getRequest = getRequestService.getRequestByReqClient(ruleName, client);
			 if(getRequest != null)
			 {
				 byte[] requestFileContent = getRequest.getRequestXMLContent();
				 File requestfile = null;
				 try 
				 {
					 requestfile = File.createTempFile(getRequest.getRequestXMLName().substring(0, getRequest.getRequestXMLName().indexOf(".")), ".xml");
					 FileUtils.writeByteArrayToFile(requestfile, requestFileContent);
				 } 
				 catch (IOException e1) 
				 {
				     e1.printStackTrace();
				 }
				 GET_LOCATION_REQUEST_FILE = requestfile.getAbsolutePath();
				 if(errorList.isEmpty())
				 {
					 for(int i = 0; i<columnList.size();i++)
					 {
						 checkFile = addLocationToFindError(GET_LOCATION_REQUEST_FILE, columnList.get(i), ruleName, "Location_ID");
						 is = new FileInputStream(checkFile);
					     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     SOAPPart soapPart = soapMessage.getSOAPPart();
					     SOAPEnvelope envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
							  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
							  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
					     ByteArrayOutputStream out = new ByteArrayOutputStream();
					     soapMessage.writeTo(out);
					     String strMsgChk = new String(out.toByteArray());	
					     
					     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Human_Resources";
					     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
						 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
						 out = new ByteArrayOutputStream();
						 soapResponse.writeTo(out);
						 strMsgChk = new String(out.toByteArray(), "utf-8");
						 if(strMsgChk.contains("faultstring"))
						 {
							 errorList.add(columnList.get(i)) ;
						 }
						 else
						 {
							 ReportElement reportElement = XmlParserManager.parseXml(strMsgChk);				 
							 ReportElement pageResults = reportElement.getChild("env:Body")
										.getChild("wd:Get_Locations_Response")
										.getChild("wd:Response_Results");
							 
							 String result = pageResults.getChild("wd:Total_Results").getValue().trim();
							 if(result.equalsIgnoreCase("0"))
							 {
								 errorList.add(columnList.get(i));
							 }
						 }
					 }
					 columnList.removeAll(errorList);
				 }
				 String outputfile = addLocationIdList(GET_LOCATION_REQUEST_FILE, columnList, ruleName, startIndex, columnList.size(), "Location_ID");

				 is = new FileInputStream(outputfile);
			     SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, is);
			     SOAPPart soapPart = soapMessage.getSOAPPart();
			     SOAPEnvelope envelope = soapPart.getEnvelope();
				 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
				 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
				 {
					  envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
					  createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
				 }
				 soapMessage.saveChanges();
			     ByteArrayOutputStream out = new ByteArrayOutputStream();
			     soapMessage.writeTo(out);
			     String strMsg = new String(out.toByteArray());	
			     
			     String sourceUrl = SERVICE_URL_PROTOCOL + tenant.getTenantUrl() + "Human_Resources";
			     SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
				 soapConnection = soapConnectionFactory.createConnection();
				 SOAPMessage soapResponse = soapConnection.call(soapMessage, sourceUrl);
				 out = new ByteArrayOutputStream();
				 soapResponse.writeTo(out);
				 strMsg = new String(out.toByteArray(), "utf-8");
				 
				 //if(faultStr == null)
				 {
					 ReportElement soapResp = XmlParserManager.parseXml(strMsg);				 
					 ReportElement pageData = soapResp.getChild("env:Body")
								.getChild("wd:Get_Locations_Response")
								.getChild("wd:Response_Results");
					 
					 String totalNoOfPages = pageData.getChild("wd:Total_Pages").getValue().trim();
					 String totalResults = pageData.getChild("wd:Total_Results").getValue().trim();
					 int totalResult = Integer.parseInt(totalResults);
					 System.out.println("totalNoOfPages-"+totalNoOfPages);
					 System.out.println("totalResult-"+totalResult);
					 wdCount = totalResult;
					 
					 String finalStr = "";
					 String headerStr = "";
					 
					 String locationId = "";
					 String locationName = "";
					 String timeProfile = "";
					 String locale = "";
					 String currency = "";
					 String locationUsage = "";
					 String locationUsageArr = "";
					 String locationType = "";
					 String effectiveDate = "";
					 String country = "";
					 String municipality = "";
					 String countryRegion = "";
					 String postalcode = "";
					 String communicationUsageType = "";
					 String addressLine1 = "";
					 String addressLine2 = "";
					 String addressLine3 = "";
					 
					 Map<String,String> timeProfileMap = null;
					 Map<String,String> localeMap = null;
					 Map<String,String> currencyMap = null;
					 Map<String,String> locUsageMap = null;
					 Map<String,String> countryRegMap = null;
					 Map<String,String> comUsageMap = null;
					 Map<String,String> addressMap = null;
					 Map<String,String> addressLineMap = null;
					 Map<String,String> countryMap = null;
					 
					 for (int j = 1; j <= Integer.parseInt(totalNoOfPages); j++) 
					 {					 
						 if(j == 1)
						 {
							startIndex = 0;
							if(999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = j*999;
							}
						 }
						 else
						 {
							startIndex = endIndex;
							if(j*999 > totalResult)
							{
								endIndex = totalResult;
							}
							else
							{
								endIndex = (j*999);
							}
						 }
						 outputfile = addLocationIdList(GET_LOCATION_REQUEST_FILE, columnList, ruleName, startIndex, endIndex, "Location_ID");
						 is = new FileInputStream(outputfile);
					     soapMessage = MessageFactory.newInstance().createMessage(null, is);
					     soapPart = soapMessage.getSOAPPart();
					     envelope = soapPart.getEnvelope();
						 envelope.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE_URL);
						 if (tenant.getTenantName() != null && tenant.getTenantUser() != null && tenant.getTenantUserPassword() != null) 
						 {
								envelope.addNamespaceDeclaration(HEADER_SECURITY_NS_PREFIX, HEADER_SECURITY_NAMESPACE);
								createSOAPHeader(envelope.getHeader(), tenant.getTenantName(), tenant.getTenantUser(), tenant.getTenantUserPassword());
						 }
						 soapMessage.saveChanges();
				         out = new ByteArrayOutputStream();
				         soapMessage.writeTo(out);
				         strMsg = new String(out.toByteArray());
				        
				         soapConnectionFactory = SOAPConnectionFactory.newInstance();
						 soapConnection = soapConnectionFactory.createConnection();
				         soapResponse = soapConnection.call(soapMessage, sourceUrl);
				         out = new ByteArrayOutputStream();
				         soapResponse.writeTo(out);
				         strMsg = new String(out.toByteArray(), "utf-8");
				        
				         soapResp = XmlParserManager.parseXml(strMsg);
					 
						 ReportElement responseData = soapResp.getChild("env:Body")
									.getChild("wd:Get_Locations_Response")
									.getChild("wd:Response_Data");
						 
						 List<ReportElement> locations = responseData.getChildren("wd:Location");
							
						 for(ReportElement reportElement : locations)
						 {
							 ReportElement locationData = reportElement.getChild("wd:Location_Data");
							 if(locationData != null)
							 {
								 locationId = locationData.getChild("wd:Location_ID") != null?locationData.getChild("wd:Location_ID").getValue().trim():"";
								 locationName = locationData.getChild("wd:Location_Name") != null?locationData.getChild("wd:Location_Name").getValue().trim():"";
								 if(locationName.contains(","))
								 {
									 locationName = locationName.replaceAll(",", "|");
								 }
								 
								 locationType = locationName;
								 
								 ReportElement timeProfileData = locationData.getChild("wd:Time_Profile_Reference");
								 if(timeProfileData != null)
								 {
									 List<ReportElement> timeData = timeProfileData.getChildren("wd:ID");					 
									 for(ReportElement timeElement:timeData)
									 {
										 timeProfileMap = timeElement.getAllAttributes();
										 if(timeProfileMap.get("wd:type").equals("Time_Profile_ID"))
										 {
											 timeProfile = timeElement.getValue().trim();
										 }
									 }
								 }
								 
								 ReportElement localeData = locationData.getChild("wd:Locale_Reference");
								 if(localeData != null)
								 {
									 List<ReportElement> localeDataList = localeData.getChildren("wd:ID");					 
									 for(ReportElement localeElement:localeDataList)
									 {
										 localeMap = localeElement.getAllAttributes();
										 if(localeMap.get("wd:type").equals("Locale_ID"))
										 {
											 locale = localeElement.getValue().trim();
										 }
									 }
								 }
								 
								 ReportElement currencyData = locationData.getChild("wd:Default_Currency_Reference");
								 if(currencyData != null)
								 {
									 List<ReportElement> currencyDataList = currencyData.getChildren("wd:ID");					 
									 for(ReportElement currencyElement:currencyDataList)
									 {
										 currencyMap = currencyElement.getAllAttributes();
										 if(currencyMap.get("wd:type").equals("Currency_ID"))
										 {
											 currency = currencyElement.getValue().trim();
										 }
									 }
								 }
								 
								 List<ReportElement> locUsageList = locationData.getChildren("wd:Location_Usage_Reference");
								 for(ReportElement locUsageData:locUsageList)
								 {
									 locationUsageArr = "";
									 if(locUsageData != null)
									 {
										 List<ReportElement> locUsageDataList = locUsageData.getChildren("wd:ID");					 
										 for(ReportElement locUsageElement:locUsageDataList)
										 {
											 locUsageMap = locUsageElement.getAllAttributes();
											 if(locUsageMap.get("wd:type").equals("Location_Usage_ID"))
											 {
												 locationUsage = locUsageElement.getValue().trim();
												 if(locationUsageArr.equals(""))
												 {
													 locationUsageArr = locationUsage;
												 }
												 else
												 {
													 if(!locationUsage.isEmpty())
													 {
														 locationUsageArr = locationUsageArr + "~" + locationUsage;
													 }
												 }	
											 }
										 }
									 }
								 }
								 
								 ReportElement contactData = locationData.getChild("wd:Contact_Data");
								 if(contactData != null)
								 {
									 ReportElement addressData = contactData.getChild("wd:Address_Data");									 
									 if(addressData != null)
									 {
										 addressMap = addressData.getAllAttributes();
										 effectiveDate = addressMap.get("wd:Effective_Date");
										 effectiveDate = convertDate(effectiveDate, "yyyy-MM-dd", "dd-MM-yyyy");
										 List<ReportElement> addrLineData = addressData.getChildren("wd:Address_Line_Data");
										 if(addrLineData != null)
										 {
											 for(ReportElement addrLineElement:addrLineData)
											 {
												 addressLineMap = addrLineElement.getAllAttributes();
												 if(addressLineMap.get("wd:Type").equals("ADDRESS_LINE_1"))
												 {
													addressLine1 =  addrLineElement.getValue().trim();
													if(addressLine1.contains(","))
													{
														addressLine1 = addressLine1.replaceAll(",", "|");
													}
												 }
												 else if(addressLineMap.get("wd:Type").equals("ADDRESS_LINE_2"))
												 {
													addressLine2 =  addrLineElement.getValue().trim();
													if(addressLine2.contains(","))
													{
														addressLine2 = addressLine2.replaceAll(",", "|");
													}
												 }
												 else if(addressLineMap.get("wd:Type").equals("ADDRESS_LINE_3"))
												 {
													addressLine3 =  addrLineElement.getValue().trim();
													if(addressLine3.contains(","))
													{
														addressLine3 = addressLine3.replaceAll(",", "|");
													}
												 }
											 }
										 }
										 
										 municipality = addressData.getChild("wd:Municipality") != null?addressData.getChild("wd:Municipality").getValue().trim():"";
										 if(municipality.contains(","))
										 {
											 municipality = municipality.replaceAll(",", "|");
										 }
										 
										 ReportElement countryData = addressData.getChild("wd:Country_Reference");
										 if(countryData != null)
										 {
											 List<ReportElement> countryDataList = countryData.getChildren("wd:ID");					 
											 for(ReportElement countryElement:countryDataList)
											 {
												 countryMap = countryElement.getAllAttributes();
												 if(countryMap.get("wd:type").equals("ISO_3166-1_Alpha-3_Code"))
												 {
													 country = countryElement.getValue().trim();
												 }
											 }
										 }
										 
										 ReportElement countryRegionData = addressData.getChild("wd:Country_Region_Reference");
										 if(countryRegionData != null)
										 {
											 List<ReportElement> countryRegionDataList = countryRegionData.getChildren("wd:ID");					 
											 for(ReportElement countryRegionElement:countryRegionDataList)
											 {
												 countryRegMap = countryRegionElement.getAllAttributes();
												 if(countryRegMap.get("wd:type").equals("Country_Region_ID"))
												 {
													 countryRegion = countryRegionElement.getValue().trim();
												 }
											 }
										 }
										 postalcode = addressData.getChild("wd:Postal_Code") != null?addressData.getChild("wd:Postal_Code").getValue().trim():"";
										 
										 ReportElement typeRefData = addressData.getChild("wd:Usage_Data")
												 							.getChild("wd:Type_Data")
												 							.getChild("wd:Type_Reference");
										 if(typeRefData != null)
										 {
											 List<ReportElement> typeRefDataList = typeRefData.getChildren("wd:ID");					 
											 for(ReportElement typeRefElement:typeRefDataList)
											 {
												 comUsageMap = typeRefElement.getAllAttributes();
												 if(comUsageMap.get("wd:type").equals("Communication_Usage_Type_ID"))
												 {
													 communicationUsageType = typeRefElement.getValue().trim();
												 }
											 }
										 }
									 }
								 }								 								 
								 
								 headingFromWD = "Location_ID,Location_Name,Time_Profile,Locale,Location_Usage,Location_Type,Country,Municipality,Currency,Country_Region,Postal_Code,Communication_Usage,"
								 					+ "Effective_Date,AddressLine_1,AddressLine_2,AddressLine_3";
								 
								 headerStr = locationId + "," + locationName + "," + timeProfile + "," + locale + "," + locationUsageArr + "," + locationType + "," + country + "," + municipality + "," +  
										     currency + "," + countryRegion + "," + postalcode + "," + communicationUsageType + "," + effectiveDate + "," + addressLine1 + "," + addressLine2 + "," + addressLine3;
								 if(finalStr.equals(""))
								 {
									 finalStr = headingFromWD + "\n" + headerStr;
								 }
								 else
								 {
									 finalStr = finalStr + "\n" + headerStr;
								 }								 							 							 
							 }					 
						 }
					 }
					 
					 System.out.println(finalStr);
					 targetContent = finalStr.toString().getBytes();
					 
					 headingWd = selectColumnMapping(loadCycle, ruleName, "Location_ID");
					 complete = true;
				 }
			 }
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (SOAPException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return headingWd;
	}
	
	private String addLocationToFindError(String xmlFile, String columnVal, String ruleName, String idVal) {
		
		File updatedRequestfile = null;
		try  
		{  
			File file = new File(xmlFile);     
			FileReader fr = new FileReader(file);   
			BufferedReader br = new BufferedReader(fr);   
			StringBuffer sb = new StringBuffer();     
			String line; 
			boolean executed = true;
			while((line=br.readLine())!=null)  
			{  
				sb.append(line);  
				sb.append("\n");
				if(line.contains("bsvc:Get_Locations_Request") && executed)
				{
					executed = false;
					sb.append(" <bsvc:Request_References>"); 
					sb.append("\n");
						sb.append("  <bsvc:Location_Reference bsvc:Descriptor=" + "\"" + idVal + "\"" + ">");
						sb.append("\n");
						sb.append("   <bsvc:ID bsvc:type=" + "\"" + idVal + "\"" + ">" + columnVal + "</bsvc:ID>");
						sb.append("\n");
						sb.append("  </bsvc:Location_Reference>");
						sb.append("\n");					
					sb.append(" </bsvc:Request_References>");
					sb.append("\n");
				}
			}  
			fr.close();
			br.close();
			
			updatedRequestfile = File.createTempFile(ruleName + "_" + columnVal , ".xml");
			PrintWriter writer = new PrintWriter(updatedRequestfile);
		    writer.write(sb.toString());
			writer.flush();
			writer.close();
		}  
		catch(IOException e)  
		{  
			e.printStackTrace();  
		}
		return updatedRequestfile.getAbsolutePath(); 
	}
	
	private String addLocationIdList(String xmlFile, List<String> columnList, String ruleName, int startIndex, int endIndex, String referenceId) {
		
		File updatedRequestfile = null;
		try  
		{  
			File file = new File(xmlFile);     
			FileReader fr = new FileReader(file);   
			BufferedReader br = new BufferedReader(fr);   
			StringBuffer sb = new StringBuffer();     
			String line; 
			boolean executed = true;
			while((line=br.readLine())!=null)  
			{  
				sb.append(line);  
				sb.append("\n");
				if(line.contains("bsvc:Get_Locations_Request") && executed)
				{
					executed = false;
					sb.append(" <bsvc:Request_References>"); 
					sb.append("\n");
					for(int i = startIndex;i<endIndex;i++)
					{
						sb.append("  <bsvc:Location_Reference bsvc:Descriptor=" + "\"" + referenceId + "\"" + ">");
						sb.append("\n");
						sb.append("   <bsvc:ID bsvc:type=" + "\"" + referenceId + "\"" + ">" + columnList.get(i) + "</bsvc:ID>");
						sb.append("\n");
						sb.append("  </bsvc:Location_Reference>");
						sb.append("\n");
					}
					sb.append(" </bsvc:Request_References>");
					sb.append("\n");
				}
			}  
			fr.close();
			br.close();
			
			updatedRequestfile = File.createTempFile(ruleName + "_" + startIndex , ".xml");
			PrintWriter writer = new PrintWriter(updatedRequestfile);
		    writer.write(sb.toString());
			writer.flush();
			writer.close();
		}  
		catch(IOException e)  
		{  
			e.printStackTrace();  
		}
		return updatedRequestfile.getAbsolutePath(); 
	}

	private String convertDate(String strDate, String format, String newFormat) {

		SimpleDateFormat inputFormatter=new SimpleDateFormat(format);  
	    Date da = null;
		try 
		{
			da = (Date)inputFormatter.parse(strDate);
		} 
		catch (ParseException e) 
		{
			e.printStackTrace();
		}

	    DateFormat outputFormatter = new SimpleDateFormat(newFormat);
	    String strDateTime = outputFormatter.format(da);
		return strDateTime;
	}
	
}
