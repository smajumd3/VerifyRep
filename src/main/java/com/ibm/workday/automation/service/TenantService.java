package com.ibm.workday.automation.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.workday.automation.dao.TenantDao;
import com.ibm.workday.automation.model.Tenant;

@Service("TenantService")
public class TenantService {

	@Autowired
	TenantDao tenantDao;

	@Transactional
	public List<Tenant> getTenantList() {
		return tenantDao.getTenantList();
	}
	
	@Transactional
	public List<Tenant> getTenantListByUser(Long userId, String client) {
		return tenantDao.getTenantListByUser(userId, client);
	}
	
	@Transactional
	public List<Tenant> getTenantListByClient(String client, Long userId) {
		return tenantDao.getTenantListByClient(client, userId);
	}	

	@Transactional
	public Tenant getTenant(Long id) {
		return tenantDao.getTenant(id);
	}

	@Transactional
	public void addTenant(Tenant tenant) {
		tenantDao.addTenant(tenant);
	}

	@Transactional
	public void updateTenant(Tenant tenant) {
		tenantDao.updateTenant(tenant);
	}

	@Transactional
	public void deleteTenant(Long id) {
		tenantDao.deleteTenant(id);
	}

}
