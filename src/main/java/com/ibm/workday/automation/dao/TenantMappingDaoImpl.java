package com.ibm.workday.automation.dao;

import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ibm.workday.automation.model.TenantMapping;

@Repository
public class TenantMappingDaoImpl implements TenantMappingDao {

	@Autowired
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sf) {
		this.sessionFactory = sf;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<TenantMapping> getTenantMappingList() {
		Session session = this.sessionFactory.getCurrentSession();
		List<TenantMapping>  mappingList = session.createQuery("from TenantMapping").list();
		return mappingList;
	}

	@Override
	public TenantMapping getTenantMapping(Long id) {
		Session session = this.sessionFactory.getCurrentSession();
		TenantMapping mapping = (TenantMapping) session.get(TenantMapping.class, id);
		return mapping;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TenantMapping> getTenantMappingByClient(String client) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<TenantMapping> query = session.createQuery("from TenantMapping tenantMapping where tenantMapping.client=?1");
		query.setParameter(1, client);
		
		List<TenantMapping> mappingList;
		try {
			mappingList = query.getResultList();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return mappingList;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public TenantMapping getTenantMappingByPageClient(Integer pageIndex, String client) {
		Session session = this.sessionFactory.getCurrentSession();
		
		Query<TenantMapping> query = session.createQuery("from TenantMapping tenantMapping where tenantMapping.client=?1 and tenantMapping.pageIndex=?2");
		query.setParameter(1, client);
		query.setParameter(2, pageIndex);
		
		TenantMapping mapping;
		try {
			mapping = query.getSingleResult();
		}catch(NoResultException ne) {
			// suman log here
			return null;
		}
		
		return mapping;
	}	

	@Override
	public void addTenantMapping(TenantMapping tenantMapping) {
		Session session = this.sessionFactory.getCurrentSession();
		session.save(tenantMapping);
		session.flush();
	}

	@Override
	public void updateTenantMapping(TenantMapping tenantMapping) {
		Session session = this.sessionFactory.getCurrentSession();
		session.update(tenantMapping);
		session.flush();
	}

	@Override
	public void deleteTenantMapping(Long id) {
		Session session = this.sessionFactory.getCurrentSession();
		TenantMapping mapping = (TenantMapping) session.load(TenantMapping.class, id);
		if (null != mapping) {
			session.delete(mapping);
		}
	}

}
