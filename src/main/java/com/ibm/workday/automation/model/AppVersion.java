package com.ibm.workday.automation.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="APPVERSION")
public class AppVersion {
	
	@Id
	@Column(name="versionId")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long versionId;
	
	@Column(name="version")
	private String version;
	
	@Column(name = "client")
	private String client;

	public AppVersion() {
		super();
	}

	protected AppVersion(Long versionId, String version, String client) {
		super();
		this.versionId = versionId;
		this.version = version;
		this.client = client;
	}

	public Long getVersionId() {
		return versionId;
	}

	public void setVersionId(Long versionId) {
		this.versionId = versionId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	@Override
	public String toString() {
		return "AppVersion [versionId=" + versionId + ", version=" + version + ", client=" + client + "]";
	}

}
