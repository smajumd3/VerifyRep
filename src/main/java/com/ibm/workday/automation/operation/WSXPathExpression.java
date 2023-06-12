package com.ibm.workday.automation.operation;

public class WSXPathExpression {

	private String requestName;
	private String responseExpression;
	private String validationExpression;
	private String faultExpression;

	public String getRequestName() {
		return requestName;
	}

	public void setRequestName(String requestName) {
		this.requestName = requestName;
	}

	public String getResponseExpression() {
		return responseExpression;
	}

	public void setResponseExpression(String responseExpression) {
		this.responseExpression = responseExpression;
	}

	public String getValidationExpression() {
		return validationExpression;
	}

	public void setValidationExpression(String validationExpression) {
		this.validationExpression = validationExpression;
	}

	public String getFaultExpression() {
		return faultExpression;
	}

	public void setFaultExpression(String faultExpression) {
		this.faultExpression = faultExpression;
	}

}
