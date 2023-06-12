package com.ibm.workday.automation.controller;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.workday.automation.common.CommonConstants;
import com.ibm.workday.automation.model.Client;
import com.ibm.workday.automation.service.ClientService;

@RestController
public class ClientController implements CommonConstants {

	@Autowired
	ClientService clientService;
	
	@RequestMapping(value = "/getAllClients", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<Client> getAllClients() {
		List<Client> listOfClients = clientService.getClientList();
		return listOfClients;
	}
	
	@RequestMapping(value = "/getClient/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
	public Client getClientById(@PathVariable("id") Long id) {
		return clientService.getClient(id);
	}
	
	@RequestMapping(value = "/addClient/{date}", method = RequestMethod.POST, headers = "Accept=application/json")
	public void addClient(@RequestBody Client client, @PathVariable("date") Long date) {
		Date d = new Date(date);
		client.setClientExpirationDate(d);
		clientService.addClient(client);
	}
	
	@RequestMapping(value = "/deleteClient/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public void deleteClient(@PathVariable("id") Long id) {
		clientService.deleteClient(id);
	}
	
}
