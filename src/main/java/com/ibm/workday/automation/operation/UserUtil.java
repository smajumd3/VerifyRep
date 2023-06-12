package com.ibm.workday.automation.operation;

import javax.servlet.http.HttpSession;

import com.ibm.workday.automation.model.User;

public interface UserUtil {
	
	User createUser(String userName, String email, String password, String client);
	User updateUser(String userName, String email, String password, String client);
	User validateCredentials(String email, String password, String client, HttpSession httpSession);
}
