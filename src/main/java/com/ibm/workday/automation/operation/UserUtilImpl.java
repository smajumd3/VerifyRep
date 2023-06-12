package com.ibm.workday.automation.operation;

import java.util.Date;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ibm.workday.automation.common.CommonConstants;
import com.ibm.workday.automation.model.User;
import com.ibm.workday.automation.service.UserService;

@Component
public class UserUtilImpl implements UserUtil, CommonConstants {
	
	@Autowired
	UserService userService;

	@Override
	public User createUser(String userName, String email, String password, String client) {
		User userVal = userService.getUserByEmailClient(email, client);

		if(userVal == null) {
		    User user = new User();
		    user.setUserName(userName);
		    user.setUserEmail(email);
		    user.setUserPassword(password);
		    user.setClient(client);
		    user.setSuperUser(userService.getUserList().isEmpty());
		    user.setAdmin(false);
		    user.setUserAccess(true);
		    user.setCreateTime(new Date(System.currentTimeMillis()));
		    userService.createUser(user);
		    return user;
		} else {
			userVal.setUserPassword(password);
			userService.updateUser(userVal);
		}
		
		return userVal;
	}
	
	@Override
	public User updateUser(String userName, String email, String password, String client) {
		User userVal = userService.getUserByEmailClient(email, client);
		
		if(userVal!= null) {
			userVal.setUserPassword(password);
			userService.updateUser(userVal);
		}
		
		return userVal;
	}

	@Override
	public User validateCredentials(String email, String password, String client, HttpSession httpSession) {
		if ((email != null && !email.isEmpty()) 
				&& (password != null && !password.isEmpty())
				&& (client!= null && !client.isEmpty())) {
			User user = userService.getUserByEmailClient(email, client);
			if(user != null && !user.getUserAccess()) {
				return null;
			}
			if(user != null && password.equals(user.getUserPassword())) {
				httpSession.setAttribute(SESSION_USER_ID, user.getId());
				return user;
			}
		}
		
		return null;
	}

}
