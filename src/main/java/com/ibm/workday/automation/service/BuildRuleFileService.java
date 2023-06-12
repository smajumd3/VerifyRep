package com.ibm.workday.automation.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.workday.automation.dao.BuildRuleFileDao;
import com.ibm.workday.automation.model.BuildRuleFile;

@Service("BuildRuleFileService")
public class BuildRuleFileService {
	
	@Autowired
	BuildRuleFileDao buildRuleFileDao;
	
	@Transactional
	public List<BuildRuleFile> getBuildFileList() {
		return buildRuleFileDao.getBuildFileList();
	}
	
	@Transactional
	public BuildRuleFile getBuildFileById(Long fileId) {
		return buildRuleFileDao.getBuildFileById(fileId);
	}
	
	@Transactional
	public List<BuildRuleFile> getBuildFilesByUser(Long userId) {
		return buildRuleFileDao.getBuildFilesByUser(userId);
	}
	
	@Transactional
	public List<BuildRuleFile> getBuildFilesByClient(String client) {
		return buildRuleFileDao.getBuildFilesByClient(client);
	}
	
	@Transactional
	public List<BuildRuleFile> getBuildFilesByFileNameClient(String fileName, String client) {
		return buildRuleFileDao.getBuildFilesByFileNameClient(fileName, client);
	}
	
	@Transactional
	public BuildRuleFile getBuildFileByFileNameClient(String fileName, String client) {
		return buildRuleFileDao.getBuildFileByFileNameClient(fileName, client);
	}
	
	@Transactional
	public BuildRuleFile getBuildFileByFileName(String fileName) {
		return buildRuleFileDao.getBuildFileByFileName(fileName);
	}
	
	@Transactional
	public void addBuildFile(BuildRuleFile file) {
		buildRuleFileDao.addBuildFile(file);
	}
	
	@Transactional
	public void updateBuildFile(BuildRuleFile file) {
		buildRuleFileDao.updateBuildFile(file);
	}
	
	@Transactional
	public void deleteBuildFile(Long fileId) {
		buildRuleFileDao.deleteBuildFile(fileId);
	}

}
