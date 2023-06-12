package com.ibm.workday.automation.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.workday.automation.dao.PostLoadDao;
import com.ibm.workday.automation.model.PostLoad;

@Service("PostLoadService")
public class PostLoadService {
	
	@Autowired
	PostLoadDao postLoadDao;
	
	@Transactional
	public List<PostLoad> getPostLoadList() {
		return postLoadDao.getPostLoadList();
	}
	
	@Transactional
	public PostLoad getPostLoad(Long postLoadId) {
		return postLoadDao.getPostLoad(postLoadId);
	}
	
	@Transactional
	public PostLoad getPostLoad(String ruleName) {
		return postLoadDao.getPostLoad(ruleName);
	}
	
	@Transactional
	public PostLoad getPostLoadByLoadRule(String loadCycle, String ruleName) {
		return postLoadDao.getPostLoadByLoadRule(loadCycle, ruleName);
	}
	
	@Transactional
	public PostLoad getPostLoadByClient(String client) {
		return postLoadDao.getPostLoadByClient(client);
	}
	
	@Transactional
	public List<PostLoad> getPostLoadsByClient(String client) {
		return postLoadDao.getPostLoadsByClient(client);
	}
	
	@Transactional
	public PostLoad getPostLoadByLoadRuleClient(String loadCycle, String ruleName, String client) {
		return postLoadDao.getPostLoadByLoadRuleClient(loadCycle, ruleName, client);
	}
	
	@Transactional
	public void addPostLoad(PostLoad postLoad) {
		postLoadDao.addPostLoad(postLoad);
	}
	
	@Transactional
	public void updatePostLoad(PostLoad postLoad) {
		postLoadDao.updatePostLoad(postLoad);
	}
	
	@Transactional
	public void deletePostLoad(Long postLoadId) {
		postLoadDao.deletePostLoad(postLoadId);
	}

}
