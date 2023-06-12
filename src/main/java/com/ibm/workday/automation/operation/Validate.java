package com.ibm.workday.automation.operation;

import java.util.ArrayList;
import java.util.List;

public class Validate {
	
	private String fileName;
	private boolean valid;
	private String validString;
	private List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
	private String generalMessage;
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	public String getValidString() {
		return validString;
	}
	public void setValidString(String validString) {
		this.validString = validString;
	}
	public List<ValidationMessage> getMessages() {
		return messages;
	}
	public void setMessages(List<ValidationMessage> messages) {
		this.messages = messages;
	}
	public String getGeneralMessage() {
		return generalMessage;
	}
	public void setGeneralMessage(String generalMessage) {
		this.generalMessage = generalMessage;
	}

	public void addMessage(ValidationMessage message) {
		messages.add(message);
	}

}
