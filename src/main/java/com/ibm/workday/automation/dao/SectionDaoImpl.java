package com.ibm.workday.automation.dao;

import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ibm.workday.automation.model.Section;

@Repository
public class SectionDaoImpl implements SectionDao {

	@Autowired
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sf) {
		this.sessionFactory = sf;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Section> getSectionsToExecute() {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<Section> query = session.createQuery("from Section section where section.execute=?1");
		query.setParameter(1, true);
		
		List<Section> sections;
		try {
			sections = query.getResultList();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return sections;
	}

	@Override
	public Section getSection(Long sectionId) {
		Session session = this.sessionFactory.getCurrentSession();
		Section section = (Section) session.get(Section.class, sectionId);
		return section;
	}
	
	@Override
	public void addSection(Section section) {
		Session session = this.sessionFactory.getCurrentSession();
		session.save(section);
		session.flush();
	}

	@Override
	public void updateSection(Section section) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(section);
		session.flush();
	}

	@Override
	public void deleteSection(Long sectionId) {
		Session session = this.sessionFactory.getCurrentSession();
		Section section = (Section) session.load(Section.class, sectionId);
		if (null != section) {
			session.delete(section);
		}
	}

}
