package com.ibm.workday.automation.dao;

import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ibm.workday.automation.model.Tenant;

@Repository
public class TenantDaoImpl implements TenantDao {
	
	@Autowired
	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sf) {
		this.sessionFactory = sf;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Tenant> getTenantList() {
		Session session = this.sessionFactory.getCurrentSession();
		List<Tenant>  tenantList = session.createQuery("from Tenant").list();
		return tenantList;
	}

	@Override
	public Tenant getTenant(Long id) {
		Session session = this.sessionFactory.getCurrentSession();
		Tenant tenant = (Tenant) session.get(Tenant.class, id);
		return tenant;
	}

	@Override
	public void addTenant(Tenant tenant) {
		Session session = this.sessionFactory.getCurrentSession();
		session.save(tenant);
		session.flush();
	}

	@Override
	public void updateTenant(Tenant tenant) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(tenant);
		session.flush();
	}

	@Override
	public void deleteTenant(Long id) {
		Session session = this.sessionFactory.getCurrentSession();
		Tenant tenant = (Tenant) session.load(Tenant.class, id);
		if (null != tenant) {
			session.delete(tenant);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Tenant> getTenantListByUser(Long userId, String client) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<Tenant> query = session.createQuery("from Tenant tenant where tenant.userId=?1 and tenant.client=?2");
		query.setParameter(1, userId);
		query.setParameter(2, client);
		
		List<Tenant> tenants;
		try {
			tenants = query.getResultList();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return tenants;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Tenant> getTenantListByClient(String client, Long userId) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<Tenant> query = session.createQuery("from Tenant tenant where tenant.client=?1 and tenant.userId=?2");
		query.setParameter(1, client);
		query.setParameter(2, userId);
		
		List<Tenant> tenants;
		try {
			tenants = query.getResultList();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return tenants;
	}	

}
