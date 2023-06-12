package com.ibm.workday.automation.dao;

import java.util.List;

import com.ibm.workday.automation.model.AppVersion;

public interface AppVersionDao {
	List<AppVersion> getVersionList();
	AppVersion getVersion(Long id);
	AppVersion getVersionByClient(String client);
	void addAppversion(AppVersion appversion);
	void updateAppversion(AppVersion appversion);
	void deleteAppversion(Long id);
}
