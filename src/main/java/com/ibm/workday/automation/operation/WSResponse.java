package com.ibm.workday.automation.operation;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WSResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	private String name;
	private String result;
	private String message;
	private Date responseDateTime;
	private String responseDateTimeText;
	private Date requestDateTime;
	private String requestDateTimeText;
	private long totalTime;
	private String resultXML;
	private boolean complete;
	private List<String> faultMessages = new ArrayList<>();
	private String faultMessage;
	private int batchNo;
	private String status;
	private boolean running;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getRequestDateTime() {
		return requestDateTime;
	}

	public void setRequestDateTime(Date date) {
		this.requestDateTime = date;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        String strDate = dateFormat.format(date);
        setRequestDateTimeText(strDate);
	}

	public String getRequestDateTimeText() {
		return requestDateTimeText;
	}

	public void setRequestDateTimeText(String requestDateTimeText) {
		this.requestDateTimeText = requestDateTimeText;
	}

	public Date getResponseDateTime() {
		return responseDateTime;
	}

	public void setResponseDateTime(Date responseDateTime) {
		this.responseDateTime = responseDateTime;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        String strDate = dateFormat.format(responseDateTime);
        setResponseDateTimeText(strDate);
	}

	public String getResponseDateTimeText() {
		return responseDateTimeText;
	}

	public void setResponseDateTimeText(String responseDateTimeText) {
		this.responseDateTimeText = responseDateTimeText;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

	public String getResultXML() {
		return resultXML;
	}

	public void setResultXML(String resultXML) {
		this.resultXML = resultXML;
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public List<String> getFaultMessages() {
		return faultMessages;
	}

	public void setFaultMessages(List<String> faultMessages) {
		this.faultMessages = faultMessages;
	}

	public void addFaultMessage(String faultMessage) {
		faultMessages.add(faultMessage);
	}

	public String getFaultMessage() {
		return faultMessage;
	}

	public void setFaultMessage(String faultMessage) {
		this.faultMessage = faultMessage;
	}

	public int getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(int batchNo) {
		this.batchNo = batchNo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

}
