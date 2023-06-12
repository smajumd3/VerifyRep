package com.ibm.workday.automation.dao;

import com.ibm.workday.automation.model.Result;

public interface ResultDao {

	Result getResult(Long resultId);
		
	void addResult(Result result);

	void updateResult(Result result);

	void deleteResult(Long resultId) ;
}
