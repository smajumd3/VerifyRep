package com.ibm.workday.automation.dao;

import java.util.List;

import com.ibm.workday.automation.model.Client;

public interface ClientDao {
	List<Client> getClientList();
	Client getClient(Long id);
	void addClient(Client client);
	void deleteClient(Long id);
}
