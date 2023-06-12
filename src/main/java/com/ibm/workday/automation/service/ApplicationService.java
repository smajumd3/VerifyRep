package com.ibm.workday.automation.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.workday.automation.dao.ApplicationDao;
import com.ibm.workday.automation.model.Application;

@Service("ApplicationService")
public class ApplicationService {
	
	@Autowired
	ApplicationDao applicationDao;
	
	@Transactional
	public List<Application> getApplicationList() {
		return applicationDao.getApplicationList();
	}
	
	@Transactional
	public List<Application> getApplicationListByUser(Long userId) {
		return applicationDao.getApplicationListByUser(userId);
	}
	
	@Transactional
	public Application getApplication(Long applicationId) {
		return applicationDao.getApplication(applicationId); 
	}
	
	@Transactional
	public Application getApplication(String applicationName, String version, Long userId) {
		return applicationDao.getApplication(applicationName, version, userId);
	}
	
	@Transactional
	public void addApplication(Application application) {
		applicationDao.addApplication(application);
	}
	
	@Transactional
	public void updateApplication(Application application) {
		applicationDao.updateApplication(application);
	}
	
	@Transactional
	public void deleteApplication(Long applicationId) {
		applicationDao.deleteApplication(applicationId);
	}

}
