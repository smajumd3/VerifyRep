package com.ibm.workday.automation.dao;

import java.util.List;

import com.ibm.workday.automation.model.Application;

public interface ApplicationDao {
	List<Application> getApplicationList();
	
	List<Application> getApplicationListByUser(Long userId);

	Application getApplication(Long applicationId);
	
	Application getApplication(String applicationName, String version, Long userId);
	
	void addApplication(Application application);

	void updateApplication(Application application);

	void deleteApplication(Long applicationId) ;

}
