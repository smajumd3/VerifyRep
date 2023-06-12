package com.ibm.workday.automation.dao;

import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ibm.workday.automation.model.GetRequest;

@Repository
public class GetRequestDaoImpl implements GetRequestDao {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sf) {
		this.sessionFactory = sf;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<GetRequest> getRequestList() {
		Session session = this.sessionFactory.getCurrentSession();
		List<GetRequest> getRequestList = session.createQuery("from GetRequest").list();
		return getRequestList;
	}

	@Override
	public GetRequest getRequestId(Long getRequestId) {
		Session session = this.sessionFactory.getCurrentSession();
		GetRequest getRequest = (GetRequest) session.get(GetRequest.class, getRequestId);
		return getRequest;
	}

	@Override
	@SuppressWarnings("unchecked")
	public GetRequest getRequestName(String requestName) {
		Session session = this.sessionFactory.getCurrentSession();		
		Query<GetRequest> query = session.createQuery("from GetRequest getRequest where getRequest.requestName=?1");
		query.setParameter(1, requestName);		
		GetRequest getRequest;
		try 
		{
			getRequest = query.getSingleResult();
		}
		catch(NoResultException ne)
		{
			return null;
		}		
		return getRequest;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public GetRequest getRequestByClient(String client) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<GetRequest> query = session.createQuery("from GetRequest getRequest where getRequest.client=?1");
		query.setParameter(1, client);
		
		GetRequest request;
		try {
			request = query.getSingleResult();
		}catch(NoResultException ne) {
			
			return null;
		}
		
		return request;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<GetRequest> getRequestsByClient(String client) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<GetRequest> query = session.createQuery("from GetRequest getRequest where getRequest.client=?1");
		query.setParameter(1, client);
		
		List<GetRequest> requests;
		try {
			requests = query.getResultList();
		}catch(NoResultException ne) {
			
			return null;
		}
		
		return requests;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public GetRequest getRequestByReqClient(String requestName, String client) {
		
		Session session = this.sessionFactory.getCurrentSession();		
		Query<GetRequest> query = session.createQuery("from GetRequest getRequest where getRequest.requestName=?1 and getRequest.client=?2");
		query.setParameter(1, requestName);
		query.setParameter(2, client);		
		GetRequest request;
		try 
		{
			request = query.getSingleResult();
		}
		catch(NoResultException ne)
		{
			return null;
		}		
		return request;
	}

	@Override
	public void addGetRequest(GetRequest getRequest) {
		Session session = this.sessionFactory.getCurrentSession();
		session.save(getRequest);
		session.flush();		
	}

	@Override
	public void updateGetRequest(GetRequest getRequest) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(getRequest);
		session.flush();		
	}

	@Override
	public void deleteGetRequest(Long getRequestId) {
		Session session = this.sessionFactory.getCurrentSession();
		GetRequest getRequest = (GetRequest) session.load(GetRequest.class, getRequestId);
		if (null != getRequest) {
			session.delete(getRequest);
		}			
	}

	
}
