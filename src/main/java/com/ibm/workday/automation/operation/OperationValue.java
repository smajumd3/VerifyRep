package com.ibm.workday.automation.operation;

import org.springframework.stereotype.Component;

@Component
public class OperationValue {
	
	private Long operationId;
	private String operationName;
	private String responsePath;
	private String ruleName;
	private String applicationName;
	private String applicationVersion;
	
	public OperationValue() {
		super();
	}

	public OperationValue(Long operationId, String operationName, String responsePath, String ruleName,
			String applicationName, String applicationVersion) {
		super();
		this.operationId = operationId;
		this.operationName = operationName;
		this.responsePath = responsePath;
		this.ruleName = ruleName;
		this.applicationName = applicationName;
		this.applicationVersion = applicationVersion;
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

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getApplicationVersion() {
		return applicationVersion;
	}

	public void setApplicationVersion(String applicationVersion) {
		this.applicationVersion = applicationVersion;
	}

	@Override
	public String toString() {
		return "OperationVaule [operationId=" + operationId + ", operationName=" + operationName + ", responsePath="
				+ responsePath + ", ruleName=" + ruleName + ", applicationName=" + applicationName
				+ ", applicationVersion=" + applicationVersion + "]";
	}

}
