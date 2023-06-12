package com.ibm.workday.automation.dao;

import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ibm.workday.automation.model.Operation;

@Repository
public class OperationDaoImpl implements OperationDao {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sf) {
		this.sessionFactory = sf;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Operation> getOperationList() {
		Session session = this.sessionFactory.getCurrentSession();
		List<Operation>  operationList = session.createQuery("from Operation").list();
		return operationList;
	}

	@Override
	public Operation getOperation(Long operationId) {
		Session session = this.sessionFactory.getCurrentSession();
		Operation operation = (Operation) session.get(Operation.class, operationId);
		return operation;
	}

	@Override
	public void addOperation(Operation operation) {
		Session session = this.sessionFactory.getCurrentSession();
		session.save(operation);
		session.flush();
	}

	@Override
	public void updateOperation(Operation operation) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(operation);
		session.flush();		
	}

	@Override
	public void deleteOperation(Long operationId) {
		Session session = this.sessionFactory.getCurrentSession();
		Operation operation = (Operation) session.load(Operation.class, operationId);
		if (null != operation) {
			session.delete(operation);
		}		
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Operation> getOperationListByUser(Long userId) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<Operation> query = session.createQuery("from Operation operation where operation.userId=?1");
		query.setParameter(1, userId);
		
		List<Operation> operation;
		try {
			operation = query.getResultList();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return operation;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Operation getOperation(String operationName, Long userId) {
		Session session = this.sessionFactory.getCurrentSession();
		Query<Operation> query = session.createQuery("from Operation operation where operation.operationName=?1 and operation.userId=?2");
		query.setParameter(1, operationName);
		query.setParameter(2, userId);
		
		Operation operation;
		try {
			operation = query.getSingleResult();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return operation;
	}

}
