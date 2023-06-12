package com.ibm.workday.automation.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ibm.workday.automation.model.Result;

@Repository
public class ResultDaoImpl implements ResultDao {

	@Autowired
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sf) {
		this.sessionFactory = sf;
	}
	
	@Override
	public Result getResult(Long resultId) {
		Session session = this.sessionFactory.getCurrentSession();
		Result result = (Result) session.get(Result.class, resultId);
		return result;
	}

	@Override
	public void addResult(Result result) {
		Session session = this.sessionFactory.getCurrentSession();
		session.save(result);
		session.flush();
	}

	@Override
	public void updateResult(Result result) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(result);
		session.flush();
	}

	@Override
	public void deleteResult(Long resultId) {
		Session session = this.sessionFactory.getCurrentSession();
		Result result = (Result) session.load(Result.class, resultId);
		if (null != result) {
			session.delete(result);
		}
	}

}
