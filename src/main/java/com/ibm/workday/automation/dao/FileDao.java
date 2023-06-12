package com.ibm.workday.automation.dao;

import java.util.List;

import com.ibm.workday.automation.model.File;

public interface FileDao {
	List<File> getFileList();
	
	File getFileById(Long fileId);
	
	List<File> getFilesByUserId(Long userId);
	
	File getFileByClient(String client);
	
	List<File> getFilesByClient(String client);
	
	List<File> getFilesByFileNameClient(String fileName, String client);
	
	File getFileByFileNameClient(String fileName, String client);
	
	File getFileByFileNameClientUser(String fileName, String client, Long userId);	
	
	File getFileByFileName(String fileName);
	
	void addFile(File file);
	
	void updateFile(File file);
	
	void deleteFile(Long fileId);
	
}
