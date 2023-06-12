package com.ibm.workday.automation.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.workday.automation.dao.PageDao;
import com.ibm.workday.automation.model.Page;

@Service("PageService")
public class PageService {

	@Autowired
	PageDao pageDao;
	
	@Transactional
	public List<Page> getPagesByUser(Long userId) {
		return pageDao.getPagesByUser(userId);
	}
	
	@Transactional
	public Page getPageByIndex(Integer index) {
		return pageDao.getPageByIndex(index);
	}
	
	@Transactional
	public Page getPage(Long pageId) {
		return pageDao.getPage(pageId);
	}
	
	@Transactional
	public void addPage(Page page) {
		pageDao.addPage(page);
	}
	
	@Transactional
	public void updatePage(Page page) {
		pageDao.updatePage(page);
	}
	
	@Transactional
	public void deletePage(Long pageId) {
		pageDao.deletePage(pageId);
	}
}
