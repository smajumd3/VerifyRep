package com.ibm.workday.automation.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.workday.automation.dao.SectionDao;
import com.ibm.workday.automation.model.Section;

@Service("SectionService")
public class SectionService {
	
	@Autowired
	SectionDao sectionDao;
	
	@Transactional
	public List<Section> getSectionsToExecute() {
		return sectionDao.getSectionsToExecute();
	}

	@Transactional
	public Section getSection(Long sectionId) {
		return sectionDao.getSection(sectionId);
	}
	
	@Transactional
	public void addSection(Section section) {
		sectionDao.addSection(section);
	}
	
	@Transactional
	public void updateSection(Section section) {
		sectionDao.updateSection(section);
	}
	
	@Transactional
	public void deleteSection(Long sectionId) {
		sectionDao.deleteSection(sectionId);
	}
}
