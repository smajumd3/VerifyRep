package com.ibm.workday.automation.dao;

import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ibm.workday.automation.model.OperationDir;

@Repository
public class OperationDirDaoImpl implements OperationDirDao {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sf) {
		this.sessionFactory = sf;
	}	

	@SuppressWarnings("unchecked")
	@Override
	public List<OperationDir> getOperationDirList() {
		Session session = this.sessionFactory.getCurrentSession();
		List<OperationDir>  operationDirList = session.createQuery("from OperationDir").list();
		return operationDirList;
	}

	@Override
	public OperationDir getOperationDir(Long id) {
		Session session = this.sessionFactory.getCurrentSession();
		OperationDir operationDir = (OperationDir) session.get(OperationDir.class, id);
		return operationDir;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String getApplicationForOperarion(String operation) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<OperationDir> query = session.createQuery("from OperationDir opDir where opDir.operationName=?1");
		query.setParameter(1, operation);
		
		OperationDir opDir;
		try {
			opDir = query.getSingleResult();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return opDir.getApplicationName();
		
	}

	@Override
	public void addOperationDir(OperationDir operationDir) {
		Session session = this.sessionFactory.getCurrentSession();
		session.save(operationDir);
		session.flush();		
	}

	@Override
	public void updateOperationDir(OperationDir operationDir) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(operationDir);
		session.flush();		
	}

	@Override
	public void deleteOperationDir(Long id) {
		Session session = this.sessionFactory.getCurrentSession();
		OperationDir operationDir = (OperationDir) session.load(OperationDir.class, id);
		if (null != operationDir) {
			session.delete(operationDir);
		}		
	}

}
