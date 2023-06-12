package com.ibm.workday.automation.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.workday.automation.dao.OperationDao;
import com.ibm.workday.automation.model.Operation;

@Service("OperationService")
public class OperationService {

	@Autowired
	OperationDao operationDao;
	
	@Transactional
	public List<Operation> getOperationList() {
		return operationDao.getOperationList();
	}
	@Transactional
	public List<Operation> getOperationListByUser(Long userId) {
		return operationDao.getOperationListByUser(userId);
	}
	
	@Transactional
	public Operation getOperation(Long operationId) {
		return operationDao.getOperation(operationId); 
	}
	
	@Transactional
	public Operation getOperation(String operationName, Long userId) {
		return operationDao.getOperation(operationName, userId);
	}
	
	@Transactional
	public void addOperation(Operation operation) {
		operationDao.addOperation(operation);
	}
	
	@Transactional
	public void updateOperation(Operation operation) {
	    operationDao.updateOperation(operation);
	}
	
	@Transactional
	public void deleteOperation(Long operationId) {
		operationDao.deleteOperation(operationId);
	}
	
}
