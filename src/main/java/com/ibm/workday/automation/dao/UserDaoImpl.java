package com.ibm.workday.automation.dao;

import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ibm.workday.automation.model.User;

@Repository
public class UserDaoImpl implements UserDao {

	@Autowired
	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sf) {
		this.sessionFactory = sf;
	}
	
	@Override
	public User getUser(Long id) {
		Session session = this.sessionFactory.getCurrentSession();
		User user = session.get(User.class, id);
		return user;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<User> getUserList() {
		Session session = this.sessionFactory.getCurrentSession();
		List<User> userList = session.createQuery("FROM User").list();
		return userList;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<User> getUserListByClient(String client) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<User> query = session.createQuery("from User user where user.client=?1");
		query.setParameter(1, client);
		
		List<User> users;
		try {
			users = query.getResultList();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return users;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<User> getSuperUserList() {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<User> query = session.createQuery("from User user where user.superUser=?1");
		query.setParameter(1, true);
		
		List<User> users;
		try {
			users = query.getResultList();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return users;
	}	
	
	@Override
	@SuppressWarnings("unchecked")
	public List<User> getAdminListByClient(String client) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<User> query = session.createQuery("from User user where user.client=?1 and user.admin=?2");
		query.setParameter(1, client);
		query.setParameter(2, true);
		
		List<User> users;
		try {
			users = query.getResultList();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return users;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<User> getUserListByEmailPassword(String email, String password) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<User> query = session.createQuery("from User user where user.userEmail=?1 and user.userPassword=?2");
		query.setParameter(1, email);
		query.setParameter(2, password);
		
		List<User> users;
		try {
		    users = query.getResultList();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		return users;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public User getUserByEmailClient(String email, String client) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<User> query = session.createQuery("from User user where user.userEmail=?1 and user.client=?2");
		query.setParameter(1, email);
		query.setParameter(2, client);
		
		User user;
		try {
		    user = query.getSingleResult();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return user;
	}
	
	@Override
	public void addUser(User user) {
		Session session = this.sessionFactory.getCurrentSession();
		session.save(user);
		session.flush();
	}

	@Override
	public void updateUser(User user) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(user);
		session.flush();
	}

	@Override
	public void deleteUser(Long id) {
		Session session = this.sessionFactory.getCurrentSession();
		User user = session.load(User.class, id);
		if (null != user) {
			session.delete(user);
		}
	}

}
