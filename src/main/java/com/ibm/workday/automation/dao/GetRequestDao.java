package com.ibm.workday.automation.dao;

import java.util.List;

import com.ibm.workday.automation.model.GetRequest;

public interface GetRequestDao {
	
	List<GetRequest> getRequestList();
	
	GetRequest getRequestId(Long getRequestId);
	
	GetRequest getRequestName(String requestName);
	
	GetRequest getRequestByClient(String client);
	
	GetRequest getRequestByReqClient(String requestName, String client);
	
	List<GetRequest> getRequestsByClient(String client);
	
	void addGetRequest(GetRequest getRequest);

	void updateGetRequest(GetRequest getRequest);
	
	void deleteGetRequest(Long getRequestId);

}
