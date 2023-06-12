package com.ibm.workday.automation.dao;

import java.util.List;

import com.ibm.workday.automation.model.Section;

public interface SectionDao {
	
	List<Section> getSectionsToExecute();	

	Section getSection(Long sectionId);
		
	void addSection(Section section);

	void updateSection(Section section);

	void deleteSection(Long sectionId) ;

}
