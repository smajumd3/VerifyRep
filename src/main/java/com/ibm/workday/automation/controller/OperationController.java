package com.ibm.workday.automation.controller;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.commons.json.JSONArray;
import org.apache.commons.json.JSONException;
import org.apache.commons.json.JSONObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.workday.automation.common.CommonConstants;
import com.ibm.workday.automation.dao.OperationDirDao;
import com.ibm.workday.automation.model.Application;
import com.ibm.workday.automation.model.BuildRuleFile;
import com.ibm.workday.automation.model.Operation;
import com.ibm.workday.automation.model.Page;
import com.ibm.workday.automation.model.Section;
import com.ibm.workday.automation.model.User;
import com.ibm.workday.automation.operation.ApplicationUtil;
import com.ibm.workday.automation.operation.DataAttribute;
import com.ibm.workday.automation.operation.DataElement;
import com.ibm.workday.automation.operation.LoadDataRules;
import com.ibm.workday.automation.operation.OperationValue;
import com.ibm.workday.automation.operation.WriteDataRules;
import com.ibm.workday.automation.service.AppVersionService;
import com.ibm.workday.automation.service.ApplicationService;
import com.ibm.workday.automation.service.BuildRuleFileService;
import com.ibm.workday.automation.service.FileService;
import com.ibm.workday.automation.service.OperationService;
import com.ibm.workday.automation.service.PageService;
import com.ibm.workday.automation.service.SectionService;
import com.ibm.workday.automation.service.UserService;

@RestController
public class OperationController implements CommonConstants {
	
	@Autowired
	OperationService operationService;
	
	@Autowired
	ApplicationService applicationService;
	
	@Autowired
	AppVersionService appVersionService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	ApplicationUtil applicationUtil;
	
	@Autowired
	PageService pageService;
	
	@Autowired
	SectionService sectionService;	
	
	@Autowired
	FileService fileService;
	
	@Autowired
	BuildRuleFileService buildRuleFileService;
	
	@Autowired
	OperationDirDao opDirDao;
	
	@Autowired
	OperationValue operationValue;
	
	@Autowired
	LoadDataRules loadDataRules;
	
    @Autowired
	private Environment env;
	
	Application application;
	
	Operation operation;
	
	DataElement dataTree;
	
	DataElement actElement;
	
	WriteDataRules writeDataRules;
	
	JSONArray jsonArr = new JSONArray();
	
	Map<String, JSONArray> attrMap = new HashMap<>();
	
	AtomicInteger automicInt = new AtomicInteger(0);
	
	Map<String, List<String>> csvFileMap = new HashMap<>();
	
	Map<String, List<String>> csvRequiredMap = new HashMap<>();
	
	Map<String, List<String>> csvSampleDataMap = new HashMap<>();
	
	Map<String, List<String>> csvColDescMap = new HashMap<>();
	
	List<String> csvValueList = null;
	
	List<File> fileList = new ArrayList<>();
	
	List<String> requiredList = new ArrayList<>();
	
	List<String> sampleDataList = new ArrayList<>();
	
	List<String> colDescList = new ArrayList<>();
	
	String currentCSVfileName = null;
	
	String oldCSVFileName = "";
	
	String uniqueIdVal = null;
	
	String requiredVal = null;
	
	String sampleDataVal = null;
	
	String colDescVal = null;
	
	String firstSampleVal = null;
	
	String firstColDescVal = null;
	
	private static final String WS_NAMESPACE = "urn:com.workday/bsvc";
	
	private static final String WS_PREFIX = "wd";
	
	private static final String RULES_NAMESPACE = "com.ibm.conversion.tool/rules";
	
	private static final String RULES_PREFIX = "ct";
	
	private static final String ATTRIBUTE = "Attribute";
	
	private static final String ATTRIBUTE_MAPPING_COLUMN_NAME = "mappingColumnName";
	
	private static final String ATTRIBUTE_CONSTANT_VALUE = "constantValue";
	
	@RequestMapping(value = "/getOperationsForApplication/{applicationId}", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<Operation> getOperationsForApplication(@PathVariable("applicationId") Long applicationId) {
		application = applicationService.getApplication(applicationId);
		return application.getOperations();
	}
	
	@RequestMapping(value = "/getAllOperations", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<Operation> getOperationList() {
		return operationService.getOperationList();
	}
	
	@RequestMapping(value = "/getAllOperationsByUser", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<Operation> getAllOperationsUser(HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		return operationService.getOperationListByUser(userId);
	}
	
	@RequestMapping(value = "/getOperationValue/{operationId}", method = RequestMethod.GET, headers = "Accept=application/json")
	public OperationValue getAllOperationsValueByUser(@PathVariable("operationId") Long operationId) {
		operation = operationService.getOperation(operationId);
		operationValue.setApplicationName(operation.getApplication().getApplicationName());
		operationValue.setApplicationVersion(operation.getApplication().getVersion());
		operationValue.setOperationId(operation.getOperationId());
		operationValue.setOperationName(operation.getOperationName());
		operationValue.setResponsePath(operation.getResponsePath());
		operationValue.setRuleName(operation.getRuleName());
		return operationValue;
	}
	
	@RequestMapping(value = "/getOperation/{operationId}", method = RequestMethod.GET, headers = "Accept=application/json")
	public Operation getOperation(@PathVariable("operationId") Long operationId) {
		return operationService.getOperation(operationId);
	}
	
	@RequestMapping(value = "/addOperation", method = RequestMethod.POST, headers = "Accept=application/json")
	public void addOperation(@RequestBody Operation operation, HttpSession httpSession) {
		operation.setApplication(application);
		
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		operation.setUserId(userId);
		
		operationService.addOperation(operation);
	}
	
	@RequestMapping(value = "/upDateData/{pageId}", method = RequestMethod.POST, headers = "Accept=application/json")
	public void upDateData(HttpSession httpSession, @PathVariable("pageId") Long pageId) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);

		String version = appVersionService.getVersionByClient(user.getClient()).getVersion();
		
		if(version == null || version.isEmpty()) {
			version = "35.0";		// Suman
		}
		
		Page page = pageService.getPage(pageId);
		List<Section> sections = page.getSections();
		
		for(Section section : sections) {
			if(section.getExecute()) {
				String operationName = section.getOperationName();
//				String applicationName = opDirDao.getApplicationForOperarion(operationName);
				String applicationName = env.getProperty("_" + operationName);
				Application application = applicationService.getApplication(applicationName, version, userId);
				if(application == null) {
					application = new Application();
					application.setApplicationName(applicationName);
					application.setUserId(userId);
					application.setVersion(version);
					byte[] wsdlFileData = applicationUtil.generateWsdlData(application);
					application.setWsdlFileData(wsdlFileData);
				}
				
				Operation operation = operationService.getOperation(operationName, userId);
				if(operation == null || !operation.getApplication().getVersion().equals(version)) {
					operation = new Operation();
					operation.setOperationName(operationName);
					operation.setUserId(userId);
					operation.setApplication(application);
					String responsePath = env.getProperty(operationName);
					operation.setResponsePath(responsePath);
					operation.setRuleName(operationName);
					List<BuildRuleFile> files = buildRuleFileService.getBuildFilesByFileNameClient(operationName, user.getClient());		// Suman
					
					if(files != null && !files.isEmpty()) {
						for(BuildRuleFile file : files) {
							User checkAdmin = userService.getUser(file.getUserId());
							if(checkAdmin.getAdmin()) {
								operation.setRuleFileData(file.getFileData());
								break;
							}
						}
					}
					operationService.addOperation(operation);
				}
			}
		}
	}
	
	@RequestMapping(value = "/addSection/{pageId}/{areaName}/{taskName}/{operationName}", method = RequestMethod.POST, headers = "Accept=application/json")
	public void addSection(HttpSession httpSession, @PathVariable("pageId") Long pageId, @PathVariable("areaName") String areaName,
			               @PathVariable("taskName") String taskName, @PathVariable("operationName") String operationName ) {
		Page page = pageService.getPage(pageId);
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		
		Section section = new Section();
		section.setAreaName(areaName.trim());
		section.setAssignedTo(user.getUserEmail());
		section.setExecute(false);
//		section.setIndex(page.getSections().size() + 4);		// Suman
		section.setIndex(page.getSections().size() + 1);
		section.setIsDownload(false);
		section.setOperationName(operationName.trim());
		section.setTaskName(taskName.trim());
		section.setStatus(0);
		section.setPage(page);
		
		sectionService.addSection(section);
	}
	
	@RequestMapping(value = "/addOperation", method = RequestMethod.PUT, headers = "Accept=application/json")
	public void updateOperation(@RequestBody Operation operation) {
		operationService.updateOperation(operation); 
	}
	
	@RequestMapping(value = "/deleteOperation/{operationId}", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public void deleteOperation(@PathVariable("operationId") Long operationId) {
		Operation operation = operationService.getOperation(operationId);
		operation.setApplication(new Application());
		operationService.deleteOperation(operationId);
	}
	
	@RequestMapping(value = "/loadRootXmlData/{operationId}", method = RequestMethod.POST)		// Suman
	public void loadRootXmlData(@PathVariable("operationId") Long operationId, HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		Operation operation = operationService.getOperation(operationId);
		
		List<BuildRuleFile> files = buildRuleFileService.getBuildFilesByFileNameClient(operation.getOperationName(), user.getClient());
		
		if(files != null && !files.isEmpty()) {
			for(BuildRuleFile file : files) {
				User checkAdmin = userService.getUser(file.getUserId());
				if(checkAdmin.getAdmin()) {
					operation.setRuleFileData(file.getFileData());
					break;
				}
			}
			operationService.updateOperation(operation);
		}
	}
	
	@RequestMapping(value = "/uploadRuleFileData", method = RequestMethod.POST)
	public void uploadRuleFileData(@RequestParam("file") MultipartFile file) {
		try {
			operation.setRuleFileData(file.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		operationService.updateOperation(operation);
	}
	
	@RequestMapping(value = "/getRootDataElement", method = RequestMethod.GET, headers = "Accept=application/json")
	public String getRootDataElement() {
		
		DataElement dataRoot = loadDataRules.getDataRules(operation, false);
		if(dataRoot != null) 
		{
			dataTree = new DataElement();
			dataTree.addChild(dataRoot);
		}
		return STATUS_SUCCESS;
	}
	
	@RequestMapping(value = "/saveBuildRulesData/{operationId}/{itemId}/{rulesArray}/{removeItem}/{addId}/{delId}/{addItem}/{currentTreeNode}/{editId}", method = RequestMethod.POST, headers = "Accept=application/json")
	public void saveRuleData(@PathVariable("operationId") String operationId, @PathVariable("itemId") Long itemId, @PathVariable("rulesArray") String rulesArray, 
			@PathVariable("removeItem") String removeItem, @PathVariable("addId") Long addId, @PathVariable("delId") Long delId, @PathVariable("addItem") String addItem,
			@PathVariable("currentTreeNode") String currentTreeNode, @PathVariable("editId") Long editId, HttpSession httpSession) {
				
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		String[] ruleBuildArr = null;
		System.out.println("rulesArray>>" + rulesArray);
		
		if("a".equals(addItem) && currentTreeNode != null)
		{
			DataElement actElement = findExactDataElementForAddRemove(dataTree.getChildren().get(0), addId, operationId, userId);
			if (actElement != null) 
			{
				DataElement childElement = new DataElement();
				childElement.setName(currentTreeNode);
				actElement.addChild(childElement);
				childElement.setId(automicInt.incrementAndGet());
				childElement.setParent(actElement);
				updateAttrRules(operationId, userId);
				itemId = childElement.getId();
				if(!rulesArray.equals("null") && rulesArray != null && rulesArray.length() >0)
				{
					rulesArray = rulesArray.replace("[", "");
					rulesArray = rulesArray.replace("]", "");
					rulesArray = rulesArray.replaceAll("\"", "");
					System.out.println(rulesArray);
					ruleBuildArr = rulesArray.split("=");
					findExactDataElement(dataTree.getChildren().get(0), itemId, ruleBuildArr, operationId, userId);
				}
			}
		}
		else if(!"a".equals(addItem) && !("null".equals(currentTreeNode)))
		{
			DataElement actElement = findExactDataElementForAddRemove(dataTree.getChildren().get(0), editId, operationId, userId);
			actElement.setName(currentTreeNode);
			updateAttrRules(operationId, userId);
			
			if(!rulesArray.equals("null") && rulesArray != null && rulesArray.length() >0)
			{
				rulesArray = rulesArray.replace("[", "");
				rulesArray = rulesArray.replace("]", "");
				rulesArray = rulesArray.replaceAll("\"", "");
				System.out.println(rulesArray);
				ruleBuildArr = rulesArray.split("=");
				findExactDataElement(dataTree.getChildren().get(0), editId, ruleBuildArr, operationId, userId);
			}
		}
		else if(!("d".equals(removeItem)))
		{
			if(!rulesArray.equals("null") && rulesArray != null && rulesArray.length() >0)
			{
				rulesArray = rulesArray.replace("[", "");
				rulesArray = rulesArray.replace("]", "");
				rulesArray = rulesArray.replaceAll("\"", "");
				System.out.println(rulesArray);
				ruleBuildArr = rulesArray.split("=");
				findExactDataElement(dataTree.getChildren().get(0), itemId, ruleBuildArr, operationId, userId);
			}
			else
			{
				findExactDataElement(dataTree.getChildren().get(0), itemId, ruleBuildArr, operationId, userId);
			}
		}
		else
		{
			actElement = null;
			DataElement actElement = findExactDataElementForAddRemove(dataTree.getChildren().get(0), delId, operationId, userId);
			removeElement(actElement, operationId, userId);
		}
	}
	
	private void updateAttrRules(String operationId, Long userId) {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if(updateResult(dataTree.getChildren().get(0), baos))
		{
			operation.setRuleFileData(baos.toByteArray());			
			operationService.updateOperation(operation);
		}
	}
	
	private boolean updateResult(DataElement dataElement, OutputStream os) {
		boolean result = false;
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		docFactory.setNamespaceAware(true);
		try 
		{
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			Document document = docBuilder.newDocument();
			Element rootElement = document.createElementNS(WS_NAMESPACE, dataElement.getName());
			rootElement.setPrefix(WS_PREFIX);
			updateAttributes(dataElement.getAttributes(), document, rootElement);
			createElement(dataElement.getChildren(), document, rootElement);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource dom = new DOMSource(rootElement);
			StreamResult streamResult = new StreamResult(os);
			transformer.transform(dom, streamResult);
			result = true;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return result;
	}
	
	private void updateAttributes(final List<DataAttribute> dataAttributes, final Document document, final Element parentElement) {
		for (DataAttribute dataAttribute : dataAttributes) {
			String namespace = "";
			String prefix = "";
			boolean isAttribute = false;
			if (dataAttribute.getType().equalsIgnoreCase(ATTRIBUTE)) 
			{
				namespace = WS_NAMESPACE;
				prefix = WS_PREFIX;
			} 
			else 
			{
				namespace = RULES_NAMESPACE;
				prefix = RULES_PREFIX;
			}

			if (dataAttribute.getName().equalsIgnoreCase(ATTRIBUTE_MAPPING_COLUMN_NAME) || dataAttribute.getName().equalsIgnoreCase(ATTRIBUTE_CONSTANT_VALUE)) 
			{
				isAttribute = false;
			} 
			else 
			{
				isAttribute = true;
			}
			if (isAttribute) 
			{
				Attr attr = document.createAttributeNS(namespace, dataAttribute.getName());
				attr.setPrefix(prefix);
				attr.setValue(dataAttribute.getValue());
				parentElement.setAttributeNode(attr);
			} 
			else 
			{
				String value = dataAttribute.getValue();
				if (ATTRIBUTE_MAPPING_COLUMN_NAME.equalsIgnoreCase(dataAttribute.getName())) 
				{
					if (!dataAttribute.getValue().startsWith("$")) 
					{
						value = "$" + value;
					}
				}
				parentElement.appendChild(document.createTextNode(value));
			}

		}
	}
	
	private Element createElement(List<DataElement> elements, Document document, Element parentElement) {
		Element anElement = null;

		for (DataElement dataElement : elements) {

			Element newElement = document.createElementNS(WS_NAMESPACE, dataElement.getName());
			newElement.setPrefix(WS_PREFIX);

			if (!dataElement.getAttributes().isEmpty()) 
			{
				updateAttributes(dataElement.getAttributes(), document, newElement);
			}
			
			if (!dataElement.getChildren().isEmpty()) 
			{
				createElement(dataElement.getChildren(), document, newElement);
			}
			parentElement.appendChild(newElement);
		}
		return anElement;
	}

	private DataElement findExactDataElementForAddRemove(DataElement element, long itemId, String operationId, long userId) {
		 
		List<DataElement> eleList = null;
		if(element != null)
		{
			System.out.println(element.getName() +"-" + element.getId());
			eleList = element.getChildren();
			if(eleList.size() > 0)
			{
				if(element.getId() == itemId)
				{
					System.out.println("element-"+element.getId());
					actElement = element;
				}
				for(DataElement dataElement : eleList)
				{
					findExactDataElementForAddRemove(dataElement, itemId, operationId, userId);
				}
			}
			else
			{
				if(element.getId() == itemId)
				{
					System.out.println("element-"+element.getId());
					actElement = element;
				}
			}
		}
		return actElement;
		
	}
	
	private void findExactDataElement(DataElement element, long itemId, String[] ruleBuildArr, String operationId, long userId) {
		
		List<DataElement> eleList = null;
		if(element != null)
		{
			System.out.println(element.getName() +"-" + element.getId());
			eleList = element.getChildren();
			if(eleList.size() > 0)
			{
				if(element.getId() == itemId)
				{
					System.out.println("element-"+element.getId());
					addRemoveAttrRules(element, ruleBuildArr, operationId, userId);
					return;
				}
				for(DataElement dataElement : eleList)
				{
					findExactDataElement(dataElement, itemId, ruleBuildArr, operationId, userId);
				}
			}
			else
			{
				if(element.getId() == itemId)
				{
					System.out.println("element-"+element.getId());
					addRemoveAttrRules(element, ruleBuildArr, operationId, userId);
					return;
				}
			}
		}		
	}
	
	private void addRemoveAttrRules(DataElement element, String[] ruleBuildArr, String operationId, long userId) {
		
		element.removeAttribute(element.getAttributes());
		
		if(ruleBuildArr != null)
		{
			String finalStr[] = null;
			for(int i = 0; i< ruleBuildArr.length; i++)
			{
				String finalStrArr = ruleBuildArr[i];
				if(i == 0)
				{
					if(finalStrArr.length() > 0)
					{
						finalStr = finalStrArr.split(",");
						try 
						{
							DataAttribute da = new DataAttribute(finalStr[1], finalStr[2], finalStr[0], null);
							element.addAttribute(da);
						} 
						catch (Exception e) 
						{
							e.printStackTrace();
						}
						System.out.println("Val"+ i + "-" + finalStr[0] + ":" + finalStr[1] + ":"+ finalStr[2]);
					}
				}
				else
				{
					if(finalStrArr.length() > 0)
					{
						finalStr = finalStrArr.split(",");
						try 
						{
							DataAttribute da = new DataAttribute(finalStr[2], finalStr[3], finalStr[1], null);
							element.addAttribute(da);
						} 
						catch (Exception e) 
						{
							e.printStackTrace();
						}
						System.out.println("Val" + i + "-" + finalStr[1] + ":" + finalStr[2] + ":"+ finalStr[3]);
					}
				}
			}
		}
		updateAttrRules(operationId, userId);
	}
	
	private void removeElement(DataElement element, String operationId, long userId) {
		int index = 0;
		if (element.getParent() != null) 
		{
			for (DataElement de : element.getParent().getChildren()) {
				if (element.getId() == de.getId()) 
				{
					break;
				}
				index++;
			}
			if (index < element.getParent().getChildren().size()) 
			{
				element.getParent().getChildren().remove(index);
			}
		}
		updateAttrRules(operationId, userId);
	}

	
	@RequestMapping(value = "/getRuleDetailsInTree", method = RequestMethod.GET, headers = "Accept=application/json")
	public JSONArray getRuleDetailsInTree() {
		
		JSONArray details = new JSONArray();
		DataElement dataRoot = loadDataRules.getDataRules(operation, true);
		dataTree = new DataElement();
		dataTree.addChild(dataRoot);
		details = findAllElements(dataRoot, 0);
		return details;
	}

	private JSONArray findAllElements(DataElement dataRoot, int i) {
		
		if(i == 0)
		{
			jsonArr = new JSONArray();
			attrMap.clear();
		}
		
		List<DataElement> eleList = null;
		String parent = null;
		String pid = null;
		if(dataRoot != null)
		{
			//System.out.println(dataRoot.getName());
			eleList = dataRoot.getChildren();
			if(eleList.size() > 0)
			{
				JSONObject jsonObj = new JSONObject();
				try 
				{
					jsonObj.put("id", String.valueOf(dataRoot.getId()));
					jsonObj.put("text", dataRoot.getName());
				} 
				catch (JSONException e) 
				{
					e.printStackTrace();
				}
				
				if(dataRoot.getParent() != null)
				{
					parent = dataRoot.getParent().getName();
					pid = findParentId(parent,jsonArr);
				}
				else
				{
					pid = "-1";
				}
				try 
				{
					jsonObj.put("parentid", pid);
					jsonArr.put(jsonObj);
					getAllAttributes(dataRoot, (String)jsonObj.get("id"));
				} 
				catch (JSONException e) 
				{
					e.printStackTrace();
				}

				for(DataElement dataElement : eleList)
				{
					findAllElements(dataElement, 9999);
				}
			}
			else
			{
				JSONObject jsonObj = new JSONObject();
				try 
				{
					jsonObj.put("id", String.valueOf(dataRoot.getId()));
					jsonObj.put("text", dataRoot.getName());
				} 
				catch (JSONException e) 
				{
					e.printStackTrace();
				}
				
				if(dataRoot.getParent() != null)
				{
					parent = dataRoot.getParent().getName();
					pid = findParentId(parent,jsonArr);
				}
				try 
				{
					jsonObj.put("parentid", pid);
					jsonArr.put(jsonObj);
					getAllAttributes(dataRoot, (String)jsonObj.get("id"));
				} 
				catch (JSONException e) 
				{
					e.printStackTrace();
				}

			}
		}
		return jsonArr;
	}
	
	private void getAllAttributes(DataElement rootElement, String elemetId) {
		
		List<DataAttribute> rootAttrList = null;
		JSONArray jsonAttrRuleArr = new JSONArray();
		
		rootAttrList = rootElement.getAttributes();
		for(DataAttribute dataAttributeRoot : rootAttrList)
		{
			//System.out.println(dataAttributeRoot.getName() + "=" + dataAttributeRoot.getValue() + "( " +dataAttributeRoot.getType() + " )");
			JSONObject attrRuleList = new JSONObject();
			try 
			{
				attrRuleList.put("name", dataAttributeRoot.getName());
				attrRuleList.put("value", dataAttributeRoot.getValue());
				attrRuleList.put("type", dataAttributeRoot.getType());
				jsonAttrRuleArr.put(attrRuleList);
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
			
			attrMap.put(elemetId, jsonAttrRuleArr);
		}
	}

	private String findParentId(String parent, JSONArray jArr) {

		String pid = null;
		for(int i = 0; i < jArr.length(); i++)
		{
			JSONObject jObj = null;
			try 
			{
				jObj = jArr.getJSONObject(i);
				if(parent.equals(jObj.get("text")))
				{
					pid = (String) jObj.get("id");
				}
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
		}
		return pid;
	}
	
	@RequestMapping(value = "/retriveAttrAndRule/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
	public JSONArray retriveAttrAndRule(@PathVariable("id") String id) {
		
		JSONArray attrRuleArr = attrMap.get(id);
		return attrRuleArr;
	}
	
	@RequestMapping(value = "/downloadXMLFiles/{operationName}", method = RequestMethod.GET, headers = "Accept=application/json")
	public void downloadXMLFiles(@PathVariable("operationName") String operationName, HttpSession httpSession, HttpServletResponse response) {
		
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		operation = operationService.getOperation(operationName, userId);
		
		String fileName = operationName + "_Modified";
		response.setHeader("Content-Disposition", "attachment;filename=" + fileName + "");
		response.setContentType("text/xml");
		try (ByteArrayInputStream bis = new ByteArrayInputStream(operation.getRuleFileData())) {
			IOUtils.copy(bis, response.getOutputStream());
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/downloadCSVFiles/{operationName}", method = RequestMethod.GET, headers = "Accept=application/json")
	public void downloadCSVFiles(@PathVariable("operationName") String operationName, HttpSession httpSession, HttpServletResponse response) {
		
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		operation = operationService.getOperation(operationName, userId);		
				
		DataElement dataElement = loadDataRules.getDataRules(operation, true);
		csvFileMap.clear();
		csvValueList = null;
		currentCSVfileName = null;
		oldCSVFileName = "";
		uniqueIdVal = null;
		createCSVFileData(dataElement);
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
		createCSVTemplates(response);
		if(fileList.size() > 0)
		{
			downloadMultipleFile(response, fileList, operationName);
		}
	}
	
	private void createCSVFileData(DataElement rootElement) {
		
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
				/*if("type".equalsIgnoreCase(dataAttributeRoot.getName()))
				{
					if(dataAttributeRoot.getValue().equalsIgnoreCase("$Worker Type"))
					{
						if(csvValueList == null)
						{
							csvValueList = new ArrayList<>();
						}
						csvValueList.add("Worker Type");
					}
				}*/
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
					List<DataAttribute> attrList = dataElement.getAttributes();
					for(int a=0;a<attrList.size();a++)
					{
						if(attrList.get(a).getValue().startsWith("$") && attrList.get(a).getName().equalsIgnoreCase("type"))
						{
							if(!csvValueList.contains(attrList.get(a).getValue().substring(1, attrList.get(a).getValue().length())))
							{
								csvValueList.add(attrList.get(a).getValue().substring(1, attrList.get(a).getValue().length()));
							}
						}
					}
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
	}
	
	private void createCSVTemplates(HttpServletResponse response) {
		
		fileList.clear();
		Iterator<Map.Entry<String, List<String>>> itr = csvFileMap.entrySet().iterator();
		while(itr.hasNext()) 
        {
			Map.Entry<String, List<String>> entry = itr.next();
        	String csvFile = entry.getKey().substring(0,entry.getKey().indexOf("."));
        	System.out.println(csvFile);
        	ArrayList<String> valueList = (ArrayList<String>) entry.getValue();
        	
        	File file = null;
			try 
			{
				file = File.createTempFile(csvFile, ".csv");
			} 
			catch (IOException e1) 
			{
				e1.printStackTrace();
			}
        	  		
    		FileWriter csvWriter = null;
    		FileInputStream fis = null;
			try 
			{
				csvWriter = new FileWriter(file);
				for(int i = 0; i < valueList.size(); i++)
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
				csvWriter.flush();
				
				if(csvFileMap.size() == 1)
				{
					response.setHeader("Content-Disposition", "attachment;filename=" + csvFile + ".csv" + "");
					response.setContentType("text/csv");
					try
					{
						fis = new FileInputStream(file);
						IOUtils.copy(fis, response.getOutputStream());
					}
					catch (IOException e) 
					{
						e.printStackTrace();
					}
					csvWriter.close();
					fis.close();
				}
				else
				{
					csvWriter.close();
					fileList.add(file);
				}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			} 
        }         
	}
	
	private void downloadMultipleFile(HttpServletResponse response, List<File> fileList, String operationName) {
        
        FileOutputStream fos = null;
        ZipOutputStream zipOut = null;
        FileInputStream fis = null;        
        File zipFile = null;

        try 
        {
        	zipFile = File.createTempFile(operationName, ".zip");
        	fos = new FileOutputStream(zipFile);
            zipOut = new ZipOutputStream(new BufferedOutputStream(fos));
			for (File file : fileList)
            {
	            fis = new FileInputStream(file);
	            ZipEntry ze = new ZipEntry(file.getName());
                zipOut.putNextEntry(ze);
                byte[] tmp = new byte[4*1024];
                int size = 0;
                while((size = fis.read(tmp)) != -1){
                    zipOut.write(tmp, 0, size);
                }
                zipOut.flush();
                fis.close();
            }
			zipOut.close();
            System.out.println("Done... Zipped the files...");
            
            response.setContentType("APPLICATION/OCTET-STREAM");
            response.setHeader("Content-Disposition","attachment; filename=\"" + operationName + ".zip" + "\"");

            OutputStream out = response.getOutputStream();
            FileInputStream in = new FileInputStream(zipFile);
            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) > 0){
               out.write(buffer, 0, length);
            }
            in.close();
            out.flush();    
		} 
        catch (IOException e) 
        {
			e.printStackTrace();
		}           
	}
	
	@RequestMapping(value = "/downloadWorksheet/{operationName}", method = RequestMethod.GET, headers = "Accept=application/json")
	public void downloadWorksheet(@PathVariable("operationName") String operationName, HttpSession httpSession, HttpServletResponse response) {
		
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		operation = operationService.getOperation(operationName, userId);		
				
		DataElement dataElement = loadDataRules.getDataRules(operation, true);
		csvFileMap.clear();
		csvRequiredMap.clear();
		csvSampleDataMap.clear();
		csvColDescMap.clear();
		csvValueList = null;
		requiredList = null;
		sampleDataList = null;
		colDescList = null;
		currentCSVfileName = null;
		oldCSVFileName = "";
		uniqueIdVal = null;
		fileList.clear();
		createWorksheetData(dataElement);
		if(csvFileMap.get(oldCSVFileName) == null)
		{
			csvFileMap.put(oldCSVFileName, csvValueList);
			csvRequiredMap.put(oldCSVFileName, requiredList);
			csvSampleDataMap.put(oldCSVFileName, sampleDataList);
			csvColDescMap.put(oldCSVFileName, colDescList);
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
			
			ArrayList<String> oldReqValList = (ArrayList<String>) csvRequiredMap.get(oldCSVFileName);
			if(requiredList != null && colDescList != null)
			{
				if(requiredList.size()>colDescList.size())
				{
					requiredList.remove(1);
				}
				for(int i = 0;i<requiredList.size();i++)
				{
					oldReqValList.add(requiredList.get(i));				
				}
			}
			csvRequiredMap.put(oldCSVFileName, oldReqValList);
			
			ArrayList<String> oldSampleValList = (ArrayList<String>) csvSampleDataMap.get(oldCSVFileName);
			if(sampleDataList != null)
			{
				for(int i = 0;i<sampleDataList.size();i++)
				{
					if(!oldSampleValList.contains(sampleDataList.get(i)))
					{
						oldSampleValList.add(sampleDataList.get(i));
					}
				}
			}
			csvSampleDataMap.put(oldCSVFileName, oldSampleValList);
			
			ArrayList<String> oldColValList = (ArrayList<String>) csvColDescMap.get(oldCSVFileName);
			if(colDescList != null)
			{
				for(int i = 0;i<colDescList.size();i++)
				{
					oldColValList.add(colDescList.get(i));				
				}
			}
			csvColDescMap.put(oldCSVFileName, oldColValList);
		}
		createWorksheetTemplate(response, operationName);
		if(fileList.size() > 0)
		{
			downloadMultipleFile(response, fileList, operationName);
		}
	}
	
	private void createWorksheetData(DataElement rootElement) {
		 
		List<DataElement> eleList = null;
		List<DataAttribute> rootAttrList = null;
		boolean isCSV = false;
		if(rootElement != null)
		{
			rootAttrList = rootElement.getAttributes();
			for(DataAttribute dataAttributeRoot : rootAttrList)
			{
				if("columnDescription".equalsIgnoreCase(dataAttributeRoot.getName()))
				{
					colDescVal = dataAttributeRoot.getValue();
				}
				if("fileName".equalsIgnoreCase(dataAttributeRoot.getName()))
				{
					currentCSVfileName = dataAttributeRoot.getValue();
					if(csvFileMap.get(currentCSVfileName) != null)
					{
						colDescVal = null;
					}
				}
				if("uniqueId".equalsIgnoreCase(dataAttributeRoot.getName())) 
				{
					if(csvValueList == null)
					{
						if(csvValueList == null)
						{
							csvValueList = new ArrayList<>();
						}
						uniqueIdVal = dataAttributeRoot.getValue().substring(1, dataAttributeRoot.getValue().length());
						csvValueList.add(uniqueIdVal);
					}
					else
					{
						uniqueIdVal = dataAttributeRoot.getValue().substring(1, dataAttributeRoot.getValue().length());
					}
				}
				if(oldCSVFileName.equals("") || currentCSVfileName.equalsIgnoreCase(oldCSVFileName))
				{
					//boolean isReqExist = checkRequiredExist(rootAttrList, "required");
					/*if("type".equalsIgnoreCase(dataAttributeRoot.getName()))
					{
						if(dataAttributeRoot.getValue().equalsIgnoreCase("$Worker Type"))
						{
							if(csvValueList == null)
							{
								csvValueList = new ArrayList<>();
							}
							csvValueList.add("Worker Type");
						}
					}*/
					if("required".equalsIgnoreCase(dataAttributeRoot.getName()))
					{
						if(requiredList == null)
						{
							requiredList = new ArrayList<>();
						}
						if(dataAttributeRoot.getValue().equalsIgnoreCase("true"))
						{
							requiredList.add("Required");
						}
						else
						{
							requiredList.add("Optional");
						}
					}
					if("mappedValue".equalsIgnoreCase(dataAttributeRoot.getName()))
					{
						if(sampleDataList == null)
						{
							sampleDataList = new ArrayList<>();
						}
						//if(!sampleDataList.contains(dataAttributeRoot.getValue()))
						if(sampleDataList.size() == 0)
						{
							firstSampleVal = dataAttributeRoot.getValue();
							sampleDataList.add(dataAttributeRoot.getValue());
						}
						else
						{
							sampleDataList.add(dataAttributeRoot.getValue());
						}
					}
					if("columnDescription".equalsIgnoreCase(dataAttributeRoot.getName()))
					{
						if(colDescList == null)
						{
							colDescList = new ArrayList<>();
						}
						//if(!colDescList.contains(dataAttributeRoot.getValue()))
						if(colDescList.size() == 0)
						{
							firstColDescVal = dataAttributeRoot.getValue();
							colDescList.add(dataAttributeRoot.getValue());
						}
						else
						{
							colDescList.add(dataAttributeRoot.getValue());
						}
					}
				}
				else
				{
					isCSV = true;
					if("required".equalsIgnoreCase(dataAttributeRoot.getName()))
					{
						if(dataAttributeRoot.getValue().equalsIgnoreCase("true"))
						{
							requiredVal = "Required";
						}
						else
						{
							requiredVal = "Optional";
						}
					}
					if("mappedValue".equalsIgnoreCase(dataAttributeRoot.getName()))
					{
						sampleDataVal = dataAttributeRoot.getValue();
					}					
				}
			}
			if(!oldCSVFileName.equals("") && !currentCSVfileName.equals(oldCSVFileName))
			{
				if(csvFileMap.get(oldCSVFileName) == null)
				{
					csvFileMap.put(oldCSVFileName, csvValueList);
					csvRequiredMap.put(oldCSVFileName, requiredList);
					csvSampleDataMap.put(oldCSVFileName, sampleDataList);
					csvColDescMap.put(oldCSVFileName, colDescList);
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
					
					ArrayList<String> oldReqValList = (ArrayList<String>) csvRequiredMap.get(oldCSVFileName);
					for(int i = 0;i<requiredList.size();i++)
					{
						oldReqValList.add(requiredList.get(i));				
					}
					csvRequiredMap.put(oldCSVFileName, oldReqValList);
					
					ArrayList<String> oldSampleValList = (ArrayList<String>) csvSampleDataMap.get(oldCSVFileName);
					for(int i = 0;i<sampleDataList.size();i++)
					{					
						oldSampleValList.add(sampleDataList.get(i));				
					}
					csvSampleDataMap.put(oldCSVFileName, oldSampleValList);
					
					ArrayList<String> oldColValList = (ArrayList<String>) csvColDescMap.get(oldCSVFileName);
					for(int i = 0;i<colDescList.size();i++)
					{
						oldColValList.add(colDescList.get(i));				
					}
					csvColDescMap.put(oldCSVFileName, oldColValList);
				}
				csvValueList = null;
				requiredList = null;
				sampleDataList = null;
				colDescList = null;
			}
			oldCSVFileName = currentCSVfileName;
			eleList = rootElement.getChildren();
			if(eleList.size() > 0)
			{
				for(DataElement dataElement : eleList)
				{
					List<DataAttribute> attrList = dataElement.getAttributes();
					for(int a=0;a<attrList.size();a++)
					{
						if(attrList.get(a).getValue().startsWith("$") && attrList.get(a).getName().equalsIgnoreCase("type"))
						{
							if(!csvValueList.contains(attrList.get(a).getValue().substring(1, attrList.get(a).getValue().length())))
							{
								csvValueList.add(attrList.get(a).getValue().substring(1, attrList.get(a).getValue().length()));
							}
						}
					}
					if(dataElement.getValue().contains("$"))
					{
						if(csvValueList == null)
						{
							csvValueList = new ArrayList<>();
							csvValueList.add(uniqueIdVal);
							if(csvFileMap.get(currentCSVfileName) == null)
							{
								if(requiredList == null)
								{
									requiredList = new ArrayList<>();
									requiredList.add("Required");
								}
								if(sampleDataList == null)
								{
									sampleDataList = new ArrayList<>();
									sampleDataList.add(firstSampleVal);
								}
								if(colDescList == null)
								{
									colDescList = new ArrayList<>();
									colDescList.add(firstColDescVal);
								}
							}
						}
						if(isCSV)
						{
							if(requiredList == null && requiredVal != null)
							{
								requiredList = new ArrayList<>();
								requiredList.add(requiredVal);
								requiredVal = null;
							}
							if(sampleDataList == null && sampleDataVal != null)
							{
								sampleDataList = new ArrayList<>();
								sampleDataList.add(sampleDataVal);
								sampleDataVal = null;
							}
							if(colDescList == null && colDescVal != null)
							{
								colDescList = new ArrayList<>();
								colDescList.add(colDescVal);
								colDescVal = null;
							}
						}
						if(!csvValueList.contains(dataElement.getValue().substring(1, dataElement.getValue().length())))
						{
							csvValueList.add(dataElement.getValue().substring(1, dataElement.getValue().length()));
						}
					}					
					createWorksheetData(dataElement);
				}
			}			
		}
	}

	private void createWorksheetTemplate(HttpServletResponse response, String operationName) {
		
		ArrayList<String> csvValueList = null;
		ArrayList<String> csvReqList = null;
		ArrayList<String> csvSampleList = null;
		ArrayList<String> csvColDescList = null;
		ArrayList<String> actCsvColDescList = null;
		String csvFileName = null;
		String csv = null;
		
		Iterator<Map.Entry<String, List<String>>> itr = csvFileMap.entrySet().iterator();
		while(itr.hasNext()) 
        {
			Map.Entry<String, List<String>> entry = itr.next();
			csvFileName = entry.getKey().substring(0,entry.getKey().indexOf("."));
			csv = entry.getKey();
        	csvValueList = (ArrayList<String>) entry.getValue();
        	csvReqList = (ArrayList<String>) csvRequiredMap.get(csv);
        	csvSampleList = (ArrayList<String>) csvSampleDataMap.get(csv);
        	csvColDescList = (ArrayList<String>) csvColDescMap.get(csv);
        	if(csvColDescList != null && csvValueList != null)
        	{
	        	if(csvColDescList.size() > csvValueList.size())
	        	{
	        		actCsvColDescList = (ArrayList<String>) csvColDescList.stream().distinct().collect(Collectors.toList());
	        		if(actCsvColDescList.size() > csvValueList.size())
	        		{
	        			actCsvColDescList.remove(actCsvColDescList.size()-1);
	        		}
	        	}
	        	else
	        	{
	        		actCsvColDescList = csvColDescList;
	        	}
        	}
        	createWorksheet(csvValueList, csvReqList, csvSampleList, actCsvColDescList, response, csvFileName, csvFileMap.size());
        }
	}

	private void createWorksheet(ArrayList<String> csvValueList, ArrayList<String> csvReqList,
			ArrayList<String> csvSampleList, ArrayList<String> csvColDescList, HttpServletResponse response, String csvFileName, int csvSize) {
		
			File file = null;
			Workbook workbook = new XSSFWorkbook(); 			
		
			createCover(workbook);
			createPurpose(workbook);
			createDocumentControlInfo(workbook);
			createInstructionSheet(workbook);
			String sheetName = null;
			
			if(csvFileName.contains("IBM Data Gathering Template"))
	        {
				sheetName = csvFileName.substring(csvFileName.indexOf("_")+1, csvFileName.length());
	        }
			else
			{
				sheetName = csvFileName;
			}
			Sheet sheet = workbook.createSheet(sheetName);
			
	        Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			
	        Font dataFont = workbook.createFont();
	        dataFont.setBold(true);
	        dataFont.setColor(IndexedColors.WHITE.getIndex());
			
	        CellStyle reqStyle = workbook.createCellStyle();
	        reqStyle.setAlignment(HorizontalAlignment.LEFT);
	        reqStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex()); 
	        reqStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	        
	        CellStyle blueStyle = workbook.createCellStyle();
	        blueStyle.setAlignment(HorizontalAlignment.LEFT);
	        blueStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex()); 
	        blueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	        blueStyle.setFont(headerFont);
	        
	        CellStyle dataStyle = workbook.createCellStyle();
	        dataStyle.setAlignment(HorizontalAlignment.LEFT);
	        dataStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex()); 
	        dataStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	        dataStyle.setFont(dataFont);
	        
	        Row row = sheet.createRow(1);
	        if(csvValueList != null)
	        {
		        for(int i=0;i<csvValueList.size();i++)
		        {
		        	Cell cell = row.createCell(i+1);
		        	cell.setCellValue(csvValueList.get(i));
		        	cell.setCellStyle(blueStyle);
		        }
	        }
	        
	        row = sheet.createRow(2);
	        if(csvReqList != null)
	        {
		        for(int i=0;i<csvReqList.size();i++)
		        {
		        	Cell cell1 = row.createCell(i+1);
		        	cell1.setCellValue(csvReqList.get(i));
		        	cell1.setCellStyle(reqStyle);
		        }
	        }
	        
	        row = sheet.createRow(3);
	        if(csvSampleList != null)
	        {
		        for(int i=0;i<csvSampleList.size();i++)
		        {
		        	Cell cell2 = row.createCell(i+1);
		        	cell2.setCellValue(csvSampleList.get(i));
		        }
	        }
	        
	        row = sheet.createRow(4);
	        if(csvColDescList != null)
	        {
		        for(int i=0;i<csvColDescList.size();i++)
		        {
		        	Cell cell3 = row.createCell(i+1);
		        	cell3.setCellValue(csvColDescList.get(i));
		        }
	        }
	        
	        row = sheet.createRow(10);
	        if(csvValueList != null)
	        {
		        for(int i=0;i<csvValueList.size();i++)
		        {
		        	Cell cell4 = row.createCell(i+1);
		        	cell4.setCellValue(csvValueList.get(i));
		        	cell4.setCellStyle(dataStyle);
		        }
	        }
	        
	        for(int i = 0; i < csvValueList.size() + 1; i++) {
	            sheet.autoSizeColumn(i);
	        }
	        
	        if(!csvFileName.contains("IBM Data Gathering Template"))
	        {
	        	csvFileName = "IBM Data Gathering Template_" + csvFileName;
	        }
	        	        	        
	        if(csvSize == 1)
	        {
				try 
				{
					file = File.createTempFile(csvFileName, ".xlsx");
					
					FileOutputStream fileOut = new FileOutputStream(file);
			        workbook.write(fileOut);
			        
					FileInputStream fis = null;
					response.setHeader("Content-Disposition", "attachment;filename=" + csvFileName + ".xlsx" + "");
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
	        else
	        {
	        	try 
	        	{
					file = File.createTempFile(csvFileName, ".xlsx");
					FileOutputStream fileOut = new FileOutputStream(file);
			        workbook.write(fileOut);
			        fileList.add(file);
				} 
	        	catch (IOException e) 
	        	{
					e.printStackTrace();
				}						
	        }
	}

	private void createCover(Workbook workbook) {
		
		
		Font font = workbook.createFont();  
        font.setFontHeightInPoints((short)20);
        font.setBold(true);
        font.setColor(IndexedColors.YELLOW.getIndex());
        
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex()); 
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(font);
        
		Sheet sheet = workbook.createSheet("Cover");
		Row row = sheet.createRow(17);
		Cell cell = row.createCell(5);
		cell.setCellValue("HYPERLOADER DATA GATHERING WORKBOOK");
		cell.setCellStyle(style);
		sheet.addMergedRegion(new CellRangeAddress(17, 17, 5, 13));
		
		InputStream my_banner_image = null;
		try 
		{
			//my_banner_image = new FileInputStream("C:\\HyperloaderCloud\\Hyperloader\\src\\main\\webapp\\images\\Workday.png");
			String currentDir = System.getProperty("user.dir");
	        System.out.println("Current dir using System:" +currentDir);
	        System.out.println(this.getClass().getClassLoader().getResource("Workday.png"));
			my_banner_image = this.getClass().getClassLoader().getResourceAsStream(("Workday.png"));
	        byte[] bytes = IOUtils.toByteArray(my_banner_image);
	        int my_picture_id = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
	        my_banner_image.close();
	        XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
	        XSSFClientAnchor my_anchor = new XSSFClientAnchor();
	        my_anchor.setCol1(5); 
	        my_anchor.setRow1(10); 
	        my_anchor.setCol2(7);
	        my_anchor.setRow2(13); 
	        XSSFPicture my_picture = drawing.createPicture(my_anchor, my_picture_id);
	        
			//my_banner_image = new FileInputStream("C:\\HyperloaderCloud\\Hyperloader\\src\\main\\webapp\\images\\IBM.png");
			my_banner_image = this.getClass().getClassLoader().getResourceAsStream(("IBM.png"));
	        bytes = IOUtils.toByteArray(my_banner_image);
	        my_picture_id = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
	        my_banner_image.close();
	        drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
	        my_anchor = new XSSFClientAnchor();
	        my_anchor.setCol1(9); 
	        my_anchor.setRow1(10); 
	        my_anchor.setCol2(11);
	        my_anchor.setRow2(12); 
	        my_picture = drawing.createPicture(my_anchor, my_picture_id);
	        
			//my_banner_image = new FileInputStream("C:\\HyperloaderCloud\\Hyperloader\\src\\main\\webapp\\images\\Hyperloader.png");
			my_banner_image = this.getClass().getClassLoader().getResourceAsStream(("Hyperloader.png"));
	        bytes = IOUtils.toByteArray(my_banner_image);
	        my_picture_id = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
	        my_banner_image.close();
	        drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
	        my_anchor = new XSSFClientAnchor();
	        my_anchor.setCol1(9); 
	        my_anchor.setRow1(12); 
	        my_anchor.setCol2(13);
	        my_anchor.setRow2(15); 
	        my_picture = drawing.createPicture(my_anchor, my_picture_id);
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

		
		for(int i = 0; i < 9; i++) {
            sheet.autoSizeColumn(i);
        }
		
	}

	private void createPurpose(Workbook workbook) {
		
        Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short)16);
		headerFont.setColor(IndexedColors.WHITE.getIndex());
		
        CellStyle blueStyle = workbook.createCellStyle();
        blueStyle.setAlignment(HorizontalAlignment.CENTER);
        blueStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex()); 
        blueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        blueStyle.setFont(headerFont);
        
		Sheet sheet = workbook.createSheet("Puspose");
		Row row = sheet.createRow(1);
		Cell cell = row.createCell(1);
		cell.setCellValue("Purpose");
		cell.setCellStyle(blueStyle);
		cell = row.createCell(2);
		cell.setCellValue("");
		
		row = sheet.createRow(2);
		cell = row.createCell(1);
		cell.setCellValue("");
		cell = row.createCell(2);
		cell.setCellValue("This is the reference material to help understand the technical jargon in Workday.");
		
		row = sheet.createRow(3);
		cell = row.createCell(1);
		cell.setCellValue("");
		cell = row.createCell(2);
		cell.setCellValue("Will be placeholder for Data Gathering session and discovery sessions.");
		
		row = sheet.createRow(4);
		cell = row.createCell(1);
		cell.setCellValue("");
		cell = row.createCell(2);
		cell.setCellValue("Will help the client to fill the data required to Load Data into Workday.");
		
		for(int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
		
	}

	private void createDocumentControlInfo(Workbook workbook) {
		
        Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setColor(IndexedColors.WHITE.getIndex());
		
		Font font = workbook.createFont();
		font.setBold(true);
		
        CellStyle blueStyle = workbook.createCellStyle();
        blueStyle.setAlignment(HorizontalAlignment.CENTER);
        blueStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex()); 
        blueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        blueStyle.setFont(headerFont);
                
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.LEFT);
        headerStyle.setFont(font);
        
		Sheet sheet = workbook.createSheet("Document Control Info");
		Row row = sheet.createRow(1);
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 2 ));
		Cell cell = row.createCell(1);
		cell.setCellValue("Document Control Information");
		cell.setCellStyle(blueStyle);
		cell = row.createCell(2);
		cell.setCellValue("");

		
		row = sheet.createRow(2);
		cell = row.createCell(1);
		cell.setCellValue("Document Name");
		cell.setCellStyle(headerStyle);
		cell = row.createCell(2);
		cell.setCellValue("HYPERLOADER DATA GATHERING WORKBOOK");
		
		row = sheet.createRow(3);
		cell = row.createCell(1);
		cell.setCellValue("Document Author");
		cell.setCellStyle(headerStyle);
		cell = row.createCell(2);
		cell.setCellValue("IBM Workday COE.");
		
		row = sheet.createRow(4);
		cell = row.createCell(1);
		cell.setCellValue("Document Version");
		cell.setCellStyle(headerStyle);
		cell = row.createCell(2);
		cell.setCellValue("");
		
		row = sheet.createRow(5);
		cell = row.createCell(1);
		cell.setCellValue("Document Status");
		cell.setCellStyle(headerStyle);
		cell = row.createCell(2);
		cell.setCellValue("");
		
		row = sheet.createRow(6);
		cell = row.createCell(1);
		cell.setCellValue("Date Released");
		cell.setCellStyle(headerStyle);
		cell = row.createCell(2);
		cell.setCellValue("");
		
		for(int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
		
	}

	private void createInstructionSheet(Workbook workbook) {
		
        Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setColor(IndexedColors.WHITE.getIndex());
		
        CellStyle blueStyle = workbook.createCellStyle();
        blueStyle.setAlignment(HorizontalAlignment.CENTER);
        blueStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex()); 
        blueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        blueStyle.setFont(headerFont);
        
		Sheet sheet = workbook.createSheet("Instruction");
		Row row = sheet.createRow(1);
		Cell cell = row.createCell(1);
		cell.setCellValue("ACTION");
		cell.setCellStyle(blueStyle);
		cell = row.createCell(2);
		cell.setCellValue("REFFERENCE");
		cell.setCellStyle(blueStyle);
		
		row = sheet.createRow(2);
		cell = row.createCell(1);
		cell.setCellValue("Populate Data");
		cell = row.createCell(2);
		cell.setCellValue("Column A3 onwards (Refer to comments for required fields).");
		
		row = sheet.createRow(3);
		cell = row.createCell(1);
		cell.setCellValue("Data Population comprises*");
		cell = row.createCell(2);
		cell.setCellValue("Refer to A7 and A8 cells, where the worksheet references are provided.");
		
		row = sheet.createRow(4);
		cell = row.createCell(1);
		cell.setCellValue("Key date for data extraction");
		cell = row.createCell(2);
		cell.setCellValue("Include all the \"Last changed\" or \"Latest Effective Dated\" changes only");
		
		row = sheet.createRow(5);
		cell = row.createCell(1);
		cell.setCellValue("Add/Delete/Modify of existing field items or columns");
		cell = row.createCell(2);
		cell.setCellValue("Please consult IBM resource before taking any of the action as it involves change to HyperLoader tool mapping in the backend.");
		
		row = sheet.createRow(6);
		cell = row.createCell(1);
		cell.setCellValue("Create Position");
		cell = row.createCell(2);
		cell.setCellValue("New Position for Position Management staffing model");
		
		row = sheet.createRow(7);
		cell = row.createCell(1);
		cell.setCellValue("");
		cell = row.createCell(2);
		cell.setCellValue("");
		
		row = sheet.createRow(8);
		sheet.addMergedRegion(new CellRangeAddress(8, 8, 1, 2 ));
		cell = row.createCell(1);
		cell.setCellValue("OUTPUT FILE REFERENCE");
		cell.setCellStyle(blueStyle);
		cell = row.createCell(2);
		cell.setCellValue("");		

		cell.setCellStyle(blueStyle);
		
		row = sheet.createRow(9);
		cell = row.createCell(1);
		cell.setCellValue("File Format");
		cell = row.createCell(2);
		cell.setCellValue("CSV (UTF - 8 formatted to account for international characters).");
		
		row = sheet.createRow(10);
		cell = row.createCell(1);
		cell.setCellValue("Unique ID");
		cell = row.createCell(2);
		cell.setCellValue("Follow unique Applicant ID, if data exists in multiple worksheets");
		
		row = sheet.createRow(11);
		cell = row.createCell(1);
		cell.setCellValue("");
		cell = row.createCell(2);
		cell.setCellValue("");
		
		row = sheet.createRow(12);
		sheet.addMergedRegion(new CellRangeAddress(12, 12, 1, 2 ));
		cell = row.createCell(1);
		cell.setCellValue("IMPORTANT POINTS");
		cell.setCellStyle(blueStyle);
		cell = row.createCell(2);
		cell.setCellValue("");				
		cell.setCellStyle(blueStyle);
		
		row = sheet.createRow(13);
		cell = row.createCell(1);
		cell.setCellValue("Comments on Cell");
		cell = row.createCell(2);
		cell.setCellValue("Please refer to comments as reference for the cells with comment.");
		
		for(int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
		
	}

}
