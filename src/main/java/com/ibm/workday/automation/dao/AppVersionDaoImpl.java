package com.ibm.workday.automation.dao;

import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ibm.workday.automation.model.AppVersion;

@Repository
public class AppVersionDaoImpl implements AppVersionDao {

	@Autowired
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sf) {
		this.sessionFactory = sf;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<AppVersion> getVersionList() {
		Session session = this.sessionFactory.getCurrentSession();
		List<AppVersion>  versionList = session.createQuery("from AppVersion").list();
		return versionList;
	}

	@Override
	public AppVersion getVersion(Long id) {
		Session session = this.sessionFactory.getCurrentSession();
		AppVersion appVersion = (AppVersion) session.get(AppVersion.class, id);
		return appVersion;
	}

	@SuppressWarnings("unchecked")
	@Override
	public AppVersion getVersionByClient(String client) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<AppVersion> query = session.createQuery("from AppVersion appVersion where appVersion.client=?1");
		query.setParameter(1, client);
		
		AppVersion appVersion;
		try {
			appVersion = query.getSingleResult();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return appVersion;
	}

	@Override
	public void addAppversion(AppVersion appversion) {
		Session session = this.sessionFactory.getCurrentSession();
		session.save(appversion);
		session.flush();
	}

	@Override
	public void updateAppversion(AppVersion appversion) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(appversion);
		session.flush();
	}	

	@Override
	public void deleteAppversion(Long id) {
		Session session = this.sessionFactory.getCurrentSession();
		AppVersion appVersion = (AppVersion) session.load(AppVersion.class, id);
		if (null != appVersion) {
			session.delete(appVersion);
		}	
	}

}
