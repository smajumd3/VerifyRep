package com.ibm.workday.automation.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="EXCLUSIONREFERENCE")
public class ExclusionReference {

	@Id
	@Column(name="exclusionRefId")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long exclusionRefId;

	@Column(name = "exclusionRefName")
	private String exclusionRefName;
	
	@Column(name = "client")
	private String client;
	
	public ExclusionReference() {
		super();
	}

	protected ExclusionReference(Long exclusionRefId, String exclusionRefName, String client) {
		super();
		this.exclusionRefId = exclusionRefId;
		this.exclusionRefName = exclusionRefName;
		this.client = client;
	}

	public Long getExclusionRefId() {
		return exclusionRefId;
	}

	public void setExclusionRefId(Long exclusionRefId) {
		this.exclusionRefId = exclusionRefId;
	}

	public String getExclusionRefName() {
		return exclusionRefName;
	}

	public void setExclusionRefName(String exclusionRefName) {
		this.exclusionRefName = exclusionRefName;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	@Override
	public String toString() {
		return "ExclusionReference [exclusionRefId=" + exclusionRefId + ", exclusionRefName=" + exclusionRefName
				+ ", client=" + client + "]";
	}
	
}
