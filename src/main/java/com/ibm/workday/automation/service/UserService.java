package com.ibm.workday.automation.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.workday.automation.dao.UserDao;
import com.ibm.workday.automation.model.User;

@Service("UserService")
public class UserService {
	
	@Autowired
	UserDao userDao;
	
	@Transactional
	public User getUser(Long id) {
		return userDao.getUser(id);
	}
	
	@Transactional
	public List<User> getUserList() {
		return userDao.getUserList();
	}
	
	@Transactional
	public List<User> getUsersByClient(String client) {
		return userDao.getUserListByClient(client);
	}
	
	@Transactional
	public List<User> getAdminListByClient(String client) {
		return userDao.getAdminListByClient(client);
	}
	
	@Transactional
	public List<User> getSuperUserList() {
		return userDao.getSuperUserList();
	}
	
	@Transactional
	public List<User> getUsersByEmailPassword(String email, String password) {
		return userDao.getUserListByEmailPassword(email, password);
	}

	@Transactional
	public User getUserByEmailClient(String email, String client) {
		return userDao.getUserByEmailClient(email, client);
	}
	
	@Transactional
	public void createUser(User user) {
		userDao.addUser(user);
	}

	@Transactional
	public void updateUser(User user) {
		userDao.updateUser(user);
	}

	@Transactional
	public void deletUser(Long id) {
		userDao.deleteUser(id);
	}

}
