package com.ibm.workday.automation.common;

public interface CommonConstants {
	String NAMESPACE_URL = "urn:com.workday/bsvc";
	String NAMESPACE_PREFIX = "wd";
	String SOAP_ENV_NAMESPACE_URL = "http://schemas.xmlsoap.org/soap/envelope/";
	String SOAP_ENV_NAMESPACE_PREFIX = "SOAP-ENV";
	String STATUS_SUCCESS = "Success";
	String STATUS_FAILUE = "Failure";
	String STATUS_TIMEOUT = "Timeout";
	String ERROR_FILE = "Error_File";
	String BATCH_NAME_PREFIX = "Batch-";
	String EXCLUSION_REFERENCE_ID_PROP = "Employee_ID;Organization_Reference_ID;Job_Profile_ID;Position_ID;Applicant_ID;Company_Reference_ID;Cost_Center_Reference_ID;Contingent_Worker_Type_ID;ADDRESS_LINE_1;ADDRESS_LINE_2;ADDRESS_LINE_3;ADDRESS_LINE_4;ADDRESS_LINE_5;ADDRESS_LINE_6;ADDRESS_LINE_7;ADDRESS_LINE_8;ADDRESS_LINE_9;";
	String DELIMITER_SEMI_COLON = ";";
	String REFERENCE_ID_TYPE = "type";
//	String APPLICATION_RULES_LIST_PROP = "application.rules.list";
	String GENERAL_DATE_FORMAT = "MM/dd/yyyy hh:mm:ss";
	String PROJECT_CHECKLIST_TEMPLATE = "Project_Checklist_template";
	String CLIENT_WORKBOOK = "Client Workbook/Templates";
	String APPLICATION_TYPE = "Application";
	String XML_TYPE = "xml";
	String WSDL_SUFFIX = ".wsdl";
	String XSD_SUFFIX = ".xsd";
	String XML_SUFFIX = ".xml";
	String SOAP_FAULT_XPATH = "SOAP-ENV:Fault/faultstring";
//	String TENANT_SERVICE_URL_MAPPING = "application.tenant.service.url.mapping";
//	String TENANT_SERVICE_URL_SUFFIX = "application.tenant.service.url.suffix";
	String SEMI_COLON_DELIMITER = ";";
	String COLON_DELIMITER = ":";
	String SERVICE_URL_PROTOCOL = "https://";
	String SUPER_USER_CLIENT = "All";
	
	String SESSION_USER_ID = "userId";
	String SESSION_WWS_STOPREQUEST = "stopSWExecution";
	String REFERENCE_ID_WS_REQUEST = "REFERENCE_ID_WS_REQUEST";
	
	/*String REFERENCE_ID_WS_REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
			"<wd:Get_References_Request xmlns:wd=\"urn:com.workday/bsvc\"\r\n" + 
			"	wd:version=\"v26.0\">\r\n" + 
			"	<wd:Request_Criteria>\r\n" + 
			"		<wd:Reference_ID_Type>Event_Classification_Subcategory_ID</wd:Reference_ID_Type>\r\n" + 
			"	</wd:Request_Criteria>\r\n" + 
			"	<wd:Response_Filter>\r\n" + 
			"		<wd:Page>1</wd:Page>\r\n" + 
			"		<wd:Count>999</wd:Count>\r\n" + 
			"	</wd:Response_Filter>\r\n" + 
			"</wd:Get_References_Request>";*/
}
