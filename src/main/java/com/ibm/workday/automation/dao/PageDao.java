package com.ibm.workday.automation.dao;

import java.util.List;

import com.ibm.workday.automation.model.Page;

public interface PageDao {
	
	List<Page> getPagesByUser(Long userId);
	
	Page getPageByIndex(Integer index);

	Page getPage(Long pageId);
	
	void addPage(Page page);

	void updatePage(Page page);

	void deletePage(Long pageId) ;
}
