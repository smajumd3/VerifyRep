package com.ibm.workday.automation.dao;

import java.util.List;

import com.ibm.workday.automation.model.Operation;

public interface OperationDao {
	List<Operation> getOperationList();
		
	List<Operation> getOperationListByUser(Long userId);

	Operation getOperation(Long operationId);
	
	Operation getOperation(String operationName, Long userId);
		
	void addOperation(Operation operation);

	void updateOperation(Operation operation);

	void deleteOperation(Long operationId) ;

}
