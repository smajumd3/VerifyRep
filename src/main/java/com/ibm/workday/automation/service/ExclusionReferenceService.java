package com.ibm.workday.automation.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.workday.automation.dao.ExclusionReferenceDao;
import com.ibm.workday.automation.model.ExclusionReference;

@Service("ExclusionReferenceService")
public class ExclusionReferenceService {

	@Autowired
	ExclusionReferenceDao exclusionReferenceDao;
	
	@Transactional
	public List<ExclusionReference> getReferenceList() {
		return exclusionReferenceDao.getReferenceList();
	}
	
	@Transactional
	public ExclusionReference getReferenceById(Long refId) {
		return exclusionReferenceDao.getReferenceById(refId);
	}	
	
	@Transactional
	public void addReference(ExclusionReference exclusionReference) {
		exclusionReferenceDao.addReference(exclusionReference);
	}
	
	@Transactional
	public void updateReference(ExclusionReference exclusionReference) {
		exclusionReferenceDao.updateReference(exclusionReference);
	}
	
	@Transactional
	public void deleteReference(Long refId) {
		exclusionReferenceDao.deleteReference(refId);
	}
	
	@Transactional
	public ExclusionReference getReferenceByClient(String client) {
		return exclusionReferenceDao.getReferenceByClient(client);
	}
	
	@Transactional
	public List<ExclusionReference> getReferencesByClient(String client) {
		return exclusionReferenceDao.getReferencesByClient(client);
	}
	
	@Transactional
	public ExclusionReference getReferencesByRefNameClient(String refName, String client) {
		return exclusionReferenceDao.getReferencesByRefNameClient(refName, client);
	}
	
	@Transactional
	public ExclusionReference getReferencesByRefName(String refName) {
		return exclusionReferenceDao.getReferencesByRefName(refName);
	}
	
}
