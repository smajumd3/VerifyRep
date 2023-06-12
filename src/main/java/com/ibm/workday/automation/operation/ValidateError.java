package com.ibm.workday.automation.operation;

public class ValidateError {
	
	boolean isError;
	
	String errorMsg;

	public ValidateError() {
		super();
	}

	public ValidateError(boolean isError, String errorMsg) {
		super();
		this.isError = isError;
		this.errorMsg = errorMsg;
	}

	public boolean isError() {
		return isError;
	}

	public void setError(boolean isError) {
		this.isError = isError;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

}
