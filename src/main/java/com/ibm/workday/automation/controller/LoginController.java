package com.ibm.workday.automation.controller;

import java.util.Date;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ibm.workday.automation.common.CommonConstants;
import com.ibm.workday.automation.model.User;
import com.ibm.workday.automation.operation.UserUtil;
import com.ibm.workday.automation.service.UserService;

@Controller
public class LoginController implements CommonConstants {
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserUtil userUtil;
	
	@RequestMapping(value = "/login", method = RequestMethod.GET, headers = "Accept=application/json")
	public String login() {
		return "login";
	}
	
	@RequestMapping(value = "/logout", method = RequestMethod.GET, headers = "Accept=application/json")
	public String logout(HttpSession httpSession) {
		httpSession.setAttribute(SESSION_USER_ID, null);
		httpSession.invalidate();
		return "redirect:login";
	}
	
	@RequestMapping(value = "/mainView", method = RequestMethod.GET, headers = "Accept=application/json")
	public String mainView(HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		
		if(userId == null) {
			return "redirect:login";
		}
		
		return "mainview";
	}
	
	@RequestMapping(value = "/superUser", method = RequestMethod.GET, headers = "Accept=application/json")
	public String superUser(HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		
		if(userId == null) {
			return "redirect:login";
		}
		
		return "superuser";
	}
	
	@RequestMapping(value = "/adminUser", method = RequestMethod.GET, headers = "Accept=application/json")
	public String adminUser(HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		
		if(userId == null) {
			return "redirect:login";
		}
		
		return "adminuser";
	}
	
	@RequestMapping(value = "/", method = RequestMethod.GET, headers = "Accept=application/json")
	public String goToLogin() {
		return "redirect:login";
	}
	
	@RequestMapping(value = "/registerUser", method = RequestMethod.POST, headers = "Accept=application/json")
	public String registerUser(@RequestParam("userName") String userName, 
			                 @RequestParam("email") String email,
			                 @RequestParam("newpassword") String newpassword,
			                 @RequestParam("confpassword") String confpassword,
			                 @RequestParam("client") String client) {
		
		if(newpassword.equals(confpassword)) {
			userUtil.createUser(userName, email, newpassword, client);
		}
	
		return "redirect:login";
	}
	
	@RequestMapping(value = "/initiate", method = RequestMethod.POST, headers = "Accept=application/json")
	public String initiate(@RequestParam("email") String email,
                           @RequestParam("password") String password,
                           @RequestParam("client") String client,
                           HttpSession httpSession) {

		User user  = userUtil.validateCredentials(email, password, client, httpSession);
		
		if(user == null) {
			return "redirect:login";
		} else if(user.getSuperUser()) {
			return "redirect:superUser";
		} else if(user.getAdmin()) {
			return "redirect:adminUser";
		} else {
			user.setCurrentLoginTime(new Date(System.currentTimeMillis()));
			userService.updateUser(user);
			return "redirect:mainView";
		}
	}

}
