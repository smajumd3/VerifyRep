package com.ibm.workday.automation.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.workday.automation.dao.FileDao;
import com.ibm.workday.automation.model.File;

@Service("FileService")
public class FileService {

	@Autowired
	FileDao fileDao;
	
	@Transactional
	public List<File> getFileList() {
		return fileDao.getFileList();
	}
	
	@Transactional
	public File getFileById(Long fileId) {
		return fileDao.getFileById(fileId);
	}
	
	@Transactional
	public List<File> getFilesByUserId(Long userId) {
		return fileDao.getFilesByUserId(userId);
	}	
	
	@Transactional
	public void addFile(File file) {
		fileDao.addFile(file);
	}
	
	@Transactional
	public void updateFile(File file) {
		fileDao.updateFile(file);
	}
	
	@Transactional
	public void deleteFile(Long fileId) {
		fileDao.deleteFile(fileId);
	}
	
	@Transactional
	public File getFileByClient(String client) {
		return fileDao.getFileByClient(client);
	}
	
	@Transactional
	public List<File> getFilesByClient(String client) {
		return fileDao.getFilesByClient(client);
	}
	
	@Transactional
	public File getFileByFileNameClient(String fileName, String client) {
		return fileDao.getFileByFileNameClient(fileName, client);
	}
	
	@Transactional
	public File getFileByFileNameClientUser(String fileName, String client, Long userId) {
		return fileDao.getFileByFileNameClientUser(fileName, client, userId);
	}	
	
	@Transactional
	public File getFileByFileName(String fileName) {
		return fileDao.getFileByFileName(fileName);
	}
	
	@Transactional
	public List<File> getFilesByFileNameClient(String fileName, String client) {
		return fileDao.getFilesByFileNameClient(fileName, client);
	}
	
}
