package com.ibm.workday.automation.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.workday.automation.dao.AppVersionDao;
import com.ibm.workday.automation.model.AppVersion;

@Service("AppVersionService")
public class AppVersionService {

	@Autowired
	AppVersionDao appVersionDao;
	
	@Transactional
	public List<AppVersion> getVersionList() {
		return appVersionDao.getVersionList();
	}
	
	@Transactional
	public AppVersion getVersion(Long id) {
		return appVersionDao.getVersion(id);
	}
	
	@Transactional
	public AppVersion getVersionByClient(String client) {
		return appVersionDao.getVersionByClient(client);
	}
	
	@Transactional
	public void addAppversion(AppVersion appversion) {
		appVersionDao.addAppversion(appversion);
	}
	
	@Transactional
	public void updateAppversion(AppVersion appversion) {
		appVersionDao.updateAppversion(appversion);
	}
	
	@Transactional
	public void deleteAppversion(Long id) {
		appVersionDao.deleteAppversion(id);
	}
	
}
