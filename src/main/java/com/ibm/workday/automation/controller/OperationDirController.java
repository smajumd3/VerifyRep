package com.ibm.workday.automation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.workday.automation.model.OperationDir;
import com.ibm.workday.automation.service.OperationDirService;

@RestController
public class OperationDirController {

	@Autowired
	OperationDirService operationDirService;
	
	@RequestMapping(value = "/getOperationDirList", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<OperationDir> getOperationDirList() {
		List<OperationDir> listOfOperationDirs = operationDirService.getOperationDirList();
		return listOfOperationDirs;
	}
	
	@RequestMapping(value = "/getOperationDir/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
	public OperationDir getOperationDirById(@PathVariable("id") Long id) {
		return operationDirService.getOperationDir(id);
	}
	
	@RequestMapping(value = "/addOperationDir", method = RequestMethod.POST, headers = "Accept=application/json")
	public void addOperationDir(@RequestBody OperationDir operationDir) {
		operationDirService.addOperationDir(operationDir);
	}
	
	@RequestMapping(value = "/updateOperationDir", method = RequestMethod.PUT, headers = "Accept=application/json")
	public void updateOperationDir(@RequestBody OperationDir operationDir) {
		operationDirService.updateOperationDir(operationDir);
	}
	
	@RequestMapping(value = "/deleteOperationDir/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public void deleteOperationDir(@PathVariable("id") Long id) {
		operationDirService.deleteOperationDir(id);
	}
}
