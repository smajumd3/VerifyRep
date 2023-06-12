package com.ibm.workday.automation.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.workday.automation.common.CommonConstants;
import com.ibm.workday.automation.model.AppVersion;
import com.ibm.workday.automation.model.User;
import com.ibm.workday.automation.service.AppVersionService;
import com.ibm.workday.automation.service.UserService;

@RestController
public class AppVersionController implements CommonConstants {
	
	@Autowired
	AppVersionService appVersionService;
	
	@Autowired
	UserService userService; 
	
	@RequestMapping(value = "/getVersionByClient", method = RequestMethod.GET, headers = "Accept=application/json")
	public AppVersion getVersionByClient(HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		return appVersionService.getVersionByClient(user.getClient());
	}
	
	@RequestMapping(value = "/addAppVersion/{version}", method = RequestMethod.POST, headers = "Accept=application/json")
	public void addUpdateAppVersion(@PathVariable("version") String version, HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		AppVersion appVersion = appVersionService.getVersionByClient(user.getClient());
		if(appVersion == null) {
			appVersion = new AppVersion();
			appVersion.setClient(user.getClient());
			appVersion.setVersion(version);
			appVersionService.addAppversion(appVersion);
		} else {
			appVersion.setVersion(version);
			appVersionService.updateAppversion(appVersion);
		}
	}

}
