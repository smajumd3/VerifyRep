package com.ibm.workday.automation.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.workday.automation.common.CommonConstants;
import com.ibm.workday.automation.model.ExclusionReference;
import com.ibm.workday.automation.model.User;
import com.ibm.workday.automation.service.ExclusionReferenceService;
import com.ibm.workday.automation.service.UserService;

@RestController
public class ExclusionController implements CommonConstants{
	
	@Autowired
	ExclusionReferenceService exclusionReferenceService;
	
	@Autowired
	UserService userService;
	
	@RequestMapping(value = "executeExcRefType/{refName}", method = RequestMethod.POST)
	public void executeExcRefType(@PathVariable("refName") String refName, HttpSession httpSession) {
		
		System.out.println("reqName-"+refName);
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		
		ExclusionReference exclusionReference = exclusionReferenceService.getReferencesByRefNameClient(refName, user.getClient());
		if(exclusionReference == null)
		{
			exclusionReference = new ExclusionReference();
			exclusionReference.setExclusionRefName(refName);
			exclusionReference.setClient(user.getClient());
			exclusionReferenceService.addReference(exclusionReference);
		}
		else
		{
			exclusionReference.setExclusionRefName(refName);
			exclusionReference.setClient(user.getClient());
			exclusionReferenceService.updateReference(exclusionReference);
		}
	}
	
	@RequestMapping(value = "/getAllExclusionReference", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<ExclusionReference> getAllExclusionReference(HttpSession httpSession) {
		Long userId = (Long) httpSession.getAttribute(SESSION_USER_ID);
		User user = userService.getUser(userId);
		return exclusionReferenceService.getReferencesByClient(user.getClient());
	}
	
	@RequestMapping(value = "/deleteExcRef/{exclusionRefId}", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public void deleteTenant(@PathVariable("exclusionRefId") Long exclusionRefId) {
		exclusionReferenceService.deleteReference(exclusionRefId);;
	}

}
