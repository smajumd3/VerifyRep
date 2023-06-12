package com.ibm.workday.automation.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.workday.automation.common.CommonConstants;
import com.ibm.workday.automation.model.TenantMapping;
import com.ibm.workday.automation.model.User;
import com.ibm.workday.automation.service.TenantMappingService;
import com.ibm.workday.automation.service.UserService;

@RestController
public class TenantMappingController implements CommonConstants {

	@Autowired
	TenantMappingService mappingService;
	
	@Autowired
	UserService userService;
	
	@RequestMapping(value = "/getTenantMappingByClient", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<TenantMapping> getTenantMappingByClient(HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		return mappingService.getTenantMappingListByClient(user.getClient());		
	}
	
	@RequestMapping(value = "/addTenantMapping", method = RequestMethod.POST, headers = "Accept=application/json")
	public void addTenantMapping(@RequestBody TenantMapping mapping, HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		mapping.setClient(user.getClient());		
		mappingService.addTenantMapping(mapping);
	}
	
	@RequestMapping(value = "/deleteTenantMapping/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public void deleteTenantMapping(@PathVariable("id") Long id) {
		mappingService.deleteTenantMapping(id);
	}
}
