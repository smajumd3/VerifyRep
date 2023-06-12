package com.ibm.workday.automation.controller;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.workday.automation.common.CommonConstants;
import com.ibm.workday.automation.model.Application;
import com.ibm.workday.automation.operation.ApplicationUtil;
import com.ibm.workday.automation.properties.ApplicationProperties;
import com.ibm.workday.automation.service.ApplicationService;

@RestController
public class ApplicationController implements CommonConstants {
	
	@Autowired
	ApplicationProperties appProperties;
	
	@Autowired
	ApplicationUtil applicationUtil;
	
	@Autowired
	ApplicationService applicationService;
	
	@RequestMapping(value = "/getApplicationOperations/{applicationId}", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<String> getApplicationOperations(@PathVariable("applicationId") Long applicationId) {
		Application application = applicationService.getApplication(applicationId);
		return applicationUtil.getAvailableOperations(application);
	}
	
	@RequestMapping(value = "/getApplications", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<String> getApplicationNames() {
		
		String applicationNames = appProperties.getNames();
		List<String> applications = Arrays.asList(applicationNames.split(";"));
		applications.sort(String.CASE_INSENSITIVE_ORDER);

		return applications;
	}
	
	@RequestMapping(value = "/getAllApplications", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<Application> getApplicationList() {
		return applicationService.getApplicationList();
	}
	
	@RequestMapping(value = "/getAllApplicationsByUser", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<Application> getAllApplicationsByUser(HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		return applicationService.getApplicationListByUser(userId);
	}

	@RequestMapping(value = "/getApplication/{applicationId}", method = RequestMethod.GET, headers = "Accept=application/json")
	public Application getApplicationById(@PathVariable("applicationId") Long applicationId) {
		return applicationService.getApplication(applicationId);
	}

	@RequestMapping(value = "/addApplication", method = RequestMethod.POST, headers = "Accept=application/json")
	public void addApplication(@RequestBody Application application, HttpSession httpSession) {
		byte[] wsdlFileData = applicationUtil.generateWsdlData(application);
//		byte[] xsdFileData = applicationUtil.generateXsdData(application);
		
		application.setWsdlFileData(wsdlFileData);
//		application.setXsdFileData(xsdFileData);
		
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		application.setUserId(userId);
		
		applicationService.addApplication(application);
	}

	@RequestMapping(value = "/updateApplication", method = RequestMethod.PUT, headers = "Accept=application/json")
	public void updateApplication(@RequestBody Application application) {
		byte[] wsdlFileData = applicationUtil.generateWsdlData(application);
//		byte[] xsdFileData = applicationUtil.generateXsdData(application);
		
		application.setWsdlFileData(wsdlFileData);
//		application.setXsdFileData(xsdFileData);
		
		applicationService.updateApplication(application); 
	}	

	@RequestMapping(value = "/deleteApplication/{applicationId}", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public void deleteApplication(@PathVariable("applicationId") Long applicationId) {
		applicationService.deleteApplication(applicationId);
	}	

}
