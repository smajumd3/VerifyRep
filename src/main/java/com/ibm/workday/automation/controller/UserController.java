package com.ibm.workday.automation.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.workday.automation.common.CommonConstants;
import com.ibm.workday.automation.model.User;
import com.ibm.workday.automation.operation.UserUtil;
import com.ibm.workday.automation.service.UserService;

@RestController
public class UserController implements CommonConstants {
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserUtil userUtil;
	
	String client;
	
	@RequestMapping(value = "/addSuperUser", method = RequestMethod.POST, headers = "Accept=application/json")
	public void addSuperUser(@RequestBody User user, HttpSession httpSession) {	
		User userVal = userUtil.createUser(user.getUserName(), user.getUserEmail(), user.getUserPassword(), "All");
		userVal.setSuperUser(true);
		userService.updateUser(userVal);
	}
	
	@RequestMapping(value = "/addAdminUser", method = RequestMethod.POST, headers = "Accept=application/json")
	public void addAdminUser(@RequestBody User user, HttpSession httpSession) {	
		User userVal = userUtil.createUser(user.getUserName(), user.getUserEmail(), user.getUserPassword(), user.getClient());
		userVal.setAdmin(true);
		userService.updateUser(userVal);
	}
	
	@RequestMapping(value = "/updateAdminAccess/{id}/{checked}", method = RequestMethod.PUT, headers = "Accept=application/json")
	public void updateAdminAccess(@PathVariable("id") Long id, @PathVariable("checked") Boolean checked) {	
		User user = userService.getUser(id);
		user.setAdmin(checked);
		userService.updateUser(user);
	}
	
	@RequestMapping(value = "/updateUserAccess/{id}/{checked}", method = RequestMethod.PUT, headers = "Accept=application/json")
	public void updateUserAccess(@PathVariable("id") Long id, @PathVariable("checked") Boolean checked) {	
		User user = userService.getUser(id);
		user.setUserAccess(checked);
		userService.updateUser(user);
	}	
	
	@RequestMapping(value = "/getClientListForUser/{email}/{password}", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<String> getClientListForUser(@PathVariable("email")  String email,
			                                 @PathVariable("password") String password) {
		List<User> users = userService.getUsersByEmailPassword(email, password);
		List<String> clients = new ArrayList<>();
		
		if(users != null) {
			for(User user : users) {
				clients.add(user.getClient());
			}
		}
		
		return clients;
	}
	
	@RequestMapping(value = "/getUser", method = RequestMethod.GET, headers = "Accept=application/json")
	public User getUser(HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		return userService.getUser(userId);
	}	
	
	@RequestMapping(value = "/getProjectMembers", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<User> getProjectMembers(HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		client = user.getClient();
		return userService.getUsersByClient(client);
	}
	
	@RequestMapping(value = "/addProjectMember", method = RequestMethod.POST, headers = "Accept=application/json")
	public void addProjectMember(@RequestBody User user, HttpSession httpSession) {	
		userUtil.createUser(user.getUserName(), user.getUserEmail(), user.getUserPassword(), client);
	}
	
	@RequestMapping(value = "/updateProjectMember", method = RequestMethod.POST, headers = "Accept=application/json")
	public void updateProjectMember(@RequestBody User user, HttpSession httpSession) {	
		userUtil.updateUser(user.getUserName(), user.getUserEmail(), user.getUserPassword(), client);
	}	
	
	@RequestMapping(value = "/deleteProjectMember/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public void deleteProjectMember(@PathVariable("id") Long id) {
		userService.deletUser(id);
	}

	@RequestMapping(value = "/getAllSuperUsers", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<User> getAllSuperUsers() {
		return userService.getSuperUserList();
	}
	
	@RequestMapping(value = "/getAdminsByClient/{clientName}", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<User> getAdminListByClient(@PathVariable("clientName")  String clientName) {
		return userService.getAdminListByClient(clientName);
	}
	
	@RequestMapping(value = "/getUsersByClient/{clientName}", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<User> getUsersByClient(@PathVariable("clientName")  String clientName) {
		return userService.getUsersByClient(clientName);
	}	

}