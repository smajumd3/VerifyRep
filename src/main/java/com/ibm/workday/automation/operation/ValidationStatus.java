package com.ibm.workday.automation.operation;

import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class ValidationStatus {

	private boolean status;
	private Date lastUpdatedDateTime;
	private String severity = "WARNING";

	public boolean getStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public Date getLastUpdatedDateTime() {
		return lastUpdatedDateTime;
	}

	public void setLastUpdatedDateTime(Date lastUpdatedDateTime) {
		this.lastUpdatedDateTime = lastUpdatedDateTime;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

}
