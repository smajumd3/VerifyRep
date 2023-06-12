package com.ibm.workday.automation.model;

import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name="GETREQUEST")
public class GetRequest {
	
	@Id
	@Column(name="getReqId")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long getReqId;

	@Column(name = "requestName")
	private String requestName;
	
	@Column(name = "requestXMLName")
	private String requestXMLName;

	@Lob
	@Column (name="requestXMLContent", length=10485760 )
	private byte[] requestXMLContent;
	
	@Lob
	@Column (name="variableXMLContent", length=10485760 )
	private byte[] variableXMLContent;
	
	@Column(name = "userId")
	private long userId;
	
	@Column(name = "client")
	private String client;

	public GetRequest() {
		super();
	}

	public GetRequest(Long getReqId, String requestName, String requestXMLName, byte[] requestXMLContent, byte[] variableXMLContent, long userId) {
		super();
		this.getReqId = getReqId;
		this.requestName = requestName;
		this.requestXMLName = requestXMLName;
		this.requestXMLContent = requestXMLContent;
		this.variableXMLContent = variableXMLContent;
		this.userId = userId;
	}
	
	public Long getGetReqId() {
		return getReqId;
	}

	public void setGetReqId(Long getReqId) {
		this.getReqId = getReqId;
	}

	public String getRequestName() {
		return requestName;
	}

	public void setRequestName(String requestName) {
		this.requestName = requestName;
	}
	
	public String getRequestXMLName() {
		return requestXMLName;
	}

	public void setRequestXMLName(String requestXMLName) {
		this.requestXMLName = requestXMLName;
	}

	public byte[] getRequestXMLContent() {
		return requestXMLContent;
	}

	public void setRequestXMLContent(byte[] requestXMLContent) {
		this.requestXMLContent = requestXMLContent;
	}

	public byte[] getVariableXMLContent() {
		return variableXMLContent;
	}

	public void setVariableXMLContent(byte[] variableXMLContent) {
		this.variableXMLContent = variableXMLContent;
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
		return "GetRequest [getReqId=" + getReqId + ", requestName=" + requestName + ", requestXMLName="
				+ requestXMLName + ", requestXMLContent=" + Arrays.toString(requestXMLContent) + ", variableXMLContent="
				+ Arrays.toString(variableXMLContent) + ", userId=" + userId + ", client=" + client + "]";
	}




}
