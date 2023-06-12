package com.ibm.workday.automation.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.workday.automation.dao.OperationDirDao;
import com.ibm.workday.automation.model.OperationDir;

@Service("OperationDirService")
public class OperationDirService {
	
	@Autowired
	OperationDirDao operationDirDao;
	
	@Transactional
	public List<OperationDir> getOperationDirList() {
		return operationDirDao.getOperationDirList();
	}
	
	@Transactional
	public OperationDir getOperationDir(Long id) {
		return operationDirDao.getOperationDir(id);
	}
	
	@Transactional
	public void addOperationDir(OperationDir operationDir) {
		operationDirDao.addOperationDir(operationDir);
	}
	
	@Transactional
	public void updateOperationDir(OperationDir operationDir) {
		operationDirDao.updateOperationDir(operationDir);
	}
	
	@Transactional
	public void deleteOperationDir(Long id) {
		operationDirDao.deleteOperationDir(id);
	}

}
