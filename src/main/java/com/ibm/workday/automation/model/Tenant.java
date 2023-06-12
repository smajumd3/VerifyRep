package com.ibm.workday.automation.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TENANT")
public class Tenant {
	
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;

	@Column(name = "tenantName")
	private String tenantName;

	@Column(name = "tenantDataCenter")
	private String tenantDataCenter;
	
	@Column(name = "tenantUrl")
	private String tenantUrl;

	@Column(name = "tenantUser")
	private String tenantUser;
	
	@Column(name = "tenantUserPassword")
	private String tenantUserPassword;
	
	@Column(name = "userId")
	private long userId;
	
	@Column(name = "client")
	private String client;	

	public Tenant() {
		super();
	}

	public Tenant(Long id, String tenantName, String tenantDataCenter, String tenantUrl, String tenantUser,
			String tenantUserPassword, long userId, String client) {
		super();
		this.id = id;
		this.tenantName = tenantName;
		this.tenantDataCenter = tenantDataCenter;
		this.tenantUrl = tenantUrl;
		this.tenantUser = tenantUser;
		this.tenantUserPassword = tenantUserPassword;
		this.userId = userId;
		this.client = client;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	public String getTenantDataCenter() {
		return tenantDataCenter;
	}

	public void setTenantDataCenter(String tenantDataCenter) {
		this.tenantDataCenter = tenantDataCenter;
	}

	public String getTenantUrl() {
		return tenantUrl;
	}

	public void setTenantUrl(String tenantUrl) {
		this.tenantUrl = tenantUrl;
	}

	public String getTenantUser() {
		return tenantUser;
	}

	public void setTenantUser(String tenantUser) {
		this.tenantUser = tenantUser;
	}

	public String getTenantUserPassword() {
		return tenantUserPassword;
	}

	public void setTenantUserPassword(String tenantUserPassword) {
		this.tenantUserPassword = tenantUserPassword;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	@Override
	public String toString() {
		return "Tenant [id=" + id + ", tenantName=" + tenantName + ", tenantDataCenter=" + tenantDataCenter
				+ ", tenantUrl=" + tenantUrl + ", tenantUser=" + tenantUser + ", tenantUserPassword="
				+ tenantUserPassword + ", userId=" + userId + ", client=" + client + "]";
	}

}
