package com.ibm.workday.automation.dao;

import java.util.List;

import com.ibm.workday.automation.model.PostLoad;

public interface PostLoadDao {
	
	List<PostLoad> getPostLoadList();
	
	PostLoad getPostLoad(Long postLoadId);
	
	PostLoad getPostLoad(String ruleName);
	
	PostLoad getPostLoadByLoadRule(String loadCycle, String ruleName);
	
	PostLoad getPostLoadByLoadRuleClient(String loadCycle, String ruleName, String client);
	
	PostLoad getPostLoadByClient(String client);
	
	List<PostLoad> getPostLoadsByClient(String client);
	
	void addPostLoad(PostLoad postLoad);

	void updatePostLoad(PostLoad postLoad);
	
	void deletePostLoad(Long postLoadId);

}
