package com.ibm.workday.automation.dao;

import java.util.List;

import com.ibm.workday.automation.model.BuildRuleFile;

public interface BuildRuleFileDao {
	
	List<BuildRuleFile> getBuildFileList();
	
	BuildRuleFile getBuildFileById(Long fileId);
	
	List<BuildRuleFile> getBuildFilesByUser(Long userId);
	
	List<BuildRuleFile> getBuildFilesByClient(String client);
	
	List<BuildRuleFile> getBuildFilesByFileNameClient(String fileName, String client);
	
	BuildRuleFile getBuildFileByFileNameClient(String fileName, String client);
	
	BuildRuleFile getBuildFileByFileName(String fileName);
	
	void addBuildFile(BuildRuleFile file);
	
	void updateBuildFile(BuildRuleFile file);
	
	void deleteBuildFile(Long fileId);	

}
