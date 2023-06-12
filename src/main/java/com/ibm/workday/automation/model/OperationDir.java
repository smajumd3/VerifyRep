package com.ibm.workday.automation.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "OPERATIONDIR")
public class OperationDir {

	@Id
	@Column(name="operDirId")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long operDirId;
	
	@Column(name="operation")
	private String operationName;
	
	@Column(name="application")
	private String applicationName;

	public OperationDir() {
		super();
	}

	public OperationDir(Long operDirId, String operationName, String applicationName) {
		super();
		this.operDirId = operDirId;
		this.operationName = operationName;
		this.applicationName = applicationName;
	}

	public Long getOperDirId() {
		return operDirId;
	}

	public void setOperDirId(Long operDirId) {
		this.operDirId = operDirId;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	@Override
	public String toString() {
		return "OperationDir [operDirId=" + operDirId + ", operationName=" + operationName + ", applicationName="
				+ applicationName + "]";
	}	
	
}
