package com.ibm.workday.automation.dao;

import java.util.List;

import com.ibm.workday.automation.model.TenantMapping;

public interface TenantMappingDao {
	List<TenantMapping> getTenantMappingList();
	TenantMapping getTenantMapping(Long id);
	List<TenantMapping> getTenantMappingByClient(String client);
	TenantMapping getTenantMappingByPageClient(Integer pageIndex, String client);
	void addTenantMapping(TenantMapping tenantMapping);
	void updateTenantMapping(TenantMapping tenantMapping);
	void deleteTenantMapping(Long id);
}
