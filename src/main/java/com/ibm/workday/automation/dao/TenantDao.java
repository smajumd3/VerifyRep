package com.ibm.workday.automation.dao;

import java.util.List;

import com.ibm.workday.automation.model.Tenant;

public interface TenantDao {
	List<Tenant> getTenantList();
	
	List<Tenant> getTenantListByUser(Long userId, String client);
	
	List<Tenant> getTenantListByClient(String client, Long userId);

	Tenant getTenant(Long id);

	void addTenant(Tenant tenant);

	void updateTenant(Tenant tenant);

	void deleteTenant(Long id) ;

}
