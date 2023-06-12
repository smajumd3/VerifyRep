package com.ibm.workday.automation.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ibm.workday.automation.model.Client;

@Repository
public class ClientDaoImpl implements ClientDao {

	@Autowired
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sf) {
		this.sessionFactory = sf;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Client> getClientList() {
		Session session = this.sessionFactory.getCurrentSession();
		List<Client>  clientList = session.createQuery("from Client").list();
		return clientList;
	}
	
	@Override
	public Client getClient(Long id) {
		Session session = this.sessionFactory.getCurrentSession();
		Client client = (Client) session.get(Client.class, id);
		return client;
	}
	
	@Override
	public void addClient(Client client) {
		Session session = this.sessionFactory.getCurrentSession();
		session.save(client);
		session.flush();
	}
	
	@Override
	public void deleteClient(Long id) {
		Session session = this.sessionFactory.getCurrentSession();
		Client client = (Client) session.load(Client.class, id);
		if (null != client) {
			session.delete(client);
		}
	}
	
}
