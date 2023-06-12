package com.ibm.workday.automation.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.workday.automation.dao.ResultDao;
import com.ibm.workday.automation.model.Result;

@Service("ResultService")
public class ResultService {
	
	@Autowired
	ResultDao resultDao;
	
	@Transactional
	public Result getResult(Long resultId) {
		return resultDao.getResult(resultId);
	}
	
	@Transactional
	public void addResult(Result result) {
		resultDao.addResult(result);
	}
	
	@Transactional
	public void updateResult(Result result) {
		resultDao.updateResult(result);
	}
	
	@Transactional
	public void deleteResult(Long resultId) {
		resultDao.deleteResult(resultId);
	}

 }
