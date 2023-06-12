package com.ibm.workday.automation.dao;

import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ibm.workday.automation.model.ExclusionReference;

@Repository
public class ExclusionReferenceDaoImpl implements ExclusionReferenceDao {

	@Autowired
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sf) {
		this.sessionFactory = sf;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ExclusionReference> getReferenceList() {
		Session session = this.sessionFactory.getCurrentSession();
		List<ExclusionReference> refList = session.createQuery("from ExclusionReference").list();
		return refList;
	}

	@Override
	public ExclusionReference getReferenceById(Long refId) {
		Session session = this.sessionFactory.getCurrentSession();
		ExclusionReference exclusionReference = (ExclusionReference) session.get(ExclusionReference.class, refId);
		return exclusionReference;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ExclusionReference getReferenceByClient(String client) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<ExclusionReference> query = session.createQuery("from ExclusionReference er where er.client=?1");
		query.setParameter(1, client);
		
		ExclusionReference exclusionReference;
		try {
			exclusionReference = query.getSingleResult();
		}catch(NoResultException ne) {
			return null;
		}
		
		return exclusionReference;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ExclusionReference> getReferencesByClient(String client) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<ExclusionReference> query = session.createQuery("from ExclusionReference er where er.client=?1");
		query.setParameter(1, client);
		
		List<ExclusionReference> refLists;
		try {
			refLists = query.getResultList();
		}catch(NoResultException ne) {
			return null;
		}
		
		return refLists;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ExclusionReference getReferencesByRefNameClient(String refName, String client) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<ExclusionReference> query = session.createQuery("from ExclusionReference er where er.exclusionRefName=?1 and er.client=?2");
		query.setParameter(1, refName);
		query.setParameter(2, client);
		
		ExclusionReference ref;
		try {
			ref = query.getSingleResult();
		}catch(NoResultException ne) {
			return null;
		}
		
		return ref;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ExclusionReference getReferencesByRefName(String refName) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<ExclusionReference> query = session.createQuery("from ExclusionReference er where er.exclusionRefName=?1");
		query.setParameter(1, refName);
		
		ExclusionReference ref;
		try {
			ref = query.getSingleResult();
		}catch(NoResultException ne) {
			return null;
		}
		
		return ref;
	}

	@Override
	public void addReference(ExclusionReference exclusionReference) {
		Session session = this.sessionFactory.getCurrentSession();
		session.save(exclusionReference);
		session.flush();		
	}
	
	@Override
	public void updateReference(ExclusionReference exclusionReference) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(exclusionReference);
		session.flush();
	}

	@Override
	public void deleteReference(Long refId) {
		Session session = this.sessionFactory.getCurrentSession();
		ExclusionReference ref = (ExclusionReference) session.load(ExclusionReference.class, refId);
		if (null != ref) {
			session.delete(ref);
		}
	}	

}
