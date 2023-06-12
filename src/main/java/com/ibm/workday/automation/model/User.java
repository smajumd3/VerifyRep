package com.ibm.workday.automation.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="USER")
public class User {
	
	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="userAccess")
	private Boolean userAccess;
	
	@Column(name="admin")
	private Boolean admin;
	
	@Column(name="superUser")
	private Boolean superUser;
	
	@Column(name="client")
	private String client;

	@Column(name="userName")
	private String userName; 

	@Column(name="userEmail")
	private String userEmail;
	
	@Column(name="userPassword")
	private String userPassword;
	
	@Column(name="createTime")
	private Date createTime;
	
	@Column(name="currentLoginTime")
	private Date currentLoginTime;
	
	@Column(name="lastLoginTime")
	private Date lastLoginTime;
	
	@Column(name="accessExpireDate")
	private Date accessExpireDate;

	public User() {
		super();
		admin = false;
		superUser = false;
		userAccess = false;
	}

	public User(Long id, Boolean userAccess, Boolean admin, Boolean superUser, String client, String userName,
			String userEmail, String userPassword, Date createTime, Date currentLoginTime, Date lastLoginTime,
			Date accessExpireDate) {
		super();
		this.id = id;
		this.userAccess = userAccess;
		this.admin = admin;
		this.superUser = superUser;
		this.client = client;
		this.userName = userName;
		this.userEmail = userEmail;
		this.userPassword = userPassword;
		this.createTime = createTime;
		this.currentLoginTime = currentLoginTime;
		this.lastLoginTime = lastLoginTime;
		this.accessExpireDate = accessExpireDate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getUserAccess() {
		return userAccess;
	}

	public void setUserAccess(Boolean userAccess) {
		this.userAccess = userAccess;
	}

	public Boolean getAdmin() {
		return admin;
	}

	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}

	public Boolean getSuperUser() {
		return superUser;
	}

	public void setSuperUser(Boolean superUser) {
		this.superUser = superUser;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getCurrentLoginTime() {
		return currentLoginTime;
	}

	public void setCurrentLoginTime(Date currentLoginTime) {
		this.currentLoginTime = currentLoginTime;
	}

	public Date getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public Date getAccessExpireDate() {
		return accessExpireDate;
	}

	public void setAccessExpireDate(Date accessExpireDate) {
		this.accessExpireDate = accessExpireDate;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", userAccess=" + userAccess + ", admin=" + admin + ", superUser=" + superUser
				+ ", client=" + client + ", userName=" + userName + ", userEmail=" + userEmail + ", userPassword="
				+ userPassword + ", createTime=" + createTime + ", currentLoginTime=" + currentLoginTime
				+ ", lastLoginTime=" + lastLoginTime + ", accessExpireDate=" + accessExpireDate + "]";
	}

}
