package com.ibm.workday.automation.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name="OPERATION")
public class Operation {
	
	@Id
	@Column(name="operationId")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long operationId;
	
	@Column(name="operationName")
	private String operationName;
	
	@Column(name="responsePath")
	private String responsePath;
	
	@JsonBackReference
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name ="applicationId")
	private Application application;
	
	@Column(name = "ruleName")
	private String ruleName;
	
	@Column(name = "userId")
	private Long userId;
	
	@Lob
	@Column (name="ruleFileData", length=10485760 )
	private byte[] ruleFileData;

	public Operation() {
		super();
	}

	public Operation(Long operationId, String operationName, String responsePath, Application application,
			String ruleName, Long userId, byte[] ruleFileData) {
		super();
		this.operationId = operationId;
		this.operationName = operationName;
		this.responsePath = responsePath;
		this.application = application;
		this.ruleName = ruleName;
		this.userId = userId;
		this.ruleFileData = ruleFileData;
	}

	public Long getOperationId() {
		return operationId;
	}

	public void setOperationId(Long operationId) {
		this.operationId = operationId;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public String getResponsePath() {
		return responsePath;
	}

	public void setResponsePath(String responsePath) {
		this.responsePath = responsePath;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public byte[] getRuleFileData() {
		return ruleFileData;
	}

	public void setRuleFileData(byte[] ruleFileData) {
		this.ruleFileData = ruleFileData;
	}

	@Override
	public String toString() {
		return "Operation [operationId=" + operationId + ", operationName=" + operationName + ", responsePath="
				+ responsePath + ", application=" + application + ", ruleName=" + ruleName + ", userId=" + userId
				+ "]";
	}

}
