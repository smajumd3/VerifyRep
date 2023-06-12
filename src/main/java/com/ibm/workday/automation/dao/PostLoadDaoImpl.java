package com.ibm.workday.automation.dao;

import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ibm.workday.automation.model.PostLoad;

@Repository
public class PostLoadDaoImpl implements PostLoadDao {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sf) {
		this.sessionFactory = sf;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PostLoad> getPostLoadList() {
		Session session = this.sessionFactory.getCurrentSession();
		List<PostLoad> postLoadList = session.createQuery("from PostLoad").list();
		return postLoadList;
	}

	@Override
	public PostLoad getPostLoad(Long postLoadId) {
		Session session = this.sessionFactory.getCurrentSession();
		PostLoad postLoad = (PostLoad) session.get(PostLoad.class, postLoadId);
		return postLoad;
	}
	
	@Override
	public PostLoad getPostLoad(String ruleName) {
		Session session = this.sessionFactory.getCurrentSession();
		PostLoad postLoad = (PostLoad) session.get(PostLoad.class, ruleName);
		return postLoad;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public PostLoad getPostLoadByLoadRule(String loadCycle, String ruleName) {
		
		Session session = this.sessionFactory.getCurrentSession();		
		Query<PostLoad> query = session.createQuery("from PostLoad postLoad where postLoad.loadCycle=?1 and postLoad.ruleName=?2");
		query.setParameter(1, loadCycle);
		query.setParameter(2, ruleName);		
		PostLoad postLoad;
		try 
		{
			postLoad = query.getSingleResult();
		}
		catch(NoResultException ne)
		{
			return null;
		}		
		return postLoad;
	}

	@Override
	public void addPostLoad(PostLoad postLoad) {
		Session session = this.sessionFactory.getCurrentSession();
		session.save(postLoad);
		session.flush();
	}

	@Override
	public void updatePostLoad(PostLoad postLoad) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(postLoad);
		session.flush();
	}

	@Override
	public void deletePostLoad(Long postLoadId) {
		Session session = this.sessionFactory.getCurrentSession();
		PostLoad postLoad = (PostLoad) session.load(PostLoad.class, postLoadId);
		if (null != postLoad) {
			session.delete(postLoad);
		}	
	}

	@Override
	@SuppressWarnings("unchecked")
	public PostLoad getPostLoadByClient(String client) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<PostLoad> query = session.createQuery("from PostLoad postLoad where postLoad.client=?1");
		query.setParameter(1, client);
		
		PostLoad postLoad;
		try {
			postLoad = query.getSingleResult();
		}catch(NoResultException ne) {
			
			return null;
		}
		
		return postLoad;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<PostLoad> getPostLoadsByClient(String client) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<PostLoad> query = session.createQuery("from PostLoad postLoad where postLoad.client=?1");
		query.setParameter(1, client);
		
		List<PostLoad> postLoads;
		try {
			postLoads = query.getResultList();
		}catch(NoResultException ne) {
			
			return null;
		}
		
		return postLoads;
	}

	@Override
	@SuppressWarnings("unchecked")
	public PostLoad getPostLoadByLoadRuleClient(String loadCycle, String ruleName, String client) {
		Session session = this.sessionFactory.getCurrentSession();		
		Query<PostLoad> query = session.createQuery("from PostLoad postLoad where postLoad.loadCycle=?1 and postLoad.ruleName=?2 and postLoad.client=?3");
		query.setParameter(1, loadCycle);
		query.setParameter(2, ruleName);
		query.setParameter(3, client);
		PostLoad postLoad;
		try 
		{
			postLoad = query.getSingleResult();
		}
		catch(NoResultException ne)
		{
			return null;
		}		
		return postLoad;
	}

}
