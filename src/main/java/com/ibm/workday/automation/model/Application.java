package com.ibm.workday.automation.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "APPLICATION")
public class Application {
	
	@Id
	@Column(name="applicationId")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long applicationId;
	
	@Column(name="applicationName")
	private String applicationName;
	
	@Column(name="version")
	private String version;
	
	@JsonManagedReference
	@OneToMany(mappedBy = "application", orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<Operation> operations;
	
	@Lob
	@Column (name="wsdlFileData", length=10485760 )
	private byte[] wsdlFileData;
	
	@Column(name = "userId")
	private long userId;

	public Application() {
		super();
	}

	public Application(Long applicationId, String applicationName, String version, List<Operation> operations,
			byte[] wsdlFileData, long userId) {
		super();
		this.applicationId = applicationId;
		this.applicationName = applicationName;
		this.version = version;
		this.operations = operations;
		this.wsdlFileData = wsdlFileData;
		this.userId = userId;
	}

	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<Operation> getOperations() {
		return operations;
	}

	public void setOperations(List<Operation> operations) {
		this.operations = operations;
	}

	public byte[] getWsdlFileData() {
		return wsdlFileData;
	}

	public void setWsdlFileData(byte[] wsdlFileData) {
		this.wsdlFileData = wsdlFileData;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "Application [applicationId=" + applicationId + ", applicationName=" + applicationName + ", version="
				+ version + ", userId=" + userId + "]";
	}

}
