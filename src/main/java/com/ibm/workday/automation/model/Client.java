package com.ibm.workday.automation.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="CLIENT")
public class Client {

	@Id
	@Column(name="clietnId")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long clietnId;
	
	@Column(name="clientName")
	private String clientName;
	
	@Column(name="clientExpirationDate")
	private Date clientExpirationDate;	

	public Client() {
		super();
	}

	public Client(Long clietnId, String clientName, Date clientExpirationDate) {
		super();
		this.clietnId = clietnId;
		this.clientName = clientName;
		this.clientExpirationDate = clientExpirationDate;
	}

	public Long getClietnId() {
		return clietnId;
	}

	public void setClietnId(Long clietnId) {
		this.clietnId = clietnId;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public Date getClientExpirationDate() {
		return clientExpirationDate;
	}

	public void setClientExpirationDate(Date clientExpirationDate) {
		this.clientExpirationDate = clientExpirationDate;
	}

	@Override
	public String toString() {
		return "Client [clietnId=" + clietnId + ", clientName=" + clientName + ", clientExpirationDate=" + clientExpirationDate
				+ "]";
	}	
	
}
