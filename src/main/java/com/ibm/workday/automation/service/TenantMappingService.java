package com.ibm.workday.automation.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.workday.automation.dao.TenantMappingDao;
import com.ibm.workday.automation.model.TenantMapping;

@Service("TenantMappingService")
public class TenantMappingService {
	
	@Autowired
	TenantMappingDao tenantMappingDao;

	@Transactional
	public List<TenantMapping> getTenantMappingList() {
		return tenantMappingDao.getTenantMappingList();
	}
	
	@Transactional
	public TenantMapping getTenantMapping(Long id) {
		return tenantMappingDao.getTenantMapping(id);
	}
	
	@Transactional
	public List<TenantMapping> getTenantMappingListByClient(String client) {
		return tenantMappingDao.getTenantMappingByClient(client);
	}
	
	@Transactional
	public TenantMapping getTenantMappingByPageClient(Integer pageIndex, String client) {
		return tenantMappingDao.getTenantMappingByPageClient(pageIndex, client);
	}
	
	@Transactional
	public void addTenantMapping(TenantMapping tenantMapping) {
		tenantMappingDao.addTenantMapping(tenantMapping);
	}
	
	@Transactional
	public void updateTenantMapping(TenantMapping tenantMapping) {
		tenantMappingDao.updateTenantMapping(tenantMapping);
	}
	
	@Transactional
	public void deleteTenantMapping(Long id) {
		tenantMappingDao.deleteTenantMapping(id);
	}

}
