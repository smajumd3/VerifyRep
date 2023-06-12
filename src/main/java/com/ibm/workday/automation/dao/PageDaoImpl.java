package com.ibm.workday.automation.dao;

import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ibm.workday.automation.model.Page;

@Repository
public class PageDaoImpl implements PageDao {

	@Autowired
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sf) {
		this.sessionFactory = sf;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Page> getPagesByUser(Long userId) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<Page> query = session.createQuery("from Page page where page.userId=?1");
		query.setParameter(1, userId);
		
		List<Page> pages;
		try {
			pages = query.getResultList();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return pages;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Page getPageByIndex(Integer index) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<Page> query = session.createQuery("from Page page where page.index=?1");
		query.setParameter(1, index);
		
		Page page;
		try {
			page = query.getSingleResult();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return page;
	}

	@Override
	public Page getPage(Long pageId) {
		Session session = this.sessionFactory.getCurrentSession();
		Page page = (Page) session.get(Page.class, pageId);
		return page;
	}

	@Override
	public void addPage(Page page) {
		Session session = this.sessionFactory.getCurrentSession();
		session.save(page);
		session.flush();
	}

	@Override
	public void updatePage(Page page) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(page);
		session.flush();
	}

	@Override
	public void deletePage(Long pageId) {
		Session session = this.sessionFactory.getCurrentSession();
		Page page = (Page) session.load(Page.class, pageId);
		if (null != page) {
			session.delete(page);
		}
	}

}
