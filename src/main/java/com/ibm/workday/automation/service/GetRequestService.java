package com.ibm.workday.automation.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.workday.automation.dao.GetRequestDao;
import com.ibm.workday.automation.model.GetRequest;

@Service("GetRequestService")
public class GetRequestService {
	
	@Autowired
	GetRequestDao getRequestDao;
	
	@Transactional
	public List<GetRequest> getRequestList() {
		return getRequestDao.getRequestList();
	}
	
	@Transactional
	public GetRequest getRequestId(Long getRequestId) {
		return getRequestDao.getRequestId(getRequestId);
	}
	
	@Transactional
	public GetRequest getRequestName(String requestName) {
		return getRequestDao.getRequestName(requestName);
	}
	
	@Transactional
	public GetRequest getRequestByClient(String client) {
		return getRequestDao.getRequestByClient(client);
	}
	
	@Transactional
	public GetRequest getRequestByReqClient(String requestName, String client) {
		return getRequestDao.getRequestByReqClient(requestName, client);
	}
	
	@Transactional
	public List<GetRequest> getRequestsByClient(String client) {
		return getRequestDao.getRequestsByClient(client);
	}
	
	@Transactional
	public void addGetRequest(GetRequest getRequest) {
		getRequestDao.addGetRequest(getRequest);
	}
	
	@Transactional
	public void updateGetRequest(GetRequest getRequest) {
		getRequestDao.updateGetRequest(getRequest);
	}
	
	@Transactional
	public void deleteGetRequest(Long getRequestId) {
		getRequestDao.deleteGetRequest(getRequestId);
	}

}
