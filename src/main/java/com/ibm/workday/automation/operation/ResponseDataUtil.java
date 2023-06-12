package com.ibm.workday.automation.operation;

public class ResponseDataUtil {
	
	private int totalBatches;
	private int currentRunningBatch;
	private int totalRecords;
	private int totalSuccess;
	private int totalFailures;
	private int successCount;
	private int failureCount;
	private int batchCount;
	private int runningbatchNumber;
	private int totalSoapExecutionPending;

	public ResponseDataUtil() {
		super();
	}

	public int getTotalBatches() {
		return totalBatches;
	}

	public void setTotalBatches(int totalBatches) {
		this.totalBatches = totalBatches;
	}

	public int getCurrentRunningBatch() {
		return currentRunningBatch;
	}

	public int getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}

	public void setCurrentRunningBatch(int currentRunningBatch) {
		this.currentRunningBatch = currentRunningBatch;
	}

	public int getTotalSuccess() {
		return totalSuccess;
	}

	public void setTotalSuccess(int totalSuccess) {
		this.totalSuccess = totalSuccess;
	}

	public int getTotalFailures() {
		return totalFailures;
	}

	public void setTotalFailures(int totalFailures) {
		this.totalFailures = totalFailures;
	}
	
	public void incrementSuccessCount() {
		this.successCount++;
	}

	public int getSuccessCount() {
		return successCount;
	}

	public void setSuccessCount(int successCount) {
		this.successCount = successCount;
	}
	
	public void incrementFailureCount() {
		this.failureCount++;
	}

	public int getFailureCount() {
		return failureCount;
	}

	public void setFailureCount(int failureCount) {
		this.failureCount = failureCount;
	}

	public int getBatchCount() {
		return batchCount;
	}

	public void setBatchCount(int batchCount) {
		this.batchCount = batchCount;
	}

	public int getRunningbatchNumber() {
		return runningbatchNumber;
	}

	public void setRunningbatchNumber(int runningbatchNumber) {
		this.runningbatchNumber = runningbatchNumber;
	}
	
	public int getTotalSoapExecutionPending() {
		return totalSoapExecutionPending;
	}

	public void setTotalSoapExecutionPending(int totalSoapExecutionPending) {
		this.totalSoapExecutionPending = totalSoapExecutionPending;
	}
	
	public void decrementSoapExecutionPendingCount() {
		if(this.totalSoapExecutionPending > 0) {
			this.totalSoapExecutionPending--;
		}
	}

	public boolean isAllComplete() {
		return (totalSoapExecutionPending == 0) ;
	}

	@Override
	public String toString() {
		return "ResponseDataUtil [totalBatches=" + totalBatches + ", currentRunningBatch=" + currentRunningBatch
				+ ", totalRecords=" + totalRecords + ", totalSuccess=" + totalSuccess + ", totalFailures="
				+ totalFailures + ", successCount=" + successCount + ", failureCount=" + failureCount + ", batchCount="
				+ batchCount + ", runningbatchNumber=" + runningbatchNumber + ", totalSoapExecutionPending="
				+ totalSoapExecutionPending + "]";
	}

}
