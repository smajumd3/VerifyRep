package com.ibm.workday.automation.dao;

import java.util.List;

import com.ibm.workday.automation.model.OperationDir;


public interface OperationDirDao {
	List<OperationDir> getOperationDirList();
	OperationDir getOperationDir(Long id);
	void addOperationDir(OperationDir operationDir);
	void updateOperationDir(OperationDir operationDir);
	void deleteOperationDir(Long id);
	String getApplicationForOperarion(String operation);
}
