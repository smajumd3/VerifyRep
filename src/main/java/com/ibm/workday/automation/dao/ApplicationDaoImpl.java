package com.ibm.workday.automation.dao;

import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ibm.workday.automation.model.Application;

@Repository
public class ApplicationDaoImpl implements ApplicationDao{
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sf) {
		this.sessionFactory = sf;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Application> getApplicationList() {
		Session session = this.sessionFactory.getCurrentSession();
		List<Application>  appList = session.createQuery("from Application").list();
		return appList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Application> getApplicationListByUser(Long userId) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<Application> query = session.createQuery("from Application application where application.userId=?1");
		query.setParameter(1, userId);
		
		List<Application> application;
		try {
			application = query.getResultList();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return application;
	}

	@Override
	public Application getApplication(Long applicationId) {
		Session session = this.sessionFactory.getCurrentSession();
		Application application = (Application) session.get(Application.class, applicationId);
		return application;
	}

	@Override
	public void addApplication(Application application) {
		Session session = this.sessionFactory.getCurrentSession();
		session.save(application);
		session.flush();
	}

	@Override
	public void updateApplication(Application application) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(application);
		session.flush();
	}

	@Override
	public void deleteApplication(Long applicationId) {
		Session session = this.sessionFactory.getCurrentSession();
		Application application = (Application) session.load(Application.class, applicationId);
		if (null != application) {
			session.delete(application);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Application getApplication(String applicationName, String version, Long userId) {
		Session session = this.sessionFactory.getCurrentSession();
		Query<Application> query = session.createQuery("from Application application where "
				+ "application.applicationName=?1 and application.version=?2 and application.userId=?3");
		query.setParameter(1, applicationName);
		query.setParameter(2, version);
		query.setParameter(3, userId);
		
		Application application;
		try {
			application = query.getSingleResult();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return application;
	}

}
