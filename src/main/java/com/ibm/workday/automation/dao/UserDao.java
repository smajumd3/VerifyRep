package com.ibm.workday.automation.dao;

import java.util.List;

import com.ibm.workday.automation.model.User;

public interface UserDao {
	List<User> getUserList();
	
	List<User> getSuperUserList();
	
	List<User> getUserListByClient(String client);
	
	List<User> getAdminListByClient(String client);
	
	List<User> getUserListByEmailPassword(String email, String password);
	
	User getUserByEmailClient(String email,String client);

	User getUser(Long id);

	void addUser(User user);
	
	void updateUser(User user);

	void deleteUser(Long id) ;
}
