package com.ibm.workday.automation.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.workday.automation.dao.ClientDao;
import com.ibm.workday.automation.model.Client;

@Service("ClientService")
public class ClientService {
	
	@Autowired
	ClientDao clientDao;
	
	@Transactional
	public List<Client> getClientList() {
		return clientDao.getClientList();
	}
	
	@Transactional
	public Client getClient(Long id) {
		return clientDao.getClient(id);
	}

	@Transactional
	public void addClient(Client client) {
		clientDao.addClient(client);
	}
	
	@Transactional
	public void deleteClient(Long id) {
		clientDao.deleteClient(id);
	}

}
