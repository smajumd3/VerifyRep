package com.ibm.workday.automation.dao;

import java.util.List;

import com.ibm.workday.automation.model.ExclusionReference;

public interface ExclusionReferenceDao {
	
	List<ExclusionReference> getReferenceList();
	
	ExclusionReference getReferenceById(Long refId);
	
	ExclusionReference getReferenceByClient(String client);
	
	List<ExclusionReference> getReferencesByClient(String client);
	
	ExclusionReference getReferencesByRefNameClient(String refName, String client);
	
	ExclusionReference getReferencesByRefName(String refName);
	
	void addReference(ExclusionReference exclusionReference);
	
	void updateReference(ExclusionReference exclusionReference);
	
	void deleteReference(Long refId);
}
