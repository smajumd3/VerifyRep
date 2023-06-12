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
import com.ibm.workday.automation.model.Tenant;
import com.ibm.workday.automation.model.User;
import com.ibm.workday.automation.service.TenantService;
import com.ibm.workday.automation.service.UserService;


@RestController
public class TenantController implements CommonConstants {

	@Autowired
	TenantService tenantService;
	
	@Autowired
	UserService userService;

	@RequestMapping(value = "/getAllTenants", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<Tenant> getAllTenants() {
		List<Tenant> listOfTenants = tenantService.getTenantList();
		return listOfTenants;
	}
	
	@RequestMapping(value = "/getAllTenantsByUser", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<Tenant> getAllTenantsByUser(HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		return tenantService.getTenantListByUser(userId, user.getClient());
	}
	
	@RequestMapping(value = "/getAllTenantsByClient/{clientName}", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<Tenant> getAllTenantsByClient(@PathVariable("clientName") String clientName) {
		return tenantService.getTenantListByClient(clientName, (long) -1);
	}
	
	@RequestMapping(value = "/getTenantsByClient", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<Tenant> getTenantsByClient(HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		String clientName = user.getClient();
		return tenantService.getTenantListByClient(clientName, (long) -1);
	}

	@RequestMapping(value = "/getTenant/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
	public Tenant getTenantById(@PathVariable("id") Long id) {
		return tenantService.getTenant(id);
	}

	@RequestMapping(value = "/addTenant", method = RequestMethod.POST, headers = "Accept=application/json")
	public void addTenant(@RequestBody Tenant tenant, HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		tenant.setUserId(userId);
		tenantService.addTenant(tenant);
	}
	
	@RequestMapping(value = "/createTenant", method = RequestMethod.POST, headers = "Accept=application/json")
	public void createTenant(@RequestBody Tenant tenant, HttpSession httpSession) {
		tenantService.addTenant(tenant);
	}	

	@RequestMapping(value = "/updateTenant", method = RequestMethod.PUT, headers = "Accept=application/json")
	public void updateTenant(@RequestBody Tenant tenant) {
		tenantService.updateTenant(tenant); 
	}	

	@RequestMapping(value = "/deleteTenant/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public void deleteTenant(@PathVariable("id") Long id) {
		tenantService.deleteTenant(id);
	}

}
